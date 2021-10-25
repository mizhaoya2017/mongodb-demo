package com.example.mongodbdemo.excepition;

/**
 * @Author xieyitong  <xieyitong@situdata.com>
 * @Date 2020/8/15 14:16
 **/
public class FileException extends RuntimeException{
    public FileException(String message) {
        super(message);
    }

    public FileException(String message, Throwable cause) {
        super(message, cause);
    }
}