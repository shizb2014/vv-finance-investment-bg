package com.vv.finance.investment.bg.entity.f10;

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
public class PerShareIndicatorDigestVo implements Serializable {

    private static final long serialVersionUID = -7778452456196491585L;
    /**
     * 每股盈利
     */
    @ApiModelProperty("每股盈利")
    private F10Val earningPerShare;
    /**
     * 每股资产净值
     */
    @ApiModelProperty("每股资产净值")
    private F10Val netAssetPerShare;
}
