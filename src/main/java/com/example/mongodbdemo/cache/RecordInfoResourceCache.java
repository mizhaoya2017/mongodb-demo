package com.example.mongodbdemo.cache;

import com.example.mongodbdemo.content.CommonContent;
import com.example.mongodbdemo.data.to.RecordInfoResourceCacheTO;
import com.example.mongodbdemo.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.example.mongodbdemo.content.CommonContent.CACHE_SELECT_RANGE_RATE;
import static com.example.mongodbdemo.content.CommonContent.CACHE_SELECT_RANGE_RATE_START;


/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/15 11:12
 **/
@Component
@Slf4j
public class RecordInfoResourceCache {


    @Autowired
    private RedisTemplate redisTemplate;

    private String simpleName = this.getClass().getSimpleName();

    private static Long maxListSize = 900000L;

    /**
     * 获取key
     *
     * @return
     */
    public String getKeyForList() {
        return String.format("%s_%s", simpleName, CommonContent.RECORD_INFO_SOURCE_PREFIX);
    }

    /**
     * 弹出一个任务，后进先出
     * @author Logan
     * @date 2020-11-19 18:26
     * @param

     * @return
     */
    public RecordInfoResourceCacheTO popRecordInfo(){
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        String key = getKeyForList();
        Object value = listOperations.leftPop(key);
        if(value != null){
            return JsonUtils.fromJson(value.toString(), RecordInfoResourceCacheTO.class);
        }
        return null;
    }

    /**
     * redis-list 每次从右列表中获取5个元素并删除
     *
     * @return
     */
    public List<String> getRangeForList() {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        String key = getKeyForList();
        List<String> range = listOperations.range(key, CACHE_SELECT_RANGE_RATE_START, (CACHE_SELECT_RANGE_RATE - 1));
        listOperations.trim(key, CACHE_SELECT_RANGE_RATE, -1);
        return range;
    }

    /**
     * 右列表添加
     *
     * @param recordInfoResourceCacheTO
     */
    public void rightPushForList(RecordInfoResourceCacheTO recordInfoResourceCacheTO) {
        String json = JsonUtils.toJson(recordInfoResourceCacheTO);
        String key = getKeyForList();
        if(redisTemplate.opsForList().size(key) > maxListSize){
            //控制大小
            log.warn("redis list 已经超过限制了");
            redisTemplate.opsForList().rightPop(key);
        }
        redisTemplate.opsForList().rightPush(key, json);
    }

    /**
     * 左列表弹出
     *
     * @return
     */
    public List<String> getLeftPopRangeForList() {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        String key = getKeyForList();
        List<String> resultElement = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String resultInfo = listOperations.leftPop(key);
            if (StringUtils.isNotBlank(resultInfo)) {
                resultElement.add(resultInfo);
            }
        }
        return resultElement;
    }


    /**
     * 元素向左列表移动
     *
     * @param recordInfoResourceCacheTO
     */
    public void leftPushByNotExistForList(RecordInfoResourceCacheTO recordInfoResourceCacheTO) {
        String json = JsonUtils.toJson(recordInfoResourceCacheTO);
        String key = getKeyForList();
        // 删除元素
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        listOperations.remove(key, 0, json);
        if(redisTemplate.opsForList().size(key) > maxListSize){
            //控制大小
            log.warn("redis list 已经超过限制了");
            redisTemplate.opsForList().rightPop(key);
        }
        // 列表左头部添加元素
        listOperations.leftPush(key, json);
    }

}
