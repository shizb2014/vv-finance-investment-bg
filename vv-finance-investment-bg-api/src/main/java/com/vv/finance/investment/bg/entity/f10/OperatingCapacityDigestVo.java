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
public class OperatingCapacityDigestVo implements Serializable {

    private static final long serialVersionUID = 7500890343808001805L;
    /**
     * 资产周转率(非金融)
     * 减值准备对客户贷款比率(金融)
     * 毛承保费及保单费增长(保险)
     */
    @ApiModelProperty("资产周转率(非金融)\n" +
            "减值准备对客户贷款比率(金融)\n" +
            "毛承保费及保单费增长(保险)")
    private F10Val totalAssetsTurnover;
    /**
     * 流动资金周转率(非金融)
     * 资本重组比率(金融)
     * 保险准备金增长(保险)
     */
    @ApiModelProperty("流动资金周转率(非金融)\n" +
            "资本重组比率(金融)\n" +
            "保险准备金增长(保险)")
    private F10Val currentAssetsTurnover;


}
