package com.vv.finance.investment.bg.api.stock;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.investment.bg.dto.stock.StockTrendFollowDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hqj
 * @date 2020/10/28 11:02
 */
public interface StockDishApi {

//    /**
//     * 批量保存/更新  股票快照
//     *
//     * @param snapshotReqs
//     * @return
//     */
//    ResultT<Void> saveStockSnapshot(List<HistorySnapshotReq> snapshotReqs);

//    /**
//     * 批量保存/更新逐笔交易
//     *
//     * @param historyTradeReqs
//     * @return
//     */
//     ResultT<Void> saveTradeHistory(List<HistoryTradeReq> historyTradeReqs);
//
//    /**
//     * 批量保存/更新委托挂单
//     *
//     * @param historyOrderReqs
//     * @return
//     */
//     ResultT<Void> saveHistoryOrder(List<HistoryOrderReq> historyOrderReqs);
//
//    /**
//     * 保存/更新经济席位
//     *
//     * @param economyReqs
//     * @return
//     */
//     ResultT<Void> saveEconomy(List<EconomyReq> economyReqs);
//
//    /**
//     * 批量保存/更新经纪席位 receiver用
//     *
//     * @param stockEconomies
//     * @return
//     */
//     ResultT<Void> saveStockEconomys(List<StockEconomy> stockEconomies);
//
//    /**
//     * 批量保存/更新股票快照 receiver用
//     *
//     * @param stockSnapshots
//     * @return
//     */
//     ResultT<Void> saveStockSnapshots(List<StockSnapshot> stockSnapshots);
//
//    /**
//     * 批量保存/更新成交分布
//     *
//     * @param baseReqs
//     * @return
//     */
//     ResultT<Void> saveCapitalDistribution(List<BaseReq> baseReqs);
//
//    /**
//     * 批量保存/更新 资金流转
//     *
//     * @param baseReqs
//     * @return
//     */
//     ResultT<Void> saveCapitalFlow(List<BaseReq> baseReqs);
//
//    /**
//     * 更新  股票快照
//     *
//     * @param stockSnapshot
//     * @return
//     */
//     ResultT<Void> saveOrUpdateStockSnapshot(StockSnapshot stockSnapshot);
//
//    /**
//     * 更新  trade
//     *
//     * @param stockTrade
//     * @return
//     */
//     ResultT<Void> saveOrUpdateTradeHistory(StockTrade stockTrade);
//
//    /**
//     * 保存/更新委托挂单
//     *
//     * @param stockOrder
//     * @return
//     */
//     ResultT<Void> saveHistoryOrder(StockOrder stockOrder);
//
//    /**
//     * 保存/更新经济席位
//     *
//     * @param stockEconomy
//     * @return
//     */
//     ResultT<Void> saveEconomy(StockEconomy stockEconomy);
//
//    /**
//     * 保存/更新成交分布
//     *
//     * @param stockCapitalDistribution
//     * @return
//     */
//     ResultT<Void> saveCapitalDistribution(StockCapitalDistribution stockCapitalDistribution);
//
//    ResultT<StockCapitalDistribution> getStockCapitalDistribution(String code);
//
//    /**
//     * 保存/更新 资金流转
//     *
//     * @param stockCapitalFlow
//     * @return
//     */
//     ResultT<Void> saveCapitalFlow(StockCapitalFlow stockCapitalFlow);
//
//    /**
//     * 9.15分清除涨跌幅
//     *
//     * @param stockSnapshots 股票快照
//     * @return
//     */
//     ResultT<Void> cleanSnapshotChg(List<StockSnapshot> stockSnapshots);

    /**
     * 根据股票代码查询快照
     *
     * @param stockCode
     * @return
     */
    StockSnapshot queryStockSnapshot(String stockCode);

    /**
     * 根据股票代码获取最新股票对应最新价格
     *
     * @param code
     * @return
     */
    ResultT<BigDecimal> getLastPrice(String code);

    ResultT<Map<String, BigDecimal>> getLastPriceMap(Set<String> codes);

    ResultT<Map<String, BigDecimal>> getLastPriceMap();

    /**
     * 根据股票代码获取最新股票对应股票昨收价
     *
     * @param code
     * @return
     */
    ResultT<BigDecimal> getPreClose(String code);

}
