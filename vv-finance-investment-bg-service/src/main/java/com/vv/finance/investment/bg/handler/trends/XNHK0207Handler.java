package com.vv.finance.investment.bg.handler.trends;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0204;
import com.vv.finance.investment.bg.entity.uts.Xnhk0207;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0207Mapper;
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
//@CanalTable("XNHK0207")
@Slf4j
public class XNHK0207Handler extends AbstractTrendsHandler implements EntryHandler<Xnhk0207> {


    @Autowired
    Xnhk0207Mapper xnhk0207Mapper;

    @Override
    public void sync() {
        LambdaQueryWrapper<Xnhk0207> wrapper = new LambdaQueryWrapper<Xnhk0207>().isNotNull(Xnhk0207::getSeccode);
        Long count = syncHelp(xnhk0207Mapper, wrapper, CompanyTrendsFactory::fromXnhk0207);
        log.info("XNHK0207Handler.sync 同步数据 {} 条", count);
    }

    @Override
    public void syncRecent(Integer minusDays) {
        deleteRecentData(CompanyTrendsType.ELEVEN.getCode(), CompanyTrendsSubType.FINANCIAL_REPORT_3.getCode(), minusDays);
        LambdaQueryWrapper<Xnhk0207> wrapper = new LambdaQueryWrapper<Xnhk0207>().isNotNull(Xnhk0207::getSeccode)
                .gt(Xnhk0207::getF001d, LocalDate.now().minusDays(minusDays));
        Long count = syncHelp(xnhk0207Mapper, wrapper, CompanyTrendsFactory::fromXnhk0207);
        log.info("XNHK0207Handler.syncRecent 同步最近数据 {} 条", count);
    }

    @Override
    public void insert(Xnhk0207 xnhk0207) {
        log.info("==== xnhk0207   insert");
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk0207(xnhk0207);
        save(companyTrendsMergeEntity);
    }

    @Override
    public void update(Xnhk0207 before, Xnhk0207 after) {
        log.info("==== XNHK0207   update   " + after.getXdbmask());
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk0207(after);
        companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
    }

    @Override
    public void delete(Xnhk0207 xnhk0207) {
        log.info("==== XNHK0207   delete");
        if (xnhk0207.getSeccode() == null || xnhk0207.getF002d() == null || xnhk0207.getF006v() == null) {
            return;
        }
        remove(CompanyTrendsType.ELEVEN.getCode(), getMD5(String.valueOf(CompanyTrendsType.ELEVEN.getCode()),
                CompanyTrendsSubType.FINANCIAL_REPORT_3.getCode(),
                xnhk0207.getSeccode(),
                xnhk0207.getF002d(),
                xnhk0207.getF006v()));
    }

}
