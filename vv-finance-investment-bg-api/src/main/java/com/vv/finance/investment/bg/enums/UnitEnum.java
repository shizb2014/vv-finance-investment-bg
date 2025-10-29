package com.vv.finance.investment.bg.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * description: UnitEnum
 * date: 2022/8/10 15:14
 * author: fenghua.cai
 */
@Getter
public enum UnitEnum {

    DAY("day", "日"),
    MONTH("month", "月"),
    YEAR("year", "年"),
    ;

    @JsonValue
    private String name;
    private String desc;

    UnitEnum(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public static String getDescByName(String name){
        for (UnitEnum value : UnitEnum.values()) {
            if (value.getName().equals(name)) {
                return value.getDesc();
            }
        }
        return "未知";
    }
}
