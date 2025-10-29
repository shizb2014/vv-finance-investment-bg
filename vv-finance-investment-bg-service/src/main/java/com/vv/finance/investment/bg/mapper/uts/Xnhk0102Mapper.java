package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.uts.Xnhk0102;

import java.util.List;

/**
 * @ClassName: Xnhk0102Mapper
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/11/10   17:50
 */
@DS("db2")
public interface Xnhk0102Mapper extends BaseMapper<Xnhk0102> {

    /**
     * 多表联查获取市场数据
     * @param stockCode
     * @return
     */
    Xnhk0102 queryMarketData(String stockCode);

    /**
     * 获取多个股票的市场数据
     * @param stockCodeList
     * @return
     */
    List<Xnhk0102> listMarketData(List<String> stockCodeList);

    /**
     * 多表联查获取市场数据
     * @param stockCode
     * @return
     */
    Xnhk0102 selectStockMarketData(String stockCode);
}