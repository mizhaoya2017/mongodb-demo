package com.example.mongodbdemo.service.impl;

import com.example.mongodbdemo.content.CommonContent;
import com.example.mongodbdemo.entity.OperationVersionResourceEntity;
import com.example.mongodbdemo.entity.ResourceHostReplaceEntity;
import com.example.mongodbdemo.entity.ResourceHostReplaceService;
import com.example.mongodbdemo.enums.StorageType;
import com.example.mongodbdemo.service.BaseMultithreadService;
import com.example.mongodbdemo.service.OperationVersionBasicInfoService;
import com.example.mongodbdemo.service.RecordInfoMultiStorageService;
import com.example.mongodbdemo.service.RecordInfoResolverHandler;
import com.example.mongodbdemo.utils.RegExUtils;
import com.example.mongodbdemo.utils.StUtils;
import com.example.mongodbdemo.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/1 16:50
 **/
@Slf4j
@Service
public class RecordInfoResolverHandlerImpl extends BaseMultithreadService implements RecordInfoResolverHandler {

    @Autowired
    private StorageType storageType;
    @Autowired
    private RecordInfoMultiStorageService recordInfoMultiStorageService;

    @Autowired
    private OperationVersionBasicInfoService versionBasicInfoService;

    private static List<ResourceHostReplaceEntity> hostReplaceList = new ArrayList<>();

    @Autowired
    private ResourceHostReplaceService resourceHostReplaceService;

//    @Autowired
//    private OperationVersionResourceDao operationVersionResourceDao;

    /**
     * 任务下载线程数
     */
    private static final Integer DOWNLOAD_THREAD_COUNT = 5;

    @PostConstruct
    public void initHostReplaceList() {
        hostReplaceList = resourceHostReplaceService.getResourceHostReplaceList();

    }

    @Override
    public String process(String recordInfoPlaintext, Integer resourceInfoId, String bucketName, String filePathPrefix, String version) {

        return refresh(recordInfoPlaintext, resourceInfoId, bucketName, filePathPrefix, version);
    }

    /**
     * 下载后返回各自的相对路径
     *
     * @param recordInfoPlaintext
     * @param resourceInfoId
     * @param bucketName
     * @param filePathPrefix
     * @param version
     * @return
     */
    private String refresh(String recordInfoPlaintext, Integer resourceInfoId, String bucketName, String filePathPrefix, String version) {
        String increaseStr = recordInfoPlaintext;
        try {
            // 收集url
            List<String> grabUrls = strUrlResolve(recordInfoPlaintext);
            // url去重
            grabUrls = grabUrls.stream().distinct().collect(Collectors.toList());
            log.info("资源抓取时捕获到的所有去重后的url： {}", grabUrls);
            //要替换recordInfo字符串的所有资源列表，含已下载的和新下载的
            List<OperationVersionResourceEntity> operationVersionResourceList = new ArrayList<>();
            //要新下载的资源列表
            List<OperationVersionResourceEntity> downloadVersionResourceList = new ArrayList<>();
            List<OperationVersionResourceEntity> localResourceList = getLocalVersionResourceList(grabUrls,version);

            // 处理每一个网络url;  grabUrl: 存在带参数的网络url； lastAccessUrl：去掉参数的网络url
            for (String grabUrl : grabUrls) {
                //1.找到已经下载的资源列表，用operationVersionResourceList(本次所有要替换的资源)存起来
                OperationVersionResourceEntity localResourceEntity = findLocalResourceEntity(grabUrl,localResourceList);
                if (localResourceEntity != null) {
                    operationVersionResourceList.add(localResourceEntity);
                    if(localResourceIsCopied(localResourceEntity)){
                        //original_url是复制的
                        localResourceList.add(localResourceEntity);
                    }
                }else {
                    //2.将需要新下载的资源列表放在downloadVersionResourceList(本次新下载的资源)和用operationVersionResourceList(本次所有要替换的资源)存起来
                    // url去参数化
                    String clearParamsUrl = removeUrlParam(grabUrl);
                    //有些url需要预处理，比如换一个域名，如太平财
                    clearParamsUrl = preHandleReplace(clearParamsUrl);
                    // 网络图片资源下载
                    OperationVersionResourceEntity resourceEntity = new OperationVersionResourceEntity();
                    resourceEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    resourceEntity.setVersion(version);
                    resourceEntity.setResourceInfoId(resourceInfoId);
                    resourceEntity.setAccessUrl(clearParamsUrl);
                    resourceEntity.setOriginalUrl(grabUrl);
                    resourceEntity.setBucketName(bucketName);
                    operationVersionResourceList.add(resourceEntity);
                    downloadVersionResourceList.add(resourceEntity);
                }
            }

            //3.按照 DOWNLOAD_THREAD_COUNT 线程数，下载资源
            downloadInMultiThread(downloadVersionResourceList, filePathPrefix);

            //4.最后统一替换资源
            //按照operationVersionResourceList(本次所有要替换的资源)，统一替换资源
            List<OperationVersionResourceEntity> availableList =
                    operationVersionResourceList.stream().parallel()
                            .filter(o -> org.apache.commons.lang.StringUtils.isNotEmpty(o.getLocalUrl()))
                            .collect(Collectors.toList());
            increaseStr = replaceResourceAddress(availableList, recordInfoPlaintext);
            log.info("替换检查:resourceInfoId:{}", resourceInfoId);

        } catch (Exception e) {
            log.error("替换检查,抓取资源过程报错",e);
            return recordInfoPlaintext;
        }
        if (increaseStr.equals(recordInfoPlaintext)) {
            log.info("没有捕获到要下载的url连接...");
            return "";
        }
        return increaseStr;

    }


