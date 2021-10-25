package com.example.mongodbdemo.oss.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @program: optrace
 * @description:  赞华
 * @author: Logan
 * @create: 2020-09-16 11:40
 **/
@Getter
@Setter
public class ZanhuaOssProperties {
    private String serverAddress;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String fileBasePath;
}
