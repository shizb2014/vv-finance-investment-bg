package com.vv.finance.investment.bg.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;


/**
 * 股份类型枚举
 *
 * @author yangpeng
 * @date 2023/10/17
 */
@Getter
@AllArgsConstructor
public enum StockShareTypeEnum {

    OS("OS", "Ordinary share", "普通股"),
    HS("HS", "H share", "H股"),
    DS("DS", "HDR share", "HDR股"),
    CA("CA", "Class A Share", "A类股份"),
    CB("CB", "Class B Share", "B类股份"),
    CC("CC", "Class C Share", "C类股份"),
    CZ("CZ", "Class Z Share", "Z类股份"),
    SS("SS", "Stapled Security", "股份合订单位"),
    UT("UT", "Unit Trust", "单位信托"),
    ;

    private final String code;
    private final String enDesc;
    private final String cnDesc;

    public static String getCnDescByCode(String code) {
        // 默认down升序
        return Stream.of(values()).filter(sua -> StrUtil.equals(sua.code, code)).findFirst().map(StockShareTypeEnum::getCnDesc).orElse(null);
    }
}
