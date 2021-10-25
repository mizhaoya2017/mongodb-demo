package com.example.mongodbdemo.service;

import com.example.mongodbdemo.data.bo.RecordInfoMultiStorageBO;

import java.io.InputStream;
import java.util.List;

/**
 * @program: optrace
 * @description: RecordInfo的多种存储方式服务
 * @author: Logan
 * @create: 2020-09-15 16:08
 **/
public interface RecordInfoMultiStorageService {

    Integer saveRecordInfo(RecordInfoMultiStorageBO recordInfoMultiStorageBO);

    Integer IndexCheck(RecordInfoMultiStorageBO recordInfoMultiStorageBO);

    List<String> getRecordInfoList(String taskId);

    String saveRecordResource(InputStream inputStream, String filePath, String fileShortName);

    List<String> queryRecordInfoPageByTaskId(String taskId, int offset, int size);

}
