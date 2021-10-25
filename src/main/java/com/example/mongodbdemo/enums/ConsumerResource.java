package com.example.mongodbdemo.enums;

/**
 * 客服资源枚举类
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/10/16 11:59
 **/
public class ConsumerResource {

    /**
     * 客服资源文件类型
     */
    public enum ContextTypeEnum {

        TEXT(1, "文本"),
        IMAGE(2, "图片"),
        LINK(3, "链接"),
        RICHTEXT(4, "富文本");
        private Integer code;
        private String contextType;

        ContextTypeEnum(Integer code, String contextType) {
            this.code = code;
            this.contextType = contextType;
        }

        public Integer getCode() {
            return code;
        }

        public String getContextType() {
            return contextType;
        }

        public static ContextTypeEnum valueOfByCode(Integer code) {
            ContextTypeEnum[] values = ContextTypeEnum.values();
            for (ContextTypeEnum value : values) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 客户角色
     */
    public enum RoleTypeEnum {
        CONSUMER_USER(1, "用户"),
        CONSUMER_SERVICE(2, "客服");

        private Integer code;
        private String role;

        RoleTypeEnum(Integer code, String role) {
            this.code = code;
            this.role = role;
        }

        public Integer getCode() {
            return code;
        }

        public String getRole() {
            return role;
        }


        public static RoleTypeEnum valueOfByCode(Integer code) {
            RoleTypeEnum[] values = RoleTypeEnum.values();
            for (RoleTypeEnum value : values) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }
}
