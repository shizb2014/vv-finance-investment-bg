package com.vv.finance.investment.bg.entity.f10.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @ClassName F10MarketTypeEnum
 * @Deacription F10 市场类型枚举
 * @Author lh.sz
 * @Date 2021年07月23日 14:41
 **/
public enum F10TableTypeEnum {

    /**
     * 主要指标
     */
    RATINGS(1, "主要指标"),

    /**
     * 利润表
     */
    PROFIT(2, "利润表"),

    /**
     * 资产负债表
     */
    DEPT(3, "资产负债表"),
    /**
     * 现金流量表
     */
    CASH(4, "现金流量表");


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
