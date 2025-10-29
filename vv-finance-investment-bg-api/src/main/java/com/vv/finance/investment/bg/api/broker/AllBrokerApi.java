package com.vv.finance.investment.bg.api.broker;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.investment.bg.dto.broker.IndustryBrokerRankDTO;
import com.vv.finance.investment.bg.dto.broker.IndustryBrokerRankReq;
import com.vv.finance.investment.bg.entity.broker.allBroker.*;
import com.vv.finance.investment.bg.stock.info.BrokerMarketValueStatistics;

import java.time.LocalDate;
import java.util.List;

public interface AllBrokerApi {

    /**
     * 获得全部经纪商榜单
     * @param brokerIdOrName
     * @param sortKey
     * @param sort
     * @param startDate
     * @param endDate
     * @return
     */
    public List<AllBrokerRank> getAllBrokerRank(String brokerIdOrName, String sortKey, String sort,
                                                      LocalDate startDate, LocalDate endDate);


    /**
     * 获取行业维度经纪商榜单
     * @param req
     * @return
     */
    List<IndustryBrokerRankDTO> getIndustryBrokerRank(IndustryBrokerRankReq req);

    /**
     * 获得经纪商持股市值走势图
     * @param brokerId
     * @param current
     * @param size
     * @return
     */
    public List<BrokerMarketValueStatistics> getBrokerMarketValueTrend(String brokerId) ;

    /**
     * 获得经纪商持股市值走势图(APP)
     * @param brokerId
     * @param endDate
     * @param startDate
     * @return
     */
    public List<BrokerMarketValueStatistics> getBrokerMarketValueTrendApp(String brokerId);

    /**
     * APP端-全部经纪商-股票分布（分页返回）
     * @param brokerId
     * @return
     */
    public PageDomain<BrokerShareHoldingsByCode> getBrokerShareHoldingsByCode(String brokerId,String code,LocalDate startDate,LocalDate endDate,String sort,String sortKey,Integer currentPage,Integer pageSize);

    /**
     * APP端-全部经纪商-行业分布（分页返回）
     * @param brokerId
     * @return
     */
    public PageDomain<BrokerShareHoldingsByIndustry> getBrokerShareHoldingsByIndustry(String brokerId,String code,String industryCode,LocalDate startDate,LocalDate endDate,String sort,String sortKey,Integer currentPage,Integer pageSize);

    /**
     * PC端-全部经纪商-股票分布（无需分页返回）
     * @param brokerId
     * @param endDate
     * @param sort
     * @param sortKey
     * @return
     */
    public List<BrokerShareHoldingsByCode> getPCBrokerShareHoldingsByCode(String brokerId,LocalDate endDate,String sort,String sortKey);

    /**
     * PC端-全部经纪商-行业分布（无需分页返回）
     * @param brokerId
     * @param endDate
     * @param sort
     * @param sortKey
     * @return
     */
    public List<BrokerShareHoldingsByIndustry> getPCBrokersShareHoldingsByIndustry(String brokerId,LocalDate endDate,String sort,String sortKey);


}
