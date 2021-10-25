package com.example.mongodbdemo.data.vo;

import com.example.mongodbdemo.data.dto.OrderInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * description:
 * Created by whq on 2020/7/24
 */
@Setter
@Getter
public class TaskDetailVO {
    /**
     * 订单基本信息
     */
    private OrderInfoDTO orderInfo;
    /**
     * 订单额外信息
     */
    private String extraInfo;

    /**
     * 操作日志信息
     */
    private List<OperationInfoVO> operationInfo;

    /**
     * 图片/视频等资源信息
     */
    private RecordResourceVO resourceInfo;
}
