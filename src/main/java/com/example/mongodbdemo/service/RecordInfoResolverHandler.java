package com.example.mongodbdemo.service;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/1 16:49
 **/
public interface RecordInfoResolverHandler {


    /**
     * recordInfo 资源下载保存
     *
     * @param recordInfoPlaintext recordInfo明文
     * @return
     */
    String process(String recordInfoPlaintext, Integer resourceInfoId, String bucketName, String filePathPrefix, String version);
}
