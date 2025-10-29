package com.vv.finance.investment.bg.api.broker;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.calc.hk.entity.StockKline;
import com.vv.finance.investment.bg.dto.broker.*;
import com.vv.finance.investment.bg.entity.broker.allBroker.BrokerSearch;
import com.vv.finance.investment.bg.entity.broker.appBroker.BrokersHold;
import com.vv.finance.investment.bg.entity.uts.Xnhk0102;
import com.vv.finance.investment.bg.stock.info.BrokerHeldInfo;
import com.vv.finance.investment.bg.stock.info.BrokerIndustryStatistics;
import com.vv.finance.investment.bg.stock.info.BrokerMarketValueStatistics;
import com.vv.finance.investment.bg.stock.info.BrokerStatistics;
import com.vv.finance.investment.bg.stock.info.entity.StockTrade;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Auther: shizhibiao
 * @Date: 2022/10/8
 * @Description: com.vv.finance.investment.bg.api.broker
 * @version: 1.0
 */
public interface BrokerAnalysisApi {

    /**
     * 经纪商净买卖前十
     * 今日：type = 0; 近五日：type = 1; 近十日：type =;近二十日：type=3; 近六十日：type = 4
     * @param code
     * @param type
     * @return
     */
    ResultT<NetBuyAndSellResp> getNetTradeBroker(String code, int type);



    /**
     * 个股主页-经纪商持股比例（APP端、PC端）
     * @param brokersProportionResp
     * @return
     */
    ResultT<BrokersProportionResp> getAppBrokersProportion(String code, BrokersProportionResp brokersProportionResp);


    /**
     * 经纪商持股榜单
     * @param code
     * @param type
     * @param sortKey
     * @param sort
     * @param pageSize
     * @param currentPage
     * @return
     */
    ResultT<PageDomain<BrokersDetail>> getBrokerHoldingList(String code,Integer type,String sortKey,String sort,Integer pageSize,Integer currentPage);

    /**
     * 收盘之后，更新上一天持有量,通过当日净买卖数据将当日持有量数据落库
     * @param code
     * @param date
     * @return
     */
    void updateNetTradeBrokerJob(String code, LocalDate date);

    /**
     * 每天先删除当天的redis缓存，并且重新计算第二天的redis
     * @param code
     * @param date
     * @return
     */
    void updateRedisBrokerJob(String code, LocalDate date);

    /**
     * 1、将经纪商id和名称关系维护一份到redis，使用时方便
     * 2、将行业和股票关系维护一份到redis，使用时方便
     * @param
     * @return
     */
    void updateBrokerInfoJob();

    /**
     * 指定日期数据落库
     * @param code
     * @return
     */
    void updateNetTradeBrokerForDateJob(String code, Long date, Map<String, Xnhk0102> xnhk0102Map);

    /**
     * 经纪商维度计算每天的市值(经纪商维度)
     * @param date
     * @param codes
     */
    void insertBrokersMakValStatistics(Long date, List<String> codes);


    /**
     * 获得数据最新更新时间
     * @return
     */
    DateResp getDateResp();

    /**
     * 计算除权事件
     */
    void updateBrokerStockEventV2(String code, BigDecimal factor, List<Long> date);

    /**
     * 删除两年前的数据
     */
    void deleteHisBrokerStock(String code, Long date);

    /**
     * 删除两年前的数据
     */
    void deleteHisBrokerStockMarket(Long date);

    /**
     * 搜索对股票有持仓的经纪商信息
     * @param code
     * @param brokerIdOrName
     * @return
     */

    List<BrokerSearch> getBrokerInformation(String code,String brokerIdOrName);

    /**
     * 更新缓存时间
     * @return
     */
    void updateBrokerDatetime();

    /**
     * 计算经纪商今日数据
     */
    void updateBrokerStatisticsJob(String code,Xnhk0102 xnhk0102);

    /**
     * 批量增加
     *
     * @param brokerHeldInfos
     * @return
     */
    Boolean saveBatch(List<BrokerHeldInfo> brokerHeldInfos);

    void updateBrokerInfoEventV2(String code, BigDecimal factor, List<Long> date);

    /**
     * 按照传参获取时间区间
     * @param num   近num个交易日
     * @param startDate 起始日期
     * @return
     */
    LocalDate getStartDate(Integer num,LocalDate startDate);

    void updateIndustryStatisticsBatch(Long date, List<String> codes);

    /**
     * 根据股票code删除经纪商股票数据
     */
    void delByStockCode(String stockCode);

    /**
     * 更新行业-经纪商持股市值走势图
     */
    void updateIndustryBrokersProportionJob(Map<String, List<StockKline>> industryKlineMap);

    /**
     * 获取行业-经纪商持股市值走势图
     *
     * @param industryCode 行业code
     * @return
     */
    IndustryBrokersProportionDTO getIndustryBrokersProportion(String industryCode);

    /**
     * 创建经纪商数据
     *
     * @param stockCode 代码
     */
    void createBrokerDataByCode(String stockCode);

    /**
     * 删除经纪商数据
     *
     * @param stockCode 代码
     */
    void deleteBrokerDataByCode(String stockCode);

    /**
     * 更新经纪商数据
     *
     * @param oldStockCode 老股票股票代码
     * @param newStockCode 新增功能股票股票代码
     */
    void updateBroker0DataByCode(String oldStockCode, String newStockCode);

    String getBrokerName(String brokerId);

    Xnhk0102 getXnhk0102(String code);

    List<BrokerStatistics> getBrokerStatisticsLimit60(String code, String brokerId);
}
