package com.example.mongodbdemo.service;

import org.springframework.stereotype.Service;

/**
 * 解析xml
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/22 13:31
 **/
@Service
public interface HtmlHandlerResolver {

    /**
     * 解析
     *
     * @param url              要解析的url
     * @param filePathPrefix   页面保存的磁盘路径
     * @param mainHtmlFileName HTML页面文件名称
     * @return
     */
     String dowmload(String url, String filePathPrefix, String mainHtmlFileName);


}
