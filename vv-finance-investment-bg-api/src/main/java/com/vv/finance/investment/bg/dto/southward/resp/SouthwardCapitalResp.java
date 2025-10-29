package com.vv.finance.investment.bg.dto.southward.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author qinxi
 * @date 2023/8/23 09:57
 * @description: 南向资金列表
 */
@Data
public class SouthwardCapitalResp implements Serializable {


    private static final long serialVersionUID = -2158971277433830678L;


    @ApiModelProperty(value = "SH：沪  SZ：深  ALL:南向资金")
    private String market;


    @ApiModelProperty(value = "今日净流入额")
    private BigDecimal netTurnoverIn;

    @ApiModelProperty(value = "今日净买入额")
    private BigDecimal netBuyingTurnover;

    @ApiModelProperty(value = "今日余额")
    private BigDecimal surplusQuota;


    @ApiModelProperty(value = "近5日净买入额")
    private BigDecimal netBuyingTurnoverNearly5Days;

    @ApiModelProperty(value = "近20日净买入额")
    private BigDecimal netBuyingTurnoverNearly20Days;

    @ApiModelProperty(value = "近60日净买入额")
    private BigDecimal netBuyingTurnoverNearly60Days;


    @ApiModelProperty(value = "近5日净流入额")
    private BigDecimal netTurnoverInNearly5Days;

    @ApiModelProperty(value = "近20日净流入额")
    private BigDecimal netTurnoverInNearly20Days;

    @ApiModelProperty(value = "近60日净流入额")
    private BigDecimal netTurnoverInNearly60Days;


}
