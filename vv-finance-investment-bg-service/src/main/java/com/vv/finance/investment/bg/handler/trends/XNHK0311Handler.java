package com.vv.finance.investment.bg.handler.trends;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0207;
import com.vv.finance.investment.bg.entity.uts.Xnhk0311;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0311Mapper;
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
 * 监听交易告警
 *
 * @author lichao
 * @date 2023/9/20 11:00
 */
@RequiredArgsConstructor
@Component
//@CanalTable("XNHK0311")
@Slf4j
public class XNHK0311Handler extends AbstractTrendsHandler implements EntryHandler<Xnhk0311> {

    @Autowired
    Xnhk0311Mapper xnhk0311Mapper;

    @Override
    public void sync() {
        LambdaQueryWrapper<Xnhk0311> wrapper = new LambdaQueryWrapper<Xnhk0311>().isNotNull(Xnhk0311::getSeccode);
        Long count = syncHelp(xnhk0311Mapper, wrapper, CompanyTrendsFactory::fromXnhk0311);
        log.info("XNHK0311Handler.sync 同步数据 {} 条", count);
    }

    @Override
    public void syncRecent(Integer minusDays) {
        deleteRecentData(CompanyTrendsType.ALARM.getCode(), null, minusDays);
        LambdaQueryWrapper<Xnhk0311> wrapper = new LambdaQueryWrapper<Xnhk0311>().isNotNull(Xnhk0311::getSeccode)
                .gt(Xnhk0311::getF001d, LocalDate.now().minusDays(minusDays));
        Long count = syncHelp(xnhk0311Mapper, wrapper, CompanyTrendsFactory::fromXnhk0311);
        log.info("XNHK0311Handler.syncRecent 同步最近数据 {} 条", count);
    }

    @Override
    public void insert(Xnhk0311 xnhk0311) {
        log.info("==== xnhk0311   insert");
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk0311(xnhk0311);
        save(companyTrendsMergeEntity);
    }

    @Override
    public void update(Xnhk0311 before, Xnhk0311 after) {
        log.info("==== XNHK0311   update   " + after.getXdbmask());
        if (true) {
            return;
        }
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk0311(after);
        companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
    }

    @Override
    public void delete(Xnhk0311 xnhk0311) {
        log.info("==== XNHK0311   delete");
        if (xnhk0311.getSeccode() == null || xnhk0311.getF001d() == null || xnhk0311.getF003v() == null) {
            return;
        }
        remove(CompanyTrendsType.ALARM.getCode(), getMD5(String.valueOf(CompanyTrendsType.ALARM.getCode()),
                xnhk0311.getSeccode(),
                xnhk0311.getF001d(),
                xnhk0311.getF003v()));
    }
}
