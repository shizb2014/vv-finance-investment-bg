package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 净流入排名
 * @author wsliang
 * @date 2021/11/11 17:10
 **/
@Data
@Builder
public class NetRankVo implements Serializable {
    private static final long serialVersionUID = -738513423587217159L;

    @ApiModelProperty("统计的天数")
    private Integer days;

    @ApiModelProperty("净流入")
    private BigDecimal netInflow;

    @ApiModelProperty("名次")
    private Integer rank;
}
