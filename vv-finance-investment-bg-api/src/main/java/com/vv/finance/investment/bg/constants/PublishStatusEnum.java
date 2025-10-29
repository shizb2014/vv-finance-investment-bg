package com.vv.finance.investment.bg.constants;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.vv.finance.common.constants.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author hamilton
 * @date 2021/9/17 15:18
 */
@RequiredArgsConstructor
@Getter
public enum PublishStatusEnum implements BaseEnum {
    /**
     * 发布
     */
    YES(0, "发布"),
    /**
     * 不发布
     */
    NO(1, "不发布");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
}
