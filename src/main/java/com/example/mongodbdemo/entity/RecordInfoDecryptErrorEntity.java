package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/12 12:13
 **/
@Data
@Document(collation = "record_info_decrypt_error")
public class RecordInfoDecryptErrorEntity {
    /**
     * 主键id
     */
    @Id
    private Integer id;
    /**
     * recordInfo表主键id
     */
    private Integer rid;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 可回溯编码
     */
    private String taskId;
    /**
     * 创建时间
     */
    private Timestamp createTime;


}
