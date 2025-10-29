package com.vv.finance.investment.bg.stock.rank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.rank.entity.StockRanking;
import com.vv.finance.investment.bg.stock.rank.mapper.StockRankingMapper;
import com.vv.finance.investment.bg.stock.rank.service.IStockRankingService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 股票排行榜 服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
@Service
public class StockRankingServiceImpl extends ServiceImpl<StockRankingMapper, StockRanking> implements IStockRankingService {

}
