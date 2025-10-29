package com.vv.finance.investment.bg.api.frontend.v2;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.bean.SimplePageResp;
import com.vv.finance.common.entity.common.CommonTradeCapital;
import com.vv.finance.common.entity.quotation.common.ComDDENetVo;
import com.vv.finance.common.entity.receiver.Order;
import com.vv.finance.investment.bg.dto.DdePolicyDto;
import com.vv.finance.investment.bg.dto.MoneyFlowDto;
import com.vv.finance.investment.bg.dto.info.*;
import com.vv.finance.investment.bg.entity.trade.TradeStatisticsDetail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author chenyu
 * @date 2021/3/17 11:19
 */
public interface StockServiceV2 {

    /**
     * 资金分布
     *
     * @param stockCode
     * @return
     */
    CapitalDistributionVo getCapitalDistribution(String stockCode);

    /**
     * 港股通
     *
     * @param direction
     * @return
     */
    ConnectTurnoverDTO getConnectTurnover(String direction);

    /**
     * 计算主力净流入
     * @param stockCode
     * @return
     */
    List<DDENetVo> listDDENet(String stockCode);

    /**
     * 获取主力资金分布
     *
     * @param stockCode 股票代码
     * @return ResultT
     */
    List<ComDDENetVo> listDDENetByTime(String stockCode, Long startTime, Long endTime);

    /**
     * 获取累计资金排名
     * @param code
     * @return
     */
    List<NetRankVo> getTotalNetRank(String code);

    SimplePageResp<DdePolicyDto> listDDePolicy(SimplePageReq simplePageReq);

    SimplePageResp<MoneyFlowDto> listMoneyFlow(SimplePageReq simplePageReq);
    /**
     * 该接口查询
     * @param stockCode
     * @return
     */
    List<TradeStatisticsDetail> listDDEByTime(String stockCode, long startTime, long endTime);


    /**
     * 1、交易日9点到9.30 显示i上一个交易日数据
     * 2、9.30之后像是当天数据
     */
    Order getStockOrder(String stockCode);

    List<Order> getStockOrderByCodes(List<String> codes, Integer number);


    /**
     * 计算主力资金近一年（量化）
     * @param stockCode
     * @return
     */
    List<CommonTradeCapital> listDDEForQuant(String stockCode, LocalDate date, Integer todayFlag);

    Boolean saveOrUpdateBatchTradeStatic(List<TradeStatisticsDetail> tradeStatisticsDetails);

    Boolean delDdeBySizeCriterion(String stockCode, long time);
}
