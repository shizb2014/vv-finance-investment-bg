package com.vv.finance.investment.bg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author qinxi
 * @date 2023/6/26 16:42
 * @description: 南向资金日期枚举
 */
@AllArgsConstructor
@Getter
public enum SouthwardCapitalDateTypeEnum {

    TODAY(0, "今日"),

    NEARLY5DAYS(1, "近5日"),

    NEARLY20DAYS(2, "近20日"),

    NEARLY60DAYS(3, "近60日");


    private final Integer dateType;

    private final String desc;

    public static SouthwardCapitalDateTypeEnum getByDateType(Integer dateType) {
        if (dateType == null) {
            return null;
        }
        for (SouthwardCapitalDateTypeEnum value : SouthwardCapitalDateTypeEnum.values()) {
            if (value.getDateType() == dateType) {
                return value;
            }
        }
        return null;
    }

}
