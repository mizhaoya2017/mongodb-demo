package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/7/23 12:43
 **/
@Data
@Document(collation = "operation_log")
public class OperationLogEntity {

    @Id
    private String id;
    private Integer actId;
    private String actCode;
    private String actDesc;
    private String msg;
    private String orderId;
    private String taskId;
    private String url;
    private Timestamp createTime;
    private Integer operateType;
    private Timestamp relativeTime;
    private String productName;
    private String productCode;
}
