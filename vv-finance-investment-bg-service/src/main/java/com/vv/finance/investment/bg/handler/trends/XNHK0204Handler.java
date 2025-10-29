package com.vv.finance.investment.bg.handler.trends;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0201;
import com.vv.finance.investment.bg.entity.uts.Xnhk0204;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0204Mapper;
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
//@CanalTable("XNHK0204")
@Slf4j
public class XNHK0204Handler extends AbstractTrendsHandler implements EntryHandler<Xnhk0204> {

    @Autowired
    Xnhk0204Mapper xnhk0204Mapper;

    @Override
    public void sync() {
        LambdaQueryWrapper<Xnhk0204> wrapper = new LambdaQueryWrapper<Xnhk0204>().isNotNull(Xnhk0204::getSeccode);
        Long count = syncHelp(xnhk0204Mapper, wrapper, CompanyTrendsFactory::fromXnhk0204);
        log.info("XNHK0204Handler.sync 同步数据 {} 条", count);
    }

    @Override
    public void syncRecent(Integer minusDays) {
        deleteRecentData(CompanyTrendsType.ELEVEN.getCode(), CompanyTrendsSubType.FINANCIAL_REPORT_2.getCode(), minusDays);
        LambdaQueryWrapper<Xnhk0204> wrapper = new LambdaQueryWrapper<Xnhk0204>().isNotNull(Xnhk0204::getSeccode)
                .gt(Xnhk0204::getF001d, LocalDate.now().minusDays(minusDays));
        Long count = syncHelp(xnhk0204Mapper, wrapper, CompanyTrendsFactory::fromXnhk0204);
        log.info("XNHK0204Handler.syncRecent 同步最近数据 {} 条", count);
    }

    @Override
    public void insert(Xnhk0204 xnhk0204) {
        log.info("==== xnhk0204   insert");
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk0204(xnhk0204);
        save(companyTrendsMergeEntity);
    }

    @Override
    public void update(Xnhk0204 before, Xnhk0204 after) {
        log.info("==== XNHK0204   update   " + after.getXdbmask());
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk0204(after);
        companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
    }

    @Override
    public void delete(Xnhk0204 xnhk0204) {
        log.info("==== XNHK0204   delete");
        if (xnhk0204.getSeccode() == null || xnhk0204.getF002d() == null || xnhk0204.getF006v() == null) {
            return;
        }
        remove(CompanyTrendsType.ELEVEN.getCode(), getMD5(String.valueOf(CompanyTrendsType.ELEVEN.getCode()),
                CompanyTrendsSubType.FINANCIAL_REPORT_2.getCode(),
                xnhk0204.getSeccode(),
                xnhk0204.getF002d(),
                xnhk0204.getF006v()));
    }
}
