package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @program: optrace
 * @description: 资源替换，太平财的资源，在抓取之前需要替换一下
 * @author: Logan
 * @create: 2020-09-22 21:08
 **/
@Data
@Document(collation = "resource_host_replace")
public class ResourceHostReplaceEntity {
    @Id
    private String id;
    /**
    * 原始host
    */
    private String hostOriginal;
    /**
    * 替换后host
    */
    private String hostReplace;
}
