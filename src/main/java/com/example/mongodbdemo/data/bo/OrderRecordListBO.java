package com.example.mongodbdemo.data.bo;

import lombok.Data;

import java.sql.Date;

/**
 * 可回溯列表请求VO
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/7/23 20:03
 **/
@Data
public class OrderRecordListBO {

    /**
     * 是否包含orderId
     */
    public Integer cateOrder;

    /**
     * 检索条件, 订单号
     */
    public String orderId;

    /**
     * 检索条件， 保单号
     */
    public String insuranceNo;

    /**
     * 检索条件， 可回溯编码
     */
    public String taskId;

    /**
     * 检索条件， 账号
     */
    public String account;
    /**
     * 检索条件， 产品名称
     */
    public String productName;
    /**
     * 检索条件， 产品编码
     */
    public String productCode;
    /**
     * 检索条件， 投保人姓名
     */
    public String policyHolder;

    /**
     * 检索条件， 平台
     */
    public String platform;

    /**
     * 检索条件， 渠道
     */
    public String channel;

    /**
     * 检索时间，提交开始时间
     */
    public Date uploadStartTime;
    /**
     * 检索时间，提交结束时间
     */
    public Date uploadEndTime;

}
