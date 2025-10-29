package com.vv.finance.investment.bg.entity.f10.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wsliang
 * @date 2021/9/27 17:03
 **/
@AllArgsConstructor
@Getter
public enum EstimationDimensionEnum {
    profit("profit", "盈利能力"),
    growth("growth", "成长能力"),
    operating("operating", "营运能力"),
    debt("debt", "偿债能力"),
    cash("cash", "现金能力"),
    ;

    private String field;
    private String value;

    public static String getValue(String field) {
        if (StringUtils.isBlank(field)) {
            return "";
        }
        for (EstimationDimensionEnum value : EstimationDimensionEnum.values()) {
            if (value.getField().equals(field)) {
                return value.getValue();
            }
        }
        return "";
    }
}
