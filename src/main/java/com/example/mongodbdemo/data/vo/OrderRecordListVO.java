package com.example.mongodbdemo.data.vo;

import com.example.mongodbdemo.data.bo.OrderRecordListBO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;


/**
 * 可回溯列表请求VO
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/7/23 20:03
 **/
@Data
public class OrderRecordListVO {
    /**
     * 当前页
     */
    public Integer currPage;
    /**
     * 是否包含orderId
     */
    public Integer cateOrder;
    /**
     * 每页显示条数
     */
    public Integer perPage;
    /**
     * 检索条件, 订单号
     */
    public String orderId;

    /**
     * 检索条件， 保单号
     */
    public String insuranceNo;

    /**
     * 是否显示有保单号的（1=检索有保单号的，0=可以检索保单号为空的）
     */
    public Integer insuranceNoShow;

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
     * 检索条件， 业务类型
     */
    public String businessType;

    /**
     * 检索条件， 机构名称
     */
    public String agencyName;

    /**
     * 检索条件， 机构编码
     */
    public String agencyCode;

    /**
     * 检索时间，提交开始时间
     */
    public Date uploadStartTime;
    /**
     * 检索时间，提交结束时间
     */
    public Date uploadEndTime;
    /**
     * 用户业务类型
     */
    public List<String> businessTypeList;
    /**
     * 用户渠道
     */
    public List<String> channelList;
    /**
     * 机构
     */
    public List<String> agencyList;



    /**
     * 适配/转换
     *
     * @param orderRecordListVO
     * @return
     */
    public static OrderRecordListBO convert(OrderRecordListVO orderRecordListVO) {

        OrderRecordListBO orderRecordListBO = new OrderRecordListBO();
        BeanUtils.copyProperties(orderRecordListVO, orderRecordListBO);
        return orderRecordListBO;

    }

}
