package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: wsliang
 * @Date: 2021/9/9 10:46
 **/
@Data
@AllArgsConstructor
public class SolvencyDigestVo implements Serializable {

    private static final long serialVersionUID = -8476836213401922722L;
    /**
     * 总负债/总资产
     */
    @ApiModelProperty("总负债/总资产")
    private F10Val totalLiabilityAssets;

    /**
     * 股东权益/总资产
     */
    @ApiModelProperty("股东权益/总资产")
    private F10Val stockholderEquityTotalAssets;
}
