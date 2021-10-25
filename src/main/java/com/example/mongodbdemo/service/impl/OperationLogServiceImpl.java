package com.example.mongodbdemo.service.impl;

import com.example.mongodbdemo.data.vo.CreateOperationVO;
import com.example.mongodbdemo.data.vo.RespCode;
import com.example.mongodbdemo.data.vo.Result;
import com.example.mongodbdemo.entity.ActionInfoEntity;
import com.example.mongodbdemo.entity.OperationLogEntity;
import com.example.mongodbdemo.excepition.ResultException;
import com.example.mongodbdemo.service.OperationLogService;
import com.example.mongodbdemo.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/7/23 12:36
 **/
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

//    @Autowired
//    private OperationLogDao operationLogDao;
//    @Autowired
//    private ActionInfoDao actionInfoDao;

    @Override
    public Result createOperation(String requestURI, CreateOperationVO operationVO) {

        ActionInfoEntity actionInfoEntity = null;
//        ActionInfoEntity actionInfoEntity = actionInfoDao.findAllByCode(operationVO.getType());
        if (null == actionInfoEntity) {
            throw new ResultException(RespCode.ERROR_8_);
        }
        OperationLogEntity operationLogEntity = new OperationLogEntity();
        // 日志信息
        operationLogEntity.setActId(null);
//        operationLogEntity.setActId(actionInfoEntity.getId());
        operationLogEntity.setActCode(actionInfoEntity.getCode());
        operationLogEntity.setActDesc(actionInfoEntity.getDesc());
        operationLogEntity.setMsg(operationVO.getDescription());
        // 其他信息
        operationLogEntity.setTaskId(operationVO.getTaskId());
        operationLogEntity.setOrderId(operationVO.getOrderId());
        operationLogEntity.setUrl(requestURI);
        operationLogEntity.setCreateTime(DateUtils.getTimeStamp(operationVO.getTimestamp(), true));
        operationLogEntity.setProductCode(operationVO.getProductCode());
        operationLogEntity.setProductName(operationVO.getProductName());

//        operationLogDao.save(operationLogEntity);
        log.info("orderId:{}, taskId:{},日志类型：{} 创建操作日志成功..", operationVO.getOrderId(), operationVO.getTaskId(), operationVO.getType());
        return Result.success(null);
    }

    @Override
    public List<OperationLogEntity> getAllByTaskId(String taskId) {
//        return operationLogDao.findAllByTaskId(taskId);
        return null;

    }
}
