package com.vv.finance.investment.bg.stock.kline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.dto.stock.StockTrendFollowDTO;
import com.vv.finance.investment.bg.stock.kline.StockDailyKline;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-26
 */
@Deprecated
public interface StockDailyKlineMapper extends BaseMapper<StockDailyKline> {

    int batchInsert(List<StockKline> list);

    StockTrendFollowDTO getFollowDurationPrice(
        @Param("code") String code,
        @Param("trackRange") Integer trackRange
    );
}
