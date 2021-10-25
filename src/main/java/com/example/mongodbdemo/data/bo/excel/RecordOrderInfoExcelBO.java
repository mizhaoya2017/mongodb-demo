package com.example.mongodbdemo.data.bo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.example.mongodbdemo.conf.converter.EasyExcelDateConverter;
import lombok.Data;

import java.util.Date;

/**
 * 可回溯列表页
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/10/14 17:02
 **/
@Data
public class RecordOrderInfoExcelBO {

    /**
     * 录制任务ID，可回溯编码
     */
    @ExcelProperty("可回溯编码")
    private String taskId;

    /**
     * 订单号
     */
    @ExcelProperty("订单号")
    private String orderId;


    /**
     * 保单号
     */
    @ExcelProperty("保单号")
    private String insuranceNo;


    /**
     * 业务类型
     */
    @ExcelProperty("业务类型")
    private String businessType;

    /**
     * 渠道
     */
    @ExcelProperty("渠道")
    private String channel;


    /**
     * 平台：web、微信、小程序、app、H5
     */
    @ExcelProperty("平台")
    private String platform;
    /**
     * 产品名称
     */
    @ExcelProperty("产品名称")
    private String productName;
    /**
     * 产品编码
     */
    @ExcelProperty("产品编码")
    private String productCode;
    /**
     * 当前客户账号，可能是微信ID、手机号
     */
    @ExcelProperty("账号")
    private String account;
    /**
     * 投保人
     */
    @ExcelProperty("投保人")
    private String policyHolder;
    /**
     * 提交时间
     * 设置时间转换方式
     */
    @ExcelProperty(value = "提交时间", converter = EasyExcelDateConverter.class)
    private Date createTime;


}
