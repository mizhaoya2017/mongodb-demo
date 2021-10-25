package com.example.mongodbdemo.service.impl;

import com.example.mongodbdemo.cache.RecordInfoResourceCache;
import com.example.mongodbdemo.content.CommonContent;
import com.example.mongodbdemo.data.bo.*;
import com.example.mongodbdemo.data.dto.*;
import com.example.mongodbdemo.data.to.RecordInfoResourceCacheTO;
import com.example.mongodbdemo.data.vo.OperationVersionBasicInfoListVO;
import com.example.mongodbdemo.data.vo.RespCode;
import com.example.mongodbdemo.data.vo.Result;
import com.example.mongodbdemo.entity.*;
import com.example.mongodbdemo.enums.StorageType;
import com.example.mongodbdemo.enums.StorageVersionType;
import com.example.mongodbdemo.excepition.ResultException;
import com.example.mongodbdemo.oss.service.OSSService;
import com.example.mongodbdemo.service.*;
import com.example.mongodbdemo.utils.DateUtils;
import com.example.mongodbdemo.utils.MD5Utils;
import com.example.mongodbdemo.utils.SituThreadLocal;
import com.example.mongodbdemo.utils.StUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.mongodbdemo.content.CommonContent.*;


/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/12 19:21
 **/
@Slf4j
@Service
public class OperationVersionBasicInfoServiceImpl extends BaseMultithreadService implements OperationVersionBasicInfoService {

//    @Autowired
//    private OperationVersionBasicInfoDao versionBasicInfoDao;
//    @Autowired
//    private OperationVersionResourceDao versionSourceDao;
    @Autowired
    private HtmlHandlerResolver htmlHandlerResolver;
    @Autowired
    private RecordInfoService recordInfoService;
//    @Autowired
//    private VersionLogDao versionLogDao;
    @Autowired(required = false)
    private OSSService myOssService;
//    @Autowired
//    private OrderInfoMapper orderInfoMapper;
//    @Autowired
//    private TaskInfoMapper taskInfoMapper;
    @Autowired
    private RecordOssInfoService recordOssInfoService;
    @Autowired
    private StorageType storageType;
    @Autowired
    private TaskInfoService taskInfoService;
    @Autowired
    private RecordInfoResourceCache recordInfoResourceCache;
    @Autowired
    private StorageVersionType storageVersionType;
//    @Autowired
//    private RecordOssInfoMapper recordOssInfoMapper;

    @Override
    public Result save(OperationVersionBasicInfoDTO versionBasicInfoDTO) {
        String version = versionBasicInfoDTO.getVersion();
        // 版本号是否数字结尾
        if (!com.example.mongodbdemo.utils.StringUtils.strLastIsNumber(version)) {
            return Result.fail(RespCode.ERROR_17_);
        }
        boolean existVersion = isExistVersion(versionBasicInfoDTO);
        if (existVersion) {
            return Result.fail(RespCode.ERROR_18_);
        }
//        OperationVersionBasicInfoEntity basicInfoEntity = versionBasicInfoDao.findLastVersionByRecordTime();
        OperationVersionBasicInfoEntity basicInfoEntity = null;
        if (basicInfoEntity != null) {
            // 判断版本时间是否大于当前版本时间
            long dbVersionTime = basicInfoEntity.getRecordTime().getTime();
            long inputVersionTime = versionBasicInfoDTO.getRecordTime();
            if (inputVersionTime <= dbVersionTime) {
                return Result.fail(RespCode.ERROR_16_);
            }
        }


        OperationVersionBasicInfoEntity versionBasicInfoEntity = OperationVersionBasicInfoDTO.convert(versionBasicInfoDTO);
        // 设置用户信息
        UserInfoCacheBO userInfo = SituThreadLocal.getUserInfo();
        if (null == userInfo) {
            throw new ResultException(RespCode.ERROR_LOGIN_NULL);
        }
        versionBasicInfoEntity.setUid(userInfo.getId());
        versionBasicInfoEntity.setUsername(userInfo.getUserName());

        //入库
        saveVersion(versionBasicInfoEntity);
        return Result.success(RespCode.SUCC);
    }

