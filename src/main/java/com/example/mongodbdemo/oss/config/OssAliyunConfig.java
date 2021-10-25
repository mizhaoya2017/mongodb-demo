package com.example.mongodbdemo.oss.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
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
 * @create: 2020-09-14 21:40
 **/
@Configuration
@Slf4j
public class OssAliyunConfig {

    @Bean("ossAliyunYum")
    @Profile({"dev", "profile", "staging","beta"})
    public YamlMapFactoryBean ossAliyunYamldev() {
        try {
            Resource resource = new ClassPathResource("oss/oss-aliyun-dev.yml");
            YamlMapFactoryBean yaml = new YamlMapFactoryBean();
            yaml.setResources(resource);
            return yaml;
        } catch (Exception e) {
            log.error("加载阿里云yml失败", e);
            return null;
        }
    }

    @Bean("ossAliyunYum")
    @Profile("formal")
    public YamlMapFactoryBean ossAliyunYamlPreonline() {
        try {
            Resource resource = new ClassPathResource("oss/oss-aliyun-formal.yml");
            YamlMapFactoryBean yaml = new YamlMapFactoryBean();
            yaml.setResources(resource);
            return yaml;
        } catch (Exception e) {
            log.error("加载阿里云yml失败", e);
            return null;
        }
    }


//    @Autowired
//    @Qualifier("ossAliyunYum")
//    private YamlMapFactoryBean ossAliyunYml;


    @Bean
    public AliyunOssProperties aliyunProperties(@Qualifier("ossAliyunYum") YamlMapFactoryBean ossAliyunYml) {
        try {
            Map<String, Object> yamlObject = ossAliyunYml.getObject();
            AliyunOssProperties aliyunProperties = new AliyunOssProperties();
            String accessId = yamlObject.get("access_id").toString();
            String accessSecret = yamlObject.get("access_secret").toString();
            String bucketName = yamlObject.get("bucket_name").toString();
            String endpoint = yamlObject.get("oss_endpoint").toString();
            aliyunProperties.setAccessId(accessId);
            aliyunProperties.setAccessSecret(accessSecret);
            aliyunProperties.setBucketName(bucketName);
            aliyunProperties.setOssEndpoint(endpoint);
            return aliyunProperties;
        } catch (Exception e) {
            log.error("加载阿里云配置信息异常", e);
            return null;
        }
    }


    @Bean
    public OSS getOssClient(AliyunOssProperties ossProperties) {
        if (ossProperties != null) {
            try {
                return new OSSClientBuilder().build(ossProperties.getOssEndpoint(), ossProperties.getAccessId(), ossProperties.getAccessSecret());
            } catch (Exception e) {
                log.error("获取阿里云OssClient异常", e);
                return null;
            }
        } else {
            return null;
        }
    }
}
