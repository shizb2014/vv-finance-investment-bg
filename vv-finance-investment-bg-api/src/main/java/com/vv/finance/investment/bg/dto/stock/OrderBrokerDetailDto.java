package com.vv.finance.investment.bg.dto.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName OrderBrokerDetailDto
 * @Deacription TODO
 * @Author lh.sz
 * @Date 2020年11月25日 11:42
 **/
@Data
public class OrderBrokerDetailDto implements Serializable {
    private static final long serialVersionUID = 2785834498464566089L;
    @ApiModelProperty(value = "买方席位详情")
    private List<List<StockBrokerDetailDto>> buyDetailList= Collections.emptyList();

    @ApiModelProperty(value = "卖方席位详情")
    private List<List<StockBrokerDetailDto>> sellDetailList = Collections.emptyList();
}
