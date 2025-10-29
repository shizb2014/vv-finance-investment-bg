package com.vv.finance.investment.bg.stock.information.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.mapper.stock.quotes.StockNewsMapper;
import com.vv.finance.investment.bg.stock.information.service.IStockNewsService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2021-09-15
 */
@Service
public class StockNewsServiceImpl extends ServiceImpl<StockNewsMapper, StockNewsEntity> implements IStockNewsService {

}
