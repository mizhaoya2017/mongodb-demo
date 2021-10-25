package com.example.mongodbdemo.data.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * description:
 * Created by whq on 2020/7/23
 */
@Data
@NoArgsConstructor
public class OrderInfoDTO implements Serializable {

    private Integer index;                  // 分包序号，从0开始计数
    private String orderId;
    private String insuranceNo;           //保单号
    private String productCode;
    private String productName;
    private String policyHolder;//投保人姓名
    private String platform;   //平台类型： web、微信、小程序、app、H5
    private String account;
    private String channel;
    private String extraInfo;  //订单其他信息
    private Integer success;   //是否购买成功，0=失败，1=成功
    private String businessType; // 业务类型
    private String agencyName; //机构名称
    private String agencyCode; //机构编码
    private String taskId; // 增加taskId

}
