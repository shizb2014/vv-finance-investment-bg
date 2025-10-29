package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Author: wsliang
 * @Date: 2021/9/14 10:14
 **/
@Data
@ToString
@ApiModel("日历资讯-财报")
public class FinancialReportVo implements Serializable {

    @ApiModelProperty("资讯id")
    private String newsid;

    @ApiModelProperty("时间")
    private String dateTime;

    @ApiModelProperty("日期")
    private Long date;

    @ApiModelProperty("股票代码")
    private String SECCODE;
    @ApiModelProperty("股票名称")
    private String stockName;

    @ApiModelProperty("财报链接，pdf")
    private String financialReportLink;

    @ApiModelProperty("文本")
    private String text;

}
