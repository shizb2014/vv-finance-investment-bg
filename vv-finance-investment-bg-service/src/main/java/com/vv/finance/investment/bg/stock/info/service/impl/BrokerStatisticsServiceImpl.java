package com.vv.finance.investment.bg.stock.info.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.info.BrokerStatistics;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerStatisticsMapper;
import com.vv.finance.investment.bg.stock.info.service.BrokerStatisticsService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 股票码表 服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
@Service
@DS("db1")
public class BrokerStatisticsServiceImpl extends ServiceImpl<BrokerStatisticsMapper, BrokerStatistics> implements BrokerStatisticsService {

}
