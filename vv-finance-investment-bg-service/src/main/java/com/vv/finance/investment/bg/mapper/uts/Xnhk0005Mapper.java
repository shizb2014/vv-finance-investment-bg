package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.uts.Xnhk0005;
import com.vv.finance.investment.bg.stock.info.StockDefine;

import java.util.List;

/**
*   @ClassName:    Xnhk0005Mapper
*   @Description:  子行业
*   @Author:   Demon
*   @Datetime:    2020/12/22   10:45
*/
@DS("db2")
public interface Xnhk0005Mapper extends BaseMapper<Xnhk0005> {

    /**
     * 查询所有行业
     *
     * @return {@link List }<{@link StockDefine }>
     */
    List<StockDefine> listIndustryDefines();
}