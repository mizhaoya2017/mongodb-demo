package com.example.mongodbdemo.data.vo;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/7/27 19:13
 **/
@Data
public class OperationInfoVO {

    private String code;
    private String codeDesc;
    private String msg;
    private Timestamp createTime;
    private String operateDesc;
    private Integer operateType;
    private Timestamp absoluteTime;
    private Timestamp relativeTime;
    private String productCode;
    private String productName;

}
