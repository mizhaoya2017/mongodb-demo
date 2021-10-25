package com.example.mongodbdemo.data.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * description:
 * Created by whq on 2020/7/24
 */
@Getter
@Setter
public class CreateRecordInfoVO {
    private String taskId;

    public CreateRecordInfoVO(String taskId) {
        this.taskId = taskId;
    }
}
