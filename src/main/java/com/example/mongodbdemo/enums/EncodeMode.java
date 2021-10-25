package com.example.mongodbdemo.enums;

/**
* Task 编码类型
* @author Logan
* @date 2020-11-10 14:50

* @return
*/
public enum EncodeMode {
    //编码类型
    //AES加密
    ENCODE_AES(1),
    //明文
    ENCODE_PLAINTEXT(2),
    //压缩
    ENCODE_COMPRESS(3);

    private int value;
    private EncodeMode(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public static EncodeMode getEncodeMode(Integer value){
        if(value == null){
            return ENCODE_AES;
        }
        for(EncodeMode temp: EncodeMode.values()){
            if(temp.value == value){
                return temp;
            }
        }
        return null;
    }
}
