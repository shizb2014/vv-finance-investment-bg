package com.vv.finance.investment.bg.api.impl.broker;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.stream.CollectorUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fiu.data.http.common.enums.KlineTypeEnum;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.BaseEnum;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.quotation.kline.hk.KlineEntity;
import com.vv.finance.common.entity.quotation.req.ComBatchKlineReq;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.common.utils.BigDecimalUtil;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.common.utils.ZipUtil;

import com.vv.finance.investment.bg.api.broker.BrokerAnalysisApi;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.dto.broker.IndustryBrokersProportionDTO;
import com.vv.finance.investment.bg.api.frontend.IStockKlineService;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.impl.HkTradingCalendarApiImpl;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.BrokerConstants;
import com.vv.finance.investment.bg.constants.BrokerSectionTypeEnum;
import com.vv.finance.investment.bg.dto.StockIndustry;
import com.vv.finance.investment.bg.dto.broker.*;
import com.vv.finance.investment.bg.dto.kline.BaseKlineDTO;
import com.vv.finance.investment.bg.dto.kline.KlineDTO;
import com.vv.finance.investment.bg.dto.req.KlineReq;
import com.vv.finance.investment.bg.dto.uts.resp.StockRightsDTO;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.broker.allBroker.*;
import com.vv.finance.investment.bg.entity.broker.appBroker.BrokersHold;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.stock.info.*;
import com.vv.finance.investment.bg.stock.info.mapper.*;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.stock.info.service.impl.BrokerHeldInfoServiceImpl;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import com.vv.finance.investment.bg.utils.DateUtils;

import com.vv.finance.investment.gateway.api.broker.BrokerInfoServiceApi;
import com.vv.finance.investment.gateway.dto.req.BrokerStatisticsParams;
import com.vv.finance.investment.gateway.dto.resp.broker.BrokerDetailsRes;
import com.vv.finance.investment.gateway.dto.resp.broker.BrokerStatisticsRes;
import com.vv.finance.common.calc.hk.entity.StockKline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Sets;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.SocketUtils;

import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

/**
 * @Auther: shizhibiao
 * @Date: 2022/10/8
 * @Description: 经纪商需求实现类
 * @version: 1.0
 */
