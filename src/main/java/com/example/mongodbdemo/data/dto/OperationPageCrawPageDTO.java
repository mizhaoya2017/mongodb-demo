package com.example.mongodbdemo.data.dto;

import lombok.Data;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/14 14:37
 **/
@Data
public class OperationPageCrawPageDTO {
    /**
     * 当前页
     */
    private Integer currPage;
    /**
     * 每页显示条数
     */
    private Integer perPage;

    /**
     * 检索条件 -- 产品名称
     */
    private String productName;

    /**
     * 版本id
     */
    private Integer versionId;

    /**
     * 检索条件 -- 渠道
     */
    private String channel;
    /**
     * 检索条件 -- 平台
     */
    private String platform;


}
