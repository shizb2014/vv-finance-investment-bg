package com.vv.finance.investment.bg.api.impl.broker;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.broker.BrokerViewApi;
import com.vv.finance.investment.bg.api.frontend.IStockKlineService;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.BrokerConstants;
import com.vv.finance.investment.bg.dto.kline.BaseKlineDTO;
import com.vv.finance.investment.bg.dto.kline.KlineDTO;
import com.vv.finance.investment.bg.dto.req.KlineReq;
import com.vv.finance.investment.bg.entity.broker.allBroker.*;
import com.vv.finance.investment.bg.entity.industry.IndustryDailyKline;
import com.vv.finance.investment.bg.entity.uts.Xnhk0004;
import com.vv.finance.investment.bg.entity.uts.Xnhk0102;
import com.vv.finance.investment.bg.entity.uts.Xnhks0101;
import com.vv.finance.investment.bg.entity.uts.Xnhks0104;
import com.vv.finance.investment.bg.industry.service.IIndustryDailyKlineService;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.stock.info.BrokerIndustryStatistics;
import com.vv.finance.investment.bg.stock.info.BrokerStatistics;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerIndustryStatisticsMapper;
import com.vv.finance.investment.bg.stock.info.mapper.BrokerStatisticsMapper;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import com.vv.finance.investment.bg.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Sets;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class BrokerViewApiImpl implements BrokerViewApi {
    @Resource
    Xnhk0610Mapper xnhk0610Mapper;
    @Resource
    BrokerStatisticsMapper brokerStatisticsMapper;
    @Resource
    StockDefineMapper stockDefineMapper;
    @Resource
    IStockDefineService stockDefineService;
    @Resource
    Xnhks0104Mapper xnhks0104Mapper;
    @Resource
    Xnhk0102Mapper xnhk0102Mapper;
    @Resource
    Xnhk0004Mapper xnhk0004Mapper;
    @Resource
    HkTradingCalendarApi hkTradingCalendarApi;
    @Resource
    RedisClient redisClient;
    @Resource
    Xnhks0101Mapper xnhks0101Mapper;
    @Resource
    BrokerCommonUtils brokerCommonUtils;
    @Resource
    BrokerIndustryStatisticsMapper brokerIndustryStatisticsMapper;
    @Resource
    IIndustryDailyKlineService industryDailyKlineService;

    @Resource
    private IIndustrySubsidiaryService industrySubsidiaryService;
    @Resource
    private StockCache stockCache;

    @Override
    public List<BrokerSearch> getBrokerIdAndName(String codeOrName) {
//        List<Xnhk0610> xnhk0610List = null;
//        if(StringUtils.isBlank(codeOrName)){
//            //搜索条件为空，返回所有经纪商数据
//            xnhk0610List = xnhk0610Mapper.selectList(null);
//        }else {
//            xnhk0610List = xnhk0610Mapper.selectList(new QueryWrapper<Xnhk0610>().like("f001v", codeOrName).or().like("f005v", codeOrName));
//        }
//        if(CollectionUtils.isEmpty(xnhk0610List)){
//            return null;
//        }
//        List<BrokerSearch> brokerSearchList = new ArrayList<>();
//        for (Xnhk0610 xnhk0610 : xnhk0610List) {
//            BrokerSearch brokerSearch = BrokerSearch.builder()
//                    .brokerId(xnhk0610.getF001v())
//                    .brokerName(xnhk0610.getF005v())
//                    .build();
//            brokerSearchList.add(brokerSearch);
//        }
        List<BrokerSearch> brokerSearchList = null;
        if(StringUtils.isBlank(codeOrName)){
            //搜索条件为空，返回所有经纪商数据
            brokerSearchList = redisClient.get(BrokerConstants.BG_BROKER_ID_PROFIT_LIST);
        }else {
            brokerSearchList = redisClient.get(BrokerConstants.BG_BROKER_ID_PROFIT_LIST);
            brokerSearchList = brokerSearchList.stream().filter(item -> {
                return item.getBrokerId().toLowerCase().contains(codeOrName.toLowerCase()) || item.getBrokerName().toLowerCase().contains(codeOrName.toLowerCase());
            }).collect(Collectors.toList());
        }
        return brokerSearchList;
    }

    @Override
    public List<StockSearch> getCodeAndName(String codeOrName) {
        List<StockDefine> stockDefines = null;
        if(StringUtils.isBlank(codeOrName)){
            //搜索条件为空，返回所有经纪商数据
            stockDefines = stockDefineService.listStockColumns(null);
        }else {
            stockDefines = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().eq("stock_type", StockTypeEnum.STOCK.getCode()).like("code", codeOrName).or().like("name", codeOrName));
        }

        if(CollectionUtils.isEmpty(stockDefines)){
            return null;
        }
        List<StockSearch> stockSearchList = new ArrayList<>();
        for (StockDefine stockDefine : stockDefines) {
            StockSearch stockSearch = StockSearch.builder()
                    .code(stockDefine.getCode())
                    .stockName(stockDefine.getStockName())
                    .build();
            stockSearchList.add(stockSearch);
        }
        return stockSearchList;
    }

    @Override
    public List<IndustrySearch> getIndustryCodeAndName(String codeOrName) {
        // List<Xnhk0004> xnhk0004List = null;
        // if(StringUtils.isBlank(codeOrName)){
        //     //搜索条件为空，返回所有行业数据
        //     xnhk0004List = xnhk0004Mapper.selectList(null);
        // }else {
        //     codeOrName = codeOrName.replace("%", "=");
        //     xnhk0004List = xnhk0004Mapper.selectList(new QueryWrapper<Xnhk0004>().like("code", codeOrName).or().like("f001v", codeOrName));
        // }
        //
        // if(CollectionUtils.isEmpty(xnhk0004List)){
        //     return null;
        // }
        // List<IndustrySearch> industrySearches = new ArrayList<>();
        // for (Xnhk0004 xnhk0004 : xnhk0004List) {
        //     IndustrySearch industrySearch = IndustrySearch.builder()
        //             .industryCode(xnhk0004.getCode())
        //             .industryName(xnhk0004.getF001v())
        //             .build();
        //     industrySearches.add(industrySearch);
        // }
        // return industrySearches;
        List<IndustrySubsidiary> allIndustry = industrySubsidiaryService.getAllIndustry();
        return allIndustry.stream()
                .filter(ai -> StrUtil.isBlank(codeOrName) || (StrUtil.containsIgnoreCase(ai.getCode(), codeOrName) || StrUtil.containsIgnoreCase(ai.getName(), codeOrName)))
                .map(indus -> IndustrySearch.builder()
                .industryCode(indus.getCode())
                .industryName(indus.getName())
                .build()).collect(Collectors.toList());
    }

    @Override
    public IndustrySearch getIndustryByCode(String code) {
        IndustrySubsidiary subsidiary = industrySubsidiaryService.getStockIndustry(code);
        Map<String, Long> stockCodeIdMap = stockCache.queryStockIdMap(ListUtil.toList(subsidiary.getCode()));
        // Xnhks0104 xnhks0104 = xnhks0104Mapper.selectOne(new QueryWrapper<Xnhks0104>().eq("seccode", code));
        // if(Objects.isNull(xnhks0104) || Objects.isNull(xnhks0104.getF014v())){
        //     return null;
        // }
        // String industryName = xnhks0104.getF014v();
        // Xnhk0004 xnhk0004 = xnhk0004Mapper.selectOne(new QueryWrapper<Xnhk0004>().eq("f001v", industryName));
        // String industryCode = Objects.isNull(xnhk0004)||Objects.isNull(xnhk0004.getCode()) ? null : xnhk0004.getCode();
        return IndustrySearch.builder().industryId(stockCodeIdMap.get(subsidiary.getCode())).industryName(subsidiary.getName()).industryCode(subsidiary.getCode()).build();
    }


    @Override
    public List<ShareholdingsTable> getShareholdingsTable(String brokerId,String industryCode,
                                                          String code,String sortKey,String sort,
                                                          LocalDate startDate,LocalDate endDate) {
        //依据行业code获取行业名称
        // Xnhk0004 xnhk0004 = xnhk0004Mapper.selectOne(new QueryWrapper<Xnhk0004>().eq("code", industryCode));
        IndustrySubsidiary oneIndustry = industrySubsidiaryService.getOneIndustry(industryCode);
        String industryName = oneIndustry == null ? null : oneIndustry.getName();
        List<ShareholdingsTable> shareholdingsTables = new ArrayList<>();
        //开盘前时间处理
        endDate = brokerCommonUtils.processBeforeOpening(endDate);
        //按经纪商代码、行业名称以及股票代码筛选
        List<BrokerStatistics> stockList = getStockList(brokerId,industryName,code,startDate,endDate);

        if(CollectionUtils.isEmpty(stockList)){
            return null;
        }
        //提前拿出所需表
        List<Xnhk0102> xnhk0102s = xnhk0102Mapper.selectList(null);
        Map<String, Xnhk0102> xnhk0102Map = xnhk0102s.stream().collect(Collectors.toMap(Xnhk0102::getSeccode, Function.identity(), (o, v) -> v));
        // List<StockDefine> stockDefines = stockDefineMapper.selectList(null);
        List<StockDefine> stockDefines = stockDefineService.listStockColumns(null);
        Map<String, StockDefine> stockDefineMap = stockDefines.stream().collect(Collectors.toMap(StockDefine::getCode, Function.identity(), (o, v) -> v));
//        List<Xnhks0104> xnhks0104s = xnhks0104Mapper.selectList(null);
        //获得筛选出来的股票code集合
//        Set<String> codeSet = stockList.stream().map(item -> item.getSeccode()).collect(Collectors.toSet());
        Set<String> codeSet = brokerCommonUtils.getHoldingCodeSet(brokerId,endDate,industryName,code);
        if(CollectionUtils.isEmpty(codeSet)){
            return null;
        }
        Map<String, String> industryNameAndCode = this.getIndustryCodeAndName(null).stream().collect(Collectors.toMap(IndustrySearch::getIndustryName, IndustrySearch::getIndustryCode, (o, v) -> v));
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

        Map<String, ComStockSimpleDto> stockInfoMap = stockCache.queryStockInfoMap(new ArrayList<>(codeSet));
        //按股票code遍历赋值
//        System.out.println("开始时间："+System.currentTimeMillis());
        if(endDate.equals(LocalDate.now())){
            //包含今日
            //取出redis数据(list),并转化为map
            List<BrokerRedisField> brokerRedisFields = redisClient.get(BrokerConstants.BROKER_VALUE_LIST + brokerId);
            if(brokerRedisFields == null){
                return null;
            }
            Map<String, BrokerRedisField> redisMap = brokerRedisFields.stream().collect(Collectors.toMap(BrokerRedisField::getCode, Function.identity(), (o, v) -> v));
            for (String k : codeSet) {
                //每次遍历根据股票code再去筛选数据
                List<BrokerStatistics> brokerStatisticsList = stockList.stream().filter(item -> k.equals(item.getSeccode())).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(brokerStatisticsList)){
                    ShareholdingsTable shareholdingsTable = new ShareholdingsTable();
                    Xnhk0102 xnhk0102 = xnhk0102Map.get(k);
                    BrokerRedisField brokerRedisField = redisMap.get(k);
                    //股票代码
                    shareholdingsTable.setCode(k);
                    if (!stockInfoMap.containsKey(k)) {
                        log.info("getShareholdingsTable not cached code: {}", k);
                        continue;
                    }
                    ComStockSimpleDto simpleDto = stockInfoMap.get(k);
                    shareholdingsTable.setStockId(simpleDto.getStockId());
                    //股票名称
                    // StockDefine stockDefine = stockDefineMap.get(k);
                    shareholdingsTable.setStockName(simpleDto.getStockName());
                    //行业名称
//                Xnhks0104 xnhks0104 = xnhks0104s.stream().filter(item -> k.equals(item.getSeccode())).collect(Collectors.toList()).get(0);
//                shareholdingsTable.setIndustryName(xnhks0104 == null ? null : xnhks0104.getF014v());
                    shareholdingsTable.setIndustryName(brokerStatisticsList.get(0).getF014v());
                    shareholdingsTable.setIndustryCode(industryNameAndCode.get(brokerStatisticsList.get(0).getF014v()));
                    shareholdingsTable.setIndustryId(simpleDto.getIndustryId());
                    //持股变动股数
                    BigDecimal changeInShareholding = brokerRedisField.getNetTrade();
                    changeInShareholding = changeInShareholding == null ? BigDecimal.ZERO : changeInShareholding;
                    shareholdingsTable.setChangeInShareholding(changeInShareholding);
                    //变动比例
                    BigDecimal holdingYesterday = getNumberOfHolding(brokerStatisticsList,startDate);
                    BigDecimal changeRatio = BigDecimal.ZERO;
                    if(Objects.nonNull(holdingYesterday) && !(holdingYesterday.compareTo(BigDecimal.ZERO)==0)){
                        changeRatio = changeInShareholding.divide(holdingYesterday,4,RoundingMode.HALF_UP);
                    }
                    shareholdingsTable.setChangeRatio(changeRatio);
                    //持股数量
                    BigDecimal numberOfHolding = brokerRedisField.getNumberOfHolding();
                    numberOfHolding = numberOfHolding == null ? BigDecimal.ZERO : numberOfHolding;
                    shareholdingsTable.setNumberOfHolding(numberOfHolding);
//                    //持股比例(占流通股)
//                    BigDecimal flowShares = xnhk0102 == null ? null :xnhk0102.getF069n();
//                    BigDecimal holdingRatioInFlowShares = BigDecimal.ZERO;
//                    if(Objects.nonNull(numberOfHolding) && Objects.nonNull(flowShares) && !(flowShares.compareTo(BigDecimal.ZERO)==0)){
//                        holdingRatioInFlowShares = numberOfHolding.divide(flowShares,4,RoundingMode.HALF_UP);
//                    }
//                    shareholdingsTable.setHoldingRatioInFlowShares(holdingRatioInFlowShares);
                    //持股比例(占已发行普通股)
                    BigDecimal ordinaryShares = xnhk0102 == null ? null :xnhk0102.getF070n();
                    BigDecimal holdingRatioInOrdinaryShares = BigDecimal.ZERO;
                    if(Objects.nonNull(numberOfHolding) && Objects.nonNull(ordinaryShares) && !(ordinaryShares.compareTo(BigDecimal.ZERO)==0)){
                        holdingRatioInOrdinaryShares = numberOfHolding.divide(ordinaryShares,4,RoundingMode.HALF_UP);
                    }
                    shareholdingsTable.setHoldingRatioInOrdinaryShares(holdingRatioInOrdinaryShares);
                    //变动市值
                    BigDecimal todayMarketVal = brokerRedisField.getHoldingMarketValue();
                    BigDecimal historyMarketVal = getReferenceMarketValue(brokerStatisticsList,startDate,code);
                    BigDecimal changeMarketValue = BigDecimal.ZERO;
                    if(Objects.nonNull(todayMarketVal) && Objects.nonNull(historyMarketVal)){
                        changeMarketValue = todayMarketVal.subtract(historyMarketVal);
                    }
                    shareholdingsTable.setChangeMarketValue(changeMarketValue);
                    //参考持股市值
                    shareholdingsTable.setReferenceHoldingMarketValue(todayMarketVal);
                    //持股日期
                    shareholdingsTable.setHoldingDate(DateUtils.localDateToLong(LocalDate.now()));

                    shareholdingsTables.add(shareholdingsTable);
                }
            }
        }else {
            //不包含今日
            for (String k : codeSet) {
                //每次遍历根据股票code再去筛选数据
                List<BrokerStatistics> brokerStatisticsList = stockList.stream().filter(item -> k.equals(item.getSeccode())).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(brokerStatisticsList)){
                    ShareholdingsTable shareholdingsTable = new ShareholdingsTable();
                    Xnhk0102 xnhk0102 = xnhk0102Map.get(k);
                    //股票代码
                    shareholdingsTable.setCode(k);
                    if (!stockInfoMap.containsKey(k)) {
                        log.info("getShareholdingsTable not cached code: {}", k);
                        continue;
                    }
                    ComStockSimpleDto simpleDto = stockInfoMap.get(k);
                    shareholdingsTable.setStockId(simpleDto.getStockId());
                    //股票名称
                    // StockDefine stockDefine = stockDefineMap.get(k);
                    shareholdingsTable.setStockName(simpleDto.getStockName());
                    //行业名称
//                Xnhks0104 xnhks0104 = xnhks0104s.stream().filter(item -> k.equals(item.getSeccode())).collect(Collectors.toList()).get(0);
//                shareholdingsTable.setIndustryName(xnhks0104 == null ? null : xnhks0104.getF014v());
                    shareholdingsTable.setIndustryName(brokerStatisticsList.get(0).getF014v());
                    shareholdingsTable.setIndustryCode(industryNameAndCode.get(brokerStatisticsList.get(0).getF014v()));
                    shareholdingsTable.setIndustryId(simpleDto.getIndustryId());
                    //持股变动股数
                    shareholdingsTable.setChangeInShareholding(getChangeInShareholding(brokerStatisticsList,beforeDate,endDate,isDaily));
                    //变动比例
                    shareholdingsTable.setChangeRatio(calcCashScale(getChangeRatio(brokerStatisticsList,beforeDate,endDate,isDaily),BigDecimal.valueOf(100),6));
                    //持股数量
                    shareholdingsTable.setNumberOfHolding(getNumberOfHolding(brokerStatisticsList,endDate));
//                    //持股比例(占流通股)
//                    shareholdingsTable.setHoldingRatioInFlowShares(getHoldingRatioInFlowShares(brokerStatisticsList,xnhk0102,endDate));
                    //持股比例(占已发行普通股)
                    shareholdingsTable.setHoldingRatioInOrdinaryShares(getHoldingRatioInOrdinaryShares(brokerStatisticsList,xnhk0102,endDate));
                    //变动市值
                    shareholdingsTable.setChangeMarketValue(getChangeMarketValue(brokerStatisticsList,beforeDate,endDate,k));
                    //参考持股市值
                    shareholdingsTable.setReferenceHoldingMarketValue(getReferenceMarketValue(brokerStatisticsList,endDate,k));
                    //持股日期
                    shareholdingsTable.setHoldingDate(getHoldingDate(brokerStatisticsList));

                    shareholdingsTables.add(shareholdingsTable);
                }
            }
        }
