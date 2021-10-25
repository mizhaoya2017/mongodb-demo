package com.example.mongodbdemo.oss.config;

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
public class H5ImgConfig {

    @Bean("h5ImgYum")
    @Profile({"dev", "profile", "staging","beta"})
    public YamlMapFactoryBean ossAliyunYamldev() {
        try {
            Resource resource = new ClassPathResource("oss/h5img-dev.yml");
            YamlMapFactoryBean yaml = new YamlMapFactoryBean();
            yaml.setResources(resource);
            return yaml;
        } catch (Exception e) {
            log.error("加载云影像yml失败", e);
            return null;
        }
    }

    @Bean("h5ImgYum")
    @Profile("formal")
    public YamlMapFactoryBean ossAliyunYamlPreonline() {
        try {
            Resource resource = new ClassPathResource("oss/h5img-formal.yml");
            YamlMapFactoryBean yaml = new YamlMapFactoryBean();
            yaml.setResources(resource);
            return yaml;
        } catch (Exception e) {
            log.error("加载云影像yml失败", e);
            return null;
        }
    }


//    @Autowired
//    @Qualifier("ossAliyunYum")
//    private YamlMapFactoryBean ossAliyunYml;


    @Bean
    public H5ImgProperties h5ImgProperties(@Qualifier("h5ImgYum") YamlMapFactoryBean ossAliyunYml) {
        try {
            Map<String, Object> yamlObject = ossAliyunYml.getObject();
//            AliyunOssProperties aliyunProperties = new AliyunOssProperties();
//            String accessId = yamlObject.get("access_id").toString();
//            String accessSecret = yamlObject.get("access_secret").toString();
//            String bucketName = yamlObject.get("bucket_name").toString();
//            String endpoint = yamlObject.get("oss_endpoint").toString();
//            aliyunProperties.setAccessId(accessId);
//            aliyunProperties.setAccessSecret(accessSecret);
//            aliyunProperties.setBucketName(bucketName);
//            aliyunProperties.setOssEndpoint(endpoint);
            H5ImgProperties h5ImgProperties = new H5ImgProperties();
            String comCode = yamlObject.get("com_code").toString();
            String comName = yamlObject.get("com_name").toString();
            String operator = yamlObject.get("operator").toString();
            String operatorName = yamlObject.get("operator_name").toString();
            String operatorRole = yamlObject.get("operator_role").toString();
            String businessType = yamlObject.get("business_type").toString();
            String bussCom = yamlObject.get("buss_com").toString();
            String imgType = yamlObject.get("img_type").toString();
            String imgTypeName = yamlObject.get("img_type_name").toString();
            String uploadNode = yamlObject.get("upload_node").toString();
            String serviceUrl = yamlObject.get("service_url").toString();
            h5ImgProperties.setBusinessType(businessType);
            h5ImgProperties.setComCode(comCode);
            h5ImgProperties.setComName(comName);
            h5ImgProperties.setOperatorName(operatorName);
            h5ImgProperties.setOperatorRole(operatorRole);
            h5ImgProperties.setBussCom(bussCom);
            h5ImgProperties.setImgType(imgType);
            h5ImgProperties.setImgTypeName(imgTypeName);
            h5ImgProperties.setUploadNode(uploadNode);
            h5ImgProperties.setServiceUrl(serviceUrl);
            h5ImgProperties.setOperator(operator);
            return h5ImgProperties;
        } catch (Exception e) {
            log.error("加载云影像配置异常", e);
            return null;
        }
    }


//    @Bean
//    public OSSClient getOssClient(AliyunOssProperties ossProperties) {
//        if (ossProperties != null) {
//            try {
//                return new OSSClient(ossProperties.getOssEndpoint(), ossProperties.getAccessId(), ossProperties.getAccessSecret());
//            } catch (Exception e) {
//                log.error("获取阿里云OssClient异常", e);
//                return null;
//            }
//        } else {
//            return null;
//        }
//    }
}
