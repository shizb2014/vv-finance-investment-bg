package com.vv.finance.investment.bg.entity.f10.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 规模对比排序字段枚举
 *
 * @author lh.sz
 */
public enum ScaleSortEnum {

    /**
     * 总市值
     */
    TOTAL_VALUE(1, "totalValue"),

    TOTAL_ASSETS(2, "totalAssets"),

    TAKING(3, "taking"),

    GROSS_MARGIN(4, "grossMargin"),

    RETAINED_PROFITS(5, "retainedProfits");


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

    ScaleSortEnum(
            int code,
            String desc
    ) {
        this.code = code;
        this.desc = desc;
    }

    public static ScaleSortEnum getByCode(int code) {
        for (ScaleSortEnum value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }
}
