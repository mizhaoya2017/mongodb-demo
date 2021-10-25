package com.example.mongodbdemo.utils;

import com.example.mongodbdemo.data.bo.UserInfoCacheBO;

import java.util.HashMap;

/**
 * @Author : dgguo <guodingguo@situdata.com>
 * @Description : SituThreadLocal
 * @Date : 2019/8/8
 */
public class SituThreadLocal {

    private static final ThreadLocal<HashMap> threadLocal = ThreadLocal.withInitial(() -> new HashMap());

    private static HashMap getMap() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }

    public static UserInfoCacheBO getUserInfo() {
        return null == getMap().get("userInfo") ? null : (UserInfoCacheBO) getMap().get("userInfo");
    }

    public static void setUserInfo(UserInfoCacheBO userInfoCacheBO) {
        getMap().put("userInfo", userInfoCacheBO);
    }

}
