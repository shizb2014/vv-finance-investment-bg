package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/27 14:19
 */
@Data
public class IndustrySectorDTO implements Serializable {
    private static final long serialVersionUID = -381339762212608945L;
    @ApiModelProperty(value = "股票id")
    private Long stockId;

    @ApiModelProperty(value = "行业代码")
    private String code;

    @ApiModelProperty(value = "行业名称")
    private String name;

    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal chg_pct;
}