//        System.out.println("结束时间："+System.currentTimeMillis());
        if(StringUtils.isBlank(sortKey) || StringUtils.isBlank(sort)){
            //默认按交易日期和持股比例(占已发行普通股)倒序排列
            shareholdingsTables = shareholdingsTables.stream()
                    .sorted(Comparator.comparing(ShareholdingsTable::getHoldingDate).reversed()
                            .thenComparing(ShareholdingsTable::getHoldingRatioInOrdinaryShares).reversed())
                    .collect(Collectors.toList());
        }else{
            //根据指定字段进行排序
            sort(shareholdingsTables, sortKey, sort);
        }

        return shareholdingsTables;
    }

    @Override
    public BrokerViewRank getRankTopFiveList(String brokerId, String industryCode,
                                                         String code, LocalDate startDate, LocalDate endDate){
        //依据行业code获取行业名称
        // Xnhk0004 xnhk0004 = xnhk0004Mapper.selectOne(new QueryWrapper<Xnhk0004>().eq("code", industryCode).select("code","f001v"));
        // String industryName = xnhk0004 == null ? null : xnhk0004.getF001v();
        IndustrySubsidiary oneIndustry = industrySubsidiaryService.getOneIndustry(industryCode);
        String industryName = oneIndustry == null ? null : oneIndustry.getName();
        List<BrokerStatistics> stockList = getStockList(brokerId,industryName,code,startDate,endDate);
        if(CollectionUtils.isEmpty(stockList)){
            return null;
        }
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
        //获得筛选出来的股票code集合
        Set<String> codeSet = brokerCommonUtils.getHoldingCodeSet(brokerId,endDate,industryName,code);
        if(CollectionUtils.isEmpty(codeSet)){
            return null;
        }
//      Set<String> codeSet = stockList.stream().map(item -> item.getSeccode()).collect(Collectors.toSet());


        //待排序的集合
        List<StockToRank> changeRatioRank = new ArrayList<>();
        List<StockToRank> holdingRatioInOrdinarySharesRank = new ArrayList<>();
        List<StockToRank> changeMarketValueRank = new ArrayList<>();
        List<StockToRank> referenceHoldingMarketValueRank = new ArrayList<>();

        long l1 = System.currentTimeMillis();
        //将股票code和需排行的值包装成List
        List<Xnhk0102> xnhk0102s = xnhk0102Mapper.selectList(new QueryWrapper<Xnhk0102>().select("seccode","f070n"));
        Map<String, Xnhk0102> xnhk0102Map = xnhk0102s.stream().collect(Collectors.toMap(Xnhk0102::getSeccode, Function.identity()));
        if(endDate.equals(LocalDate.now())){
            //包含今日
            //取出redis数据(list),并转化为map
            List<BrokerRedisField> brokerRedisFields = redisClient.get(BrokerConstants.BROKER_VALUE_LIST + brokerId);
            if(CollectionUtils.isEmpty(brokerRedisFields)){
                return null;
            }
            Map<String, BrokerRedisField> redisMap = brokerRedisFields.stream().collect(Collectors.toMap(BrokerRedisField::getCode, Function.identity()));
            for (String k : codeSet) {
                List<BrokerStatistics> brokerStatisticsList = stockList.stream().filter(item -> {
                    return k.equals(item.getSeccode());
                }).collect(Collectors.toList());
                Xnhk0102 xnhk0102 = xnhk0102Map.get(k);
                BrokerRedisField brokerRedisField = redisMap.get(k);

                //取需排序的字段数值（区间末那天的数值）
                //变动比例
                BigDecimal holdingYesterday = getNumberOfHolding(brokerStatisticsList,startDate);
                holdingYesterday = holdingYesterday == null ? BigDecimal.ZERO : holdingYesterday;
                BigDecimal changeRatio = BigDecimal.ZERO;
                BigDecimal shares = xnhk0102 == null ? null :xnhk0102.getF070n();
                if(isDaily){
                    //今日持股变动比例=100%*今日变动数/今日已发行普通股数
                    BigDecimal changeInShareholding = brokerRedisField == null ? BigDecimal.ZERO : brokerRedisField.getNetTrade();
                    if(Objects.nonNull(changeInShareholding) && Objects.nonNull(shares) && !(shares.compareTo(BigDecimal.ZERO)==0)){
                        changeRatio = changeInShareholding.divide(shares,6,RoundingMode.HALF_UP);
                    }
                }else {
                    //区间持股变动比例=区间末持股比例-区间初前一日的持股比例
                    BigDecimal holdingRatioInFlowShares = brokerRedisField == null ? BigDecimal.ZERO : brokerRedisField.getHoldingRatioInFlowShares();
                    if(Objects.nonNull(holdingRatioInFlowShares) && Objects.nonNull(holdingYesterday)){
                        changeRatio = holdingRatioInFlowShares.subtract(holdingYesterday);
                    }
                }
                //持股数量
                BigDecimal numberOfHolding = brokerRedisField == null ? BigDecimal.ZERO : brokerRedisField.getNumberOfHolding();
                numberOfHolding = numberOfHolding == null ? BigDecimal.ZERO : numberOfHolding;
                //持股比例(占已发行普通股)
                BigDecimal holdingRatioInOrdinaryShares = BigDecimal.ZERO;
                if(Objects.nonNull(numberOfHolding) && Objects.nonNull(shares) && !(shares.compareTo(BigDecimal.ZERO)==0)){
                    holdingRatioInOrdinaryShares = numberOfHolding.divide(shares,4,RoundingMode.HALF_UP);
                }
                //变动市值
                BigDecimal referenceMarketValue = brokerRedisField == null ? BigDecimal.ZERO : brokerRedisField.getHoldingMarketValue();
                BigDecimal historyMarketVal = getReferenceMarketValue(brokerStatisticsList,startDate,code);
                BigDecimal changeMarketValue = BigDecimal.ZERO;
                if(Objects.nonNull(referenceMarketValue) && Objects.nonNull(historyMarketVal)){
                    changeMarketValue = referenceMarketValue.subtract(historyMarketVal);
                }

                //取需排序的字段数值（区间末那天的数值）
//                BigDecimal changeRatio = getChangeRatio(brokerStatisticsList, beforeDate, endDate, isDaily);
//                BigDecimal holdingRatioInFlowShares = getHoldingRatioInFlowShares(brokerStatisticsList, xnhk0102, endDate);
//                BigDecimal changeMarketValue = getChangeMarketValue(brokerStatisticsList, startDate, endDate, k);
//                BigDecimal referenceMarketValue = getReferenceMarketValue(brokerStatisticsList, endDate, k);

                changeRatioRank.add(StockToRank.builder().code(k)
                        .rankValue(changeRatio == null ? BigDecimal.ZERO : changeRatio).build());
                holdingRatioInOrdinarySharesRank.add(StockToRank.builder().code(k)
                        .rankValue(holdingRatioInOrdinaryShares == null ? BigDecimal.ZERO : holdingRatioInOrdinaryShares).build());
                changeMarketValueRank.add(StockToRank.builder().code(k)
                        .rankValue(changeMarketValue == null ? BigDecimal.ZERO : changeMarketValue).build());
                referenceHoldingMarketValueRank.add(StockToRank.builder().code(k)
                        .rankValue(referenceMarketValue == null ? BigDecimal.ZERO : referenceMarketValue).build());
            }
        }else{
            //不含今日
            for (String k : codeSet) {
                Xnhk0102 xnhk0102 = xnhk0102Map.get(k);
                List<BrokerStatistics> brokerStatisticsList = stockList.stream().filter(item -> {
                    return k.equals(item.getSeccode());
                }).collect(Collectors.toList());
                //取需排序的字段数值（区间末那天的数值）
                BigDecimal changeRatio = getChangeRatio(brokerStatisticsList, beforeDate, endDate, isDaily);
                BigDecimal holdingRatioInOrdinaryShares = getHoldingRatioInOrdinaryShares(brokerStatisticsList, xnhk0102, endDate);
                BigDecimal changeMarketValue = getChangeMarketValue(brokerStatisticsList, startDate, endDate, k);
                BigDecimal referenceMarketValue = getReferenceMarketValue(brokerStatisticsList, endDate, k);

                changeRatioRank.add(StockToRank.builder().code(k)
                        .rankValue(changeRatio == null ? BigDecimal.ZERO : changeRatio).build());
                holdingRatioInOrdinarySharesRank.add(StockToRank.builder().code(k)
                        .rankValue(holdingRatioInOrdinaryShares == null ? BigDecimal.ZERO : holdingRatioInOrdinaryShares).build());
                changeMarketValueRank.add(StockToRank.builder().code(k)
                        .rankValue(changeMarketValue == null ? BigDecimal.ZERO : changeMarketValue).build());
                referenceHoldingMarketValueRank.add(StockToRank.builder().code(k)
                        .rankValue(referenceMarketValue == null ? BigDecimal.ZERO : referenceMarketValue).build());
            }
        }
        log.info("==================获取排序数值时间:{}===================",System.currentTimeMillis()-l1);
        //进行排序并取出前5
        List<String> codeList1 = getTopFiveCode(changeRatioRank);
        List<String> codeList2 = getTopFiveCode(holdingRatioInOrdinarySharesRank);
        List<String> codeList3 = getTopFiveCode(changeMarketValueRank);
        List<String> codeList4 = getTopFiveCode(referenceHoldingMarketValueRank);
        log.info("==================排序时间:{}===================",System.currentTimeMillis()-l1);

        long l2 = System.currentTimeMillis();
        // List<StockDefine> stockDefines = stockDefineMapper.selectList(null);
        List<StockDefine> stockDefines = stockDefineService.listStockColumns(null);
        Map<String, StockDefine> stockDefineMap = stockDefines.stream().collect(Collectors.toMap(StockDefine::getCode, Function.identity()));
        //选择单日默认展示一年的数据
        LocalDate startDotDate = startDate;
        if(isDaily){
            startDotDate = endDate.plusDays(-364);
        }
        //获取打点日期
        List<LocalDate> dotDateList = getDotDate(startDotDate, endDate,code);
        List<Long> dotDatesLong = dotDateList.stream().map(item -> Long.parseLong(item.toString().replace("-", ""))).collect(Collectors.toList());
        List<String> codeList = Lists.newArrayList();
        codeList.addAll(codeList1);
        codeList.addAll(codeList2);
        codeList.addAll(codeList3);
        codeList.addAll(codeList4);
        List<BrokerStatistics> brokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("f002v",brokerId).in("f001d",dotDatesLong).in("seccode",codeList));
        BrokerViewRank brokerViewRank = BrokerViewRank
                .builder()
                .changeRatioTopFive(getTopFiveValue(codeList1,"holdingNum",xnhk0102Map,stockDefineMap,dotDateList,brokerStatisticsList,false))
                .holdingRatioTopFive(getTopFiveValue(codeList2,"holdingNum",xnhk0102Map,stockDefineMap,dotDateList,brokerStatisticsList,true))
                .changeMarketValueTopFive(getTopFiveValue(codeList3,"marketValue",xnhk0102Map,stockDefineMap,dotDateList,brokerStatisticsList,false))
                .holdingMarketValueTopFive(getTopFiveValue(codeList4,"marketValue",xnhk0102Map,stockDefineMap,dotDateList,brokerStatisticsList,true))
                .build();
        return brokerViewRank;
    }

    @Override
    public StockAndHoldingRatio getHoldingRatioTrend(String brokerId, String code, LocalDate startDate, LocalDate endDate,Boolean isDaily) {
        //开盘前时间处理
        endDate = brokerCommonUtils.processBeforeOpening(endDate);
        //交易日处理
        //区间前一日
        if(startDate == null){
            //单日的情况
            endDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
            isDaily = true;
        }else {
            //区间的情况
            endDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
            startDate = hkTradingCalendarApi.isTradingDay(startDate) ? startDate : hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
            if(startDate.equals(endDate)) {
                startDate = hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
            }
        }
        List<String> codeList = Lists.newArrayList(code);
        //将股票code和需排行的值包装成List
        List<Xnhk0102> xnhk0102s = xnhk0102Mapper.selectList(new QueryWrapper<Xnhk0102>().select("seccode","f070n"));
        Map<String, Xnhk0102> xnhk0102Map = xnhk0102s.stream().collect(Collectors.toMap(Xnhk0102::getSeccode, Function.identity()));
        // List<StockDefine> stockDefines = stockDefineMapper.selectList(null);
        List<StockDefine> stockDefines = stockDefineService.listStockColumns(null);
        Map<String, StockDefine> stockDefineMap = stockDefines.stream().collect(Collectors.toMap(StockDefine::getCode, Function.identity()));
        //选择单日默认展示一年的数据
        LocalDate startDotDate = startDate;
        if(isDaily){
            startDotDate = endDate.plusDays(-364);
        }
        //获取打点日期
        List<LocalDate> dotDateList = getDotDate(startDotDate, endDate,code);
        if(CollectionUtils.isEmpty(dotDateList)){
            StockDefine stockDefine = stockDefineMapper.selectOne(new QueryWrapper<StockDefine>().eq("code", code));
            String stockName = stockDefine == null ? null : stockDefine.getStockName();
            StockAndHoldingRatio stockAndHoldingRatio = StockAndHoldingRatio.builder().code(code).stockName(stockName).build();
            return stockAndHoldingRatio;
        }
        List<Long> dotDatesLong = dotDateList.stream().map(item -> Long.parseLong(item.toString().replace("-", ""))).collect(Collectors.toList());
        List<BrokerStatistics> brokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("f002v",brokerId).in("f001d",dotDatesLong).in("seccode",codeList));
        List<StockAndHoldingRatio> stockAndHoldingRatios = getTopFiveValue(codeList, "holdingNum", xnhk0102Map, stockDefineMap, dotDateList, brokerStatisticsList,false);
        StockAndHoldingRatio stockAndHoldingRatio = CollectionUtils.isEmpty(stockAndHoldingRatios) ? new StockAndHoldingRatio() : stockAndHoldingRatios.get(0);
