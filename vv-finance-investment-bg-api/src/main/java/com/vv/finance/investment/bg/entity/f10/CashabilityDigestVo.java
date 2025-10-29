package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: wsliang
 * @Date: 2021/9/9 10:47
 **/
@Data
@AllArgsConstructor
public class CashabilityDigestVo implements Serializable {
    private static final long serialVersionUID = 1979497822378348632L;
    /**
     * 流动比率
     */
    @ApiModelProperty("流动比率")
    private F10Val currentRatio;
    /**
     * 速动比率
     */
    @ApiModelProperty("速动比率")
    private  F10Val quickRatio;
}
