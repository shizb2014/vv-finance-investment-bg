package com.vv.finance.investment.bg.handler.trends;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhks0310;
import com.vv.finance.investment.bg.entity.uts.Xnhks0314;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0314Mapper;
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
 * 监听并行交易
 *
 * @author lichao
 * @date 2023/9/20 11:00
 */
@RequiredArgsConstructor
@Component
//@CanalTable("XNHKS0314")
@Slf4j

public class XNHKS0314Handler extends AbstractTrendsHandler implements EntryHandler<Xnhks0314> {

    @Autowired
    Xnhks0314Mapper xnhks0314Mapper;

    @Override
    public void sync() {
        LambdaQueryWrapper<Xnhks0314> wrapper = new LambdaQueryWrapper<Xnhks0314>().isNotNull(Xnhks0314::getSeccode);
        Long count = syncHelp(xnhks0314Mapper, wrapper, CompanyTrendsFactory::fromXnhks0314);
        log.info("XNHKS0314Handler.sync 同步数据 {} 条", count);
    }


    @Override
    public void syncRecent(Integer minusDays) {
        deleteRecentData(CompanyTrendsType.PARALLEL.getCode(), null, minusDays);
        LambdaQueryWrapper<Xnhks0314> wrapper = new LambdaQueryWrapper<Xnhks0314>().isNotNull(Xnhks0314::getSeccode)
                .gt(Xnhks0314::getF001d, LocalDate.now().minusDays(minusDays));
        Long count = syncHelp(xnhks0314Mapper, wrapper, CompanyTrendsFactory::fromXnhks0314);
        log.info("XNHKS0314Handler.syncRecent 同步最近数据 {} 条", count);
    }




    @Override
    public void insert(Xnhks0314 xnhks0314) {
        log.info("==== xnhks0314   insert");
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhks0314(xnhks0314);
        save(companyTrendsMergeEntity);
    }

    @Override
    public void update(Xnhks0314 before, Xnhks0314 after) {
        log.info("==== xnhks0314   update   " + after.getXdbmask());
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhks0314(after);
        companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
    }

    @Override
    public void delete(Xnhks0314 xnhks0314) {
        log.info("==== xnhks0314   delete");
        if (xnhks0314.getSeccode() == null || xnhks0314.getF006d() == null) {
            return;
        }
        remove(CompanyTrendsType.PARALLEL.getCode(), getMD5(String.valueOf(CompanyTrendsType.PARALLEL.getCode()),
                xnhks0314.getSeccode(),
                xnhks0314.getF006d()));
    }
}