//        fillClose(stockAndHoldingRatio.getValueAndDateList(), code);


//        List<ValueAndDate> valueAndDateList = new ArrayList<>();
//        //选择单日默认展示一年的数据
//        if(isDaily){
//            startDate = endDate.plusDays(-365);
//        }
//        //获取打点日期
//        List<LocalDate> dotDateList = getDotDate(startDate, endDate,code);
//        Xnhk0102 xnhk0102 = xnhk0102Mapper.selectOne(new QueryWrapper<Xnhk0102>().eq("seccode", code));
//        List<Long> dotDatesLong = dotDateList.stream().map(item -> Long.parseLong(item.toString().replace("-", ""))).collect(Collectors.toList());
//        List<BrokerStatistics> brokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
//                .eq("f002v",brokerId).eq("seccode",code).in("f001d",dotDatesLong));
//        for (LocalDate dotDate : dotDateList) {
//            valueAndDateList.add(ValueAndDate.builder()
//                    .val(getHoldingRatioInOrdinaryShares(brokerStatisticsList,xnhk0102,dotDate))
//                    .date(dotDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli())
//                    .build());
//        }
//        StockDefine stockDefine = stockDefineMapper.selectOne(new QueryWrapper<StockDefine>().eq("code", code));
//        String stockName = stockDefine == null ? null : stockDefine.getStockName();
//        StockAndHoldingRatio stockAndHoldingRatio = StockAndHoldingRatio.builder().code(code).stockName(stockName).valueAndDateList(valueAndDateList).build();
        return stockAndHoldingRatio;
    }

    @Override
    public StockAndHoldingRatio getStockMarketValue(String brokerId, String code, LocalDate startDate, LocalDate endDate, Boolean isDaily) {
        List<ValueAndDate> valueAndDateList = new ArrayList<>();
        //选择单日默认展示一年的数据
        if(isDaily){
            startDate = endDate.plusDays(-364);
        }
        //获取打点日期
        List<LocalDate> dotDateList = getDotDate(startDate, endDate,code);
        if(CollectionUtils.isEmpty(dotDateList)){
            StockDefine stockDefine = stockDefineMapper.selectOne(new QueryWrapper<StockDefine>().eq("code", code));
            String stockName = stockDefine == null ? null : stockDefine.getStockName();
            StockAndHoldingRatio stockAndHoldingRatio = StockAndHoldingRatio.builder().code(code).stockName(stockName).build();
            return stockAndHoldingRatio;
        }
        List<Long> dotDatesLong = dotDateList.stream().map(item -> Long.parseLong(item.toString().replace("-", ""))).collect(Collectors.toList());
        List<BrokerStatistics> brokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("f002v",brokerId).eq("seccode",code).in("f001d",dotDatesLong));
        for (LocalDate dotDate : dotDateList) {
            BigDecimal referenceMarketValue = getReferenceMarketValue(brokerStatisticsList, dotDate, code);
            valueAndDateList.add(ValueAndDate.builder()
                    .val(referenceMarketValue.compareTo(BigDecimal.ZERO) == 0 ? null : referenceMarketValue)
                    .date(dotDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli())
                    .build());
        }
        StockDefine stockDefine = stockDefineMapper.selectOne(new QueryWrapper<StockDefine>().eq("code", code));
        String stockName = stockDefine == null ? null : stockDefine.getStockName();
        StockAndHoldingRatio stockAndHoldingRatio = StockAndHoldingRatio.builder().code(code).stockName(stockName).valueAndDateList(valueAndDateList).build();
