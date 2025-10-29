package com.vv.finance.investment.bg.entity.f10.shareholder;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 股东
 * @author yangpeng
 * @date 2023/10/17
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StockHolder implements Serializable {

    private static final long serialVersionUID = -8342129787285271362L;

    /**
     * 股东名称
     */
    @ApiModelProperty(value = "股东名称")
    private String holderName;

    /**
     * 股东类型
     */
    @ApiModelProperty(value = "股东类型")
    private String holderType;

    /**
     * 所有股东类型（A,C,D,F）
     */
    @ApiModelProperty(value = "所有股东类型")
    private String holderTypes;

    /**
     * 股东类型
     */
    @ApiModelProperty(value = "股份类型")
    private String shareType;

    /**
     * 持股数量
     */
    @ApiModelProperty(value = "持股数量")
    private BigDecimal num;
    /**
     * 占比
     */
    @ApiModelProperty(value = "占比")
    private BigDecimal pop;

    /**
     * 日期 yyyyMMdd
     */
    @ApiModelProperty(value = "日期")
    private Long date;
}
