package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2021/4/8 19:18
 */
@Data
public class DealStatisticsDTO implements Serializable {

    private static final long serialVersionUID = -2557591114517751454L;
    @ApiModelProperty(value = "成交价")
    private BigDecimal price;
    @ApiModelProperty(value = "总成交量")
    private BigDecimal totalVolume;
    @ApiModelProperty(value = "主动买入")
    private BigDecimal inVolume;
    @ApiModelProperty(value = "主动卖出")
    private BigDecimal outVolume;
    @ApiModelProperty(value = "中性")
    private BigDecimal neuterVolume;
    @ApiModelProperty(value = "占比")
    private BigDecimal ratio;

    public void init(){
        this.totalVolume =BigDecimal.ZERO;
        this.inVolume =BigDecimal.ZERO;
        this.outVolume =BigDecimal.ZERO;
        this.neuterVolume =BigDecimal.ZERO;
    }
}
