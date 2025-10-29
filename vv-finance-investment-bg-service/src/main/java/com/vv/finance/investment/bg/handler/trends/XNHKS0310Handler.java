package com.vv.finance.investment.bg.handler.trends;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhks0308;
import com.vv.finance.investment.bg.entity.uts.Xnhks0310;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0310Mapper;
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
 * 监听会议
 *
 * @author lichao
 * @date 2023/9/20 11:00
 */
@RequiredArgsConstructor
@Component
//@CanalTable("XNHKS0310")
@Slf4j

public class XNHKS0310Handler extends AbstractTrendsHandler implements EntryHandler<Xnhks0310> {

    @Autowired
    Xnhks0310Mapper xnhks0310Mapper;

    @Override
    public void sync() {
        LambdaQueryWrapper<Xnhks0310> wrapper = new LambdaQueryWrapper<Xnhks0310>().isNotNull(Xnhks0310::getSeccode);
        Long count = syncHelp(xnhks0310Mapper, wrapper, CompanyTrendsFactory::fromXnhks0310);
        log.info("XNHKS0310Handler.sync 同步数据 {} 条", count);
    }


    @Override
    public void syncRecent(Integer minusDays) {
        deleteRecentData(CompanyTrendsType.METTING.getCode(), null, minusDays);
        LambdaQueryWrapper<Xnhks0310> wrapper = new LambdaQueryWrapper<Xnhks0310>().isNotNull(Xnhks0310::getSeccode)
                .gt(Xnhks0310::getF001d, LocalDate.now().minusDays(minusDays));
        Long count = syncHelp(xnhks0310Mapper, wrapper, CompanyTrendsFactory::fromXnhks0310);
        log.info("XNHKS0310Handler.syncRecent 同步最近数据 {} 条", count);
    }



    @Override
    public void insert(Xnhks0310 xnhks0310) {
        log.info("==== xnhks0310   insert");
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhks0310(xnhks0310);
        save(companyTrendsMergeEntity);
    }

    @Override
    public void update(Xnhks0310 before, Xnhks0310 after) {
        log.info("==== xnhks0310   update   " + after.getXdbmask());
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhks0310(after);
        companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
    }

    @Override
    public void delete(Xnhks0310 xnhks0310) {
        log.info("==== xnhks0310   delete");
        if (xnhks0310.getSeccode() == null || xnhks0310.getF001d() == null || xnhks0310.getF005v() == null) {
            return;
        }
        remove(CompanyTrendsType.METTING.getCode(), getMD5(String.valueOf(CompanyTrendsType.METTING.getCode()),
                xnhks0310.getSeccode(),
                xnhks0310.getF001d(),
                xnhks0310.getF005v()));
    }
}
