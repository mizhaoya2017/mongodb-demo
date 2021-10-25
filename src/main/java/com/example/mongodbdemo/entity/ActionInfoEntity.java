package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/7/23 13:01
 **/
@Data
@Document(collation = "action_info")
public class ActionInfoEntity {

    /**
     * 主键
     */
    @Id
    private String id;
    /**
     * 操作类型 CLICK，INPUT，SCROLL等
     */
    private String code;
    /**
     * 客户自定义操作描述
     */
    private String desc;


}
