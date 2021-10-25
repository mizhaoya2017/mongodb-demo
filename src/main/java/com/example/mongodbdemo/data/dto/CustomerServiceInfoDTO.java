package com.example.mongodbdemo.data.dto;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @Author xieyitong  <xieyitong@situdata.com>
 * @Date 2020/9/4 11:46
 **/
@Data
public class CustomerServiceInfoDTO {

    private String taskId;
    private String sessionId;
    private String personType;
    private String context;
    private String contextType;
    private Timestamp createTime;
}