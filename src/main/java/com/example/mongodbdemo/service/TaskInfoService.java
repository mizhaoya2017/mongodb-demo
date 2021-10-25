package com.example.mongodbdemo.service;

import com.example.mongodbdemo.entity.TaskInfoEntity;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/21 11:53
 **/
public interface TaskInfoService {

    /**
     * 根据时间查询主键
     *
     * @return
     */
    List<Integer> findTaskIdByUploadTime(Timestamp uploadTime);

    /**
     * 根据taskId查询版本主键
     *
     * @param taskId
     * @return
     */
    Integer findVersionIdByTaskId(String taskId);

    TaskInfoEntity findTaskInfoByTaskId(String taskId);



}
