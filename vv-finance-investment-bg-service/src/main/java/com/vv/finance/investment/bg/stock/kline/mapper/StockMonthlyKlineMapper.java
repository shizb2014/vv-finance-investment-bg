package com.vv.finance.investment.bg.stock.kline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.req.KlineBatchQueryReq;
import com.vv.finance.investment.bg.stock.kline.StockMonthlyKline;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author hamilton
 * @since 2020-10-26
 */
public interface StockMonthlyKlineMapper extends BaseMapper<StockMonthlyKline> {


    boolean batchInsert(List<StockMonthlyKline> list);

    List<StockMonthlyKline> batchQuery(@Param("list") List<String> codes,@Param("num") Integer num);
}
