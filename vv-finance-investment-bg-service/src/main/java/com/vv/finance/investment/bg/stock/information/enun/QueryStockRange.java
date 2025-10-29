package com.vv.finance.investment.bg.stock.information.enun;

import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wsliang
 * @date 2021/9/18 10:04
 **/
@AllArgsConstructor
@Getter
public enum QueryStockRange {
    ALL(0),
    FREE(1),
    POSITION(2),
    CLEARANCE(3),
    FOCUS(4),
    CONDITION(5),
    CUSTOM(6),
    ;

    private Integer stockRange;

}
