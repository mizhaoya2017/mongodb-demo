package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/12 19:17
 **/
@Data
@Document(collation = "operation_version_resource")
public class OperationVersionResourceEntity {

    @Id
    private String id;
    private Integer resourceInfoId;
    private String originalUrl;
    private String accessUrl;
    private String localUrl;
    private String version;
    private String versionType;
    private String bucketName;
    private Integer isOss;
    private Timestamp createTime;
    /**
     * //引用的连接是检查过，
     * true-检查过，false-未检查过，如果是非js或css的内容，
     * 我们认为是一个原始资源，即内部不含子链接的，这时referedLinkChecked=true
     */
    private Boolean referedLinkChecked;


}
