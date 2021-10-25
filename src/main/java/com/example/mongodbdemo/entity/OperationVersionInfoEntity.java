package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Wang XinXin
 * @email wangxinxin@situdata.com
 * @date 2020-10-21 10:31:48
 */
@Data
@Document(collation = "operation_version_info")
public class OperationVersionInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @Id
    private String id;
    /**
     * 产品主键id
     */
    private Integer productId;
    /**
     * 版本号
     */
    private String version;
    /**
     * 版本更新内容
     */
    private String updateContext;
    /**
     * 责任人记录
     */
    private String responsibleRecord;
    /**
     * 版本开始时间
     */
    private Date versionTime;
    /**
     * 系统使用的版本号，在整个系统中唯一
     */
    private String systemVersion;
    /**
     * 是否是系统版本
     */
    private Integer versionCategory;
    /**
     * 记录创建时间
     */
    private Date createTime;


}
