package com.vv.finance.investment.bg.entity.f10.capitalstructure;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/18 11:17
 * @Version 1.0
 */
@Data
@Builder
@ToString
public class CapitalStatistics implements Serializable {
    private static final long serialVersionUID = 8251829028020751693L;
    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private String updateDate;
    /**
     * 自由流通股本
     */
    @ApiModelProperty("自由流通股本")
    private BigDecimal freelyCirculating;
    /**
     * 非自由流通股本
     */
    @ApiModelProperty("非自由流通股本")
    private BigDecimal nonFreeCirculating;

    /**
     * 已发行股本数量
     */
    @ApiModelProperty("已发行股本数量")
    private BigDecimal issuedCirculating;
    /**
     * 类型
     */
    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("最后股本变动时间")
    private String lastTime;
}
