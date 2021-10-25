package com.example.mongodbdemo.oss.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @program: optrace
 * @description:
 * @author: Logan
 * @create: 2020-09-14 21:41
 **/
@Getter
@Setter
public class AliyunOssProperties {
    private String accessId;
    private String accessSecret;
    private String bucketName;
    private String ossEndpoint;
}
