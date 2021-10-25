package com.example.mongodbdemo.data.bo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @program: optrace
 * @description: 多存储方式下的RecordInfo信息
 * @author: Logan
 * @create: 2020-09-15 16:37
 **/
@Getter
@Setter
public class RecordInfoMultiStorageBO {
    /**
     * task_info的主键
     */
    private Integer tiId;
    /**
     * 可回溯号
     */
    private String taskId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 视频信息
     */
    private String recordInfo;
    /**
     * 是否是最后一个
     */
    private Integer last;
    /**
     * 时间戳
     */
    private Date uploadTime;

    private Integer index;
    private Integer recordMode;
}
