package com.example.mongodbdemo.service.impl;

import com.example.mongodbdemo.entity.TaskInfoEntity;
import com.example.mongodbdemo.service.TaskInfoService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/21 11:54
 **/
@Service
public class TaskInfoServiceImpl implements TaskInfoService {

//    @Autowired
//    private TaskInfoMapper taskInfoMapper;

    /**
     * 根据时间查询主键
     *
     * @param uploadTime
     * @return
     */
    @Override
    public List<Integer> findTaskIdByUploadTime(Timestamp uploadTime) {
//        return taskInfoMapper.findTaskIdByUploadTime(uploadTime);
        return null;
    }

    @Override
    public Integer findVersionIdByTaskId(String taskId) {
//        return taskInfoMapper.findVersionIdByTaskId(taskId);
        return null;
    }

    @Override
    public TaskInfoEntity findTaskInfoByTaskId(String taskId) {
//        return taskInfoMapper.findAllByTaskId(taskId);
        return null;
    }


}
