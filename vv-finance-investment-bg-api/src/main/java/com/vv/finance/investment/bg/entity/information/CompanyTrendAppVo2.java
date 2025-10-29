package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Author: wsliang
 * @Date: 2021/9/14 10:39
 **/
@Data
@ToString
@ApiModel("日历资讯-公司动向")
public class CompanyTrendAppVo2 implements Serializable {
    private static final long serialVersionUID = 8140568842346780088L;

    @ApiModelProperty(value = "自增id")
    private Long id;
    @ApiModelProperty(value = "股票代码")
    private String SECCODE;

    @ApiModelProperty(value = "股票代码")
    private String stockCode;
    @ApiModelProperty("股票名称")
    private String stockName;

    @ApiModelProperty("文本内容")
    private String content;

    @ApiModelProperty("发布日期")
    private Long releaseDate;

    @ApiModelProperty("动向类型")
    private String trendsType;

    @ApiModelProperty("动向类型 1：交易警报，2：并行交易 ，3：停/复牌，4：股东大会，5：公司重组，6：收购及合并")
    private Integer trendsCode;


}
