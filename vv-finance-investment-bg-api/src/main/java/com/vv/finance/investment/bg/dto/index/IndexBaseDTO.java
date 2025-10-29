package com.vv.finance.investment.bg.dto.index;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/27 14:14
 */
@Data
public class IndexBaseDTO implements Serializable {

    private static final long serialVersionUID = -6551345719357608515L;
    @ApiModelProperty(value = "日期")
    private Long time;
    @ApiModelProperty(value = "指数代码")
    private String code;
    @ApiModelProperty(value = "指数名称")
    private String name;
    @ApiModelProperty(value = "今日价格")
    private String last = "";
    @ApiModelProperty(value = "涨跌价")
    private String riseFall="";
    @ApiModelProperty(value = "涨跌幅")
    private String increase="";

}
