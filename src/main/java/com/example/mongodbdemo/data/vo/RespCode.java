package com.example.mongodbdemo.data.vo;

/**
 * @Description : RespCode
 * @Date : 2019/8/8
 * @Author : zhudakang@situdata.com
 */
public enum RespCode {
    /**
     *
     */

    SUCC(10000, "OK"),


    ERROR_1(1, "请检查license"),

    ERROR_2(2, "非法ip"),

    UNKNOWN(-1, "未知异常"),

    ERROR_2_(-2, "缺少某个域"),

    ERROR_3_(-3, "不支持的请求类型"),

    ERROR_4_(-4, "参数错误"),

    ERROR_5_(-5, "数据源异常"),

    ERROR_6_(-6, "系统内部错误，请稍后重试"),

    ERROR_7_(-7, "订单状态异常"),
    ERROR_AUTH_FAIL(90000, "请求权限校验失败"),
    ERROR_DECRIPT(90001, "解密失败"),
    ERROR_STAMP_MISSING(90002, "丢失Stamp"),
    ERROR_8_(-8, "日志事件CODE错误"),

    ERROR_9_(-9, "授权时间已过期"),
    ERROR_10_(-10, "服务器不匹配"),

    ERROR_11_(-11, "未传递信息主键id"),
    ERROR_12_(-12, "未传递taskId"),
    ERROR_13_(-13, "未传递操作信息"),
    ERROR_14_(-14, "时间格式转换失败"),
    ERROR_15_(-15, "图片文件名不能重复"),
    ERROR_16_(-16, "新创建的版本小于或等于当前最新版本时间"),
    ERROR_17_(-17, "版本号不是数字结尾"),
    ERROR_18_(-18, "版本号已存在"),
    ERROR_19_(-19, "产品已存在"),
    ERROR_20_(-20, "产品列表不能为空"),
    ERROR_21_(-21, "暂不支持该格式"),
    ERROR_22_(-22, "文件不能为空"),
    ERROR_23_(-23, "产品信息不存在"),

    ERROR_IMAGE(200, "请上传图片"),
    ERROR_IMAGE_EMPTY(200, "上传图片为空"),
    ERROR_APPLET_PRODUCT_EMPTY(200, "小程序版本管理中不存在该订单的产品编码"),
    ERROR_APPLET_VERSION_EMPTY(200, "小程序版本管理中当前产品编码不存在版本信息"),
    ERROR_APPLET_VERSION_IMAGE_EMPTY(200, "小程序版本管理中没有上传图片"),
    ERROR_APPLET_PRODUCT_CODE_REPEAT(200, "产品编码重复"),

    /**
     * OSS异常
     */
    ERROR_3000_(3000, "OSS对象存储异常"),
    ERROR_3001_(3001, "OSS获取对象异常"),
    ERROR_3002_(3002, "OSS删除对象异常"),
    ERROR_3003_(3003, "OSS获取访问连接异常"),
    ERROR_3004_(3004, "OSS IOException"),

    /**
     * 版本材料异常
     */
    ERROR_MATERIAL_NULL(-5, "版本文件信息为空"),


    ERROR_INTERFACE(-1, "接口调用异常"),


    ERROR_LOGIN_EXCEPTION(200, "登录异常"),
    ERROR_LOGIN_EXISTS(200, "用户已存在"),
    ERROR_LOGIN_NULL(200, "用户名或密码错误"),
    ERROR_LOGIN_AUTHLISTNULL(200, "未查询到用户在该页面的权限"),
    ERROR_LOGIN_FOBIDDEN(200, "用户已被禁用"),
    ERROR_LOGIN_WRONGPASSWORD(200, "用户名或密码错误"),
    ERROR_LOGIN_NOROLE(200, "未查询到用户角色，请联系管理员"),
    ERROR_LOGIN_USERNAME_DISABLE(200, "账号已禁用,请联系管理员解锁"),
    ERROR_LOGOUT_TOKEN(300, "登出Token为空"),
    ERROR_MODIFY_PWD_OLD(300, "旧密码不正确"),
    ERROR_USER_INFO_EMPTY(300, "用户不存在"),
    ERROR_MODIFY_PWD_NULL(300, "密码不能为空"),
    ERROR_ROLE_SAVE_NAME_EMPTY(300, "角色名称不能为空"),
    ERROR_ROLE_SAVE_NAME_REPEAT(300, "角色名称已存在"),
    ERROR_ROLEID_MENU_AUTH_REPEAT(300, "角色id不能为空"),

    ERROR_LOGIN_AUTH_DENIED(5001, "登录授权失败"),
    ERROR_LOGIN_TIMEOUT(5002, "登录超时"),

    ERROR_TIME(300, "请传递起止时间");


    private int code;

    private String msg;


    RespCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static RespCode get(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

    public static RespCode getByCode(int code) {
        for (RespCode temp : RespCode.values()) {
            if (temp.getCode() == code) {
                return temp;
            }
        }
        return null;
    }
}
