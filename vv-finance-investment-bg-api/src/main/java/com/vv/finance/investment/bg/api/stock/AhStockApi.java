package com.vv.finance.investment.bg.api.stock;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.dto.StockCodeNameBaseDTO;
import com.vv.finance.investment.bg.dto.stock.AhStockDetailResp;

import java.util.List;

/**
 * @Author:maling
 * @Date:2023/6/27
 * @Description:AH股票API接口
 */
public interface AhStockApi {

    /**
     * 查询AH股票代码列表
     */
    ResultT<List<StockCodeNameBaseDTO>> queryAhStockCodeList(String sort, String sortKey);

    /**
     * 查询AH股票列表
     */
    ResultT<List<AhStockDetailResp>> queryAhStockDetailList(List<String> ahStockCodelist);
}