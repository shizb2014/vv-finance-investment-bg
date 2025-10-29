package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订阅行业涨跌幅
 *
 * @author wsliang
 * @date 2021/9/26 19:22
 **/
@Data
@ToString
public class SubBusinessInfo implements Serializable {

    private static final long serialVersionUID = -2121013983237817467L;

    @ApiModelProperty("所属行业名称")
    private String busName;

    @ApiModelProperty("行业代码")
    private String busCode;

//    @ApiModelProperty("订阅事件")
//    private String subEvent;

    @ApiModelProperty("涨跌幅")
    private BigDecimal increase;
}
