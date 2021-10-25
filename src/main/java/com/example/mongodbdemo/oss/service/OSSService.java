package com.example.mongodbdemo.oss.service;

import com.example.mongodbdemo.content.CommonContent;
import com.example.mongodbdemo.utils.StUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @program: optrace
 * @description:
 * @author: Logan
 * @create: 2020-09-05 09:38
 **/
public interface OSSService {
    int TryCount = 2;
    int STOP_TRY_TIMES_VALUE = -1;
    Long TRY_WATTING_MILLS = 2 * 1000L;

    /**
     * 保存一个对象，返回对象所在地址
     *
     * @param objectName 例如 /video/video.mp4
     * @param input
     * @return
     * @author Logan
     * @date 2020-09-05 09:49
     */
    void putObject(String objectName, InputStream input);

    void putObject(String objectName, String content);


    /**
     * 根据bucket,key返回对象数据流
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 09:51
     */
    String getObjectContentString(String objectName);

    /**
     * 获取浏览地址（有有效期），不直接暴露给前端，需要通过本服务器重定向
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 18:23
     */
    String getObjectSignatureUrl(String objectName);


    /**
     * 删除一个对象
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 18:35
     */
    void deleteObject(String objectName);

    /**
     * 实际暴露给前端的地址，通过做个地址进入Controller重定向到oss实际地址
     *
     * @param objectName
     * @return
     * @author Logan
     * @date 2020-09-15 12:46
     */
    default String getSourceUrl(String objectName) {
        return String.format("%s/record/resources?%s=%s", CommonContent.DO_MAIN, CommonContent.RESOURCE_NAME_REQ_PARAM, objectName);
    }

    String getBucketName();

    /**
     * 判断是否超时
     *
     * @param code
     * @return
     * @author Logan
     * @date 2020-09-24 11:19
     */
    boolean timeoutAndWait(String code);

    /**
     * 生成recordInfo对应的ObjectName
     *
     * @param orderId
     * @param taskId
     * @return
     * @author Logan
     * @date 2020-09-16 10:18
     */


    default String generateRecordInfoObjectName(String orderId, String taskId) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String strDate = dtf.format(now);

        return String.format("%s/recordInfo/%s/%s/%s/%s", CommonContent.RESOURCE_OSS_PREFIX, strDate, StringUtils.isNotEmpty(orderId) ? orderId : "null", taskId, StUtils.getUUID());
    }

    /**
     * 生成资源下载对应的ObjectName
     *
     * @param version 版本号
     * @param extName 后缀名
     * @return
     * @author Logan
     * @date 2020-09-16 10:18
     */
    default String generateResouceObjectName(String version, String extName) {

        return String.format("%s/%s/%s.%s", CommonContent.RESOURCE_OSS_PREFIX, version, StUtils.getUUID16(), extName);
    }


    /**
     * 生成recordInfo对应的ObjectName
     */
    default String generateMaterialObjectName(String materialName) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String strDate = dtf.format(now);

        return String.format("%s/recordInfo/%s/%s/%s", CommonContent.RESOURCE_OSS_PREFIX, strDate, StUtils.getUUID(), materialName);
    }


}
