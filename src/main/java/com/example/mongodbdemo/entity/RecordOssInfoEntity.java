package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

@Data
@Document(collation = "record_oss_info")
public class RecordOssInfoEntity implements BaseEntity{
    @Id
    private String _id;
    /**
     * 可回溯号
     */
    private String taskId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * task_info的主键
     */
    private Integer tiId;
    /**
     * 路径，objectName
     */
    private String ossPath;
    /**
     * bucket名
     */
    private String bucketName;
    /**
     * 青云独有的
     */
    private String zoneKey;
    /**
     * 上传时间 前端传来的
     */
    private Timestamp uploadTime;
    /**
     * 1:最后一个，0：不是最后一个
     */
    private Integer last;

    /**
     * 资源处理状态(0=未下载； 1=已下载； 2=已处理未替换； 5=正在处理中)
     */
    private Integer downloadStatus;
    /**
     * 录制片段顺序
     */
    private Integer recordIndex;
    /**
     * 1:整段录制，2：时间片录制
     */
    private Integer recordMode;
}
