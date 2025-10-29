package com.vv.finance.investment.bg.entity.information.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wsliang
 * @date 2021/11/3 17:55
 **/
@Getter
@AllArgsConstructor
public enum TwentyFourHoursTypeEnum {
    ZERO(0, "全部"),
    ONE(1, "全球快讯"),
    TWO(2, "全球指数"),
    THREE(3, "香港股市"),
    FOUR(4, "美国股市"),
    FIVE(5, "外汇期货"),
    ;

    private Integer code;

    private String value;

    public static String getValue(Integer code) {
        for (TwentyFourHoursTypeEnum value : TwentyFourHoursTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value.getValue();
            }
        }
        return "";
    }
}
