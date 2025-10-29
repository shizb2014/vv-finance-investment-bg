package com.vv.finance.investment.bg.dto.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName StockBrokerDetailDto
 * @Deacription TODO 经纪席位详情
 * @Author lh.sz
 * @Date 2020年11月13日 16:34
 **/
@Data
public class StockBrokerDetailDto implements Serializable {
    private static final long serialVersionUID = 7506157592321138442L;
    @ApiModelProperty(value = "经纪席位代码")
    private String stockBrokerCode;
    @ApiModelProperty(value = "经纪席位名称")
    private String stockBrokerName;
}
