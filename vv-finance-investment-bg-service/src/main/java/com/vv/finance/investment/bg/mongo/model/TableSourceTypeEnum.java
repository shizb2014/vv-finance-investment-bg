package com.vv.finance.investment.bg.mongo.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @ClassName TableTypeEnum
 * @Deacription 表格类型枚举
 * @Author lh.sz
 * @Date 2021年07月15日 17:11
 **/
public enum TableSourceTypeEnum {
    /**
     * 主要指标(非金融)
     */
    INDEX_NO_FINANCE(1, "主要指标(非金融)"),

    INDEX_FINANCE(2, "主要指标(金融)"),

    INDEX_INSURANCE(3, "主要指标(保险)"),

    PROFIT_NO_FINANCE(4, "利润表(非金融)"),

    PROFIT_FINANCE(5, "利润表(金融)"),

    PROFIT_INSURANCE(6, "利润表(保险)"),

    ASSET_NO_FINANCE(7, "资产负债表（非金融)"),

    ASSET_FINANCE(8, "资产负债表（金融）"),

    ASSET_INSURANCE(9, "资产负债表（保险）"),

    CASH_FLOW(10, "现金流量表");


    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    TableSourceTypeEnum(
            int code,
            String desc
    ) {
        this.code = code;
        this.desc = desc;
    }

    public static TableSourceTypeEnum getByCode(int code) {
        for (TableSourceTypeEnum value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }

}
