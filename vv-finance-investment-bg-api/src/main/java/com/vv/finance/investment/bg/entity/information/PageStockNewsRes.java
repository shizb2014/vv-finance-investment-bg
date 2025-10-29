package com.vv.finance.investment.bg.entity.information;

import com.vv.finance.base.domain.PageDomain;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Author: wsliang
 * @Date: 2021/9/14 16:36
 **/
@Data
@ToString
//@Builder
@ApiModel("分页查询个股资讯信息，包含股票信息")
public class PageStockNewsRes implements Serializable {

    private static final long serialVersionUID = 1459664871963546325L;

    private SimpleStockVo simpleStockVo;

    private PageDomain<StockNewsVo> stockNewsPage;


}
