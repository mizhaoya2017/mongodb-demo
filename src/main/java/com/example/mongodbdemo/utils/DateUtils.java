package com.example.mongodbdemo.utils;

import com.example.mongodbdemo.data.vo.RespCode;
import com.example.mongodbdemo.excepition.ResultException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 时间相关工具类
 *
 * @author WANG Minghao
 */
@Slf4j
public class DateUtils {

    private static ThreadLocal<SimpleDateFormat> simpleDateFomratThreadLocal
            = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    private static ThreadLocal<SimpleDateFormat> simpleDateFomratMillsThreadLocal
            = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
    private static Date weekStartTimeInDate;

    public static String convertFormat(SimpleDateFormat sdf, Date date) {
        return sdf.format(date);
    }

    public static String convertFormat(String str) {
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sf2.format(sf1.parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date dateParse(SimpleDateFormat sdf, String dateTime) {
        try {
            return sdf.parse(dateTime);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date getHourStartTime(Date date, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setHour(calendar, i);
        return calendar.getTime();
    }

    private static void setHour(Calendar calendar, int i) {
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - i);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
    }

    public static Date getDayBeforeStartTime(int i) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setDay(calendar, -i);
        return calendar.getTime();
    }

    public static Date adjustTime(int i) {
        Calendar c = Calendar.getInstance();
        Date date = new Date();
        c.setTime(date);
        c.add(Calendar.SECOND, i);//把日期往后增加SECOND 秒.整数往后推,负数往前移动
        date = c.getTime();
        return date;
    }

    public static Date getDayBeforeStartTime(Date date, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setDay(calendar, i);
        return calendar.getTime();
    }

    private static void setDay(Calendar calendar, int i) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, i);
    }

    public static Date getMonthBeforeStartTime(Date date, int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setMonth(calendar, i);
        return calendar.getTime();
    }

