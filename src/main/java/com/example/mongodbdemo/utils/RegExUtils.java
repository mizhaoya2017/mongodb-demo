package com.example.mongodbdemo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/21 17:47
 **/
public class RegExUtils {

    public final static String URL_MATCH = "(http|https)://(?!(\\.png|\\.jpg|\\.jpeg|\\.gif|\\.jfif|\\.jpe|\\.net|\\.rp|\\.tif|\\.tiff|\\.wbmp|\\.avg|\\.ttf|\\.woff|\\.css|\\.js)).+?(\\.jpg|\\.png|\\.jpeg|\\.gif|\\.jfif|\\.jpe|\\.net|\\.rp|\\.tif|\\.tiff|\\.wbmp|\\.avg|\\.ttf|\\.woff|\\.css|\\.js)+[\\w-_/?&=#%:]*";
    private final static String BLANK = "\\s*|\t|\r|\n";
    private final static String NUMBER = "[0-9]*";
    private final static String END_NUMBER = "(\\d+)$";



    /**
     * 匹配特殊字符并删除
     *
     * @return
     */
    public static String matchSpecialChar(String str) {

        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 匹配特殊字符并返回索引
     *
     * @param str
     * @return 返回第一个特殊字符的索引
     */
    public static int matchIndexSpecialChar(String str) {

        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        int index = -1;
        try {
            index = m.start();

        } catch (Exception exception) {
            return index;
        }
        return index;
    }

    // 去除所有空格和换行
    public static String replaceAllBlank(String str) {
        try {
            Pattern p = Pattern.compile(BLANK);
            Matcher m = p.matcher(str);
            return m.replaceAll("");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return "";
    }

    /**
     * 匹配字符串中的url
     *
     * @param resourceStr
     * @return
     */
    public static List<String> getUrlMatch(String resourceStr) {
        List<String> list = new ArrayList<>();
        Pattern pattern = Pattern.compile(RegExUtils.URL_MATCH);
        Matcher matcher = pattern.matcher(resourceStr);
        while (matcher.find()) {
            list.add(matcher.group(0));
        }
        return list;
    }

    //匹配所有url
    public static String matchUrl(String str) {
        try {
            Matcher matcher = Patterns.WEB_URL.matcher(str);
            if (matcher.find()) {
                return matcher.group();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return "";

    }

    /**
     * 判断是否为整数
     *
     * @param str 传入的字符串
     * @return 是整数返回true, 否则返回false
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile(NUMBER);
        return pattern.matcher(str).matches();
    }
    /**
     * 获取字符串的尾部数字
     *
     * @param source
     * @return
     */
    public static Integer getStrLastInteger(String source) {
        Pattern pattern = Pattern.compile(END_NUMBER);
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;

    }
    public static void main(String[] args) {

        String str = "234234234\"\n\"456546";
        String allBlank = replaceAllBlank(str);
        System.out.println(allBlank);
//        System.out.println(RegExUtils.matchSpecialChar("12313213-4564?6786~456"));
//        String str = "http://www.baidu.com:8080/idr-record-fe/js/chunk-vendors.js\"},\"childNodes\":[],\"id\":252},{\"type\":2,\"tagName\":\"script\",\"attributes\":{\"type\":\"text/javascript\",\"src\"";
//        System.out.println(matchUrl(str));

    }

}