@Slf4j
@RequiredArgsConstructor
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class BrokerAnalysisApiImpl implements BrokerAnalysisApi {

    @Resource
    HkTradingCalendarApiImpl hkTradingCalendarApi;

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    BrokerInfoServiceApi brokerInfoServiceApi;

    @Resource
    BrokerStatisticsMapper brokerStatisticsMapper;

    @Resource
    BrokerMarketValueStatisticsMapper brokerMarketValueStatisticsMapper;

    @Resource
    Xnhk0608Mapper xnhk0608Mapper;

    @Resource
    Xnhk0609Mapper xnhk0609Mapper;

    @Resource
    Xnhk0610Mapper xnhk0610Mapper;

    @Resource
    Xnhk0102Mapper xnhk0102Mapper;

    @Resource
    StockDefineMapper stockDefineMapper;


    private final RedisClient redisClient;

    @Resource
    StockService stockService;

    @Resource
    Xnhks0104Mapper xnhks0104Mapper;

//
//    @DubboReference(group = "${dubbo.investment.composite.service.group:composite}", registry = "compositeservice")
//    private HkStockCompositeApi compositeApi;

    @Resource
    BrokerCommonUtils brokerCommonUtils;

    @Resource
    SehkEpMapper sehkEpMapper;

    @Resource
    Xnhk0127Mapper xnhk0127Mapper;

    @Resource
    BrokerHeldInfoMapper brokerHeldInfoMapper;

    @Resource
    BrokerHeldInfoServiceImpl brokerHeldInfoService;

    @Resource
    IStockKlineService iStockKlineService;

    @Resource
    IStockMarketService stockMarketService;

    @Resource
    BrokerIndustryStatisticsMapper brokerIndustryStatisticsMapper;

    @Resource
    private Xnhks0317Mapper xnhks0317Mapper;

    @Resource
    private IIndustrySubsidiaryService industrySubsidiaryService;

    @Resource
    private IStockDefineService stockDefineService;

    @Resource
    private StockCache stockCache;

    @Resource
    private UtsInfoService utsInfoService;

    @Value("${batch.delete.size:1000}")
    private Integer batchSize;

    @Override
    public ResultT<NetBuyAndSellResp> getNetTradeBroker(String code, int type) {
        List<Xnhk0609Resp> xnhk0609RespListBuy = new ArrayList<>();
        List<Xnhk0609Resp> xnhk0609RespListSale = new ArrayList<>();
        List<Xnhk0609Resp> xnhk0609RespList = new ArrayList<>();

        NetBuyAndSellResp netBuyAndSellResp = new NetBuyAndSellResp();

        Boolean tradeFlag = hkTradingCalendarApi.isTradingDay(LocalDate.now());

        //该股票所有变动量数据
        Set<ZSetOperations.TypedTuple<Object>> all = null;

        //直接查询融聚汇接口今日净买卖数据
        BrokerStatisticsParams brokerStatisticsReq = new BrokerStatisticsParams();
        brokerStatisticsReq.setPeriod(0);
        brokerStatisticsReq.setSize(10000);
        brokerStatisticsReq.setSymbol(code);
        brokerStatisticsReq.setType("");
        ResultT<BrokerStatisticsRes> brokerStatisticsRespResultT = brokerInfoServiceApi.getBrokerStatistics(brokerStatisticsReq);
        Map<String,BigDecimal> todayMap = new HashMap<>();
        String updateTime = null;
        if(brokerStatisticsRespResultT.getCode() == 200){
            //将买和卖的数据合并成一个结果集
            List<BrokerDetailsRes> buyList = brokerStatisticsRespResultT.getData().getBuy();
            if(!CollectionUtils.isEmpty(buyList)){
                Map<String, BigDecimal> buyMap = buyList.stream().collect(Collectors.toMap(item -> brokerCommonUtils.getBrokerId(item.getBrokerId()), BrokerDetailsRes::getNetVolume));
                todayMap.putAll(buyMap);
            }
            List<BrokerDetailsRes> sellList = brokerStatisticsRespResultT.getData().getSell();
            if(!CollectionUtils.isEmpty(sellList)){
                Map<String, BigDecimal> sellMap = sellList.stream().collect(Collectors.toMap(item -> brokerCommonUtils.getBrokerId(item.getBrokerId()), BrokerDetailsRes::getNetVolume));
                todayMap.putAll(sellMap);
            }
            updateTime = brokerStatisticsRespResultT.getData() == null ? null : brokerStatisticsRespResultT.getData().getUpdateTime();
        }
        //数据更新时间
        if(StringUtils.isNotBlank(updateTime)){
            redisClient.set(BrokerConstants.BROKER_UPDATETIME, updateTime);
        }else {
            updateTime = redisClient.get(BrokerConstants.BROKER_UPDATETIME);
        }
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(updateTime);
        } catch (ParseException e) {
            log.warn("获取最新时间失败");
            e.printStackTrace();
        }
        if(Objects.nonNull(date)){
            netBuyAndSellResp.setDate(date.getTime());
        }

        //当天为交易日逻辑
        if (tradeFlag && type != BrokerSectionTypeEnum.ONE_DAY.getCode()) {
            //如果是交易日，需要获取所有的历史变动量，加上当天的变动量
            if (type == BrokerSectionTypeEnum.FIVE_DAYS.getCode()) {
                all = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_5DAY + code, 0, -1);
            } else if (type == BrokerSectionTypeEnum.TEN_DAYS.getCode()) {
                all = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_10DAY + code, 0, -1);
            } else if (type == BrokerSectionTypeEnum.TWENTY_DAYS.getCode()) {
                all = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_20DAY + code, 0, -1);
            } else if (type == BrokerSectionTypeEnum.SIXTY_DAYS.getCode()) {
                all = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_60DAY + code, 0, -1);
            }

            Iterator iterator = all.iterator() ;
            while (iterator.hasNext()) {
                ZSetOperations.TypedTuple<Object> next = (ZSetOperations.TypedTuple<Object>) iterator.next();
                //获取到经纪商ID
                String brokerId = String.valueOf(next.getValue());
                //获取到今日净买入数据
                Double score = next.getScore();
                String brokerName=brokerCommonUtils.getBrokerName(brokerId);
                //过滤经纪商id与经纪商名称有问题的数据
                if("0".equals(brokerId)||brokerId.length()==4){
                    continue;
                }
                //获取该股票的该经纪商当天的买卖数量，然后加上前四日的数据，放入list，然后将list排序，取前五
                BigDecimal scoreToday = todayMap.get(brokerId)==null ? BigDecimal.ZERO : todayMap.get(brokerId);
                Xnhk0609Resp xnhk0609Resp = new Xnhk0609Resp();
                xnhk0609Resp.setBrokerId(brokerId);
                xnhk0609Resp.setF003n(BigDecimal.valueOf(score).add(scoreToday));
                xnhk0609Resp.setBrokerName(brokerName);
                xnhk0609RespList.add(xnhk0609Resp);
            }

            xnhk0609RespList.sort(Comparator.comparing(Xnhk0609Resp::getF003n));
            List<Xnhk0609Resp> resultBuy=xnhk0609RespList.size()<10?xnhk0609RespList:xnhk0609RespList.subList(xnhk0609RespList.size() - 10, xnhk0609RespList.size());
            resultBuy = resultBuy.stream().filter(item -> item.getF003n().compareTo(BigDecimal.ZERO)>0).collect(Collectors.toList());
            //取前十条数据,买盘前十
            resultBuy.sort(Comparator.comparing(Xnhk0609Resp::getF003n).reversed());
            //进行降序
            //取后十条数据,卖盘前十
            List<Xnhk0609Resp> resultSale=xnhk0609RespList.size()<10?xnhk0609RespList:xnhk0609RespList.subList(0, 10);
            resultSale = resultSale.stream().filter(item -> item.getF003n().compareTo(BigDecimal.ZERO)<0).collect(Collectors.toList());
            netBuyAndSellResp.setXnhk0609RespListBuy(resultBuy);
            netBuyAndSellResp.setXnhk0609RespListSale(resultSale);
            return ResultT.success(netBuyAndSellResp);

        }

        //从小到大
        Set<ZSetOperations.TypedTuple<Object>> buy = null;
        //从小到大
        Set<ZSetOperations.TypedTuple<Object>> sell = null;

        //当天为非交易日，查询当天净买卖前十，直接获取接口的数据
        if (type == BrokerSectionTypeEnum.ONE_DAY.getCode()) {
            List<Xnhk0609Resp> resultBuy = new ArrayList<>();
            List<Xnhk0609Resp> resultSale = new ArrayList<>();
            List<Xnhk0609Resp> finalResultBuy = resultBuy;
            List<Xnhk0609Resp> finalResultSale = resultSale;
            todayMap.forEach((k, v) -> {
                //过滤4位id的经纪商
                if(!("0".equals(k)||k.length()==4)){
                    BigDecimal scoreToday = todayMap.get(k)==null ? BigDecimal.ZERO : todayMap.get(k);
                    String brokerName=brokerCommonUtils.getBrokerName(k);
                    Xnhk0609Resp xnhk0609Resp = new Xnhk0609Resp();
                    xnhk0609Resp.setBrokerId(k);
                    xnhk0609Resp.setF003n(scoreToday);
                    xnhk0609Resp.setBrokerName(brokerName);
                    if (v.compareTo(BigDecimal.ZERO) > 0) {
                        finalResultBuy.add(xnhk0609Resp);
                    }else if(v.compareTo(BigDecimal.ZERO) < 0){
                        finalResultSale.add(xnhk0609Resp);
                    }
                }
            });
            resultBuy = finalResultBuy.stream().sorted(Comparator.comparing(Xnhk0609Resp::getF003n).reversed()).limit(10).collect(Collectors.toList());
            resultSale = finalResultSale.stream().sorted(Comparator.comparing(Xnhk0609Resp::getF003n)).limit(10).collect(Collectors.toList());
            netBuyAndSellResp.setXnhk0609RespListBuy(resultBuy);
            netBuyAndSellResp.setXnhk0609RespListSale(resultSale);
            return ResultT.success(netBuyAndSellResp);
        } else if (type == BrokerSectionTypeEnum.FIVE_DAYS.getCode()) {
            //从小到大
            buy = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_5DAY + code, 0, 19);
            //从小到大
            sell = redisClient.getZSetRangeWithScore(BrokerConstants.BROKER_5DAY + code, 0, 19);
        } else if (type == BrokerSectionTypeEnum.TEN_DAYS.getCode()) {
            //从小到大
            buy = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_10DAY + code, 0, 19);
            //从小到大
            sell = redisClient.getZSetRangeWithScore(BrokerConstants.BROKER_10DAY + code, 0, 19);
        } else if (type == BrokerSectionTypeEnum.TWENTY_DAYS.getCode()) {
            //从小到大
            buy = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_20DAY + code, 0, 19);
            //从小到大
            sell = redisClient.getZSetRangeWithScore(BrokerConstants.BROKER_20DAY + code, 0, 19);
        } else if (type == BrokerSectionTypeEnum.SIXTY_DAYS.getCode()) {
            //从小到大
            buy = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_60DAY + code, 0, 19);
            //从小到大
            sell = redisClient.getZSetRangeWithScore(BrokerConstants.BROKER_60DAY + code, 0, 19);
        }
        Iterator iteratorBuy = buy.iterator() ;
        while (iteratorBuy.hasNext()) {
            ZSetOperations.TypedTuple<Object> next = (ZSetOperations.TypedTuple<Object>) iteratorBuy.next();
            //获取到经纪商ID
            String brokerId = String.valueOf(next.getValue());
            String brokerName=brokerCommonUtils.getBrokerName(brokerId);
            //过滤经纪商id与经纪商名称有问题的数据
            if("0".equals(brokerId)||brokerId.length()==4){
                continue;
            }
            //获取到今日净买入数据
            Double score = next.getScore();
            Xnhk0609Resp xnhk0609Resp = new Xnhk0609Resp();
            xnhk0609Resp.setBrokerId(brokerId);
            xnhk0609Resp.setF003n(new BigDecimal(score));
            xnhk0609Resp.setBrokerName(brokerCommonUtils.getBrokerName(brokerId));
            xnhk0609RespListBuy.add(xnhk0609Resp);
        }
        xnhk0609RespListBuy = xnhk0609RespListBuy.stream().filter(item -> item.getF003n().compareTo(BigDecimal.ZERO)>0).collect(Collectors.toList());
        netBuyAndSellResp.setXnhk0609RespListBuy(xnhk0609RespListBuy.size()<=10 ? xnhk0609RespListBuy : xnhk0609RespListBuy.subList(0,10));

        Iterator iteratorSell = sell.iterator() ;
        while (iteratorSell.hasNext()) {
            ZSetOperations.TypedTuple<Object> next = (ZSetOperations.TypedTuple<Object>) iteratorSell.next();
            //获取到经纪商ID
            String brokerId = String.valueOf(next.getValue());
            String brokerName=brokerCommonUtils.getBrokerName(brokerId);
            //过滤经纪商id与经纪商名称有问题的数据
            if("0".equals(brokerId)||brokerId.length()==4){
                continue;
            }
            //获取到今日净买入数据
            Double score = next.getScore();
            Xnhk0609Resp xnhk0609Resp = new Xnhk0609Resp();
            xnhk0609Resp.setBrokerId(brokerId);
            xnhk0609Resp.setF003n(new BigDecimal(score));
            xnhk0609Resp.setBrokerName(brokerCommonUtils.getBrokerName(brokerId));
            xnhk0609RespListSale.add(xnhk0609Resp);
        }
        xnhk0609RespListSale = xnhk0609RespListSale.stream().filter(item -> item.getF003n().compareTo(BigDecimal.ZERO)<0).collect(Collectors.toList());
        netBuyAndSellResp.setXnhk0609RespListSale(xnhk0609RespListSale.size()<=10 ? xnhk0609RespListSale : xnhk0609RespListSale.subList(0,10));

        return ResultT.success(netBuyAndSellResp);
    }


    @Override
    public ResultT<BrokersProportionResp> getAppBrokersProportion(String code, BrokersProportionResp brokersProportionResp) {
        long l1 = System.currentTimeMillis();
        Boolean tradeFlag = false;
        //获取到今日的date
        Long todayDate = DateUtils.localDateToF001D(LocalDate.now());
        //定义一个str用于时间戳转换
        String str=" 00:00:00";
        log.info("==================获取基本信息耗时:{}====================",System.currentTimeMillis()-l1);

        long l2 = System.currentTimeMillis();
        /**
         * 1、获取经纪商前五：先从redis获取到前五的经纪商id，然后通过id查询近一个月数据
         * 2、判断今日是否是交易日，如果是交易日，取前29+今天，如果是非交易日，直接查询前30天
         */
        Set<Object> brokers = redisClient.getZSetReverseRange(BrokerConstants.BROKER_TOP5 + code, 0, 4);
        if(CollectionUtils.isEmpty(brokers)){
            //如果redis中没有前五经纪商集合，直接将前五经纪商集合设为null
            brokersProportionResp.setBrokersProportionList(null);

            List<String> brokerIdList = Lists.newArrayList("A00003", "A00004");
            LambdaQueryWrapper<BrokerStatistics> queryWrapper=new LambdaQueryWrapper<BrokerStatistics>();
            BgTradingCalendar bgTradingCalendar = hkTradingCalendarApi.queryBeforeTradingCalendars(30);
            Long f001d = DateUtils.localDateToF001D(bgTradingCalendar.getDate());
            queryWrapper.eq(BrokerStatistics::getSeccode,code).in(BrokerStatistics::getF002v,brokerIdList).ge(BrokerStatistics::getF001d,f001d);
            List<BrokerStatistics> brokerStatistics = brokerStatisticsMapper.selectList(queryWrapper);
            Map<String, List<BrokerStatistics>> brokerStatisticMap = brokerStatistics.stream().collect(Collectors.groupingBy(BrokerStatistics::getF002v));
            setHKStockConnectShanghai(brokersProportionResp,"A00003",code,brokerStatisticMap,tradeFlag);
            setHKStockConnectShenzhen(brokersProportionResp,"A00004",code,brokerStatisticMap,tradeFlag);
        }else{
            //筛选出经纪商Id集合，同时将4位id转换为6位id
            List<String> todayBrokerIdList = brokers.stream().map(item -> brokerCommonUtils.getBrokerId(item.toString())).collect(Collectors.toList());
            //将前五经纪商id及对应顺序存储到map中
            Map<String,Integer> map=new HashMap<>();
            if(todayBrokerIdList.size()>5){
                for (int i = 0; i < 5; i++) {
                    map.put(todayBrokerIdList.get(i),i);
                }
            }else{
                for(int i=0;i<todayBrokerIdList.size();i++){
                    map.put(todayBrokerIdList.get(i),i);
                }
            }

            //一次取出前30日的数据
            List<String> brokerIdList = Lists.newArrayList("A00003", "A00004");
            brokerIdList.addAll(todayBrokerIdList);
            LambdaQueryWrapper<BrokerStatistics> queryWrapper=new LambdaQueryWrapper<BrokerStatistics>();
            BgTradingCalendar bgTradingCalendar = hkTradingCalendarApi.queryBeforeTradingCalendars(31);
            Long f001d = DateUtils.localDateToF001D(bgTradingCalendar.getDate());
            queryWrapper.eq(BrokerStatistics::getSeccode,code).in(BrokerStatistics::getF002v,brokerIdList).ge(BrokerStatistics::getF001d,f001d).lt(BrokerStatistics::getF001d,DateUtils.localDateToF001D(LocalDate.now()));
            List<BrokerStatistics> brokerStatistics = brokerStatisticsMapper.selectList(queryWrapper);
            Map<String, List<BrokerStatistics>> brokerStatisticMap = brokerStatistics.stream().collect(Collectors.groupingBy(BrokerStatistics::getF002v));

            //创建一个空的集合，保存前五经纪商的持股数据集合
            List<List<BrokersProportionDetail>> brokerProportionList=new ArrayList<>();
            //判断今日是否是交易日
            if(tradeFlag){
                //如果是交易日，取前29日+今天的
                List<BrokerSearch> nameList = redisClient.get(BrokerConstants.BG_BROKER_ID_PROFIT_LIST);
                Map<String, String> nameMap = nameList.stream().collect(Collectors.toMap(BrokerSearch::getBrokerId, BrokerSearch::getBrokerName));
                for (String brokerId : todayBrokerIdList){
                    //获取经纪商名称
                    String brokerName = nameMap.get(brokerId);
                    //获取前29日数据
                    List<BrokersProportionDetail> twentyNineDaysShare = getThirtyDaysShare(code, brokerId, 29,brokerStatisticMap);
                    //获取今天的
                    BrokersProportionDetail brokersProportionDetail=new BrokersProportionDetail();
                    //持股日期
                    brokersProportionDetail.setF001d(Timestamp.valueOf(LocalDate.now().atStartOfDay()).getTime());
                    if(!hkTradingCalendarApi.isTradingTimeAM(LocalTime.now())) {
                        //获取今日的持股比例
                        Double score = redisClient.getScore(BrokerConstants.BROKER_TODAYPROPORTIONSHAREHOLD + code, brokerId);
                        BigDecimal holdingRatio = score == null ? BigDecimal.ZERO : BigDecimal.valueOf(score);
                        brokersProportionDetail.setF004n(holdingRatio.multiply(BigDecimal.valueOf(100)));
                        //经纪商Id
                        brokersProportionDetail.setBrokerId(brokerId);
                        //经纪商名称
                        brokersProportionDetail.setBrokerName(brokerName);
                    }
                    //将今日的添加到List中
                    twentyNineDaysShare.add(brokersProportionDetail);
                    //将这个经纪商的数据添加到List中
                    brokerProportionList.add(twentyNineDaysShare);
                }
            }else{
                //如果是非交易日，直接获取近30天的数据
                //遍历集合
                for (String brokerId : todayBrokerIdList){
                    //过滤掉经纪商id与经纪商名称有问题的数据
                    if("0".equals(brokerId)||brokerId.length()==4){
                        continue;
                    }
                    //获取近30交易日数据
                    List<BrokersProportionDetail> thirtyDaysShare = getThirtyDaysShare(code, brokerId, 30,brokerStatisticMap);
                    //将这个经纪商的数据添加到List中
                    brokerProportionList.add(thirtyDaysShare);
                }

            }
            //现在集合中已经有五个对象数据，需要判断集合中是否包含港股通（沪）和港股通（深）
            //如果包含港股通（沪）
            if(todayBrokerIdList.contains("A00003")){
                brokersProportionResp.setBrokersProportionHKStockConnectShanghai(brokerProportionList.get(map.get("A00003")));
                brokerProportionList.set(map.get("A00003"),null);
            }else{
                //不包含，取值并为港股通（沪）赋值
                setHKStockConnectShanghai(brokersProportionResp,"A00003",code,brokerStatisticMap,tradeFlag);
            }
            //如果包含港股通（深）
            if(todayBrokerIdList.contains("A00004")){
                brokersProportionResp.setBrokersProportionHKStockConnectShenzhen(brokerProportionList.get(map.get("A00004")));
                brokerProportionList.set(map.get("A00004"),null);
            }else{
                //不包含，取值并为港股通（深）赋值
                setHKStockConnectShenzhen(brokersProportionResp,"A00004",code,brokerStatisticMap,tradeFlag);
            }
            //将前五经纪商集合中为空的过滤掉
            List<List<BrokersProportionDetail>> newBrokerProportionList = brokerProportionList.stream().filter(Objects::nonNull).collect(Collectors.toList());
            //为前五经纪商集合赋值
            brokersProportionResp.setBrokersProportionList(newBrokerProportionList);
        }

        log.info("==================前五耗时:{}====================",System.currentTimeMillis()-l2);
        long l3 = System.currentTimeMillis();

        /**
         * 2、获取前5/10/20经纪商合计
         * select SECCODE,sum(F004N) from XNHK0609 where SECCODE = '00700.hk' AND F001D = 20221013 ORDER BY F004N DESC limit 5
         */
        //创建一个空的集合保存前五经纪商合计的对象
        ArrayList<TopBrokersProportionDetail> brokersProportionSum5list=new ArrayList<>();
        //创建一个空的集合保存前五经纪商合计的对象
        ArrayList<TopBrokersProportionDetail> brokersProportionSum10list=new ArrayList<>();
        //创建一个空的集合保存前五经纪商合计的对象
        ArrayList<TopBrokersProportionDetail> brokersProportionSum20list=new ArrayList<>();
        //交易日历
        List<BgTradingCalendar> bgTradingCalendars = null;
        if(tradeFlag){
            //如果是交易日且不在开盘前
            if(!hkTradingCalendarApi.isTradingTimeAM(LocalTime.now())) {
                //从redis中获取今日的数据，该经纪商前5/10/20经纪商合计
                Set<ZSetOperations.TypedTuple<Object>> top5Broker = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_TODAYPROPORTIONSHAREHOLD + code, 0, 4);
                Set<ZSetOperations.TypedTuple<Object>> top10Broker = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_TODAYPROPORTIONSHAREHOLD + code, 0, 9);
                Set<ZSetOperations.TypedTuple<Object>> top20Broker = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_TODAYPROPORTIONSHAREHOLD + code, 0, 19);
                BigDecimal top5BrokerSum = top5Broker.stream().map(item -> BigDecimal.valueOf(item.getScore())).reduce(BigDecimal.ZERO, BigDecimal::add);;
                BigDecimal top10BrokerSum = top10Broker.stream().map(item -> BigDecimal.valueOf(item.getScore())).reduce(BigDecimal.ZERO, BigDecimal::add);;
                BigDecimal top20BrokerSum = top20Broker.stream().map(item -> BigDecimal.valueOf(item.getScore())).reduce(BigDecimal.ZERO, BigDecimal::add);;

                //向正无限大方向舍入
                BigDecimal top5Ration = top5BrokerSum.multiply(BigDecimal.valueOf(100));
                BigDecimal top10Ration = top10BrokerSum.multiply(BigDecimal.valueOf(100));
                BigDecimal top20Ration = top20BrokerSum.multiply(BigDecimal.valueOf(100));

                //将交易日时间转换成时间戳格式
                String s = todayDate.toString();
                String strTemp1 = s.substring(0, 4).concat("-").concat(s.substring(4, 6)).concat("-").concat(s.substring(6)).concat(str);
                //创建今日的对象
                //创建一个空的对象
                TopBrokersProportionDetail todayTop5BrokersProportionDetail=new TopBrokersProportionDetail();
                todayTop5BrokersProportionDetail.setF001d(Timestamp.valueOf(strTemp1).getTime());
                todayTop5BrokersProportionDetail.setTopPercent(top5Ration);
                //创建一个空的对象
                TopBrokersProportionDetail todayTop10BrokersProportionDetail=new TopBrokersProportionDetail();
                todayTop10BrokersProportionDetail.setF001d(Timestamp.valueOf(strTemp1).getTime());
                todayTop10BrokersProportionDetail.setTopPercent(top10Ration);
                //创建一个空的对象
                TopBrokersProportionDetail todayTop20BrokersProportionDetail=new TopBrokersProportionDetail();
                todayTop20BrokersProportionDetail.setF001d(Timestamp.valueOf(strTemp1).getTime());
                todayTop20BrokersProportionDetail.setTopPercent(top20Ration);

                //将今日数据添加到集合中
                brokersProportionSum5list.add(todayTop5BrokersProportionDetail);
                brokersProportionSum10list.add(todayTop10BrokersProportionDetail);
                brokersProportionSum20list.add(todayTop20BrokersProportionDetail);

                //查询前29日，i=1时查询的是今日，由于是查询历史，i=2开始
                LocalDate startDate = LocalDate.parse(hkTradingCalendarApi.queryBeginTradingCalendars(30).getRdate().toString(), BASIC_ISO_DATE);
                bgTradingCalendars = hkTradingCalendarApi.queryTradingCalendarsBySection(startDate, LocalDate.now());
            }else {
                //将交易日时间转换成时间戳格式
                String s = todayDate.toString();
                String strTemp1 = s.substring(0, 4).concat("-").concat(s.substring(4, 6)).concat("-").concat(s.substring(6)).concat(str);
                //创建今日的对象
                //创建一个空的对象，赋值时间
                TopBrokersProportionDetail topBrokersProportionDetail =new TopBrokersProportionDetail();
                topBrokersProportionDetail.setF001d(Timestamp.valueOf(strTemp1).getTime());
                //将今日数据添加到集合中
                brokersProportionSum5list.add(topBrokersProportionDetail);
                brokersProportionSum10list.add(topBrokersProportionDetail);
                brokersProportionSum20list.add(topBrokersProportionDetail);
                //查询前29日，i=1时查询的是今日，由于是查询历史，i=2开始
                LocalDate startDate = LocalDate.parse(hkTradingCalendarApi.queryBeginTradingCalendars(30).getRdate().toString(), BASIC_ISO_DATE);
                bgTradingCalendars = hkTradingCalendarApi.queryTradingCalendarsBySection(startDate, LocalDate.now());
            }
        }else{
            //如果是非交易日，直接查询30天的数据
            LocalDate startDate = LocalDate.parse(hkTradingCalendarApi.queryBeginTradingCalendars(31).getRdate().toString(), BASIC_ISO_DATE);
            bgTradingCalendars = hkTradingCalendarApi.queryTradingCalendarsBySection(startDate, LocalDate.now());
        }
        for (BgTradingCalendar bgTradingCalendar : bgTradingCalendars) {
            //查询交易日
            LocalDate date = bgTradingCalendar.getDate();
            Long hkexTdDate = Long.parseLong(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            String s2 = hkexTdDate.toString();
            String strTemp2 = s2.substring(0, 4).concat("-").concat(s2.substring(4, 6)).concat("-").concat(s2.substring(6)).concat(str);

            //查询当天的持股前5/10/20的经纪商持股情况
            BrokerStatistics top5Sum = brokerStatisticsMapper.queryBrokerTop5Change(code, hkexTdDate);
            BrokerStatistics top10Sum = brokerStatisticsMapper.queryBrokerTop10Change(code, hkexTdDate);
            BrokerStatistics top20Sum = brokerStatisticsMapper.queryBrokerTop20Change(code, hkexTdDate);
            //创建一个空的对象
            TopBrokersProportionDetail top5BrokersProportionDetail=new TopBrokersProportionDetail();
            top5BrokersProportionDetail.setF001d(Timestamp.valueOf(strTemp2).getTime());
            top5BrokersProportionDetail.setTopPercent(top5Sum == null ? null : top5Sum.getF004n());
            brokersProportionSum5list.add(top5BrokersProportionDetail);

            TopBrokersProportionDetail top10BrokersProportionDetail=new TopBrokersProportionDetail();
            top10BrokersProportionDetail.setF001d(Timestamp.valueOf(strTemp2).getTime());
            top10BrokersProportionDetail.setTopPercent(top10Sum == null ? null : top10Sum.getF004n());
            brokersProportionSum10list.add(top10BrokersProportionDetail);

            TopBrokersProportionDetail top20BrokersProportionDetail=new TopBrokersProportionDetail();
            top20BrokersProportionDetail.setF001d(Timestamp.valueOf(strTemp2).getTime());
            top20BrokersProportionDetail.setTopPercent(top20Sum == null ? null : top20Sum.getF004n());
            brokersProportionSum20list.add(top20BrokersProportionDetail);
        }
        //log.info("==================计算总和耗时:{}====================",System.currentTimeMillis()-l3);

        //前5经纪商持股比例之和
        brokersProportionResp.setBrokersProportionSum5(brokersProportionSum5list);
        //前10经纪商持股比例之和
        brokersProportionResp.setBrokersProportionSum10(brokersProportionSum10list);
        //前20经纪商持股比例之和
        brokersProportionResp.setBrokersProportionSum20(brokersProportionSum20list);
        subBrokersProportion(brokersProportionResp);
        return ResultT.success(brokersProportionResp);
    }

    @Override
    public ResultT<PageDomain<BrokersDetail>> getBrokerHoldingList(String code, Integer type, String sortKey, String sort, Integer pageSize, Integer currentPage) {
        //获取上一个交易日的数据
        LocalDate queryDate = hkTradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate();
        Long queryDateLong = DateUtils.localDateToF001D(queryDate);

        //获取前x日的数据
        LocalDate beforeDate = null;
        //近N日
        BrokerSectionTypeEnum brokerSectionTypeEnum = BaseEnum.getByCode(type, BrokerSectionTypeEnum.class);
        Long rdate = hkTradingCalendarApi.queryBeforeTradingCalendars(queryDate,brokerSectionTypeEnum.getDay());
        beforeDate = LocalDate.parse(rdate + "", DateTimeFormatter.ofPattern("yyyyMMdd"));

        Long beforeDateLong = DateUtils.localDateToF001D(beforeDate);
        List<BrokerStatistics> beforeList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("seccode", code).eq("f001d", beforeDateLong));
        if (CollectionUtils.isEmpty(beforeList)) {
            // 数据为空，查看是否因为停牌原因导致，是的话则往前一个交易日查询数据
            Integer count = xnhks0317Mapper.countByCodeAndDate(code, beforeDateLong);
            if (count != null && count > 0) {
                beforeDate = hkTradingCalendarApi.getBeforeTradingCalendar(beforeDate).getDate();
                log.info("期初数据因停牌原因导致为空，往前一个交易日:{}查询数据", beforeDate);
                beforeDateLong = DateUtils.localDateToF001D(beforeDate);
                beforeList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                        .eq("seccode", code).eq("f001d", beforeDateLong));
            }
        }
        Map<String, BrokerStatistics> beforeMap = beforeList.stream().collect(Collectors.toMap(BrokerStatistics::getF002v, Function.identity()));

        List<BrokerStatistics> brokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("seccode", code).eq("f001d", queryDateLong));
        if (CollectionUtils.isEmpty(brokerStatisticsList)) {
            // 数据为空，查看是否因为停牌原因导致，是的话则往前一个交易日查询数据
            Integer count = xnhks0317Mapper.countByCodeAndDate(code, queryDateLong);
            if (count != null && count > 0) {
                queryDate = hkTradingCalendarApi.getBeforeTradingCalendar(queryDate).getDate();
                if (queryDate.isBefore(beforeDate)) {
                    //期末向前找的交易日早于期初日期，则本区间内按无数据处理
                    return ResultT.success();
                }
                log.info("期末数据因停牌原因导致为空，往前一个交易日:{}查询数据", queryDate);
                queryDateLong = DateUtils.localDateToF001D(queryDate);
                brokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                        .eq("seccode", code).eq("f001d", queryDateLong));
            }
        }
        if (CollectionUtils.isEmpty(brokerStatisticsList)) {
            return ResultT.success();
        }
        brokerStatisticsList = brokerStatisticsList.stream().filter(item -> item.getF002v().length()==6).collect(Collectors.toList());

        List<BrokersDetail> brokersDetails = Lists.newArrayList();
        BigDecimal totalHolding = BigDecimal.ZERO;
        Xnhk0102 xnhk0102 = xnhk0102Mapper.selectOne(new QueryWrapper<Xnhk0102>().eq("seccode", code));
        //已发行股本
        BigDecimal issuedShare = xnhk0102 == null ? null : xnhk0102.getF070n();
        //循环上一个交易日的经纪商进行赋值
        for (BrokerStatistics brokerStatistics : brokerStatisticsList) {
            BrokersDetail brokersDetail = new BrokersDetail();
            //经纪商id
            String brokerId = brokerStatistics.getF002v();
            brokersDetail.setBrokerId(brokerId);
            //经纪商名称
            String brokerName = brokerCommonUtils.getBrokerName(brokerId);
            brokersDetail.setBrokerName(brokerName);
            //今日持股数
            BigDecimal holding = brokerStatistics.getF003n();
            totalHolding = totalHolding.add(holding);
            brokersDetail.setShareHeld(holding);
            //持股比例(占已发行股本)
            BigDecimal f004n = brokerStatistics.getF004n();
            brokersDetail.setShareholdingRatioOfIssue(calcCashScale(f004n,BigDecimal.valueOf(100), 6));
            //持股变动数和持股变动比例
            BrokerStatistics beforeStatistics = beforeMap.get(brokerId);
            if(Objects.nonNull(beforeStatistics)){
                BigDecimal beforeHolding = beforeStatistics.getF003n();
                BigDecimal changHolding = calcSubtract(holding, beforeHolding);
                brokersDetail.setShareholdChange(changHolding);
                BigDecimal f004nBefore = beforeStatistics.getF004n();
                brokersDetail.setShareholdChangeOfCirculation(calcCashScale(calcSubtract(f004n,f004nBefore),BigDecimal.valueOf(100), 6));
                //brokersDetail.setShareholdChangeOfCirculation(changHolding == null ? null : calcCash(changHolding,issuedShare));
            }
            brokersDetails.add(brokersDetail);
        }
        totalHolding = calcCashScale(totalHolding,issuedShare, 6);
        for (BrokersDetail brokersDetail : brokersDetails) {
            brokersDetail.setTotalHolding(totalHolding);
        }
        //筛选掉0609中没有的经纪商
        List<BrokerSearch> brokerSearchList = redisClient.get(BrokerConstants.BG_BROKER_ID_PROFIT_LIST);
        if(!CollectionUtils.isEmpty(brokerSearchList)) {
            List<String> brokerIdList = brokerSearchList.stream().map(item -> item.getBrokerId()).collect(Collectors.toList());
            brokersDetails = brokersDetails.stream().filter(item -> brokerIdList.contains(item.getBrokerId())).collect(Collectors.toList());
        }

        //排序和分页
        sort(brokersDetails,sortKey,sort);
        PageDomain<BrokersDetail> pageDomain=new PageDomain<>();
        pageDomain.setRecords(brokersDetails);
        pageDomain.setTotal(brokersDetails.size());
        pageDomain.setSize(pageSize);
        pageDomain.setCurrent(currentPage);
        getPageList(pageDomain,currentPage,pageSize);
        return ResultT.success(pageDomain);
    }


