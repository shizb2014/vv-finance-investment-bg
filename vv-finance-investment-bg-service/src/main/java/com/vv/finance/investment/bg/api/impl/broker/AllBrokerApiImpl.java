package com.vv.finance.investment.bg.api.impl.broker;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.alibaba.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.common.annotation.MethodMonitor;
import com.vv.finance.common.utils.SortListUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.broker.AllBrokerApi;
import com.vv.finance.investment.bg.api.broker.BrokerAnalysisApi;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.BrokerConstants;
import com.vv.finance.investment.bg.dto.broker.DateResp;
import com.vv.finance.investment.bg.dto.broker.IndustryBrokerRankDTO;
import com.vv.finance.investment.bg.dto.broker.IndustryBrokerRankReq;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.broker.allBroker.*;
import com.vv.finance.investment.bg.entity.uts.Xnhk0102;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.stock.info.BrokerIndustryStatistics;
import com.vv.finance.investment.bg.stock.info.BrokerMarketValueStatistics;
import com.vv.finance.investment.bg.stock.info.BrokerStatistics;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerIndustryStatisticsMapper;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerMarketValueStatisticsMapper;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerStatisticsMapper;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import com.vv.finance.investment.bg.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class AllBrokerApiImpl implements AllBrokerApi {
    @Resource
    BrokerStatisticsMapper brokerStatisticsMapper;
    @Resource
    HkTradingCalendarApi hkTradingCalendarApi;
    @Resource
    RedisClient redisClient;
    @Resource
    BrokerViewApiImpl brokerViewApi;

    @Resource
    Xnhk0901Mapper xnhk0901Mapper;

    @Resource
    Xnhk0102Mapper xnhk0102Mapper;

    @Resource
    StockDefineMapper stockDefineMapper;

    @Resource
    IStockDefineService stockDefineService;

    @Resource
    Xnhk0004Mapper xnhk0004Mapper;

    @Resource
    Xnhks0101Mapper xnhks0101Mapper;

    @Resource
    Xnhks0104Mapper xnhks0104Mapper;

    @Resource
    BrokerMarketValueStatisticsMapper brokerMarketValueStatisticsMapper;

    @Resource
    BrokerCommonUtils brokerCommonUtils;

    @Resource
    BrokerAnalysisApi brokerAnalysisApi;

    @Resource
    IStockMarketService stockMarketService;

    @Resource
    IIndustrySubsidiaryService industrySubsidiaryService;

    @Resource
    BrokerIndustryStatisticsMapper brokerIndustryStatisticsMapper;

    @Resource
    private StockCache stockCache;

    @Override
    public List<AllBrokerRank> getAllBrokerRank(String brokerIdOrName, String sortKey, String sort, LocalDate startDate, LocalDate endDate) {
        //取出符合条件的经纪商id及名字
        //List<AllBrokerRank> allBrokerRanks = xnhk0610Mapper.getIdAndName();
        List<BrokerSearch> brokerList = redisClient.get(BrokerConstants.BG_BROKER_ID_PROFIT_LIST);
        if(CollectionUtils.isEmpty(brokerList)){
            return null;
        }
        List<AllBrokerRank> allBrokerRanks = new ArrayList<>();
        for (BrokerSearch brokerSearch : brokerList) {
            allBrokerRanks.add(AllBrokerRank.builder().brokerId(brokerSearch.getBrokerId()).brokerName(brokerSearch.getBrokerName()).build());
        }


        if (CollectionUtils.isEmpty(allBrokerRanks)) {
            return null;
        }

        //开盘前时间处理
        endDate = brokerCommonUtils.processBeforeOpening(endDate);

        //交易日处理
        if(startDate == null){
            //单日的情况,需前一天数据
            endDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
            startDate = hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
        }else{
            //区间的情况，需区间前一天数据
            endDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
            startDate = hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
            if(startDate.equals(endDate)){
                startDate = hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
            }
        }

        Long startDateLong = Long.parseLong(startDate.toString().replace("-", ""));
        if (endDate.equals(LocalDate.now()) && hkTradingCalendarApi.isTradingDay(endDate)) {
            //取得区间初前一天的数据
            List<BrokerMarketValueStatistics> startList = brokerMarketValueStatisticsMapper.selectList(new QueryWrapper<BrokerMarketValueStatistics>()
                    .eq("f001d", startDateLong));
            Map<String, BrokerMarketValueStatistics> startMap = startList.stream().collect(Collectors.toMap(BrokerMarketValueStatistics::getBrokerId, Function.identity()));

            //取得当天上一日的数据
            LocalDate beforeDate = hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
            Long beforeDateLong = DateUtils.localDateToF001D(beforeDate);
            List<BrokerMarketValueStatistics> beforeList = brokerMarketValueStatisticsMapper.selectList(new QueryWrapper<BrokerMarketValueStatistics>()
                    .eq("f001d", beforeDateLong));
            Map<String, BrokerMarketValueStatistics> beforeMap = beforeList.stream().collect(Collectors.toMap(BrokerMarketValueStatistics::getBrokerId, Function.identity()));

            //从redis取得当日经纪商维度的变动股数和变动市值
            List<BrokerIdAndValue> changeHoldingNumList = redisClient.get(BrokerConstants.BROKER_VALUE + "holdingNum");
            Map<String, BrokerIdAndValue> changeHoldingNumMap = changeHoldingNumList.stream().collect(Collectors.toMap(BrokerIdAndValue::getBrokerId, Function.identity()));
            List<BrokerIdAndValue> changeMarketValueList = redisClient.get(BrokerConstants.BROKER_VALUE + "marketValue");

            for (AllBrokerRank allBrokerRank : allBrokerRanks) {
                //区间初市值
                List<BrokerIdAndValue> changeMarkets = changeMarketValueList.stream().filter(item -> allBrokerRank.getBrokerId().equals(item.getBrokerId())).collect(Collectors.toList());
                BrokerMarketValueStatistics startStatistics = startMap.get(allBrokerRank.getBrokerId());
                BigDecimal startMarketValue = startStatistics == null ? BigDecimal.ZERO : startStatistics.getMarketVal();
                //当天变动市值
                BigDecimal changeMarketValue = CollectionUtils.isEmpty(changeMarkets) ? BigDecimal.ZERO : changeMarkets.get(0).getValue();
                changeMarketValue = changeMarketValue == null ? BigDecimal.ZERO : changeMarketValue;
                //当日前一天市值
                BrokerMarketValueStatistics beforeStatistics = beforeMap.get(allBrokerRank.getBrokerId());
                BigDecimal beforeMarketVal = beforeStatistics == null ? BigDecimal.ZERO : beforeStatistics.getMarketVal();
                //今日市值=当日前一天市值+当天变动市值
                BigDecimal todayMarketVal = beforeMarketVal.add(changeMarketValue);

                //持股市值
                allBrokerRank.setHoldingMarketValue(todayMarketVal);
                //持股市值变动=今日市值-区间初市值
                changeMarketValue = calcSubtract(todayMarketVal,startMarketValue);
                allBrokerRank.setChangeMarketValue(changeMarketValue);
                //持股市值变动比例
                if (Objects.isNull(changeMarketValue) || Objects.isNull(startMarketValue) || (startMarketValue.compareTo(BigDecimal.ZERO) == 0)) {
                    allBrokerRank.setChangeMarketValueRatio(BigDecimal.ZERO);
                } else {
                    allBrokerRank.setChangeMarketValueRatio(changeMarketValue.divide(startMarketValue, 4, RoundingMode.HALF_UP));
                }

                //变动持股量
//                List<BrokerIdAndValue> changHoldings = changeHoldingNumList.stream().filter(item -> allBrokerRank.getBrokerId().equals(item.getBrokerId())).collect(Collectors.toList());
//                BigDecimal changeInShareholding = CollectionUtils.isEmpty(changHoldings) ? BigDecimal.ZERO : changHoldings.get(0).getValue();
                BigDecimal changeInShareholding = changeHoldingNumMap.get(allBrokerRank.getBrokerId()) == null ? BigDecimal.ZERO : changeHoldingNumMap.get(allBrokerRank.getBrokerId()).getValue();
                allBrokerRank.setChangeInShareholding(changeInShareholding);
            }
        } else {
            //历史数据
            //筛选出区间初前一天和区间末的数据
            Long endDateLong = Long.parseLong(endDate.toString().replace("-", ""));
            List<BrokerMarketValueStatistics> endList = brokerMarketValueStatisticsMapper.selectList(new QueryWrapper<BrokerMarketValueStatistics>()
                    .eq("f001d", endDateLong));
            Map<String, BrokerMarketValueStatistics> endMap = endList.stream().collect(Collectors.toMap(BrokerMarketValueStatistics::getBrokerId, Function.identity()));
            List<BrokerMarketValueStatistics> startList = brokerMarketValueStatisticsMapper.selectList(new QueryWrapper<BrokerMarketValueStatistics>()
                    .eq("f001d", startDateLong));
            Map<String, BrokerMarketValueStatistics> startMap = startList.stream().collect(Collectors.toMap(BrokerMarketValueStatistics::getBrokerId, Function.identity()));

            for (AllBrokerRank allBrokerRank : allBrokerRanks) {
                //持股市值
//                BigDecimal endMarketValue = endList.stream().filter(item -> allBrokerRank.getBrokerId().equals(item.getBrokerId())).collect(Collectors.toList()).get(0).getMarketVal();
//                BigDecimal startMarketValue = startList.stream().filter(item -> allBrokerRank.getBrokerId().equals(item.getBrokerId())).collect(Collectors.toList()).get(0).getMarketVal();
                BigDecimal endMarketValue = endMap.get(allBrokerRank.getBrokerId()) == null ? BigDecimal.ZERO : endMap.get(allBrokerRank.getBrokerId()).getMarketVal();
                BigDecimal startMarketValue = startMap.get(allBrokerRank.getBrokerId()) == null ? BigDecimal.ZERO : startMap.get(allBrokerRank.getBrokerId()).getMarketVal();
                allBrokerRank.setHoldingMarketValue(endMarketValue);
                //持股市值变动
                BigDecimal changMarketValue = null;
                if (Objects.nonNull(endMarketValue) && Objects.nonNull(startMarketValue)) {
                    changMarketValue = endMarketValue.subtract(startMarketValue);
                }
                allBrokerRank.setChangeMarketValue(changMarketValue);
                //市值变动比例
                if (Objects.isNull(changMarketValue) || Objects.isNull(startMarketValue) || (startMarketValue.compareTo(BigDecimal.ZERO) == 0)) {
                    allBrokerRank.setChangeMarketValueRatio(null);
                } else {
                    allBrokerRank.setChangeMarketValueRatio(changMarketValue.divide(startMarketValue, 4, RoundingMode.HALF_UP));
                }
                //持股量变动
                BigDecimal endHolding = endMap.get(allBrokerRank.getBrokerId()) == null ? null : endMap.get(allBrokerRank.getBrokerId()).getBrokerHeldNumber();
                BigDecimal startHolding = startMap.get(allBrokerRank.getBrokerId()) == null ? null : startMap.get(allBrokerRank.getBrokerId()).getBrokerHeldNumber();
//                BigDecimal endHolding = endList.stream().filter(item -> allBrokerRank.getBrokerId().equals(item.getBrokerId())).collect(Collectors.toList()).get(0).getBrokerHeldNumber();
//                BigDecimal startHolding = startList.stream().filter(item -> allBrokerRank.getBrokerId().equals(item.getBrokerId())).collect(Collectors.toList()).get(0).getBrokerHeldNumber();
                BigDecimal changeInShareholding = null;
                if (Objects.nonNull(endHolding) && Objects.nonNull(startHolding)) {
                    changeInShareholding = endHolding.subtract(startHolding);
                }
                allBrokerRank.setChangeInShareholding(changeInShareholding);
            }
        }
        //排序
        if (StringUtils.isBlank(sortKey) || StringUtils.isBlank(sort)) {
            //默认按持股市值进行排序
            sort(allBrokerRanks, "holdingMarketValue", "desc");
        } else {
            //按字段进行排序
            sort(allBrokerRanks, sortKey, sort);
        }

        //设置排名
        for (int i = 0; i < allBrokerRanks.size(); i++) {
            allBrokerRanks.get(i).setRank(i + 1);
        }

        //用经纪商模糊查询(id，名字，拼音)
        if (StringUtils.isNotBlank(brokerIdOrName)) {
            allBrokerRanks = allBrokerRanks.stream()
                    .filter(item -> item.getBrokerId().toLowerCase().contains(brokerIdOrName.toLowerCase())
                            || item.getBrokerName().toLowerCase().contains(brokerIdOrName.toLowerCase())
                            || PinyinUtil.getPinyin(item.getBrokerName(), "").toLowerCase().contains(brokerIdOrName.toLowerCase())
                            || PinyinUtil.getFirstLetter(item.getBrokerName(), "").toLowerCase().contains(brokerIdOrName.toLowerCase()))
                    .sorted(Comparator.comparing(AllBrokerRank::getRank))
                    .collect(Collectors.toList());
        }
        DateResp dateResp = brokerAnalysisApi.getDateResp();
        if (!CollectionUtils.isEmpty(allBrokerRanks) && Objects.nonNull(dateResp)) {
            for (AllBrokerRank allBrokerRank : allBrokerRanks) {
                allBrokerRank.setDate(dateResp.getDate());
            }
        }
        return allBrokerRanks;
    }

    @Override
    @MethodMonitor(methodDesc = "获取行业维度经纪商榜单")
    public List<IndustryBrokerRankDTO> getIndustryBrokerRank(IndustryBrokerRankReq req) {
        List<IndustryBrokerRankDTO> list = new ArrayList<>();
        IndustrySubsidiary oneIndustry = industrySubsidiaryService.getOneIndustry(req.getIndustryCode());
        if (oneIndustry == null) {
            log.info("未找到该行业:{}", req.getIndustryCode());
            return list;
        }
        List<BrokerSearch> brokerList = redisClient.get(BrokerConstants.BG_BROKER_ID_PROFIT_LIST);
        List<BrokerIndustryStatistics> startDateList = brokerIndustryStatisticsMapper.selectList(new QueryWrapper<BrokerIndustryStatistics>()
                .eq("industry_name", oneIndustry.getName())
                .eq("f001d", req.getStartDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        );
        List<BrokerIndustryStatistics> endDateList = brokerIndustryStatisticsMapper.selectList(new QueryWrapper<BrokerIndustryStatistics>()
                .eq("industry_name", oneIndustry.getName())
                .eq("f001d", req.getEndDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        );
        for (BrokerSearch broker : brokerList) {
            BrokerIndustryStatistics startDateBroker = startDateList.stream().filter(item -> Objects.equals(item.getBrokerId(), broker.getBrokerId())).findFirst().orElse(null);
            BrokerIndustryStatistics endDateBroker = endDateList.stream().filter(item -> Objects.equals(item.getBrokerId(), broker.getBrokerId())).findFirst().orElse(null);
            if (startDateBroker == null && endDateBroker == null) {
                continue;
            }
            IndustryBrokerRankDTO dto = new IndustryBrokerRankDTO();
            dto.setBrokerId(broker.getBrokerId());
            dto.setBrokerName(broker.getBrokerName());
            // 持股市值
            BigDecimal endDateMarketVal = endDateBroker == null ? BigDecimal.ZERO : endDateBroker.getMarketVal();
            BigDecimal startMarketVal = startDateBroker == null ? BigDecimal.ZERO : startDateBroker.getMarketVal();
            dto.setHoldingMarketValue(NumberUtil.toBigDecimal(endDateMarketVal));
            // 持股变动市值
            dto.setChangeMarketValue(NumberUtil.sub(endDateMarketVal, startMarketVal));
            list.add(dto);
        }
        if(StringUtils.isBlank(req.getSortKey())) {
            SortListUtil.sortIncludingNull(list, "holdingMarketValue", SortListUtil.DESC);
        } else {
            SortListUtil.sortIncludingNull(list, req.getSortKey(), req.getSort());
        }
        return list;
    }

    @Override
    public List<BrokerMarketValueStatistics> getBrokerMarketValueTrend(String brokerId) {
        //0609表按经纪商id进行筛选
        List<BrokerMarketValueStatistics> dataList = brokerMarketValueStatisticsMapper.selectList(new QueryWrapper<BrokerMarketValueStatistics>().eq("broker_id", brokerId));
        return dataList;
    }

    @Override
    public List<BrokerMarketValueStatistics> getBrokerMarketValueTrendApp(String brokerId) {
        //0609表按经纪商id进行筛选
        List<BrokerMarketValueStatistics> dataList = brokerMarketValueStatisticsMapper.selectList(new QueryWrapper<BrokerMarketValueStatistics>().eq("broker_id", brokerId));
         return dataList;
    }

    /**
     * 全部经纪商-按股票分布
     *
     * @param brokerId
     * @return
     */

    @Override
    public PageDomain<BrokerShareHoldingsByCode> getBrokerShareHoldingsByCode(String brokerId,String code,LocalDate startDate,LocalDate endDate, String sort, String sortKey, Integer currentPage, Integer pageSize) {
        //创建集合保存数据
        List<BrokerShareHoldingsByCode> shareByCodeList = new ArrayList<>();
        //获取总股，查询指定字段
        List<Xnhk0102> xnhk0102List = xnhk0102Mapper.selectList(new QueryWrapper<Xnhk0102>().select("seccode", "f070n"));
        Map<String, Xnhk0102> xnhk0102Map = xnhk0102List.stream().
                collect(Collectors.toMap(item -> item.getSeccode(), Function.identity()));
        // List<StockDefine> stockDefineList = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().select("code", "stock_name"));
        List<StockDefine> stockDefineList = stockDefineService.listStockColumns(ListUtil.of("code", "stock_name"));
        Map<String, StockDefine> stockDefineMap = stockDefineList.stream().
                collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
        //开盘前时间处理
        endDate = brokerCommonUtils.processBeforeOpening(endDate);
        //交易日处理
        //是否是单日
        Boolean isDaily = false;
        //区间前一日
        LocalDate beforeDate = null;
        if(startDate == null){
            //单日的情况
            endDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
            startDate = hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
            beforeDate = startDate;
            isDaily = true;
        }else{
            //区间的情况
            endDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
            startDate = hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
            if(startDate.equals(endDate)){
                startDate = hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
            }
        }

        Long startDateLong = DateUtils.localDateToF001D(startDate);
        //计算前一日的该经纪商总持股市值
        BrokerMarketValueStatistics brokerMarketValueStatistics = brokerMarketValueStatisticsMapper.selectOne(new QueryWrapper<BrokerMarketValueStatistics>().eq("f001d", startDateLong).eq("broker_id", brokerId));
        BigDecimal totalMarketValue = brokerMarketValueStatistics == null ? BigDecimal.ZERO : brokerMarketValueStatistics.getMarketVal();

        //按股票code遍历赋值
        List<BrokerStatistics> stockList = getStockList(brokerId,null,code,startDate,endDate);
//        Set<String> codeSet = stockList.stream().map(item -> item.getSeccode()).collect(Collectors.toSet());
        Set<String> codeSet = brokerCommonUtils.getHoldingCodeSet(brokerId,endDate,null,code);
        if(CollectionUtils.isEmpty(codeSet)){
            return null;
        }

        List<BrokerShareHoldingsByCode> list = new ArrayList<>();
        Map<String, Long> stockCodeIdMap = stockCache.queryStockIdMap(null);
        if(endDate.equals(LocalDate.now())){
            //包含今日
            //取出redis数据(list),并转化为map
            List<BrokerRedisField> brokerRedisFields = redisClient.get(BrokerConstants.BROKER_VALUE_LIST + brokerId);
            if(brokerRedisFields == null){
                return null;
            }
            Map<String, BrokerRedisField> redisMap = brokerRedisFields.stream().collect(Collectors.toMap(BrokerRedisField::getCode, Function.identity()));
            for (String k : codeSet) {
                //每次遍历根据股票code再去筛选数据
                List<BrokerStatistics> brokerStatisticsList = stockList.stream().filter(item -> k.equals(item.getSeccode())).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(brokerStatisticsList)){
                    BrokerShareHoldingsByCode brokerShareHoldingsByCode = new BrokerShareHoldingsByCode();
                    Xnhk0102 xnhk0102 = xnhk0102Map.get(k);
                    BrokerRedisField brokerRedisField = redisMap.get(k);
                    Map<Long, BrokerStatistics> brokerMap = brokerStatisticsList.stream().collect(Collectors.toMap(BrokerStatistics::getF001d, Function.identity()));
                    //股票代码
                    brokerShareHoldingsByCode.setCode(k);
                    brokerShareHoldingsByCode.setStockId(stockCodeIdMap.get(k));
                    //股票名称
                    StockDefine stockDefine = stockDefineMap.get(k);
                    brokerShareHoldingsByCode.setStockName(stockDefine == null ? null : stockDefine.getStockName());
                    //持股数量
                    BigDecimal numberOfHolding = brokerRedisField == null ? BigDecimal.ZERO : brokerRedisField.getNumberOfHolding();
                    numberOfHolding = numberOfHolding == null ? BigDecimal.ZERO : numberOfHolding;
                    //持股变动股数
                    BigDecimal startShareholding = brokerMap.get(startDateLong) == null ? BigDecimal.ZERO : brokerMap.get(startDateLong).getF003n();
                    BigDecimal changeInShareholding = calcSubtract(numberOfHolding,startShareholding);
//                    BigDecimal changeInShareholding = brokerRedisField == null ? BigDecimal.ZERO : brokerRedisField.getNetTrade();
                    changeInShareholding = changeInShareholding == null ? BigDecimal.ZERO : changeInShareholding;
                    //变动比例（占已发行股）
                    BigDecimal f070n = xnhk0102 == null ? BigDecimal.ZERO : xnhk0102.getF070n();
                    BigDecimal changeRatio = calcCashScale(changeInShareholding,f070n,6);
                    brokerShareHoldingsByCode.setHoldChangeRation(changeRatio);
                    //持股比例(占已发行普通股)
                    BigDecimal holdingRatioInOrdinaryShares = calcCashScale(numberOfHolding,f070n,6);
                    brokerShareHoldingsByCode.setHoldRation(holdingRatioInOrdinaryShares);
                    //变动市值
                    BigDecimal todayMarketVal = brokerRedisField == null ? BigDecimal.ZERO : brokerRedisField.getHoldingMarketValue();
//                    Map<Long, BigDecimal> marketValMap = brokerStatisticsList.stream().collect(Collectors.toMap(BrokerStatistics::getF001d, BrokerStatistics::getMarketVal));
                    BigDecimal historyMarketVal = brokerMap.get(startDateLong) == null ? BigDecimal.ZERO : brokerMap.get(startDateLong).getMarketVal();
                    BigDecimal changeMarketValue = calcSubtract(todayMarketVal,historyMarketVal);
                    brokerShareHoldingsByCode.setRecentChangeMarketValue(changeMarketValue);
                    //参考持股市值
                    brokerShareHoldingsByCode.setHoldingMarketValue(todayMarketVal);
                    //总持股比例
                    BigDecimal rationOfTotalMarketValue = calcCash(todayMarketVal,totalMarketValue);
                    brokerShareHoldingsByCode.setRationOfTotalMarketValue(rationOfTotalMarketValue);
                    list.add(brokerShareHoldingsByCode);
                }
            }
        }else {
            //不包含今日
            for (String k : codeSet) {
                //每次遍历根据股票code再去筛选数据
                List<BrokerStatistics> brokerStatisticsList = stockList.stream().filter(item -> k.equals(item.getSeccode())).collect(Collectors.toList());
                Long endDateLong = DateUtils.localDateToF001D(endDate);
                if(!CollectionUtils.isEmpty(brokerStatisticsList)){
                    BrokerShareHoldingsByCode brokerShareHoldingsByCode = new BrokerShareHoldingsByCode();
                    Xnhk0102 xnhk0102 = xnhk0102Map.get(k);
                    //股票代码
                    brokerShareHoldingsByCode.setCode(k);
                    brokerShareHoldingsByCode.setStockId(stockCodeIdMap.get(k));
                    //股票名称
                    StockDefine stockDefine = stockDefineMap.get(k);
                    brokerShareHoldingsByCode.setStockName(stockDefine == null ? null : stockDefine.getStockName());
                    //持股数量
                    Map<Long, BrokerStatistics> brokerStatisticsMap = brokerStatisticsList.stream().collect(Collectors.toMap(BrokerStatistics::getF001d, Function.identity()));
//                    BigDecimal numberOfHolding = brokerStatisticsMap.get(endDateLong) == null ? BigDecimal.ZERO : brokerStatisticsMap.get(endDateLong).getF003n();
//                    numberOfHolding = numberOfHolding == null ? BigDecimal.ZERO : numberOfHolding;
//                    //持股变动股数
//                    BigDecimal startHolding = brokerStatisticsMap.get(startDateLong) == null ? BigDecimal.ZERO : brokerStatisticsMap.get(startDateLong).getF003n();
//                    BigDecimal changeInShareholding = calcSubtract(numberOfHolding,startHolding);
//                    changeInShareholding = changeInShareholding == null ? BigDecimal.ZERO : changeInShareholding;
                    //变动比例（占已发行股）
                    BigDecimal endRatio = brokerStatisticsMap.get(endDateLong) == null ? BigDecimal.ZERO : brokerStatisticsMap.get(endDateLong).getF004n();
                    BigDecimal startRatio = brokerStatisticsMap.get(startDateLong) == null ? BigDecimal.ZERO : brokerStatisticsMap.get(startDateLong).getF004n();
                    BigDecimal changeRatio = calcSubtract(endRatio,startRatio);
                    brokerShareHoldingsByCode.setHoldChangeRation(calcCashScale(changeRatio,BigDecimal.valueOf(100),6));
                    //持股比例(占已发行普通股)
                    brokerShareHoldingsByCode.setHoldRation(calcCash(endRatio,BigDecimal.valueOf(100)));
                    //变动市值
                    BigDecimal endMarketVal = brokerStatisticsMap.get(endDateLong) == null ? BigDecimal.ZERO : brokerStatisticsMap.get(endDateLong).getMarketVal();
                    BigDecimal startMarketVal = brokerStatisticsMap.get(startDateLong) == null ? BigDecimal.ZERO : brokerStatisticsMap.get(startDateLong).getMarketVal();
                    BigDecimal changeMarketValue = calcSubtract(endMarketVal,startMarketVal);
                    brokerShareHoldingsByCode.setRecentChangeMarketValue(changeMarketValue);
                    //参考持股市值
                    brokerShareHoldingsByCode.setHoldingMarketValue(endMarketVal);
                    //总持股比例
                    BigDecimal rationOfTotalMarketValue = calcCash(endMarketVal,totalMarketValue);
                    brokerShareHoldingsByCode.setRationOfTotalMarketValue(rationOfTotalMarketValue);
                    list.add(brokerShareHoldingsByCode);
                }
            }
        }

        //过滤到股票code为空的数据
        shareByCodeList = list.stream().filter(item -> Objects.nonNull(item.getCode())).collect(Collectors.toList());
        //默认按持股市值倒叙排序
        List<BrokerShareHoldingsByCode> newList = shareByCodeList.stream().sorted(Comparator.comparing(BrokerShareHoldingsByCode::getHoldRation).reversed()).collect(Collectors.toList());

        //排序方法
        sortByCode(newList, sortKey, sort);
        PageDomain<BrokerShareHoldingsByCode> pageDomain = new PageDomain<>();
        pageDomain.setRecords(newList);
        pageDomain.setTotal(newList.size());
        pageDomain.setSize(pageSize);
        pageDomain.setCurrent(currentPage);
        getPageList(pageDomain, currentPage, pageSize);
        return pageDomain;

    }

    /**
     * 全部经纪商-按行业分布
     *
     * @param brokerId
     * @param endDate
     * @return
     */
    @Override
    public PageDomain<BrokerShareHoldingsByIndustry> getBrokerShareHoldingsByIndustry(String brokerId,String code,String industryCode,LocalDate startDate,LocalDate endDate, String sort, String sortKey, Integer currentPage, Integer pageSize) {
        List<BrokerShareHoldingsByIndustry> shareByIndustryList = new ArrayList<>();
        //开盘前时间处理
        endDate = brokerCommonUtils.processBeforeOpening(endDate);
        //交易日处理(处理后都为交易日)
        //区间前一日
        LocalDate beforeDate = null;
        if(startDate == null){
            //单日的情况
            endDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
            startDate = hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
            beforeDate = startDate;
        }else{
            //区间的情况
            endDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
            if(hkTradingCalendarApi.isTradingDay(startDate)){
                if(startDate.equals(endDate)){
                    startDate = hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
                    beforeDate = startDate;
                }else {
                    beforeDate = hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
                }
            }else {
                startDate = hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
                beforeDate = startDate;
            }
        }
//        Long startDateLong = DateUtils.localDateToF001D(startDate);
//        Long endDateLong = DateUtils.localDateToF001D(endDate);
//        //经纪商的总市值
//        BigDecimal totalMarkerVal = BigDecimal.ZERO;
//
//        //取出所需数据表
//        List<StockDefine> stockDefines = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().select("code", "industry_code"));
//        Map<String, String> industryMap = stockDefines.stream().filter(item -> item.getIndustryCode() != null).collect(Collectors.toMap(StockDefine::getCode, StockDefine::getIndustryCode));
//        List<Xnhk0004> xnhk0004s = xnhk0004Mapper.selectList(new QueryWrapper<Xnhk0004>().select("code", "f001v"));
//        Map<String, String> xnhk0004Map = xnhk0004s.stream().collect(Collectors.toMap(Xnhk0004::getF001v, Xnhk0004::getCode));
//
//        //按经纪商代码、行业名称以及股票代码筛选
//        List<BrokerStatistics> stockList = getStockList(brokerId,null,null,startDate,endDate);
//        if(CollectionUtils.isEmpty(stockList)){
//            return null;
//        }
//        //获得筛选出来的股票code集合
//        Set<String> codeSet = brokerCommonUtils.getHoldingCodeSet(brokerId,endDate,null,null);
//        if(CollectionUtils.isEmpty(codeSet)){
//            return null;
//        }
////        Set<String> codeSet = stockList.stream().map(item -> item.getSeccode()).collect(Collectors.toSet());
//
//        //储存未按行业分类数据集合
//        List<BrokerShareHoldingsByIndustry> list = new ArrayList<>();
//        if(endDate.equals(LocalDate.now())){
//            //包含今日
//            //取出redis数据(list),并转化为map
//            List<BrokerRedisField> brokerRedisFields = redisClient.get(BrokerConstants.BROKER_VALUE_LIST + brokerId);
//            if(brokerRedisFields == null){
//                return null;
//            }
//            //总持股市值
//            totalMarkerVal = brokerRedisFields.stream().map(BrokerRedisField::getHoldingMarketValue).reduce(BigDecimal.ZERO,BigDecimal::add);
//            Map<String, BrokerRedisField> redisMap = brokerRedisFields.stream().collect(Collectors.toMap(BrokerRedisField::getCode, Function.identity()));
//            for (String k : codeSet) {
//                //每次遍历根据股票code再去筛选数据
//                List<BrokerStatistics> brokerStatisticsList = stockList.stream().filter(item -> k.equals(item.getSeccode())).collect(Collectors.toList());
//                if(!CollectionUtils.isEmpty(brokerStatisticsList)){
//                    BrokerShareHoldingsByIndustry brokerShareHoldingsByIndustry = new BrokerShareHoldingsByIndustry();
//                    BrokerRedisField brokerRedisField = redisMap.get(k);
//                    //行业代码
//                    String industryId = industryMap.get(k);
//                    brokerShareHoldingsByIndustry.setIndustryId(industryId);
//                    //参考持股市值
//                    BigDecimal todayMarketVal = brokerRedisField == null ? BigDecimal.ZERO : brokerRedisField.getHoldingMarketValue();
//                    brokerShareHoldingsByIndustry.setHoldingMarketValue(todayMarketVal);
//                    //变动市值
//                    Map<Long, BigDecimal> marketValMap = brokerStatisticsList.stream().collect(Collectors.toMap(BrokerStatistics::getF001d, BrokerStatistics::getMarketVal));
//                    BigDecimal historyMarketVal = marketValMap.get(startDateLong);
//                    BigDecimal changeMarketValue = calcSubtract(todayMarketVal,historyMarketVal);
//                    brokerShareHoldingsByIndustry.setRecentChangeMarketValue(changeMarketValue);
//                    list.add(brokerShareHoldingsByIndustry);
//                }
//            }
//        }else {
//            //不包含今日
//            //总持股比例
//            BrokerMarketValueStatistics brokerMarketValueStatistics = brokerMarketValueStatisticsMapper.selectOne(new QueryWrapper<BrokerMarketValueStatistics>().eq("f001d", endDateLong).eq("broker_id", brokerId));
//            totalMarkerVal = brokerMarketValueStatistics == null ? BigDecimal.ZERO : brokerMarketValueStatistics.getMarketVal();
//            for (String k : codeSet) {
//                //每次遍历根据股票code再去筛选数据
//                List<BrokerStatistics> brokerStatisticsList = stockList.stream().filter(item -> k.equals(item.getSeccode())).collect(Collectors.toList());
//                if(!CollectionUtils.isEmpty(brokerStatisticsList)){
//                    //将历史数据按时间分为key
//                    Map<Long, BrokerStatistics> brokerStatisticsMap = brokerStatisticsList.stream().collect(Collectors.toMap(BrokerStatistics::getF001d, Function.identity()));
//
//                    BrokerShareHoldingsByIndustry brokerShareHoldingsByIndustry = new BrokerShareHoldingsByIndustry();
//                    //行业名称
//                    //String industryId = industryMap.get(k);
//                    String industryName = brokerStatisticsMap.get(endDateLong) == null ? null : brokerStatisticsMap.get(endDateLong).getF014v();
//                    brokerShareHoldingsByIndustry.setIndustryName(industryName);
//                    //参考持股市值
//                    BigDecimal endMarketVal = brokerStatisticsMap.get(endDateLong) == null ? BigDecimal.ZERO : brokerStatisticsMap.get(endDateLong).getMarketVal();
//                    brokerShareHoldingsByIndustry.setHoldingMarketValue(endMarketVal);
//                    //变动市值
//                    BigDecimal startMarketVal = brokerStatisticsMap.get(startDateLong) == null ? BigDecimal.ZERO : brokerStatisticsMap.get(startDateLong).getMarketVal();
//                    BigDecimal changeMarketValue = calcSubtract(endMarketVal,startMarketVal);
//                    brokerShareHoldingsByIndustry.setRecentChangeMarketValue(changeMarketValue);
//                    list.add(brokerShareHoldingsByIndustry);
//                }
//            }
//        }
//
//        //对数据按行业进行分类加总
//        Map<String, List<BrokerShareHoldingsByIndustry>> map = list.stream().filter(item -> ObjectUtils.isNotEmpty(item.getIndustryName())).collect(Collectors.groupingBy(BrokerShareHoldingsByIndustry::getIndustryName));
//        List<BrokerShareHoldingsByIndustry> totalList = new ArrayList<>();
//        BigDecimal totalMarkerValue = totalMarkerVal;
//        map.forEach((k,v) -> {
//            //行业id
//            String instryId = xnhk0004Map.get(k);
//            //行业持股市值
//            BigDecimal holdingMarketValue = v.stream().map(BrokerShareHoldingsByIndustry::getHoldingMarketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
//            //行业变动市值
//            BigDecimal changeMarketValue = v.stream().map(BrokerShareHoldingsByIndustry::getRecentChangeMarketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
//            //行业占总市值比例
//            BigDecimal rationOfTotalMarketValue = calcCash(holdingMarketValue, totalMarkerValue);
//            totalList.add(BrokerShareHoldingsByIndustry.builder()
//                    .industryId(instryId).industryName(k)
//                    .holdingMarketValue(holdingMarketValue).recentChangeMarketValue(changeMarketValue)
//                    .rationOfTotalMarketValue(rationOfTotalMarketValue).build());
//        });
//
//

        List<BrokerShareHoldingsByIndustry> totalList = getNewIndustryList(brokerId,endDate,startDate);

        //取出所需数据表
        // List<StockDefine> stockDefines = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().select("code", "industry_code"));
        List<StockDefine> stockDefines = stockDefineService.listStockColumns(ListUtil.of("code", "industry_code"));
        Map<String, String> industryMap = stockDefines.stream().filter(item -> item.getIndustryCode() != null).collect(Collectors.toMap(StockDefine::getCode, StockDefine::getIndustryCode));

        //过滤掉股票行业code为空的数据
        shareByIndustryList = totalList.stream().filter(item -> Objects.nonNull(item.getIndustryId())).collect(Collectors.toList());
        if(code != null){
            industryCode = industryMap.get(code);
        }
        if(Objects.nonNull(industryCode)){
            String industryId = industryCode;
            shareByIndustryList = shareByIndustryList.stream().filter(item -> industryId.equals(item.getIndustryId())).collect(Collectors.toList());
        }
        //默认按持股市值倒叙排序
        List<BrokerShareHoldingsByIndustry> newList = shareByIndustryList.stream().sorted(Comparator.comparing(BrokerShareHoldingsByIndustry::getHoldingMarketValue).reversed()).collect(Collectors.toList());

        //排序方法
        sortByIndustry(newList, sortKey, sort);
        PageDomain<BrokerShareHoldingsByIndustry> pageDomain = new PageDomain<>();
        pageDomain.setRecords(newList);
        pageDomain.setTotal(newList.size());
        pageDomain.setSize(pageSize);
        pageDomain.setCurrent(currentPage);
        getPageList(pageDomain, currentPage, pageSize);
        return pageDomain;
    }

    @Override
    public List<BrokerShareHoldingsByCode> getPCBrokerShareHoldingsByCode(String brokerId, LocalDate endDate, String sort, String sortKey) {
        //创建集合保存数据
        List<BrokerShareHoldingsByCode> shareByCodeList = new ArrayList<>();
        //获取总股，查询指定字段
        List<Xnhk0102> xnhk0102List = xnhk0102Mapper.selectList(new QueryWrapper<Xnhk0102>().select("seccode", "f070n"));
        Map<String, Xnhk0102> xnhk0102Map = xnhk0102List.stream().
                collect(Collectors.toMap(item -> item.getSeccode(), Function.identity()));
//        //判断用户选择的时间是否为今日，如果是今日并且是交易日查询redis，如果不是，查询历史数据
//        if (endDate.equals(LocalDate.now()) && hkTradingCalendarApi.isTradingDay(endDate)) {
//            List<BrokerShareHoldingsByCode> list = new ArrayList<>();
//            //查询上一个交易日 获取上一个交易日持股数
//            BgTradingCalendar beforeTradingCalendar = hkTradingCalendarApi.getBeforeTradingCalendar(endDate);
//            LocalDate beforeDate = beforeTradingCalendar.getDate();
//            //转换成long类型
//            long beforeDateLong = Long.parseLong(beforeDate.toString().replace("-", ""));
//            //计算前一日的该经纪商总持股市值
//            //首先筛选出前一日经纪商持有的股票列表数据
//            BrokerMarketValueStatistics brokerMarketValueStatistics = brokerMarketValueStatisticsMapper.selectOne(new QueryWrapper<BrokerMarketValueStatistics>().eq("f001d", beforeDateLong).eq("broker_id", brokerId));
//            BigDecimal brokerMarketValue = brokerMarketValueStatistics == null ? BigDecimal.ZERO : brokerMarketValueStatistics.getMarketVal();
//
//            //从redis中获取到该经纪商今日的持股数据情况
//            Set<ZSetOperations.TypedTuple<Object>> todayProportionSharehold = redisClient.getZSetRangeWithScore(BrokerConstants.BROKER_TODAY + brokerId, 0, -1);
//            //遍历集合
//            Iterator<ZSetOperations.TypedTuple<Object>> iterator = todayProportionSharehold.iterator();
//
//            //筛选出股票code和股票名称的集合
//            //从redis获取
//            List<StockDefine> stockDefineList = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().select("code", "name"));
//            Map<String, StockDefine> stockDefineMap = stockDefineList.stream().
//                    collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
//            //根据交易日和经纪商Id，获取到经纪商前一个交易日对股票的持股数据
//            List<BrokerStatistics> beforeBrokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>().eq("F001D", beforeDateLong).eq("F002V", brokerId));
//
//            Map<String, BrokerStatistics> brokerStatisticsMap = beforeBrokerStatisticsList.stream().
//                    collect(Collectors.toMap(item -> item.getSeccode(), Function.identity()));
//
//            //根据brokerId获取到经纪商今日的持有股票的数据情况
//            List<BrokerRedisField> redisFields = redisClient.get(BrokerConstants.BROKER_VALUE_LIST.concat(brokerId));
//            if(CollectionUtils.isEmpty(redisFields)){
//                return null;
//            }
//            Map<String, BrokerRedisField> brokerRedisFieldMap = redisFields.stream().
//                    collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
//
//            //从外层拿到今日的持股市值变动的所有集合
//            Set<ZSetOperations.TypedTuple<Object>> todayHoldMarketValue = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_TODAYMARKETVAL, 0, -1);
//            Map<Object, ZSetOperations.TypedTuple<Object>> collect = todayHoldMarketValue.stream().collect(Collectors.toMap(item -> item.getValue(), Function.identity()));
//            Map<?, ?> map = collect;
//            Map<String, ZSetOperations.TypedTuple<Object>> newMap = (Map<String, ZSetOperations.TypedTuple<Object>>) map;
//
//            while (iterator.hasNext()) {
//                BrokerShareHoldingsByCode brokerShareHoldingsByCode = new BrokerShareHoldingsByCode();
//                BigDecimal todayMarketValue = BigDecimal.ZERO;
//                BigDecimal changeMarketValue = BigDecimal.ZERO;
//                ZSetOperations.TypedTuple<Object> next = iterator.next();
//                //获取到股code
//                String code = (String) next.getValue();
//
//                //获取到流通股股数
//                BigDecimal f070n = xnhk0102Map.get(code) == null ? BigDecimal.ZERO : xnhk0102Map.get(code).getF070n();
//                if (Objects.isNull(f070n) || f070n.compareTo(BigDecimal.ZERO) == 0) {
//                    continue;
//                }
//                String stockName = stockDefineMap.get(code) == null ? "" : stockDefineMap.get(code).getName();
//                //获取今日持股数
//                Double score = next.getScore();
//                //获取前一日持股数
//                BigDecimal beforeF003n = brokerStatisticsMap.get(code) == null ? BigDecimal.ZERO : brokerStatisticsMap.get(code).getF003n();
//                //计算今日持股数=今日买卖数+前一日持股数
//                BigDecimal todayHoldQuantity = beforeF003n.add(BigDecimal.valueOf(score));
//
//                //计算今日持股比例(占已发行普通股)
//                BigDecimal todayHoldRation = todayHoldQuantity.divide(f070n, 6, RoundingMode.HALF_UP);
//                brokerShareHoldingsByCode.setHoldRation(todayHoldRation);
//                //持股变动比例=持股变动数（今日净买卖数）/发行股数
//                BigDecimal netTrade = brokerRedisFieldMap.get(code) == null ? BigDecimal.ZERO : brokerRedisFieldMap.get(code).getNetTrade();
//                BigDecimal todayHoldChangeRation = netTrade.divide(f070n,6,RoundingMode.HALF_UP);
//                brokerShareHoldingsByCode.setHoldChangeRation(todayHoldChangeRation);
//
//                //从redis中获取到今日经纪商对当前股票的持股市值
//                todayMarketValue = brokerRedisFieldMap.get(code) == null ? BigDecimal.ZERO : brokerRedisFieldMap.get(code).getHoldingMarketValue();
//                Double d = newMap.get(code.concat("-").concat(brokerId)).getScore();
//                changeMarketValue = BigDecimal.valueOf(d);
//
//                //计算占市值比例
//                if (Objects.isNull(brokerMarketValue) || brokerMarketValue.compareTo(BigDecimal.ZERO) == 0) {
//                    brokerShareHoldingsByCode.setRationOfTotalMarketValue(null);
//                } else {
//                    BigDecimal rationOfMarketValue = todayMarketValue.divide(brokerMarketValue, 6, RoundingMode.HALF_UP);
//                    brokerShareHoldingsByCode.setRationOfTotalMarketValue(rationOfMarketValue);
//                }
//                brokerShareHoldingsByCode.setCode(code);
//                brokerShareHoldingsByCode.setStockName(stockName);
//                brokerShareHoldingsByCode.setHoldingMarketValue(todayMarketValue);
//                brokerShareHoldingsByCode.setRecentChangeMarketValue(changeMarketValue);
//                list.add(brokerShareHoldingsByCode);
//            }
//            shareByCodeList = list;
//        } else if (hkTradingCalendarApi.isTradingDay(endDate)) {
//            //如果查询日非今日，但是交易日
//            //首先获取到查询日前一交易日的日期
//            BgTradingCalendar beforeTradingCalendar = hkTradingCalendarApi.getBeforeTradingCalendar(endDate);
//            LocalDate beforedate = beforeTradingCalendar.getDate();
//            List<BrokerShareHoldingsByCode> codeList = getNewCodeList(brokerId, endDate, beforedate);
//            shareByCodeList = codeList;
//
//        } else {
//            //如果不是，查询历史数据
//            //首相根据用户传入的时间，查找对应的交易日
//            BgTradingCalendar queryTradingCalendar = hkTradingCalendarApi.getBeforeTradingCalendar(endDate);
//            //获取Date日期
//            LocalDate queryDate = queryTradingCalendar.getDate();
//            //根据这个交易日，查找前一个交易日
//            BgTradingCalendar beforeTradingCalendar = hkTradingCalendarApi.getBeforeTradingCalendar(queryTradingCalendar.getDate());
//            //将前一日的Date日期
//            LocalDate beforeDate = beforeTradingCalendar.getDate();
//            List<BrokerShareHoldingsByCode> codeList = getNewCodeList(brokerId, queryDate, beforeDate);
//            shareByCodeList = codeList;
//        }
        //20221227改版  取上一交易日和上两个交易日的数据
        LocalDate queryDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
        LocalDate beforeDate = hkTradingCalendarApi.getBeforeTradingCalendar(queryDate).getDate();
        shareByCodeList = getNewCodeList(brokerId,queryDate,beforeDate);
        //过滤到股票code为空的数据
        shareByCodeList = shareByCodeList.stream().filter(item -> Objects.nonNull(item.getCode())).collect(Collectors.toList());
        //默认按持股市值倒叙排序
        List<BrokerShareHoldingsByCode> newList = shareByCodeList.stream().sorted(Comparator.comparing(BrokerShareHoldingsByCode::getHoldingMarketValue).reversed()).collect(Collectors.toList());
        //排序方法
        sortByCode(newList, sortKey, sort);
        return newList;

    }

    @Override
    public List<BrokerShareHoldingsByIndustry> getPCBrokersShareHoldingsByIndustry(String brokerId, LocalDate endDate, String sort, String sortKey) {
        List<BrokerShareHoldingsByIndustry> shareByIndustryList = new ArrayList<>();
//        //判断用户选择的时间是否为今日，如果是今日并且是交易日查询redis，如果不是，查询历史数据
//        if (endDate.equals(LocalDate.now()) && hkTradingCalendarApi.isTradingDay(endDate)) {
//            List<BrokerShareHoldingsByIndustry> brokerShareHoldingsByIndustryList = new ArrayList<>();
//            //查询到上一个交易日
//            BgTradingCalendar beforeTradingCalendar = hkTradingCalendarApi.getBeforeTradingCalendar(endDate);
//            LocalDate beforeDate = beforeTradingCalendar.getDate();
//            //转换成long类型
//            long beforeDateLong = Long.parseLong(beforeDate.toString().replace("-", ""));
//            //计算前一日的该经纪商总持股市值
//            //首先筛选出前一日经纪商持有的股票列表数据
//            BrokerMarketValueStatistics brokerMarketValueStatistics = brokerMarketValueStatisticsMapper.selectOne(new QueryWrapper<BrokerMarketValueStatistics>().eq("f001d", beforeDateLong).eq("broker_id", brokerId));
//            BigDecimal brokerMarketValue = brokerMarketValueStatistics == null ? BigDecimal.ZERO : brokerMarketValueStatistics.getMarketVal();
//
//            //从redis中获取到今日该经纪商持股的股票数据
//            Set<ZSetOperations.TypedTuple<Object>> todayProportionSharehold = redisClient.getZSetRangeWithScore(BrokerConstants.BROKER_TODAY + brokerId, 0, -1);
//            //筛选出股票集合
//            List<Object> codeList = todayProportionSharehold.stream().map(ZSetOperations.TypedTuple::getValue).collect(Collectors.toList());
//            List<?> list = codeList;
//            List<String> newCode = (List<String>) list;
//            //创建一个行业代码集合
//            Set<String> industryCodeList = new HashSet<>();
//            //获取到行业对应代码外层list,包含所有股票的
//            List<StockDefine> stockDefinesList = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().select("code","industry_code"));
//            Map<String, StockDefine> stockDefineMap = stockDefinesList.stream().
//                    collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
//
//            //获取到行业的集合
//            for (String code : newCode) {
//                //通过stockDefine表获取到对应行业代码
//                String industryCode = stockDefineMap.get(code) == null ? null : stockDefineMap.get(code).getIndustryCode();
//                if (Objects.nonNull(industryCode)) {
//                    industryCodeList.add(industryCode);
//                }
//            }
//            //获取到行业名称的外层List,包含所有股票的
//            List<Xnhk0004> xnhk0004List = xnhk0004Mapper.selectList(new QueryWrapper<Xnhk0004>().select("CODE", "F001V"));
//            Map<String, Xnhk0004> xnhk0004Map = xnhk0004List.stream().
//                    collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
//            //获取到前一交易日持股数据外层list，包含所有股票的
//            List<BrokerStatistics> beforeBrokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>().eq("F001D", beforeDateLong).eq("F002v", brokerId));
//
//            //转换成map
//            Map<String,BrokerStatistics> beforeBrokerStatisticsMap = beforeBrokerStatisticsList.stream().collect(Collectors.toMap(item->item.getSeccode(),Function.identity()));
//            //根据brokerId获取到经纪商今日的持有股票的数据情况
//            List<BrokerRedisField> redisFields = redisClient.get(BrokerConstants.BROKER_VALUE_LIST.concat(brokerId));
//            //如果查询为空，直接返回空的list
//            if(CollectionUtils.isEmpty(redisFields)){
//                shareByIndustryList=brokerShareHoldingsByIndustryList;
//            }else{
//                Map<String, BrokerRedisField> brokerRedisFieldMap = redisFields.stream().
//                        collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
//                for (String industryCode : industryCodeList) {
//                    BrokerShareHoldingsByIndustry brokerShareHoldingsByIndustry = new BrokerShareHoldingsByIndustry();
//                    //根据行业代码获取行名称
//                    String industryName = xnhk0004Map.get(industryCode) == null ? null :xnhk0004Map.get(industryCode).getF001v();
//
//                    BigDecimal todayTotalMarketValue = BigDecimal.ZERO;
//                    BigDecimal beforeTotalMarketValue = BigDecimal.ZERO;
//                    //遍历今日交易的股票集合
//                    for (String code : newCode) {
//                        //根据股票code获取到行业代码
//                        //通过stockDefine表获取到对应行业代码
//                        String newIndustryCode = stockDefineMap.get(code) == null ? null :stockDefineMap.get(code).getIndustryCode();
//
//                        //如果股票在对应行业中
//                        if (Objects.nonNull(newIndustryCode) && newIndustryCode.equals(industryCode)) {
//                            //该经纪商今日对这只股票的持股市值
//                            BigDecimal todayMarketValue = brokerRedisFieldMap.get(code) == null ? BigDecimal.ZERO : brokerRedisFieldMap.get(code).getHoldingMarketValue();
//                            //计算该经纪商前一交易日对这只股票的持股市值
//                            BigDecimal beforeMarketValue = beforeBrokerStatisticsMap.get(code) == null ? BigDecimal.ZERO : beforeBrokerStatisticsMap.get(code).getMarketVal();
//                            //累加今日经纪商持股市值
//                            todayTotalMarketValue = todayTotalMarketValue.add(todayMarketValue);
//                            //累加前一日持股市值
//                            beforeTotalMarketValue = beforeTotalMarketValue.add(beforeMarketValue);
//                        }
//                    }
//                    //计算近一日持股市值变化
//                    BigDecimal changeMarketValue = todayTotalMarketValue.subtract(beforeTotalMarketValue);
//                    //计算占市值比例
//                    if (Objects.isNull(brokerMarketValue) || brokerMarketValue.compareTo(BigDecimal.ZERO) == 0) {
//                        brokerShareHoldingsByIndustry.setRationOfTotalMarketValue(null);
//                    } else {
//                        BigDecimal rationOfMarketValue = todayTotalMarketValue.divide(brokerMarketValue, 6, RoundingMode.HALF_UP);
//                        brokerShareHoldingsByIndustry.setRationOfTotalMarketValue(rationOfMarketValue);
//                    }
//                    brokerShareHoldingsByIndustry.setIndustryId(industryCode);
//                    brokerShareHoldingsByIndustry.setIndustryName(industryName);
//                    brokerShareHoldingsByIndustry.setHoldingMarketValue(todayTotalMarketValue);
//                    brokerShareHoldingsByIndustry.setRecentChangeMarketValue(changeMarketValue);
//                    brokerShareHoldingsByIndustryList.add(brokerShareHoldingsByIndustry);
//
//                }
//                shareByIndustryList = brokerShareHoldingsByIndustryList;
//            }
//
//        } else if (hkTradingCalendarApi.isTradingDay(endDate)) {
//            //如果查询日非今日，但是交易日
//            //首先获取到查询日前一交易日的日期
//            BgTradingCalendar beforeTradingCalendar = hkTradingCalendarApi.getBeforeTradingCalendar(endDate);
//            LocalDate beforeDate = beforeTradingCalendar.getDate();
//            //List<BrokerShareHoldingsByIndustry> industryList = getNewIndustryList(brokerId, endDate, beforeDate);
//            shareByIndustryList = getNewIndustryList(brokerId, endDate, beforeDate);
//
//        } else {
//            //如果不是今日也不是交易日，查询历史数据
//            //查询到对应的交易日
//            BgTradingCalendar queryTradingCalendar = hkTradingCalendarApi.getBeforeTradingCalendar(endDate);
//            //获取到对应的交易日
//            LocalDate queryDate = queryTradingCalendar.getDate();
//            //查询该交易日的上一个交易日
//            BgTradingCalendar beforeTradingCalendar = hkTradingCalendarApi.getBeforeTradingCalendar(queryDate);
//            //获取到前一个上一个交易日
//            LocalDate beforeDate = beforeTradingCalendar.getDate();
//            //List<BrokerShareHoldingsByIndustry> industryList = getNewIndustryList(brokerId, queryDate, beforeDate);
//            shareByIndustryList = getNewIndustryList(brokerId, queryDate, beforeDate);
//
//        }
        //20221227改版  取上一交易日和上两个交易日的数据
        LocalDate queryDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
        LocalDate beforeDate = hkTradingCalendarApi.getBeforeTradingCalendar(queryDate).getDate();
        shareByIndustryList = getNewIndustryList(brokerId,queryDate,beforeDate);

        //过滤掉股票行业code为空的数据
        shareByIndustryList = shareByIndustryList.stream().filter(item -> Objects.nonNull(item.getIndustryId())).collect(Collectors.toList());
        //默认按持股市值倒叙排序
        List<BrokerShareHoldingsByIndustry> newList = shareByIndustryList.stream().sorted(Comparator.comparing(BrokerShareHoldingsByIndustry::getHoldingMarketValue).reversed()).collect(Collectors.toList());
        sortByIndustry(newList, sortKey, sort);
        return newList;
    }



//    /**
//     * 计算经纪商当天持股市值
//     *
//     * @param brokerStatistics 前一日的0609表数据
//     * @param brokerId
//     * @return
//     */
//    private BigDecimal getTodayMarketValue(List<BrokerStatistics> brokerStatistics, String brokerId) {
//        //按经纪商进行筛选，同时以股票进行排列
//        brokerStatistics = brokerStatistics.stream()
//                .filter(item -> brokerId.equals(item.getF002v()))
//                .sorted(Comparator.comparing(item -> item.getSeccode())).collect(Collectors.toList());
//        BigDecimal beforeHolding = null;
//        BigDecimal todayHolding = null;
//        BigDecimal todayMarketValue = BigDecimal.ZERO;
//        for (int i = 0; i < brokerStatistics.size(); i++) {
//            //今日净买卖数
//            Double scoreToday = redisClient.getScore(brokerId, "today" + brokerStatistics.get(i).getSeccode());
//            BigDecimal score = BigDecimal.valueOf(Objects.isNull(scoreToday) ? 0 : scoreToday);
//            //取得今日持股数
//            beforeHolding = brokerStatistics.get(i).getF003n() == null ? BigDecimal.ZERO : brokerStatistics.get(i).getF003n();
//            todayHolding = score.add(beforeHolding);
//            StockSnapshot stockSnapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(brokerStatistics.get(i).getSeccode()));
//            BigDecimal last = stockSnapshot.getLast() == null ? BigDecimal.ZERO : stockSnapshot.getLast();
//            todayMarketValue.add(last.multiply(todayHolding));
//        }
//        return todayMarketValue;
//    }
//
//    /**
//     * 计算经纪商当天持股数
//     *
//     * @param brokerStatistics 前一日的0609表数据
//     * @param brokerId
//     * @return
//     */
//    private BigDecimal getTodayHolding(List<BrokerStatistics> brokerStatistics, String brokerId) {
//        //按经纪商进行筛选，同时以股票进行排列
//        brokerStatistics = brokerStatistics.stream()
//                .filter(item -> brokerId.equals(item.getF002v()))
//                .sorted(Comparator.comparing(item -> item.getSeccode())).collect(Collectors.toList());
//        BigDecimal todayHolding = BigDecimal.ZERO;
//        BigDecimal beforeHolding = null;
//        for (int i = 0; i < brokerStatistics.size(); i++) {
//            //今日净买卖数
//            Double scoreToday = redisClient.getScore(brokerId, "today" + brokerStatistics.get(i).getSeccode());
//            BigDecimal score = BigDecimal.valueOf(Objects.isNull(scoreToday) ? 0 : scoreToday);
//            //取得前日持股数再加上净买卖数
//            beforeHolding = brokerStatistics.get(i).getF003n() == null ? BigDecimal.ZERO : brokerStatistics.get(i).getF003n();
//            todayHolding.add(score.add(beforeHolding));
//        }
//        return todayHolding;
//    }
//
//    /**
//     * 获得经纪商历史某一天的持股市值
//     *
//     * @param brokerStatistics 所有经纪商列表（只按时间筛选过）
//     * @param localDate        日期
//     * @param brokerId         单个经纪商id
//     * @return
//     */
//    private BigDecimal getBrokerMarketValue(List<BrokerStatistics> brokerStatistics, LocalDate localDate, String brokerId) {
//        //按经纪商进行筛选，同时以股票进行排列(保证与K线的顺序能对应上)
//        if (Objects.nonNull(brokerId)) {
//            brokerStatistics = brokerStatistics.stream()
//                    .filter(item -> brokerId.equals(item.getF002v()))
//                    .sorted(Comparator.comparing(item -> item.getSeccode())).collect(Collectors.toList());
//        } else {
//            brokerStatistics = brokerStatistics.stream().sorted(Comparator.comparing(item -> item.getSeccode())).collect(Collectors.toList());
//        }
//        List<String> codeList = brokerStatistics.stream().map(item -> item.getSeccode()).collect(Collectors.toList());
//        String timeStr = localDate.toString().concat(" 00:00:00");
//        List<KlineEntity> klineList = klineDailyApi.getTimeList(codeList, timeStr);
//        BigDecimal marketValue = BigDecimal.ZERO;
//        for (int j = 0; j < klineList.size(); j++) {
//            if (Objects.nonNull(brokerStatistics.get(j).getF003n()) && Objects.nonNull(klineList.get(j).getClose())) {
//                marketValue.add(brokerStatistics.get(j).getF003n().multiply(klineList.get(j).getClose()));
//            }
//        }
//        return marketValue;
//    }

    /**
     * 按指定字段排序
     *
     * @param AllBrokerRanks 持股列表数据
     * @param sortKey        排序字段
     * @param sort           排序方式(asc:升序 desc:降序)
     */
    private void sort(List<AllBrokerRank> AllBrokerRanks, String sortKey, String sort) {
        List<String> fieldList = Arrays.stream(AllBrokerRank.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        if (fieldList.contains(sortKey)) {
            try {
                Comparator<AllBrokerRank> comparator = null;
                PropertyDescriptor descriptor = new PropertyDescriptor(sortKey, AllBrokerRank.class);
                Method readMethod = descriptor.getReadMethod();
                comparator = (t1, t2) -> {
                    try {
                        Object result1 = readMethod.invoke(t1);
                        BigDecimal decimal1 = result1 == null ? BigDecimal.valueOf(Long.MIN_VALUE) : new BigDecimal(result1.toString());
                        Object result2 = readMethod.invoke(t2);
                        BigDecimal decimal2 = result2 == null ? BigDecimal.valueOf(Long.MIN_VALUE) : new BigDecimal(result2.toString());
                        return decimal1.compareTo(decimal2);
                    } catch (Exception e) {
                        log.error("执行方法失败");
                        return 0;
                    }
                };
                if (comparator != null) {
                    if ("asc".equals(sort)) {
                        AllBrokerRanks.sort(comparator);
                    } else {
                        AllBrokerRanks.sort(comparator.reversed());
                    }
                }
            } catch (Exception e) {
                log.error("排序失败", e);
            }
        }
    }

    /**
     * 按请求页数及页面大小进行分页
     *
     * @param pageDomain
     * @param current
     * @param size
     */
    private <T> void getPageList(PageDomain<T> pageDomain, Integer current, Integer size) {
        if (current == null && size == null) {
            return;
        }

        List<T> records = pageDomain.getRecords();
        int ListSize = records.size();

        if ((ListSize < size || ListSize < 1) && current == 1) {
            return;
        }

        //当前页面起始位置(条数)
        int i = (current - 1) * size;

        if (ListSize < i) {
            pageDomain.setRecords(Collections.emptyList());
            return;
        }

        if (ListSize < (i + size)) {
            List<T> collect = Lists.newArrayList(records.subList(i, ListSize));
            pageDomain.setRecords(collect);
            return;
        }
        pageDomain.setRecords(new ArrayList<>(records.subList(i, i + size)));
    }



    /**
     * 根据经纪商Id，查询交易日，交易日前一日，获取到按股票分布的list
     *
     * @param brokerId
     * @param queryDate
     * @param beforeDate
     * @return
     */

    public List<BrokerShareHoldingsByCode> getNewCodeList(String brokerId, LocalDate queryDate, LocalDate beforeDate) {
        //创建一个空的集合返回数据
        List<BrokerShareHoldingsByCode> list = new ArrayList<>();
        //根据传入日期，将日期转换为long类型
        //将查询日转换成long类型
        Long queryDateLong = Long.parseLong(queryDate.toString().replace("-", ""));
        //将查询日的前一日转换成long类型
        Long beforeDateLong = Long.parseLong(beforeDate.toString().replace("-", ""));
        //根据交易日和经纪商Id，获取到经纪商对应交易日这一天对股票的持股数据
        List<BrokerStatistics> queryBrokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>().eq("F001D", queryDateLong).eq("F002V", brokerId));
        Map<String, BrokerStatistics> queryBrokerStatisticsMap = queryBrokerStatisticsList.stream().
                collect(Collectors.toMap(item -> item.getSeccode(), Function.identity()));

        //根据交易日和经纪商Id，获取到经纪商前一个交易日对股票的持股数据
        List<BrokerStatistics> beforeBrokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>().eq("F001D", beforeDateLong).eq("F002V", brokerId));
        Map<String, BrokerStatistics> beforeBrokerStatisticsMap = beforeBrokerStatisticsList.stream().
                collect(Collectors.toMap(item -> item.getSeccode(), Function.identity()));
        //计算前一日的该经纪商总持股市值
        BrokerMarketValueStatistics brokerMarketValueStatistics = brokerMarketValueStatisticsMapper.selectOne(new QueryWrapper<BrokerMarketValueStatistics>().eq("f001d", beforeDateLong).eq("broker_id", brokerId));
        BigDecimal brokerMarketValue = brokerMarketValueStatistics == null ? BigDecimal.ZERO : brokerMarketValueStatistics.getMarketVal();

        //筛选出股票code和股票名称的集合
        // List<StockDefine> stockDefineList = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().select("code", "name","stock_name"));
        List<StockDefine> stockDefineList = stockDefineService.listStockColumns(ListUtil.of("code", "name","stock_name"));
        Map<String, StockDefine> stockDefineMap = stockDefineList.stream().
                collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
        Map<String, Long> stockIdMap = stockCache.queryStockIdMap(null);
        for (BrokerStatistics b : queryBrokerStatisticsList) {
            if (!Objects.isNull(b.getSeccode())) {
                BrokerShareHoldingsByCode brokerShareHoldingsByCode = new BrokerShareHoldingsByCode();
                //获取到股票code
                String code = b.getSeccode();
                brokerShareHoldingsByCode.setCode(code);
                brokerShareHoldingsByCode.setStockId(stockIdMap.get(code));
                //根据股票code获取名称
                String stockName = stockDefineMap.get(code) == null ? null : stockDefineMap.get(code).getStockName();
                brokerShareHoldingsByCode.setStockName(stockName);
                //获取到查询日经纪商对股票的持股比例
                BigDecimal queryHoldRation = b.getF004n() == null ? BigDecimal.ZERO : b.getF004n().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                brokerShareHoldingsByCode.setHoldRation(queryHoldRation);
                //获取查询日持股市值
                BigDecimal queryMarketValue = b.getMarketVal() == null ? BigDecimal.ZERO : b.getMarketVal();
                //查询日持股市值
                brokerShareHoldingsByCode.setHoldingMarketValue(queryMarketValue);
                //获取前一日对该股票的持股比例
                if (Objects.isNull(beforeBrokerStatisticsMap.get(code))) {
                    BigDecimal beforeHoldRation = BigDecimal.ZERO;
                    //计算持股变动比例
                    brokerShareHoldingsByCode.setHoldChangeRation(queryHoldRation.subtract(beforeHoldRation));
                    //获取前一日对该股票的持股市值,由于未获取到前一日持股市值,持股市值变化=查询日持股市值
                    //计算近一日持股市值变化
                    brokerShareHoldingsByCode.setRecentChangeMarketValue(queryMarketValue);

                } else {
                    BigDecimal beforeHoldRation = beforeBrokerStatisticsMap.get(code).getF004n() == null ? BigDecimal.ZERO : beforeBrokerStatisticsMap.get(code).getF004n().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                    //计算持股变动比例
                    brokerShareHoldingsByCode.setHoldChangeRation(queryHoldRation.subtract(beforeHoldRation));
                    //获取前一日对该股票的持股市值
                    BigDecimal beforeTotalMarketValue = beforeBrokerStatisticsMap.get(code).getMarketVal() == null ? BigDecimal.ZERO : beforeBrokerStatisticsMap.get(code).getMarketVal();
                    //计算近一日持股市值变化
                    BigDecimal changeMarketValue = queryMarketValue.subtract(beforeTotalMarketValue);
                    brokerShareHoldingsByCode.setRecentChangeMarketValue(changeMarketValue);
                }
                //计算占市值比例
                if (Objects.isNull(brokerMarketValue) || brokerMarketValue.compareTo(BigDecimal.ZERO) == 0) {
                    brokerShareHoldingsByCode.setRationOfTotalMarketValue(null);
                } else {
                    BigDecimal rationOfMarketValue = queryMarketValue.divide(brokerMarketValue, 6, RoundingMode.HALF_UP);
                    brokerShareHoldingsByCode.setRationOfTotalMarketValue(rationOfMarketValue);
                }
                list.add(brokerShareHoldingsByCode);
            }
        }
        return list;
    }



    /**
     * 根据经纪商Id，查询交易日，交易日前一日，获取到按股票分布的List
     *
     * @param brokerId
     * @param queryDate
     * @param beforeDate
     * @return
     */

    public List<BrokerShareHoldingsByIndustry> getNewIndustryList(String brokerId, LocalDate queryDate, LocalDate beforeDate) {
        //创建一个空的集合返回保存数据
        List<BrokerShareHoldingsByIndustry> list = new ArrayList<>();
        //将查询日转换成long类型
        long queryDateLong = Long.parseLong(queryDate.toString().replace("-", ""));
        //将查询日的前一日转换成long类型
        Long beforeDateLong = Long.parseLong(beforeDate.toString().replace("-", ""));
        //获取退市股票列表
        List<String> quitCodeList = stockMarketService.getQuitCode().getData();
        //首先获取交易日的经纪商对行业持股数据(不计算退市股票数据)
        List<BrokerStatistics> todayIndustryList = brokerStatisticsMapper.getIndustryList(queryDateLong, brokerId,quitCodeList);
        //获取到前一交易日经纪商对行业的持股数据(不计算退市股票数据)
        List<BrokerStatistics> beforeIndustryList = brokerStatisticsMapper.getIndustryList(beforeDateLong, brokerId,quitCodeList);
        Map<String, BrokerStatistics> beforeIndustryMap = beforeIndustryList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getF014v()))
                .collect(Collectors.toMap(item -> item.getF014v(), Function.identity()));

