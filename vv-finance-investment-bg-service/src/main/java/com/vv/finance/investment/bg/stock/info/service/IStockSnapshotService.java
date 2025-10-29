package com.vv.finance.investment.bg.stock.info.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.common.entity.common.StockSnapshot;

import java.util.List;

/**
 * <p>
 * 股票快照 服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-28
 */
public interface IStockSnapshotService extends IService<StockSnapshot> {

    boolean batchSaveOrUpdate(List<StockSnapshot> snapshotList);
}
