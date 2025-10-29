package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wsliang
 * @date 2021/11/18 19:27
 **/
@Data
public class TradeStatisticsDto implements Serializable {
    private static final long serialVersionUID = 4427055985593961952L;

    @ApiModelProperty(value = "时间")
    private Long time;
    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty("股票ID")
    private Long stockId;

    @ApiModelProperty("流入单统计")
    private BaseTradeStatisticsDto typeIn;

    @ApiModelProperty("流出单统计")
    private BaseTradeStatisticsDto typeOut;

    public TradeStatisticsDto() {
        this.typeIn = new BaseTradeStatisticsDto();
        this.typeOut = new BaseTradeStatisticsDto();
        this.time = 0L;
    }
}
