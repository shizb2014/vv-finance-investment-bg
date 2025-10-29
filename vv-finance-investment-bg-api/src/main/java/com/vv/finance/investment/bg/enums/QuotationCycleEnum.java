package com.vv.finance.investment.bg.enums;

import lombok.Getter;

/**
 * description: QuotationCycleEnum
 * date: 2022/8/10 15:07
 * author: fenghua.cai
 */
@Getter
public enum QuotationCycleEnum {

    MIN1("min1", "1分"),
    MIN5("min5", "5分"),
    MIN15("min15", "15分"),
    MIN30("min30", "30分"),
    MIN60("min60", "60分"),
    MIN120("min120", "120分"),
    DAILY("day", "日K"),
    WEEKLY("week", "周K"),
    MONTHLY("month", "月K"),
    QUARTERLY("quarter", "季K"),
    YEARLY("year", "年K"),
    ;

    private String name;
    private String desc;

    QuotationCycleEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static String getDescByName(String name){
        for (QuotationCycleEnum value : QuotationCycleEnum.values()) {
            if (value.getName().equals(name)) {
                return value.getDesc();
            }
        }
        return "未知";
    }
}
