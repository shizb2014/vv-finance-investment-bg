package com.vv.finance.investment.bg.constants;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.vv.finance.common.constants.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author hamilton
 * @date 2021/9/17 15:21
 */
@RequiredArgsConstructor
@Getter
public enum PublishTerminalEnum implements BaseEnum {
    /**
     * 电脑端
     */
    PC(0,"web"),
    /**
     * 手机端
     */
    APP(1,"app"),
    /**
     * 电脑和手机
     */
    PC_APP(2,"web and app");

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;



}
