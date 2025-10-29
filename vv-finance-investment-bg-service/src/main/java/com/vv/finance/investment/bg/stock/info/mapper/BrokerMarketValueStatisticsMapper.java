package com.vv.finance.investment.bg.stock.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.info.BrokerMarketValueStatistics;
import com.vv.finance.investment.bg.stock.info.BrokerStatistics;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 股票码表 Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
public interface BrokerMarketValueStatisticsMapper extends BaseMapper<BrokerMarketValueStatistics> {

    @Delete("delete from t_broker_market_value_statistics where f001d < #{f001d}")
    int deleteHisBrokerStockMarket(@Param("f001d") Long f001d);

    int insertBrokersMakValStatistics(List<BrokerMarketValueStatistics> list);

}
