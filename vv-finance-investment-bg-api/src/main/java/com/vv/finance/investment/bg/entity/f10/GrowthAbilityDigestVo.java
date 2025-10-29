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
public class GrowthAbilityDigestVo implements Serializable {
    private static final long serialVersionUID = 1267014695036245419L;

    /**
     * 营业收入增长率
     */
    @ApiModelProperty("营业收入增长率")
    private F10Val operatingRevenueGrowth;
    /**
     * 净利润增长率
     */
    @ApiModelProperty("净利润增长率")
    private F10Val netProfitGrowth;
}