    /**
     * 保存版本
     *
     * @param versionBasicInfoEntity
     */
    public void saveVersion(OperationVersionBasicInfoEntity versionBasicInfoEntity) {

        OperationVersionBasicInfoEntity operationVersionBasicInfoEntity = null;
//        OperationVersionBasicInfoEntity operationVersionBasicInfoEntity = versionBasicInfoDao.save(versionBasicInfoEntity);
        log.info("以平台为粒度的版本管理 -> 新建版本保存完成，版本号：{}", operationVersionBasicInfoEntity.getVersion());
        // 历史数据版本控制
        VersionHistoryOrderInfoBO versionHistoryOrderInfoBO = new VersionHistoryOrderInfoBO();
        BeanUtils.copyProperties(operationVersionBasicInfoEntity, versionHistoryOrderInfoBO);
        versionHistoryOrderInfoBO.setCurrentVersionId(null);
//        versionHistoryOrderInfoBO.setCurrentVersionId(operationVersionBasicInfoEntity.getId());
        versionHistoryOrderInfoBO.setRecordVersionTime(operationVersionBasicInfoEntity.getRecordTime());
        versionHistoryOrderInfoProcess(versionHistoryOrderInfoBO);
    }

    /**
     * 判断版本号是否存在
     *
     * @return
     */
    private boolean isExistVersion(OperationVersionBasicInfoDTO versionBasicInfoDTO) {
        Integer versionType = versionBasicInfoDTO.getVersionType();
        String version = versionBasicInfoDTO.getVersion();
        if (versionType == 1) {
            OperationVersionBasicInfoEntity basicInfoEntity = null;
//            OperationVersionBasicInfoEntity basicInfoEntity = versionBasicInfoDao.findAllByVersionAndH5(version);
            if (basicInfoEntity != null) {
                return true;
            }
            return false;

        }
        return false;
    }

    @Override
    public void saveRecordInfoResource(OperationVersionResourceEntity versionResourceEntity) {
//        versionSourceDao.save(versionResourceEntity);
//        versionSourceDao.save(versionResourceEntity);
    }

    @Override
    public void saveRecordInfoResourceList(List<OperationVersionResourceEntity> versionResourceEntityList) {
//        versionSourceDao.saveAll(versionResourceEntityList);
    }

    @Override
    public void versionHistoryOrderInfoProcess(VersionHistoryOrderInfoBO versionHistoryOrderInfoBO) {
        Integer versionType = versionHistoryOrderInfoBO.getVersionType();
        // H5 新增版本
        if (versionType == 1) {
//            orderInfoMapper.findH5IdByCreateTime(recordTime, currentVersionId);
            updateVersionHistory(versionHistoryOrderInfoBO);
        }
        // 小程序新增版本
        else if (versionType == 2) {
//            orderInfoMapper.findAppletIdByCreateTime(recordTime, currentVersionId);
        }

    }

    @Override
    public OperationVersionResourceEntity isExistByVersionAndAccessUrl(String version, String accessUrl) {
//        return versionSourceDao.findAllByVersionAndAccessUrl(version, accessUrl);
        return null;
    }

