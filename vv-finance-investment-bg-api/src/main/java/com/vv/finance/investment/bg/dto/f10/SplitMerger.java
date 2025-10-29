package com.vv.finance.investment.bg.dto.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/8/17 19:45
 */
@Data
public class SplitMerger implements Serializable {
    private static final long serialVersionUID = -1305056095277282389L;
    @ApiModelProperty(value = "公告日期")
    private String publicationDate;
    private Long publicationDateLong;
    @ApiModelProperty(value = "重组类型")
    private String recombinationType;

    @ApiModelProperty(value = "方案说明")
    private String schemeDescription;

    @ApiModelProperty(value = "事项状态")
    private String eventStatus;


    @ApiModelProperty(value = "除净日")
    private String exDate;

    @ApiModelProperty(value = "并行证券代码")
    private String parallelSecuritiesCode;

    @ApiModelProperty(value = "并行证券名称")
    private String parallelSecuritiesName;


    @ApiModelProperty(value = "并行买卖单位")
    private BigDecimal parallelUnit;

    @ApiModelProperty(value = "并行原因")
    private String parallelReason;

    @ApiModelProperty(value = "并行开始日")
    private String parallelStartDate;

    @ApiModelProperty(value = "并行暂停买卖日")
    private String parallelSuspendDate;

    @ApiModelProperty(value = "并行买卖日")
    private String parallelDate;

    @ApiModelProperty(value = "年度")
    private String year;
}