//    @Override
//    public ResultT<PageDomain<BrokersDetail>> getNewAppBrokerList(String code, Integer type, String sortKey, String sort, Integer pageSize, Integer currentPage) {
//        //设置数据更新时间
//        DateResp dateResp = getDateResp();
//        Long date = dateResp == null ? null :getDateResp().getDate();
//
//        //查询前一个交易日
//        BgTradingCalendar beforeTradingCalendar = hkTradingCalendarApi.getBeforeTradingCalendar(LocalDate.now());
//        LocalDate beforeDate = beforeTradingCalendar.getDate();
//        //将查询日的前一日转换成long类型
//        Long beforeDateLong = Long.parseLong(beforeDate.toString().replace("-", ""));
//
//        //查询前五交易日
//        Long rdate5 = hkTradingCalendarApi.queryBeginTradingCalendars(5).getRdate();
//        Long rdate20 = hkTradingCalendarApi.queryBeginTradingCalendars(20).getRdate();
//        Long rdate60 = hkTradingCalendarApi.queryBeginTradingCalendars(60).getRdate();
//        //查询近一年的交易日
//
//        LocalDate today = LocalDate.now();
//        LocalDate previousYear = today.minus(1, ChronoUnit.YEARS);
//        Long rdate360 = Long.parseLong(hkTradingCalendarApi.getBeforeTradingCalendar(previousYear).getDate().toString().replace("-", ""));
//
//        //查询前一交易日不同经纪商对该股票的持股情况
//        List<BrokerStatistics> beforeBrokerStatisticsList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>().eq("SECCODE", code).eq("F001D", beforeDateLong));
//        Map<String, BrokerStatistics> beforeBrokerStatisticsMap = beforeBrokerStatisticsList.stream().
//                collect(Collectors.toMap(item -> item.getF002v(), Function.identity()));
//        //从redis中获取到今日持股变动量集合
//        Set<ZSetOperations.TypedTuple<Object>> todayRedis = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_TODAY + code, 0, -1);
//
//
//        //近5日
//        List<BrokerStatistics> brokerStatistics5 = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>().eq("SECCODE", code).eq("F001D", rdate5));
//        Map<String, BrokerStatistics> before5BrokerStatisticsMap = brokerStatistics5.stream().
//                collect(Collectors.toMap(item -> item.getF002v(), Function.identity()));
//        //从redis中获取到近五日的持股变动量集合
//        Set<ZSetOperations.TypedTuple<Object>> before5Redis = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_5DAY + code, 0, -1);
//        Map<Object, ZSetOperations.TypedTuple<Object>> collect5 = before5Redis.stream().collect(Collectors.toMap(item -> item.getValue(), Function.identity()));
//        Map<?,?> map5=collect5;
//        Map<String,ZSetOperations.TypedTuple<Object>> before5Map =(Map<String,ZSetOperations.TypedTuple<Object>>)map5;
//
//        //近20日
//        List<BrokerStatistics> brokerStatistics20 = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>().eq("SECCODE", code).eq("F001D", rdate20));
//        Map<String, BrokerStatistics> before20BrokerStatisticsMap = brokerStatistics20.stream().
//                collect(Collectors.toMap(item -> item.getF002v(), Function.identity()));
//        //从redis中获取到近20日的持股变动量集合
//        Set<ZSetOperations.TypedTuple<Object>> before20Redis = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_20DAY + code, 0, -1);
//        Map<Object, ZSetOperations.TypedTuple<Object>> collect20 = before20Redis.stream().collect(Collectors.toMap(item -> item.getValue(), Function.identity()));
//        Map<?,?> map20=collect20;
//        Map<String,ZSetOperations.TypedTuple<Object>> before20Map =(Map<String,ZSetOperations.TypedTuple<Object>>)map20;
//
//        //近60日
//        List<BrokerStatistics> brokerStatistics60 = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>().eq("SECCODE", code).eq("F001D", rdate60));
//        Map<String, BrokerStatistics> before60BrokerStatisticsMap = brokerStatistics60.stream().
//                collect(Collectors.toMap(item -> item.getF002v(), Function.identity()));
//        //从redis中获取到近60日的持股变动量集合
//        Set<ZSetOperations.TypedTuple<Object>> before60Redis = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_60DAY + code, 0, -1);
//        Map<Object, ZSetOperations.TypedTuple<Object>> collect60 = before60Redis.stream().collect(Collectors.toMap(item -> item.getValue(), Function.identity()));
//        Map<?,?> map60=collect60;
//        Map<String,ZSetOperations.TypedTuple<Object>> before60Map =(Map<String,ZSetOperations.TypedTuple<Object>>)map60;
//
//        //近一年
//        List<BrokerStatistics> brokerStatistics360 = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>().eq("SECCODE", code).eq("F001D", rdate360));
//        Map<String, BrokerStatistics> before360BrokerStatisticsMap = brokerStatistics360.stream().
//                collect(Collectors.toMap(item -> item.getF002v(), Function.identity()));
//        //从redis中获取到近360日的持股变动量集合
//        Set<ZSetOperations.TypedTuple<Object>> before360Redis = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_360DAY + code, 0, -1);
//        Map<Object, ZSetOperations.TypedTuple<Object>> collect360 = before360Redis.stream().collect(Collectors.toMap(item -> item.getValue(), Function.identity()));
//        Map<?,?> map360=collect360;
//        Map<String,ZSetOperations.TypedTuple<Object>> before360Map =(Map<String,ZSetOperations.TypedTuple<Object>>)map360;
//
//        //获取总股本
//        Xnhk0102 xnhk0102 = xnhk0102Mapper.selectOne(new QueryWrapper<Xnhk0102>().eq("seccode", code));
//        //流通股
//        BigDecimal f069n = xnhk0102 == null ? BigDecimal.ZERO:xnhk0102.getF069n();
//        //发行股
//        BigDecimal f070n = xnhk0102 == null ? BigDecimal.ZERO:xnhk0102.getF070n();
//        //除权因子
//        BigDecimal factor = BigDecimal.ONE;
//        Xnhk0127 xnhk0127 = xnhk0127Mapper.selectOne(new QueryWrapper<Xnhk0127>().eq("seccode", code).ge("f003d",LocalDate.now()).in("f002v",Lists.newArrayList("SC","SS")));
//        if(Objects.nonNull(xnhk0127) && xnhk0102.getModifiedDate().getTime() >= DateUtils.localDateToLong(LocalDate.now())){
//            factor = xnhk0127.getF004n();
//        }
//        //创建一个空的集合
//        List<BrokersDetail> list=new ArrayList<>();
//        //如果流通股、发行股有数据
//        if(Objects.nonNull(f069n)&&f069n.compareTo(BigDecimal.ZERO)!=0&&Objects.nonNull(f070n)&&f070n.compareTo(BigDecimal.ZERO)!=0){
//            //计算今日总的持股比例
//            BigDecimal todayTotalBrokersHold = getTodayTotalBrokersHold(beforeBrokerStatisticsList, code, f070n);
//            //如果是今日，直接查询redis数据库
//            Set<ZSetOperations.TypedTuple<Object>> todayBuyAndSell = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_TODAY + code, 0, -1);
//            Iterator<ZSetOperations.TypedTuple<Object>> iterator = todayBuyAndSell.iterator();
//            while(iterator.hasNext()){
//                ZSetOperations.TypedTuple<Object> next = iterator.next();
//                //创建一个返回对象
//                BrokersDetail brokersDetail=new BrokersDetail();
//                //获取经纪商ID-redis缓存的value值
//                String tempBrokerId = (String) next.getValue();
//                String brokerId = brokerCommonUtils.getBrokerId(tempBrokerId);
//                brokersDetail.setBrokerId(brokerId);
//                //获取经纪商名称
//                String brokerName = brokerCommonUtils.getBrokerName(brokerId);
//                brokersDetail.setBrokerName(brokerName);
//                //将经纪商id为0和经纪商名称为空的数据过滤掉
//                if("0".equals(brokerId)||brokerId.length()==4){
//                    continue;
//                }
//                //获取今日持股变动数
//                BigDecimal todayHoldChange = BigDecimal.valueOf(next.getScore());
//                brokersDetail.setShareholdChange(todayHoldChange);
//                //查询前一日经纪商持股情况,获取到前一交易日的持股数
//                BigDecimal beforeHold=beforeBrokerStatisticsMap.get(brokerId)==null?BigDecimal.ZERO:beforeBrokerStatisticsMap.get(brokerId).getF003n();
//                //计算今日持股数=变动数+前日持股数
//                BigDecimal todayHold = beforeHold.add(BigDecimal.valueOf(next.getScore()));
//                brokersDetail.setShareHeld(todayHold);
//                //计算持股比例
//                //计算今日持股比例（占流通股）
//                BigDecimal todayShare = todayHold.divide(f069n,6,RoundingMode.HALF_UP).divide(factor);
//                brokersDetail.setShareholdingRatioOfCirculation(todayShare);
//                //计算持股比例（占发行股）
//                BigDecimal todayShareOfIssue= todayHold.divide(f070n,6,RoundingMode.HALF_UP).divide(factor);
//                brokersDetail.setShareholdingRatioOfIssue(todayShareOfIssue);
//                //计算总持股比例
//                brokersDetail.setTotalHolding(todayTotalBrokersHold);
//                //获取数据更新时间
//                brokersDetail.setDate(date);
//                if(type==0){
//                    //变动数量
//                    Double score = next.getScore();
//                    if (Objects.isNull(score)) {
//                        brokersDetail.setShareholdChange(null);
//                    } else {
//                        brokersDetail.setShareholdChange(BigDecimal.valueOf(score));
//                    }
//                    //计算今日持股变动比例=今日变动数/今日流通股数
//                    //计算持股变动比例（占发行股）
//                    brokersDetail.setShareholdChangeOfCirculation(todayHoldChange.divide(f070n,6,RoundingMode.HALF_UP));
//                }else if(type==1){
//                    //持股变动数
//                    BigDecimal before5Change=before5Map.get(brokerId)==null?BigDecimal.ZERO:BigDecimal.valueOf(before5Map.get(brokerId).getScore());
//                    brokersDetail.setShareholdChange(before5Change);
//                    //前一日持股比例，加%的
//                    BigDecimal f004n=before5BrokerStatisticsMap.get(brokerId)==null?BigDecimal.ZERO:before5BrokerStatisticsMap.get(brokerId).getF004n();
//                    //计算持股变动比例=今日持股比例-区间初持股比例
//                    brokersDetail.setShareholdChangeOfCirculation(todayShareOfIssue.subtract(f004n.divide(BigDecimal.valueOf(100),6,RoundingMode.HALF_UP)));
//
//                }else if(type==3){
//                    //持股变动数
//                    BigDecimal before20Change=before20Map.get(brokerId)==null?BigDecimal.ZERO:BigDecimal.valueOf(before20Map.get(brokerId).getScore());
//                    brokersDetail.setShareholdChange(before20Change);
//
//                    //计算持股变动比例=今日持股比例-区间初持股比例
//                    BigDecimal f004n=before20BrokerStatisticsMap.get(brokerId)==null?BigDecimal.ZERO:before20BrokerStatisticsMap.get(brokerId).getF004n();
//                    brokersDetail.setShareholdChangeOfCirculation(todayShareOfIssue.subtract(f004n.divide(BigDecimal.valueOf(100),6,RoundingMode.HALF_UP)));
//                }else if(type==4){
//                    //持股变动数
//                    BigDecimal before60Change=before60Map.get(brokerId)==null?BigDecimal.ZERO:BigDecimal.valueOf(before60Map.get(brokerId).getScore());
//                    brokersDetail.setShareholdChange(before60Change);
//
//                    //计算持股变动比例=今日持股比例-区间初持股比例
//                    BigDecimal f004n=before60BrokerStatisticsMap.get(brokerId)==null?BigDecimal.ZERO:before60BrokerStatisticsMap.get(brokerId).getF004n();
//                    brokersDetail.setShareholdChangeOfCirculation(todayShareOfIssue.subtract(f004n.divide(BigDecimal.valueOf(100),6,RoundingMode.HALF_UP)));
//
//                }else {
//                    //持股变动数
//                    BigDecimal before360Change=before360Map.get(brokerId)==null?BigDecimal.ZERO:BigDecimal.valueOf(before360Map.get(brokerId).getScore());
//                    brokersDetail.setShareholdChange(before360Change);
//
//                    //计算持股变动比例=今日持股比例-区间初持股比例
//                    BigDecimal f004n=before360BrokerStatisticsMap.get(brokerId)==null?BigDecimal.ZERO:before360BrokerStatisticsMap.get(brokerId).getF004n();
//                    brokersDetail.setShareholdChangeOfCirculation(todayShareOfIssue.subtract(f004n.divide(BigDecimal.valueOf(100),6,RoundingMode.HALF_UP)));
//                }
//                list.add(brokersDetail);
//            }
//            if(list.size()<= 0){
//                log.info("error");
//            }
//            //List<BrokersDetail> brokersDetailList = list.stream().sorted(Comparator.comparing(BrokersDetail::getShareHeld).reversed()).collect(Collectors.toList());
//            //排序字段
//            sort(list,sortKey,sort);
//            PageDomain<BrokersDetail> pageDomain=new PageDomain<>();
//            pageDomain.setRecords(list);
//            pageDomain.setTotal(list.size());
//            pageDomain.setSize(pageSize);
//            pageDomain.setCurrent(currentPage);
//            getPageList(pageDomain,currentPage,pageSize);
//            return ResultT.success(pageDomain);
//
//
//        }else{
//            PageDomain<BrokersDetail> pageDomain=new PageDomain<>();
//            pageDomain.setRecords(list);
//            return ResultT.success(pageDomain);
//        }
//    }

    @Override
    public void updateBrokerDatetime() {

        BrokerStatisticsParams brokerStatisticsReq = new BrokerStatisticsParams();
        brokerStatisticsReq.setPeriod(0);
        brokerStatisticsReq.setSize(10);
        brokerStatisticsReq.setSymbol("00700.hk");
        brokerStatisticsReq.setType("");
        ResultT<BrokerStatisticsRes> brokerStatisticsRespResultT = brokerInfoServiceApi.getBrokerStatistics(brokerStatisticsReq);
        if (brokerStatisticsRespResultT.getCode() != 200) {
            log.error("融聚汇获取经纪商成交统计异常!");
        } else {
            if(StringUtils.isNotBlank(brokerStatisticsRespResultT.getData().getUpdateTime())){
                redisClient.set(BrokerConstants.BROKER_UPDATETIME, brokerStatisticsRespResultT.getData().getUpdateTime());
            }
            log.info("经纪商数据更新时间:{}",brokerStatisticsRespResultT.getData().getUpdateTime());
        }
    }

    @Override
    public void updateBrokerStatisticsJob(String code,Xnhk0102 xnhk0102) {
        //非交易日不计算当日净买卖
//        if(!hkTradingCalendarApi.isTradingDay(LocalDate.now())){
//            return;
//        }
        LocalDate date = hkTradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate();
        List<BrokerStatistics> brokerStatistics = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("seccode", code).eq("f001d", DateUtils.localDateToF001D(date)));
        Set<String> brokerSet = brokerStatistics.stream().map(item -> item.getF002v()).collect(Collectors.toSet());
        Map<String, BrokerStatistics> Xnhk0609Map = getXnhk0609Map(brokerStatistics);

        BigDecimal ordinaryShares = BigDecimal.ZERO;
        if(ObjectUtils.isNotEmpty(xnhk0102)){
            ordinaryShares = xnhk0102.getF070n();
        }

        //获取股票最新价，股票昨日收盘价
        StockSnapshot snapshot = stockService.getOnlyStockSnapshot(code);
        //获取到该支股票的最新价,昨收价
        BigDecimal last = snapshot.getLast() == null ? BigDecimal.ZERO : snapshot.getLast();

        BrokerStatisticsParams brokerStatisticsReq = new BrokerStatisticsParams();
        brokerStatisticsReq.setPeriod(0);
        brokerStatisticsReq.setSize(10000);
        brokerStatisticsReq.setSymbol(code);
        brokerStatisticsReq.setType("");
        ResultT<BrokerStatisticsRes> brokerStatisticsRespResultT = brokerInfoServiceApi.getBrokerStatistics(brokerStatisticsReq);
        Map<String,BigDecimal> netMap = new HashMap<>();
        if(brokerStatisticsRespResultT.getCode() == 200){
            List<BrokerDetailsRes> buy = brokerStatisticsRespResultT.getData().getBuy();
            if(!CollectionUtils.isEmpty(buy)){
                for (BrokerDetailsRes brokerDetailsRes : buy) {
                    if (brokerDetailsRes.getBrokerId() == null) {
                        continue;
                    }
                    brokerDetailsRes.setBrokerId(brokerCommonUtils.getBrokerId(brokerDetailsRes.getBrokerId()));
                    brokerSet.add(brokerCommonUtils.getBrokerId(brokerDetailsRes.getBrokerId()));
                }
                Map<String, BigDecimal> buyMap = buy.stream().collect(Collectors.toMap(BrokerDetailsRes::getBrokerId, BrokerDetailsRes::getNetVolume, (v1, v2)->v1));
                netMap.putAll(buyMap);
            }
            List<BrokerDetailsRes> sell = brokerStatisticsRespResultT.getData().getSell();
            if(!CollectionUtils.isEmpty(sell)){
                for (BrokerDetailsRes brokerDetailsRes : sell) {
                    if (brokerDetailsRes.getBrokerId() == null) {
                        continue;
                    }
                    brokerDetailsRes.setBrokerId(brokerCommonUtils.getBrokerId(brokerDetailsRes.getBrokerId()));
                    brokerSet.add(brokerCommonUtils.getBrokerId(brokerDetailsRes.getBrokerId()));
                }
                Map<String, BigDecimal> sellMap = sell.stream().collect(Collectors.toMap(BrokerDetailsRes::getBrokerId, BrokerDetailsRes::getNetVolume, (v1, v2)->v1));
                netMap.putAll(sellMap);
            }
        }

        //除权因子
        BigDecimal factor = BigDecimal.ONE;
        Xnhk0127 xnhk0127 = xnhk0127Mapper.selectOne(new QueryWrapper<Xnhk0127>().eq("seccode", code).ge("f003d",LocalDate.now()).in("f002v",Lists.newArrayList("SC","SS")));
        if(Objects.nonNull(xnhk0127) && xnhk0102.getModifiedDate().getTime() >= DateUtils.localDateToLong(LocalDate.now())){
            factor = xnhk0127.getF004n();
        }

        for (String brokerId : brokerSet) {
            log.info("实时股票：{}-经纪商:{}", code , brokerId);
            redisClient.zSet(BrokerConstants.BROKER_TODAY + code, brokerId, netMap.get(brokerId)== null ? 0.0 : netMap.get(brokerId).doubleValue());
            redisClient.zSet(BrokerConstants.BROKER_TODAY + brokerId, code, netMap.get(brokerId)== null ? 0.0 : netMap.get(brokerId).doubleValue());
        }
    }

    /**
     * List<Xnhk0609> xnhk0609s
     *
     * @param xnhk0609s
     * @return
     */
    private Map<String, BrokerStatistics> getXnhk0609Map(List<BrokerStatistics> xnhk0609s) {

        return xnhk0609s.stream().
                collect(Collectors.toMap(item -> item.getF002v(), Function.identity()));
    }

    @Override
    public void updateBrokerInfoJob() {
        BrokerConstants.BG_BROKER_ID_PROFIT_MAP.clear();
        BrokerConstants.BG_BROKER_ID_RELATION_PROFIT_MAP.clear();
        //20221227改版 经纪商名单从0609表中获取
//        List<String> brokerIdList = xnhk0609Mapper.getBrokerIdList();
        List<Xnhk0610> xnhk0610s = xnhk0610Mapper.selectList(null);
        Map<String, String> brokerIdAndName = xnhk0610s.stream().collect(Collectors.toMap(Xnhk0610::getF001v, Xnhk0610::getF005v));
        List<SehkEp> sehkEps = sehkEpMapper.selectList(null);
        Map<String, String> shortNameMap = sehkEps.stream().collect(Collectors.toMap(SehkEp::getParticipantId, SehkEp::getShortName));
        //匹配对应的简称(根据经纪商id后四位)
        brokerIdAndName.forEach((k,v) -> {
            if(shortNameMap.containsKey(k.substring(2,6))){
                brokerIdAndName.put(k,shortNameMap.get(k.substring(2,6)));
            }
            if("A00003".equals(k)){
                brokerIdAndName.put(k,"沪港通");
            }
            if("A00004".equals(k)){
                brokerIdAndName.put(k,"深港通");
            }
        });

        List<BrokerSearch> brokerList = new ArrayList<>();
        brokerIdAndName.forEach((k,v) -> {
            BrokerConstants.BG_BROKER_ID_PROFIT_MAP.put(k,v);
            BrokerConstants.BG_BROKER_ID_RELATION_PROFIT_MAP.put(k.substring(2,6),k);
//            redisClient.set(BrokerConstants.BG_BROKER_ID_PROFIT.concat(k), v); // todo 放本地
//            redisClient.set(BrokerConstants.BG_BROKER_ID_RELATION_PROFIT.concat(k.substring(2,6)), k); // todo 放本地
            brokerList.add(BrokerSearch.builder().brokerId(k).brokerName(v).build());
        });
        redisClient.set(BrokerConstants.BG_BROKER_ID_PROFIT_LIST,brokerList);

        //将行业和股票关系维护一份到redis，使用时方便
        // List<Xnhks0104> xnhks0104s = xnhks0104Mapper.selectList(new QueryWrapper<>());
        // for (Xnhks0104 xnhks0104 : xnhks0104s) {
        //     redisClient.set(BrokerConstants.BROKER_CODE_INDUSTRY_PROFIT.concat(xnhks0104.getSeccode()), xnhks0104.getF014v());
        // }
//        List<StockIndustry> stockIndustries = industrySubsidiaryService.getStockIndustries(null);
//        for (StockIndustry industry : stockIndustries) {
//            redisClient.set(BrokerConstants.BROKER_CODE_INDUSTRY_PROFIT.concat(industry.getStockCode()), industry.getIndustryName()); // todo 放本地
//        }

    }

    @Override
    public void updateNetTradeBrokerJob(String code, LocalDate date) {
        brokerStatisticsMapper.delete(new QueryWrapper<BrokerStatistics>()
                .eq("f001d", date.toString().replaceAll("-", ""))
                .eq("seccode", code));
        /**
         *
         * 1、更新 t_broker_statistics 表中上一个交易日的持股数据
         * 2、给 t_broker_statistics 表落当日交易日的持股数据
         * 3、计算前4天，前9天变动量存redis
         * 4、计算30天内前五的持股经纪商
         * 5、今日前5、10经纪商持股比例之和
         * 6、计算股票的持仓统计
         *
         *
         * 日终数据落库
         */
        // 查询快照，使用昨收
        StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(code));
        BigDecimal price = BigDecimal.ZERO;
        if(ObjectUtils.isNotEmpty(snapshot)){
            price = snapshot.getPreClose();
        }


        //获取当前日期的上一个交易日
        Long hkexTdBeginDate = Long.parseLong(hkTradingCalendarApi.getBeforeTradingCalendar(date).getDate().toString().replace("-", ""));

