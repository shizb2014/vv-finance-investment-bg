package com.vv.finance.investment.bg.stock.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.info.BrokerIndustryStatistics;
import org.apache.ibatis.annotations.Param;

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
public interface BrokerIndustryStatisticsMapper extends BaseMapper<BrokerIndustryStatistics> {

    int deleteEarlyStatistics(@Param("f001d") Long f001d);

    /**
     * 获取前10行业
     * @param f001d
     * @return
     */
    List<String> getTopIndustry(@Param("brokerId") String brokerId,@Param("f001d") Long f001d);

    List<BrokerIndustryStatistics> getIndustryStatistics(@Param("brokerId") String brokerId,@Param("dateList") List<Long> dateList,@Param("industryList") List<String> industryList);

    int insertIndustryMakValStatistics(@Param("list") List<BrokerIndustryStatistics> list);

    List<BrokerIndustryStatistics> selectListByRangeDate(@Param("startDate")Long startDate);


}
