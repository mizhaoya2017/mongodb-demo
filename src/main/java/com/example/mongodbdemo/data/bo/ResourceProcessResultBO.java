package com.example.mongodbdemo.data.bo;

import lombok.Data;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/25 21:26
 **/
@Data
public class ResourceProcessResultBO {

    /**
     * 资源处理结果
     */
    private String encryptRecordInfoProcessResult;
    /**
     * 下载状态
     */
    private Integer downLoadStatus;

}