//
//        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        //获取当日价格
//        String timeStrToday = fmt.format(date)+" 00:00:00";
//        List<KlineEntity> klineEntitiesToday = klineDailyApi.getTimeList(Arrays.asList(code),timeStrToday);


        long time1 = System.currentTimeMillis();
        log.info("================落当日交易日的持股数据======耗时1 :{}===",System.currentTimeMillis()-time1);
        Boolean tradeTodayFlag = hkTradingCalendarApi.isTradingDay(date);

        List<BrokerSearch> brokerSearchList = redisClient.get(BrokerConstants.BG_BROKER_ID_PROFIT_LIST);
        List<String> nameList = brokerSearchList.stream().map(item -> item.getBrokerId()).collect(Collectors.toList());

        if (tradeTodayFlag) {
            long time2 = System.currentTimeMillis();
            /**
             * 1、计算持股数量、持股数量百分比、持股市值（日期、股票、经纪商ID）
             */
            //上一天数据
            List<BrokerStatistics> queryBrokerStatistics = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>().eq("F001D", hkexTdBeginDate).eq("SECCODE", code));
            //如果当天是交易日，获取价格，计算市值
            for (BrokerStatistics x : queryBrokerStatistics) {
                //获取日期的下一个交易日
                x.setEndPrice(price);
                x.setEndPriceOrg(price);
                x.setF001d(Long.parseLong(hkTradingCalendarApi.getNextTradingCalendar(LocalDate.parse(x.getF001d().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))).getDate().toString().replace("-", "")));
                x.setCreateDate(new Date());
                x.setModifiedDate(new Date());
            }

            //获取当天所有净买卖数据
            Set<ZSetOperations.TypedTuple<Object>> all = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_TODAY + code, 0, -1);

            List<BrokerStatistics> brokerStatistics = new ArrayList<>();
            //流通股 XNHK0102中F069N
            Xnhk0102 xnhk0102 = xnhk0102Mapper.selectOne(new QueryWrapper<Xnhk0102>()
                    .eq("seccode", code));

            if (ObjectUtil.isEmpty(xnhk0102)) {
                log.info("================落当日交易日的持股数据======uts查询不到， code:{}===", code);
                return;
            }

            //todo 这个地方这么做，下面的改下
            //此处调整为循环所有的经纪商id（all + queryBrokerStatistics）. 如果经纪商ID 不在id 列表里面，return(这个经纪商主要是当日实时接口会有这种情况)
            //持有量 = all 持有量 + queryBrokerStatistics 持有量
            //持股比例 = 持有量 / F070N
            //市值 = 持有量 * 价格

            Map<String, Double> allMap = all.stream().collect(Collectors.toMap(item -> String.valueOf(item.getValue()), item -> item.getScore()));
            Map<String, BrokerStatistics> queryMap = queryBrokerStatistics.stream().filter(item -> item.getF003n().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toMap(BrokerStatistics::getF002v, Function.identity()));
            Set<String> brokerSet = all.stream().map(item -> String.valueOf(item.getValue())).collect(Collectors.toSet());
            Set<String> querySet = queryBrokerStatistics.stream().map(item -> item.getF002v()).collect(Collectors.toSet());
            brokerSet.addAll(querySet);
            brokerSet = brokerSet.stream().filter(item -> nameList.contains(item)).collect(Collectors.toSet());
            for (String id : brokerSet) {
                //获取到经纪商ID
                String brokerId = id;
                //获取到今日净买入数据
                BigDecimal score = allMap.get(id) == null ? BigDecimal.ZERO : BigDecimal.valueOf(allMap.get(id));
                //当天参与者持股数量
                BrokerStatistics queryData = queryMap.get(id);
                BigDecimal f003n = queryData == null ? score : score.add(queryData.getF003n());
                //当天参与者持股比例
                BigDecimal f004n = calcCashScale(f003n,xnhk0102.getF069n(),7).multiply(BigDecimal.valueOf(100));

                BrokerStatistics b = new BrokerStatistics();
                b.setSeccode(code);
                b.setF001d(Long.parseLong(date.toString().replace("-", "")));
                b.setF002v(brokerId);
                b.setF003n(f003n);
                b.setF003nOrg(f003n);
                b.setF004n(f004n);
                b.setF014v(brokerCommonUtils.getBrokerIndustry(code));
                b.setEndPrice(price);
                b.setEndPriceOrg(price);
                b.setMarketVal(f003n.multiply(price));
                b.setCreateDate(new Date());
                b.setModifiedDate(new Date());
                brokerStatistics.add(b);
            }
            //数据落库
            batchSave(brokerStatistics);


            log.info("================落当日交易日的持股数据======耗时1 :{}===",System.currentTimeMillis()-time2);
        }
    }

    @Override
    public void updateRedisBrokerJob(String code, LocalDate date) {
        /**
         *
         * 1、更新 t_broker_statistics 表中上一个交易日的持股数据
         * 2、给 t_broker_statistics 表落当日交易日的持股数据
         * 3、计算前4天，前9天变动量存redis
         * 4、计算30天内前五的持股经纪商
         * 5、今日前5、10经纪商持股比例之和
         * 6、计算股票的持仓统计
         */

        //2.1 维护表中行业字段
//        brokerStatisticsMapper.updateBrokerStatistics(code, LocalDate.now().atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli());
        // 3、计算前4天，前9天变动量存redis
        //如果当天是交易日，则缓存当天开始前四天的交易数据，如果当天不是交易日，则存当天开始前五天的交易数据
        Boolean tradeFlag = hkTradingCalendarApi.isTradingDay(date);

        //获取上一个交易日期
        Long hkexTdDateEnd = Long.parseLong(hkTradingCalendarApi.getBeforeTradingCalendar(date).getDate().toString().replace("-", ""));

        //获取上上一个交易日期
        Long hkexTdDateStart = hkTradingCalendarApi.queryBeforeTradingCalendars(LocalDate.now(),2);

        //获取期末数据
        List<BrokerStatistics> xnhk0609End = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("f001d", hkexTdDateEnd)
                .eq("seccode", code));
        Long netSalesDateEnd = hkexTdDateEnd;
        netSalesDateEnd = netSalesComplement(xnhk0609End,code,hkexTdDateEnd);

        Long hkexTdDateEnd5;
        Long hkexTdDateEnd10;
        Long hkexTdDateEnd20;
        Long hkexTdDateEnd60;
        LocalDate previousYear = date.minus(1, ChronoUnit.YEARS);
        Long hkexTdDateEnd360 = Long.parseLong(hkTradingCalendarApi.getBeforeTradingCalendar(previousYear).getDate().toString().replace("-", ""));
        //若是交易日，查近四日，否则查净五日
        if (tradeFlag) {
            //获取最近四天的变动量
            hkexTdDateEnd5 = hkTradingCalendarApi.queryBeforeTradingCalendars(LocalDate.now(),5);
            hkexTdDateEnd10 = hkTradingCalendarApi.queryBeforeTradingCalendars(LocalDate.now(),10);
            hkexTdDateEnd20 = hkTradingCalendarApi.queryBeforeTradingCalendars(LocalDate.now(),20);
            hkexTdDateEnd60 = hkTradingCalendarApi.queryBeforeTradingCalendars(LocalDate.now(),60);
        } else {
            hkexTdDateEnd5 = hkTradingCalendarApi.queryBeforeTradingCalendars(LocalDate.now(),6);
            hkexTdDateEnd10 = hkTradingCalendarApi.queryBeforeTradingCalendars(LocalDate.now(),11);
            hkexTdDateEnd20 = hkTradingCalendarApi.queryBeforeTradingCalendars(LocalDate.now(),21);
            hkexTdDateEnd60 = hkTradingCalendarApi.queryBeforeTradingCalendars(LocalDate.now(),61);
        }

        List<BrokerStatistics> xnhk0609Start5 = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("f001d", hkexTdDateEnd5)
                .eq("seccode", code));
        netSalesComplement(xnhk0609Start5,code,hkexTdDateEnd5);

        List<BrokerStatistics> xnhk0609Start10 = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("f001d", hkexTdDateEnd10)
                .eq("seccode", code));
        netSalesComplement(xnhk0609Start10,code,hkexTdDateEnd10);

        List<BrokerStatistics> xnhk0609Start20 = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("f001d", hkexTdDateEnd20)
                .eq("seccode", code));
        netSalesComplement(xnhk0609Start20,code,hkexTdDateEnd20);

        List<BrokerStatistics> xnhk0609Start60 = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("f001d", hkexTdDateEnd60)
                .eq("seccode", code));
        netSalesComplement(xnhk0609Start60,code,hkexTdDateEnd60);

        List<BrokerStatistics> xnhk0609Start360 = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("f001d", hkexTdDateEnd360)
                .eq("seccode", code));
        netSalesComplement(xnhk0609Start360,code,hkexTdDateEnd360);

        long time4 = System.currentTimeMillis();
        for (BrokerStatistics brokerStatistics1 : xnhk0609End) {
            //近五天变动量
            if(hkexTdDateEnd5 < netSalesDateEnd){
                for (BrokerStatistics brokerStatistics5 : xnhk0609Start5) {
                    if (brokerStatistics1.getF002v().equals(brokerStatistics5.getF002v()))
                        redisClient.zSet(BrokerConstants.BROKER_5DAY + brokerStatistics1.getSeccode(), brokerStatistics5.getF002v(), ((brokerStatistics1 == null ? BigDecimal.ZERO : brokerStatistics1.getF003n()).subtract(brokerStatistics5 == null ? BigDecimal.ZERO : brokerStatistics5.getF003n())).doubleValue());
                }
            }

            if(hkexTdDateEnd10 < netSalesDateEnd) {
                for (BrokerStatistics brokerStatistics10 : xnhk0609Start10) {
                    if (brokerStatistics1.getF002v().equals(brokerStatistics10.getF002v()))
                        redisClient.zSet(BrokerConstants.BROKER_10DAY + brokerStatistics1.getSeccode(), brokerStatistics10.getF002v(), ((brokerStatistics1 == null ? BigDecimal.ZERO : brokerStatistics1.getF003n()).subtract(brokerStatistics10 == null ? BigDecimal.ZERO : brokerStatistics10.getF003n())).doubleValue());
                }
            }

            if(hkexTdDateEnd20 < netSalesDateEnd) {
                for (BrokerStatistics brokerStatistics20 : xnhk0609Start20) {
                    if (brokerStatistics1.getF002v().equals(brokerStatistics20.getF002v()))
                        redisClient.zSet(BrokerConstants.BROKER_20DAY + brokerStatistics1.getSeccode(), brokerStatistics20.getF002v(), ((brokerStatistics1 == null ? BigDecimal.ZERO : brokerStatistics1.getF003n()).subtract(brokerStatistics20 == null ? BigDecimal.ZERO : brokerStatistics20.getF003n())).doubleValue());
                }
            }

            if(hkexTdDateEnd60 < netSalesDateEnd) {
                for (BrokerStatistics brokerStatistics60 : xnhk0609Start60) {
                    if (brokerStatistics1.getF002v().equals(brokerStatistics60.getF002v()))
                        redisClient.zSet(BrokerConstants.BROKER_60DAY + brokerStatistics1.getSeccode(), brokerStatistics60.getF002v(), ((brokerStatistics1 == null ? BigDecimal.ZERO : brokerStatistics1.getF003n()).subtract(brokerStatistics60 == null ? BigDecimal.ZERO : brokerStatistics60.getF003n())).doubleValue());
                }
            }

            if(hkexTdDateEnd360 < netSalesDateEnd) {
                for (BrokerStatistics brokerStatistics360 : xnhk0609Start360) {
                    if (brokerStatistics1.getF002v().equals(brokerStatistics360.getF002v()))
                        redisClient.zSet(BrokerConstants.BROKER_360DAY + brokerStatistics1.getSeccode(), brokerStatistics360.getF002v(), ((brokerStatistics1 == null ? BigDecimal.ZERO : brokerStatistics1.getF003n()).subtract(brokerStatistics360 == null ? BigDecimal.ZERO : brokerStatistics360.getF003n())).doubleValue());
                }
            }
        }

        log.info("================近五天变动量======耗时1 :{}===",System.currentTimeMillis()-time4);
        //4、计算30天内前五的持股经纪商
        long time5 = System.currentTimeMillis();
        Long hkexTdDate = hkTradingCalendarApi.queryBeginTradingCalendars(30).getRdate();
        log.info("前30个交易日查询日期：{}", hkexTdDate);
        List<BrokerStatistics> brokerStatisticTop5 = brokerStatisticsMapper.queryBrokerTop5(code, hkexTdDate);
        log.info("前30个交易日查询数据大小:{}", brokerStatisticTop5.size());
        for (BrokerStatistics brokerStatistics : brokerStatisticTop5) {
            redisClient.zSet(BrokerConstants.BROKER_TOP5 + code, brokerStatistics.getF002v(), (brokerStatistics == null ? 0 : brokerStatistics.getF004n()).doubleValue());
        }

        log.info("================计算30天内前五的持股经纪商======耗时1 :{}===",System.currentTimeMillis()-time5);
        long time6 = System.currentTimeMillis();

        //5、获取上一个交易日的前5、10经纪商持股比例之和。作为期末集中度
        BrokerStatistics brokerStatisticTop5Change = brokerStatisticsMapper.queryBrokerTop5Change(code, hkexTdDateEnd);
        BrokerStatistics brokerStatisticTop10Change = brokerStatisticsMapper.queryBrokerTop10Change(code, hkexTdDateEnd);
        redisClient.zSet(BrokerConstants.BROKER_TOP5CONCENTRATION, code, brokerStatisticTop5Change == null ? 0 : (brokerStatisticTop5Change.getF004n()).divide(BigDecimal.valueOf(100)).doubleValue());
        redisClient.zSet(BrokerConstants.BROKER_TOP10CONCENTRATION, code,brokerStatisticTop10Change == null ? 0 : (brokerStatisticTop10Change.getF004n()).divide(BigDecimal.valueOf(100)).doubleValue());

        //20221227改版  获取上上一个交易日的前5、10经纪商持股比例之和。作为期初集中度
        BrokerStatistics brokerStatisticTop5ChangeStart = brokerStatisticsMapper.queryBrokerTop5Change(code, hkexTdDateStart);
        BrokerStatistics brokerStatisticTop10ChangeStart = brokerStatisticsMapper.queryBrokerTop10Change(code, hkexTdDateStart);
        if(brokerStatisticTop5Change!=null && brokerStatisticTop5ChangeStart!=null) {
            //计算集中度变动
            Double top5ChangeToday = calcSubtract(brokerStatisticTop5Change.getF004n(), brokerStatisticTop5ChangeStart.getF004n()).doubleValue();
            if (top5ChangeToday < 100 && top5ChangeToday > -100) {
                //过滤变动度超过100%的
                redisClient.zSet(BrokerConstants.BROKER_TOP5CHANGETODAY, code, top5ChangeToday);
            }
        }
        if(brokerStatisticTop10Change!=null && brokerStatisticTop10ChangeStart!=null) {
            Double top10ChangeToday = calcSubtract(brokerStatisticTop10Change.getF004n(), brokerStatisticTop10ChangeStart.getF004n()).doubleValue();
            if (top10ChangeToday < 100 && top10ChangeToday > -100) {
                //过滤变动度超过100%的
                redisClient.zSet(BrokerConstants.BROKER_TOP10CHANGETODAY, code, top10ChangeToday);
            }
        }

        //20221227改版 计算增减持排行榜 取上一个交易日和上上一个交易日的差值
        List<BrokerStatistics> xnhk0609Start = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("f001d", hkexTdDateStart)
                .eq("seccode", code));
        Map<String, BrokerStatistics> xnhk0609StartMap = xnhk0609Start.stream().collect(Collectors.toMap(BrokerStatistics::getF002v, Function.identity()));
        //重新查找区间末数据，避免被净买卖逻辑影响
        xnhk0609End = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("f001d", hkexTdDateEnd)
                .eq("seccode", code));
        for (BrokerStatistics end : xnhk0609End) {
            String brokerId = end.getF002v();
            if(xnhk0609StartMap.get(brokerId) == null){
                break;
            }
            Double brokerHoldingRatio = calcSubtract(end.getF004n(), xnhk0609StartMap.get(brokerId).getF004n()).divide(BigDecimal.valueOf(100)).doubleValue();
            if(brokerHoldingRatio < 1 && brokerHoldingRatio > -1){
                //过滤增减持比例超过100%的
                redisClient.zSet(BrokerConstants.BROKER_TODAYQUANTITY,code.concat("-").concat(brokerId),brokerHoldingRatio);
            }
            redisClient.zSet(BrokerConstants.BROKER_TODAYMARKETVAL,code.concat("-").concat(brokerId),
                    calcSubtract(end.getMarketVal(),xnhk0609StartMap.get(brokerId) == null ? BigDecimal.ZERO : xnhk0609StartMap.get(brokerId).getMarketVal()).doubleValue());
        }

        //6、计算股票最近六个季度的持仓统计
        List<Long> dates = hkTradingCalendarApi.getLastSixTradingCalendars(date,7);

        List<BrokersPositionStatisticsDetail> brokersPositionStatisticsDetails = new ArrayList<>();


        List<BrokerHeldInfo> xnhk0608s1 = brokerHeldInfoMapper.selectList(new QueryWrapper<BrokerHeldInfo>()
                .eq("seccode", code)
                .in("f001d", dates));

        //循环到 xnhk0608s.size()-1，是因为只需要取六条记录，第六条记录需要第七条记录才能获取
        //对xnhk0608s集合按时间倒叙
        List<BrokerHeldInfo> xnhk0608s = xnhk0608s1.stream().sorted(Comparator.comparing(BrokerHeldInfo::getF001d).reversed()).collect(Collectors.toList());
        for(int i = 0;i<xnhk0608s.size()-1;i++){
            BrokersPositionStatisticsDetail brokersPositionStatisticsDetail = new BrokersPositionStatisticsDetail();
            //持股日期
            brokersPositionStatisticsDetail.setF001d(DateUtils.longDateToLongMS(xnhk0608s.get(i).getF001d()));
            //持股总数
            brokersPositionStatisticsDetail.setShareholdTotal(xnhk0608s.get(i).getF013n());
            //总持股比例（占流通股）
            brokersPositionStatisticsDetail.setShareholdTotalRatio(calcCash(xnhk0608s.get(i).getF014n(),new BigDecimal(100)));
            //变动股数(当季度末持股总量-上季度末持股总量)
            brokersPositionStatisticsDetail.setShareholdChange(xnhk0608s.get(i).getF013n().subtract(xnhk0608s.get(i+1).getF013n()));
            //变动比例(当季度末总持股比例-上季度末总持股比例)
            brokersPositionStatisticsDetail.setShareholdChangeRatio(calcCash(xnhk0608s.get(i).getF014n().subtract(xnhk0608s.get(i+1).getF014n()),new BigDecimal(100)));
            //总数（家）
            brokersPositionStatisticsDetail.setBrokersTotal(xnhk0608s.get(i).getF003n());
            //新增(用当季度末名单比对上季度末名单)
            brokersPositionStatisticsDetail.setBrokersAdd(getBrokersChange(xnhk0608s.get(i),xnhk0608s.get(i+1),"1"));
            //消失(用当季度末名单比对上季度末名单);
            brokersPositionStatisticsDetail.setBrokersReduce(getBrokersChange(xnhk0608s.get(i),xnhk0608s.get(i+1),"2"));
            //变动(当季度末总数-上季度末总数)
            brokersPositionStatisticsDetail.setBrokersChange(xnhk0608s.get(i).getF003n().subtract(xnhk0608s.get(i+1).getF003n()));
            //增持（家）(当季度末持股的经纪商持股数-该经纪商上季度末的持股数，取大于零的名单)
            brokersPositionStatisticsDetail.setBrokersIncreaseQuantity(getBrokersChange(xnhk0608s.get(i),xnhk0608s.get(i+1),"3"));
            //增持股数(当季度增持的经纪商持股数之和-增持经纪商的上季度末持股数之和)
            brokersPositionStatisticsDetail.setBrokersIncreaseSharehold(getBrokersChangeHold(xnhk0608s.get(i),xnhk0608s.get(i+1),"1"));
            //减持(当季度末持股的经纪商持股数-该经纪商上季度末的持股数，取小于零的名单)
            brokersPositionStatisticsDetail.setBrokersSubtractQuantity(getBrokersChange(xnhk0608s.get(i),xnhk0608s.get(i+1),"4"));
            //减持股数(当季度减持的经纪商持股数之和-减持经纪商上季度末持股数之和)
            brokersPositionStatisticsDetail.setBrokersSubtractSharehold(getBrokersChangeHold(xnhk0608s.get(i),xnhk0608s.get(i+1),"2"));
            //机构托管商(托管人持股百分比（占流通股）：F008N)
            //brokersPositionStatisticsDetail.setOrganizationShareHold(getOrganizationShareHoldRatio(xnhk0608s.get(i),"0"));
            brokersPositionStatisticsDetail.setOrganizationShareHold(calcCash(xnhk0608s.get(i).getF008n(),new BigDecimal(100)));
            //券商(券商持股百分比（占流通股）：F010N)
            brokersPositionStatisticsDetail.setBrokerShareHold(calcCash(xnhk0608s.get(i).getF010n(), new BigDecimal(100)));
            //其他中介（其他中介者持股百分比（占流通股）：F012N）
            brokersPositionStatisticsDetail.setOtherIntermediaryShareHold(calcCash(xnhk0608s.get(i).getF012n(), new BigDecimal(100)));
            //自愿披露投资者(愿意披露的投资者户口持有人持股百分比（占流通股）：F016N)
            brokersPositionStatisticsDetail.setVoluntaryInvestorsShareHold(calcCashScale(xnhk0608s.get(i).getF016n(), new BigDecimal(100),7));
            //不原披露投资者(不愿意披露的投资者户口持有人持股百分比（占流通股）：F018N)
            brokersPositionStatisticsDetail.setInvoluntaryInvestorsShareHold(calcCashScale(xnhk0608s.get(i).getF018n(), new BigDecimal(100),7));
//            //港股通(沪)
//            brokersPositionStatisticsDetail.setHkStockConnectShareHold(getOrganizationShareHoldRatio(xnhk0608s.get(i),"1"));
//            //港股通(深)
//            brokersPositionStatisticsDetail.setSzStockConnectShareHold(getOrganizationShareHoldRatio(xnhk0608s.get(i),"2"));
            brokersPositionStatisticsDetails.add(brokersPositionStatisticsDetail);
        }
        //不足6个季度的，最早季度存除差值以外的值
        if(!CollectionUtils.isEmpty(xnhk0608s) && brokersPositionStatisticsDetails.size() < 6){
            BrokersPositionStatisticsDetail brokersPositionStatisticsDetail = new BrokersPositionStatisticsDetail();
            Integer index = xnhk0608s.size() - 1;
            //持股日期
            brokersPositionStatisticsDetail.setF001d(DateUtils.longDateToLongMS(xnhk0608s.get(index).getF001d()));
            //持股总数
            brokersPositionStatisticsDetail.setShareholdTotal(xnhk0608s.get(index).getF013n());
            //总持股比例（占流通股）
            brokersPositionStatisticsDetail.setShareholdTotalRatio(calcCash(xnhk0608s.get(index).getF014n(),new BigDecimal(100)));
            //总数（家）
            brokersPositionStatisticsDetail.setBrokersTotal(xnhk0608s.get(index).getF003n());
            //机构托管商(托管人持股百分比（占流通股）：F008N)
            //brokersPositionStatisticsDetail.setOrganizationShareHold(getOrganizationShareHoldRatio(xnhk0608s.get(i),"0"));
            brokersPositionStatisticsDetail.setOrganizationShareHold(calcCash(xnhk0608s.get(index).getF008n(),new BigDecimal(100)));
            //券商(券商持股百分比（占流通股）：F010N)
            brokersPositionStatisticsDetail.setBrokerShareHold(calcCash(xnhk0608s.get(index).getF010n(), new BigDecimal(100)));
            //其他中介（其他中介者持股百分比（占流通股）：F012N）
            brokersPositionStatisticsDetail.setOtherIntermediaryShareHold(calcCash(xnhk0608s.get(index).getF012n(), new BigDecimal(100)));
            //自愿披露投资者(愿意披露的投资者户口持有人持股百分比（占流通股）：F016N)
            //保留7位
            brokersPositionStatisticsDetail.setVoluntaryInvestorsShareHold(calcCashScale(xnhk0608s.get(index).getF016n(), new BigDecimal(100),7));
            //不原披露投资者(不愿意披露的投资者户口持有人持股百分比（占流通股）：F018N)
            brokersPositionStatisticsDetail.setInvoluntaryInvestorsShareHold(calcCashScale(xnhk0608s.get(index).getF018n(), new BigDecimal(100),7));
            brokersPositionStatisticsDetails.add(brokersPositionStatisticsDetail);
        }
        redisClient.set(BrokerConstants.POSITION_STATISTICS_DETAIL + code, brokersPositionStatisticsDetails);
        log.info("================计算股票最近六个季度的持仓统计======耗时1 :{}===",System.currentTimeMillis()-time6);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<BrokerStatistics> result) {
        if (result.size() > 0) {
            brokerStatisticsMapper.batchSave(result);
        }
    }

    @Override
    public void updateIndustryStatisticsBatch(Long date, List<String> codeList) {
        Boolean hasDeleted;
        do {
            hasDeleted = brokerIndustryStatisticsMapper.delete(new QueryWrapper<BrokerIndustryStatistics>()
                    .eq("f001d", date)
                    .last("limit " + batchSize)) > 0;
        } while (hasDeleted);
        List<BrokerIndustryStatistics> brokerIndustryStatisticsList = Lists.newArrayList();

        List<BrokerStatistics> queryBrokerStatisticsList = Lists.newArrayList();
        List<List<String>> partition = Lists.partition(codeList.stream().collect(Collectors.toList()), 10);
        for (List<String> subCodes : partition) {
            try {
                List<BrokerStatistics> subLists = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                        .select("f001d", "f002v", "f014v", "market_val")
                        .eq("F001D", date)
                        .in("SECCODE", subCodes));
                if (!CollectionUtils.isEmpty(subLists)) {
                    queryBrokerStatisticsList.addAll(subLists);
                }
            } catch (Exception e) {
                log.error("批量获取经纪商数据异常", e);
            }
        }

        Map<String, BigDecimal> result = queryBrokerStatisticsList.stream().filter(item -> StringUtils.isNotEmpty(item.getF014v()))
                .collect(Collectors.groupingBy(
                        item -> item.getF001d() + "," + item.getF002v() + "," + item.getF014v(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                BrokerStatistics::getMarketVal,
                                BigDecimal::add)
                ));

        result.forEach((key, value) -> {
                    String[] split = key.split(",");
                    BrokerIndustryStatistics industryMarketValueStatistics = new BrokerIndustryStatistics();
                    industryMarketValueStatistics.setBrokerId(split[1]);
                    industryMarketValueStatistics.setIndustryName(split[2]);
                    industryMarketValueStatistics.setMarketVal(value);
                    industryMarketValueStatistics.setF001d(date);
                    brokerIndustryStatisticsList.add(industryMarketValueStatistics);
                }
        );
