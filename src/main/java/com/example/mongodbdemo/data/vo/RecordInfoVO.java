package com.example.mongodbdemo.data.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * description:
 * Created by whq on 2020/7/23
 */
@Getter
@Setter
public class RecordInfoVO {
    private List<String> recordInfo; //录制内容加密字符串
    private boolean finished = true;
    public RecordInfoVO(){}
    private Integer encodeMode;


}
