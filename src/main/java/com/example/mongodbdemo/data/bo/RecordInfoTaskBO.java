package com.example.mongodbdemo.data.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 * Created by whq on 2020/7/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordInfoTaskBO {

    /**
     * 订单id
     */
    private String orderId;
    /**
     * 视频主键id(oss/mysql)
     */
    private Integer resourceInfoId;
    /**
     * taskId
     */
    private String taskId;
    /**
     * 视频信息
     */
    private String recordInfo;

    private String bucketName;
    /**
     * objectName(oss path)
     */
    private String objectName;

    /**
     * 是否Oss保存数据
     */
    private Integer isOss;

}