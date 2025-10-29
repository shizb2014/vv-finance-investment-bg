package com.vv.finance.investment.bg.mapper.southward;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.southward.SouthwardCapitalStatistics;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 南向资金每日统计 Mapper 接口
 * </p>
 *
 * @author qinxi
 * @since 2023-08-22
 */
public interface SouthwardCapitalStatisticsMapper extends BaseMapper<SouthwardCapitalStatistics> {

    int saveOrUpdateBatch(@Param("entities") List<SouthwardCapitalStatistics> list);

    List<SouthwardCapitalStatistics> selectByMarket(@Param("market") String market, @Param("limit") int limit);
}
