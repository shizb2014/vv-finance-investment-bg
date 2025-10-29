package com.vv.finance.investment.bg.dto.newcode.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description 新股列表
 * @Author liuxing
 * @Create 2023/6/26 14:08
 */
@Data
public class NewStockListResp implements java.io.Serializable{

    private static final long serialVersionUID = 4871195649382975999L;

    @ApiModelProperty(value = "股票id")
    private Long stockId;

    @ApiModelProperty(value = "股票代码")
    private String code;
    @ApiModelProperty(value = "股票名称")
    private String name;
    @ApiModelProperty(value = "发行价")
    private BigDecimal issuePrice;
    @ApiModelProperty(value = "首日涨幅")
    private BigDecimal firstDayChangeRate;
    @ApiModelProperty(value = "上市至今涨跌幅")
    private BigDecimal totalChangeRate;
    @ApiModelProperty(value = "年初至今涨跌幅")
    private BigDecimal yearToNowChgPct;
    @ApiModelProperty(value = "连涨天数")
    private Integer continuousRiseDays;
//    @ApiModelProperty(value = "成交量")
//    private BigDecimal volume;
//    @ApiModelProperty(value = "成交额")
//    private BigDecimal amount;
    @ApiModelProperty(value = "认购倍数")
    private BigDecimal subMultiple;
    @ApiModelProperty(value = "一手中签率")
    private BigDecimal oneLotSuccRate;
//    @ApiModelProperty(value = "稳中股数")
//    private BigDecimal applyLotsFor1Lot;
    @ApiModelProperty(value = "首日一手最高盈利")
    private BigDecimal htOneLotProfitFD;
    @ApiModelProperty(value = "上市时间(时间戳)")
    private Long listingDateTimestamp;

    @ApiModelProperty("成交量")
    private BigDecimal sharesTraded;
    @ApiModelProperty("成交额")
    private BigDecimal turnover;
    @ApiModelProperty("最新价")
    private BigDecimal last;
    @ApiModelProperty("涨跌幅")
    private BigDecimal chgPct;
    @ApiModelProperty("涨跌额")
    private BigDecimal chg;
    @ApiModelProperty("市盈率TTM")
    private BigDecimal peTtm;
    @ApiModelProperty("总市值")
    private BigDecimal totalValue;

    @ApiModelProperty(value = "换手率")
    private BigDecimal turnoverRate;
    @ApiModelProperty("所属行业名称")
    private String industryName;
    @ApiModelProperty("所属行业代码")
    private String industryCode;
    private Integer stockType ;
    private Integer regionType ;
}
