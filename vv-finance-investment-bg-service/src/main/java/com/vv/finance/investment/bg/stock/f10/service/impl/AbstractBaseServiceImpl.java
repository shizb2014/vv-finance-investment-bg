package com.vv.finance.investment.bg.stock.f10.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.common.utils.ZipUtil;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.stock.StockRankingApi;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.uts.Xnhks0101;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0101Mapper;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.rank.dto.StockIndustryDto;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName AbstractBaseServiceImpl
 * @Deacription TODO
 * @Author lh.sz
 * @Date 2021年08月19日 14:39
 **/
@Service
@Slf4j
public abstract class AbstractBaseServiceImpl {

    @Resource
    Xnhks0101Mapper xnhks0101Mapper;
    @Resource
    RedisClient redisClient;

    @DubboReference(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
    StockRankingApi stockRankingApi;

    @Resource
    StockDefineMapper stockDefineMapper;
    @Autowired
    private StockService stockService;
    @Autowired
    protected StockCache stockCache;
    @Resource
    F10SourceServiceImpl f10SourceService;
    /**
     * 获取市场类型 0 =非金融 1=金融 2=保险
     *
     * @param code
     * @return
     */
    public int getMarketType(String code) {
        // int marketType = -1;
        // Xnhks0101 xnhks0101 = xnhks0101Mapper.selectOne(new QueryWrapper<Xnhks0101>()
        //         .eq("seccode", code));
        // if (xnhks0101 != null && StringUtils.isNotEmpty(xnhks0101.getF026v())) {
        //     marketType = Integer.parseInt(xnhks0101.getF026v());
        // }
        // return marketType;
        return f10SourceService.getMarketType(code);
    }

    /**
     * 获取股票的市场类型
     *
     * @param coll
     * @return
     */
    public List<Xnhks0101> getType(Collection<?> coll) {
        return xnhks0101Mapper.selectList(new QueryWrapper<Xnhks0101>()
                .in("seccode", coll));
    }

    /**
     * 获取股票行业一致的所有股票
     *
     * @param code 股票代码
     * @return
     */
    public List<String> getIndustryCodes(String code) {
        StockIndustryDto industryDto = stockRankingApi.queryStockIndustry(code).getData();
        if (ObjectUtils.isEmpty(industryDto) || ObjectUtil.isEmpty(industryDto.getIndustrySubsidiary()) || StrUtil.isBlank(industryDto.getIndustrySubsidiary().getCode())) {
            log.warn("getIndustryCodes queryStockIndustry is empty! code|industryDto: {}|{}", code, industryDto);
            return Collections.emptyList();
        }
        List<StockSnapshot> snapshotList = getSnapshot();
        log.info(snapshotList.stream().filter(s -> ObjectUtils.isEmpty(s.getIndustryCode())).map(StockSnapshot::getCode).collect(Collectors.toList()).toString());
        return snapshotList.stream().
                filter(s -> ObjectUtils.isNotEmpty(s.getIndustryCode()) && s.getIndustryCode().equals(industryDto.getIndustrySubsidiary().getCode()))
                .map(StockSnapshot::getCode).collect(Collectors.toList());
    }

    /**
     * 获取股票行业一致的所有股票
     *
     * @param code 股票代码
     * @return
     */
    public List<String> getIndustryCodesV2(String code) {
        StockIndustryDto industryDto = stockRankingApi.queryStockIndustry(code).getData();
        if (ObjectUtils.isEmpty(industryDto) || ObjectUtil.isEmpty(industryDto.getIndustrySubsidiary()) || StrUtil.isBlank(industryDto.getIndustrySubsidiary().getCode())) {
            log.warn("getIndustryCodesV2 queryStockIndustry is empty! code|industryDto: {}|{}", code, industryDto);
            return Collections.emptyList();
        }
        String industryCode = industryDto.getIndustrySubsidiary().getCode();
        List<StockDefine> stockDefines = stockDefineMapper.selectList(Wrappers.lambdaQuery(StockDefine.class).select(StockDefine::getCode).eq(StockDefine::getIndustryCode, industryCode).eq(StockDefine::getStockType, StockTypeEnum.STOCK.getCode()));
        return stockDefines.stream().map(StockDefine::getCode).collect(Collectors.toList());
    }

    /**
     * 获取行业代码
     *
     * @param code 股票代码
     * @return
     */
    public IndustrySubsidiary getIndustrySubsidiary(String code) {
        StockIndustryDto data = stockRankingApi.queryStockIndustry(code).getData();
        if (data == null){
            return new IndustrySubsidiary();
        }
        return data.getIndustrySubsidiary();
    }


    /**
     * 获取快照的行情数据
     */
    public List<StockSnapshot> getSnapshot() {
        List<ComStockSimpleDto> stockSimpleInfos = stockCache.queryStockInfoList(null);
        return stockService.getSnapshotListBySet(stockSimpleInfos.stream().filter(o-> StockTypeEnum.STOCK.getCode().equals(o.getStockType())).map(o -> o.getCode()).collect(Collectors.toSet()));
    }

    /**
     * 获取股票代码和名称
     *
     * @return
     */
    public Map<String, String> getCodeAndName2(List<String> codes) {
        List<StockSnapshot> snapshots = getSnapshot();
        List<String> notNameCodes = snapshots.stream().filter(o -> StringUtils.isBlank(o.getName())).map(o -> o.getCode()).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(notNameCodes)) {
            log.warn("未赋值股票名称codes : {}",notNameCodes);
        }
        return snapshots.stream().filter(s -> codes.contains(s.getCode()))
                .collect(Collectors.toMap(StockSnapshot::getCode, stockSnapshot->StringUtils.isBlank(stockSnapshot.getName())?"":stockSnapshot.getName()));
    }

    /**
     * 获取股票代码和名称
     *
     * @return
     */
    public Map<String, String> getCodeAndName(List<String> codes) {
        return stockCache.queryStockNameMap(null);
    }

    /**
     * 计算中值
     *
     * @param val1
     * @param val2
     * @return
     */
    public BigDecimal calcMin(BigDecimal val1,
                              BigDecimal val2) {
        if (val1 == null || val2 == null) {
            return BigDecimal.ZERO;
        }
        return val1.add(val2).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
    }

    /**
     * 计算估值的中值
     *
     * @param valList
     * @return
     */
    public BigDecimal calcMid(List<BigDecimal> valList) {
        BigDecimal mid = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(valList)) {
            int num = valList.size();
            if (judgeOddEven(num)) {
                mid = calcMin(valList.get((num / 2) - 1), valList.get((num / 2)));
            } else {
                mid = valList.get((num) / 2);
            }
        }
        return mid;
    }

    /**
     * 判断奇数偶数
     *
     * @param val
     * @return
     */
    public boolean judgeOddEven(int val) {
        return val % 2 == 0;
    }

}
