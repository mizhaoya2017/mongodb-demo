package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

/**
 * @Author xieyitong  <xieyitong@situdata.com>
 * @Date 2020/9/7 19:00
 **/
@Data
@Document(collation = "customer_service_info")
public class CustomerServiceInfoEntity {

    @Id
    private String Id;
    /**
     * taskId
     */
    private String taskId;
    /**
     * 会话id
     */
    private String sessionId;
    /**
     * 角色类型
     */
    private String personType;
    /**
     * 文件内容，保存数据库（逐步废弃）
     */
    private String context;
    /**
     * 文件类型
     */
    private String contextType;
    /**
     * 存储介质存储地址
     */
    private String insteadContext;
    /**
     * 是否存储介质存储
     */
    private Integer instead;
    /**
     * 创建时间
     */
    private Timestamp createTime;

}
