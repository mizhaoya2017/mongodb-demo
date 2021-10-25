package com.example.mongodbdemo.data.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/7/23 18:11
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordOrderInfoVO {
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 录制任务ID，可回溯编码
     */
    private String taskId;

    /**
     * 保单号
     */
    private String insuranceNo;
    /**
     * 渠道
     */
    private String channel;

    /**
     * 业务类型
     */
    private String businessType;
    /**
     * 机构名称
     */
    private String agencyName;
    /**
     * 机构编码
     */
    private String agencyCode;
    
    /**
     * 平台：web、微信、小程序、app、H5
     */
    private String platform;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 产品编码
     */
    private String productCode;
    /**
     * 当前客户账号，可能是微信ID、手机号
     */
    private String account;
    /**
     * 投保人
     */
    private String policyHolder;
    /**
     * 投保时间
     */
    private Timestamp createTime;
    /**
     * 额外信息
     */
    private String extraInfo;

    /**
     * 是否购买成功
     */
    private Integer success;


}
