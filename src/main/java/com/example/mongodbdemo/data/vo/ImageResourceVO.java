package com.example.mongodbdemo.data.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/13 10:46
 **/
@Data
public class ImageResourceVO {

    /**
     * 图片组title
     */
    private String title;
    /**
     * 图片组
     */
    private List<ImageResourceInfoVO> imageList;
}
