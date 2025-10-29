package com.vv.finance.investment.bg.entity.f10.capitalstructure;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/18 11:29
 * @Version 1.0
 * 股本变动
 */
@Data
@Builder
@ToString
@ApiModel()
public class CapitalChange implements Serializable {
    private static final long serialVersionUID = -5974340026741390934L;

    /**
     * 公布日期
     */
    @ApiModelProperty(value = "公布日期")
    private String releaseDate;
    /**
     * 变动日期
     */
    @ApiModelProperty(value = "变动日期")
    private String changeDate;
    /**
     * 股份类型
     */
    @ApiModelProperty(value = "股份类型")
    private String type;
    /**
     * 股本（股）
     */
    @ApiModelProperty(value = "股本（股）")
    private BigDecimal capital;
    /**
     * 股本变动（股）
     */
    @ApiModelProperty(value = "股本变动（股）")
    private BigDecimal changeCapital;
    /**
     * 股本变动百分比
     */
    @ApiModelProperty(value = "股本变动百分比")
    private BigDecimal changeRatio;
    /**
     * 变动原因
     */
    @ApiModelProperty(value = "变动原因")
    private String changeReason;
}
