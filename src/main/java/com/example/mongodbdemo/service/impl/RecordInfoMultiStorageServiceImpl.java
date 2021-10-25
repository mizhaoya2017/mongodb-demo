package com.example.mongodbdemo.service.impl;

import com.example.mongodbdemo.cache.RecordInfoResourceCache;
import com.example.mongodbdemo.content.CommonContent;
import com.example.mongodbdemo.data.bo.RecordInfoMultiStorageBO;
import com.example.mongodbdemo.data.to.RecordInfoResourceCacheTO;
import com.example.mongodbdemo.entity.*;
import com.example.mongodbdemo.enums.StorageType;
import com.example.mongodbdemo.oss.config.QingStorOssProperties;
import com.example.mongodbdemo.oss.service.OSSService;
import com.example.mongodbdemo.service.OperationVersionBasicInfoService;
import com.example.mongodbdemo.service.RecordInfoMultiStorageService;
import com.example.mongodbdemo.service.RedisLockService;
import com.example.mongodbdemo.service.TaskInfoService;
import com.example.mongodbdemo.utils.RegExUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @program: optrace
 * @description:
 * @author: Logan
 * @create: 2020-09-15 16:42
 **/
@Service
@Slf4j
public class RecordInfoMultiStorageServiceImpl implements RecordInfoMultiStorageService {
    @Autowired(required = false)
    private OSSService myOssService;
    @Autowired
    private StorageType storageType;
    @Autowired(required = false)
    private QingStorOssProperties qingStorOssProperties;
    @Autowired
    private RecordInfoResourceCache recordInfoResourceCache;
    @Autowired
    private TaskInfoService taskInfoService;
    @Autowired
    private OperationVersionBasicInfoService versionBasicInfoService;

