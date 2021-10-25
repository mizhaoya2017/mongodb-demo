package com.example.mongodbdemo.data.dto;

import lombok.Data;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/13 10:46
 **/
@Data
public class ImageResourceDTO {

    /**
     * 图片组title
     */
    private String title;
    /**
     * 图片组
     */
    private ImageResourceInfoDTO[] imageList;
}
