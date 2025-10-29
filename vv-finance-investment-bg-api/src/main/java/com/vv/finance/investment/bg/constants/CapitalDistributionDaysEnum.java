package com.vv.finance.investment.bg.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资金分布
 * @author wsliang
 * @date 2021/11/15 15:10
 **/
@Getter
@AllArgsConstructor
public enum CapitalDistributionDaysEnum {
    ONE_DAY(0, 1, "今日"),
    FIVE_DAYS(1, 5, "五日"),
    TEN_DAYS(2, 10, "十日"),
    TWENTY_DAYS(3, 20, "二十日"),
    SIXTY_DAYS(4, 60, "六十日"),
    ;

    private Integer code;
    private Integer days;
    private String note;
}
