package com.example.mongodbdemo.data.dto;

import lombok.Data;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/14 14:37
 **/
@Data
public class OperationVersionBasicInfoPageDTO {
    /**
     * 当前页
     */
    private Integer currPage;
    /**
     * 每页显示条数
     */
    private Integer perPage;
    /**
     * 时间检索-开始时间
     */
    private Long startTime;
    /**
     * 时间检索-结束时间
     */
    private Long endTime;
    /**
     * 模糊查询字段
     */
    private String fuzzyCondition;
    /**
     * 检索条件 - 备注
     */
    private String remark;


}
