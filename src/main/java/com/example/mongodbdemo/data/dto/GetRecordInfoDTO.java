package com.example.mongodbdemo.data.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * description:
 * Created by whq on 2020/7/23
 */
@Getter
@Setter
public class GetRecordInfoDTO {
    private String taskId; //任务id
    private Integer pos;
    private Integer size = 10;
}
