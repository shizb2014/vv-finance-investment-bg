package com.vv.finance.investment.bg.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName: StockBaseResp
 * @Description:
 * @Author: Demon
 * @Datetime: 2021/2/3   11:49
 */
@Data
public class StockBaseInfoDTO implements Serializable {
    private static final long serialVersionUID = 5785466481849307207L;

    /**
     * 证券代码ID
     */
    @ApiModelProperty(value = "证券代码ID")
    private Long stockId;

    /**
     * 证券代码
     */
    @ApiModelProperty(value = "证券代码")
    private String stockCode;

    /**
     * 证券名称
     */
    @ApiModelProperty(value = "证券名称")
    private String stockName;

    /**
     * 快照类型
     */
    @ApiModelProperty(value = "快照类型 1-指数 0-股票")
    private Integer type;


    @ApiModelProperty(value = "加入自选最新价")
    private BigDecimal toCustomizeLast;

    @ApiModelProperty(value = "加入自选时间")
    private Long toCustomizeTime;

    @ApiModelProperty(value = "股票类型")
    private Integer stockType;

    @ApiModelProperty(value = "区域类型 0-港股 1-美股")
    private Integer regionType;

}
