package com.vv.finance.investment.bg.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author wsliang
 * @date 2021/11/22 14:26
 **/
@Data
public class TradeReq implements Serializable {
    @ApiModelProperty("股票代码")
    private String code;
    @ApiModelProperty("当前页码")
    private Long current;
    @ApiModelProperty("页码长度")
    private Long pageSize;
    @ApiModelProperty("时间")
    private Long time;
    @ApiModelProperty("最小成交量")
    private Long minVolume;
    @ApiModelProperty("最大成交量")
    private Long maxVolume;
    @ApiModelProperty("最小成交额")
    private BigDecimal minPrice;
    @ApiModelProperty("最大成交额")
    private BigDecimal maxPrice;
}
