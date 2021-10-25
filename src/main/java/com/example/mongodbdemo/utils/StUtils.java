package com.example.mongodbdemo.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.UUID;

/**
 * @Author : zhudakang@situdata.com
 * @Description : StUtils 包含UUID等一些业务需求的静态方法
 * @Date : 2019/8/8
 */
@Slf4j
public class StUtils {

    public static String getUUID() {
        return UUID.randomUUID().toString().trim().replaceAll("-", "");
    }

    /**
     * 16位UUID
     *
     * @return
     */
    public static String getUUID16() {
        String uuid = getUUID();
        int i = uuid.length() / 2;
        return uuid.substring(i);
    }

    /**
     * 获取16位UUID 作为文件名
     *
     * @return
     */
    public static String getFileNameByUUId() {
        String uuid = StUtils.getUUID();
        String subUUID = uuid.substring(uuid.length() / 2);
        return subUUID;
    }

    private static int getRandomIntInRange(int min, int max) {
        Random r = new Random();
        return r.ints(min, (max + 1)).limit(1).findFirst().getAsInt();
    }

    /**
     * 等待线程
     *
     * @param waittingMills
     * @return
     * @author Logan
     * @date 2020-09-24 10:22
     */
    public static void ThreadWatting(Long waittingMills) {
        try {
            Thread.sleep(waittingMills);
            log.warn("尝试下一次请求等待时间:{}秒", waittingMills / 1000);
        } catch (InterruptedException e) {
            log.error("尝试下一次请求等待时间:{}秒,失败", waittingMills / 1000, e);
        }
    }


    public static void main(String[] args) {
        System.out.println(getUUID());
        for (int i = 0; i < 20; i++) {
            String fileNameByUUId = getFileNameByUUId();
            System.out.println(fileNameByUUId);

        }
    }
}
