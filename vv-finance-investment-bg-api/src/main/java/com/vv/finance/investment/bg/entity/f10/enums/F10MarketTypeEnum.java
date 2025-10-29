package com.vv.finance.investment.bg.entity.f10.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @ClassName F10MarketTypeEnum
 * @Deacription F10 市场类型枚举
 * @Author lh.sz
 * @Date 2021年07月23日 14:41
 **/
public enum F10MarketTypeEnum {

    /**
     * 非金融
     */
    NO_FINANCIAL(0, "非金融"),

    /**
     * 金融
     */
    FINANCIAL(1, "金融"),

    /**
     * 保险
     */
    INSURANCE(2, "保险");


    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    F10MarketTypeEnum(
            int code,
            String desc
    ) {
        this.code = code;
        this.desc = desc;
    }

    public static F10MarketTypeEnum getByCode(int code) {
        for (F10MarketTypeEnum value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }


}
