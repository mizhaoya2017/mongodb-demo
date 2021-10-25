package com.example.mongodbdemo.data.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/13 10:44
 **/
@Data
public class RecordResourceVO {

    /**
     * 图片资源数组
     */
    private List<ImageResourceVO> image = new ArrayList<>();

    /**
     * 视频数组
     */
    private List<VideoResourceVO> video = new ArrayList<>();

}
