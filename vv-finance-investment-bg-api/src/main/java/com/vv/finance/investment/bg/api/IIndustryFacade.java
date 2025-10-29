package com.vv.finance.investment.bg.api;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.entity.industry.IndustryDailyKline;

import java.util.List;

/**
 * @author hamilton
 * @date 2021/12/29 11:51
 */
public interface IIndustryFacade {


    ResultT<Void> saveDailyKline(List<IndustryDailyKline> dailyKlines);
}