    private boolean localResourceIsCopied(OperationVersionResourceEntity localResourceEntity) {
        return localResourceEntity.getId() == null;
    }


    List<OperationVersionResourceEntity> getLocalVersionResourceList(List<String> grabUrls,String version){
        List<String> accessUrlList = grabUrls.stream().map(url->{
            // 过滤掉参数
            String accessUrl = removeUrlParam(url);
            return preHandleReplace(accessUrl);
        }).collect(Collectors.toList());

        List<OperationVersionResourceEntity> localResources =
//                operationVersionResourceDao.findAllByVersionAndAccessUrl(version,accessUrlList);
                null;
        return localResources;
    }



    /**
     * 将下载任务分为多线程下载
     *
     * @param downloadResourceList
     * @param filePathPrefix
     * @return
     * @author Logan
     * @date 2020-09-27 13:34
     */
    private void downloadInMultiThread(List<OperationVersionResourceEntity> downloadResourceList, String filePathPrefix) {
        List<List<OperationVersionResourceEntity>> downloadTaskList = devideTask(downloadResourceList);

        CountDownLatch countDownLatch = new CountDownLatch(downloadTaskList.size());
        log.info("分5个线程去处理下载任务...");
        for (List<OperationVersionResourceEntity> taskList : downloadTaskList) {
            threadPool.execute(() -> {
                download(taskList, filePathPrefix, countDownLatch);
            });
        }
        //等待任务处理完成之后,将新下载的资源替换
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("多线程下载资源失败", e);
        }
    }



    private String replaceResourceAddress(List<OperationVersionResourceEntity> resoureList, String recordInfo) {
        String increaseStr = recordInfo;
        log.info("替换检查：{}", resoureList.toString());
        for (OperationVersionResourceEntity operationVersionResource : resoureList) {
            String orginalUrl = operationVersionResource.getOriginalUrl();
            increaseStr = increaseStr.replace(orginalUrl, operationVersionResource.getLocalUrl());
            if (increaseStr.indexOf(orginalUrl) >= 0) {
                log.error("替换检查失败:originalUrl:{}, localUrl:{}", orginalUrl, operationVersionResource.getLocalUrl());
            } else {
                log.info("替换检查成功:originalUrl:{}, localUrl:{}", orginalUrl, operationVersionResource.getLocalUrl());
            }
        }
        return increaseStr;

    }

    /**
     * 将任务拆分成5个线程的任务
     *
     * @param operationVersionResourceList
     * @return
     * @author Logan
     * @date 2020-09-27 12:55
     */
    private List<List<OperationVersionResourceEntity>> devideTask(List<OperationVersionResourceEntity> operationVersionResourceList) {
        //将任务拆分成5个部分，按照5个线程去处理
        List<List<OperationVersionResourceEntity>> downloadTaskList = new ArrayList<>();
        int totalResourceListCount = operationVersionResourceList.size();
        int pos = 0;
        //将总任务按照线程数均分后的商
        int percount = totalResourceListCount /DOWNLOAD_THREAD_COUNT;
        //将总任务数按照线程数均分后的余数，最后要摊到一部分线程上去
        int remained = totalResourceListCount % DOWNLOAD_THREAD_COUNT;

        if(percount >0){
            //1.如果商>0，先按照商均分任务数
            for(int i =0; i< DOWNLOAD_THREAD_COUNT; i ++){
                List<OperationVersionResourceEntity> taskList = new ArrayList<>();
                for(int j=0; j<percount; j++) {
                    taskList.add(operationVersionResourceList.get(pos));
                    pos++;
                }
                downloadTaskList.add(taskList);
            }
        }


        if (remained >0 && downloadTaskList.size() == 0) {
            //2.商=0并且余数>0 需要初始化 让 线程数=余数
            for (int i = 0; i < remained; i++) {
                List<OperationVersionResourceEntity> taskList = new ArrayList<>();
                downloadTaskList.add(taskList);
            }
        }

        for (int i = 0; i <  remained; i++) {
            //3.将余数分配到部分线程任务中去
            downloadTaskList.get(i).add(operationVersionResourceList.get(pos));
            pos++;
        }
        return downloadTaskList;
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);
        list.add(7);
