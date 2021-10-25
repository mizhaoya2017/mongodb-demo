package com.example.mongodbdemo.data.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author : zhudakang
 * @Description : RespResult
 * @Date : 2019/8/8
 */
@Getter
@Setter
public class Result<T> {

    private int code;
    private String msg;
    private T result;

    public Result() {
        super();
    }

    public Result(int code, String msg, T result) {
        this.code = code;
        this.msg = msg;
        this.result = result;
    }

    public Result(RespCode respCode, T result) {
        this.code = respCode.getCode();
        this.msg = respCode.getMsg();
        this.result = result;
    }


    public static <T> Result<T> success(T result) {
        Result<T> res = new Result<>();
        res.setCode(RespCode.SUCC.getCode());
        res.setMsg(RespCode.SUCC.getMsg());
        res.setResult(result);
        return res;
    }

    public static Result success() {
        Result res = new Result<>();
        res.setCode(RespCode.SUCC.getCode());
        res.setMsg(RespCode.SUCC.getMsg());
        res.setResult(null);
        return res;
    }

    public static Result<String> fail(RespCode respCode) {
        Result<String> res = new Result<>();
        res.setCode(respCode.getCode());
        res.setMsg(respCode.getMsg());
        res.setResult(null);
        return res;
    }
}
