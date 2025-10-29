package com.vv.finance.investment.bg.stock.kline.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author chenyu
 * @date 2020/11/3 17:08
 */
@Data
public class RtStockKline extends BaseStockKlineEntity{
    @ApiModelProperty("现价")
    private String price;
    @ApiModelProperty("平均价")
    private String avg_price;
}
