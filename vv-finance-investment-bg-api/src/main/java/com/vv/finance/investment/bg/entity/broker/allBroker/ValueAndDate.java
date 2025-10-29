package com.vv.finance.investment.bg.entity.broker.allBroker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueAndDate implements Serializable {
    private static final long serialVersionUID = 2054555379889192816L;
    @ApiModelProperty(value = "数值")
    private BigDecimal val;
    @ApiModelProperty(value = "日期")
    private Long date;
    @ApiModelProperty(value = "收盘价")
    private BigDecimal close;

}