    private static void setMonth(Calendar calendar, int i) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, 0);
        calendar.add(Calendar.MONTH, i);
    }

    public static Date getStartTimeInDateOf(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setDayStart(calendar);
        return calendar.getTime();
    }

    public static Date getEndTimeInDateOf(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        setDayEnd(calendar);
        return calendar.getTime();
    }

    public static Date getTodayStartTimeInDate() {
        Calendar calendar = new GregorianCalendar();
        setDayStart(calendar);
        return calendar.getTime();
    }

    public static Date getTodayEndTimeInDate() {
        Calendar calendar = new GregorianCalendar();
        setDayEnd(calendar);
        return calendar.getTime();
    }

    public static long getTodayStartTimeInLong() {
        Calendar calendar = new GregorianCalendar();
        setDayStart(calendar);
        return calendar.getTimeInMillis();
    }

    public static Date getStartTimeInDateOfNextDay(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, 1);
        setDayStart(calendar);
        return calendar.getTime();
    }

    public static Date getMidTimeInDateOf(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        setDayMid(calendar);
        return calendar.getTime();
    }

    public static Date getOneDayBeforeStartTimeInDate(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, -1);
        setDayStart(calendar);
        return calendar.getTime();
    }

    public static Date getYesterdayStartTimeInDate() {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, -1);
        setDayStart(calendar);
        return calendar.getTime();
    }

    public static long getYesterdayStartTimeInLong() {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE, -1);
        setDayStart(calendar);
        return calendar.getTimeInMillis();
    }

    public static Date getMonthStartTimeInDate() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        setDayStart(calendar);
        return calendar.getTime();
    }

    public static Date getMonthStartTimeInString(String dataString) {
        if (dataString.length() != 6) {
            return null;
        }
        Calendar calendar = new GregorianCalendar();
        Integer year = Integer.parseInt(dataString.substring(0, 4));
        Integer month = Integer.parseInt(dataString.substring(4)) - 1;
        if (year < 2000 || month > 11 || month < 0) {
            return null;
        }
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        setDayStart(calendar);
        return calendar.getTime();
    }

    public static Date getOneYearBeforeTimeInDate() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, -1);
        setDayStart(calendar);
        return calendar.getTime();
    }

    public static long getMonthStartTimeInLong() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        setDayStart(calendar);
        return calendar.getTimeInMillis();
    }

    private static void setDayStart(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private static void setDayEnd(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    private static void setDayMid(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
    }

    public static Date getWeekStartTimeInDate() {
        Calendar calendar = new GregorianCalendar();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        setDayStart(calendar);
        return calendar.getTime();
    }

    public static Date getLastWeekStartTimeInDate() {
        Calendar calendar = new GregorianCalendar();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        setDayStart(calendar);
        return calendar.getTime();
    }


    public static String dateConvertStr() {
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd");
        return sf2.format(new Date());
    }

    public static String longToDate(long lo) {
        Date date = new Date(lo);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sd.format(date);
    }

    public static String longToDay(long lo) {
        Date date = new Date(lo);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
        return sd.format(date);
    }

    public static Date getDayStart() {
        Date cur = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        //一天的开始时间 yyyy:MM:dd 00:00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getDayEnd() {
        Date cur = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * @param timeStamp
     * @param useSystemDefault 如果转换异常了，使用系统时间？
     * @return
     * @author Logan
     * @date 2020-09-23 16:03
     */
    public static Timestamp getTimeStamp(String timeStamp, boolean useSystemDefault) {
        //先尝试整型
        Timestamp result = null;
        result = getLongTypeTimeStamp(timeStamp);
        if (result == null) {
            result = getFormatedTypeTimeStamp(timeStamp);
        }
        if (result == null) {
            result = getFormated2TypeTimeStamp(timeStamp);
        }
        if (result != null) {
            return result;
        }
        if (useSystemDefault) {
            log.warn("整型时间戳和带T的时间戳转换失败，将使用系统时间");
            return new Timestamp(System.currentTimeMillis());
        } else {
            log.error("时间格式转换失败，原始时间格式:{}", timeStamp);
            throw new ResultException(RespCode.ERROR_14_);
        }


    }


    /**
     * 根据长整型转时间戳
     *
     * @param longTypeTimeStamp 时间戳是长整数
     * @return
     * @author Logan
     * @date 2020-09-23 15:49
     */
    private static Timestamp getLongTypeTimeStamp(String longTypeTimeStamp) {
        if (StringUtils.isNotEmpty(longTypeTimeStamp)) {
            try {
                return new Timestamp(Long.parseLong(longTypeTimeStamp));
            } catch (Exception e) {
                log.error("尝试整型时间戳转换失败，传入的是:{}", longTypeTimeStamp);
                return null;
            } finally {
                simpleDateFomratMillsThreadLocal.remove();
            }
        } else {
            log.warn("前端没有传timestamp");
            return null;
        }
    }

    /**
     * 根据js格式化之后的时间戳转时间戳
     *
     * @param formatedTypeTimeStamp 格式化的时间如: 2020-09-08T18:27:11.111Z
     * @return
     * @author Logan
     * @date 2020-09-23 15:51
     */
    private static Timestamp getFormatedTypeTimeStamp(String formatedTypeTimeStamp) {
        String handledTimeformated = formatedTypeTimeStamp.replace("T", " ").replace("Z", "");
        try {
            Long timeStamp = simpleDateFomratMillsThreadLocal.get().parse(handledTimeformated).getTime();
            return new Timestamp(timeStamp);
        } catch (Exception e) {
            log.error("尝试格式化时间转换失败，举例:2020-09-08T18:27:11.111Z，现在是:{},后台容错采取当前时间点", formatedTypeTimeStamp);
            return null;
        } finally {
            simpleDateFomratMillsThreadLocal.remove();
        }
    }

    /**
     * 转换时间戳 格式2 2020-09-25 20:49:10.038
     *
     * @param formatedTypeTimeStamp
     * @return
     * @author Logan
     * @date 2020-09-30 14:51
     */
    private static Timestamp getFormated2TypeTimeStamp(String formatedTypeTimeStamp) {
        try {
            Long timeStamp = simpleDateFomratMillsThreadLocal.get().parse(formatedTypeTimeStamp).getTime();
            return new Timestamp(timeStamp);
        } catch (Exception e) {
            log.error("尝试格式化时间转换失败，举例:2020-09-08T18:27:11.111Z，现在是:{},后台容错采取当前时间点", formatedTypeTimeStamp);
            return null;
        } finally {
            simpleDateFomratMillsThreadLocal.remove();
        }
    }

    public static Date timestampToDate(Timestamp timestamp) {

        return new Date(timestamp.getTime());

    }

    /**
     * 设置毫秒为0
     *
     * @param date
     * @return
     */
    public static Date setMillisecondToZero(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        // 设置毫秒为0
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTime();
    }

    /**
     * 检查时间戳是否是6个月前的时间
     *
     * @return true : 六个月前的时间， false： 六个月后的时间
     */
    public static boolean compare6MonthAgo(Timestamp inputDateTime) {
        LocalDateTime inputLocalDateTime = inputDateTime.toLocalDateTime();
        LocalDateTime currentLocalDateTime = LocalDateTime.now();
        LocalDateTime sixMonthsAgo = currentLocalDateTime.plusMonths(-6);
        // 指定时间是否超过了六个月前的时间
        if (inputLocalDateTime.isBefore(sixMonthsAgo)) {
            // 已超过六个月前的时间
            return true;
        }
        return false;
    }

    /**
     * 检查时间戳是否是3分钟前的时间
     *
     * @param inputDateTime
     * @return
     */
    public static boolean compare3MinuteAgo(Timestamp inputDateTime) {
        LocalDateTime inputLocalDateTime = inputDateTime.toLocalDateTime();
        LocalDateTime currentLocalDateTime = LocalDateTime.now();
        LocalDateTime threeMinutesAgo = currentLocalDateTime.plusMinutes(-3);
        // 指定时间是否超过了三分钟前的时间
        if (inputLocalDateTime.isBefore(threeMinutesAgo)) {
            // 已超过三分钟前的时间
            return true;
        }
        return false;
    }

    /**
     * 获取三个月前的时间（参数时间向前推三个月）
     *
     * @param time 传入时间
     * @return
     */
    public static Timestamp get3MonthsAgoTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 3);
        return new Timestamp(calendar.getTime().getTime());
    }

    /**
     * 获取5分钟前的时间（参数时间向前推5分钟）
     *
     * @param time 传入时间
     * @return
     */
    public static Timestamp get5MinuteAgoTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 5);
        return new Timestamp(calendar.getTime().getTime());
    }

    /**
     * 根据时间戳设置为 前一天的零点
     *
     * @param currentTimeMillis
     * @return
     */
    public static Timestamp theDayBeforeZeroPoint(long currentTimeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(currentTimeMillis));

        // 日期 -1天
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
        // 小时
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        // 分钟
        calendar.set(Calendar.MINUTE, 0);
        // 秒
        calendar.set(Calendar.SECOND, 0);
        // 毫秒
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTime().getTime());
    }

    /**
     * 根据时间戳设置为前一天的最后一个时间点
     *
     * @param currentTimeMillis
     * @return
     */
    public static Timestamp theDayBeforeEndPoint(long currentTimeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(currentTimeMillis));

        // 日期 -1天
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
        // 小时
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        // 分钟
        calendar.set(Calendar.MINUTE, 59);
        // 秒
        calendar.set(Calendar.SECOND, 59);
        // 毫秒
        calendar.set(Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTime().getTime());
    }

}