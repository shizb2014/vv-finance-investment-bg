package com.vv.finance.investment.bg.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 股票code和名称
 * @Author liuxing
 * @Create 2023/6/26 14:05
 */
@Data
public class StockCodeNameBaseDTO implements Serializable {

    private static final long serialVersionUID = 5823675088865242124L;

    @ApiModelProperty(value = "股票id")
    private Long stockId;

    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "股票名称")
    private String name;

    private Integer stockType ;
    private Integer regionType ;
}
