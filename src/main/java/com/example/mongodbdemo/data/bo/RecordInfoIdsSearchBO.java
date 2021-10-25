package com.example.mongodbdemo.data.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/31 20:05
 **/
@Data
@NoArgsConstructor
public class RecordInfoIdsSearchBO {
    /**
     * 订单号
     */
    private String orderId;
    /**
     * taskId（可回溯编码）
     */
    private List<String> taskId;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 渠道
     */
    private String channel;

    /**
     * 平台
     */
    private String platform;

    /**
     * 抓取时间(创建时间)
     */
    private Timestamp createTime;

}
