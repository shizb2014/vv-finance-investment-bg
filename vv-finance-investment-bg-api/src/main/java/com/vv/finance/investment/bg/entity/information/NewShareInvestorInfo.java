package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 新股投资者信息
 * @author wsliang
 * @date 2021/11/2 15:01
 **/
@Data
public class NewShareInvestorInfo implements Serializable {
    private static final long serialVersionUID = 2576231774191941351L;

    /**
     * 投资者名称
     */
    @ApiModelProperty("投资者名称")
    private String name;

    /**
     * 投资货币
     */
    @ApiModelProperty("投资货币")
    private String currency;

    /**
     * 投资货币
     */
    @ApiModelProperty("投资货币中文")
    private String currencyName;

//    /**
//     * @see com.vv.finance.common.enums.CurrencyTypeEnum
//     */
//    @ApiModelProperty(value = "货币类型 0:港元 1:美元 2:人民币")
//    private Integer currencyType;

    /**
     * 投资金额
     */
    @ApiModelProperty("投资金额")
    private BigDecimal amount;

    /**
     * 计划认购股数
     */
    @ApiModelProperty("计划认购股数")
    private BigDecimal planSubscriptionNum;

    /**
     * 计划占未行使超额配股权前总发售股数占比
     */
    @ApiModelProperty("占未行使超额配股权前总发售股数占比")
    private BigDecimal planRate;

    /**
     * 实际占未行使超额配股权前总发售股数占比
     */
    @ApiModelProperty("实际占未行使超额配股权前总发售股数占比")
    private BigDecimal actualRate;

    /**
     * 实际认购股数
     */
    @ApiModelProperty("实际认购股数")
    private BigDecimal actualSubscriptionNum;

    /**
     * 股份禁售期届满日期
     */
    @ApiModelProperty("股份禁售期届满日期")
    private Long expiredDate;

}
