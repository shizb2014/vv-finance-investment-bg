package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author wsliang
 * @date 2021/11/15 15:34
 **/
@Data
public class TodayCapitalTotalDto implements Serializable {
    private static final long serialVersionUID = -1694975996816222172L;

    @ApiModelProperty("code")
    private String stockCode;

    @ApiModelProperty(value = "资金流入总额")
    private BigDecimal capitalInTotal;

    @ApiModelProperty(value = "资金流出总额")
    private BigDecimal capitalOutTotal;

    @ApiModelProperty(value = "净流入值")
    private BigDecimal net;

    @Override
    public boolean equals(Object o) {
        TodayCapitalTotalDto that = (TodayCapitalTotalDto) o;
        return Objects.equals(stockCode, that.stockCode);
    }
}
