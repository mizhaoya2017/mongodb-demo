package com.example.mongodbdemo.service;

import java.util.concurrent.*;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/12 15:25
 **/
public class BaseMultithreadService {

    protected ExecutorService threadPool = new ThreadPoolExecutor(8,
            100,
            30L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(8),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());

}
