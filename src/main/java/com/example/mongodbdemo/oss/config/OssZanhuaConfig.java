package com.example.mongodbdemo.oss.config;

import com.amazonaws.services.s3.AmazonS3;
import com.example.mongodbdemo.utils.S3Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Map;

/**
 * @program: optrace
 * @description: 赞华（光大永明）
 * @author: Logan
 * @create: 2020-09-16 11:48
 **/
@Slf4j
@Configuration
public class OssZanhuaConfig {
    @Bean("ossZanhuaYum")
    @Profile({"dev", "profile", "staging", "beta"})
    public YamlMapFactoryBean ossZanhuaYamlstaging() {
        try {
            Resource resource = new ClassPathResource("oss/oss-zanhua-dev.yml");
            YamlMapFactoryBean yaml = new YamlMapFactoryBean();
            yaml.setResources(resource);
            return yaml;
        } catch (Exception e) {
            log.error("加载赞华yml失败", e);
            return null;
        }
    }

    @Bean("ossZanhuaYum")
    @Profile("formal")
    public YamlMapFactoryBean ossZanhuaYamlprofile() {
        try {
            Resource resource = new ClassPathResource("oss/oss-zanhua-formal.yml");
            YamlMapFactoryBean yaml = new YamlMapFactoryBean();
            yaml.setResources(resource);
            return yaml;
        } catch (Exception e) {
            log.error("加载赞华yml失败", e);
            return null;
        }
    }


    @Bean
    public ZanhuaOssProperties zuanhuaProperties(@Qualifier("ossZanhuaYum") YamlMapFactoryBean ossZanhuaYum) {
        try {
            Map<String, Object> yamlObject = ossZanhuaYum.getObject();
            ZanhuaOssProperties zanhuaProperties = new ZanhuaOssProperties();
            String serverAddress = yamlObject.get("serverAddress").toString();
            String accessKey = yamlObject.get("accessKey").toString();
            String secretKey = yamlObject.get("secretKey").toString();
            String bucketName = yamlObject.get("bucketName").toString();
            String fileBasePath = yamlObject.get("fileBasePath").toString();
            zanhuaProperties.setServerAddress(serverAddress);
            zanhuaProperties.setAccessKey(accessKey);
            zanhuaProperties.setSecretKey(secretKey);
            zanhuaProperties.setBucketName(bucketName);
            zanhuaProperties.setFileBasePath(fileBasePath);
            return zanhuaProperties;
        } catch (Exception e) {
            log.error("加载赞华Osss失败", e);
            return null;
        }
    }


    @Bean
    public AmazonS3 s3Client(ZanhuaOssProperties ossProperties) {
        if (ossProperties != null) {
            try {
                S3Client s3Client = new S3Client(ossProperties.getServerAddress(), ossProperties.getAccessKey(), ossProperties.getSecretKey());
                return s3Client.buildClient();
            } catch (Exception e) {
                log.error("获取赞华OssClient异常", e);
                return null;
            }
        } else {
            return null;
        }
    }
}
