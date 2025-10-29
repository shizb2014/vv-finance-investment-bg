package com.vv.finance.investment.bg.dto.stock;/*
 *@author     :chucai.xiong
 *@date       :2022/6/2514:43
 *@description:
 *@e-mail     :xiongchucai@vv.cn
 */

import com.vv.finance.base.domain.PageDomain;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class StockQueryPageResp implements Serializable {
    @ApiModelProperty("是否有分配的股票")
    private Boolean isCostManager;
    @ApiModelProperty("分页参数")
    private PageDomain<StockQueryDTO> pageDomain;
}