//        list.add(8);
//        list.add(9);
//        list.add(10);
//        list.add(11);

//        List<List<Integer>> downloadTaskList = new ArrayList<>();
//        int totalResourceListCount = list.size();
//        int remained = totalResourceListCount % DOWNLOAD_THREAD_COUNT;
//        int percount = totalResourceListCount /DOWNLOAD_THREAD_COUNT;
//        int pos = 0 ;
//        if(percount >0){
//            for(int i =0; i< DOWNLOAD_THREAD_COUNT; i ++){
//                List<Integer> taskList = new ArrayList<>();
//                for(int j=0; j<percount; j++) {
//                    taskList.add(list.get(pos));
//                    pos++;
//                }
//                downloadTaskList.add(taskList);
//            }
//        }
//        System.out.println(downloadTaskList);
//        if (remained >0 && downloadTaskList.size() == 0) {
//            for (int i = 0; i < remained; i++) {
//                List<Integer> taskList = new ArrayList<>();
//                downloadTaskList.add(taskList);
//            }
//        }
//
//        for (int i = 0; i < downloadTaskList.size() && remained > 0; i++) {
//            downloadTaskList.get(i).add(list.get(pos));
//            pos++;
//            remained--;
//        }
//        System.out.println(downloadTaskList);

//
//        int resourceRemains = totalResourceListCount;
//        int pos = 0;
//        while (resourceRemains > 0) {
//            List<Integer> taskList = new ArrayList<>();
//            float perCountTemp = (float) list.size() / DOWNLOAD_THREAD_COUNT;
//            int perCount = (int) Math.ceil(perCountTemp);
//            for (int i = 0; i < perCount && resourceRemains > 0; i++) {
//                taskList.add(list.get(pos));
//                resourceRemains--;
//                pos++;
//            }
//            System.out.println("pos:" + pos);
//            downloadTaskList.add(taskList);
//        }
//
//        System.out.println(downloadTaskList);

    }

    private void download(List<OperationVersionResourceEntity> resourceEntityList, String filePathPrefix, CountDownLatch countDownLatch) {
        try {
            List<OperationVersionResourceEntity> saveList = new ArrayList<>();
            for (OperationVersionResourceEntity resourceEntity : resourceEntityList) {
                String resourcePath = downloadSource(resourceEntity.getAccessUrl(), filePathPrefix);
                if (org.apache.commons.lang.StringUtils.isNotBlank(resourcePath)) {
                    resourceEntity.setLocalUrl(resourcePath);
                    saveList.add(resourceEntity);
                }
            }
            saveResourceDataList(saveList);
        } finally {
            countDownLatch.countDown();
        }
    }


    /**
     * url比较是否下载过
     *
     * @param grabUrl
     * @param accessUrlExistCollect
     * @param version
     * @return
     */
    private String filterUrl(String grabUrl, List<OperationVersionResourceEntity> accessUrlExistCollect, String version) {
        // 过滤掉参数
        String url = removeUrlParam(grabUrl);
        if (accessUrlExistCollect == null || accessUrlExistCollect.size() == 0) {
            return org.apache.commons.lang.StringUtils.EMPTY;
        }
        String handleReplaceUrl = preHandleReplace(url);
        List<OperationVersionResourceEntity> collect = accessUrlExistCollect.stream()
                .filter(
                        item -> item.getVersion().equals(version)
                                && item.getAccessUrl().equals(handleReplaceUrl))
                .collect(Collectors.toList());
        if (collect.size() > 0) {
            return collect.get(0).getLocalUrl();
        }
        return org.apache.commons.lang.StringUtils.EMPTY;

    }

    /**
     * url比较是否下载过
     *
     * @param grabUrl
     * @return
     */
    private OperationVersionResourceEntity findLocalResourceEntity(String grabUrl,List<OperationVersionResourceEntity> localResourceList) {
        // 过滤掉参数
        String url = removeUrlParam(grabUrl);
        String handleReplaceUrl = preHandleReplace(url);
        // 查询当前版本下当前AccessUrl(handleReplaceUrl) 是否下载过

        if(localResourceList != null && localResourceList.size()>0){
            List<OperationVersionResourceEntity> accessSearchList =
                    localResourceList.stream().filter(res->res.getAccessUrl().equals(handleReplaceUrl)).collect(Collectors.toList());
            if(accessSearchList != null && accessSearchList.size()>0){

                // 找到了合适的accessUrl，直接返回
                return accessSearchList.get(0);
            }
        }
        return null;

    }


    /**
     * 删除url中所有参数
     *
     * @param url
     * @return
     */
    private String removeUrlParam(String url) {
        if (org.apache.commons.lang.StringUtils.isBlank(url) || !url.contains("?")) {
            return url;
        }
        int index = url.indexOf("?");
        return url.substring(0, index);
    }

    /**
     * url抓取记录入库
     *
     * @param originalUrl
     * @param localUrl
     * @return
     */
    private void saveResourceData(String originalUrl, Integer resourceInfoId, String accessUrl, String localUrl, String version, String bucketName) {

        // 保存数据库
        // 1、资源下载相关信息入库(url入库)
        OperationVersionResourceEntity operationVersionResourceEntity = new OperationVersionResourceEntity();
        operationVersionResourceEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        operationVersionResourceEntity.setVersion(version);
        operationVersionResourceEntity.setResourceInfoId(resourceInfoId);
        operationVersionResourceEntity.setAccessUrl(accessUrl);
        operationVersionResourceEntity.setLocalUrl(localUrl);
        operationVersionResourceEntity.setOriginalUrl(originalUrl);

        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case OSS_ALI_STORAGE:
            case NAS_STORAGE:
            case H5IMG_STORAGE:
                operationVersionResourceEntity.setBucketName(bucketName);
                operationVersionResourceEntity.setIsOss(1);
                versionBasicInfoService.saveRecordInfoResource(operationVersionResourceEntity);
                break;
            case MYSQL_STORAGE:
                operationVersionResourceEntity.setIsOss(0);
                versionBasicInfoService.saveRecordInfoResource(operationVersionResourceEntity);
                break;
            default:
                break;
        }
        // 一次任务中 处理一个url资源将 资源url信息保存到线程缓存中，在任务结束时清空线程缓存， 目的： 保证url资源下载不会重复
        OperationVersionResourceEntity resourceEntity = new OperationVersionResourceEntity();
