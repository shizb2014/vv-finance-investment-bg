package com.vv.finance.investment.bg.stock.rank.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.dto.StockIndustry;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.mapper.IndustrySubsidiaryMapper;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 行业明细 服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
@Service
public class IndustrySubsidiaryServiceImpl extends ServiceImpl<IndustrySubsidiaryMapper, IndustrySubsidiary> implements IIndustrySubsidiaryService {

    @Resource
    private IStockDefineService stockDefineService;

    @Resource
    private StockCache stockCache;

    @Override
    public List<IndustrySubsidiary> getAllIndustry() {
        List<StockDefine> stockDefines = stockDefineService.list(Wrappers.<StockDefine>lambdaQuery().eq(StockDefine::getStockType, StockTypeEnum.HY.getCode()));
        Map<String, Long> stockIdMap = stockCache.queryStockIdMap(null);
        return stockDefines.stream().map(sd -> {
            IndustrySubsidiary subsidiary = new IndustrySubsidiary();
            subsidiary.setStockId(stockIdMap.get(sd.getCode()));
            subsidiary.setCode(sd.getCode());
            subsidiary.setName(sd.getStockName());
            subsidiary.setFirstRym(getFirstRym(sd.getStockName()));
            return subsidiary;
        }).collect(Collectors.toList());
    }

    @Override
    public IndustrySubsidiary getStockIndustry(String stockCode) {
        StockDefine stockDefine = stockDefineService.getOne(new QueryWrapper<StockDefine>().eq("code", stockCode));
        if (stockDefine == null) {
            return new IndustrySubsidiary();
        }
        // 查询股票所属行业
        return getOneIndustry(stockDefine.getIndustryCode());
    }

    @Override
    public IndustrySubsidiary getOneIndustry(String industryCode) {
        IndustrySubsidiary subsidiary = new IndustrySubsidiary();
        if (StrUtil.isNotBlank(industryCode)) {
            subsidiary.setCode(industryCode);
            Map<String, Long> stockIdMap = stockCache.queryStockIdMap(ListUtil.toList(industryCode));
            subsidiary.setStockId(stockIdMap.get(industryCode));
            StockDefine industryDefine = stockDefineService.getOne(new QueryWrapper<StockDefine>().eq("code", industryCode));
            if (ObjectUtil.isNotEmpty(industryDefine)) {
                subsidiary.setName(industryDefine.getStockName());
                subsidiary.setFirstRym(getFirstRym(industryDefine.getStockName()));
            }
        }
        return subsidiary;
    }

    private String getFirstRym(String industryName) {
        String firstLetter = PinyinUtil.getFirstLetter(StrUtil.sub(industryName, 0, 1), "");
        return StrUtil.upperFirst(firstLetter);
    }

    @Override
    public List<StockIndustry> getStockIndustries(List<String> stockCodes) {
        // 查询正股
        List<StockDefine> stockDefines = stockDefineService.listStockByCodes(stockCodes);
        List<String> industryCodes = CollUtil.map(stockDefines, StockDefine::getIndustryCode, true);
        // 查询行业
        List<StockDefine> industryDefines = stockDefineService.listDefinesByCodeType(industryCodes, StockTypeEnum.HY.getCode());
        Map<String, Long> stockIdMap = stockCache.queryStockIdMap(null);

        // 股票和行业关联
        Map<String, String> codeIndustryMap = stockDefines.stream().filter(f -> StrUtil.isNotBlank(f.getIndustryCode())).collect(Collectors.toMap(StockDefine::getCode, StockDefine::getIndustryCode, (o, v) -> v));
        Map<String, StockDefine> industryDefMap = industryDefines.stream().collect(Collectors.toMap(StockDefine::getCode, v -> v, (o, v) -> v));
        // 股票和行业关系
        return codeIndustryMap.keySet().stream().filter(code -> industryDefMap.containsKey(codeIndustryMap.get(code))).map(code -> {
            StockDefine indus = industryDefMap.get(codeIndustryMap.get(code));
            StockIndustry stockIndustry = StockIndustry.builder().stockId(stockIdMap.get(code)).stockCode(code).build();
            if (ObjectUtil.isNotEmpty(indus)) {
                stockIndustry.setIndustryId(stockIdMap.get(indus.getCode()));
                stockIndustry.setIndustryCode(indus.getCode());
                stockIndustry.setIndustryName(indus.getStockName());
            }
            return stockIndustry;
        }).collect(Collectors.toList());
    }
}