    @Autowired
    private RedisLockService redisLockService;
    @Override
    public Integer saveRecordInfo(RecordInfoMultiStorageBO recordInfoMultiStorageBO) {
        Integer recordInfoId = null;
        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case OSS_ALI_STORAGE:
            case H5IMG_STORAGE:
            case NAS_STORAGE:
                recordInfoId = saveRecordInfoToOss(recordInfoMultiStorageBO);
                break;
            case MYSQL_STORAGE:
                recordInfoId = saveRecordInfoToMysql(recordInfoMultiStorageBO);
                break;
            default:
                return null;
        }
        return recordInfoId;
    }

    @Override
    public Integer IndexCheck(RecordInfoMultiStorageBO recordInfoMultiStorageBO) {
        Integer indexCount = null;
        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case OSS_ALI_STORAGE:
            case H5IMG_STORAGE:
            case NAS_STORAGE:
                indexCount = indexCountOss(recordInfoMultiStorageBO);
                break;
            case MYSQL_STORAGE:
                indexCount = indexCountMySQL(recordInfoMultiStorageBO);
                break;
            default:
                return null;
        }
        return indexCount;
    }

    private Integer indexCountMySQL(RecordInfoMultiStorageBO recordInfoMultiStorageBO) {
//        Integer indexCount = recordOssInfoDao.getIndexCount(recordInfoMultiStorageBO.getTaskId());
//        return indexCount;
        return null;
    }

    private Integer indexCountOss(RecordInfoMultiStorageBO recordInfoMultiStorageBO) {
//        Integer indexCount = recordInfoDao.getIndexCount(recordInfoMultiStorageBO.getTaskId());
//        return indexCount;
        return null;
    }

    @Override
    public List<String> getRecordInfoList(String taskId) {
        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ALI_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case NAS_STORAGE:
                return getRecordInfoListFromOss(taskId);
            case MYSQL_STORAGE:
                return getRecordInfoListFromMysql(taskId);
            case H5IMG_STORAGE:
                return getRecordInfoListFromH5img(taskId);
            default:
                return null;
        }
    }

    @Override
    public List<String> queryRecordInfoPageByTaskId(String taskId, int offset, int size) {
        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ALI_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case H5IMG_STORAGE:
            case NAS_STORAGE:
                return getRecordInfoListFromOssPage(taskId, offset, size);
            case MYSQL_STORAGE:
                return getRecordInfoListFromMysqlPage(taskId, offset, size);
            default:
                return null;
        }
    }

    private List<String> getRecordInfoListFromMysql(String taskId) {
        List<RecordInfoEntity> recordInfoList = null;
//        List<RecordInfoEntity> recordInfoList = recordInfoDao.findByTaskId(taskId);
        List<String> infos = new ArrayList<>();
        if (recordInfoList != null && recordInfoList.size() > 0) {
            for (RecordInfoEntity recordInfo : recordInfoList) {
                String recordInfoStr = recordInfo.getRecordInfo();
                if (StringUtils.isNotEmpty(recordInfoStr)) {
                    infos.add(recordInfoStr);
                }
            }
        }
        return infos;
    }

    private List<String> getRecordInfoListFromH5img(String taskId) {
        List<RecordOssInfoEntity> recordOssInfoList = null;
//        List<RecordOssInfoEntity> recordOssInfoList = recordOssInfoDao.getRecordInfosByTaskId(taskId);
        List<String> infos = new ArrayList<>();
        if (recordOssInfoList != null && recordOssInfoList.size() > 0) {
            for (RecordOssInfoEntity recordOssInfo : recordOssInfoList) {
                String ossPath = recordOssInfo.getOssPath();
                if (StringUtils.isNotEmpty(ossPath)) {
                    String info = myOssService.getObjectContentString(ossPath);
                    infos.add(info);
                }
            }
        }
        return infos;
    }

    private List<String> getRecordInfoListFromMysqlPage(String taskId, int offset, int size) {
        List<String> list = new ArrayList<>();
        List<RecordInfoEntity> recordInfoList = null;
//        List<RecordInfoEntity> recordInfoList = recordInfoDao.queryRecordInfoPageByTaskId(taskId, offset, size);
        for (RecordInfoEntity recordInfoEntity : recordInfoList) {
            list.add(recordInfoEntity.getRecordInfo());
            // add redis cache left head
            if (recordInfoEntity.getDownloadStatus() == null || recordInfoEntity.getDownloadStatus() == 0) {
//                proDownloadCache(taskId, recordInfoEntity.getId(), 0);
                proDownloadCache(taskId, null, 0);
            }
        }
        return list;
    }

    /**
     * 在查询缓存时 将每一个recordInfo资源更新到redis的左头部， 以便后续优先处理
     *
     * @param taskId
     * @param isOss
     */
    private void proDownloadCache(String taskId, Integer resourceInfoId,Integer isOss) {
        RecordInfoResourceCacheTO recordInfoResourceCacheTO = new RecordInfoResourceCacheTO();
        recordInfoResourceCacheTO.setResourceInfoId(resourceInfoId);
        recordInfoResourceCacheTO.setIsOss(isOss);
        // 设置版本信息
        TaskInfoEntity taskInfoEntity = taskInfoService.findTaskInfoByTaskId(taskId);
        if(versionByProduct(taskInfoEntity)){
            OperationVersionInfoEntity versionInfo = getVersionByProduct(taskInfoEntity);
            recordInfoResourceCacheTO.setVersion(versionInfo.getSystemVersion());
            recordInfoResourceCacheTO.setVersionType(1);
        }else{
            OperationVersionBasicInfoEntity versionInfo = getVersionByPlatform(taskInfoEntity);
            if(versionInfo == null){
                log.warn("替换检查，优先插队任务失败,没有找到versionInfo: taskId:{}",taskId);
                return;
            }
            recordInfoResourceCacheTO.setVersion(versionInfo.getVersion());
            recordInfoResourceCacheTO.setVersionType(versionInfo.getVersionType());
        }
        recordInfoResourceCache.leftPushByNotExistForList(recordInfoResourceCacheTO);
        log.info("可回溯详情页查看时，优先任务已插入， taskId:{},resourceInfoId:{}", taskId,resourceInfoId);
    }

    /**
     * 平台级别的版本管理
     * @author Logan
     * @date 2020-11-19 17:53
     * @param taskInfoEntity

     * @return
     */
    private OperationVersionBasicInfoEntity getVersionByPlatform(TaskInfoEntity taskInfoEntity) {
        OperationVersionBasicInfoEntity versionBasicInfoEntity = versionBasicInfoService.findAllById(taskInfoEntity.getVersionId());
        return versionBasicInfoEntity;
    }

    /**
     * 产品级别的版本管理号
     * @author Logan
     * @date 2020-11-19 17:53
     * @param taskInfoEntity

     * @return
     */
    private OperationVersionInfoEntity getVersionByProduct(TaskInfoEntity taskInfoEntity) {
        Integer productVersionId = taskInfoEntity.getProductGranularityVersionId();
        OperationVersionInfoEntity versionInfoEntity = null;
//        OperationVersionInfoEntity versionInfoEntity = operationVersionInfoMapper.selectById(productVersionId);
        if(versionInfoEntity != null){
            return versionInfoEntity;
        }else{
            // 如果该 产品-平台-渠道 对应的没有版本号则归类到系统默认版本
            OperationVersionInfoEntity defaultSystemVersion = null;
//            OperationVersionInfoEntity defaultSystemVersion = operationVersionInfoMapper.selectLastSystemVersion();
            return defaultSystemVersion;
        }
    }

    /**
     * 是否为产品级的版本管理
     * @author Logan
     * @date 2020-11-19 17:50
     * @param taskInfoEntity

     * @return
     */
    private boolean versionByProduct(TaskInfoEntity taskInfoEntity) {
        return taskInfoEntity.getProductGranularityVersionId() != null;
    }

    private List<String> getRecordInfoListFromOssPage(String taskId, int offset, int size) {
        List<RecordOssInfoEntity> recordOssInfoList = null;
//        List<RecordOssInfoEntity> recordOssInfoList = recordOssInfoDao.getRecordInfosPageByTaskId(taskId, offset, size);
        List<String> infos = new ArrayList<>();
        if (recordOssInfoList != null && recordOssInfoList.size() > 0) {
            for (RecordOssInfoEntity recordOssInfo : recordOssInfoList) {
                String ossPath = recordOssInfo.getOssPath();
                if (StringUtils.isNotEmpty(ossPath)) {
                    String info = myOssService.getObjectContentString(ossPath);
                    infos.add(info);
                    if (recordOssInfo.getDownloadStatus() == null || recordOssInfo.getDownloadStatus() == 0) {
                        proDownloadCache(taskId, null, 1);
//                        proDownloadCache(taskId, recordOssInfo.getId(), 1);
                    }
                }
            }
        }
        return infos;
    }

    private Integer saveRecordInfoToMysql(RecordInfoMultiStorageBO recordInfoMultiStorageBO) {
        RecordInfoEntity recordInfoEntity = new RecordInfoEntity();
//        recordInfoEntity.set(recordInfoMultiStorageBO.getTiId());
        recordInfoEntity.setOrderId(recordInfoMultiStorageBO.getOrderId());
        recordInfoEntity.setTaskId(recordInfoMultiStorageBO.getTaskId());
        recordInfoEntity.setLast(recordInfoMultiStorageBO.getLast());
        recordInfoEntity.setRecordIndex(recordInfoMultiStorageBO.getIndex());
        recordInfoEntity.setRecordMode(recordInfoMultiStorageBO.getRecordMode());
        if (Objects.nonNull(recordInfoMultiStorageBO.getOrderId())) {
            recordInfoEntity.setOrderId(recordInfoMultiStorageBO.getOrderId());
        }
        recordInfoEntity.setDownloadStatus(CommonContent.RESOURCE_DOWNLOAD_STATUS_DEFAULT);
        recordInfoEntity.setRecordInfo(recordInfoMultiStorageBO.getRecordInfo());
        recordInfoEntity.setUploadTime(new Timestamp(recordInfoMultiStorageBO.getUploadTime().getTime()));
//        recordInfoDao.save(recordInfoEntity);
//        recordInfoDao.save(recordInfoEntity);
//        return recordInfoEntity.getId();
        return null;
    }

    /**
     * @param inputStream
     * @param filePath      aa/bb
     * @param fileShortName a.png
     * @return
     */
    private String saveRecordResourceToOss(InputStream inputStream, String filePath, String
            fileShortName) {

        String objectKey = String.format("%s/%s", filePath, fileShortName);
        myOssService.putObject(objectKey, inputStream);
        log.info("OSS保存文件成功!!!： 保存目录objectKey： {}", objectKey);
        return objectKey;
    }


    public String saveRecordResourceToMysql(InputStream inputStream, String filePath, String fileShortName) {
        return fileDownLoadToLocal((BufferedInputStream) inputStream, filePath, fileShortName);
    }

    @Override
    public String saveRecordResource(InputStream inputStream, String filePath, String fileShortName) {
        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case OSS_ALI_STORAGE:
            case H5IMG_STORAGE:
            case NAS_STORAGE:
                String ossResourcePath = saveRecordResourceToOss(inputStream, filePath, fileShortName);
                return getOssUrl(ossResourcePath);
            case MYSQL_STORAGE:
                return saveRecordResourceToMysql(inputStream, filePath, fileShortName);
            default:
                return null;
        }

    }

    /**
     * 根据文件存储路径获取可访问url
     *
     * @param filePath
     * @return
     */
    private String getOssUrl(String filePath) {
        switch (storageType) {
            case OSS_QINGSTOR_STORAGE:
            case OSS_ZANHUA_STORAGE:
            case OSS_ALI_STORAGE:
            case H5IMG_STORAGE:
                String domain = CommonContent.DO_MAIN;
                return String.format("%s/record/resources?resourceName=%s", domain, filePath);
            case NAS_STORAGE:
                return myOssService.getObjectSignatureUrl(filePath);
            default:
                return null;

        }
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
        String pathConvert = pathConvert(realFile.getAbsolutePath());
        return pathConvert;
    }

    private String pathConvert(String realSavePath) {
        String relativelySavePath = com.example.mongodbdemo.utils.StringUtils.sub(realSavePath, CommonContent.PAGE_UPLOAD);
        return relativelySavePath.startsWith("http") ? relativelySavePath : CommonContent.DO_MAIN + relativelySavePath;
    }

    private Integer saveRecordInfoToOss(RecordInfoMultiStorageBO recordInfoMultiStorageBO) {
        RecordOssInfoEntity recordOssInfoEntity = new RecordOssInfoEntity();
        recordOssInfoEntity.setRecordIndex(recordInfoMultiStorageBO.getIndex());
        recordOssInfoEntity.setRecordMode(recordInfoMultiStorageBO.getRecordMode());
        if (storageType == StorageType.NAS_STORAGE) {
            // nas存储， 将OSS-Nas 路径前缀保存起来
            recordOssInfoEntity.setBucketName(CommonContent.RESOURCE_OSS_PREFIX);
        } else {
            // 其他 OSS 保存bucketName
            recordOssInfoEntity.setBucketName(myOssService.getBucketName());
        }
        String orderId = recordInfoMultiStorageBO.getOrderId();
        String taskId = recordInfoMultiStorageBO.getTaskId();
        String ossPath = myOssService.generateRecordInfoObjectName(orderId, taskId);
        recordOssInfoEntity.setOssPath(ossPath);
        recordOssInfoEntity.setTiId(recordInfoMultiStorageBO.getTiId());
        recordOssInfoEntity.setLast(recordInfoMultiStorageBO.getLast());
        recordOssInfoEntity.setUploadTime(new Timestamp(recordInfoMultiStorageBO.getUploadTime().getTime()));
        if (storageType == StorageType.OSS_QINGSTOR_STORAGE) {
            recordOssInfoEntity.setZoneKey(qingStorOssProperties.getZoneKey());
        }
        recordOssInfoEntity.setTaskId(taskId);
        recordOssInfoEntity.setOrderId(orderId);
        recordOssInfoEntity.setDownloadStatus(CommonContent.RESOURCE_DOWNLOAD_STATUS_DEFAULT);
        myOssService.putObject(ossPath, recordInfoMultiStorageBO.getRecordInfo());
//        recordOssInfoDao.save(recordOssInfoEntity);
//        recordOssInfoDao.save(recordOssInfoEntity);
//        return recordOssInfoEntity.getId();
        return null;
    }

    private List<String> getRecordInfoListFromOss(String taskId) {
        List<RecordOssInfoEntity> recordOssInfoList = null;
//        List<RecordOssInfoEntity> recordOssInfoList = recordOssInfoDao.getRecordInfosByTaskId(taskId);
        List<String> infos = new ArrayList<>();
        if (recordOssInfoList != null && recordOssInfoList.size() > 0) {
            for (RecordOssInfoEntity recordOssInfo : recordOssInfoList) {
                String ossPath = recordOssInfo.getOssPath();
                if (StringUtils.isNotEmpty(ossPath)) {
                    String info = myOssService.getObjectContentString(ossPath);
                    infos.add(info);
                }
            }
        }
        return infos;
    }
}