//        List<BrokerStatistics> brokerIndustryStatistics = brokerStatisticsMapper.getBrokerByIndustry(date, codeList);
//        for (BrokerStatistics industry : brokerIndustryStatistics) {
//            BrokerIndustryStatistics industryMarketValueStatistics = new BrokerIndustryStatistics();
//            //f001d,broker_id,industry_name,market_val
//            industryMarketValueStatistics.setBrokerId(industry.getF002v());
//            industryMarketValueStatistics.setIndustryName(industry.getF014v());
//            industryMarketValueStatistics.setMarketVal(industry.getMarketVal());
//            industryMarketValueStatistics.setF001d(date);
//            brokerIndustryStatisticsList.add(industryMarketValueStatistics);
//        }
        if(!CollectionUtils.isEmpty(brokerIndustryStatisticsList)){
            List<List<BrokerIndustryStatistics>> subBrokerIndustryStatisticsList = Lists.partition(brokerIndustryStatisticsList, 1000);
            for (List<BrokerIndustryStatistics> sub : subBrokerIndustryStatisticsList) {
                try {
                    brokerIndustryStatisticsMapper.insertIndustryMakValStatistics(sub);
                } catch (Exception e) {
                    log.error("经纪商行业维度表数据落库异常", e);
                }
            }
        }

    }
    /**
     * 根据股票code删除经纪商股票数据
     */
    @Async
    @Override
    public void delByStockCode(String stockCode) {
        long l = System.currentTimeMillis();
        log.info("根据股票code删除经纪商股票数据开始：stockCode：{}",stockCode);
        brokerHeldInfoMapper.delete(new QueryWrapper<BrokerHeldInfo>().eq("SECCODE",stockCode));
        brokerStatisticsMapper.delete(new QueryWrapper<BrokerStatistics>().eq("SECCODE",stockCode));
        log.info("根据股票code删除经纪商股票数据结束：stockCode：{} 耗时：{}",stockCode,System.currentTimeMillis()-l);
    }


    @Override
    public void updateIndustryBrokersProportionJob(Map<String, List<StockKline>> industryKlineMap) {
        long t = System.currentTimeMillis();
        // 取30个交易日的数据
        int limit = 30;
        // 取平均持仓市值前5的经纪商
        int topNum = 5;
        LocalDate startDate;
        LocalDate endDate = hkTradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate();
        List<BgTradingCalendar> tradeDateList = hkTradingCalendarApi.getLastTradingCalendars(endDate, limit).getData();
        tradeDateList.sort(Comparator.comparing(BgTradingCalendar::getDate));
        startDate = tradeDateList.get(0).getDate();
        log.info("行业-经纪商持股市值走势图开始startDate:{} endDate:{}", startDate, endDate);
        long longStartDate = Long.parseLong(startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        long longEndDate = Long.parseLong(endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<BrokerSearch> brokerList = redisClient.get(BrokerConstants.BG_BROKER_ID_PROFIT_LIST);
        Map<String, String> brokerIdAndNameMap = brokerList.stream().collect(Collectors.toMap(BrokerSearch::getBrokerId, BrokerSearch::getBrokerName));
        log.info("brokerIdAndNameMap:{}", brokerIdAndNameMap);
        List<IndustrySubsidiary> allIndustry = industrySubsidiaryService.getAllIndustry();
        Map<String, String> industryNameAndCodeMap = allIndustry.stream().collect(Collectors.toMap(IndustrySubsidiary::getName, IndustrySubsidiary::getCode));
        List<BrokerIndustryStatistics> list = new ArrayList<>();
        for(BgTradingCalendar bgTradingCalendar : tradeDateList){
            long longDate = Long.parseLong(bgTradingCalendar.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            List<BrokerIndustryStatistics> subList = brokerIndustryStatisticsMapper.selectListByRangeDate(longDate);
            if(CollectionUtils.isEmpty(subList)){
                list.addAll(subList);
            }
        }
        Map<String, List<BrokerIndustryStatistics>> industryDataMap = list.stream().collect(Collectors.groupingBy(BrokerIndustryStatistics::getIndustryName));
        // 获取行业收盘价、涨跌幅
        Set<String> industryCodes = industryDataMap.keySet().stream().map(industryNameAndCodeMap::get).filter(Objects::nonNull).collect(Collectors.toSet());
        Collection<String> missingIndustryNames = CollUtil.subtract(industryDataMap.keySet(), industryNameAndCodeMap.keySet());
        log.info("一共:{}个行业名称无法找到对应的行业code:{}", missingIndustryNames.size(), missingIndustryNames);
//        Map<String, List<StockKline>> industryKlineMap = getIndustryKlineMap(industryCodes, limit, endDate);
        Map<String, IndustryBrokersProportionDTO> resultMap = new HashMap<>(industryDataMap.size());
        for (String industryName : industryDataMap.keySet()) {
            String industryCode = industryNameAndCodeMap.get(industryName);
            if (StringUtils.isBlank(industryCode)) {
                continue;
            }
            IndustryBrokersProportionDTO industryBrokersProportionDTO = new IndustryBrokersProportionDTO();
            List<BrokerIndustryStatistics> brokerIndustryStatistics = industryDataMap.get(industryName);
            // 取该行业平均市值前5的经纪商
            Map<String, Double> averageMarketValByBrokerId = brokerIndustryStatistics.stream().collect(Collectors.groupingBy(BrokerIndustryStatistics::getBrokerId, Collectors.averagingLong(item -> item.getMarketVal().longValue())));
            List<String> topBrokerIds = averageMarketValByBrokerId.entrySet().stream()
                    .filter(item -> !(Objects.equals(item.getKey(), BrokerConstants.SH_BROKER_ID) || Objects.equals(item.getKey(), BrokerConstants.SZ_BROKER_ID)))
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(Math.min(topNum, averageMarketValByBrokerId.size()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            // 组装经纪商数据
            List<List<IndustryBrokersProportionDTO.BrokersProportionItem>> brokersProportionList = new ArrayList<>();
            for (String brokerId : topBrokerIds) {
                brokersProportionList.add(buildBrokersProportionItemList(brokerId, industryName, tradeDateList, brokerIndustryStatistics, brokerIdAndNameMap));
            }
            industryBrokersProportionDTO.setBrokersProportionList(brokersProportionList);
            industryBrokersProportionDTO.setBrokersProportionHKStockConnectShanghai(buildBrokersProportionItemList(BrokerConstants.SH_BROKER_ID, industryName, tradeDateList, brokerIndustryStatistics, brokerIdAndNameMap));
            industryBrokersProportionDTO.setBrokersProportionHKStockConnectShenzhen(buildBrokersProportionItemList(BrokerConstants.SZ_BROKER_ID, industryName, tradeDateList, brokerIndustryStatistics, brokerIdAndNameMap));
            // 组装行业信息
            industryBrokersProportionDTO.setStockPriceList(buildStockPriceItem(tradeDateList, industryKlineMap, industryCode, industryName));
            resultMap.put(industryCode, industryBrokersProportionDTO);
        }
        // 保存至redis
        redisClient.del(BrokerConstants.BROKER_INDUSTRY_VIEW);
        redisClient.hmset(BrokerConstants.BROKER_INDUSTRY_VIEW, resultMap);
        log.info("缓存行业-经纪商持股市值走势图耗时, {}", System.currentTimeMillis() - t);
    }

    @Override
    public IndustryBrokersProportionDTO getIndustryBrokersProportion(String industryCode) {
        return redisClient.hget(BrokerConstants.BROKER_INDUSTRY_VIEW, industryCode);
    }

    private List<IndustryBrokersProportionDTO.BrokersProportionItem> buildBrokersProportionItemList(String brokerId, String industryName, List<BgTradingCalendar> tradeDateList, List<BrokerIndustryStatistics> brokerIndustryStatistics, Map<String, String> brokerIdAndNameMap) {
        if (brokerIndustryStatistics.stream().noneMatch(item -> Objects.equals(item.getBrokerId(), brokerId))) {
            return new ArrayList<>();
        }
        List<IndustryBrokersProportionDTO.BrokersProportionItem> list = new ArrayList<>(tradeDateList.size());
        for (BgTradingCalendar bgTradingCalendar : tradeDateList) {
            LocalDate date = bgTradingCalendar.getDate();
            IndustryBrokersProportionDTO.BrokersProportionItem brokersProportionItem = new IndustryBrokersProportionDTO.BrokersProportionItem();
            BrokerIndustryStatistics industryStatistics = brokerIndustryStatistics.stream()
                    .filter(item -> Objects.equals(item.getBrokerId(), brokerId) && Objects.equals(item.getF001d(), Long.parseLong(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")))))
                    .findFirst()
                    .orElse(null);
            brokersProportionItem.setBrokerId(brokerId);
            brokersProportionItem.setBrokerName(brokerIdAndNameMap.get(brokerId));
            brokersProportionItem.setDate(date.toString());
            brokersProportionItem.setTimestamp(LocalDateTimeUtil.getTimestamp(date));
            if (industryStatistics != null) {
                brokersProportionItem.setMarketVal(industryStatistics.getMarketVal());
            } else {
                log.info("未找到行业:{} 经纪商:{} 日期:{}的经纪商数据", industryName, brokerId, date);
            }
            list.add(brokersProportionItem);
        }
        return list;
    }

    private List<IndustryBrokersProportionDTO.StockPriceItem> buildStockPriceItem(List<BgTradingCalendar> tradeDateList, Map<String, List<StockKline>> industryKlineMap, String industryCode, String industryName) {
        List<IndustryBrokersProportionDTO.StockPriceItem> stockPriceItems = new ArrayList<>(tradeDateList.size());
        Map<String, Long> stockIdMap = stockCache.queryStockIdMap(null);
        for (BgTradingCalendar bgTradingCalendar : tradeDateList) {
            LocalDate date = bgTradingCalendar.getDate();
            List<StockKline> stockKlines = industryKlineMap.get(industryCode);
            IndustryBrokersProportionDTO.StockPriceItem stockPriceItem = new IndustryBrokersProportionDTO.StockPriceItem();
            if (CollUtil.isNotEmpty(stockKlines)) {
                StockKline stockKline = stockKlines.stream()
                        .filter(item -> Objects.equals(item.getTime(), LocalDateTimeUtil.getTimestamp(date)))
                        .findFirst()
                        .orElse(null);
                stockPriceItem.setIndustryId(stockIdMap.get(industryCode));
                stockPriceItem.setIndustryCode(industryCode);
                stockPriceItem.setIndustryName(industryName);
                stockPriceItem.setTimestamp(LocalDateTimeUtil.getTimestamp(date));
                stockPriceItem.setDate(date.toString());
                if (stockKline != null) {
                    stockPriceItem.setClose(stockKline.getClose());
                    stockPriceItem.setChgPct(stockKline.getChgPct());
                } else {
                    log.info("未找到行业:{} 日期:{}的K线数据", industryName, date);
                }
                stockPriceItems.add(stockPriceItem);
            } else {
                log.info("行业{}不存在K线数据", industryName);
            }
        }
        return stockPriceItems;
    }

    /**
         * 新增(用当季度末名单比对上季度末名单)
         * type = 1 : 新增
         * type = 2 : 减少
         * @param xnhk0608
         * @return
         */
    public Integer getBrokersChange(BrokerHeldInfo xnhk0608,BrokerHeldInfo xnhk0608Last,String type) {

        //当季度末名单详情
        List<BrokerStatistics> xnhk0609s = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("seccode", xnhk0608.getSeccode())
                .eq("f001d", xnhk0608.getF001d()));

        List<String> brokers = xnhk0609s.stream().map(BrokerStatistics::getF002v).collect(Collectors.toList());

        //上个季度末名单详情
        List<BrokerStatistics> xnhk0609Lasts = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("seccode", xnhk0608Last.getSeccode())
                .eq("f001d", xnhk0608Last.getF001d()));

        List<String> brokerLasts = xnhk0609Lasts.stream().map(BrokerStatistics::getF002v).collect(Collectors.toList());

        //新增(用当季度末名单比对上季度末名单)
        List<String> add = brokers.stream().filter(item -> !brokerLasts.contains(item)).collect(Collectors.toList());
        //消失(用当季度末名单比对上季度末名单);
        List<String> reduce = brokerLasts.stream().filter(item -> !brokers.contains(item)).collect(Collectors.toList());

        //两个list，判断当经纪商id相同，判断是否增持，增持的返回一个list1，减持的返回一个list2.对象里面有经纪商id，增减持数量
        List<BrokerStatistics> xnhk0609IncreaseQuantity = new ArrayList<>();
        List<BrokerStatistics> xnhk0609SubtractQuantity = new ArrayList<>();

        for(BrokerStatistics xnhk0609 : xnhk0609s){
            for(BrokerStatistics xnhk0609Last : xnhk0609Lasts){
                if(xnhk0609.getF002v().equals(xnhk0609Last.getF002v())){
                   if(xnhk0609.getF003n().compareTo(xnhk0609Last.getF003n())>0){
                       xnhk0609IncreaseQuantity.add(xnhk0609);
                   }else if(xnhk0609.getF003n().compareTo(xnhk0609Last.getF003n())<0){
                       xnhk0609SubtractQuantity.add(xnhk0609);
                   }
                }
            }
        }

        if(type.equals("1")){
            return add.size();
        }else if(type.equals("2")){
            return reduce.size();
        }else if(type.equals("3")){
            return xnhk0609IncreaseQuantity.size();
        }else if(type.equals("4")){
            return xnhk0609SubtractQuantity.size();
        }else{
            return 0;
        }
    }

    /**
     * 增减持股数
     * type = 1 : 新增
     * type = 2 : 减少
     * @param xnhk0608
     * @return
     */
    public BigDecimal getBrokersChangeHold(BrokerHeldInfo xnhk0608,BrokerHeldInfo xnhk0608Last,String type) {
        /**
         * 1、首先拿到增加名单 判断经纪商id相同时，比上一季度多，增为增持名单
         * 2、拿到减持名单 判断经纪商id相同时，比上一季度少，则为减持名单
         * 3、通过增持名单获取当前季度持有总数，通过增持名单获取上一季度持有总数.相减则为增持总数
         * 4、通过减持名单获取当前季度持有总数，通过减持名单获取上一季度持有总数。相减则为减持总数
         */

        //当季度末名单详情
        List<BrokerStatistics> xnhk0609s = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("seccode", xnhk0608.getSeccode())
                .eq("f001d", xnhk0608.getF001d()));

        //上个季度末名单详情
        List<BrokerStatistics> xnhk0609Lasts = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("seccode", xnhk0608Last.getSeccode())
                .eq("f001d", xnhk0608Last.getF001d()));

        //两个list，判断当经纪商id相同，判断是否增持，增持的返回一个list1，减持的返回一个list2.对象里面有经纪商id，增减持数量
        List<BrokerStatistics> xnhk0609IncreaseQuantity = new ArrayList<>();
        List<BrokerStatistics> xnhk0609SubtractQuantity = new ArrayList<>();

        for(BrokerStatistics xnhk0609 : xnhk0609s){
            for(BrokerStatistics xnhk0609Last : xnhk0609Lasts){
                if(xnhk0609.getF002v().equals(xnhk0609Last.getF002v())){
                    if(xnhk0609.getF003n().compareTo(xnhk0609Last.getF003n())>0){
                        xnhk0609IncreaseQuantity.add(xnhk0609);
                    }else if(xnhk0609.getF003n().compareTo(xnhk0609Last.getF003n())<0){
                        xnhk0609SubtractQuantity.add(xnhk0609);
                    }
                }
            }
        }
        //计算新增的list 这季度的总和-上季度的总和
        BigDecimal xnhk0609Increase = new BigDecimal(0);
        BigDecimal xnhk0609LastIncrease = new BigDecimal(0);
        BigDecimal xnhk0609Subtract = new BigDecimal(0);
        BigDecimal xnhk0609LastSubtract = new BigDecimal(0);
        for (BrokerStatistics xnhk0609Increase1 : xnhk0609IncreaseQuantity) {
            xnhk0609Increase = xnhk0609Increase.add(xnhk0609Increase1.getF003n());
        }

        for (BrokerStatistics xnhk0609Subtract1 : xnhk0609SubtractQuantity) {
            xnhk0609Subtract = xnhk0609Subtract.add(xnhk0609Subtract1.getF003n());
        }

        for (BrokerStatistics xnhk0609 : xnhk0609Lasts) {
            for (BrokerStatistics xnhk0609Increase1 : xnhk0609IncreaseQuantity) {
                if (xnhk0609.getF002v().equals(xnhk0609Increase1.getF002v())) {
                    xnhk0609LastIncrease = xnhk0609LastIncrease.add(xnhk0609.getF003n());
                }
            }

            for (BrokerStatistics xnhk0609Subtract1 : xnhk0609SubtractQuantity) {
                if (xnhk0609.getF002v().equals(xnhk0609Subtract1.getF002v())) {
                    xnhk0609LastSubtract = xnhk0609LastSubtract.add(xnhk0609.getF003n());
                }
            }
        }
        //计算减少的list 这季度的总和-上季度的总和

        if(type.equals("1")){
            return xnhk0609Increase.subtract(xnhk0609LastIncrease);
        }else if(type.equals("2")){
            return xnhk0609Subtract.subtract(xnhk0609LastSubtract);
        }else{
            return null;
        }
    }

//    /**
//     * 获取机构持股比例
//     * type = 0 : 机构
//     * type = 1 : hk
//     * type = 2 : sz
//     * @param xnhk0608
//     * @return
//     */
//    public BigDecimal getOrganizationShareHoldRatio(BrokerHeldInfo xnhk0608,String type) {
//        BigDecimal organizationShareHoldRatio;
//
//        BrokerStatistics xnhk0609HK = brokerStatisticsMapper.selectOne(new QueryWrapper<BrokerStatistics>()
//                .eq("seccode", xnhk0608.getSeccode())
//                .eq("f001d", xnhk0608.getF001d())
//                .eq("f002v", "A00003"));
//
//        BrokerStatistics xnhk0609SZ = brokerStatisticsMapper.selectOne(new QueryWrapper<BrokerStatistics>()
//                .eq("seccode", xnhk0608.getSeccode())
//                .eq("f001d", xnhk0608.getF001d())
//                .eq("f002v", "A00004"));
//
//        if(ObjectUtils.isNotEmpty(xnhk0609HK)){
//            organizationShareHoldRatio = calcSubtract(xnhk0608.getF008n(),xnhk0609HK.getF004n());
//        }else{
//            organizationShareHoldRatio = xnhk0608.getF008n();
//        }
//
//        if(ObjectUtils.isNotEmpty(xnhk0609SZ)){
//            organizationShareHoldRatio = calcSubtract(organizationShareHoldRatio,xnhk0609SZ.getF004n());
//        }
//
//        if(type.equals("1")){
//            if(ObjectUtils.isNotEmpty(xnhk0609HK)){
//                return calcCash(xnhk0609HK.getF004n(), new BigDecimal(100));
//            }
//            return new BigDecimal(0);
//        }else if(type.equals("2")){
//            if(ObjectUtils.isNotEmpty(xnhk0609SZ)){
//                return calcCash(xnhk0609SZ.getF004n(), new BigDecimal(100));
//            }
//            return new BigDecimal(0);
//        }else{
//            return calcCash(organizationShareHoldRatio, new BigDecimal(100));
//        }
//    }

    @Override
    public void updateNetTradeBrokerForDateJob(String code, Long date, Map<String, Xnhk0102> xnhk0102Map) {
        brokerStatisticsMapper.delete(new QueryWrapper<BrokerStatistics>()
                .eq("f001d", date)
                .eq("seccode", code));
        /**
         *
         * 1、更新 t_broker_statistics 表中指定日期的数据
         */

        //获取上一日价格 使用快照昨收替换
//        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        String timeStr = fmt.format(LocalDate.parse(date.toString(), DateTimeFormatter.ofPattern("yyyyMMdd")))+" 00:00:00";
//        List<KlineEntity> klineEntitiesUYes = klineDailyApi.getTimeList(Arrays.asList(code),timeStr);
        // 查询快照，使用昨收
        StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(code));
        BigDecimal price = BigDecimal.ZERO;
        if(ObjectUtils.isNotEmpty(snapshot)){
            price = snapshot.getPreClose();
        }

        //1、更新 t_broker_statistics 表中指定日期的数据
        List<Xnhk0609> xnhk0609s = xnhk0609Mapper.selectList(new QueryWrapper<Xnhk0609>()
                .eq("f001d", date)
                .eq("seccode", code));

        //如果同步日期等于拆并股日期，重新计算拆并股
        List<Xnhk0127> xnhk0127s = xnhk0127Mapper.selectList(new QueryWrapper<Xnhk0127>()
                .eq("F003D", LocalDate.parse(date.toString(), DateTimeFormatter.ofPattern("yyyyMMdd")))
                .eq("SECCODE", code)
                .in("F002V", Arrays.asList("SS", "SC")));

        List<BrokerStatistics> brokerStatisticss = new ArrayList<>();
        for (Xnhk0609 x : xnhk0609s) {
            BigDecimal heldNum = x.getF003n();
//            BigDecimal price = klineEntitiesUYes.size()>0 ? klineEntitiesUYes.get(0).getClose() : new BigDecimal(0);
            if(!CollectionUtils.isEmpty(xnhk0127s)){
                heldNum = calcCash(heldNum,xnhk0127s.get(0).getF004n());
                price = price.multiply(xnhk0127s.get(0).getF004n());
            }
            BigDecimal marketvalue = heldNum.multiply(price);
            if (marketvalue.compareTo(BigDecimal.ZERO) < 0 || x.getF003n().compareTo(BigDecimal.ZERO) < 0) {
                log.info("过滤掉负数的数据");
                continue;
            }
            BrokerStatistics brokerStatistics = new BrokerStatistics();
            brokerStatistics.setSeccode(x.getSeccode());
            brokerStatistics.setF001d(x.getF001d());
            brokerStatistics.setF002v(x.getF002v());
            brokerStatistics.setF003n(heldNum);
            brokerStatistics.setF003nOrg(x.getF003n());
            //同步融聚汇数据时：如果占比为空，自己计算 持股比例(占流通股)
            Xnhk0102 xnhk0102 = xnhk0102Map.get(code);
            BigDecimal flowShares = xnhk0102 == null ? null : xnhk0102.getF070n();
            BigDecimal f004n = x.getF004n() == null ? (calcCash(heldNum, flowShares)) : x.getF004n();
            brokerStatistics.setF004n(f004n);
            brokerStatistics.setF014v(brokerCommonUtils.getBrokerIndustry(code));
            brokerStatistics.setCreateDate(x.getCreateDate());
            brokerStatistics.setEndPrice(price);
            brokerStatistics.setEndPriceOrg(price);
            brokerStatistics.setMarketVal(marketvalue);
            brokerStatistics.setModifiedDate(new Date());
            if(ObjectUtils.isEmpty(brokerStatistics.getF004n())  || ObjectUtils.isEmpty(brokerStatistics.getF003n())){
                continue;
            }
            brokerStatisticss.add(brokerStatistics);
        }
        if(brokerStatisticss.size()>0){
            brokerStatisticsMapper.batchSave(brokerStatisticss);
        }
        //同步 xnhk0608 表数据
        brokerHeldInfoMapper.delete(new QueryWrapper<BrokerHeldInfo>()
                .eq("f001d", date)
                .eq("seccode", code));

        List<Xnhk0608> xnhk0608s = xnhk0608Mapper.selectList(new QueryWrapper<Xnhk0608>()
                .eq("f001d", date)
                .eq("seccode", code));

        List<BrokerHeldInfo> brokerHeldInfos = new ArrayList<>();
        for(Xnhk0608 xnhk0608 : xnhk0608s){
            BrokerHeldInfo b = new BrokerHeldInfo();
            BeanUtils.copyProperties(xnhk0608,b);
            b.setF013nOrg(xnhk0608.getF013n());
            if(!CollectionUtils.isEmpty(xnhk0127s)){
                b.setF013n(calcCash(xnhk0608.getF013n(),xnhk0127s.get(0).getF004n()));
            }
            brokerHeldInfos.add(b);
        }
        if(!CollectionUtils.isEmpty(brokerHeldInfos)){
            saveBatch(brokerHeldInfos);
        }
    }

    @Override
    public void insertBrokersMakValStatistics(Long date, List<String> codes) {
        //计算市值
        brokerMarketValueStatisticsMapper.delete(new QueryWrapper<BrokerMarketValueStatistics>()
                .eq("f001d", date));
//        List<BrokerMarketValueStatistics> brokerMarketValueStatisticsArrayList = new ArrayList<>();
        log.info("拆并股后计算经纪商-维度数据,日期:{}", date);
        List<BrokerStatistics> queryBrokerStatisticsList = new ArrayList<>();
        List<List<String>> partition = Lists.partition(codes.stream().collect(Collectors.toList()), 10);
        for (List<String> subCodes : partition) {
            try {
                List<BrokerStatistics> subLists = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                        .select("f001d", "f002v", "f003n", "market_val")
                        .eq("F001D", date)
                        .in("SECCODE", subCodes));
                if (!CollectionUtils.isEmpty(subLists)) {
                    queryBrokerStatisticsList.addAll(subLists);
                }
            } catch (Exception e) {
                log.error("批量获取经纪商数据异常", e);
            }
        }

        // 按 F001D 和 F002V 分组，并计算每个组的 F003N 和 marketVal 的总和
        List<BrokerMarketValueStatistics> brokerMarketValueStatisticsArrayList = queryBrokerStatisticsList.stream()
                .collect(Collectors.groupingBy(
                        stock -> stock.getF001d() + "," + stock.getF002v(),
                        Collectors.collectingAndThen(
                                Collectors.reducing(
                                        new BrokerMarketValueStatistics(0l, "", BigDecimal.ZERO, BigDecimal.ZERO,null),
                                        stock -> new BrokerMarketValueStatistics(
                                                stock.getF001d(),
                                                stock.getF002v(),
                                                stock.getF003n(),
                                                stock.getMarketVal(),
                                                null
                                        ),
                                        (summary1, summary2) -> new BrokerMarketValueStatistics(
                                                summary1.getF001d(),
                                                summary1.getBrokerId(),
                                                summary1.getBrokerHeldNumber().add(summary2.getBrokerHeldNumber()),
                                                summary1.getMarketVal().add(summary2.getMarketVal()),
                                                null
                                        )
                                ),
                                summary -> summary
                        )
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

//        List<BrokerStatistics> brokerStatistics = brokerStatisticsMapper.getBrokerBydates(date, codes);
//        for (BrokerStatistics broker : brokerStatistics) {
//            BrokerMarketValueStatistics brokerMarketValueStatistics = new BrokerMarketValueStatistics();
//            brokerMarketValueStatistics.setBrokerId(broker.getF002v());
//            brokerMarketValueStatistics.setBrokerHeldNumber(broker.getF003n());
//            brokerMarketValueStatistics.setMarketVal(broker.getMarketVal());
//            brokerMarketValueStatistics.setF001d(date);
//            brokerMarketValueStatisticsArrayList.add(brokerMarketValueStatistics);
//        }
        if(!CollectionUtils.isEmpty(brokerMarketValueStatisticsArrayList)){
            brokerMarketValueStatisticsMapper.insertBrokersMakValStatistics(brokerMarketValueStatisticsArrayList);
        }

    }

    @Override
    public DateResp getDateResp() {
        //20221227改版 更新时间改为上一个交易日
        LocalDate date = hkTradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate();
        Long timestamp = DateUtils.localDateToLong(date);
        DateResp dateResp = new DateResp();
        dateResp.setDate(timestamp);
        return dateResp;
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
     * 除法（指定保留位数）
     * @param val
     * @param val2
     * @return
     */
    public BigDecimal calcCashScale(BigDecimal val, BigDecimal val2,Integer scale) {
        if (val == null || val2 == null) {
            return BigDecimal.ZERO;
        }
        if (BigDecimal.ZERO.compareTo(val2) == 0) {
            return BigDecimal.ZERO;
        }
        return val.divide(val2, scale, RoundingMode.HALF_UP);
    }

    /**
     * 经纪商持股榜单按指定字段排序
     * @param brokersDetailList   持股列表数据
     * @param sortKey   排序字段
     * @param sort  排序方式(asc:升序 desc:降序)
     */
    public void sort(List<BrokersDetail> brokersDetailList , String sortKey, String sort) {
        List<String> fieldList = Arrays.stream(BrokersDetail.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());

        if (fieldList.contains(sortKey)) {
            try {
                Comparator<BrokersDetail> comparator = null;
                PropertyDescriptor descriptor = new PropertyDescriptor(sortKey, BrokersDetail.class);
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
                        brokersDetailList.sort(comparator);
                    } else {
                        brokersDetailList.sort(comparator.reversed());
                    }
                }
            } catch (Exception e) {
                log.error("排序失败", e);
            }
        }
    }

    /**
     * 根据股票code,经纪商Id获取近xxx日经纪商对这只股票的持股情况
     * @param code
     * @param brokerId
     * @return
     */
    public List<BrokersProportionDetail> getThirtyDaysShare(String code,String brokerId,Integer day,Map<String,List<BrokerStatistics>> brokerStatisticsMap){
        //查询经纪商名称
        String brokerName = brokerCommonUtils.getBrokerName(brokerId);
        //根据经纪商brokerId，获取该经纪商近一个月对这支股票的持股情况
        List<BrokerStatistics> brokerStatistics = brokerStatisticsMap.get(brokerId);
        if(CollectionUtils.isEmpty(brokerStatistics)){
            List<BrokersProportionDetail> emptyList = new ArrayList<>();
            List<BgTradingCalendar> bgTradingCalendars = hkTradingCalendarApi.queryLastTradingCalendars(day+1);
            bgTradingCalendars.remove(0);
            for (BgTradingCalendar bgTradingCalendar : bgTradingCalendars) {
                BrokersProportionDetail build = BrokersProportionDetail.builder().
                        f001d(DateUtils.localDateToLong(bgTradingCalendar.getDate())).
                        brokerId(brokerId).brokerName(brokerName).f004n(BigDecimal.ZERO).build();
                emptyList.add(build);
            }
            return emptyList;
        }
        brokerStatistics = brokerStatistics.stream().sorted(Comparator.comparing(BrokerStatistics::getF001d).reversed()).limit(day).collect(Collectors.toList());
        //创建一个空的resp集合保存对象
        List<BrokersProportionDetail> brokersProportionDetailArrayList=new ArrayList<>();
        for(BrokerStatistics brokerStatistics1 : brokerStatistics){
            //创建一个空的对象
            BrokersProportionDetail brokersProportionDetail=new BrokersProportionDetail();
            brokersProportionDetail.setF001d(DateUtils.longDateToLongMS(brokerStatistics1.getF001d()));
            //经纪商Id
            brokersProportionDetail.setBrokerId(brokerId);
            //经纪商名称
            brokersProportionDetail.setBrokerName(brokerName);
            //经纪商持股比例
            brokersProportionDetail.setF004n(brokerStatistics1.getF004n());
            brokersProportionDetailArrayList.add(brokersProportionDetail);
        }
        if(brokersProportionDetailArrayList.size() < day){
            List<BgTradingCalendar> tradingCalendars = hkTradingCalendarApi.getLastTradingCalendarsLtDate(LocalDate.now(), day).getData();
            List<Long> dateList = brokersProportionDetailArrayList.stream().map(item -> item.getF001d()).collect(Collectors.toList());
            for (BgTradingCalendar tradingCalendar : tradingCalendars) {
                LocalDate date = tradingCalendar.getDate();
                Long f001d = DateUtils.localDateToLong(date);
                if(!dateList.contains(f001d)){
                    brokersProportionDetailArrayList.add(BrokersProportionDetail.builder().brokerId(brokerId).brokerName(brokerName).f001d(f001d).build());
                }
            }
            brokersProportionDetailArrayList = brokersProportionDetailArrayList.stream().sorted(Comparator.comparing(BrokersProportionDetail::getF001d)).collect(Collectors.toList());
        }
        brokersProportionDetailArrayList = brokersProportionDetailArrayList.stream().sorted(Comparator.comparing(BrokersProportionDetail::getF001d)).collect(Collectors.toList());
        return brokersProportionDetailArrayList;
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
     * 获取经纪商持股比例-为港股通(沪)
     * @param brokersProportionResp
     * @param brokerId
     * @param code
     */
    public void setHKStockConnectShanghai(BrokersProportionResp brokersProportionResp,String brokerId,String code,Map<String, List<BrokerStatistics>> brokerStatisticMap,Boolean tradeFlag){
        //boolean tradeFlag = hkTradingCalendarApi.isTradingDay(LocalDate.now());
        if(tradeFlag){
            BrokersProportionDetail brokersProportionDetail=new BrokersProportionDetail();
            brokersProportionDetail.setF001d(LocalDate.now().atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli());
            if(!hkTradingCalendarApi.isTradingTimeAM(LocalTime.now())) {
                //获取到今日持股占比
                Double score = redisClient.getScore(BrokerConstants.BROKER_TODAYPROPORTIONSHAREHOLD+code,brokerId);
                BigDecimal shanghaiHKConnectShare = score == null ? BigDecimal.ZERO : BigDecimal.valueOf(score);
                brokersProportionDetail.setF004n(shanghaiHKConnectShare.multiply(BigDecimal.valueOf(100)));
                brokersProportionDetail.setBrokerId(brokerId);
                brokersProportionDetail.setBrokerName(brokerCommonUtils.getBrokerName(brokerId));
            }
            //获取到前29日
            List<BrokersProportionDetail> a00003 = getThirtyDaysShare(code, brokerId, 29,brokerStatisticMap);
            a00003.add(brokersProportionDetail);
            brokersProportionResp.setBrokersProportionHKStockConnectShanghai(a00003);
        }else{
            brokersProportionResp.setBrokersProportionHKStockConnectShanghai(getThirtyDaysShare(code,brokerId,30,brokerStatisticMap));
        }

    }

    /**
     * 获取经纪商持股比例-港股通(深)
     * @param brokersProportionResp
     * @param brokerId
     * @param code
     */

    public void setHKStockConnectShenzhen(BrokersProportionResp brokersProportionResp,String brokerId,String code,Map<String, List<BrokerStatistics>> brokerStatisticMap,Boolean tradeFlag){
        //boolean tradeFlag = hkTradingCalendarApi.isTradingDay(LocalDate.now());
        if(tradeFlag){
            BrokersProportionDetail brokersProportionDetail=new BrokersProportionDetail();
            brokersProportionDetail.setF001d(LocalDate.now().atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli());
            if(!hkTradingCalendarApi.isTradingTimeAM(LocalTime.now())) {
                //获取到今日持股占比
                Double score = redisClient.getScore(BrokerConstants.BROKER_TODAYPROPORTIONSHAREHOLD+code,brokerId);
                BigDecimal shenzhenHKConnectShare = score == null ? BigDecimal.ZERO : BigDecimal.valueOf(score);
                brokersProportionDetail.setF004n(shenzhenHKConnectShare.multiply(BigDecimal.valueOf(100)));
                brokersProportionDetail.setBrokerId(brokerId);
                brokersProportionDetail.setBrokerName(brokerCommonUtils.getBrokerName(brokerId));
            }
            //获取到前29日
            List<BrokersProportionDetail> a00004 = getThirtyDaysShare(code, brokerId, 29,brokerStatisticMap);
            a00004.add(brokersProportionDetail);
            brokersProportionResp.setBrokersProportionHKStockConnectShenzhen(a00004);
        }else{
            brokersProportionResp.setBrokersProportionHKStockConnectShenzhen(getThirtyDaysShare(code,brokerId,30,brokerStatisticMap));
        }

    }



    @Override
    public void updateBrokerStockEventV2(String code, BigDecimal factor, List<Long> dates) {

        brokerStatisticsMapper.updateBrokerStockEventV2(code, factor, dates);
    }

    @Override
    public void updateBrokerInfoEventV2(String code, BigDecimal factor, List<Long> date) {
        brokerHeldInfoMapper.updateBrokerInfoEventV2(code, factor, date);
    }

    @Override
    public LocalDate getStartDate(Integer num, LocalDate startDate) {
        LocalDate date = null;
        if(num != null){
            //按交易日获取日期
            if(num == 1){
                return null;
            }
            //20221227改版   以上一个交易日为基准取近x个交易日
            LocalDate lastDate = hkTradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate();
            Long rdate = hkTradingCalendarApi.queryBeforeTradingCalendars(lastDate, num-1);
            date = LocalDate.parse(rdate + "", DateTimeFormatter.ofPattern("yyyyMMdd"));
            //与数据库最早一条数据做对比
            BrokerStatistics brokerStatistics = brokerStatisticsMapper.selectOne(new QueryWrapper<BrokerStatistics>().select("distinct f001d").orderByAsc("f001d").last("limit 1"));
            if(brokerStatistics == null){
                return date;
            }
            Long f001d = brokerStatistics.getF001d();
            LocalDate earlyDate = DateUtils.f001dToLocalDate(f001d);
            return earlyDate.isBefore(date) ? date : earlyDate;
        }else if(startDate != null){
            //按自然日获取日期
            BrokerStatistics brokerStatistics = brokerStatisticsMapper.selectOne(new QueryWrapper<BrokerStatistics>().select("distinct f001d").orderByAsc("f001d").last("limit 1"));
            Long f001d = brokerStatistics.getF001d();
            LocalDate earlyDate = DateUtils.f001dToLocalDate(f001d);
            return earlyDate.isBefore(startDate) ? startDate : earlyDate;
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHisBrokerStock(String stockCode,Long f001d) {
        brokerStatisticsMapper.deleteHisBrokerStock(stockCode,f001d);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHisBrokerStockMarket(Long f001d) {

        brokerMarketValueStatisticsMapper.deleteHisBrokerStockMarket(f001d);

        Boolean hasDeleted;
        do {
            hasDeleted = brokerIndustryStatisticsMapper.delete(new QueryWrapper<BrokerIndustryStatistics>()
                    .lt("f001d", f001d)
                    .last("limit " + batchSize)) > 0;
        } while (hasDeleted);
    }

    @Override
    public List<BrokerSearch> getBrokerInformation(String code, String brokerIdOrName) {
        //20221227改版  经纪商范围变为0609表(上一交易日有持股)
        List<BrokerSearch> brokerInformationList = redisClient.get(BrokerConstants.BG_BROKER_ID_PROFIT_LIST);;
        LocalDate queryDate = hkTradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate();
        Long queryDateLong = DateUtils.localDateToF001D(queryDate);
        List<String> brokerIdList = brokerStatisticsMapper.selectBrokerIdList(code,queryDateLong);
        brokerInformationList = brokerInformationList.stream().filter(item -> brokerIdList.contains(item.getBrokerId())).collect(Collectors.toList());
        //用经纪商模糊查询(id，名字，拼音)
        if(Objects.nonNull(brokerIdOrName)){
            brokerInformationList = brokerInformationList.stream()
                    .filter(item -> item.getBrokerId().contains(brokerIdOrName)||item.getBrokerName().contains(brokerIdOrName)
                            || PinyinUtil.getPinyin(item.getBrokerName(),"").contains(brokerIdOrName)
                            || PinyinUtil.getFirstLetter(item.getBrokerName(),"").contains(brokerIdOrName))
                    .collect(Collectors.toList());
        }

        return brokerInformationList;
    }


    /**
     * 保证每个数组长度一致
     * @param brokersProportionResp
     */
    private void subBrokersProportion(BrokersProportionResp brokersProportionResp){
        List<StockPrice> stockPriceList = brokersProportionResp.getStockPriceList();
        if(CollectionUtils.isEmpty(stockPriceList)){
            return;
        }
        List<Long> dateList = stockPriceList.stream().map(item -> item.getDate()).collect(Collectors.toList());
        int size = stockPriceList.size();
        List<List<BrokersProportionDetail>> rankFiveList = brokersProportionResp.getBrokersProportionList();
        List<List<BrokersProportionDetail>> newFiveList = Lists.newArrayList();
        if(!CollectionUtils.isEmpty(rankFiveList)){
            for (List<BrokersProportionDetail> brokersProportionDetails : rankFiveList) {
                brokersProportionDetails = brokersProportionDetails.stream().filter(item -> dateList.contains(item.getF001d())).collect(Collectors.toList());
                newFiveList.add(brokersProportionDetails);
                //newFiveList.add(subBrokerList(brokersProportionDetails,size));
            }
        }
        brokersProportionResp.setBrokersProportionList(newFiveList);

        //港股通
        List<BrokersProportionDetail> shanghai = brokersProportionResp.getBrokersProportionHKStockConnectShanghai();
        if(!CollectionUtils.isEmpty(shanghai)){
            shanghai = shanghai.stream().filter(item -> dateList.contains(item.getF001d())).collect(Collectors.toList());
            brokersProportionResp.setBrokersProportionHKStockConnectShanghai(shanghai);
            //brokersProportionResp.setBrokersProportionHKStockConnectShanghai(subBrokerList(shanghai,size));
        }
        List<BrokersProportionDetail> shenzhen = brokersProportionResp.getBrokersProportionHKStockConnectShenzhen();
        if(!CollectionUtils.isEmpty(shenzhen)){
            shenzhen = shenzhen.stream().filter(item -> dateList.contains(item.getF001d())).collect(Collectors.toList());
            brokersProportionResp.setBrokersProportionHKStockConnectShenzhen(shenzhen);
            //brokersProportionResp.setBrokersProportionHKStockConnectShenzhen(subBrokerList(shenzhen,size));
        }

        //前5，10，20合计
        List<TopBrokersProportionDetail> brokersProportionSum5 = brokersProportionResp.getBrokersProportionSum5();
        List<TopBrokersProportionDetail> brokersProportionSum10 = brokersProportionResp.getBrokersProportionSum10();
        List<TopBrokersProportionDetail> brokersProportionSum20 = brokersProportionResp.getBrokersProportionSum20();
        brokersProportionSum5 = brokersProportionSum5.stream().filter(item -> dateList.contains(item.getF001d())).collect(Collectors.toList());
        brokersProportionResp.setBrokersProportionSum5(brokersProportionSum5);
        brokersProportionSum10 = brokersProportionSum10.stream().filter(item -> dateList.contains(item.getF001d())).collect(Collectors.toList());
        brokersProportionResp.setBrokersProportionSum10(brokersProportionSum10);
        brokersProportionSum20 = brokersProportionSum20.stream().filter(item -> dateList.contains(item.getF001d())).collect(Collectors.toList());
        brokersProportionResp.setBrokersProportionSum20(brokersProportionSum20);
    }

    @Override
    public Boolean saveBatch(List<BrokerHeldInfo> brokerHeldInfos) {
        boolean insert = brokerHeldInfoService.saveBatch(brokerHeldInfos);
        return insert;
    }

    /**
     * 十大净买卖无数据，期初往后追溯
     * @param xnhk0609End
     * @param code
     * @param queryDate
     */
    public Long netSalesComplement(List<BrokerStatistics> xnhk0609End,String code,Long queryDate){
        if(CollectionUtils.isEmpty(xnhk0609End)){
            BrokerStatistics brokerStatistics = brokerStatisticsMapper.selectOne(new QueryWrapper<BrokerStatistics>()
                    .eq("seccode", code)
                    .le("f001d", queryDate)
                    .orderByDesc("f001d")
                    .last("limit 1"));
            if(Objects.nonNull(brokerStatistics)){
                queryDate = brokerStatistics.getF001d();
                List<BrokerStatistics> dataList = brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                        .eq("f001d", queryDate)
                        .eq("seccode", code));
                xnhk0609End.addAll(dataList);
            }
        }
        return queryDate;
    }

    @Override
    public void createBrokerDataByCode(String stockCode) {
        try {
            log.info("BrokerAnalysisApi createBrokerDataByCode start, stockCode: {}", stockCode);
            TimeInterval timeInterval = new TimeInterval();

            List<BgTradingCalendar> tradingCalendars = hkTradingCalendarApi.queryLastTradingCalendars(5);
            // 近3个交易日(60 x 600)
            List<Long> f001ds = CollUtil.map(tradingCalendars, tc -> Long.valueOf(tc.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))), true);

            String oldCode = StrUtil.replace(stockCode, "-t", "");
            // 经纪商数据
            List<BrokerStatistics> brokerStatistics = brokerStatisticsMapper.selectList(Wrappers.<BrokerStatistics>lambdaQuery().eq(BrokerStatistics::getSeccode, oldCode).in(BrokerStatistics::getF001d, f001ds));
            List<BrokerHeldInfo> heldInfos = brokerHeldInfoMapper.selectList(new QueryWrapper<BrokerHeldInfo>().eq("SECCODE", oldCode).in("F001D", f001ds));

            CollUtil.forEach(brokerStatistics, (ff, index) -> { ff.setSeccode(stockCode); });
            CollUtil.forEach(heldInfos, (ff, index) -> { ff.setSeccode(stockCode); });

            Opt.ofEmptyAble(brokerStatistics).peek(list -> brokerStatisticsMapper.batchSave(list));
            Opt.ofEmptyAble(heldInfos).peek(list -> brokerHeldInfoMapper.batchSave(list));

            // 经纪商持股占比
            List<BrokersPositionStatisticsDetail> brokersPositionStatisticsDetailList = redisClient.get(BrokerConstants.POSITION_STATISTICS_DETAIL + oldCode);
            if (CollUtil.isNotEmpty(brokersPositionStatisticsDetailList)) {
                redisClient.set(BrokerConstants.POSITION_STATISTICS_DETAIL + stockCode, brokersPositionStatisticsDetailList);
            }
            List<String> makValCodes = getBrokerMakValCodes();

            // 股票所属行业
            for (Long f001d : f001ds) {
                // 经纪商市值
                this.insertBrokersMakValStatistics(f001d, makValCodes);
                // 经纪商行业维度
                this.updateIndustryStatisticsBatch(f001d, ListUtil.toList(stockCode));
            }
            log.info("BrokerAnalysisApi createBrokerDataByCode end, stockCode: {}, cost: {}", stockCode, timeInterval.interval() / 1000.0);
        } catch (Exception e) {
            log.error("BrokerAnalysisApi createBrokerDataByCode error, stockCode: {}", stockCode, e);
        }
    }

    @Override
    public void deleteBrokerDataByCode(String stockCode) {
        try {
            log.info("BrokerAnalysisApi deleteBrokerDataByCode start, stockCode: {}", stockCode);
            TimeInterval timeInterval = new TimeInterval();
            // 所有经纪商数据
            List<BrokerStatistics> brokerStatistics = brokerStatisticsMapper.selectList(Wrappers.<BrokerStatistics>lambdaQuery().eq(BrokerStatistics::getSeccode, stockCode));
            ArrayList<Long> f001ds = CollUtil.distinct(CollUtil.map(brokerStatistics, BrokerStatistics::getF001d, true));
            // 删除经纪商数据
            brokerStatisticsMapper.delete(Wrappers.<BrokerStatistics>lambdaQuery().eq(BrokerStatistics::getSeccode, stockCode));
            // 经纪商持股
            brokerHeldInfoMapper.delete(new QueryWrapper<BrokerHeldInfo>().eq("SECCODE", stockCode));
            // 经纪商持股占比
            redisClient.del(BrokerConstants.POSITION_STATISTICS_DETAIL + stockCode);
            // 股票所属行业
            IndustrySubsidiary stockIndustry = industrySubsidiaryService.getStockIndustry(stockCode);
            // 经纪商维度股票
            List<String> makValCodes = getBrokerMakValCodes();
            if (ObjectUtil.isNotEmpty(stockIndustry) && StrUtil.isNotBlank(stockIndustry.getCode())) {
                // 成分股
                List<StockDefine> hyDefines = stockDefineMapper.selectList(Wrappers.lambdaQuery(StockDefine.class)
                        .eq(StockDefine::getIndustryCode, stockIndustry.getCode()).eq(StockDefine::getStockType, StockTypeEnum.STOCK.getCode()));
                List<String> codeList = CollUtil.map(hyDefines, StockDefine::getCode, true);

                for (Long f001d : f001ds) {
                    // 经纪商行业维度
                    this.updateIndustryStatisticsBatch(f001d, codeList);
                }
            }
            if (CollUtil.isNotEmpty(makValCodes)) {
                for (Long f001d : f001ds) {
                    // 经纪商市值
                    this.insertBrokersMakValStatistics(f001d, makValCodes);
                }
            }
            log.info("BrokerAnalysisApi deleteBrokerDataByCode end, stockCode: {}, cost: {}", stockCode, timeInterval.interval() / 1000.0);
        } catch (Exception e) {
            log.error("BrokerAnalysisApi deleteBrokerDataByCode error, stockCode: {}", stockCode, e);
        }
    }

    private List<String> getBrokerMakValCodes() {
        Set<String> codeLists = stockService.getAllStockCode();
        //股权数据不需要落库
        Set<String> stockRightsCodes = utsInfoService.getTradingStockRights(new Date()).stream().map(StockRightsDTO::getCode).collect(Collectors.toSet());
        Set<String> codes = codeLists.stream().filter(code -> !stockRightsCodes.contains(code)).collect(Collectors.toSet());
        //计算经纪商-行业维度数据
        List<String> quitCodeList = stockMarketService.getQuitCode().getData();
        List<String> codeList = codes.stream().filter(item -> !quitCodeList.contains(item)).collect(Collectors.toList());
        return codeList;
    }

    @Override
    public void updateBroker0DataByCode(String oldStockCode, String newStockCode) {
        try {
            log.info("BrokerAnalysisApi updateBroker0DataByCode start, oldStockCode: {}, newStockCode: {}", oldStockCode, newStockCode);
            TimeInterval timeInterval = new TimeInterval();
            // 更新经纪商数据
            brokerStatisticsMapper.update(null, Wrappers.<BrokerStatistics>lambdaUpdate().set(BrokerStatistics::getSeccode, newStockCode).eq(BrokerStatistics::getSeccode, oldStockCode));
            // 更新经纪商持股
            brokerHeldInfoMapper.update(null, Wrappers.<BrokerHeldInfo>lambdaUpdate().set(BrokerHeldInfo::getSeccode, newStockCode).eq(BrokerHeldInfo::getSeccode, oldStockCode));
            // 经纪商持股占比
            List<BrokersPositionStatisticsDetail> brokersPositionStatisticsDetailList = redisClient.get(BrokerConstants.POSITION_STATISTICS_DETAIL + oldStockCode);
            if (CollUtil.isNotEmpty(brokersPositionStatisticsDetailList)) {
                redisClient.set(BrokerConstants.POSITION_STATISTICS_DETAIL + newStockCode, brokersPositionStatisticsDetailList);
            }
            log.info("BrokerAnalysisApi updateBroker0DataByCode end, oldStockCode: {}, newStockCode: {}, cost: {}", oldStockCode, newStockCode, timeInterval.interval() / 1000.0);
        } catch (NumberFormatException e) {
            log.error("BrokerAnalysisApi updateBroker0DataByCode error, oldStockCode: {}, newStockCode: {}", oldStockCode, newStockCode, e);
        }
    }

    @Override
    public String getBrokerName(String brokerId) {
        //查询经纪商名称
        return brokerCommonUtils.getBrokerName(brokerId);
    }

    @Override
    public Xnhk0102 getXnhk0102(String code) {
        return xnhk0102Mapper.selectOne(new QueryWrapper<Xnhk0102>().eq("seccode", code));
    }

    @Override
    public List<BrokerStatistics> getBrokerStatisticsLimit60(String code, String brokerId) {
        return brokerStatisticsMapper.selectList(new QueryWrapper<BrokerStatistics>()
                .eq("SECCODE", code).eq("F002V", brokerId).lt("f001d",DateUtils.localDateToF001D(LocalDate.now()))
                .orderByDesc("f001d")
                .last("limit 60"));
    }
}
