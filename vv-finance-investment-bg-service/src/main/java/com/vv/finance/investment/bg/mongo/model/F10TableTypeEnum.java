package com.vv.finance.investment.bg.mongo.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @ClassName TableTypeEnum
 * @Deacription 表格类型枚举
 * @Author lh.sz
 * @Date 2021年07月15日 17:11
 **/
public enum F10TableTypeEnum {
    /**
     * 主要指标(非金融)
     */
    INDEX(1, "主要指标"),

    PROFIT(2, "利润表"),

    ASSET(3, "资产负债表"),

    CASH(4, "现金流量表)");


    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    F10TableTypeEnum(
            int code,
            String desc
    ) {
        this.code = code;
        this.desc = desc;
    }

    public static F10TableTypeEnum getByCode(int code) {
        for (F10TableTypeEnum value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }

}
