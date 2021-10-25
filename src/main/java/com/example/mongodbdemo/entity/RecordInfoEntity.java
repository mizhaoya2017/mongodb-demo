package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

/**
 * description:
 * Created by whq on 2020/7/23
 */
@Document(collation = "record_info")
@Data
public class RecordInfoEntity {
    @Id
    private String id;
    private String orderId;
    private String taskId;
    private Integer last;   //是否是最后一个task  1-是，0-不是
    private String recordInfo; //操作视频信息
    private Timestamp uploadTime;
    private String fileDownloadUri; // 下载路径
    private Integer downloadStatus; // 资源处理状态(0=未下载； 1=已下载)
    private Integer recordIndex;//录制片段顺序
    private Integer recordMode; //1:整段录制，2：时间片录制
}