//        fillClose(stockAndHoldingRatio.getValueAndDateList(), code);
        return stockAndHoldingRatio;
    }

    @Override
    public List<MarketValueTrend> getMarketValueTrend(String brokerId, String industryCode, String code, LocalDate startDate, LocalDate endDate) {
        //开盘前时间处理
        endDate = brokerCommonUtils.processBeforeOpening(endDate);
        //获取打点日期
        LocalDate dotStartDate = startDate;
        LocalDate dotEndDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
        if(Objects.isNull(dotStartDate)){
            dotStartDate = dotEndDate.plusDays(-364);
        }
        List<LocalDate> dotDates = getDotDate(dotStartDate, dotEndDate,code);

        //依据行业code获取行业名称
        // Xnhk0004 xnhk0004 = xnhk0004Mapper.selectOne(new QueryWrapper<Xnhk0004>().eq("code", industryCode));
        // String industryName = xnhk0004 == null ? null : xnhk0004.getF001v();
        IndustrySubsidiary oneIndustry = industrySubsidiaryService.getOneIndustry(industryCode);
        String industryName = oneIndustry == null ? null : oneIndustry.getName();
//        //按经纪商代码、行业名称以及股票代码筛选
//        List<BrokerStatistics> stockList = getStockList(brokerId,industryName,code,startDate,endDate);
//        if(CollectionUtils.isEmpty(stockList)){
//            return null;
//        }
        //交易日处理
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
        //获得筛选出来的股票code集合
//        Set<String> codeSet = stockList.stream().map(item -> item.getSeccode()).collect(Collectors.toSet());
        Set<String> codeSet = brokerCommonUtils.getHoldingCodeSet(brokerId,endDate,industryName,code);
        if(CollectionUtils.isEmpty(codeSet)){
            return null;
        }
        Long endDateLong = DateUtils.localDateToF001D(endDate);
        //如果未选择行业，默认筛选前10的行业
        List<String> top10Industry = Lists.newArrayList();
        //如果选择了股票code，优先股票所在行业
        if(StringUtils.isNotEmpty(code)){
            IndustrySubsidiary subsidiary = industrySubsidiaryService.getStockIndustry(code);
            // Xnhks0104 xnhks0104 = xnhks0104Mapper.selectOne(new QueryWrapper<Xnhks0104>().eq("SECCODE", code));
            if(subsidiary != null){
                industryName = subsidiary.getName();
            }
        }
        if(StringUtils.isNotEmpty(industryName)){
            top10Industry.add(industryName);
        }else {
            top10Industry = brokerIndustryStatisticsMapper.getTopIndustry(brokerId, endDateLong);
        }
        if(CollectionUtils.isEmpty(top10Industry)){
            return new ArrayList<>();
        }
        List<IndustrySubsidiary> subsidiaries = industrySubsidiaryService.getAllIndustry();
        ArrayList<String> filterList = ListUtil.toList(top10Industry);
        subsidiaries = CollUtil.filter(subsidiaries, sub -> filterList.contains(sub.getName()));
        // List<IndustrySubsidiary> subsidiaries = industrySubsidiaryService.list(new QueryWrapper<IndustrySubsidiary>().in("name", top10Industry));
        Map<String, String> industryCodeAndNameMap = subsidiaries.stream().collect(Collectors.toMap(IndustrySubsidiary::getCode, IndustrySubsidiary::getName));
        log.info("dotDates:{}", dotDates);
        LocalDate minLocalDate = dotDates.stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxLocalDate = dotDates.stream().max(LocalDate::compareTo).orElse(LocalDate.now());
        Map<String, BigDecimal> industryTotalMarketMap = industryDailyKlineService.query()
                .select("code, time, total_market")
                .in("code", industryCodeAndNameMap.keySet())
                .between("time", LocalDateTimeUtil.getTimestamp(minLocalDate), LocalDateTimeUtil.getTimestamp(maxLocalDate))
                .list()
                .stream()
                .collect(Collectors.toMap(item->industryCodeAndNameMap.get(item.getCode()) + item.getTime(), IndustryDailyKline::getTotalMarket));
        //取得前10行业的走势图
        List<Long> dotDatesLong = dotDates.stream().map(item -> Long.parseLong(item.toString().replace("-", ""))).collect(Collectors.toList());
        List<BrokerIndustryStatistics> industryStatistics = brokerIndustryStatisticsMapper.getIndustryStatistics(brokerId, dotDatesLong, top10Industry);
        Map<String, List<BrokerIndustryStatistics>> industryMap = industryStatistics.stream().collect(Collectors.groupingBy(BrokerIndustryStatistics::getIndustryName));
        List<MarketValueTrend> result = new ArrayList<>();
        Map<String, Long> stockCodeIdMap = stockCache.queryStockIdMap(null);
        Map<String, String> stockNameMap = MapUtil.reverse(stockCache.queryStockNameMap(null));
        for (String k : top10Industry) {
            List<BrokerIndustryStatistics> industryVal = industryMap.get(k);
            if (!CollectionUtils.isEmpty(industryVal)) {
                List<MarketValueTrend.MarketValueAndDate> valueAndDateList = industryVal.stream().map(item -> {
                    Long date = DateUtils.longDateToLongMS(item.getF001d());
                    BigDecimal totalMarket = industryTotalMarketMap.get(k + date);
                    if (totalMarket != null && totalMarket.compareTo(BigDecimal.ZERO) == 0) {
                        totalMarket = null;
                    }
                    return MarketValueTrend.MarketValueAndDate.builder().val(item.getMarketVal()).date(date).totalMarketValue(totalMarket).build();
                }).collect(Collectors.toList());
                MarketValueTrend marketValueTrend = MarketValueTrend.builder().industryName(k).valueAndDateList(valueAndDateList).build();
                if (stockNameMap.containsKey(k)) {
                    marketValueTrend.setIndustryCode(stockNameMap.get(k));
                    marketValueTrend.setStockId(stockCodeIdMap.get(marketValueTrend.getIndustryCode()));
                }
                result.add(marketValueTrend);
            }
        }
        return result;

//        //筛选出持股市值前10的行业
//        List<StockMarketValue> marketValues = new ArrayList<>();
//        List<BrokerRedisField> brokerRedisFields = redisClient.get(BrokerConstants.BROKER_VALUE_LIST + brokerId);
//        //取出redis数据(list)，并转化为list
//        if(endDate.equals(LocalDate.now())){
//            if(CollectionUtils.isEmpty(brokerRedisFields)){
//                return null;
//            }
//            Map<String, BrokerRedisField> redisMap = brokerRedisFields.stream().collect(Collectors.toMap(BrokerRedisField::getCode, Function.identity()));
//            //包含今日
//            for (String k : codeSet) {
//                BrokerRedisField brokerRedisField = redisMap.get(k);
//                BigDecimal marketValue = brokerRedisField == null ? BigDecimal.ZERO : brokerRedisField.getHoldingMarketValue();
//                String industry = stockList.stream().filter(item -> k.equals(item.getSeccode())).collect(Collectors.toList()).get(0).getF014v();
//                marketValues.add(StockMarketValue.builder()
//                        .industryName(industry)
//                        .marketValue(marketValue).build());
//            }
//        }else{
//            //不含今日
////            List<BrokerStatistics> brokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>().eq("f002v", brokerId).eq("f001d", DateUtils.localDateToF001D(endDate)));
//            Long f001D = DateUtils.localDateToF001D(endDate);
//            List<BrokerStatistics> brokerStatisticsList = stockList.stream().filter(item -> f001D.equals(item.getF001d())).collect(Collectors.toList());
//            Map<String, BigDecimal> marketMap = CollectionUtils.isEmpty(brokerStatisticsList) ? new HashMap<>() : brokerStatisticsList.stream().collect(Collectors.toMap(BrokerStatistics::getSeccode, BrokerStatistics::getMarketVal));
//            for (String k : codeSet) {
//                //BigDecimal marketValue = getReferenceMarketValue(stockList, endDate, k);
//                BigDecimal marketValue = marketMap.get(k);
//                marketValue = marketValue == null ? BigDecimal.ZERO : marketValue;
//                String industry = stockList.stream().filter(item -> k.equals(item.getSeccode())).collect(Collectors.toList()).get(0).getF014v();
//                marketValues.add(StockMarketValue.builder()
//                        .industryName(industry)
//                        .marketValue(marketValue).build());
//            }
//        }
//        if(marketValues.size() == 0){
//            return null;
//        }
//        //按照行业进行分组（先过滤掉行业为空的数据）
//        marketValues = marketValues.stream().filter(item -> Objects.nonNull(item.getIndustryName())).collect(Collectors.toList());
//        Map<String, List<StockMarketValue>> industryMap = marketValues.stream().collect(Collectors.groupingBy(StockMarketValue::getIndustryName));
//        List<StockMarketValue> beforeRankList = new ArrayList<>();
//        industryMap.forEach((k,v) -> {
//            List<StockMarketValue> oneIndustryList = industryMap.get(k);
//            BigDecimal marketValue = oneIndustryList.stream().map(item -> item.getMarketValue()).reduce(BigDecimal.ZERO,BigDecimal::add);
//            beforeRankList.add(StockMarketValue.builder().industryName(k).marketValue(marketValue).build());
//        });
//        List<StockMarketValue> rankList = beforeRankList.stream().sorted(Comparator.comparing(StockMarketValue::getMarketValue).reversed()).limit(10).collect(Collectors.toList());
//
//        //返回前10行业的持股市值走势图数据
//        //将打点日期转换为long类型,以此去数据库获取打点日期的数据
//        List<Long> dotDatesLong = dotDates.stream().map(item -> Long.parseLong(item.toString().replace("-", ""))).collect(Collectors.toList());
//        List<BrokerStatistics> dataList = brokerStatisticsMapper.getBrokerStatisticsByDateAndId(dotDatesLong, brokerId,codeSet);
//        Map<Long, List<BrokerStatistics>> dataMap = dataList.stream().collect(Collectors.groupingBy(BrokerStatistics::getF001d));
//
//        List<MarketValueTrend> marketValueTrends= new ArrayList<>();
//        for (StockMarketValue stockMarketValue : rankList) {
//            String industry = stockMarketValue.getIndustryName();
//            List<ValueAndDate> valueAndDateList = new ArrayList<>();
//            //筛选出当前行业下的股票集合
//            Set<String> collect = stockList.stream().filter(item -> industry.equals(item.getF014v())).map(item -> item.getSeccode()).collect(Collectors.toSet());
//            for (LocalDate dotDate : dotDates) {
//                BigDecimal marketValue = null;
//                if(dotDate.equals(LocalDate.now())){
//                    if(CollectionUtils.isEmpty(brokerRedisFields)){
//                        continue;
//                    }
//                    //今日数据从redis拿
//                    marketValue = brokerRedisFields.stream().filter(item -> collect.contains(item.getCode())).map(item -> item.getHoldingMarketValue()).reduce(BigDecimal.ZERO,BigDecimal::add);
//                }else {
//                    Long dotDateLong = Long.parseLong(dotDate.toString().replace("-", ""));
//                    List<BrokerStatistics> brokerStatisticsList = dataMap.get(dotDateLong);
//                    marketValue = CollectionUtils.isEmpty(brokerStatisticsList) ? null : brokerStatisticsList.stream().filter(item -> collect.contains(item.getSeccode())).map(item -> item.getMarketVal()).reduce(BigDecimal.ZERO,BigDecimal::add);
//                    //marketValue = dataList.stream().filter(item -> collect.contains(item.getSeccode()) && dotDateLong.equals(item.getF001d())).map(item -> item.getMarketVal()).reduce(BigDecimal.ZERO,BigDecimal::add);
//                }
//                valueAndDateList.add(new ValueAndDate(marketValue,dotDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli(),null));
//            }
//            marketValueTrends.add(MarketValueTrend.builder().industryName(industry).valueAndDateList(valueAndDateList).build());
//        }
//        if(CollectionUtils.isEmpty(marketValueTrends)){
//            return null;
//        }
//        return marketValueTrends;
    }

    /**
     * 获得打点日期(区间初末+中间6个交易日)，区间不大于8个交易日全都要
     * @param startDate
     * @param endDate
     * @return
     */
    List<LocalDate> getDotDate(LocalDate startDate,LocalDate endDate,String code){
        endDate = hkTradingCalendarApi.isTradingDay(endDate) ? endDate : hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
        startDate = hkTradingCalendarApi.isTradingDay(startDate) ? startDate : hkTradingCalendarApi.getBeforeTradingCalendar(startDate).getDate();
        if(endDate.equals(startDate)){
            startDate = hkTradingCalendarApi.getBeforeTradingCalendar(endDate).getDate();
        }
        //比较开始日期与上市日期
        Xnhks0101 xnhks0101 = xnhks0101Mapper.selectOne(new QueryWrapper<Xnhks0101>().select("seccode", "f006d").eq("seccode", code));
        if(xnhks0101 != null && xnhks0101.getF006d() != null){
            Long f006d = xnhks0101.getF006d();
            LocalDate listDate = DateUtils.f001dToLocalDate(f006d);
            startDate = listDate.isBefore(startDate) ? startDate : listDate;
        }
        if(endDate.isBefore(startDate)){
            return new ArrayList<>();
        }
        ResultT<Integer> betweenTradingDays = hkTradingCalendarApi.getBetweenTradingDays(startDate, endDate);
        Integer betweenData = betweenTradingDays.getData();
        List<LocalDate> dotDateList;
        if(Objects.isNull(betweenData)){
            log.info("该区间没有数据");
            return null;
        }else if(betweenData <= 7){
            //不足8个交易日，全部输出
            dotDateList = new ArrayList<>();
            for(LocalDate dotDate = startDate;!dotDate.equals(endDate);dotDate = hkTradingCalendarApi.getNextTradingCalendar(dotDate).getDate()){
                dotDateList.add(dotDate);
            }
            dotDateList.add(endDate);
        }else {
            //超过8个交易日
            dotDateList = new ArrayList<>();
            int interval = betweenData / 7;
            LocalDate dotDate = startDate;
            for (int i = 0; i < 6; i++) {
                dotDateList.add(dotDate);
                dotDate = hkTradingCalendarApi.queryAfterTradingCalendars(dotDate,interval);
            }
            dotDateList.add(dotDate);
            dotDateList.add(endDate);
        }
        return dotDateList;
    }



    /**
     * 提取前5的股票代码
     * @param toRankList  未排序的集合
     * @return
     */
    private List<String> getTopFiveCode(List<StockToRank> toRankList){
        if(CollectionUtils.isEmpty(toRankList)){
            return null;
        }
        //按排序字段的绝对值降序排序(选出前五)
        toRankList.forEach(item -> {
            item.setRankValue(item.getRankValue() == null ? BigDecimal.ZERO : item.getRankValue().abs());
        });
        List<String> codeList = toRankList.stream().sorted(Comparator.comparing(StockToRank::getRankValue).reversed()).limit(5).map(item -> item.getCode()).collect(Collectors.toList());
        return codeList;
    }

    /**
     * 获得排行前5的持股比例或持股市值数据
     * @param codeList
     * @param valueField
     * @param xnhk0102Map
     * @param stockDefineMap
     * @param dotDateList
     * @param brokerStatisticsList
     * @return
     */
    private List<StockAndHoldingRatio> getTopFiveValue(List<String> codeList, String valueField, Map<String, Xnhk0102> xnhk0102Map,
                                                       Map<String, StockDefine> stockDefineMap, List<LocalDate> dotDateList, List<BrokerStatistics> brokerStatisticsList, Boolean isSorted){
        if(CollectionUtils.isEmpty(codeList)){
            return null;
        }
        Map<String, Long> stockCodeIdMap = stockCache.queryStockIdMap(codeList);
        List<StockAndHoldingRatio> stockAndHoldingRatios = new ArrayList<>();
        if("holdingNum".equals(valueField)){
            for (String code : codeList) {
                List<ValueAndDate> valueAndDateList = new ArrayList<>();
                Xnhk0102 xnhk0102 = xnhk0102Map.get(code);
                List<BrokerStatistics> dataList = brokerStatisticsList.stream().filter(item -> code.equals(item.getSeccode())).collect(Collectors.toList());
                for (LocalDate dotDate : dotDateList) {
                    BigDecimal holdingRatioInOrdinaryShares = getHoldingRatioInOrdinaryShares(dataList, xnhk0102, dotDate);
                    valueAndDateList.add(ValueAndDate.builder()
                            .val(holdingRatioInOrdinaryShares.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : holdingRatioInOrdinaryShares)
                            .date(dotDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli())
                            .build());
                }
                StockDefine stockDefine = stockDefineMap.get(code);
                String stockName = stockDefine == null ? null : stockDefine.getStockName();
                StockAndHoldingRatio stockAndHoldingRatio = StockAndHoldingRatio.builder().code(code).stockId(stockCodeIdMap.get(code)).stockName(stockName).valueAndDateList(valueAndDateList).build();
                stockAndHoldingRatios.add(stockAndHoldingRatio);
            }
        }else {
            for (String code : codeList) {
                List<ValueAndDate> valueAndDateList = new ArrayList<>();
                List<BrokerStatistics> dataList = brokerStatisticsList.stream().filter(item -> code.equals(item.getSeccode())).collect(Collectors.toList());
                for (LocalDate dotDate : dotDateList) {
                    BigDecimal referenceMarketValue = getReferenceMarketValue(dataList, dotDate, code);
                    valueAndDateList.add(ValueAndDate.builder()
                            .val(referenceMarketValue.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : referenceMarketValue)
                            .date(dotDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli())
                            .build());
                }
                StockDefine stockDefine = stockDefineMap.get(code);
                String stockName = stockDefine == null ? null : stockDefine.getStockName();
                StockAndHoldingRatio stockAndHoldingRatio = StockAndHoldingRatio.builder().code(code).stockId(stockCodeIdMap.get(code)).stockName(stockName).valueAndDateList(valueAndDateList).build();
                stockAndHoldingRatios.add(stockAndHoldingRatio);
            }
        }
        if(isSorted && !CollectionUtils.isEmpty(stockAndHoldingRatios)) {
            Integer index = stockAndHoldingRatios.get(0).getValueAndDateList().size() - 1;
            Comparator<StockAndHoldingRatio> comparator = (t1, t2) -> -(t1.getValueAndDateList().get(index).getVal().compareTo(t2.getValueAndDateList().get(index).getVal()));
            stockAndHoldingRatios = stockAndHoldingRatios.stream().sorted(comparator).collect(Collectors.toList());
        }
        return stockAndHoldingRatios;
    }

    /**
     * 按照经纪商代码、行业名称以及股票代码筛选经纪商基础数据(brokerStatistics)
     * @param brokerId
     * @param industryName
     * @param code
     * @return
     */
    public List<BrokerStatistics> getStockList(String brokerId, String industryName, String code,
                                                LocalDate startDate,LocalDate endDate){
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
//        List<StockDefine> stockDefines = stockDefineMapper.selectList(null);
//        List<String> codeList = stockDefines.stream().map(StockDefine::getCode).collect(Collectors.toList());
        List<String> codeList = stockDefineMapper.selectStockCodeList();
        stockList = stockList.stream().filter(item -> codeList.contains(item.getSeccode())).collect(Collectors.toList());
        return stockList;
    }

    /**
     * 经纪商持股列表按指定字段排序
     * @param shareholdingsTables   持股列表数据
     * @param sortKey   排序字段
     * @param sort  排序方式(asc:升序 desc:降序)
     */
    public void sort(List<ShareholdingsTable> shareholdingsTables, String sortKey, String sort) {
        List<String> fieldList = Arrays.stream(ShareholdingsTable.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        if (fieldList.contains(sortKey)) {
            try {
                Comparator<ShareholdingsTable> comparator = null;
                if ("industryName".equals(sortKey)) {
                    //按首字母排序
                    Comparator comparatorChina = Comparator.nullsLast(Collator.getInstance(Locale.CHINA));
                    comparator = Comparator.comparing(ShareholdingsTable::getIndustryName,comparatorChina);
                }else if("holdingDate".equals(sortKey)){
                    comparator = Comparator.comparing(ShareholdingsTable::getHoldingDate,Comparator.nullsLast(Long::compareTo));
                } else {
                    PropertyDescriptor descriptor = new PropertyDescriptor(sortKey, ShareholdingsTable.class);
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
                        shareholdingsTables.sort(comparator);
                    } else {
                        shareholdingsTables.sort(comparator.reversed());
                    }
                }
            } catch (Exception e) {
                log.error("排序失败", e);
            }
        }
    }

    /**
     * 获取持股变动数
     * @param brokerStatisticsList  由经纪商和股票两个维度筛选过的0609表数据
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @return
     */
    public BigDecimal getChangeInShareholding(List<BrokerStatistics> brokerStatisticsList,LocalDate startDate,LocalDate endDate,Boolean isDaily){
        if(CollectionUtils.isEmpty(brokerStatisticsList)){
            return BigDecimal.ZERO;
        }
        BigDecimal result = BigDecimal.ZERO;
        //单日或者区间
        if (isDaily){
            if(endDate.equals(LocalDate.now())){
                //今日净买卖股数
//                Double scoreToday = redisClient.getScore(brokerStatisticsList.get(0).getF002v(), "today" + brokerStatisticsList.get(0).getSeccode());
                Double scoreToday = redisClient.getScore(BrokerConstants.BROKER_TODAY + brokerStatisticsList.get(0).getF002v(), brokerStatisticsList.get(0).getSeccode());
                result = result.add(BigDecimal.valueOf(Objects.isNull(scoreToday) ? 0 : scoreToday));
            }else {
                result = subtractCalc(brokerStatisticsList,result,startDate,endDate,"changeInShareholding");
            }
        }else{
            if(endDate.equals(LocalDate.now())){
                result = subtractCalc(brokerStatisticsList,result,startDate,endDate,"changeInShareholding");
                //历史变动加上净买卖股数
//                Double scoreToday = redisClient.getScore(brokerStatisticsList.get(0).getF002v(), "today" + brokerStatisticsList.get(0).getSeccode());
                Double scoreToday = redisClient.getScore(BrokerConstants.BROKER_TODAY + brokerStatisticsList.get(0).getF002v(), brokerStatisticsList.get(0).getSeccode());
                result = result.add(BigDecimal.valueOf(Objects.isNull(scoreToday) ? 0 : scoreToday));
            }else {
                result = subtractCalc(brokerStatisticsList,result,startDate,endDate,"changeInShareholding");
            }
        }
        return result;
    }

    /**
     * 获取持股变动比例
     * @param brokerStatisticsList  由经纪商和股票两个维度筛选过的0609表数据
     * @param startDate 起始日期
     * @param endDate   结束日期
     * @return
     */
    public BigDecimal getChangeRatio(List<BrokerStatistics> brokerStatisticsList,LocalDate startDate,LocalDate endDate,Boolean isDaily){
        if(CollectionUtils.isEmpty(brokerStatisticsList)){
            return BigDecimal.ZERO;
        }
        BigDecimal result = BigDecimal.ZERO;
        //单日或者区间
        if (isDaily){
            if(endDate.equals(LocalDate.now())){
                //100%*今日变动数/前日持股数
//                Double scoreToday = redisClient.getScore(brokerStatisticsList.get(0).getF002v(), "today" + brokerStatisticsList.get(0).getSeccode());
                Double scoreToday = redisClient.getScore(BrokerConstants.BROKER_TODAY + brokerStatisticsList.get(0).getF002v(), brokerStatisticsList.get(0).getSeccode());
                BigDecimal score = BigDecimal.valueOf(Objects.isNull(scoreToday) ? 0 : scoreToday);
                BigDecimal holdingYesterday = getNumberOfHolding(brokerStatisticsList,startDate);
                if(Objects.nonNull(holdingYesterday) && !(holdingYesterday.compareTo(BigDecimal.ZERO)==0)){
                    result = result.add(score.divide(holdingYesterday,4,RoundingMode.HALF_UP));
                }
            }else {
                result = subtractCalc(brokerStatisticsList,result,startDate,endDate,"changeRatio");
            }
        }else{
            if(endDate.equals(LocalDate.now())){
                result = subtractCalc(brokerStatisticsList,result,startDate,endDate,"changeRatio");
                //历史变动加上今日持股变动比例
//                Double scoreToday = redisClient.getScore(brokerStatisticsList.get(0).getF002v(), "today" + brokerStatisticsList.get(0).getSeccode());
                Double scoreToday = redisClient.getScore(BrokerConstants.BROKER_TODAY + brokerStatisticsList.get(0).getF002v(), brokerStatisticsList.get(0).getSeccode());
                BigDecimal score = BigDecimal.valueOf(Objects.isNull(scoreToday) ? 0 : scoreToday);
                BigDecimal holdingYesterday = getNumberOfHolding(brokerStatisticsList,startDate);
                if(Objects.nonNull(holdingYesterday) && !(holdingYesterday.compareTo(BigDecimal.ZERO)==0)){
                    result = result.add(score.divide(holdingYesterday,4,RoundingMode.HALF_UP));
                }
            }else {
                result = subtractCalc(brokerStatisticsList,result,startDate,endDate,"changeRatio");
            }
        }
        return result;
    }

    /**
     * 获取持股数量
     * @param brokerStatisticsList  由经纪商和股票两个维度筛选过的0609表数据
     * @param endDate
     */
    public BigDecimal getNumberOfHolding(List<BrokerStatistics> brokerStatisticsList,LocalDate endDate){
        if(CollectionUtils.isEmpty(brokerStatisticsList)){
            return BigDecimal.ZERO;
        }
        BigDecimal result = null;
        Long endDateLong = Long.parseLong(endDate.toString().replace("-", ""));
        //今日持股数 = 前日持股数+今日净买卖股数
        if(endDate.equals(LocalDate.now())){
//            Double scoreToday = redisClient.getScore(BrokerConstants.BROKER_TODAY + brokerStatisticsList.get(0).getF002v(), brokerStatisticsList.get(0).getSeccode());
//            //今日净买卖数
//            BigDecimal score = BigDecimal.valueOf(Objects.isNull(scoreToday) ? 0 : scoreToday);
//            BigDecimal beforeHolding = BigDecimal.ZERO;
//            //取得前日持股数
//            for (BrokerStatistics brokerStatistics : brokerStatisticsList) {
//                if(endDateLong.equals(brokerStatistics.getF001d())){
//                    beforeHolding = brokerStatistics.getF003n() == null ? BigDecimal.ZERO : brokerStatistics.getF003n();
//                }
//            }
//            return score.add(beforeHolding);
            BrokerStatistics brokerStatistics = brokerStatisticsList.get(0);
            result = redisClient.get(BrokerConstants.BROKER_CODE_HOLD_NUM_VALUE + brokerStatistics.getSeccode().concat("-").concat(brokerStatistics.getF002v()));
            return result;
        }
        //不含今日或今日不是交易日
        for (BrokerStatistics brokerStatistics : brokerStatisticsList) {
            if(endDateLong.equals(brokerStatistics.getF001d())){
                result = brokerStatistics.getF003n();
            }
        }
        return result;
    }

