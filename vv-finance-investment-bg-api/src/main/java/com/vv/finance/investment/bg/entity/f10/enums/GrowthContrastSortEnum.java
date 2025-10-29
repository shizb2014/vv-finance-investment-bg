package com.vv.finance.investment.bg.entity.f10.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 成长性对比排序字段枚举
 *
 * @author lh.sz
 */
public enum GrowthContrastSortEnum {

    /**
     * 每股盈利增长率
     */
    EARNINGS_PER_SHARE(1, "earningsPerShareGrowthRate"),

    /**
     * 营业收入增长率
     */
    MBRG(2, "mbrg"),

    /**
     * 营业利润增长率
     */
    OPERATING_PROFIT(3, "operatingProfitGrowthRate"),
    /**
     * 总资产增长率
     */
    TOTAL_ASSETS(4, "growthRateTotalAssets");


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

    GrowthContrastSortEnum(
            int code,
            String desc
    ) {
        this.code = code;
        this.desc = desc;
    }

    public static GrowthContrastSortEnum getByCode(int code) {
        for (GrowthContrastSortEnum value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }
}
