package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: wsliang
 * @Date: 2021/9/9 10:45
 **/
@Data
@AllArgsConstructor
public class ProfitabilityDigestVo implements Serializable {

    private static final long serialVersionUID = -9011737033596021971L;
    /**
     * 股东权益回报率
     */
    @ApiModelProperty("股东权益回报率")
    private F10Val roe;
    /**
     * 资产回报率
     */
    @ApiModelProperty("资产回报率")
    private F10Val roa;
}
