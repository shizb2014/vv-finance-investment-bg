package com.vv.finance.investment.bg.dto.uts.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/12/9 15:30
 */
@Data
public class HoldStockChange implements Serializable {
    private static final long serialVersionUID = -3479557679685716317L;
    @ApiModelProperty(value = "代码")
    private String code;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "最新价")
    private BigDecimal last;
    @ApiModelProperty(value = "涨跌额")
    private BigDecimal chg;
    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal chgPct;
    @ApiModelProperty(value = "增减持股数")
    private BigDecimal changeQuantity;
    @ApiModelProperty(value = "增减持金额")
    private BigDecimal changeAmount;
    @ApiModelProperty(value = "变动后持股比例")
    private BigDecimal changeRate;
    @ApiModelProperty(value = "增减持人")
    private String changeNames;
    @ApiModelProperty(value = "公布日期")
    private Long publishTime;


}
