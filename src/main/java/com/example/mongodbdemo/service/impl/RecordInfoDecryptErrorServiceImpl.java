package com.example.mongodbdemo.service.impl;

import com.example.mongodbdemo.data.bo.RecordInfoDecryptErrorBO;
import com.example.mongodbdemo.entity.RecordInfoDecryptErrorEntity;
import com.example.mongodbdemo.service.RecordInfoDecryptErrorService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/12 13:31
 **/
@Service
public class RecordInfoDecryptErrorServiceImpl implements RecordInfoDecryptErrorService {

//    @Autowired
//    private RecordInfoDecryptErrorMapper recordInfoDecryptErrorMapper;

    @Override
    public void save(RecordInfoDecryptErrorBO recordInfoDecryptErrorBO) {
        RecordInfoDecryptErrorEntity recordInfoDecryptErrorEntity = new RecordInfoDecryptErrorEntity();
        BeanUtils.copyProperties(recordInfoDecryptErrorBO, recordInfoDecryptErrorEntity);
        recordInfoDecryptErrorEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
//        recordInfoDecryptErrorMapper.insert(recordInfoDecryptErrorEntity);
    }
}
