package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 累计资金流入
 *
 * @author wsliang
 * @date 2021/11/12 15:35
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotalCapitalInflowsDTO implements Serializable {
    private static final long serialVersionUID = -1596111389747622736L;

    @ApiModelProperty("股票代码")
    private String stockCode;

    @ApiModelProperty("更新时间")
    private Long updateTime;

//    @ApiModelProperty("一日累计")
//    private BigDecimal oneDayTotal;

    @ApiModelProperty("四日累计")
    private BigDecimal fiveDaysTotal;

    @ApiModelProperty("九日累计")
    private BigDecimal tenDaysTotal;

    @ApiModelProperty("十九日累计")
    private BigDecimal twentyDaysTotal;

    @ApiModelProperty("五十九日累计")
    private BigDecimal sixtyDaysTotal;


}
