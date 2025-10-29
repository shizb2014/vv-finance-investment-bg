package com.vv.finance.investment.bg.handler.trends;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhks0314;
import com.vv.finance.investment.bg.entity.uts.Xnhks0317;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0317Mapper;
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
 * 监听停牌
 *
 * @author lichao
 * @date 2023/9/20 11:00
 */
@RequiredArgsConstructor
@Component
//@CanalTable("XNHKS0317")
@Slf4j

public class XNHKS0317Handler extends AbstractTrendsHandler implements EntryHandler<Xnhks0317> {

    @Autowired
    Xnhks0317Mapper xnhks0317Mapper;

    @Override
    public void sync() {
        LambdaQueryWrapper<Xnhks0317> wrapper = new LambdaQueryWrapper<Xnhks0317>().isNotNull(Xnhks0317::getSeccode);
        Long count = syncHelp(xnhks0317Mapper, wrapper, CompanyTrendsFactory::fromXnhks0317);
        log.info("XNHKS0317Handler.sync 同步数据 {} 条", count);
    }

    @Override
    public void syncRecent(Integer minusDays) {
        deleteRecentData(CompanyTrendsType.TRADING.getCode(), CompanyTrendsSubType.SUSPENSION.getCode(), minusDays);
        LambdaQueryWrapper<Xnhks0317> wrapper = new LambdaQueryWrapper<Xnhks0317>().isNotNull(Xnhks0317::getSeccode)
                .gt(Xnhks0317::getF001d, LocalDate.now().minusDays(minusDays));
        Long count = syncHelp(xnhks0317Mapper, wrapper, CompanyTrendsFactory::fromXnhks0317);
        log.info("XNHKS0317Handler.syncRecent 同步最近数据 {} 条", count);
    }

    @Override
    public void insert(Xnhks0317 xnhks0317) {
        log.info("==== xnhks0317   insert");
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhks0317(xnhks0317);
        save(companyTrendsMergeEntity);
    }

    @Override
    public void update(Xnhks0317 before, Xnhks0317 after) {
        log.info("==== xnhks0317   update   " + after.getXdbmask());
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhks0317(after);
        companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
    }

    @Override
    public void delete(Xnhks0317 xnhks0317) {
        log.info("==== xnhks0317   delete");
        if (xnhks0317.getSeccode() == null || xnhks0317.getF002d() == null) {
            return;
        }
        remove(CompanyTrendsType.TRADING.getCode(), getMD5(String.valueOf(CompanyTrendsType.TRADING.getCode())
                , CompanyTrendsSubType.SUSPENSION.getCode()
                , xnhks0317.getSeccode()
                , xnhks0317.getF002d()));
    }
}