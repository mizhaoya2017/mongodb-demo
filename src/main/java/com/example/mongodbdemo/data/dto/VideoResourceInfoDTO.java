package com.example.mongodbdemo.data.dto;

import lombok.Data;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/13 10:48
 **/
@Data
public class VideoResourceInfoDTO {

    /**
     * 视频名称
     */
    private String name;
    /**
     * 视频路径
     */
    private String path;
}