//                .setOriginalUrl(originalUrl)
//                .setAccessUrl(accessUrl)
//                .setLocalUrl(localUrl)
//                .setVersion(version)
//                .setResourceInfoId(resourceInfoId);
        ResourceVersionTemporaryCache(resourceEntity);
        log.info("url解析并保存成功！！！ 原始url：{}， 保存后的url：{}", originalUrl, localUrl);

    }


    /**
     * url抓取记录入库
     *
     * @param OperationVersionResourceEntityList
     * @return
     */
    private void saveResourceDataList(List<OperationVersionResourceEntity> OperationVersionResourceEntityList) {
         if(OperationVersionResourceEntityList == null || OperationVersionResourceEntityList.isEmpty()){
             log.warn("没有可保存的resource的记录");
             return;
         }

        // 保存数据库
        // 1、资源下载相关信息入库(url入库)

        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case OSS_ALI_STORAGE:
            case NAS_STORAGE:
                OperationVersionResourceEntityList.stream().forEach(item -> {
                    item.setIsOss(1);
                });
                versionBasicInfoService.saveRecordInfoResourceList(OperationVersionResourceEntityList);
                break;
            case MYSQL_STORAGE:
                OperationVersionResourceEntityList.stream().forEach(item -> {
                    item.setIsOss(0);
                });
                versionBasicInfoService.saveRecordInfoResourceList(OperationVersionResourceEntityList);
                break;
            default:
                break;
        }

    }


    /**
     * 缓存到线程缓存中
     *
     * @param resourceEntity
     */
    private void ResourceVersionTemporaryCache(OperationVersionResourceEntity resourceEntity) {

        List<OperationVersionResourceEntity> list = new ArrayList<OperationVersionResourceEntity>() {{
            add(resourceEntity);
        }};
    }


    /**
     * 将绝对路径转换为项目可访问路径
     *
     * @param realSavePath
     * @return
     */
    private String pathConvert(String realSavePath) {
        String relativelySavePath = StringUtils.sub(realSavePath, CommonContent.PAGE_UPLOAD);
        // 域名 + \page-resource-upload\2020-08-24\c15c8a11ab0c876f821c6bdf26083333-cd2837d74f674d3e99d3721747ed20a7\fei7837226-article-details-79377906.png
        return relativelySavePath.startsWith("http") ? relativelySavePath : CommonContent.DO_MAIN + relativelySavePath;
    }


    /**
     * url下载文件
     *
     * @param fileUrl  网络url文件(不带参数)
     * @param savePath 本地保存路径
     * @return 返回本地
     */
    private String downloadSource(String fileUrl, String savePath) {

        if (org.apache.commons.lang.StringUtils.isBlank(fileUrl)) {
            log.info("资源下载前置处理... 网络url字符为空！ url不抓取...  url={}", fileUrl);
            return org.apache.commons.lang.StringUtils.EMPTY;
        }
        // 获取url 后缀字符
        int indexSlash = fileUrl.lastIndexOf("/");
        if (indexSlash == (fileUrl.length() - 1)) {
            log.info("资源下载前置处理... url没有文件名后缀，url不抓取， url={}", fileUrl);
            return org.apache.commons.lang.StringUtils.EMPTY;
        }
        String urlSuffixName = fileUrl.substring(indexSlash + 1);

        // 检查是否至少匹配一个url后缀
        boolean anyMatch = CommonContent.URL_SUFFIX.stream().anyMatch(urlKeyword -> urlSuffixName.contains(urlKeyword));
        if (!anyMatch) {
            return org.apache.commons.lang.StringUtils.EMPTY;
        }

        // 获取文件名
        String fileName = getFileName(fileUrl);
        // url资源下载
        Connection.Response response = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            Connection connection = Jsoup.connect(fileUrl);
            response = connection.method(Connection.Method.GET).ignoreContentType(true).timeout(5 * 1000).execute();
            bufferedInputStream = response.bodyStream();
            // 资源处理并保存
            String resource = recordInfoMultiStorageService.saveRecordResource(bufferedInputStream, savePath, fileName);
            log.info("文件保存成功！！！ filePath：{}, fileName: {}", savePath, fileName);
            return resource;
        } catch (Exception e) {
            log.error("资源抓取时url无法访问！ 资源抓取失败!  要下载的url={}", fileUrl,e);
            return org.apache.commons.lang.StringUtils.EMPTY;
        }finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    log.error("下载资源释放inputStream失败，可能调用函数里面已经close了", e);
                }
            }
        }
    }


    /**
     * url地址后缀匹配关键字
     *
     * @param url
     * @param keyWordSuffix
     * @return
     */
    private boolean urlNameSuffixMatch(String url, String keyWordSuffix) {
        int index = url.lastIndexOf(".");
        String fileNameSuffix = url.substring(index + 1);
        return fileNameSuffix.contains(keyWordSuffix);
    }

    /**
     * 文件下载本地
     *
     * @param bufferedInputStream
     * @param savePath
     * @param fileName
     * @return
     */
    private String fileDownLoadToLocal(BufferedInputStream bufferedInputStream, String savePath, String fileName) {

        //一次最多读取1k
        byte[] buffer = new byte[1024];
        //实际读取的长度
        int readLenghth;
        //根据文件保存地址，创建文件输出流
        File filePath = new File(savePath);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        File realFile = null;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            fileName = RegExUtils.matchSpecialChar(fileName);
            realFile = new File(filePath.getAbsolutePath() + "/" + fileName);
            fileOutputStream = new FileOutputStream(realFile);
            //创建的一个写出的缓冲流
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            //文件逐步写入本地
            //先读出来，保存在buffer数组中
            while ((readLenghth = bufferedInputStream.read(buffer, 0, 1024)) != -1) {
                //再从buffer中取出来保存到本地
                bufferedOutputStream.write(buffer, 0, readLenghth);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭缓冲流
                bufferedOutputStream.close();
                fileOutputStream.close();
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return realFile.getAbsolutePath();
    }


    /**
     * 根据url获取资源名称(文件名)
     *
     * @param url
     * @return
     */
    private String getFileName(String url) {
        // 获取url的文件名
        int indexOf = url.lastIndexOf("/");
        String urlFileName = url.substring(indexOf + 1);
        // 获取16位的UUID
        String uuidFileName = StUtils.getFileNameByUUId();
        // 若有文件后缀
        if (urlFileName.contains(".")) {
            // 获取url文件名后缀
            int index = urlFileName.lastIndexOf(".");
            String fileNameSuffix = urlFileName.substring(index);
            return String.format("%s%s", uuidFileName, fileNameSuffix);
        }
        // url没有文件后缀
        return uuidFileName;
    }


    /**
     * 抓取到所有的url
     *
     * @param recordInfoPlaintext
     * @return
     */
    private List<String> strUrlResolve(String recordInfoPlaintext) {
        List<String> list = new ArrayList<>();
        // 根据http截取字符串
        String[] hrefs = recordInfoPlaintext.split("http");
        // 处理每一个url地址
        for (int i = 0; i < hrefs.length; i++) {
            String href = hrefs[i];
            // 每一个请求再拼接
            String s = "http" + href;
            // 字符串过长会出现正则匹配 StackOverflowError -> 方法递归调用撑爆线程内存大小
            if (s.length() > 300) {
                s = s.substring(0, 300);
            }
            List<String> urlMatch = RegExUtils.getUrlMatch(s);
            if (urlMatch != null || urlMatch.size() > 0) {
                list.addAll(urlMatch);
            }
        }
        return list;
    }

    /**
     * 加url预处理，
     * 可能有些网络资源由于部署问题需要将host替换掉才能访问，例如太平财
     *
     * @param url
     * @return
     * @author Logan
     * @date 2020-09-22 20:49
     */
    private String preHandleReplace(String url) {
        Optional<ResourceHostReplaceEntity> optional =
                hostReplaceList.stream().filter(host -> url.indexOf(host.getHostOriginal()) == 0).findAny();
        if (optional.isPresent()) {
            ResourceHostReplaceEntity replaceEntity = optional.get();
            String result = url.replace(replaceEntity.getHostOriginal(), replaceEntity.getHostReplace());
            log.info("url预处理:原始url:{}, 替换后url:{}", url, result);
            return result;
        }
        log.info("url预处理，没有替换， url:{}", url);
        return url;

    }

    /**
    * 搜索当前recordInfo下所有的资源，哪些有内部连接的要再次下载，并且替换
    * @author Logan
    * @date 2020-10-23 20:51
    * @param recordInfoId

    * @return
    */
    private void searchAndReplaceReferLink(Integer recordInfoId){
        List<Integer> resourceIds = getResourceIds(recordInfoId);
        do{
            Integer resId = resourceIds.get(0);
            List<OperationVersionResourceEntity> resReferLinks = downloadAndReplaceResourceReferLink(resId);
            resourceIds.remove(0);
            List<Integer> resIdsNeedSearchReferLink = getResIdNeedReferLink(resReferLinks);
            resourceIds.addAll(resIdsNeedSearchReferLink);
            resourceIds = resourceIds.stream().distinct().collect(Collectors.toList());
        }while(resourceIds != null && resourceIds.size()>0);
    }

    /**
    * 哪些资源id需要抓取子链接,
     * 排除掉referedLinkChecked=true的
    * @author Logan
    * @date 2020-10-23 20:58
    * @param resReferLinks

    * @return
    */
    private List<Integer> getResIdNeedReferLink(List<OperationVersionResourceEntity> resReferLinks) {
        List<Integer> result = /*resReferLinks.stream().filter(
                res ->{return isCssOrJsResource(res.getAccessUrl()) && !res.getReferedLinkChecked();}).
                map(OperationVersionResourceEntity::getId).parallel().collect(Collectors.toList())*/null;
        return result;
    }

    /**
    * 根据url判断资源类型是否为css或者js
    * @author Logan
    * @date 2020-10-24 17:05
    * @param url

    * @return
    */
    private boolean isCssOrJsResource(String url){
      return  url.contains(".js") || url.contains(".css");
    }

    /**
    * 返回未检查连接内容的 resourceId（operation_version_resource的主键）
     * 只包含js，css
    * @author Logan
    * @date 2020-10-23 19:52
    * @param recordInfoId

    * @return
    */
    private List<Integer> getResourceIds(Integer recordInfoId) {
        //查询record_version_resource关联表，js和css的 并且referedLinkChecked=false的 resourceId列表
        return null;
    }

    /**
    * 下载资源文件里面的链接
    * @author Logan
    * @date 2020-10-23 20:03
    * @param resourceId
    
    * @return 
    */       
    private List<OperationVersionResourceEntity> downloadAndReplaceResourceReferLink(Integer resourceId){
        OperationVersionResourceEntity resourceEntity = null;
//        OperationVersionResourceEntity resourceEntity = operationVersionResourceDao.getOne(resourceId);
        if(resourceEntity.getReferedLinkChecked()) {
            return null;
        }
        String accessUrl = resourceEntity.getAccessUrl();
        String localUrl = resourceEntity.getLocalUrl();
        String resourceContent = getResourceContent(localUrl);
        //将资源里面用到的连接下载下来（已经下载过的直接从库里面取地址
        List<OperationVersionResourceEntity> resReferLinkList = searchReferLinks(resourceContent,accessUrl);
        //替换资源文件里面的链接地址
        resourceContent = replaceReferLink(resourceContent,resReferLinkList);
        saveResourceContent(resourceContent,resourceId);
        return resReferLinkList;
    }

    /**
    *  重新保存替换连接资源后的文本内容，
     * 并且将referedLinkChecked置为true
    * @author Logan
    * @date 2020-10-23 20:45
    * @param resourceContent
    * @param resourceId

    * @return
    */
    private void saveResourceContent(String resourceContent, Integer resourceId) {
    }

    /**
    * 将资源内容的连接进行替换
    * @author Logan
    * @date 2020-10-23 21:44
    * @param resourceContent
    * @param resReferLinkList

    * @return
    */
    private String replaceReferLink(String resourceContent, List<OperationVersionResourceEntity> resReferLinkList) {
        String content = resourceContent;
        for(OperationVersionResourceEntity referLink: resReferLinkList){
            if(org.apache.commons.lang.StringUtils.isNotEmpty(referLink.getLocalUrl())){
                content = content.replace(referLink.getOriginalUrl(), referLink.getLocalUrl());
            }
        }
        return content;
    }

    /**
    * 搜索资源文件里面的链接进行下载
     * 多线程下载
    * @author Logan
    * @date 2020-10-23 20:08
    * @param resourceContent
    * @param accessUrl
    
    * @return 
    */       
    private List<OperationVersionResourceEntity> searchReferLinks(String resourceContent, String accessUrl) {
        List<OperationVersionResourceEntity> resourceReferLinks = getResourceReferLinks(resourceContent,accessUrl);
        //资源内部连接中需要重新下载的任务
        List<OperationVersionResourceEntity> downloadTask = resourceReferLinks.stream().
                filter(res->res.getId() != null).parallel().collect(Collectors.toList());
        //access_url一样的那就下载一次就可以了，如果出现access_url一样但是original_url不一样的，后面把local_url拷贝一下
        downloadTask = removeDuplicateAccessUrl(downloadTask);
        List<List<OperationVersionResourceEntity>> devideDownloadTask = devideTask(downloadTask);
        CountDownLatch countDownLatch = new CountDownLatch(devideDownloadTask.size());
        log.info("分{}个线程去处理下载任务...",devideDownloadTask.size());
        for(List<OperationVersionResourceEntity> taskList: devideDownloadTask){
            threadPool.execute(() -> {
                downloadReferLink(taskList, countDownLatch);
            });
        }
        //等待任务处理完成之后,将新下载的资源替换
        try {
            countDownLatch.await();
            List<OperationVersionResourceEntity> resourceEntityListToSave = resourceReferLinks.stream().
                    filter(res->res.getId() != null).parallel().collect(Collectors.toList());
            //access_url一样的那就下载一次就可以了，如果出现access_url一样但是original_url不一样的，后面
            copyLocalUrlInSameAccessUrl(downloadTask, resourceEntityListToSave);
            //保存资源实体
            saveOperationVersionResource(resourceEntityListToSave);
        } catch (InterruptedException e) {
            log.error("多线程下载资源失败", e);
        }
        return resourceReferLinks;
    }

    /**
    * 将access_url一样的，但是不同的original_url的资源记录拷贝已经转换了的local_url
    * @author Logan
    * @date 2020-10-24 18:03
    * @param downloadTaskList

    * @return
    */
    private void copyLocalUrlInSameAccessUrl(List<OperationVersionResourceEntity> downloadTaskList,List<OperationVersionResourceEntity> resLinkList) {
        for(OperationVersionResourceEntity downloadTask: downloadTaskList){
            String accessUrl = downloadTask.getAccessUrl();
            String localUrl = downloadTask.getLocalUrl();
            if(org.apache.commons.lang.StringUtils.isNotEmpty(localUrl)) {
                resLinkList.stream().filter(link -> link.getAccessUrl().equals(accessUrl)).parallel().forEach(link -> link.setLocalUrl(localUrl));
            }else{
                log.warn("copyLocalUrlInSameAccessUrl是出现local_url为空？accessUrl:{}",accessUrl);
            }
        }
    }

    /**
    * 重复的access_url只保留一份待下载，下载得到的local_url再拷贝到其他的Access_url一样的实体中去
    * @author Logan
    * @date 2020-10-24 18:09
    * @param resList

    * @return
    */
    private List<OperationVersionResourceEntity> removeDuplicateAccessUrl(List<OperationVersionResourceEntity>  resList){
        List<OperationVersionResourceEntity> result =  new ArrayList<>();
        Set<String> accessUrlSets = new HashSet<>();
        for(OperationVersionResourceEntity resourceEntity: resList){
            if(accessUrlSets.add(resourceEntity.getAccessUrl())){
                result.add(resourceEntity);
            }
        }
        return result;
    }

    /**
    * 保存资源存储实体
    * @author Logan
    * @date 2020-10-24 17:28
    * @param downloadTask

    * @return
    */
    private void saveOperationVersionResource(List<OperationVersionResourceEntity> downloadTask) {
//        operationVersionResourceDao.saveAll(downloadTask);
    }

    /**
    * 下载资源里面的连接内容
    * @author Logan
    * @date 2020-10-23 20:29
    * @param taskList
    * @param countDownLatch

    * @return
    */
    private void downloadReferLink(List<OperationVersionResourceEntity> taskList, CountDownLatch countDownLatch) {
        try{
            for(OperationVersionResourceEntity task: taskList){
                download(task);
            }
        }finally {
            countDownLatch.countDown();
        }
    }

    /**
    * 下载，
    * @author Logan
    * @date 2020-10-24 17:23
    * @param task

    * @return
    */
    private void download(OperationVersionResourceEntity task){
        //do download
    }



    /**
    * 解析resourceContent，将需要下载的任务转成下载列表
     * 1.只下载未下载过的链接，未下载过的连接OperationVersionResourceEntity的主键为null
     * 2.已下载过的连接，未下载过的连接OperationVersionResourceEntity的主键不为空
     * 3.新增的连接中，如果是css和js文件，需要即将referedLinkChecked=false
    * @author Logan
    * @date 2020-10-23 20:16
    * @param resourceContent
    * @param accessUrl

    * @return
    */
    private List<OperationVersionResourceEntity> getResourceReferLinks(String resourceContent, String accessUrl) {
        List<OperationVersionResourceEntity> relativePathResource = getRelativePathResources(resourceContent,accessUrl);
        List<OperationVersionResourceEntity> directPathResource = getDirectPathResource(resourceContent);
        List<OperationVersionResourceEntity> result = new ArrayList<>();
        result.addAll(relativePathResource);
        result.addAll(directPathResource);
        return result;
    }

    /**
    * 直接就能下载的连接资源
    * @author Logan
    * @date 2020-10-23 20:21
    * @param resourceContent 当前资源的内容

    * @return
    */
    private List<OperationVersionResourceEntity> getDirectPathResource(String resourceContent) {
        return null;
    }

    /**
    * 有相对路径的下载资源
     * 需要根据相对路径和accessUrl进行拼接
    * @author Logan
    * @date 2020-10-23 20:21
    * @param resourceContent 当前资源的内容
    * @param accessUrl 当前资源的访问路径

    * @return
    */
    private List<OperationVersionResourceEntity> getRelativePathResources(String resourceContent, String accessUrl) {
        List<OperationVersionResourceEntity> relativeResList = getRelativeResourcesList(resourceContent,accessUrl);
        for(OperationVersionResourceEntity resource: relativeResList){
            String directAccessPath = relativePathToDirectPath(accessUrl,resource.getAccessUrl());
            resource.setAccessUrl(directAccessPath) ;
        }
       return relativeResList;
    }

    /**
    * 根据正则，匹配所有的相对路径的资源，并生成OperationVersionResourceEntity列表
    * @author Logan
    * @date 2020-10-24 17:46
    * @param resourceContent
    * @param accessUrl 当资源的访问地址，用于拼接相对路径

    * @return
    */
    private List<OperationVersionResourceEntity> getRelativeResourcesList(String resourceContent,String accessUrl) {
        return null;
    }

    /**
    * 根据相对路径和父资源的访问路径，生成直接资源地址
    * @author Logan
    * @date 2020-10-24 17:42
    * @param upperAccessUrl 父子资源的访问路径
    * @param relativePath 相对路径资源

    * @return
    */
    private String relativePathToDirectPath(String upperAccessUrl,String relativePath){
        String result = null;
        //TODO:    实现    Logan  2020-10-24
        log.info("父子资源访问路径:{}, 相对资源路径:{},转换后的直接资源路径:{}",upperAccessUrl,relativePath,result);
        return result;
    }

    /**
    * 根据localUrl获取资源内容，即js，css内容
    * @author Logan
    * @date 2020-10-23 20:05
    * @param localUrl
    
    * @return 
    */       
    private String getResourceContent(String localUrl) {
        return null;
    }


}
