package com.vv.finance.investment.bg.constants;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.vv.finance.common.constants.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 经纪商时间区间枚举
 * @Auther: shizhibiao
 * @Date: 2022/10/17
 * @Description: com.vv.finance.investment.bg.constants
 * @version: 1.0
 */
@RequiredArgsConstructor
@Getter
public enum BrokerSectionTypeEnum implements BaseEnum {

    ONE_DAY(0, 1, "今日"),
    FIVE_DAYS( 1, 5,"近五日"),
    TEN_DAYS( 2, 10,"近十日"),
    TWENTY_DAYS( 3, 20,"近二十日"),
    SIXTY_DAYS( 4, 60,"近六十日"),

    YEAR_DAYS( 5, 365, "一年");


    @EnumValue
    @JsonValue
    private final int code;
    private final int day;
    private final String desc;

}
