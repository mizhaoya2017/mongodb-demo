package com.example.mongodbdemo.service;

import com.example.mongodbdemo.data.vo.CreateOperationVO;
import com.example.mongodbdemo.data.vo.Result;
import com.example.mongodbdemo.entity.OperationLogEntity;

import java.util.List;

/**
 * 操作日志类
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/7/23 12:31
 **/
public interface OperationLogService {

    Result createOperation(String requestURI, CreateOperationVO operationVO);

    List<OperationLogEntity> getAllByTaskId(String taskId);
}
