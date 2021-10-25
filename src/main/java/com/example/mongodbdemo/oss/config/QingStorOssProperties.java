package com.example.mongodbdemo.oss.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @program: optrace
 * @description: 青云oss 属性
 * @author: Logan
 * @create: 2020-09-05 13:16
 **/
@Getter
@Setter
public class QingStorOssProperties {
    private String accessKey;
    private String accessSecret;
    private String prototol;
    private String host;
    private String zoneKey;
    private String bucketName;
}
