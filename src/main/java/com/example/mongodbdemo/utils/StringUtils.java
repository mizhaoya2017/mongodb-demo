package com.example.mongodbdemo.utils;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/24 16:47
 **/
public class StringUtils {

    private static final Integer ONE = 1;

    /**
     * 针对url进行指定字符路径截取
     *
     * @param source
     * @param subStr
     * @return
     */
    public static String sub(String source, String subStr) {
        int index = source.indexOf(subStr);
        if (index != -1) {
            // index-1 代表： 路径带上 /
            return source.substring(index - 1);
        }
        return source;
    }

    /**
     * 判断字符串是否是数字结尾的
     *
     * @param source
     * @return source是数字结尾返回true， 否则返回false
     */
    public static boolean strLastIsNumber(String source) {

        String sourceEnd = source.substring(source.length() - 1);
        return RegExUtils.isInteger(sourceEnd);
    }


    /**
     * 将source字符串末尾数字字符+1
     *
     * @param source
     * @return
     */
    public static String strLastNumberAddAndGet(String source) {

        if (org.apache.commons.lang.StringUtils.isBlank(source)) {
            return org.apache.commons.lang.StringUtils.EMPTY;
        }
        int sourceLength = source.length();
        //获取source最后一位字符
        String lastNumber = source.substring(sourceLength - ONE);
        // 判断是否是数字
        if (!RegExUtils.isInteger(lastNumber)) {
            // 认为版本号就是数字

            return String.format("%s", Integer.parseInt(source) + ONE);
        }

        // 正则查找字符串的尾部数字
        Integer lastInteger = RegExUtils.getStrLastInteger(source);
        if (lastInteger == null) {
            return String.format("%s%s", source, ONE);
        }
        // 将尾部数字 +1
        int lastNumberAdd = lastInteger + 1;
        // 获取字符串尾部数字的开始索引
        int endNumberIndex = source.lastIndexOf(String.valueOf(lastInteger));
        // 获取除尾部数字外的字符串前缀
        String sourceStrPrefix = source.substring(0, endNumberIndex);
        // 将字符串前缀和 +1后的尾部数字拼接 并返回
        return String.format("%s%s", sourceStrPrefix, lastNumberAdd);
    }


//    public static String join(List<String> list, String separator) {
//        if (list == null || list.size() == 0) {
//            return org.apache.commons.lang.StringUtils.EMPTY;
//        }
//        // 移除集合中的空元素
//        list.removeAll(Collections.singleton(null));
//        return org.apache.commons.lang.StringUtils.join(list, separator);
//
//
//    }

    public static void main(String[] args) {
//        System.out.println(sub("23423page-resource-upload424234", "page-resource-upload"));

        System.out.println("qwe1".lastIndexOf("1"));
        System.out.println("qwe1".indexOf("1"));
    }

}
