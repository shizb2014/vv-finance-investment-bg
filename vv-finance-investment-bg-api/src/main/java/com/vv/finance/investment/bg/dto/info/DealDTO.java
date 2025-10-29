package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2021/5/8 15:10
 */
@Data
public class DealDTO implements Serializable {
    private static final long serialVersionUID = -6633188659555684732L;
    @ApiModelProperty("交易时间")
    private Long time;
    @ApiModelProperty("交易类型 0-快捷交易  1-策略交易")
    private Integer tradeType;
    @ApiModelProperty("交易方向 0-买入  1-卖出")
    private Integer direct;
    @ApiModelProperty("成交价格")
    private BigDecimal price;
    @ApiModelProperty("成交数量")
    private Integer num;
    @ApiModelProperty("负责人")
    private String principalNickName;
    @ApiModelProperty("操作人")
    private String operUserNickName;
    @ApiModelProperty("父节点ID")
    private Long parentPointId;
    @ApiModelProperty("订单ID")
    private Long orderId;
}
