package com.vv.finance.investment.bg.dto.stock;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fenlibao.security.sdk.ws.core.model.dto.SnapshotDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author chenyu
 * @date 2020/11/11 19:47
 */
@Data
public class OptionStockDTO implements Serializable {
    private static final long serialVersionUID = 5101104872014244095L;
    private String code;
    private String name;
    @DateTimeFormat(
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private Date time;
    private BigDecimal preclose;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal last;
    private BigDecimal sharestraded;
    private BigDecimal turnover;
    private BigInteger shortShares;
    private BigDecimal shortTurnover;
    private BigDecimal exchangeRate;
    private BigDecimal riseFall;
    private BigDecimal increase;
    private BigDecimal amplitude;
    private BigDecimal bookRate;
    private BigDecimal bookDiffer;
    @ApiModelProperty(value = "5分钟涨跌幅")
    private BigDecimal fiveChgPct;

    @ApiModelProperty(value = "60日涨跌幅")
    private BigDecimal twoMonthsChgPct;

    @ApiModelProperty(value = "年初至今涨跌")
    private BigDecimal yearToNowChgPct;

    @ApiModelProperty(value = "月初至今涨跌")
    private BigDecimal monthToNowChgPct;

    @ApiModelProperty(value = "总股本")
    private String totalEquity = "";

    @ApiModelProperty(value = "总股值")
    private String totalValue = "";

    @ApiModelProperty(value = "市盈率(静)")
    private String peRatio = "";

    @ApiModelProperty(value = "市盈率(TTM)")
    private String peTtm = "";

    @ApiModelProperty(value = "市净率")
    private String pbRatio = "";

    @ApiModelProperty(value = "52周最高")
    private String fthigh = "";

    @ApiModelProperty(value = "量比")
    private String quantityRelativeRatio = "";

    @ApiModelProperty(value = "52周最低")
    private String ftlow = "";

    @ApiModelProperty(value = "股息TTM")
    private String dividend = "";

    @ApiModelProperty(value = "股息率TTM")
    private String dividendTTMRate = "";

    @ApiModelProperty(value = "股息率LFY")
    private String dividendRate = "";

    @ApiModelProperty(value = "买入价")
    private String buyPrice = "";

    @ApiModelProperty(value = "卖出价")
    private String sellPrice = "";

    @ApiModelProperty(value = "买量")
    private String buyVol = "";

    @ApiModelProperty(value = "卖量")
    private String sellVol = "";

    @ApiModelProperty(value = "涨跌速率")
    private String riseFallRate = "";

    @ApiModelProperty(value = "均价")
    private String vwap = "";

    @ApiModelProperty(value = "历史最高 ")
    private String historyHigh = "";

    @ApiModelProperty(value = "历史最低")
    private String historyLow = "";

    @ApiModelProperty(value = "每手股数")
    private Integer lotSize;


}
