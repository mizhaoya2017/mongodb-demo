package com.example.mongodbdemo.data.vo;

import lombok.Data;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/7/23 12:29
 **/
@Data
public class CreateOperationVO {

    /**
     * 订单id
     */
    private String orderId;
    /**
     * 任务id
     */
    private String taskId;
    /**
     * 事件类型code
     */
    private String type;
    /**
     * 事件描述
     */
    private String description;
    /**
     * 时间戳(后端不用)
     */
    private String timestamp;

    private String productCode;
    private String productName;


}
