package com.example.mongodbdemo.data.dto;

import lombok.Data;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/13 10:44
 **/
@Data
public class RecordResourceDTO {

    /**
     * 图片资源数组
     */
    private ImageResourceDTO[] image;

    /**
     * 视频数组
     */
    private VideoResourceDTO[] video;

    /**
     * taskId
     */
    private String taskId;
}
