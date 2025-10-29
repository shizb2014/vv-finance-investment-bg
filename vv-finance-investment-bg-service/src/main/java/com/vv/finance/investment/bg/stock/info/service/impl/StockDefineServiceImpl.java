package com.vv.finance.investment.bg.stock.info.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

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
public class StockDefineServiceImpl extends ServiceImpl<StockDefineMapper, StockDefine> implements IStockDefineService {

    @Resource
    private StockDefineMapper stockDefineMapper;

    @Override
    public List<StockDefine> listStockColumns(List<String> columns) {
        return listColumnsByType(columns, StockTypeEnum.STOCK.getCode());
    }

    @Override
    public List<StockDefine> listStockByCodes(List<String> codes) {
        return listDefinesByCodeType(codes, StockTypeEnum.STOCK.getCode());
    }

    @Override
    public List<StockDefine> listColumnsByType(List<String> columns, Integer stockType) {
        String[] array = ArrayUtil.toArray(CollUtil.defaultIfEmpty(columns, Collections.emptyList()), String.class);
        return stockDefineMapper.selectList(new QueryWrapper<StockDefine>().select(array).eq(ObjectUtil.isNotEmpty(stockType), "stock_type", stockType));
    }

    @Override
    public List<StockDefine> listDefinesByCodeType(List<String> codes, Integer stockType) {
        QueryWrapper<StockDefine> wrapper = new QueryWrapper<StockDefine>().in(CollUtil.isNotEmpty(codes), "code", codes).eq(ObjectUtil.isNotEmpty(stockType), "stock_type", stockType);
        return stockDefineMapper.selectList(wrapper);
    }

}
