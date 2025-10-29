package com.vv.finance.investment.bg.api.broker;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.investment.bg.entity.broker.allBroker.*;

import java.time.LocalDate;
import java.util.List;

public interface BrokerViewApi {

    /**
     * 根据经纪商编号和名称模糊查询经纪商
     * @param codeOrName
     * @return
     */
    public List<BrokerSearch> getBrokerIdAndName(String codeOrName);

    /**
     * 根据股票代码和名称模糊查询股票
     * @param codeOrName
     * @return
     */
    public List<StockSearch> getCodeAndName(String codeOrName);

    /**
     * 根据行业代码和名称模糊查询行业
     * @param codeOrName
     * @return
     */
    public List<IndustrySearch> getIndustryCodeAndName(String codeOrName);

    /**
     * 根据股票code查询行业
     * @param code
     * @return
     */
    public IndustrySearch getIndustryByCode(String code);

    /**
     * 获取持股比例列表
     * @param brokerId
     * @param industryCode
     * @param code
     * @param sortKey
     * @param sort
     * @param startDate
     * @param endDate
     * @return
     */
    public List<ShareholdingsTable> getShareholdingsTable(String brokerId, String industryCode,
                                                                String code, String sortKey, String sort,
                                                                LocalDate startDate, LocalDate endDate);

    /**
     * 获得经纪商视角排行前5股票持股比例
     * @param brokerId
     * @param industryCode
     * @param code
     * @param endDate
     * @return
     */
    public BrokerViewRank getRankTopFiveList(String brokerId, String industryCode,
                                                         String code, LocalDate startDate, LocalDate endDate);

    /**
     * 获得股票持股比例
     * @param brokerId
     * @param code
     * @param startDate
     * @param endDate
     * @return
     */
    public StockAndHoldingRatio getHoldingRatioTrend(String brokerId,String code,LocalDate startDate, LocalDate endDate,Boolean isDaily);

    /**
     * 获得股票持股市值
     * @param brokerId
     * @param code
     * @param startDate
     * @param endDate
     * @return
     */
    public StockAndHoldingRatio getStockMarketValue(String brokerId,String code,LocalDate startDate, LocalDate endDate,Boolean isDaily);

    /**
     * 获得行业持股市值走势图
     * @param brokerId
     * @param industryCode
     * @param code
     * @param startDate
     * @param endDate
     * @return
     */
    public List<MarketValueTrend> getMarketValueTrend(String brokerId,String industryCode,
                                                            String code,LocalDate startDate, LocalDate endDate);
}
