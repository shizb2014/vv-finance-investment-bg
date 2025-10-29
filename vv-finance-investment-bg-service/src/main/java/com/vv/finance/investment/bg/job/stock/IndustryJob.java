package com.vv.finance.investment.bg.job.stock;

import cn.hutool.core.util.ArrayUtil;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.utils.BeanCopyUtil;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.entity.industry.IndustryDailyKline;
import com.vv.finance.investment.bg.industry.service.IIndustryDailyKlineService;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author qinxi
 * @date 2024/6/24 14:36
 * @description:
 */
@Slf4j
@Component
public class IndustryJob {

    @Resource
    private IIndustryDailyKlineService industryDailyKlineService;

    @Resource
    private IIndustrySubsidiaryService industrySubsidiaryService;

    @Resource
    private StockService stockService;

    @Resource
    private HkTradingCalendarApi hkTradingCalendarApi;

    @XxlJob(value = "saveIndustryDailyKline", author = "秦禧", desc = "保存行业历史信息", cron = "0 0 17 ? * 1,2,3,4,5,6 *")
    public ReturnT<String> saveIndustryDailyKline(String param) {
        log.info("保存行业历史信息job 开始执行");
        if (!hkTradingCalendarApi.isTradingDay(LocalDate.now())) {
            log.info("保存行业历史信息job 非交易日，不处理");
            return ReturnT.SUCCESS;
        }
        long start = System.currentTimeMillis();
        List<IndustrySubsidiary> allIndustry = industrySubsidiaryService.getAllIndustry();
        Set<String> codes = allIndustry.stream().map(IndustrySubsidiary::getCode).collect(Collectors.toSet());
        List<StockSnapshot> snapshotList = stockService.getSnapshotList(ArrayUtil.toArray(codes, String.class));
        LocalDate date = hkTradingCalendarApi.getTradingCalendar(LocalDate.now()).getDate();
        List<IndustryDailyKline> saveList = BeanCopyUtil.copyListProperties(snapshotList, IndustryDailyKline::new, (s, t) -> {
            t.setAmount(s.getTurnover());
            t.setVolume(s.getSharesTraded());
            t.setTotalMarket(s.getTotalValue());
            t.setTime(LocalDateTimeUtil.getTimestamp(date));
            t.setTimeStr(LocalDateTime.of(date, LocalTime.MIN));
            t.setCreateTime(LocalDateTime.now());
            t.setUpdateTime(LocalDateTime.now());
        });
        industryDailyKlineService.saveOrUpdateBatch(saveList);
        log.info("保存行业历史信息job 结束，saveList.size:{} 耗时:{}ms", saveList.size(), System.currentTimeMillis() - start);
        return ReturnT.SUCCESS;
    }


}
