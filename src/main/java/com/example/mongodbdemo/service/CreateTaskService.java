package com.example.mongodbdemo.service;

import com.example.mongodbdemo.utils.StUtils;
import org.springframework.stereotype.Service;


/**
 * description:
 * Created by whq on 2020/7/13
 */
@Service
public class CreateTaskService {
    public String createTask() {
        return StUtils.getUUID() ;
    }
}
