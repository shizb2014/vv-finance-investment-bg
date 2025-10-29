package com.vv.finance.investment.bg.dto.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @公司：微微科技有限公司（金融事业部）
 * @描述：
 * @作者：Liam（梁殿豪）
 * @邮箱：liangdianhao@vv.cn
 * @时间：2021/8/17 17:00
 * @版本：1.0
 */
@Data
public class NewStockEventTimeLineDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "代码")
    private String stockCode;
    @ApiModelProperty(value = "股票名称")
    private String stockName;
    @ApiModelProperty(value = "申请日期")
    private Long applyTime;
    @ApiModelProperty(value = "申请起始日")
    private Long applyStartTime;
    @ApiModelProperty(value = "申请截止日")
    private Long applyEndTime;
    @ApiModelProperty(value = "定价日")
    private Long setPriceTime;
    @ApiModelProperty(value = "发行结果公布日")
    private Long announcedTime;
    @ApiModelProperty(value = "预计上市日")
    private Long marketTime;
    @ApiModelProperty(value = "截止日")
    private Long endTime;
    @ApiModelProperty(value = "是否上市 0-否 1-是")
    private Integer isMarket;
}
