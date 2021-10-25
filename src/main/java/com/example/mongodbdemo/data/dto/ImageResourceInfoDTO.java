package com.example.mongodbdemo.data.dto;

import lombok.Data;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/13 10:48
 **/
@Data
public class ImageResourceInfoDTO {

    /**
     * 图片名称
     */
    private String name;
    /**
     * base64 字符串
     */
    private String encode;
}
