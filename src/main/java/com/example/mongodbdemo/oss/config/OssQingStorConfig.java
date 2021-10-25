package com.example.mongodbdemo.oss.config;

import com.qingstor.sdk.config.EnvContext;
import com.qingstor.sdk.service.Bucket;
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
 * @description:
 * @author: Logan
 * @create: 2020-09-05 12:41
 **/
@Configuration
@Slf4j
public class OssQingStorConfig {


    @Bean("qingStorYam")
    @Profile({"dev", "profile", "staging","beta"})
    public YamlMapFactoryBean qingStorYamldev() {
        try {
            Resource resource = new ClassPathResource("oss/oss-qingstor-dev.yml");
            YamlMapFactoryBean yaml = new YamlMapFactoryBean();
            yaml.setResources(resource);
            return yaml;
        } catch (Exception e) {
            log.error("加载青云oss yml失败", e);
            return null;
        }
    }

    @Bean("qingStorYam")
    @Profile("formal")
    public YamlMapFactoryBean qingStorYamlprofile() {
        try {
            Resource resource = new ClassPathResource("oss/oss-qingstor-formal.yml");
            YamlMapFactoryBean yaml = new YamlMapFactoryBean();
            yaml.setResources(resource);
            return yaml;
        } catch (Exception e) {
            log.error("加载青云oss yml失败", e);
            return null;
        }
    }

    @Bean
    public QingStorOssProperties qingStorProperties(@Qualifier("qingStorYam") YamlMapFactoryBean qingStorYam) {
        try {
            Map<String, Object> yamlObject = qingStorYam.getObject();
            QingStorOssProperties qingStorOssProperties = new QingStorOssProperties();
            String accessKey = yamlObject.get("access_key_id").toString();
            String secretAccessKey = yamlObject.get("secret_access_key").toString();
            String protocol = yamlObject.get("protocol").toString();
            String host = yamlObject.get("host").toString();
            String zoneKey = yamlObject.get("zoneKey").toString();
            String bucketName = yamlObject.get("bucketName").toString();
            qingStorOssProperties.setAccessKey(accessKey);
            qingStorOssProperties.setAccessSecret(secretAccessKey);
            qingStorOssProperties.setPrototol(protocol);
            qingStorOssProperties.setHost(host);
            qingStorOssProperties.setZoneKey(zoneKey);
            qingStorOssProperties.setBucketName(bucketName);
            return qingStorOssProperties;
        } catch (Exception e) {
            log.error("加载青云配置信息失败", e);
            return null;
        }
    }

    @Bean
    public EnvContext qingStorEnvDev(QingStorOssProperties qingStorOssProperties) {
        if (qingStorOssProperties != null) {
            return createEnvContextByYaml(qingStorOssProperties);
        } else {
            log.error("初始化青云 EnvContext 失败");
            return null;
        }
    }

    @Bean
    public Bucket qingStorBucket(EnvContext envContext, QingStorOssProperties qingStorOssProperties) {
        if (envContext != null || qingStorOssProperties != null) {
            Bucket bucket = new Bucket(envContext, qingStorOssProperties.getZoneKey(), qingStorOssProperties.getBucketName());
            return bucket;
        } else {
            log.error("初始化青云 Bucket 失败");
            return null;
        }
    }

    private EnvContext createEnvContextByYaml(QingStorOssProperties qingStorOssProperties) {
        if (qingStorOssProperties != null) {
            EnvContext envContext = new EnvContext(qingStorOssProperties.getAccessKey(), qingStorOssProperties.getAccessSecret());
            envContext.setProtocol(qingStorOssProperties.getPrototol());
            envContext.setHost(qingStorOssProperties.getHost());
            return envContext;
        } else {
            log.error("初始化青云 EnvContext 失败");
            return null;
        }
    }

}
