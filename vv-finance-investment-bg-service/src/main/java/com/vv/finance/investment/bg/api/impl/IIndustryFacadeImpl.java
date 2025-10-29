package com.vv.finance.investment.bg.api.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.api.IIndustryFacade;
import com.vv.finance.investment.bg.entity.industry.IndustryDailyKline;
import com.vv.finance.investment.bg.industry.service.IIndustryDailyKlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * @author hamilton
 * @date 2021/12/29 11:53
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
@RequiredArgsConstructor
public class IIndustryFacadeImpl implements IIndustryFacade {
    private final IIndustryDailyKlineService industryDailyKlineService;
    @Override
    public ResultT<Void> saveDailyKline(List<IndustryDailyKline> dailyKlines) {
        industryDailyKlineService.saveOrUpdateBatch(dailyKlines);
        return ResultT.success();
    }


}
