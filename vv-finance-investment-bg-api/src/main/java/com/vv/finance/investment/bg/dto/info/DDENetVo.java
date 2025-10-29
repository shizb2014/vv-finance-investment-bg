package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 主力资金净值
 * @author wsliang
 * @date 2021/11/11 17:10
 **/
@Data
public class DDENetVo implements Serializable {
    private static final long serialVersionUID = -738513423587217159L;

    @ApiModelProperty("日期")
    private Long date;

    @ApiModelProperty("净流入")
    private BigDecimal netInflow;

}
