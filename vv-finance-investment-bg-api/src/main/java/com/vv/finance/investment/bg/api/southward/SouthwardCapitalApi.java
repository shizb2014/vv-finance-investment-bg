package com.vv.finance.investment.bg.api.southward;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.dto.ComScCapitalListResp;
import com.vv.finance.common.dto.ComScCapitalResp;
import com.vv.finance.common.dto.ComScNetInListResp;
import com.vv.finance.common.dto.ComScNetInTrendResp;
import com.vv.finance.investment.bg.dto.StockCodeNameBaseDTO;
import com.vv.finance.investment.bg.dto.southward.req.SouthwardCapitalTrendReq;
import com.vv.finance.investment.bg.dto.southward.resp.SouthwardCapitalResp;
import com.vv.finance.investment.bg.dto.southward.resp.SouthwardCapitalStockDetailResp;
import com.vv.finance.investment.bg.dto.southward.resp.SouthwardCapitalTrendResp;
import com.vv.finance.investment.bg.dto.stock.SouthwardCapitalStockResp;

import java.util.List;
import java.util.Set;

/**
 * @author qinxi
 * @date 2023/6/25 11:37
 * @description: 南向资金api
 */
public interface SouthwardCapitalApi {


    /**
     * 查询个股南向资金详情列表相关的股票
     *
     * @param sort    desc-降序 asc-升序
     * @param sortKey 对象对应key
     * @return 股票code列表
     */
    ResultT<List<StockCodeNameBaseDTO>> querySouthwardCapitalStockList(String sort, String sortKey);

    /**
     * 查询个股南向资金详情列表
     *
     * @param codeList 股票code列表 空则查询所有港股通股票的南向资金数据
     * @return 南向资金详情列表
     */
    ResultT<List<SouthwardCapitalStockDetailResp>> querySouthwardCapitalStockDetail(List<String> codeList);

    /**
     * 查询南向资金列表
     *
     * @return {@link ResultT }<{@link List }<{@link SouthwardCapitalResp }>>
     */
    ResultT<List<SouthwardCapitalResp>> querySouthwardCapitalList();

    /**
     * 查询南向资金列表
     * @return
     */
    ResultT<List<ComScCapitalResp>> querySouthwardCapitalList2();



    /**
     * 查询南向资金趋势图
     */
    ResultT<SouthwardCapitalTrendResp> querySouthwardCapitalTrend(SouthwardCapitalTrendReq req);


    /**
     * 南向资金股票模糊查询
     */
    ResultT<List<SouthwardCapitalStockResp>> querySouthwardCapitalStock(String stockCode);

    /**
     * 查询南向资金净流入趋势图
     *
     * @param stockId 库存 ID
     * @return {@link ResultT }<{@link ComScNetInTrendResp }>
     */
    ResultT<ComScNetInTrendResp> querySouthwardCapitalNetInTrend(Long stockId);

    /**
     * 查询南向资金资金净流入列表
     *
     * @param stockId 库存 ID
     * @return {@link ResultT }<{@link ComScNetInListResp }>
     */
    ResultT<ComScNetInListResp> querySouthwardCapitalNetInList(Long stockId);

    /**
     * 创建经纪商数据
     *
     * @param stockCode 代码
     */
    void createSouthwardDataByCode(String stockCode);

    /**
     * 删除经纪商数据
     *
     * @param stockCode 代码
     */
    void deleteSouthwardDataByCode(String stockCode);

    /**
     * 更新经纪商数据
     *
     * @param oldStockCode 老股票股票代码
     * @param newStockCode 新增功能股票股票代码
     */
    void updateSouthwardDataByCode(String oldStockCode, String newStockCode);
}
