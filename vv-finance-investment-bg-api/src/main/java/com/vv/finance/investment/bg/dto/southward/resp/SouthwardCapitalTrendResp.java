package com.vv.finance.investment.bg.dto.southward.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author:maling
 * @Date:2023/6/26
 * @Description:南向资金趋势图返回对象信息
 */
@Data
public class SouthwardCapitalTrendResp implements Serializable {

    private static final long serialVersionUID = -8666687144409180557L;

    @ApiModelProperty(value = "趋势信息列表")
    private List<CapitalTrend> capitalTrendList;

    @Data
    @ApiModel
    public static class CapitalTrend implements Serializable {

        private static final long serialVersionUID = 2323287987592613422L;

        @ApiModelProperty(value = "日期")
        private Long time;

        @ApiModelProperty(value = "港股通(沪)净买入/净流入/资金余额")
        private BigDecimal shCapital;

        @ApiModelProperty(value = "港股通(深)净买入/净流入/资金余额")
        private BigDecimal szCapital;

        @ApiModelProperty(value = "南向资金净买入/净流入/资金余额")
        private BigDecimal allCapital;

        @ApiModelProperty(value = "恒生指数")
        private BigDecimal hengShengIndex;

        @ApiModelProperty(value = "恒生指数涨跌额")
        private BigDecimal hsIndexChg;
    }



}