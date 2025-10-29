package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 资金分布
 *
 * @author wsliang
 * @date 2021/11/18 19:18
 **/
@Data
public class BaseTradeStatisticsDto implements Serializable {
    private static final long serialVersionUID = 8025471554441956125L;

    @ApiModelProperty(value = "资金总额")
    private BigDecimal capitalTotal;
    @ApiModelProperty(value = "资金小单")
    private BigDecimal capitalSmall;
    @ApiModelProperty(value = "资金中单")
    private BigDecimal capitalMid;
    @ApiModelProperty(value = "资金大单")
    private BigDecimal capitalLarge;
    @ApiModelProperty(value = "资金特大单")
    private BigDecimal capitalXLarge;

    @ApiModelProperty(value = "总单数")
    private BigDecimal qtyTotal;
    @ApiModelProperty(value = "小单数")
    private BigDecimal qtySmall;
    @ApiModelProperty(value = "中单数")
    private BigDecimal qtyMid;
    @ApiModelProperty(value = "大单")
    private BigDecimal qtyLarge;
    @ApiModelProperty(value = "特大单")
    private BigDecimal qtyXLarge;

    public BaseTradeStatisticsDto() {
        build();
    }

    public void build() {
        this.capitalTotal = BigDecimal.ZERO;
        this.capitalSmall = BigDecimal.ZERO;
        this.capitalMid = BigDecimal.ZERO;
        this.capitalLarge = BigDecimal.ZERO;
        this.capitalXLarge = BigDecimal.ZERO;
        this.qtyTotal = BigDecimal.ZERO;
        this.qtySmall = BigDecimal.ZERO;
        this.qtyMid = BigDecimal.ZERO;
        this.qtyLarge = BigDecimal.ZERO;
        this.qtyXLarge = BigDecimal.ZERO;
    }
}
