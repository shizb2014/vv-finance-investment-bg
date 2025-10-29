package com.vv.finance.investment.bg.stock.information.enun;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: wsliang
 * @Date: 2021/9/17 16:50
 **/
@AllArgsConstructor
@Getter
public enum QueryType {
    OLD(0),
    NEW(1),
    ;
    private Integer type;

}
