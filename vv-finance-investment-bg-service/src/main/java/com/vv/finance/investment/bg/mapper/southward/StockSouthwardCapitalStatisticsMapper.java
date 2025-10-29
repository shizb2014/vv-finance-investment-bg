package com.vv.finance.investment.bg.mapper.southward;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.southward.StockSouthwardCapitalStatistics;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 个股南向资金每日统计 Mapper 接口
 * </p>
 *
 * @author qinxi
 * @since 2023-06-26
 */
public interface StockSouthwardCapitalStatisticsMapper extends BaseMapper<StockSouthwardCapitalStatistics> {

    int saveOrUpdateBatch(@Param("entities") List<StockSouthwardCapitalStatistics> southwardCapitalStatistics);

    List<StockSouthwardCapitalStatistics> selectByStatisticDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<StockSouthwardCapitalStatistics> selectByStockCode(@Param("stockCode")String stockCode);
}
