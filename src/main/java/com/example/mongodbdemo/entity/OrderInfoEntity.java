package com.example.mongodbdemo.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.sql.Timestamp;

@Data
@Document(collation = "order_info")
public class OrderInfoEntity {
    @Id
    private Integer id;
    private String orderId;
    private String policyId;
    private String productCode;
    private String productName;
    private String applicantName;//投保人姓名
    private String platform;   //平台类型： Web，H5等
    private String account;
    private String channel;
    private Integer last;   //是否是最后一个task  1-是，0-不是
    private String extraInfo;  //订单其他信息
    private Integer success;   //是否购买成功，0=失败，1=成功
    private Timestamp uploadTime;
    private String idCard;
    private String businessType; // 业务类型
    private String agencyName;
    private String agencyCode;
    private Timestamp createTime;
    private Integer versionId;
    private Integer productGranularityVersionId; // 产品粒度的版本id

}
