package com.vv.finance.investment.bg.dto.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/8/17 18:56
 * 分红前后涨跌幅变化
 */
@Data
public class ChangeBeforeAfterDividend implements Serializable {
    private static final long serialVersionUID = 6968047496460733731L;
    @ApiModelProperty(value = "日期")
    private String date;

    @ApiModelProperty(value = "10日涨跌幅")
    private BigDecimal riseFall10th;

    @ApiModelProperty(value = "10日最高涨幅 ")
    private BigDecimal   highestIncrease10th;
    @ApiModelProperty(value = "0 前  1-- 后 ")
    private Integer type;

}
