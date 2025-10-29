package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName 经纪商持股动向
 * @Author liujiajian
 * @Date 2022/10/9
 */
@Data
public class BrokerHoldingsTrendDTO implements Serializable {
    private static final long serialVersionUID = 2846921252153550245L;

    @ApiModelProperty("股票ID")
    private Long stockId;

    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "股票名称")
    private String name;

    @ApiModelProperty(value = "行业代码")
    private String industryId;

    @ApiModelProperty(value = "所属行业名称")
    private String industryName;

    @ApiModelProperty(value = "经纪商编号")
    private String brokerId;

    @ApiModelProperty(value = "经纪商名称")
    private String brokerName;

    @ApiModelProperty(value = "增减持比例/市值的数值")
    private BigDecimal num;



}
