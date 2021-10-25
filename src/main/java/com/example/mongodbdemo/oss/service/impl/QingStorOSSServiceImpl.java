package com.example.mongodbdemo.oss.service.impl;

import com.example.mongodbdemo.data.vo.RespCode;
import com.example.mongodbdemo.excepition.ResultException;
import com.example.mongodbdemo.oss.config.QingStorOssProperties;
import com.example.mongodbdemo.oss.service.OSSService;
import com.qingstor.sdk.exception.QSException;
import com.qingstor.sdk.service.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

import static com.example.mongodbdemo.content.CommonContent.OSS_EXPIRED_MINUTES;

/**
 * @program: optrace
 * @description:  青云oss
 * @author: Logan
 * @create: 2020-09-05 09:55
 **/
@Service("qingStorOssService")
@Slf4j
public class QingStorOSSServiceImpl implements OSSService {
     @Autowired(required = false)
     private Bucket qingStorBucket;
     @Autowired
     private QingStorOssProperties qingStorOssProperties;


    /**
     * 保存一个对象，返回对象所在地址
     *
     * @param objectName
     * @param input
     * @return
     * @author Logan
     * @date 2020-09-05 09:49
     */
    @Override
    public void putObject(String objectName, InputStream input) {
        Bucket.PutObjectInput objectInput = new Bucket.PutObjectInput();
        objectInput.setBodyInputStream(input);
        boolean success =false;
        int times = 0;
        while(!success && times <= TryCount ) {
            times ++;
            try {
                Bucket.PutObjectOutput output = qingStorBucket.putObject(objectName, objectInput);
                if (output.getStatueCode() == 201) {
                    success = true;
                    log.info("成功存储对象");
                } else {
                    log.error("存储对象失败，objectName:{},code:{}, statueCode:{}, msg:{},requestId:{}", objectName, output.getCode(), output.getStatueCode(), output.getMessage(), output.getRequestId());
                    log.info("help url {}", output.getUrl());
                    throw new ResultException(RespCode.ERROR_3000_);
                }
            } catch (QSException e) {
                log.error("保存对象异常,objectName={}", objectName, e);
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
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes())){
            this.putObject(objectName,byteArrayInputStream);
        } catch (IOException e) {
            log.error("青云存储字符串失败",e);
            throw new ResultException(RespCode.ERROR_3000_);
        }

    }

    /**
     * 根据bucket,key返回对象数据流
     *
     * @param objectName
     * @return
     * @author Logan
     * @date 2020-09-05 09:51
     */
    @Override
    public String getObjectContentString(String objectName) {
        Bucket.GetObjectInput input = new Bucket.GetObjectInput();
        try {
            Bucket.GetObjectOutput output = qingStorBucket.getObject(objectName, input);
            if(output.getStatueCode()==200){
                try (InputStream inputStream = output.getBodyInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    return sb.toString();
                } catch (IOException e1) {
                    log.error("青云 获取内容字符串，IOException",e1);
                }
                log.info("成功获取存储对象");
            }else{
                log.error("获取对象失败，objectName:{},code:{}, statueCode:{}, msg:{},requestId:{}", objectName,output.getCode(),output.getStatueCode(),output.getMessage(),output.getRequestId());
                log.info("help url {}", output.getUrl());
                throw new ResultException(RespCode.ERROR_3001_);
            }

        } catch (QSException e) {
            log.error("青云getObject失败，objectName:{}", objectName,e);
            throw new ResultException(RespCode.ERROR_3001_);
        }
        return null;
    }


    @Override
    public boolean timeoutAndWait(String code){
        return false;
    }

    @Override
    public String getObjectSignatureUrl(String objectName) {
        long expiresTime = System.currentTimeMillis() / 1000 + 60 * OSS_EXPIRED_MINUTES; // Expired in 600 seconds(10 minutes).
        try {
            return  qingStorBucket.GetObjectSignatureUrl(objectName, expiresTime);
        } catch (QSException e) {
            log.error("获取SignatureUrl失败，objectName:{}", objectName,e);
            throw new ResultException(RespCode.ERROR_3003_);
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
        try {
            Bucket.DeleteObjectOutput output = qingStorBucket.deleteObject(objectName);
            if (output.getStatueCode() == 204) {
                log.info("Delete Object: Object objectName ={} ",objectName);
            } else {
                log.error("获取对象失败，objectName:{},code:{}, statueCode:{}, msg:{},requestId:{}", objectName,output.getCode(),output.getStatueCode(),output.getMessage(),output.getRequestId());
                log.info("help url {}", output.getUrl());
                throw new ResultException(RespCode.ERROR_3002_);
            }
        } catch (QSException e) {
            log.error("删除对象异常,objectName={}",objectName,e);
            throw new ResultException(RespCode.ERROR_3002_);
        }
    }

    @Override
    public String getBucketName() {
        return qingStorOssProperties.getBucketName();
    }
}
