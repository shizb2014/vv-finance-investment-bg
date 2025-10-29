package com.vv.finance.investment.bg.entity.f10.shareholder;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/17 17:19
 * @Version 1.0
 */
@Data
@Builder
@ToString
public class Val implements Serializable {

    private static final long serialVersionUID = -6701816181995417156L;

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
     * 日期
     */
    @ApiModelProperty(value = "日期")
    private Long Date;
}