//    /**
//     * 获得持股比例(占流通股)
//     * @param brokerStatisticsList  由经纪商和股票两个维度筛选过的0609表数据
//     * @param endDate
//     * @return
//     */
//    public BigDecimal getHoldingRatioInFlowShares(List<BrokerStatistics>brokerStatisticsList,Xnhk0102 xnhk0102,LocalDate endDate){
//        if(CollectionUtils.isEmpty(brokerStatisticsList)){
//            return BigDecimal.ZERO;
//        }
//        BigDecimal result = BigDecimal.ZERO;
//        if(endDate.equals(LocalDate.now())){
//            //100%*今日持股数/今日流通股数
//            BigDecimal todayHolding = redisClient.get(BrokerConstants.BROKER_CODE_HOLD_NUM_VALUE + brokerStatisticsList.get(0).getSeccode().concat("-").concat(brokerStatisticsList.get(0).getF002v()));
////            Double scoreToday = redisClient.getScore(BrokerConstants.BROKER_TODAY + brokerStatisticsList.get(0).getF002v(), brokerStatisticsList.get(0).getSeccode());
//            //BigDecimal score = BigDecimal.valueOf(Objects.isNull(scoreToday) ? 0 : scoreToday);
//            BigDecimal flowShares = xnhk0102.getF069n();
//            if(Objects.nonNull(todayHolding) && Objects.nonNull(flowShares) && !(flowShares.compareTo(BigDecimal.ZERO)==0)){
//                result = todayHolding.divide(flowShares,4,RoundingMode.HALF_UP);
//            }
//        }else {
//            Long endDateLong = Long.parseLong(endDate.toString().replace("-", ""));
//            for (BrokerStatistics brokerStatistics : brokerStatisticsList) {
//                if(endDateLong.equals(brokerStatistics.getF001d())){
//                    //数据库做过%处理，需要除以100
//                    return dividePercent(brokerStatistics.getF004n());
//                }
//            }
//        }
//
//        return result;
//    }

    /**
     * 获得持股比例（占已发行普通股）
     * @param brokerStatisticsList  由经纪商和股票两个维度筛选过的0609表数据
     * @param endDate
     * @return
     */
    public BigDecimal getHoldingRatioInOrdinaryShares(List<BrokerStatistics> brokerStatisticsList,Xnhk0102 xnhk0102,LocalDate endDate){
        if(CollectionUtils.isEmpty(brokerStatisticsList)){
            return BigDecimal.ZERO;
        }
        BigDecimal result = BigDecimal.ZERO;
        if(endDate.equals(LocalDate.now())){
            //100%*今日持股数/今日已发行普通股数
            BigDecimal todayHolding = redisClient.get(BrokerConstants.BROKER_CODE_HOLD_NUM_VALUE + brokerStatisticsList.get(0).getSeccode().concat("-").concat(brokerStatisticsList.get(0).getF002v()));
            BigDecimal ordinaryShares = xnhk0102.getF070n();
            if(Objects.nonNull(todayHolding) && Objects.nonNull(ordinaryShares) && !(ordinaryShares.compareTo(BigDecimal.ZERO)==0)){
                result = todayHolding.divide(ordinaryShares,4,RoundingMode.HALF_UP);
            }
        }else {
            Long endDateLong = Long.parseLong(endDate.toString().replace("-", ""));
            for (BrokerStatistics brokerStatistics : brokerStatisticsList) {
                if(endDateLong.equals(brokerStatistics.getF001d())){
                    //数据库做过%处理，需要除以100
                    return dividePercent(brokerStatistics.getF004n());
                }
            }
        }
        return result;
    }


    /**
     * 获得参考持股市值
     * @param brokerStatisticsList  由经纪商和股票两个维度筛选过的0609表数据
     * @param endDate
     * @param code
     * @return
     */
    public BigDecimal getReferenceMarketValue(List<BrokerStatistics> brokerStatisticsList,LocalDate endDate,String code){
        if(CollectionUtils.isEmpty(brokerStatisticsList)){
            return BigDecimal.ZERO;
        }
        if(endDate.equals(LocalDate.now())){
            //从redis拿取最新值
            return redisClient.get(BrokerConstants.BROKER_CODE_MARKET_VALUE + code.concat("-").concat(brokerStatisticsList.get(0).getF002v()));
        }else {
            Long endDateLong = Long.parseLong(endDate.toString().replace("-", ""));
            for (BrokerStatistics brokerStatistics : brokerStatisticsList) {
                if(endDateLong.equals(brokerStatistics.getF001d()) && code.equals(brokerStatistics.getSeccode())){
                    return brokerStatistics.getMarketVal();
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获得变动市值
     * @param brokerStatisticsList  由经纪商和股票两个维度筛选过的0609表数据
     * @param startDate
     * @param endDate
     * @return
     */
    public BigDecimal getChangeMarketValue(List<BrokerStatistics> brokerStatisticsList,LocalDate startDate,LocalDate endDate,String code){
        if(CollectionUtils.isEmpty(brokerStatisticsList)){
            return BigDecimal.ZERO;
        }
        if(endDate.equals(LocalDate.now())){
            //今日市值从redis拿，历史市值从数据库拿
            BigDecimal todayMarketVal = redisClient.get(BrokerConstants.BROKER_CODE_MARKET_VALUE + code.concat("-").concat(brokerStatisticsList.get(0).getF002v()));
            BigDecimal historyMarketVal = getReferenceMarketValue(brokerStatisticsList,startDate,code);
            if(Objects.nonNull(todayMarketVal) && Objects.nonNull(historyMarketVal)){
                return todayMarketVal.subtract(historyMarketVal);
            }
        }else {
            BigDecimal startMarketVal = getReferenceMarketValue(brokerStatisticsList,startDate,code);
            BigDecimal endMarketVal = getReferenceMarketValue(brokerStatisticsList,endDate,code);
            if(Objects.nonNull(startMarketVal) && Objects.nonNull(endMarketVal)){
                return endMarketVal.subtract(startMarketVal);
            }
        }
        return null;

    }

    /**
     * 获得持股日期
     * @param brokerStatisticsList  由经纪商和股票两个维度筛选过的0609表数据
     * @return
     */
    public Long getHoldingDate(List<BrokerStatistics> brokerStatisticsList){
        if(CollectionUtils.isEmpty(brokerStatisticsList)){
            return null;
        }
        //将0609表按照持股日期倒序排列
        brokerStatisticsList = brokerStatisticsList.stream().sorted(Comparator.comparing(BrokerStatistics::getF001d).reversed()).collect(Collectors.toList());
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyyMMdd");
        try {
            return simpleFormatter.parse(brokerStatisticsList.get(0).getF001d().toString()).getTime();
        } catch (ParseException e) {
            log.error("获取持股日期失败");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 计算区间变动差值
     * @param brokerStatisticsList  由经纪商和股票两个维度筛选过的0609表数据
     * @param result
     * @param startDate 起始时间
     * @param endDate   结束时间
     * @param key   取值字段
     */
    private BigDecimal subtractCalc(List<BrokerStatistics> brokerStatisticsList,BigDecimal result,LocalDate startDate,LocalDate endDate,String key){
        Long startDateLong = Long.parseLong(startDate.toString().replace("-", ""));
        Long endDateLong = Long.parseLong(endDate.toString().replace("-", ""));
        //区间末减区间初
        if(key.equals("changeInShareholding")){
            for (BrokerStatistics brokerStatistics : brokerStatisticsList) {
                if(endDateLong.equals(brokerStatistics.getF001d())){
                    result = result.add(brokerStatistics.getF003n());
                }else if(startDateLong.equals(brokerStatistics.getF001d())){
                    result = result.subtract(brokerStatistics.getF003n());
                }
            }
        }else if(key.equals("changeRatio")){
            for (BrokerStatistics brokerStatistics : brokerStatisticsList) {
                if(endDateLong.equals(brokerStatistics.getF001d())){
                    result = result.add(brokerStatistics.getF004n());
                }else if(startDateLong.equals(brokerStatistics.getF001d())){
                    result = result.subtract(brokerStatistics.getF004n());
                }
            }
        }
        return result;
    }

    /**
     * 按请求页数及页面大小进行分页
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
     * 百分比处理，除以100
     * @param num
     * @return
     */
    private BigDecimal dividePercent(BigDecimal num){
        return num == null ? BigDecimal.ZERO : num.divide(BigDecimal.valueOf(100L),4,RoundingMode.HALF_UP);
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
}
