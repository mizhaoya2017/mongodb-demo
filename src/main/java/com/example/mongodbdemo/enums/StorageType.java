package com.example.mongodbdemo.enums;

public enum StorageType {
    /**
    * 数据存储方式，QingStorOSS，AliOss,ZanhuaOss,MySql,H5Img
    */
    OSS_QINGSTOR_STORAGE("QingStorOss"),
    OSS_ALI_STORAGE("AliOss"),
    OSS_ZANHUA_STORAGE("ZanhuaOss"),
    MYSQL_STORAGE("MySql"),
    NAS_STORAGE("Nas"),
    H5IMG_STORAGE("H5Img");
    private String name;

    private StorageType(String name) {
        this.name = name;
    }

    public static StorageType getStorageType(String name) {
        for (StorageType temp : StorageType.values()) {
            if (temp.name.equals(name)) {
                return temp;
            }
        }
        return null;
    }
}
