package com.vv.finance.investment.bg.stock.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.broker.allBroker.IndustryMarketValue;
import com.vv.finance.investment.bg.entity.broker.allBroker.MarketValueTrend;
import com.vv.finance.investment.bg.entity.broker.allBroker.StockMarketValue;
import com.vv.finance.investment.bg.stock.info.BrokerStatistics;
import org.apache.ibatis.annotations.*;
import org.springframework.security.core.parameters.P;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 股票码表 Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
public interface BrokerStatisticsMapper extends BaseMapper<BrokerStatistics> {

    @Select(" select seccode,f001d,f002v,f003n,f004n from t_broker_statistics where seccode = #{stockCode}")
    BrokerStatistics queryBrokerStatistics(@Param("stockCode") String stockCode);


    @Update({
            "update t_broker_statistics set F014V = #{industry}, end_price = #{close}, end_price_org = #{close},f003n_org = F003N, market_val = F003N * #{close}, Modified_Date = now() where SECCODE = #{stockCode} and F001D = #{f001d}"
    })
    int updateIndustryHis(@Param("stockCode") String stockCode, @Param("industry") String industry, @Param("f001d") Long f001d, @Param("close") BigDecimal close);

    int batchSaveOrUpdate(List<BrokerStatistics> brokerStatistics);

    int batchSave(List<BrokerStatistics> brokerStatistics);

    @Select("select F002V, F004N from ( select F002V, SUM(F004N) as F004N from t_broker_statistics where SECCODE = #{stockCode} AND F001D >= #{f001d} group by F002V) a order by F004N desc limit 5")
    List<BrokerStatistics> queryBrokerTop5(@Param("stockCode") String stockCode, @Param("f001d") Long f001d);

    @Select("select SECCODE, SUM(F004N) as F004N from ( select SECCODE, F004N from t_broker_statistics where SECCODE = #{stockCode} and f001d = #{f001d} order by F004N desc limit 5) a group by SECCODE")
    BrokerStatistics queryBrokerTop5Change(@Param("stockCode") String stockCode, @Param("f001d") Long f001d);

    @Select("select SECCODE, SUM(F004N) as F004N from ( select SECCODE, F004N from t_broker_statistics where SECCODE = #{stockCode} and f001d = #{f001d} order by F004N desc limit 10) a group by SECCODE")
    BrokerStatistics queryBrokerTop10Change(@Param("stockCode") String stockCode, @Param("f001d") Long f001d);


    @Select("select SECCODE, SUM(F004N) as F004N from ( select SECCODE, F004N from t_broker_statistics where SECCODE = #{stockCode} and f001d = #{f001d} order by F004N desc limit 20) a group by SECCODE")
    BrokerStatistics queryBrokerTop20Change(@Param("stockCode") String stockCode, @Param("f001d") Long f001d);



    List<Long> getBrokerHoldingShareList(@Param("f001d") Long f001d,@Param("f002v") List<String> f002v);

    Long getBrokerHoldingShare(@Param("f001d") Long f001d,@Param("f002v") String f002v);

    int updateBrokerStatisticsMarketVal(List<BrokerStatistics> brokerStatistics);

    @Select("select f001d,f002v ,SUM(F003N) as F003N from t_broker_statistics where f002v = #{f002v} and f001d = #{f001d}")
    BrokerStatistics queryBrokerMakVal(@Param("f001d") Long f001d,@Param("f002v") String f002v);

    List<BrokerStatistics> getBrokerStatisticsByDateAndId(@Param("dateList")List<Long> dateList,@Param("f002v") String f002v,@Param("codeSet") Set<String> codeSet);

    @Update({
            "update t_broker_statistics set end_price = end_price * #{factor},f003n = f003n / #{factor}, Modified_Date = now() where SECCODE = #{stockCode} and F001D = #{f001d}"
    })
    int updateBrokerStockEvent(@Param("stockCode") String stockCode, @Param("factor") BigDecimal factor, @Param("f001d") Long f001d, @Param("close") BigDecimal close);

    int updateBrokerStockEventV2(@Param("stockCode") String stockCode, @Param("factor") BigDecimal factor, List<Long> list);

    @Update({
            "update t_broker_statistics set end_price = end_price_org,f003n = f003n_org, Modified_Date = now() where SECCODE = #{stockCode}"
    })
    int updateBrokerStockEventRollBack(@Param("stockCode") String stockCode);

    List<BrokerStatistics> getIndustryList(@Param("F001D") Long F001D,@Param("F002V") String F002V,@Param("quitCodeList")List<String> quitCodeList);

    @Delete("delete from t_broker_statistics where SECCODE = #{stockCode} and  f001d < #{f001d}")
    int deleteHisBrokerStock(@Param("stockCode") String stockCode,@Param("f001d") Long f001d);

    @Select("select seccode,f014v from t_broker_statistics where F002V=#{brokerId} and F001D=#{f001d}")
    List<BrokerStatistics> selectCodeList(@Param("brokerId") String brokerId,@Param("f001d") Long f001d);

    @Select("select f002v from t_broker_statistics where SECCODE = #{code} and  f001d = #{f001d}")
    List<String> selectBrokerIdList(@Param("code") String code,@Param("f001d") Long f001d);

    List<String> getIndustryMarketValueTop10(@Param("brokerId") String brokerId,@Param("f001d") Long f001d,@Param("list") Set<String> codeSet);

    List<IndustryMarketValue> getIndustryMarketValueByf001d(@Param("brokerId") String brokerId, @Param("f001d") List<Long> f001d, @Param("list") Set<String> codeSet, @Param("industry")List<String> industryNameList);

//    List<BrokerStatistics> getBrokerBydates(@Param("f001d")Long date,@Param("list") List<String> list);

//    List<BrokerStatistics> getBrokerByIndustry(@Param("f001d")Long date,@Param("list") List<String> list);
}
