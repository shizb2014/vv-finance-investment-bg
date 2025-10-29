package com.vv.finance.investment.bg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author:maling
 * @Date:2023/8/28
 * @Description: 南向资金趋势图类型枚举
 */

@AllArgsConstructor
@Getter
public enum SouthwardCapitalTrendTypeEnum {

    NET_BUYING_TURNOVER(0, "净买入"),

    NET_TURNOVER_IN(1, "净流入"),

    SURPLUS_QUOTA(2, "资金余额");


    private final Integer trendType;

    private final String desc;

    public static SouthwardCapitalTrendTypeEnum getByTrendType(Integer trendType) {
        if (trendType == null) {
            return null;
        }
        for (SouthwardCapitalTrendTypeEnum value : SouthwardCapitalTrendTypeEnum.values()) {
            if (value.getTrendType() == trendType) {
                return value;
            }
        }
        return null;
    }

}
