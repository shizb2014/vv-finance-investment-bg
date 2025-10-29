package com.vv.finance.investment.bg.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;


/**
 * 股东类型枚举
 * @author yangpeng
 * @date 2023/10/17
 */
@Getter
@AllArgsConstructor
public enum StockHolderTypeEnum {

    AGENT("A", "Approved Lending Agent", "核准借出代理人"),
    CORPORATION("C", "Corporation", "法团"),
    DIRECTOR("D", "Board of Director", "董事"),
    INSTITUTION("F", "Financial Institutions", "金融机构"),
    PERSONAL("I", "Individual", "个人"),
    COMPANY("L", "HK Listed Company", "香港上市公司"),
    TRUSTEE("T", "Trustee", "受托人"),
    OTHER("O", "Other", "其他"),
    ;

    private final String code;
    private final String enDesc;
    private final String cnDesc;

    public static String getCnDescByCode(String code) {
        // 默认down升序
        return Stream.of(values()).filter(sua -> StrUtil.equals(sua.code, code)).findFirst().map(StockHolderTypeEnum::getCnDesc).orElse(null);
    }
}
