package com.vv.finance.investment.bg.dto.stock;/*
 *@author     :chucai.xiong
 *@date       :2022/6/2414:55
 *@description:
 *@e-mail     :xiongchucai@vv.cn
 */

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StockCostQueryDTO implements Serializable {

    @ApiModelProperty(value = "分配的股票")
    private List<StockQueryDTO> costStock;
    @ApiModelProperty(value = "其他股票")
    private List<StockQueryDTO> otherStock;
}
