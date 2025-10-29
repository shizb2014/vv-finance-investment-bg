package com.vv.finance.investment.bg.dto.southward.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author:maling
 * @Date:2023/6/26
 * @Description:南向资金趋势图请求参数对象
 */
@Data
public class SouthwardCapitalTrendReq implements Serializable {
    private static final long serialVersionUID = 8582903009088129615L;

    @ApiModelProperty(value = "趋势图tab类型 0:净买入 1:净流入 2:资金余额")
    private Integer trendType;

    @ApiModelProperty(value = "日期类型 0:当日 1:近5日 2:近20日：3:近60日")
    private Integer dateType;
}