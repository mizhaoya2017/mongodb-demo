package com.example.mongodbdemo.data.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/13 10:53
 **/
@Data
public class VideoResourceVO {

    /**
     * 视频组title
     */
    private String title;
    /**
     * 视频组
     */
    private List<VideoResourceInfoVO> videoList;
}
