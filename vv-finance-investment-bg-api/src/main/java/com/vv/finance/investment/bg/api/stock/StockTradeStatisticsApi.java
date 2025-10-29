package com.vv.finance.investment.bg.api.stock;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.dto.info.TodayCapitalTotalDto;
import com.vv.finance.investment.bg.dto.info.TotalCapitalInflowsDTO;
import com.vv.finance.investment.bg.dto.info.TradeStatisticsDto;
import com.vv.finance.investment.bg.entity.trade.TradeStatistics;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wsliang
 * @date 2021/12/25 14:22
 **/
public interface StockTradeStatisticsApi {


    /**
     * 资金分布落库
     *
     * @param tradeStatistics
     * @return
     */
    Boolean saveBatch(List<TradeStatisticsDto> tradeStatistics);

    /**
     * 资金分布落库
     *
     * @param tradeStatistics
     * @return
     */
    Boolean saveBatchV2(List<TradeStatistics> tradeStatistics);

    /**
     * 查询最新的六十条数据
     *
     * @param stockCode
     * @return
     */
    List<TradeStatistics> listSixty(String stockCode);

    /**
     * 按日期查询最新的数据
     *
     * @param stockCode
     * @return
     */
    List<TradeStatistics> listTradeStatistics(String stockCode, LocalDate date);

    /**
     * 获取资金分布（含今日实时数据）
     *
     * @param stockCode
     * @param isTradingDay
     * @return
     */
    List<TradeStatistics> listTradeStatisticsRealTime(String stockCode, boolean isTradingDay, Boolean isWarrant, LocalDate date, Integer todayFlag);

    /**
     * 获取60日资金分布（含今日实时数据）
     *
     * @param stockCode
     * @param isTradingDay
     * @return
     */
    List<TradeStatistics> listSixtyRealTime(String stockCode, boolean isTradingDay, Boolean isWarrant);

    /**
     * 获取资金分布（含今日实时数据）
     *
     * @param stockCode
     * @param isTradingDay
     * @return
     */
    List<TradeStatistics> listStaticTradeStatistics(String stockCode, boolean isTradingDay, Long startTime, Long endTime);

    /**
     * 获取今日的所有的资金分布数据
     *
     * @param isWarrant
     * @return
     */
    Map<String, TradeStatisticsDto> getAllTradeStatics(Boolean isWarrant);

    /**
     * dto转实体
     *
     * @param dto
     * @return
     */
    TradeStatistics buildFromDto(TradeStatisticsDto dto);

    /**
     * 查询最新的六十条数据
     *
     * @param stockCode
     * @return
     */
    ResultT<List<TradeStatistics>> listSixty4Dubbo(String stockCode);

    /**
     * 从redis里面获取 累计资金流入
     *
     * @param date
     * @return
     */
    Map<String, TotalCapitalInflowsDTO> getTotalCapitalInflowsFromRedis(LocalDate date, Boolean isWarrant);

    /**
     * 从redis获取当日累计资金流入
     *
     * @param stockCode
     * @param date
     * @return
     */
    Map<String, TodayCapitalTotalDto> getTodayTotalAmountFromRedis(String stockCode, LocalDate date, Boolean isWarrant);

    /**
     * 从redis获取当日累计资金流入
     *
     * @param date
     * @return
     */
    Map<String, TodayCapitalTotalDto> getAllTodayTotalAmountFromRedis(LocalDate date, Boolean isWarrant);
    /**
     * 获取热力图排行榜
     *
     * @param num   前几名
     * @param isAsc 是否是正序
     * @return
     */
    String getTradeCapitalNatTop(Integer num, Boolean isAsc);

    /**
     * 获取每日成交统计数量
     *
     * @param stockCodes
     * @return
     */
    Map<String, Long> queryPreviousTradeNum(Set<String> stockCodes);

    /**
     * 查询最近N条数据
     *
     * @param stockCode 股票代码
     * @param limit     条数
     * @return
     */
    List<TradeStatistics> listTradeStatistics(Collection<String> stockCodes, Integer limit);

    TradeStatisticsDto listTradeStatisticsByDate(String stockCode, LocalDate date);

    /**
     * 删除临时股票资金数据
     * 1、逐笔统计
     * 2、资金分布
     * 3、成交统计
     * 4、流入流出
     */
    void delTradeStatisticByStockCode(String stockCode);

    /**
     * 变更资金数据股票code
     * 1、逐笔统计
     * 2、资金分布
     * 3、成交统计
     * 4、流入流出
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    void updateTradeStatisticStockCode(String sourceCode, String targetCode);

    /**
     * 变更资金数据股票code
     * 1、逐笔统计
     * 2、资金分布
     * 3、成交统计
     * 4、流入流出
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    void copyTradeStatisticStockCode(String sourceCode, String targetCode,Boolean mockFlag);

}
