package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Author: wsliang
 * @Date: 2021/9/13 14:05
 **/
@Data
@ToString
//@Builder
@ApiModel("个股资讯详情")
public class StockNewsDetailVo extends StockNewsVo {
    private static final long serialVersionUID = -7836301330386356632L;

    @ApiModelProperty("资讯详情文本")
    private String content;

    @ApiModelProperty("股票信息")
    private List<SimpleStockVo> simpleStockVos;
}
