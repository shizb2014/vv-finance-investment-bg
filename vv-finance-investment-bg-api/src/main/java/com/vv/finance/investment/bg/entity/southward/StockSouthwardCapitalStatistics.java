package com.vv.finance.investment.bg.entity.southward;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 个股南向资金每日统计
 * </p>
 *
 * @author qinxi
 * @since 2023-06-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_stock_southward_capital_statistics")
@ApiModel(value="StockSouthwardCapitalStatistics对象", description="个股南向资金每日统计")
public class StockSouthwardCapitalStatistics implements Serializable {


    private static final long serialVersionUID = -3159093816751843937L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "股票ID")
    private Long stockId;

    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "股票名称")
    private String name;

    @ApiModelProperty(value = "今日净流入额（带正负）")
    private BigDecimal todayNetTurnoverIn;

    @ApiModelProperty(value = "今日净买卖股数）")
    private BigDecimal todayNetBuyingShares;

    @ApiModelProperty(value = "今日持股比例")
    private BigDecimal todayHoldingRate;

    @ApiModelProperty(value = "今日持股市值")
    private BigDecimal todayHoldingMarketValue;

    @ApiModelProperty(value = "统计日期")
    private LocalDate statisticsDate;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


}
