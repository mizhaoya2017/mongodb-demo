package com.example.mongodbdemo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @program: optrace
 * @description:
 * @author: Logan
 * @create: 2020-09-27 21:38
 **/
@Service
@Slf4j
public class RedisLockService {

    @Autowired
    private RedisTemplate redisTemplate;
    private String taskLockKey(String taskId){
        return String.format("TASK_LOCK_%s",taskId);
    }

    public boolean lockTask(String taskId,String requestId) {
        boolean result = false;
        result = redisTemplate.opsForValue().setIfAbsent(taskLockKey(taskId), requestId, 5,TimeUnit.SECONDS);
        log.info("lock taskId:{},requestId:{},lock success:{}",taskId,requestId,result);
        if(!result){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.error("分布式锁",e);
            }
        }
        return result;
    }

    public boolean releaseTask(String taskId,String requestId) {
        String key = taskLockKey(taskId);
        if (redisTemplate.hasKey(key)) {
            String lockValue = (String) redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotEmpty(lockValue) && lockValue.equals(requestId)) {
                redisTemplate.delete(key);
                return true;
            }
        }else{
            log.warn("taskId:{}的锁已经被释放或者过期",taskId);
        }
        return false;
    }


    private String orderLockKey(String orderId){
        return String.format("ORDER_LOCK_%s",orderId);
    }



    public boolean lockOrder(String orderId,String requestId){
        boolean result = false;
        result =  redisTemplate.opsForValue().setIfAbsent(orderLockKey(orderId), requestId, 5,TimeUnit.SECONDS);
        if(!result){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.error("分布式锁",e);
            }
        }
        return result;
    }

    public boolean releaseOrder(String orderId,String requestId) {
        String key = orderLockKey(orderId);
        if (redisTemplate.hasKey(key)) {
            String lockValue = (String) redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotEmpty(lockValue) && lockValue.equals(requestId)) {
                redisTemplate.delete(key);
                return true;
            }
        }else{
            log.warn("orderId:{}的锁已经被释放或者过期",orderId);
        }
        return false;
    }

    private String recordInfoLockKey(Integer recordInfoId){
        return String.format("RECORDINFO_LOCK_%d",recordInfoId);
    }
    public boolean lockRecordInfoResource(Integer recordInfoId,String requestId){
        String key = recordInfoLockKey(recordInfoId);
        boolean result = false;
        result =  redisTemplate.opsForValue().setIfAbsent(key, requestId, 2,TimeUnit.MINUTES);
        return result;
    }
    private static  String RECORD_INFO_IN_PROCESS_KEY = "RECORD_INFO_IN_PROCESS_KEY";
    public void setRecordInfoInProcess(String hostAddr,Integer recordInfoId){
        String key = recordInfoProcessKey(hostAddr);
        redisTemplate.opsForValue().set(key,recordInfoId.toString(),2,TimeUnit.MINUTES);
    }

    private String recordInfoProcessKey(String hostAddr){
        return String.format("%s_%s",RECORD_INFO_IN_PROCESS_KEY,hostAddr);
    }
    public boolean getRecordInfoInProcess(String hostAddr){
        String key = recordInfoProcessKey(hostAddr);
        Object recordInfoId = redisTemplate.opsForValue().get(key);
        if(recordInfoId != null) {
            log.info("ip:{}服务器正在处理 recordInfoId:{},等待下次触发抓取资源,key:{}", hostAddr,recordInfoId.toString(),key);
            return true;
        }
        return false;
    }

    public void  recordInfoProcessFinished(String hostAddr){
        String key = recordInfoProcessKey(hostAddr);
        redisTemplate.delete(key);
        log.info("已经删除key:{}",key);
    }


}
