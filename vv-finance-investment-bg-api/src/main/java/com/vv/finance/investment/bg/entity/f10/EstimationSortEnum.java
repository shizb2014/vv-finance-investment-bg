package com.vv.finance.investment.bg.entity.f10;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 估值对比排序字段枚举
 *
 * @author lh.sz
 */
public enum EstimationSortEnum {

    /**
     * 市盈率TTM
     */
    PE_TTM(1, "peTtm"),
    PE_LYR(2, "peLyr"),
    DIVIDEND_YIELD_TTM(3, "dividendYieldTtm"),
    DIVIDEND_YIELD_LYR(4, "dividendYieldLyr"),
    PB(5, "pb"),
    PRG(6, "prg"),
    ROE(7, "roe"),
    ROA(8, "roa");


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

    EstimationSortEnum(
            int code,
            String desc
    ) {
        this.code = code;
        this.desc = desc;
    }

    public static List<String> getNewSortList() {
        return Stream.of(PE_TTM, PE_LYR, PB).map(EstimationSortEnum::getDesc).collect(Collectors.toList());
    }

    public static EstimationSortEnum getByCode(int code) {
        for (EstimationSortEnum value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }
}
