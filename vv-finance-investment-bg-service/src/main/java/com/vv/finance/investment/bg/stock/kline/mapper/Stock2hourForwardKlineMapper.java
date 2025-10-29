package com.vv.finance.investment.bg.stock.kline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.kline.Stock2hourForwardKline;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-26
 */
public interface Stock2hourForwardKlineMapper extends BaseMapper<Stock2hourForwardKline> {
    int batchInsert(List<Stock2hourForwardKline> list);

}
