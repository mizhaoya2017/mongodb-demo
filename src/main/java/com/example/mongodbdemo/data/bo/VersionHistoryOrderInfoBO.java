package com.example.mongodbdemo.data.bo;

import lombok.Data;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/10/24 18:35
 **/
@Data
public class VersionHistoryOrderInfoBO {

    /**
     * 版本号
     */
    private String version;
    /**
     * 版本更新时间
     */
    private Timestamp recordVersionTime;
    /**
     * 版本主键
     */
    private Integer currentVersionId;

    /**
     * 版本类型 1=H5,2=小程序，3=APP
     */
    private Integer versionType;

    /**
     * 产品编码
     */
    private String productCode;
    /**
     * 渠道
     */
    private String channel;
    /**
     * 平台
     */
    private String platform;
}
