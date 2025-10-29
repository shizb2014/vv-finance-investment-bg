package com.vv.finance.investment.bg.dto.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/8/17 15:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class StockBaseInfo implements Serializable {
    private static final long serialVersionUID = 3939208646419690354L;
    @ApiModelProperty(value = "股票名称")
    private String name;
    @ApiModelProperty(value = "股票代码")
    private String code;
}
