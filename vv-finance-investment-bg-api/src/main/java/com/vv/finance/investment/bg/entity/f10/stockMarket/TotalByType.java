package com.vv.finance.investment.bg.entity.f10.stockMarket;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: wsliang
 * @Version 1.0
 * 股东分类统计持股数量
 */
@Data
@Builder
@ToString
public class TotalByType implements Serializable {

    private static final long serialVersionUID = -7130835566537764265L;

    @ApiModelProperty(value = "股东类型")
    private String type;
    @ApiModelProperty(value = "该类型总数")
    private BigDecimal quantity;
    @ApiModelProperty(value = "日期")
    private Long date;

}
