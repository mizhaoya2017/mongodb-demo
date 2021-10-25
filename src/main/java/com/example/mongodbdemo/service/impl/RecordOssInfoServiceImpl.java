package com.example.mongodbdemo.service.impl;

import com.example.mongodbdemo.entity.RecordOssInfoEntity;
import com.example.mongodbdemo.service.RecordOssInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/18 0:49
 **/
@Service
public class RecordOssInfoServiceImpl implements RecordOssInfoService {

//    @Autowired
//    private RecordOssInfoDao recordOssInfoDao;
//    @Autowired
//    private RecordOssInfoMapper recordOssInfoMapper;

    @Override
    public RecordOssInfoEntity findAllById(Integer id) {
//        return recordOssInfoDao.findAllById(id);
        return null;


    }

    @Override
    public List<RecordOssInfoEntity> findAllByOrderIdAndTaskId(String orderId, String taskId) {
        return null;
//        return recordOssInfoDao.findAllByOrderIdAndTaskId(orderId, taskId);
    }

    @Override
    public List<Integer> findIdByTaskIdAndOrderId(List<String> taskIds) {

        return null;
//        return recordOssInfoMapper.findIdByTaskIdAndOrderId(taskIds);
    }

    /**
     * 根据主键更新下载状态
     *
     * @param id
     * @param downLoadStatus
     */
    @Override
    public void updateDownLoadStatusById(Integer id, Integer downLoadStatus) {

//        recordOssInfoMapper.updateDownLoadStatusById(id, downLoadStatus);
    }
}
