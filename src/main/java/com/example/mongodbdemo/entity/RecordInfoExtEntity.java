package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 维护了 recordInfo订单信息表与版本的关系表
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/8 1:40
 **/
@Data
@Document(collation = "record_info_ext")
public class RecordInfoExtEntity {

    @Id
    private String id;
    /**
     * recordInfo主键
     */
    private Integer rid;
    /**
     * 版本Id
     */
    private Integer versionId;
    /**
     * recordInfo订单信息是否是最后一个
     */
    private Integer last;
    /**
     * 平台
     */
    private String platform;


}
