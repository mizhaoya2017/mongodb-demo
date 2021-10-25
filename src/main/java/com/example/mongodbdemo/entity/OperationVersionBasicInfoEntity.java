package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

/**
 * 版本管理 基本信息
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/12 18:53
 **/
@Data
@Document(collation = "operation_version_basic_info")
public class OperationVersionBasicInfoEntity {

    @Id
    private Integer id;
    /**
     * 用户id
     */
    private Integer uid;
    /**
     * 用户姓名
     */
    private String username;
    /**
     * 版本号
     */
    private String version;
    /**
     * 版本更新记录内容
     */
    private String updateRecord;
    /**
     * 责任人记录
     */
    private String responsibleRecord;
    /**
     * 版本类型；1=H5,2=小程序，3=APP
     */
    private Integer versionType;
    /**
     * 产品主键Id
     */
    private Integer productId;
    /**
     * 记录开始时间
     */
    private Timestamp recordTime;
    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 备注
     */
    private String remark;
}
