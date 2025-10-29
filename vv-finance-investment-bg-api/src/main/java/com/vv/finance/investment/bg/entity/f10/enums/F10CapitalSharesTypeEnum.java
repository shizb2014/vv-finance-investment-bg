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
public enum F10CapitalSharesTypeEnum {
    S("S","普通股"),
    O("O","其他已发行股份"),
    H("H","H股"),
    P("P","优先股"),
    T("T","除普通股、H股、优先股及其他已发行股份外的股票"),
    ;

    private String code;
    private String value;

    public static String getValue(String code){
        if (StringUtils.isBlank(code)){
            return code;
        }

        for (F10CapitalSharesTypeEnum value : F10CapitalSharesTypeEnum.values()) {
            if (value.getCode().equals(code)){
                return value.getValue();
            }
        }
        return code;
    }
}
