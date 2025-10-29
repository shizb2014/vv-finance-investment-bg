package com.vv.finance.investment.bg.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

/**
 * @ClassName: DateUtils
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/11/2   18:37
 */
@Slf4j
public class DateUtils {

    private DateUtils(){}
    /**
     * Date转LocalDateTime
     * @param date
     * @return
     */
    public static LocalDateTime dateToLocalDateTime(Date date){
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDateTime转Date
     * @param dateTime
     * @return
     */
    public static Date localDateTimeToDate(LocalDateTime dateTime){
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());

    }

    /**
     * 时间格式化
     * @param date yyyyMMdd
     * @return yyyy/MM/dd
     */
    public static String dateFormat(Long date) {
        try {
            String dateStr = date.toString();
            return dateStr.substring(0, 4) + "/" + dateStr.substring(4, 6) + "/" + dateStr.substring(6);
        }catch (Exception e){
            return "";
        }

    }

    public static Long localDateToLong(LocalDate date){
        return date.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();

    }

    public static Long longDateToLongMS(Long date){

        return LocalDate.parse(date.toString(), DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();

    }

    /**
     * yyyy-MM-dd转yyyyMMdd
     * @param date
     * @return
     */
    public static Long localDateToF001D(LocalDate date){
        return  Long.parseLong(date.toString().replace("-", ""));
    }

    /**
     * k线时间格式化
     * @param date yyyyMMdd
     * @return yyyy-MM-dd 00:00:00
     */
    public static String klineDateFormat(Long date) {
        try {
            String dateStr = date.toString();
            return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6) + " 00:00:00";
        }catch (Exception e){
            return "";
        }
    }

    public static LocalDate f001dToLocalDate(Long f001d){
        String dateStr = f001d.toString();
        return LocalDate.parse(dateStr, BASIC_ISO_DATE);
    }

    /**
     * 从现在到23点59分59秒还有多少秒
     *
     * @return
     */
    public static long nowToDayFinish() {
        long nowTime = dateStr2Timestamp(getCurrentMinuteTime());

        String endTimeStr = getNowDay() + " 23:59:59";
        long endTime = dateStr2Timestamp(endTimeStr);

        long time = endTime - nowTime;

        return time / 1000;
    }

    /**
     * 字符串日期转时间戳
     *
     * @param dateTimeStr 字符串日期
     * @return
     */
    public static Long dateStr2Timestamp(String dateTimeStr) {
        SimpleDateFormat dateFormatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long timestamp = null;
        try {
            Date date = dateFormatDateTime.parse(dateTimeStr);
            timestamp = date.getTime();
        } catch (ParseException e) {
            log.error("日期转换异常：{}", e);
        }
        return timestamp;
    }

    /**
     * 获取当前分钟时间
     * @return
     */
    public static String getCurrentMinuteTime() {
        SimpleDateFormat dateFormatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date=new Date();
        String dateTime = dateFormatDateTime.format(date);
        char[] dateTimeArr = dateTime.toCharArray();
        dateTimeArr[dateTimeArr.length-1] = '0';
        dateTimeArr[dateTimeArr.length-2] = '0';
        return new String(dateTimeArr);
    }

    /**
     * 获取今日日期  yyyy-MM-dd
     * @return
     */
    public static String getNowDay () {

        Date date=new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

}
