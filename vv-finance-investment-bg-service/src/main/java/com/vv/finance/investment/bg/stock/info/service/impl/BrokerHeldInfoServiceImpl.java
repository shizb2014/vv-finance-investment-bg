package com.vv.finance.investment.bg.stock.info.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.stock.info.BrokerHeldInfo;
import com.vv.finance.investment.bg.stock.info.BrokerStatistics;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerHeldInfoMapper;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerStatisticsMapper;
import com.vv.finance.investment.bg.stock.info.service.BrokerHeldInfoService;
import com.vv.finance.investment.bg.stock.info.service.BrokerStatisticsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 股票码表 服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
@Service
@DS("db1")
public class BrokerHeldInfoServiceImpl extends ServiceImpl<BrokerHeldInfoMapper, BrokerHeldInfo> implements BrokerHeldInfoService {

}
