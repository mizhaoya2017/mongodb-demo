package com.example.mongodbdemo.data.vo;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/10/15 21:08
 **/
@Data
public class GetTaskDetailVO {
    /**
     * taskId
     */
    private String taskId;
    /**
     * 客服资源表主键id
     */
    private Integer id;
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
     * 创建时间
     */
    private Timestamp createTime;
}
