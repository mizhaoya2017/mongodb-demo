package com.example.mongodbdemo.excepition;


import com.alibaba.fastjson.JSONObject;
import com.example.mongodbdemo.data.vo.RespCode;
import com.example.mongodbdemo.data.vo.Result;

/**
 * @Description : ResultException
 * @Date : 2019/8/8
 * @Author : zhudakang@situdata.com
 */
public class ResultException extends RuntimeException {
    public ResultException(RespCode respCode, String result) {
        super(JSONObject.toJSONString(new Result(respCode, result)));
    }

    public ResultException(RespCode respCode) {
        super(JSONObject.toJSONString(new Result(respCode, null)));
    }

    public ResultException(int code, String msg, Object result) {
        super(JSONObject.toJSONString(new Result(code, msg, result)));
    }

    public ResultException(int code, String msg) {
        super(JSONObject.toJSONString(new Result(code, msg, null)));
    }
}
