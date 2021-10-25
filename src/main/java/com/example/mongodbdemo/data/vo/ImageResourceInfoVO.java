package com.example.mongodbdemo.data.vo;

import lombok.Data;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/13 10:48
 **/
@Data
public class ImageResourceInfoVO {

    /**
     * 图片名称
     */
    private String name;
    /**
     * base64 字符串
     */
    private String encode;
}
