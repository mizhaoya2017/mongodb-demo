package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/13 11:14
 **/
@Data
@Document(collation = "record_resource")
public class RecordResourceEntity {

    @Id
    private String id;
    private String taskId;
    private String title;
    private String resource;
    private String resourceType;
    private String resourceName;
    private Timestamp createTime;
}
