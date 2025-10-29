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
 * @Description:南向资金资金列表返回对象信息
 */
@Data
public class CapitalInfoResp implements Serializable {

    private static final long serialVersionUID = -8692969240583008511L;

    @ApiModelProperty(value = "资金列表")
    private List<CapitalInfo> list;

    @Data
    @ApiModel
    public static class CapitalInfo implements Serializable {

        private static final long serialVersionUID = 7515075317253741949L;

        @ApiModelProperty(value = "资金类型")
        private BigDecimal capitalType;

        @ApiModelProperty(value = "今日净买入")
        private BigDecimal TodayNetBuyingTurnover;

        @ApiModelProperty(value = "今日净流入")
        private BigDecimal TodayNetTurnoverIn;

        @ApiModelProperty(value = "今日余额")
        private BigDecimal surplusQuota;

        @ApiModelProperty(value = "近5日净买入")
        private BigDecimal fiveDaysNetBuyingTurnover;

        @ApiModelProperty(value = "近5日净流入")
        private BigDecimal fiveDaysNetTurnoverIn;

        @ApiModelProperty(value = "近20日净买入")
        private BigDecimal twentyDaysNetBuyingTurnover;

        @ApiModelProperty(value = "近20日净流入")
        private BigDecimal twentyDaysNetTurnoverIn;

        @ApiModelProperty(value = "近60日净买入")
        private BigDecimal sixtyDaysNetBuyingTurnover;

        @ApiModelProperty(value = "近60日净流入")
        private BigDecimal sixtyDaysNetTurnoverIn;
    }
}