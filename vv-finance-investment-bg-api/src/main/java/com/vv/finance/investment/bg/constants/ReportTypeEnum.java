package com.vv.finance.investment.bg.constants;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.vv.finance.common.constants.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 报表种类
 */
@RequiredArgsConstructor
@Getter
@Deprecated
public enum ReportTypeEnum {
    Q1("Q1", "第一季业绩"),
    I("I", "中期业绩"),
    Q3("Q3", "第三季业绩"),
    F("F", "全年业绩")
    ;

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    /**
     * 不处理的报表类型
     * @return {@link List}<{@link String}>
     */
    public static List<String> unResolveTypeList() {
        return ListUtil.of("P", "Q5");
    }
}
