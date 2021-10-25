package com.example.mongodbdemo.data.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 将需要下载的资源保存到redis中
 * 定时任务处理
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/14 20:35
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordInfoResourceCacheTO implements Serializable {
    /**
     * recordOssInfo主键id(视频资源主键)
     */
    private Integer resourceInfoId;
    /**
     * 是否是oss存储
     */
    private Integer isOss;
    /**
     * 版本号
     */
    private String version;
    /**
     * 版本类型；1=H5,2=小程序，3=APP
     */
    private Integer versionType;


}
