package com.vv.finance.investment.bg.dto.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName StockBrokerDto
 * @Deacription TODO
 * @Author lh.sz
 * @Date 2020年11月13日 16:32
 **/
@Data
public class StockBrokerDto implements Serializable {
    private static final long serialVersionUID = -4573252857183926086L;
    @ApiModelProperty(value = "股票代码")
    private String code;
    @ApiModelProperty(value = "时间")
    private String time;
    @ApiModelProperty(value = "类型")
    private String type;
    @ApiModelProperty(value = "买卖方经纪席位详情")
    private OrderBrokerDetailDto orderBrokerDetailDto;
}
