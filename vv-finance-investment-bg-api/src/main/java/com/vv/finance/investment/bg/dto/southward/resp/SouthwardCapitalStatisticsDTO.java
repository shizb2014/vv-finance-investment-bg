package com.vv.finance.investment.bg.dto.southward.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author qinxi
 * @date 2023/8/25 11:10
 * @description:
 */
@Data
public class SouthwardCapitalStatisticsDTO implements Serializable {

    private static final long serialVersionUID = -3433376899221917258L;


    @ApiModelProperty(value = "SH：沪  SZ：深")
    private String market;

    @ApiModelProperty(value = "今日净流入额（带正负）")
    private BigDecimal netTurnoverIn;

    @ApiModelProperty(value = "今日净买入额（带正负）")
    private BigDecimal netBuyingTurnover;

    @ApiModelProperty(value = "今日余额（带正负）")
    private BigDecimal surplusQuota;

    @ApiModelProperty(value = "统计日期")
    private LocalDate statisticsDate;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "净买入响应时间")
    private LocalDateTime turnoverFlowRespTime;

    @ApiModelProperty(value = "净流入、余额响应时间")
    private LocalDateTime connectBalanceRespTime;

}
