package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author: wsliang
 * @Date: 2021/9/14 16:12
 **/
@Data
@ToString
//@Builder
@ApiModel("简要股票信息")
public class SimpleStockVo implements Serializable {
    private static final long serialVersionUID = -1265368111399666149L;

    @ApiModelProperty(value = "股票名字")
    private String name;

    @ApiModelProperty(value = "股票ID")
    private Long stockId;

    @ApiModelProperty(value = "股票代码")
    private String stockCode;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "涨跌额")
    private BigDecimal chg;

    @ApiModelProperty(value = "涨跌辐")
    private BigDecimal chgPct;

    @ApiModelProperty(value = "权证代码")
    private String warrantCode;
    @ApiModelProperty(value = "权证名称")
    private String warrantName;
    @ApiModelProperty("区域代码 0-中国香港 1-美国 2-中国大陆 3-新加坡")
    private Integer regionType;
    @ApiModelProperty("金融产品类型 1-正股 2-ETF 3-权证 4-指数 5-板块 6-其他 7-基金 8-债券 9-ETN；决定点击跳转的页面类型是指数、股票、权证、基金、期货等；")
    private Integer stockType;

    @ApiModelProperty(value = "最新价")
    private BigDecimal last;
}
