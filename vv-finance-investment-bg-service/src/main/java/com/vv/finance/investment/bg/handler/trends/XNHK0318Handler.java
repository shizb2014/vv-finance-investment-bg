package com.vv.finance.investment.bg.handler.trends;


import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vv.finance.investment.bg.api.uts.TrendsService;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0311;
import com.vv.finance.investment.bg.entity.uts.Xnhk0318;
import com.vv.finance.investment.bg.entity.uts.Xnhks0317;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0318Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0317Mapper;
import com.vv.finance.investment.bg.stock.information.enun.CompanyTrendsSubType;
import com.vv.finance.investment.bg.stock.information.enun.CompanyTrendsType;
import com.vv.finance.investment.bg.stock.information.factory.CompanyTrendsFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import javax.annotation.Resource;

import java.time.LocalDate;

import static com.vv.finance.investment.bg.stock.information.factory.CompanyTrendsFactory.getMD5;

/**
 * 监听复牌
 *
 * @author lichao
 * @date 2023/9/20 11:00
 */
@RequiredArgsConstructor
@Component
//@CanalTable("XNHK0318")
@Slf4j
public class XNHK0318Handler extends AbstractTrendsHandler implements EntryHandler<Xnhk0318> {

    @Autowired
    Xnhk0318Mapper xnhk0318Mapper;

    @Autowired
    Xnhks0317Mapper xnhks0317Mapper;

    @Resource
    TrendsService trendsService;

    @Override
    public void sync() {
        LambdaQueryWrapper<Xnhk0318> wrapper = new LambdaQueryWrapper<Xnhk0318>()
                .isNotNull(Xnhk0318::getSeccode)
                .isNotNull(Xnhk0318::getF002d)
                .isNotNull(Xnhk0318::getF003d);
        Long count = syncHelp(xnhk0318Mapper, wrapper, CompanyTrendsFactory::fromXnhk0318);
        log.info("XNHK0318Handler.sync 同步数据 {} 条", count);
//        log.info("XNHK0318Handler.sync");
    }

    @Override
    public void syncRecent(Integer minusDays) {
        deleteRecentData(CompanyTrendsType.TRADING.getCode(), CompanyTrendsSubType.RESUMPTION.getCode(), minusDays);
        LambdaQueryWrapper<Xnhk0318> wrapper = new LambdaQueryWrapper<Xnhk0318>().isNotNull(Xnhk0318::getSeccode)
                .gt(Xnhk0318::getF001d, LocalDate.now().minusDays(minusDays));
        Long count = syncHelp(xnhk0318Mapper, wrapper, CompanyTrendsFactory::fromXnhk0318);
        log.info("XNHK0318Handler.syncRecent 同步最近数据 {} 条", count);
    }



    /**
     * 复牌需要重写 save 方法，当停复牌的发布日期是在同一天时，不插入数据。
     * 同时还需要触发修改停牌的数据
     *
     * @param companyTrendsMergeEntity
     */
    @Override
    public void save(CompanyTrendsMergeEntity companyTrendsMergeEntity) {
        // 查看是否有当天的停牌数据，没有则插入复牌数据
        CompanyTrendsMergeEntity suspensionData = companyTrandsMergeMapper.selectOne(new LambdaQueryWrapper<CompanyTrendsMergeEntity>()
                .eq(CompanyTrendsMergeEntity::getType, CompanyTrendsType.TRADING.getCode())
                .eq(CompanyTrendsMergeEntity::getSubType, CompanyTrendsSubType.SUSPENSION.getCode())
                .eq(CompanyTrendsMergeEntity::getSECCODE, companyTrendsMergeEntity.getSECCODE())
                .eq(CompanyTrendsMergeEntity::getDate1, companyTrendsMergeEntity.getDate1())
        );
        if (suspensionData == null) {
            log.warn("找不到对应的停牌数据,{}", JSON.toJSON(companyTrendsMergeEntity));
            return;
        }
        if (!suspensionData.getReleaseDate().equals(companyTrendsMergeEntity.getReleaseDate())) {
            // 发布日期不同才插入
            companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
        }
        // 更新停牌数据。此处要做一个补偿
        if (!companyTrendsMergeEntity.getDate2().equals(suspensionData.getDate2())) {
            suspensionData.setDate2(companyTrendsMergeEntity.getDate2());
            companyTrandsMergeMapper.updateById(suspensionData);
        }
    }

    @Override
    public void insert(Xnhk0318 xnhk0318) {
        log.info("==== xnhk0318   insert");
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk0318(xnhk0318);
        save(companyTrendsMergeEntity);
    }

    @Override
    public void update(Xnhk0318 before, Xnhk0318 after) {
        log.info("==== XNHK0318   update   " + after.getXdbmask());
        CompanyTrendsMergeEntity companyTrendsMergeEntity = CompanyTrendsFactory.fromXnhk0318(after);
        companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
    }

    @Override
    public void delete(Xnhk0318 xnhk0318) {
        log.info("==== XNHK0318   delete");
        if (xnhk0318.getSeccode() == null || xnhk0318.getF002d() == null) {
            return;
        }
        remove(CompanyTrendsType.TRADING.getCode(), getMD5(String.valueOf(CompanyTrendsType.TRADING.getCode()),
                CompanyTrendsSubType.RESUMPTION.getCode(),
                xnhk0318.getSeccode(),
                xnhk0318.getF002d()));
    }
}