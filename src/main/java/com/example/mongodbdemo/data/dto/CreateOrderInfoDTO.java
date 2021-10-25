package com.example.mongodbdemo.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 * Created by whq on 2020/7/13
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderInfoDTO {
    private OrderInfoDTO orderInfo;
    private String taskId;   //可回溯编码
}
