package com.vv.finance.investment.bg.handler.trends;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.entity.uts.Xnhk0201;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0201Mapper;
import com.vv.finance.investment.bg.stock.information.enun.CompanyTrendsSubType;
import com.vv.finance.investment.bg.stock.information.enun.CompanyTrendsType;
import com.vv.finance.investment.bg.stock.information.factory.CompanyTrendsFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.time.LocalDate;

import static com.vv.finance.investment.bg.stock.information.factory.CompanyTrendsFactory.getMD5;

/**
 * 监听财报
 *
 * @author lichao
 * @date 2023/9/20 11:00
 */
@RequiredArgsConstructor
@Component
//@CanalTable("XNHK0201")
@Slf4j
public class XNHK0201Handler extends AbstractTrendsHandler implements EntryHandler<Xnhk0201> {

    @Autowired
    Xnhk0201Mapper xnhk0201Mapper;

    @Override
    public void sync() {
        LambdaQueryWrapper<Xnhk0201> wrapper = new LambdaQueryWrapper<Xnhk0201>().isNotNull(Xnhk0201::getSeccode);
        Long count = syncHelp(xnhk0201Mapper, wrapper, CompanyTrendsFactory::fromXnhk0201);
        log.info("XNHK0201Handler.sync 同步数据 {} 条", count);
    }

    @Override
    public void syncRecent(Integer minusDays) {
        deleteRecentData(CompanyTrendsType.ELEVEN.getCode(), CompanyTrendsSubType.FINANCIAL_REPORT_1.getCode(), minusDays);
        LambdaQueryWrapper<Xnhk0201> wrapper = new LambdaQueryWrapper<Xnhk0201>()
                .isNotNull(Xnhk0201::getSeccode)
                .gt(Xnhk0201::getF001d, LocalDate.now().minusDays(minusDays));
        Long count = syncHelp(xnhk0201Mapper, wrapper, CompanyTrendsFactory::fromXnhk0201);
        log.info("XNHK0201Handler.syncRecent 同步最近数据 {} 条", count);
    }

    @Override
    public void insert(Xnhk0201 xnhk0201) {
        log.info("==== xnhk0201   insert");
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk0201(xnhk0201);
        save(companyTrendsMergeEntity);
    }

    @Override
    public void update(Xnhk0201 before, Xnhk0201 after) {
        log.info("==== XNHK0201   update   " + after.getXdbmask());
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk0201(after);
        companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
    }

    @Override
    public void delete(Xnhk0201 xnhk0201) {
        log.info("==== XNHK0201   delete");
        if (xnhk0201.getSeccode() == null || xnhk0201.getF002d() == null || xnhk0201.getF006v() == null) {
            return;
        }
        remove(CompanyTrendsType.ELEVEN.getCode(), getMD5(String.valueOf(CompanyTrendsType.ELEVEN.getCode()),
                CompanyTrendsSubType.FINANCIAL_REPORT_1.getCode(),
                xnhk0201.getSeccode(),
                xnhk0201.getF002d(),
                xnhk0201.getF006v()));
    }
}
