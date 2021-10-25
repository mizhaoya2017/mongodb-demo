package com.example.mongodbdemo.enums;

/**
 * 版本管理类型
 */
public enum StorageVersionType {
    /**
     * 数据存储方式，QingStorOSS，AliOss,ZanhuaOss,MySql,H5Img
     */
    PRODUCT_GRANULARITY_VERSION_TYPE("productGranularityVersionType"),
    BASE_VERSION_TYPE("baseVersionType");
    private String name;

    private StorageVersionType(String name) {
        this.name = name;
    }

    public static StorageVersionType getStorageVersionType(String name) {
        for (StorageVersionType temp : StorageVersionType.values()) {
            if (temp.name.equals(name)) {
                return temp;
            }
        }
        return null;
    }
}
