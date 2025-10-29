package com.vv.finance.investment.bg.mongo.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author hamilton
 * @date 2021/7/30 18:20
 */
@Data
public class F10EntityBase implements Serializable {
    private static final long serialVersionUID = -5007761504981825426L;
    @ApiModelProperty(value = "唯一标识")
    private String id;
    @Indexed
    private String stockCode;
    @ApiModelProperty(value = "报告类型 I\n" +
            "F\n" +
            "Q1\n" +
            "Q3\n" +
            "P\n" +
            "Q4\n" +
            "Q5")
    private String reportType;
    @ApiModelProperty(value = "起始日期yyyy/MM/dd")
    private String startDate;
    @ApiModelProperty(value = "截止日期yyyy/MM/dd")
    private String endDate;
    @ApiModelProperty(value = "发布日期 yyyy/MM/dd")
    private String releaseDate;
    @ApiModelProperty(value = "起始日期时间戳")
    private Long startTimestamp;
    @ApiModelProperty(value = "截止日期时间戳")
    private Long endTimestamp;
    @ApiModelProperty(value = "发布日期时间戳ms")
    private Long releaseTimestamp;
    @ApiModelProperty(value = "币种 英文简称例如 HKD ")
    private String currency;

    @ApiModelProperty(value = "汇率 ")
    private BigDecimal exchangeRate = BigDecimal.ONE;

    private Date updateTime;

}
