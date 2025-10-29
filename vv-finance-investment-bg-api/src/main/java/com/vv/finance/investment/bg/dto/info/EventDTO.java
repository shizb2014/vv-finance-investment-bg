package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2021/3/16 10:51
 */
@Data
public class EventDTO implements Serializable {
    private static final long serialVersionUID = 2568785048557982256L;
    /**
     * 时间
     */
    private Long time;
    /**
     * 事件类型
     */
    private Integer eventType;
    /**
     * 除权方案
     */
    private String exContent;
    /**
     * 营业收入
     */
    private BigDecimal operatingRevenue;
    /**
     * 净利润
     */
    private BigDecimal netProfits;
    /**
     * 币种描述
     */
    private String CurrencyDesc;

    @ApiModelProperty(value = "原交易所")
    private String sourceExchange;

    @ApiModelProperty(value = "现交易所")
    private String targetExchange;

}
