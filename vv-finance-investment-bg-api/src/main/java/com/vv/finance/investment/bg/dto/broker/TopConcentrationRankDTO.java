package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName top5/10集中度变动排行榜
 * @Author liujiajian
 * @Date 2022/10/09
 */
@Data
public class TopConcentrationRankDTO implements Serializable {
    private static final long serialVersionUID = -5409873476838108362L;

    @ApiModelProperty("股票ID")
    private Long stockId;

    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "股票名称")
    private String name;

    @ApiModelProperty(value = "行业代码")
    private String industryId;

    @ApiModelProperty(value = "所属行业名称")
    private String industryName;

    @ApiModelProperty(value = "top5/10占比")
    private BigDecimal topConcentrationPercent;

    @ApiModelProperty(value = "集中度变动比例")
    private BigDecimal concentrationTrend;
}
