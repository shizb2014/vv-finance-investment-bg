package com.vv.finance.investment.bg.stock.kline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.kline.Stock2hourBackwardKline;
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
public interface Stock2hourBackwardKlineMapper extends BaseMapper<Stock2hourBackwardKline> {
    int batchInsert(List<Stock2hourBackwardKline> list);

}
