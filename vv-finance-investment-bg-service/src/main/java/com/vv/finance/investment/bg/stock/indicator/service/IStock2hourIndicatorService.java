package com.vv.finance.investment.bg.stock.indicator.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.indicator.entity.Stock1hourIndicator;
import com.vv.finance.investment.bg.stock.indicator.entity.Stock2hourIndicator;

import java.util.Collection;

/**
 * <p>
 * 股票分指服务类
 * </p>
 *
 * @author hqj
 * @since 2020-10-28
 */
public interface IStock2hourIndicatorService extends IService<Stock2hourIndicator> {
    boolean insertOrUpdateBatch(Collection<Stock2hourIndicator> values);

}
