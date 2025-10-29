package com.vv.finance.investment.bg.stock.information.enun;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wsliang
 * @date 2021/9/22 15:23
 **/
@AllArgsConstructor
@Getter
public enum CompanyTrendsType {
    ALARM(1, "交易警报"),
    PARALLEL(2, "并行交易"),
    TRADING(3, "停/复牌"),
    METTING(4, "股东大会"),
    REORGANIZE(5, "公司重组"),
    PURCHASE(6, "收购及合并"),
    SEVEN(7, "拆股合并"),
    EIGHT(8, "分红派息"),
    NINE(9, "股票回购"),

    TEN(10, "除权"),
    ELEVEN(11, "财报"),
    ;

    private Integer code;

    private String name;

    public static String getCompanyTrendsType(Integer code) {
        return getByCode(code).getName();
    }

    public static CompanyTrendsType getByCode(Integer code) {
        for (CompanyTrendsType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

}
