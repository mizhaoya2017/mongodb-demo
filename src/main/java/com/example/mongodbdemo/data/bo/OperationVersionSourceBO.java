package com.example.mongodbdemo.data.bo;

import lombok.Data;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/18 16:51
 **/
@Data
public class OperationVersionSourceBO {

    /**
     * 页面url
     */
    private String pageUrl;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 平台信息
     */
    private String platform;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * task id
     */
    private String taskId;

    /**
     * 版本id
     */
    private Integer versionId;

    /**
     * 版本号
     */
    private String version;


}
