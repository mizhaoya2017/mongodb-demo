package com.example.mongodbdemo.data.dto;

import lombok.Data;

/**
 * 类型为小程序的订单在详情页获取图片DTO
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/11 16:17
 **/
@Data
public class ResourceImageDTO {

    /**
     * 订单创建时间
     */
    private Long createTime;
    /**
     * 产品编码
     */
    private String productCode;

}
