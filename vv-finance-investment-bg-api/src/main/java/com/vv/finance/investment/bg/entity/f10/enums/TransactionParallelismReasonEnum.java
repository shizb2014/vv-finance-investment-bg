package com.vv.finance.investment.bg.entity.f10.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wsliang
 * @date 2021/10/22 11:18
 **/
@AllArgsConstructor
@Getter
public enum TransactionParallelismReasonEnum {
    B("B", "董事会变化"),
    C("C", "股票整合"),
    S("S", "股权分值"),
    ;

    private String code;
    private String value;

    public static String getValue(String code) {
        for (TransactionParallelismReasonEnum value : TransactionParallelismReasonEnum.values()) {
            if (value.code.equals(code)) {
                return value.getValue();
            }
        }
        return code;
    }
}
