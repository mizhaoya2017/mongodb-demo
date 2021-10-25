package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/6 11:28
 **/
@Data
@Document(collation = "operation_version_applet_product")
public class OperationVersionAppletProductEntity {

    @Id
    private Integer id;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 创建时间
     */
    private Timestamp createTime;

}
