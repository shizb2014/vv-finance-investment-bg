package com.vv.finance.investment.bg.entity.f10.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wsliang
 * @date 2021/9/27 13:55
 **/
@AllArgsConstructor
@Getter
public enum F10CapitalStatisticsType {
    O("O", "普通股"),
    H("H", "H股"),
    P("B", "B股"),
    ;

    private String code;
    private String value;

    public static String getValue(String code) {
        if (StringUtils.isBlank(code)) {
            return code;
        }

        for (F10CapitalStatisticsType value : F10CapitalStatisticsType.values()) {
            if (value.getCode().equals(code)) {
                return value.getValue();
            }
        }
        return code;
    }
}
