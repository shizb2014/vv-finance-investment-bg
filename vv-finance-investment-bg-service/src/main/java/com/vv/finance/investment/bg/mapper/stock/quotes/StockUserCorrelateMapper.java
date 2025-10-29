package com.vv.finance.investment.bg.mapper.stock.quotes;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.info.StockSelfChoose;

import java.util.List;

/**
 * @author chenyu
 * @date 2020/10/27 17:58
 */
public interface StockUserCorrelateMapper extends BaseMapper<StockSelfChoose> {

    /**
     * 获取自选股
     * @param userId
     * @return
     */
    List<StockSelfChoose> getStockSelfChoose(String userId);

    boolean batchInsert(List<StockSelfChoose> list);

}