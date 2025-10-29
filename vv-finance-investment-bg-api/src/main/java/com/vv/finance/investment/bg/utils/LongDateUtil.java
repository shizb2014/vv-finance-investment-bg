package com.vv.finance.investment.bg.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @Description
 * @Author liuxing
 * @Create 2023/9/14 18:11
 */
public class LongDateUtil {

    /**
     * uts这边日期好多都是yyyyMMdd格式
     * @param date
     * @return
     */
    public static long getLongDate(LocalDate date){
        return Long.parseLong(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    /**
     * yyyyMMdd格式转LocalDate
     * @param longDate
     * @return
     */
    public static LocalDate getLocalDate(long longDate){
        String dateString = String.valueOf(longDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(dateString, formatter);
    }

    public static void main(String[] args) {
        System.out.println(getLocalDate(20100819));
    }
}
