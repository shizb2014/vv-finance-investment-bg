package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.entity.uts.HkexTd;

/**
*   @ClassName:    HkexTdMapper
*   @Description:  交易日历
*   @Author:   Demon
*   @Datetime:    2020/12/22   10:43
*/
@DS("db2")
public interface HkexTdMapper extends BaseMapper<HkexTd> {
}