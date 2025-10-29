package com.vv.finance.investment.bg.handler.trends;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0127Mapper;
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
 * 监听除权事件
 *
 * @author lichao
 * @date 2023/9/20 11:00
 */

@RequiredArgsConstructor
@Component
//@CanalTable("xnhk0127")
@Slf4j
public class XNHK0127Handler extends AbstractTrendsHandler implements EntryHandler<Xnhk0127> {

    @Autowired
    Xnhk0127Mapper xnhk0127Mapper;


    @Override
    public void sync() {
        LambdaQueryWrapper<Xnhk0127> wrapper = new LambdaQueryWrapper<Xnhk0127>().isNotNull(Xnhk0127::getF003d);
        Long count = syncHelp(xnhk0127Mapper, wrapper, CompanyTrendsFactory::fromXnhk127);
        log.info("XNHK0127Handler.sync 同步数据 {} 条", count);
    }

    @Override
    public void syncRecent(Integer minusDays) {
        deleteRecentData(CompanyTrendsType.TEN.getCode(), null, minusDays);
        LambdaQueryWrapper<Xnhk0127> wrapper = new LambdaQueryWrapper<Xnhk0127>()
                .isNotNull(Xnhk0127::getF003d)
                .gt(Xnhk0127::getF003d, LocalDate.now().minusDays(minusDays));
        Long count = syncHelp(xnhk0127Mapper, wrapper, CompanyTrendsFactory::fromXnhk127);
        log.info("XNHK0127Handler.syncRecent 同步最近数据 {} 条", count);
    }

    @Override
    public void insert(Xnhk0127 xnhk0127) {
        log.info("==== XNHK0127   insert");
        // F003D 除尽日为空认为是脏数据
        if (xnhk0127.getF003d() == null) {
            return;
        }
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk127(xnhk0127);
        save(companyTrendsMergeEntity);
    }

    @Override
    public void update(Xnhk0127 before, Xnhk0127 after) {
//        log.info("==== XNHK0127   update   " + after.getId());
        // F003D 除尽日为空认为是脏数据
        if (after.getF003d() == null) {
            return;
        }
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk127(after);
        companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
    }

    @Override
    public void delete(Xnhk0127 xnhk0127) {
        log.info("==== XNHK0127   delete");
        remove(CompanyTrendsType.TEN.getCode(), getMD5(CompanyTrendsType.TEN.getCode(), xnhk0127.getId()));
    }
}
