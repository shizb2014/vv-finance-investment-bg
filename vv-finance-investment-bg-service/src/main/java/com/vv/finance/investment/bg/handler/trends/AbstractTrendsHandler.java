package com.vv.finance.investment.bg.handler.trends;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.mapper.stock.quotes.CompanyTrandsMergeMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
public abstract class AbstractTrendsHandler {


    @Resource
    CompanyTrandsMergeMapper companyTrandsMergeMapper;

    /**
     * 全量数据同步
     */
    public abstract void sync();

    /**
     * 提供一种新的更新方式，用于解决融聚汇那边一直更新数据，导致canal大量无效事件
     * <p>
     * 删除并更新，通过定时任务触发
     */
    public abstract void syncRecent(Integer minusDays);

    public void deleteRecentData(Integer type, Integer subType, Integer minusDays) {
        LambdaQueryWrapper<CompanyTrendsMergeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompanyTrendsMergeEntity::getType, type);
        if (subType != null) {
            wrapper.eq(CompanyTrendsMergeEntity::getSubType, subType);
        }
        wrapper.gt(CompanyTrendsMergeEntity::getReleaseDate, LocalDate.now().minusDays(minusDays).toString().replace("-",""));
        companyTrandsMergeMapper.delete(wrapper);
    }


    public <T> Long syncHelp(BaseMapper<T> mapper, LambdaQueryWrapper<T> wrapper, Function<T, CompanyTrendsMergeEntity> convert) {
        long page = 1;
        long size = 1000;
        long count = 0;
        while (true) {
//            List<CompanyTrendsMergeEntity> companyTrendsMergeEntityList = new ArrayList<>();
            List<T> records = mapper.selectPage(new Page<>(page, size), wrapper).getRecords();
            if (CollUtil.isNotEmpty(records)) {
//                for (T recor : records) {
//                    CompanyTrendsMergeEntity companyTrendsMergeEntity = convert.apply(recor);
//                    save(companyTrendsMergeEntity);
//                }
                records.parallelStream().forEach(recor -> {
                    CompanyTrendsMergeEntity companyTrendsMergeEntity = convert.apply(recor);
                    if (ObjectUtils.isEmpty(companyTrendsMergeEntity)) {
                        return;
                    }
                    try {
                        save(companyTrendsMergeEntity);
                    } catch (Exception e) {
                        log.error("companyTrendsMergeEntity 异常信息：{}", JSON.toJSONString(companyTrendsMergeEntity), e);
                        log.error("recor 异常信息：{}", JSON.toJSONString(recor), e);
                    }
//                    companyTrendsMergeEntityList.add(companyTrendsMergeEntity);
                });
//                try{
//                    batchSave(companyTrendsMergeEntityList);
//                }catch(Exception e){
//                    log.error("companyTrendsMergeEntity 异常信息：{}" , JSON.toJSONString(companyTrendsMergeEntityList), e);
//                }
            }
            count += records.size();
            if (records.size() < size) {
                break;
            }
            page++;
        }
        return count;
    }


    public void batchSave(List<CompanyTrendsMergeEntity> companyTrendsMergeEntityList) {
        companyTrandsMergeMapper.batchSaveDupUpdate(companyTrendsMergeEntityList);
    }

    public void save(CompanyTrendsMergeEntity companyTrendsMergeEntity) {
        companyTrandsMergeMapper.saveDupUpdate(companyTrendsMergeEntity);
    }

    public boolean remove(Integer code, String uni) {
        companyTrandsMergeMapper.removeByUni(code, uni);
        return true;
    }


}
