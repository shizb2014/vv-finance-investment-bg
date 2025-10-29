package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 交易资金体量区分
 * 特大单/大单/中单/小单
 *
 * @author wsliang
 * @date 2021/11/10 19:24
 **/
@Data
public class FundSizeCriterionDTO implements Serializable {
    private static final long serialVersionUID = 5577496731720650721L;

    /**
     * 查询前30日
     */
    @ApiModelProperty(value = "中单")
    private BigDecimal mValue;
    @ApiModelProperty(value = "大单")
    private BigDecimal lValue;
    @ApiModelProperty(value = "特大单")
    private BigDecimal xlValue;

    /**
     * 异动只查询前五日
     */
    @ApiModelProperty(value = "异动判断")
    private BigDecimal moveValue;

    public FundSizeCriterionDTO() {
        this.mValue = BigDecimal.ZERO;
        this.lValue = BigDecimal.ZERO;
        this.xlValue = BigDecimal.ZERO;
        this.moveValue = BigDecimal.ZERO;
    }
}
