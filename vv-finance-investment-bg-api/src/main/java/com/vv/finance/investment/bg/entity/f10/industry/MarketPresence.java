package com.vv.finance.investment.bg.entity.f10.industry;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName MarketPresenceVal
 * @Deacription 市场表现对比
 * @Author lh.sz
 * @Date 2021年08月17日 11:14
 **/
@Data
@ToString
@Builder
public class MarketPresence implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

    @ApiModelProperty(value = "代码")
    private String code;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "今日涨跌幅")
    private BigDecimal todayChgPct;

    @ApiModelProperty("5日涨跌幅")
    private BigDecimal fiveDayChgPct;

    @ApiModelProperty(value = "近一周涨跌幅")
    private BigDecimal weekChgPct;

    @ApiModelProperty(value = "近一月累计涨跌幅")
    private BigDecimal monthChgPct;

    @ApiModelProperty(value = "近3月累计涨跌幅")
    private BigDecimal nearThreeMonthChgPct;

    @ApiModelProperty(value = "近6月累计涨跌幅")
    private BigDecimal nearSixMonthChgPct;

    @ApiModelProperty(value = "五十二周累计涨跌幅")
    private BigDecimal fiftyTwoWeeksChgPct;

    @ApiModelProperty(value = "年初至今累计涨跌幅")
    private BigDecimal yearToDateChgPct;

    @ApiModelProperty(value = "近1年累计涨跌幅")
    private BigDecimal nearOneYearChgPct;

    @ApiModelProperty(value = "近2年累计涨跌幅")
    private BigDecimal nearTwoYearChgPct;

    @ApiModelProperty(value = "近3年累计涨跌幅")
    private BigDecimal nearThreeYearChgPct;

    @ApiModelProperty(value = "时间")
    private Long time;
    @ApiModelProperty(value = "1:股票 2:指数 3:行业")
    private Integer type;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
    @ApiModelProperty(value = "日期")
    private String strTime;
}
