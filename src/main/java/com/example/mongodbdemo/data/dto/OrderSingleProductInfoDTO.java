package com.example.mongodbdemo.data.dto;

import lombok.Data;

/**
 * 版本管理-订单列表页下钻订单视频详情
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/5 10:57
 **/
@Data
public class OrderSingleProductInfoDTO {

    /**
     * 订单号
     */
    private String orderId;
    /**
     * taskId（可回溯编码）
     */
    private String taskId;

}
