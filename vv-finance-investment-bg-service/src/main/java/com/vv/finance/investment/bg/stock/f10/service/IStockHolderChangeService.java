package com.vv.finance.investment.bg.stock.f10.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.entity.f10.shareholder.StockHolderChange;

/**
 * <p>
 * 股票码表 服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
public interface IStockHolderChangeService extends IService<StockHolderChange> {

    void updateStockHolderChangeByCode(String code);
}
