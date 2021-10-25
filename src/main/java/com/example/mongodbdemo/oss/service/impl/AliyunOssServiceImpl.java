package com.example.mongodbdemo.oss.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.example.mongodbdemo.data.vo.RespCode;
import com.example.mongodbdemo.excepition.ResultException;
import com.example.mongodbdemo.oss.config.AliyunOssProperties;
import com.example.mongodbdemo.oss.service.OSSService;
import com.example.mongodbdemo.utils.StUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static com.example.mongodbdemo.content.CommonContent.OSS_EXPIRED_MINUTES;

/**
 * @program: optrace
 * @description: 阿里云OSS服务
 * @author: Logan
 * @create: 2020-09-14 22:07
 **/
@Service("aliyunOssService")
@Slf4j
public class AliyunOssServiceImpl implements OSSService {
    @Autowired(required = false)
    private OSS ossClient;
    @Autowired(required = false)
    private AliyunOssProperties aliyunOssProperties;

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
        boolean success = false;
        int times = 0; // -1的时候停止尝试
        try {
            while (!success && times != STOP_TRY_TIMES_VALUE && times <= TryCount) {
                times++;
                try {
                    ossClient.putObject(aliyunOssProperties.getBucketName(), objectName, input);
                    success = true;
                } catch (OSSException oe) {
                    logOssException(oe);
                    if (!timeoutAndWait(oe.getErrorCode())) {
                        times = STOP_TRY_TIMES_VALUE;
                    }
                } catch (ClientException ce) {
                    logClientExpception(ce);
                    if (!timeoutAndWait(ce.getErrorCode())) {
                        times = STOP_TRY_TIMES_VALUE;
                    }
                }
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logIOException(e);
                }

            }
        }
    }

    @Override
    public boolean timeoutAndWait(String code){
        if(code.equals("RequestTimeout")){
            //超时，线程停止2s时间再尝试
            StUtils.ThreadWatting(TRY_WATTING_MILLS);
            return true;
        }else{
            return false;
        }
    }
    @Override
    public void putObject(String objectName, String content) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes())) {
            this.putObject(objectName, byteArrayInputStream);
        } catch (IOException e) {
            log.error("阿里云存储字符串失败", e);
            throw new ResultException(RespCode.ERROR_3000_);
        }
    }


    /**
     * 根据bucket,key返回对象字符串
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 09:51
     */
    @Override
    public String getObjectContentString(String objectName) {
        int times = 0; // -1的时候停止尝试
        while (times != STOP_TRY_TIMES_VALUE && times <= TryCount) {
            times++;
            try (OSSObject ossObject = ossClient.getObject(aliyunOssProperties.getBucketName(), objectName);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (OSSException oe) {
                logOssException(oe);
                if(!timeoutAndWait(oe.getErrorCode())){
                    times = STOP_TRY_TIMES_VALUE;
                }
            } catch (ClientException ce) {
                logClientExpception(ce);
                if(!timeoutAndWait(ce.getErrorCode())){
                    times = STOP_TRY_TIMES_VALUE;
                }
            } catch (IOException ie) {
                logIOException(ie);
                times = STOP_TRY_TIMES_VALUE;
            }
        }
        return null;
    }

    /**
     * 获取浏览地址（有有效期）
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 18:23
     */
    @Override
    public String getObjectSignatureUrl(String objectName) {
        LocalDateTime now = LocalDateTime.now();
        Date expiredAt = localDateTime2Date(now.plusMinutes(OSS_EXPIRED_MINUTES));
        try {
            URL url = ossClient.generatePresignedUrl(aliyunOssProperties.getBucketName(), objectName, expiredAt);
            return url.toString().replace("http:","https:");
        } catch (ClientException ce) {
            logClientExpception(ce);
        }
        return null;
    }

    /**
     * LocalDateTime转换为Date
     *
     * @param localDateTime
     */
    private Date localDateTime2Date(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);//Combines this date-time with a time-zone to create a  ZonedDateTime.
        Date date = Date.from(zdt.toInstant());
        return date;
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
            ossClient.deleteObject(aliyunOssProperties.getBucketName(), objectName);
        } catch (OSSException oe) {
            logOssException(oe);
        } catch (ClientException ce) {
            logClientExpception(ce);
        }
    }

    @Override
    public String getBucketName() {
        return aliyunOssProperties.getBucketName();
    }


    private void logOssException(OSSException oe) {
        log.error("Caught an OSSException, which means your request made it to OSS, "
                + "but was rejected with an error response for some reason.");
        log.error("Error Message: {}", oe.getErrorMessage());
        log.error("Error Code: {} ", oe.getErrorCode());
        log.error("Request ID: {}  ", oe.getRequestId());
        log.error("Host ID:  {}    ", oe.getHostId());
    }

    private void logClientExpception(ClientException ce) {
        log.error("Caught an ClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with OSS, "
                + "such as not being able to access the network.");
        log.error("Error Message: {}", ce.getMessage());
    }

    private void logIOException(IOException ie) {
        log.error("OSS IOException", ie);
        throw new ResultException(RespCode.ERROR_3004_);
    }

    public static void main(String[] args) {
        String s = new AliyunOssServiceImpl().generateResouceObjectName(null, "123");
        System.out.println("-------------> " + s);
    }
}
