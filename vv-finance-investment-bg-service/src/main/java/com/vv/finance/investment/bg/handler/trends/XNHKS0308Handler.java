package com.vv.finance.investment.bg.handler.trends;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0318;
import com.vv.finance.investment.bg.entity.uts.Xnhks0308;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0308Mapper;
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
 * 监听收并购事件
 *
 * @author lichao
 * @date 2023/9/20 11:00
 */
@RequiredArgsConstructor
@Component
//@CanalTable("XNHKS0308")
@Slf4j

public class XNHKS0308Handler extends AbstractTrendsHandler implements EntryHandler<Xnhks0308> {

    @Autowired
    Xnhks0308Mapper xnhks0308Mapper;

    @Override
    public void sync() {
        LambdaQueryWrapper<Xnhks0308> wrapper = new LambdaQueryWrapper<Xnhks0308>().isNotNull(Xnhks0308::getF005d);
        Long count = syncHelp(xnhks0308Mapper, wrapper, CompanyTrendsFactory::fromXnhks0308);
        log.info("XNHKS0308Handler.sync 同步数据 {} 条", count);

    }

    @Override
    public void syncRecent(Integer minusDays) {
        deleteRecentData(CompanyTrendsType.PURCHASE.getCode(), null, minusDays);
        LambdaQueryWrapper<Xnhks0308> wrapper = new LambdaQueryWrapper<Xnhks0308>().isNotNull(Xnhks0308::getSeccode)
                .isNotNull(Xnhks0308::getF005d)
                .gt(Xnhks0308::getF001d, LocalDate.now().minusDays(minusDays));
        Long count = syncHelp(xnhks0308Mapper, wrapper, CompanyTrendsFactory::fromXnhks0308);
        log.info("XNHKS0308Handler.syncRecent 同步最近数据 {} 条", count);
    }



    @Override
    public void insert(Xnhks0308 xnhks0308) {
        log.info("==== xnhks0308   insert");
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhks0308(xnhks0308);
        save(companyTrendsMergeEntity);
    }

    @Override
    public void update(Xnhks0308 before, Xnhks0308 after) {
        log.info("==== xnhks0308   update   " + after.getXdbmask());
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhks0308(after);
        companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
    }

    @Override
    public void delete(Xnhks0308 xnhks0308) {
        log.info("==== xnhks0308   delete");
        if (xnhks0308.getSeccode() == null || xnhks0308.getF001d() == null) {
            return;
        }
        remove(CompanyTrendsType.PURCHASE.getCode(), getMD5(String.valueOf(CompanyTrendsType.PURCHASE.getCode()),
                xnhks0308.getSeccode(),
                xnhks0308.getF001d()));
    }
}