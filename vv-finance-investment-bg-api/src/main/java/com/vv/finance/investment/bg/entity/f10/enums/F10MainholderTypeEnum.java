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
public enum F10MainholderTypeEnum {
    A("A","A股"),
    B("B","B股"),
    H("H","H股"),
    O("O","普通股"),
    P("P","优先股"),
    U("U","单位份额"),
    T("T","其他"),
    ;

    private String code;
    private String value;

    public static String getValue(String code){
        if (StringUtils.isBlank(code)){
            return code;
        }

        for (F10MainholderTypeEnum value : F10MainholderTypeEnum.values()) {
            if (value.getCode().equals(code)){
                return value.getValue();
            }
        }
        return "--";
    }
}