    /**
     * 更新历史订单和download url资源版本
     *
     * @param versionHistoryOrderInfoBO 当天保存的版本信息
     */
    private void updateVersionHistory(VersionHistoryOrderInfoBO versionHistoryOrderInfoBO) {
        Timestamp recordTime = versionHistoryOrderInfoBO.getRecordVersionTime();
        String version = versionHistoryOrderInfoBO.getVersion();
        Integer currentVersionId = versionHistoryOrderInfoBO.getCurrentVersionId();
        if (storageVersionType.name().equals(StorageVersionType.PRODUCT_GRANULARITY_VERSION_TYPE.name())) {
            // 更新订单版本号
//            orderInfoMapper.updateOrderInfoProductGranularityVersionIdByH5AndCreateTimeAndProduct(versionHistoryOrderInfoBO);
            // 更新task版本号,并返回taskId(并非主键id), 主键返回
//            orderInfoMapper.updateTaskVersionIdByH5AndCreateTimeAndProduct(versionHistoryOrderInfoBO);

        } else {
            // 更新订单版本号
//            orderInfoMapper.updateOrderVersionIdByH5AndCreateTime(recordTime, currentVersionId);
            // 更新task版本号,并返回taskId(并非主键id), 主键返回
//            orderInfoMapper.updateTaskVersionIdByH5AndCreateTime(recordTime, currentVersionId);
        }
//        List<String> taskIds = orderInfoMapper.selectTaskIdByVersionId(currentVersionId);
        List<String> taskIds = null;
        if (taskIds.size() == 0) {
            return;
        }
        // 更新资源版本
        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ALI_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case H5IMG_STORAGE:
            case NAS_STORAGE:
                List<Integer> recordOssInfoIds = null;
//                List<Integer> recordOssInfoIds = recordOssInfoMapper.findRecordOssIdByTaskId(taskIds);
                updateResourceHistory(recordOssInfoIds, version, 1, 1);
                log.info("以产品-渠道-平台 为粒度的版本下的资源信息已保存redis成功，等待视频redis任务处理... 当前添加的版本号:{} 要更正的recordOSSInfo主键id：{}", version, recordOssInfoIds.toString());
                break;
            case MYSQL_STORAGE:
                List<Integer> recordInfoIds = recordInfoService.findRecordOssIdByTaskId(taskIds);
                updateResourceHistory(recordInfoIds, version, 0, 1);
                log.info("以平台为粒度的版本下的资源信息已保存redis成功，等待视频redis任务处理... 当前添加的版本号:{} 要更正的recordInfo主键id：{}", version, recordInfoIds.toString());
                break;
            default:
                break;

        }


    }

    /**
     * 当版本更新时重新下载视频资源(recordOssInfo)
     *
     * @param recordOssInfoIds oss保存的视频表主键id
     * @param version          版本号
     */
    private void updateResourceHistory(List<Integer> recordOssInfoIds, String version, Integer isOss, Integer versionType) {

        // 判空
        if (recordOssInfoIds == null || recordOssInfoIds.size() == 0) {
            return;
        }
        for (Integer recordOssInfoId : recordOssInfoIds) {

            RecordInfoResourceCacheTO recordInfoResourceCacheTO = new RecordInfoResourceCacheTO();
            recordInfoResourceCacheTO.setResourceInfoId(recordOssInfoId);
            recordInfoResourceCacheTO.setIsOss(isOss);
            recordInfoResourceCacheTO.setVersion(version);
            recordInfoResourceCacheTO.setVersionType(versionType);
            // 保存redis重新下载
            recordInfoResourceCache.leftPushByNotExistForList(recordInfoResourceCacheTO);
        }

    }


    @Override
    public Result listVersionHandler(OperationVersionBasicInfoPageDTO basicInfoPageDTO) {
        Integer currPage = basicInfoPageDTO.getCurrPage();
        Integer pageNumber = basicInfoPageDTO.getPerPage();
        Long startTime = basicInfoPageDTO.getStartTime();
        Long endTime = basicInfoPageDTO.getEndTime();
        String fuzzyCondition = basicInfoPageDTO.getFuzzyCondition();
        String inputRemark = basicInfoPageDTO.getRemark();
        // 查询Web(H5)版本的所有数据
        List<OperationVersionBasicInfoEntity> basicInfoEntities = null;
//        List<OperationVersionBasicInfoEntity> basicInfoEntities = versionBasicInfoDao.findAllByBusinessType(1);
        // 条件检索
        if (startTime != null) {
            basicInfoEntities = basicInfoEntities.stream().filter(
                    versionBasicInfoEntity -> versionBasicInfoEntity.getRecordTime().getTime() >= startTime).collect(Collectors.toList());
        }
        if (endTime != null) {
            basicInfoEntities = basicInfoEntities.stream().filter(
                    versionBasicInfoEntity -> versionBasicInfoEntity.getRecordTime().getTime() < endTime).collect(Collectors.toList());
        }
        // 备注 - 条件过滤
        if (StringUtils.isNotBlank(inputRemark)) {
            basicInfoEntities = basicInfoEntities.stream().filter(
                    versionBasicInfoEntity -> {
                        String remark = Optional.ofNullable(versionBasicInfoEntity.getRemark()).orElse("");
                        return remark.contains(inputRemark);
                    }).collect(Collectors.toList());
        }
        // 多字段模糊 查询条件
        if (StringUtils.isNotBlank(fuzzyCondition)) {
            basicInfoEntities = basicInfoEntities.stream().filter(
                    versionBasicInfoEntity -> {
                        String updateRecord = Optional.ofNullable(versionBasicInfoEntity.getUpdateRecord()).orElse("");
                        String responsibleRecord = Optional.ofNullable(versionBasicInfoEntity.getResponsibleRecord()).orElse("");
                        String username = Optional.ofNullable(versionBasicInfoEntity.getUsername()).orElse("");
                        return updateRecord.contains(fuzzyCondition)
                                || responsibleRecord.contains(fuzzyCondition)
                                || username.contains(fuzzyCondition);
                    }).collect(Collectors.toList());
        }

        // 数据分页处理
        List<OperationVersionBasicInfoListVO> collect = basicInfoEntities.stream()
                .map(OperationVersionBasicInfoListVO::convert)
                .sorted(Comparator.comparing(OperationVersionBasicInfoListVO::getRecordTime).reversed())
                .skip((currPage - 1) * pageNumber)
                .limit(pageNumber)
                .collect(Collectors.toList());
        PageBO<List<OperationVersionBasicInfoListVO>> pageBO = PageBO.handler(collect, currPage, basicInfoEntities.size(), pageNumber);
        return Result.success(pageBO);
    }


    @Override
    public Result orderProductList(OperationPageCrawPageDTO basicInfoPageDTO) {

        Integer currPage = basicInfoPageDTO.getCurrPage();
        Integer pageNumber = basicInfoPageDTO.getPerPage();
        Integer versionId = basicInfoPageDTO.getVersionId();
        String channel = basicInfoPageDTO.getChannel();
        String productName = basicInfoPageDTO.getProductName();
        String platform = basicInfoPageDTO.getPlatform();
        // 查询到版本号下的所有有taskId的订单
        List<OrderInfoEntity> orderInfoEntities = null;
//        List<OrderInfoEntity> orderInfoEntities = orderInfoMapper.findListByVersionId(versionId, channel, productName, platform);

        //  检索taskId
        List<String> orderList = orderInfoEntities.stream().map(OrderInfoEntity::getOrderId).collect(Collectors.toList());
        List<TaskInfoEntity> taskInfoEntityList = new ArrayList<>();
        if (orderList != null && orderList.size() > 0) {
//            taskInfoEntityList = taskInfoMapper.findAllByOrderId(orderList);
        }


        // 不同订单按照产品名称和渠道去重， 只取最早的订单
        List<RecordInfoIdsSearchBO> recordInfoResult = new ArrayList<>();
        List<OrderInfoEntity> recordInfo = new ArrayList<>(orderInfoEntities);
        for (OrderInfoEntity orderInfoEntity : orderInfoEntities) {

            // 1、先判断最终结果集中是否有相同数据的
            long existCount = recordInfoResult.stream().filter(item -> {
                String itemProductName = Optional.ofNullable(item.getProductName()).orElse("");
                String itemChannel = Optional.ofNullable(item.getChannel()).orElse("");
                String itemPlatform = Optional.ofNullable(item.getPlatform()).orElse("");
                return itemProductName.equalsIgnoreCase(orderInfoEntity.getProductName())
                        && itemChannel.equalsIgnoreCase(orderInfoEntity.getChannel())
                        && itemPlatform.equalsIgnoreCase(orderInfoEntity.getPlatform());
            }).count();
            if (existCount == 1) {
                continue;
            }
            // 判断相关数据是否重复, 是否有 产品-渠道-平台纬度 重复数据存在
            long count = recordInfo.stream().filter(item -> {
                String itemProductName = Optional.ofNullable(item.getProductName()).orElse("");
                String itemChannel = Optional.ofNullable(item.getChannel()).orElse("");
                String itemPlatform = Optional.ofNullable(item.getPlatform()).orElse("");

                return itemProductName.equalsIgnoreCase(orderInfoEntity.getProductName())
                        && itemChannel.equalsIgnoreCase(orderInfoEntity.getChannel())
                        && itemPlatform.equalsIgnoreCase(orderInfoEntity.getPlatform());

            }).count();
            if (count > 1) {
                // 获取重复数据中时间最早的记录
                OrderInfoEntity infoEntity = recordInfo.stream().filter(item -> {
                    String itemProductName = Optional.ofNullable(item.getProductName()).orElse("");
                    String itemChannel = Optional.ofNullable(item.getChannel()).orElse("");
                    String itemPlatform = Optional.ofNullable(item.getPlatform()).orElse("");

                    return itemProductName.equalsIgnoreCase(orderInfoEntity.getProductName())
                            && itemChannel.equalsIgnoreCase(orderInfoEntity.getChannel())
                            && itemPlatform.equalsIgnoreCase(orderInfoEntity.getPlatform());
                }).sorted(Comparator.comparing(OrderInfoEntity::getCreateTime)).findFirst().get();

                // 对象转换,收集
                RecordInfoIdsSearchBO recordInfoIdsSearchBO = new RecordInfoIdsSearchBO();
                BeanUtils.copyProperties(infoEntity, recordInfoIdsSearchBO);
                // 获取该订单下的所有taskId
                List<String> taskIds = taskInfoEntityList.stream().filter(item -> item.getOrderId().equalsIgnoreCase(orderInfoEntity.getOrderId())).map(TaskInfoEntity::getTaskId).collect(Collectors.toList());
                recordInfoIdsSearchBO.setTaskId(taskIds);
                recordInfoResult.add(recordInfoIdsSearchBO);
            } else {
                // 若不重复则直接添加到最终结果集
                RecordInfoIdsSearchBO recordInfoIdsSearchBO = new RecordInfoIdsSearchBO();
                BeanUtils.copyProperties(orderInfoEntity, recordInfoIdsSearchBO);
                // 获取该订单下的所有taskId
                List<String> taskIds = taskInfoEntityList.stream().filter(item -> item.getOrderId().equalsIgnoreCase(orderInfoEntity.getOrderId())).map(TaskInfoEntity::getTaskId).collect(Collectors.toList());
                recordInfoIdsSearchBO.setTaskId(taskIds);
                recordInfoResult.add(recordInfoIdsSearchBO);
            }
        }

        List<RecordInfoIdsSearchBO> recordInfoIdsSearchBOList = recordInfoResult.stream().sorted(Comparator.comparing(RecordInfoIdsSearchBO::getCreateTime).reversed())
                .skip((currPage - 1) * pageNumber)
                .limit(pageNumber)
                .collect(Collectors.toList());
        // 3、返回分页处理数据
        PageBO<List<RecordInfoIdsSearchBO>> listPageBO = PageBO.handler(recordInfoIdsSearchBOList, currPage, recordInfoResult.size(), pageNumber);
        return Result.success(listPageBO);
    }

    @Override
    public Integer operationVersionResourceSave(OperationVersionResourceEntity versionSourceEntity) {

        OperationVersionResourceEntity resourceEntity = null;
//        OperationVersionResourceEntity resourceEntity = versionSourceDao.save(versionSourceEntity);
        return null;
//        return resourceEntity.getId();

    }

    @Override
    public void operationVersionResourceUpdateRecordInfoIdById(Integer recordInfoId, List<Integer> resourceIds) {
//        versionSourceDao.updateResourceInfoIdById(recordInfoId, resourceIds);
    }

    @Override
    public List<OperationVersionBasicInfoEntity> findAllByProductId(Integer productId) {
        return null;
//        return versionBasicInfoDao.findAllByProductId(productId);

    }

    @Override
    public List<OperationVersionBasicInfoEntity> findAllAppletByMoreCreateTime(Integer productId, Timestamp createTime) {
        return null;
//        return versionBasicInfoDao.findAllAppletByMoreCreateTime(productId, createTime);
    }

    @Override
    public boolean needDownloadResource(String decrptRecordInfo, Integer versionType) {
        String latestVersion = getLatestVersion(versionType);
        if (StringUtils.isEmpty(latestVersion)) {
            log.warn("没有最新的版本，不需要下载资源");
            return false;
        } else {
            List<String> urls = parsePageUrls(decrptRecordInfo);
            List<String> needDownloadList = needDownloadResourceUrls(urls, versionType, latestVersion);
            if (needDownloadList != null && needDownloadList.size() > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean needDownloadResouceByUrl(String url, Integer versionType) {
        List<String> versions = null;
//        List<String> versions = versionSourceDao.originalUrlVersions(url, versionType);
        String latestVersion = getLatestVersion(versionType);
        if (StringUtils.isEmpty(latestVersion)) {
            log.warn("没有最新的版本，不需要下载资源");
            return false;
        } else {
            if (versions != null && versions.size() > 0) {
                return !versions.contains(latestVersion);
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean needDownloadResouceByOriginalPages(List<String> pageUrls, Integer versionType) {
        if (pageUrls == null || pageUrls.size() == 0) {
            return false;
        }
        pageUrls = pageUrls.stream().parallel().distinct().collect(Collectors.toList());
        String latestVersion = getLatestVersion(versionType);
        if (StringUtils.isEmpty(latestVersion)) {
            log.warn("没有最新的版本，不需要下载资源");
            return false;
        } else {
            List<String> needDownloadList = needDownloadResourceUrls(pageUrls, versionType, latestVersion);
            if (needDownloadList != null && needDownloadList.size() > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * @param urls
     * @param versionType 1-web 2-小程序 3-app
     * @return
     * @author Logan
     * @date 2020-09-14 16:39
     */
    private List<String> needDownloadResourceUrls(List<String> urls, Integer versionType, String latestVersion) {
        List<String> urlNeedDowload = new ArrayList<>();
        for (String url : urls) {
            String urlMd5 = MD5Utils.getMD5String(url);
            List<String> versions = null;
//            List<String> versions = versionLogDao.pageVersions(urlMd5, versionType);
            if (versions.contains(latestVersion)) {
                break;
            } else {
                urlNeedDowload.add(url);
            }
        }
        return urlNeedDowload;
    }

    /**
     * 解析record_info中的页面url
     *
     * @param decrptRecordInfo
     * @return
     * @author Logan
     * @date 2020-09-14 16:45
     */
    private List<String> parsePageUrls(String decrptRecordInfo) {

        String periodPatternStr = RRWEB_PAGE_URL_REG_PATTERN;
        try {
            Pattern pattern = Pattern.compile(periodPatternStr);
            Matcher matcher = pattern.matcher(decrptRecordInfo);
            List<String> pageUrls = new ArrayList<>();
            while (matcher.find()) {
                pageUrls.add(matcher.group(RRWEB_PAGE_URL_REG_GROUP));
            }
            return pageUrls;
        } catch (Exception e) {
            log.error("没有匹配到record_info中type=4的http地址", e);
            return null;
        }
    }

    private String getLatestVersion(Integer versionType) {
        return null;
//        return versionBasicInfoDao.findLatestVersion(versionType);
    }

    /**
     * 解析url页面信息并存储
     *
     * @param sourceBO
     * @throws IOException
     */
    @Override
    public void parsePageInfo(OperationVersionSourceBO sourceBO) {

//        String url = "http://staging-idr-record-video.situdata.com/idr-record-fe/orderRecordList";
        String pageUrl = null;
        String title = null;
        URL realUrl = null;
        // 获取可回溯页面的基础信息
        try {
            pageUrl = sourceBO.getPageUrl();
            Document doc = Jsoup.connect(pageUrl).get();
            title = doc.title();
            realUrl = new URL(pageUrl);
        } catch (IOException e) {
            return;
        }
        //
        String htmlName = realUrl.getPath() + ".html";
        // main页面保存相对位置
        String localFilePathProfix = CommonContent.PAGE_UPLOAD + "/" + DateUtils.dateConvertStr() + "/" + sourceBO.getOrderId() + "-" + sourceBO.getVersion() + "-" + StUtils.getUUID();
        // 根据原请求url作为 本地保存文件名前缀
        htmlName = (htmlName = convertStr(htmlName)) == "" ? StUtils.getUUID() : htmlName;
        // 开始main页面解析，并返回磁盘物理保存路径
        String fileAbsolutePath = htmlResourceParse(pageUrl, localFilePathProfix, htmlName);
        // 将真实物理路径转换为相对项目路径
        fileAbsolutePath = com.example.mongodbdemo.utils.StringUtils.sub(fileAbsolutePath, CommonContent.PAGE_UPLOAD);
        // 数据组装并存库
        OperationVersionResourceEntity operationVersionSourceEntity = new OperationVersionResourceEntity()
//                .setOriginalUrl(pageUrl)
//                .setLocalUrl(fileAbsolutePath)
//                .setVersion(sourceBO.getVersion())
//                .setVersionId(sourceBO.getVersionId())
//                .setOrderId(sourceBO.getOrderId())
//                .setTaskId(sourceBO.getTaskId())
//                .setProductName(sourceBO.getProductName())
//                .setPlatform(sourceBO.getPlatform())
//                .setPageName(htmlName)
//                .setPageTitle(title)
                /*.setCreateTime(new Timestamp(System.currentTimeMillis()))*/;

//        versionSourceDao.save(operationVersionSourceEntity);
        log.info("页面单次解析， 原Url: {} ，本地url： {} ---> 保存成功！！！", pageUrl, fileAbsolutePath);
    }

    @Override
    public OperationVersionBasicInfoEntity findMaxVersion(String platform) {
        if (StringUtils.isBlank(platform)) {
//            return versionBasicInfoDao.findCurrentVersionByVersionType(1);
        }
        if (platform.equalsIgnoreCase(CommonContent.PLATFORM_APP)) {
//            return versionBasicInfoDao.findCurrentVersionByVersionType(3);
        } else if (platform.equalsIgnoreCase(CommonContent.PLATFORM_APPLET)) {
//            return versionBasicInfoDao.findCurrentVersionByVersionType(2);
        } else {
//            return versionBasicInfoDao.findCurrentVersionByVersionType(1);
        }
        return null;
    }

    @Override
    public Result orderSingleProductInfo(OrderSingleProductInfoDTO orderSingleProductInfoDTO) {
        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case OSS_ALI_STORAGE:
            case H5IMG_STORAGE:
            case NAS_STORAGE:
                return orderSingleProductInfoFromOss(orderSingleProductInfoDTO);
            case MYSQL_STORAGE:
                return orderSingleProductInfoFromDB(orderSingleProductInfoDTO);
            default:
                return Result.success(new ArrayList<>());
        }

    }

    private Result orderSingleProductInfoFromOss(OrderSingleProductInfoDTO orderSingleProductInfoDTO) {
        List<String> result = new ArrayList<>();
        List<RecordOssInfoEntity> recordOssInfoList = recordOssInfoService.findAllByOrderIdAndTaskId(orderSingleProductInfoDTO.getOrderId(), orderSingleProductInfoDTO.getTaskId());
        for (RecordOssInfoEntity recordOssInfoEntity : recordOssInfoList) {
            String ossPath = recordOssInfoEntity.getOssPath();
            String objectContentString = myOssService.getObjectContentString(ossPath);
            result.add(objectContentString);
        }
        return Result.success(result);
    }


    private Result orderSingleProductInfoFromDB(OrderSingleProductInfoDTO orderSingleProductInfoDTO) {

        // 根据订单id和taskId查询视频信息
        List<String> recordInfoList = recordInfoService.findRecordInfoByOrderIdAndTaskId(orderSingleProductInfoDTO.getOrderId(), orderSingleProductInfoDTO.getTaskId());

        return Result.success(recordInfoList);

    }


    @Override
    public Integer isExistByUrl(String pageUrl, String version) {

//        return versionSourceDao.findOriginUrlByOriginUrlAndVersion(pageUrl, version);
        return null;
    }

    @Override
    public void updateProductName(Integer id, String productName) {
//        versionSourceDao.updateProductNameById(id, productName);

    }

    /**
     * 保存静态页面
     *
     * @param fileUrl
     * @param filePathPrefix
     * @param fileName
     * @return
     */
    private String htmlResourceParse(String fileUrl, String filePathPrefix, String fileName) {

        return htmlHandlerResolver.dowmload(fileUrl, filePathPrefix, fileName);
    }

    /**
     * 将字符串中的所有 "/" 转换为 "-", 字符串第0位除外
     *
     * @return
     */
    private String convertStr(String str) {
        String substring = str.substring(1);
        return substring.replaceAll("/", "-");
    }

    /**
     * 根据版本号查询
     *
     * @param versionList 版本号
     * @return
     */
    @Override
    public List<OperationVersionResourceEntity> findAllByVersion(List<String> versionList) {

        if (versionList == null || versionList.size() == 0) {
            return new ArrayList<>();
        }
        return null;
//        return versionSourceDao.findAllByVersion(versionList);
    }

    @Override
    public OperationVersionBasicInfoEntity findAllById(Integer versionKeyId) {

        return null;
//        return versionBasicInfoDao.findById(versionKeyId).get();
    }

    @Override
    public Result saveAutomaticVersionInfo(AutomaticCreateVersionDTO createVersionDTO) {
//        OperationVersionBasicInfoEntity versionBasicInfoEntity = versionBasicInfoDao.findLastVersionByRecordTime();
        OperationVersionBasicInfoEntity versionBasicInfoEntity = null;
        if (versionBasicInfoEntity == null) {
            // 第一次建立版本， 生成版本号
            OperationVersionBasicInfoEntity basicInfoEntity = AutomaticCreateVersionDTO.converterEntity(createVersionDTO, OPERATION_VERSION_AUTOMATIC_NAME);
            // 设置用户信息
            basicInfoEntity.setUsername(SYSTEM);
            basicInfoEntity.setUid(-1);
            // 保存版本
            this.saveVersion(basicInfoEntity);
            return Result.success();
        }
        // 非第一次创建
        // 判断版本时间是否大于当前版本时间
        long dbVersionTime = versionBasicInfoEntity.getRecordTime().getTime();
        long inputVersionTime = Long.parseLong(createVersionDTO.getVersionTime());
        if (inputVersionTime <= dbVersionTime) {
            return Result.fail(RespCode.ERROR_16_);
        }
        // 版本号自动+1
        String versionLatest = com.example.mongodbdemo.utils.StringUtils.strLastNumberAddAndGet(versionBasicInfoEntity.getVersion());
        // 保存版本信息
        OperationVersionBasicInfoEntity basicInfoEntity = AutomaticCreateVersionDTO.converterEntity(createVersionDTO, versionLatest);
        // 设置用户信息
        basicInfoEntity.setUsername(SYSTEM);
        basicInfoEntity.setUid(-1);
        // 保存版本
        this.saveVersion(basicInfoEntity);
        return Result.success();
    }
}
