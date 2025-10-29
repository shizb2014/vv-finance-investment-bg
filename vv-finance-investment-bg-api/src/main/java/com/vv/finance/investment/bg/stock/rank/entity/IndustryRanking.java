package com.vv.finance.investment.bg.stock.rank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 行业排行榜
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_industry_ranking")
@ApiModel(value="IndustryRanking对象", description="行业排行榜")
public class IndustryRanking implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "行业时间")
    private LocalDateTime mktTime;

    @ApiModelProperty(value = "行业代码")
    private String symbol;

    @ApiModelProperty(value = "行业名称")
    private String name;

    @ApiModelProperty(value = "平均涨跌幅")
    private BigDecimal chgPct;

    @ApiModelProperty(value = "总成交量")
    private BigDecimal amount;

    @ApiModelProperty(value = "领涨股名称")
    private String firstStockCode;

    @ApiModelProperty(value = "领涨股涨跌幅")
    private BigDecimal firstStockChgPct;

    @ApiModelProperty(value = "领跌股名称")
    private String finalStockCode;

    @ApiModelProperty(value = "领跌股涨跌幅")
    private BigDecimal finalStockChgPct;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private String createBy;


}
