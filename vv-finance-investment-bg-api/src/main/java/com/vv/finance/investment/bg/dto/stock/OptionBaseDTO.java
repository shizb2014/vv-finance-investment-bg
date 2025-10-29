package com.vv.finance.investment.bg.dto.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author chenyu
 * @date 2021/1/5 10:29
 */
@Data
public class OptionBaseDTO implements Serializable {
    private static final long serialVersionUID = -6717905404821847015L;
    @ApiModelProperty(value = "股票名称")
    private String name;
    @ApiModelProperty(value = "股票代码")
    private String code;
    @ApiModelProperty(value = "是否自选")
    private Boolean optional;
}
