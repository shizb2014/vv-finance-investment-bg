package com.vv.finance.investment.bg.stock.info.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.info.StockRelatedDetails;

import java.util.List;

/**
 * @ClassName IStockRelatedDetailsService
 * @Deacription 股票相关接口
 * @Author lh.sz
 * @Date 2021年12月27日 11:47
 **/
public interface IStockRelatedDetailsService extends IService<StockRelatedDetails> {
//    /**
//     * 修复股票详情
//     *
//     * @param stocks 股票
//     */
//    void repairStocksDetailSnapshot(List<String> stocks);

    /**
     * 修改快照的停复牌状态
     *
     * @param suspension 股票状态
     */
    void updateSnapshotSuspension(int suspension);
}
