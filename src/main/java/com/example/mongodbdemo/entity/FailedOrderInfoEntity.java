package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

/**
 * @Author xieyitong  <xieyitong@situdata.com>
 * @Date 2020/9/5 11:35
 **/
@Data
@Document(collation = "failed_order_info")
public class FailedOrderInfoEntity {

    @Id
    private String Id;
    private String taskId;
    private String orderId;
    private Integer Code;
    private String Msg;
    private String insuranceNo;
    private Timestamp createTime;

}