//        //获取到行业代码和行业名称对应的集合
//        List<StockDefine> stockDefinesList = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().eq("code", "name"));
        //获取到行业名称的外层List,包含所有股票的
        // List<Xnhk0004> xnhk0004List = xnhk0004Mapper.selectList(new QueryWrapper<Xnhk0004>().select("CODE", "F001V"));
        // Map<String, String> xnhk0004Map = xnhk0004List.stream().collect(Collectors.toMap(Xnhk0004::getF001v, Xnhk0004::getCode));
        List<IndustrySubsidiary> industryList = industrySubsidiaryService.getAllIndustry();
        Map<String, String> xnhk0004Map = industryList.stream().collect(Collectors.toMap(IndustrySubsidiary::getName, IndustrySubsidiary::getCode));
        //获取经纪商前一交易日总持股市值
        //计算前一日的该经纪商总持股市值
        BigDecimal brokerMarketValue = BigDecimal.ZERO;
        Map<String, Long> stockCodeIdMap = stockCache.queryStockIdMap(null);
        BrokerMarketValueStatistics brokerMarketValueStatistics = brokerMarketValueStatisticsMapper.selectOne(new QueryWrapper<BrokerMarketValueStatistics>().eq("f001d", beforeDateLong).eq("broker_id", brokerId));
        if (Objects.isNull(brokerMarketValueStatistics)) {
            brokerMarketValue = BigDecimal.ZERO;
        } else {
            brokerMarketValue = brokerMarketValueStatistics.getMarketVal();
        }

        for (BrokerStatistics b : todayIndustryList) {
            BrokerShareHoldingsByIndustry brokerShareHoldingsByIndustry = new BrokerShareHoldingsByIndustry();
            //根据行业名称获取行业代码
            String industryCode = b.getF014v() == null ? null :xnhk0004Map.get(b.getF014v());
            //获取前一日对该行业的持股市值
            BigDecimal beforeTotalMarketValue = beforeIndustryMap.get(b.getF014v()) == null ? BigDecimal.ZERO : beforeIndustryMap.get(b.getF014v()).getMarketVal();
            //计算近一日持股市值变化
            BigDecimal changeMarketValue = calcSubtract(b.getMarketVal(), beforeTotalMarketValue);
            //获取经纪商总持股市值
            //计算占市值比例
            BigDecimal rationOfMarketValue = calcCash(b.getMarketVal(), brokerMarketValue);
            brokerShareHoldingsByIndustry.setRationOfTotalMarketValue(rationOfMarketValue);
            brokerShareHoldingsByIndustry.setIndustryId(industryCode);
            brokerShareHoldingsByIndustry.setStockId(stockCodeIdMap.get(b.getSeccode()));
            brokerShareHoldingsByIndustry.setIndustryName(b.getF014v());
            brokerShareHoldingsByIndustry.setHoldingMarketValue(b.getMarketVal());
            brokerShareHoldingsByIndustry.setRecentChangeMarketValue(changeMarketValue);
            list.add(brokerShareHoldingsByIndustry);
        }
        return list;
    }

    /**
     * 计算差值
     *
     * @param val1
     * @param val2
     * @return
     */
    public BigDecimal calcSubtract(BigDecimal val1,
                                   BigDecimal val2) {

        if (val1 == null && val2 == null) {
            return BigDecimal.ZERO;
        }

        if (val1 == null) {
            return BigDecimal.ZERO.subtract(val2);
        }
        if (val2 == null) {
            return val1;
        }
        return val1.subtract(val2);
    }

    /**
     * 除法
     *
     * @param val
     * @param val2
     * @return
     */
    public BigDecimal calcCash(BigDecimal val, BigDecimal val2) {
        if (val == null || val2 == null) {
            return BigDecimal.ZERO;
        }
        if (BigDecimal.ZERO.compareTo(val2) == 0) {
            return BigDecimal.ZERO;
        }
        return val.divide(val2, 4, RoundingMode.HALF_UP);
    }

    /**
     * 除法(保留位数)
     *
     * @param val
     * @param val2
     * @return
     */
    public BigDecimal calcCashScale(BigDecimal val, BigDecimal val2,Integer num) {
        if (val == null || val2 == null) {
            return BigDecimal.ZERO;
        }
        if (BigDecimal.ZERO.compareTo(val2) == 0) {
            return BigDecimal.ZERO;
        }
        return val.divide(val2, num, RoundingMode.HALF_UP);
    }

    /**
     * 按股票分布排序方法
     *
     * @param list
     * @param sortKey
     * @param sort
     */

    private void sortByCode(List<BrokerShareHoldingsByCode> list, String sortKey, String sort) {
        List<String> fieldList = Arrays.stream(BrokerShareHoldingsByCode.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        if (fieldList.contains(sortKey)) {
            try {
                Comparator<BrokerShareHoldingsByCode> comparator = null;
                if ("code".equals(sortKey)) {
                    comparator = Comparator.comparing(BrokerShareHoldingsByCode::getCode,Comparator.nullsLast(String::compareTo));
                }else if("stockName".equals(sortKey)){
                    comparator = Comparator.comparing(BrokerShareHoldingsByCode::getStockName,Comparator.nullsLast(String::compareTo));
                } else {
                    PropertyDescriptor descriptor = new PropertyDescriptor(sortKey, BrokerShareHoldingsByCode.class);
                    Method readMethod = descriptor.getReadMethod();
                    comparator = (t1, t2) -> {
                        try {
                            Object result1 = readMethod.invoke(t1);
                            BigDecimal decimal1 = result1 == null ? BigDecimal.valueOf(Long.MIN_VALUE) : new BigDecimal(result1.toString());
                            Object result2 = readMethod.invoke(t2);
                            BigDecimal decimal2 = result2 == null ? BigDecimal.valueOf(Long.MIN_VALUE) : new BigDecimal(result2.toString());
                            return decimal1.compareTo(decimal2);
                        } catch (Exception e) {
                            log.error("执行方法失败");
                            return 0;
                        }
                    };
                }
                if (comparator != null) {
                    if ("asc".equals(sort)) {
                        list.sort(comparator);
                    } else {
                        list.sort(comparator.reversed());
                    }
                }
            } catch (Exception e) {
                log.error("排序失败", e);
            }
        }
    }

    /**
     * 按行业分布排序方法
     *
     * @param list
     * @param sortKey
     * @param sort
     */

    private void sortByIndustry(List<BrokerShareHoldingsByIndustry> list, String sortKey, String sort) {
        List<String> fieldList = Arrays.stream(BrokerShareHoldingsByIndustry.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());

        if (fieldList.contains(sortKey)) {
            try {
                Comparator<BrokerShareHoldingsByIndustry> comparator = null;
                if ("industryId".equals(sortKey)) {
                    comparator = Comparator.comparing(BrokerShareHoldingsByIndustry::getIndustryId,Comparator.nullsLast(String::compareTo));
                }else if("industryName".equals(sortKey)){
                    //按首字母排序
                    Comparator comparatorChina = Comparator.nullsLast(Collator.getInstance(Locale.CHINA));
                    comparator = Comparator.comparing(BrokerShareHoldingsByIndustry::getIndustryName,comparatorChina);
                } else {
                    PropertyDescriptor descriptor = new PropertyDescriptor(sortKey, BrokerShareHoldingsByIndustry.class);
                    Method readMethod = descriptor.getReadMethod();
                    comparator = (t1, t2) -> {
                        try {
                            Object result1 = readMethod.invoke(t1);
                            BigDecimal decimal1 = result1 == null ? BigDecimal.valueOf(Long.MIN_VALUE) : new BigDecimal(result1.toString());
                            Object result2 = readMethod.invoke(t2);
                            BigDecimal decimal2 = result2 == null ? BigDecimal.valueOf(Long.MIN_VALUE) : new BigDecimal(result2.toString());
                            return decimal1.compareTo(decimal2);
                        } catch (Exception e) {
                            log.error("执行方法失败");
                            return 0;
                        }
                    };
                }
                if (comparator != null) {
                    if ("asc".equals(sort)) {
                        list.sort(comparator);
                    } else {
                        list.sort(comparator.reversed());
                    }
                }
            } catch (Exception e) {
                log.error("排序失败", e);
            }
        }

    }



    /**
     * 按照经纪商代码、行业名称以及股票代码筛选经纪商基础数据(brokerStatistics)
     * @param brokerId
     * @param industryName
     * @param code
     * @return
     */
    public List<BrokerStatistics> getStockList(String brokerId, String industryName, String code,
                                               LocalDate startDate, LocalDate endDate){
        endDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
        Long endDateLong = Long.parseLong(endDate.toString().replace("-", ""));
        LocalDate beforeDate = endDate;
        if(startDate == null){
            startDate = hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
        }else {
            if(hkTradingCalendarApi.isTradingDay(startDate)){
                //区间初前一日
                beforeDate = hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
            }else {
                startDate = hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
            }
        }
        if(startDate.equals(endDate)){
            startDate = hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
        }
        Long startDateLong = Long.parseLong(startDate.toString().replace("-", ""));
        Long beforeDateLong = Long.parseLong(beforeDate.toString().replace("-", ""));
        LocalDate yesterday = hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
        Long yesterdayLong = DateUtils.localDateToF001D(yesterday);
        List<Long> f001ds = new ArrayList<>();
        if(hkTradingCalendarApi.isTradingDay(endDate) && endDate.equals(LocalDate.now())){
            //若今天是交易日，还需取表中昨天持股的股票
            f001ds = Lists.newArrayList(startDateLong, endDateLong, beforeDateLong,yesterdayLong).stream().distinct().collect(Collectors.toList());
        }else {
            f001ds = Lists.newArrayList(startDateLong, endDateLong, beforeDateLong).stream().distinct().collect(Collectors.toList());
        }
        List<BrokerStatistics> stockList = new ArrayList<>();
        for(Long date : f001ds){
            List<BrokerStatistics> stock = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                    .eq("f002v", brokerId).eq("f001d", date));
            if(!CollectionUtils.isEmpty(stock)){
                stockList.addAll(stock);
            }
        }
//        List<BrokerStatistics> stockList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
//                .eq("f002v", brokerId).in("f001d",f001ds));
//                .eq("f002v", brokerId).and(item -> item.eq("f001d",startDateLong).or().eq("f001d",endDateLong).or().eq("f001d",beforeDateLong)));

        //按行业以及股票进行筛选（股票优先）
        // List<Xnhks0104> xnhks0104s = xnhks0104Mapper.selectList(null);
        if(StringUtils.isNotBlank(code)){
            stockList = stockList.stream().filter(item -> code.equals(item.getSeccode())).collect(Collectors.toList());
        }else {
            stockList = stockList.stream().filter(item -> {
                if (StringUtils.isBlank(industryName)){
                    return true;
                }else {
                    //List<Xnhks0104> collect = xnhks0104s.stream().filter(p -> item.getF014v().equals(p.getF014v())).collect(Collectors.toList());
                    //String industry = CollectionUtils.isEmpty(collect) ? "" : collect.get(0).getF014v();
                    return industryName.equals(item.getF014v());
                }
            }).collect(Collectors.toList());
        }
        //过滤掉系统中没有的股票
        // List<StockDefine> stockDefines = stockDefineMapper.selectList(null);
        List<StockDefine> stockDefines = stockDefineService.listStockColumns(null);
        List<String> codeList = stockDefines.stream().map(StockDefine::getCode).collect(Collectors.toList());
        stockList = stockList.stream().filter(item -> codeList.contains(item.getSeccode())).collect(Collectors.toList());
        return stockList;
    }
}
