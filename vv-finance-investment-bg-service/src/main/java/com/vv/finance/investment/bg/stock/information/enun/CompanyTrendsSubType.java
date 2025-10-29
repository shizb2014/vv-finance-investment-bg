package com.vv.finance.investment.bg.stock.information.enun;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wsliang
 * @date 2021/9/22 15:23
 **/
@AllArgsConstructor
@Getter
public enum CompanyTrendsSubType {


    SUSPENSION(1, "停牌-XNHKS0317"),

    RESUMPTION(2, "复牌-XNHK0318"),

    FINANCIAL_REPORT_1(3, "财报"),
    FINANCIAL_REPORT_2(4, "财报"),
    FINANCIAL_REPORT_3(5, "财报"),


    ;

    private Integer code;

    private String name;

    public static String getCompanyTrendsType(Integer code) {
        return getByCode(code).getName();
    }

    public static CompanyTrendsSubType getByCode(Integer code) {
        for (CompanyTrendsSubType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

}
