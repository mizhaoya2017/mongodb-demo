package com.example.mongodbdemo.oss.service.impl;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.example.mongodbdemo.data.vo.RespCode;
import com.example.mongodbdemo.excepition.ResultException;
import com.example.mongodbdemo.oss.config.ZanhuaOssProperties;
import com.example.mongodbdemo.oss.service.OSSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Date;

import static com.example.mongodbdemo.content.CommonContent.OSS_EXPIRED_MINUTES;


/**
 * @program: optrace
 * @description: 华赞OSSService实现类
 * @author: Logan
 * @create: 2020-09-16 12:03
 **/
@Service("zanhuaOssService")
@Slf4j
public class ZanhuaOssServiceImpl implements OSSService {
    @Autowired(required = false)
    private ZanhuaOssProperties zanhuaOssProperties;
    @Autowired(required = false)
    private AmazonS3 s3Client;

    /**
     * 保存一个对象，返回对象所在地址
     *
     * @param objectName 例如 /video/video.mp4
     * @param input
     * @return
     * @author Logan
     * @date 2020-09-05 09:49
     */
    @Override
    public void putObject(String objectName, InputStream input) {
        boolean success =false;
        int times = 0;
        while(!success && times <= TryCount) {
            times++;
            try {
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(input.available());
                PutObjectResult putFileResult = s3Client.putObject(zanhuaOssProperties.getBucketName(), objectName, input, objectMetadata);
                if (putFileResult != null && putFileResult.getETag() != null) {
                    success = true;
                    log.info("赞华上传流成功,对象etag:" + putFileResult.getETag());
                } else {
                    log.error("赞华上传流失败");
                    throw new ResultException(RespCode.ERROR_3000_);
                }
            } catch (UnsupportedEncodingException e) {
                log.error("赞华上传流失败", e);
                throw new ResultException(RespCode.ERROR_3000_);
            } catch (IOException e) {
                log.error("赞华上传流失败", e);
                throw new ResultException(RespCode.ERROR_3000_);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        log.error("IOException,objectName={}", objectName, e);
                        throw new ResultException(RespCode.ERROR_3004_);
                    }

                }
            }
        }
    }

    @Override
    public void putObject(String objectName, String content) {
        boolean success =false;
        int times = 0;
        while(!success && times <= TryCount ) {
            times ++;
            PutObjectResult putResult = s3Client.putObject(zanhuaOssProperties.getBucketName(), objectName, content);
            if (putResult != null && putResult.getETag() != null) {
                success =true;
                log.info("赞华上传文本,对象etag:" + putResult.getETag());
            } else {
                log.error("赞华上传文本失败");
                throw new ResultException(RespCode.ERROR_3000_);
            }
        }
    }

    /**
     * 判断是否超时
     *
     * @param code
     * @return
     * @author Logan
     * @date 2020-09-24 11:19
     */
    @Override
    public boolean timeoutAndWait(String code) {
        return false;
    }

    /**
     * 根据bucket,key返回对象数据流
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 09:51
     */
    @Override
    public String getObjectContentString(String objectName) {
        return s3Client.getObjectAsString(zanhuaOssProperties.getBucketName(), objectName);
    }

    /**
     * 获取浏览地址（有有效期），不直接暴露给前端，需要通过本服务器重定向
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 18:23
     */
    @Override
    public String getObjectSignatureUrl(String objectName) {
        Date expiration = new Date(System.currentTimeMillis() + OSS_EXPIRED_MINUTES * 60L * 1000L);
        try {
            URL downloadUrl = s3Client.generatePresignedUrl(zanhuaOssProperties.getBucketName(), objectName, expiration, HttpMethod.GET);
            if (downloadUrl != null) {
                return downloadUrl.toString();
            } else {
                log.error("生成的URL为空，生成失败,objectName:{}", objectName);
                return null;
            }
        } catch (Exception e) {
            log.error("生成浏览地址失败,objectName:{}", objectName, e);
            return null;
        }
    }

    /**
     * 删除一个对象
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 18:35
     */
    @Override
    public void deleteObject(String objectName) {
        s3Client.deleteObject(zanhuaOssProperties.getBucketName(), objectName);
        log.warn("oss删除对象，{}", objectName);
    }

    @Override
    public String getBucketName() {
        return zanhuaOssProperties.getBucketName();
    }
}
