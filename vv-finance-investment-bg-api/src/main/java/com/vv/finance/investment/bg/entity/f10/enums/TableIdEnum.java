package com.vv.finance.investment.bg.entity.f10.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @ClassName TableIdEnum
 * @Deacription
 * @Author lh.sz
 * @Date 2021年07月26日 17:00
 **/
public enum TableIdEnum {
    ALL(1, "全部"),

    F(2, "F"),

    I(3, "I"),

    Q(4, "Q");


    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    TableIdEnum(
            int code,
            String desc
    ) {
        this.code = code;
        this.desc = desc;
    }

    public static TableIdEnum getByCode(int code) {
        for (TableIdEnum value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }
}
