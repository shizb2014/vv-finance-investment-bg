package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author chenyu
 * @date 2021/4/9 17:38
 */

@Data
public class StockCapitalDTO implements Serializable {

    @ApiModelProperty(value = "股票名")
    private String name;

    @ApiModelProperty(value = "代码")
    private String code;

    @ApiModelProperty(value = "涨跌额")
    private String cgh;

    @ApiModelProperty(value = "涨跌幅")
    private String cghPct;

    @ApiModelProperty(value = "净流入")
    private String netCapitalInflow;



}
