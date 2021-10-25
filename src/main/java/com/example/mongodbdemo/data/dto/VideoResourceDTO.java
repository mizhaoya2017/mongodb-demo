package com.example.mongodbdemo.data.dto;

import lombok.Data;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/13 10:53
 **/
@Data
public class VideoResourceDTO {

    /**
     * 视频组title
     */
    private String title;
    /**
     * 视频组
     */
    private VideoResourceInfoDTO[] videoList;
}
