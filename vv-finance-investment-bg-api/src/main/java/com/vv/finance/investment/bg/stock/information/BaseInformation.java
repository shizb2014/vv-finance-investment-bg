package com.vv.finance.investment.bg.stock.information;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @ClassName BaseInformation
 * @Deacription 资讯base
 * @Author lh.sz
 * @Date 2021年09月13日 11:12
 **/
@Data
@ToString
public class BaseInformation implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

    private Long id;

    @ApiModelProperty(value = "新闻ID")
    private Long newsId;

    @ApiModelProperty(value = "新闻标题")
    private String newsTitle;

    @ApiModelProperty(value = "新闻日期")
    private LocalDate date;

    @ApiModelProperty(value = "新闻时间 HH:mm:ss")
    private String time;

    @ApiModelProperty(value = "新闻内容")
    private String content;

    @ApiModelProperty(value = "新闻类别")
    private String newsType;

    @ApiModelProperty(value = "新闻来源")
    private String source;

    @ApiModelProperty(value = "新闻市场")
    private String market;

    @ApiModelProperty(value = "新闻相关股票代码")
    private String stockCode;

    @ApiModelProperty(value = "新闻相关股票名称")
    private String stockName;

    @ApiModelProperty(value = "涨跌额")
    private BigDecimal chg;

    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal chgPct;
}
