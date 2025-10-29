package com.vv.finance.investment.bg.mapper.stock.quotes;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.info.StockDefine;

import java.util.List;

/**
 * @ClassName: TStockDefineMapper
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/23   10:03
 */
public interface TStockDefineMapper extends BaseMapper<StockDefine> {
    List<String> listNoDataCode();
}