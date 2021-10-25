package com.example.mongodbdemo.service;

import com.example.mongodbdemo.entity.RecordOssInfoEntity;

import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/18 0:48
 **/
public interface RecordOssInfoService {

    /**
     * 根据主键id查询数据
     *
     * @param id 主键
     * @return
     */
    RecordOssInfoEntity findAllById(Integer id);

    /**
     *
     * @param orderId
     * @param taskId
     * @return
     */
    List<RecordOssInfoEntity> findAllByOrderIdAndTaskId(String orderId, String taskId);


    List<Integer> findIdByTaskIdAndOrderId(List<String> taskIds);

    /**
     * 根据主键更新下载状态
     * @param id
     * @param downLoadStatus
     */
    void updateDownLoadStatusById(Integer id, Integer downLoadStatus);
}
