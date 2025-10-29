package com.vv.finance.investment.bg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author qinxi
 * @date 2023/8/22 16:08
 * @description:
 */
@AllArgsConstructor
@Getter
public enum SouthwardCapitalMarketEnum {

    SZ("SZ", "深市"),

    SH("SH", "沪市"),

    ALL("ALL", "所有");

    private final String market;

    private final String desc;

}
