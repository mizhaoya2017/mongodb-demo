package com.example.mongodbdemo.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * @program: optrace
 * @description: 调用js，AES解密
 * @author: Logan
 * @create: 2020-09-11 19:22
 **/
@Component
@Slf4j
public class JSAESUtils {
    @Autowired
    private ScriptEngine engine;

    public String aesDecode(String data) throws ScriptException, NoSuchMethodException {

        if (engine instanceof Invocable) {
            Invocable in = (Invocable) engine;
            String result = (String) in.invokeFunction("decoding", data);
            return result;
        }
        return "";
    }

    public String aesEncode(String data) throws ScriptException, NoSuchMethodException {

        if (engine instanceof Invocable) {
            Invocable in = (Invocable) engine;
            String result = (String) in.invokeFunction("encoding", data);
            return result;
        }
        return data;
    }

}
