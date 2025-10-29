package com.vv.finance.investment.bg.api.impl.uts;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.api.client.util.Lists;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.bean.SimplePageResp;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.utils.*;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.uts.resp.HoldStockChange;
import com.vv.finance.investment.bg.dto.uts.resp.ValuationGrowth;
import com.vv.finance.investment.bg.entity.f10.industry.MarketPresence;
import com.vv.finance.investment.bg.entity.southward.SouthwardCapitalStatistics;
import com.vv.finance.investment.bg.entity.southward.StockSouthwardCapitalStatistics;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.enums.SouthwardCapitalMarketEnum;
import com.vv.finance.investment.bg.mapper.southward.SouthwardCapitalStatisticsMapper;
import com.vv.finance.investment.bg.mapper.southward.StockSouthwardCapitalStatisticsMapper;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName: StockMarketServiceImpl
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/11/13   17:01
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class StockMarketServiceImpl implements IStockMarketService {

    private static final String PATTERN = "yyyyMMdd";
    private static final String COLLECTION_NAME = "f10_market_presence";
    @Resource
    private Xnhk0102Mapper mapper;
    @Resource
    private Xnhks0601Mapper xnhks0601Mapper;
    @Resource
    Xnhk0406Mapper xnhk0406Mapper;
    @Resource
    Xnhk0410Mapper xnhk0410Mapper;
    @Resource
    Xnhks0101Mapper  xnhks0101Mapper;
    @Resource
    Xnhk0201Mapper xnhk0201Mapper;
    @Resource
    Xnhk0203Mapper xnhk0203Mapper;
    @Resource
    Xnhk0204Mapper xnhk0204Mapper;
    @Resource
    Xnhk0205Mapper xnhk0205Mapper;
    @Resource
    Xnhk0206Mapper xnhk0206Mapper;
    @Resource
    Xnhk0207Mapper xnhk0207Mapper;
    @Resource
    Xnhk0208Mapper xnhk0208Mapper;
    @Resource
    Xnhk0209Mapper xnhk0209Mapper;
    @Resource
    Xnhk0202Mapper xnhk0202Mapper;
    @Resource
    Xnhk0702Mapper xnhk0702Mapper;
    @Resource
    Xnhks0701Mapper xnhks0701Mapper;
    @Resource
    Xnhks0501Mapper xnhks0501Mapper;
    @Resource
    Xnhk0901Mapper xnhk0901Mapper;
    @Resource
    Xnhks0314Mapper xnhks0314Mapper;
    @Resource
    StockDefineMapper stockDefineMapper;

    @Resource
    private RedisClient redisClient;
    @Resource
    Xnhk0307Mapper xnhk0307Mapper;

    @Resource
    Xnhks0503Mapper xnhks0503Mapper;
    @Resource
    Xnhk0403Mapper xnhk0403Mapper;
    @Resource
    MongoTemplate mongoTemplate;
    @Resource
    private SouthwardCapitalStatisticsMapper southwardCapitalStatisticsMapper;
    @Resource
    private StockCache stockCache;

    @Override
    public Xnhk0102 getStockMarketData(String stockCode) {
        return mapper.queryMarketData(stockCode);
    }

    @Override
    public ResultT<List<Xnhks0314>> getXnhks0314List() {
        return ResultT.success(xnhks0314Mapper.selectList(null));
    }

    @Override
    public ResultT<List<Object>> getXnhks0501(long time) {
        return ResultT.success(xnhks0501Mapper.selectObjs(new QueryWrapper<Xnhks0501>().select("SECCODE").eq("F002D",time)));
    }

    @Override
    public ResultT<List<Object>> getXnhks0101(long time) {
        return ResultT.success(xnhks0101Mapper.selectObjs(new QueryWrapper<Xnhks0101>().select("SECCODE").eq("F007D",time).eq("F014V","OS")));
    }

    @Override
    public ResultT<List<Xnhk0901>> getXnhk0901List(List<String> codes) {
        return ResultT.success(xnhk0901Mapper.getXnhk09011List("('" + codes.stream().collect(Collectors.joining("','")) + "')"));
    }

    @Override
    public ResultT<List<Xnhk0102>> getXnhk0102List() {
        return ResultT.success(mapper.selectList(null));
    }

    @Override
    public ResultT<List<String>> getQuitCode() {
        List<Object> objects =xnhks0101Mapper.selectObjs(new QueryWrapper<Xnhks0101>().eq("F010V","s")
                .lt("F009D",DateUtils.getDate("yyyyMMdd")).in("F014V", Arrays.asList("OS","PC","RS")));
        return ResultT.success(objects.stream().map(Object::toString).collect(Collectors.toList()));

    }

    @Override
    public ResultT<List<String>> getCloseCode() {
        return ResultT.success(xnhks0101Mapper.getCloseCode(DateUtils.getDate("yyyyMMdd")));
    }

    @Override
    public ResultT<List<String>> getCloseDefineCode() {
        List<String> closeList = xnhks0101Mapper.getCloseCode(DateUtils.getDate("yyyyMMdd"));
        List<String> codeList = stockDefineMapper.selectStockCodeList();
        List<String> list = closeList.stream().filter(item -> codeList.contains(item)).collect(Collectors.toList());
        return ResultT.success(list);
    }

    @Override
    public Xnhks0601 getStockholderAddOrSubtract(String stockCode) {
        Xnhks0601 xnhks0601 = new Xnhks0601();
        List<Xnhks0601> xnhks0601List = xnhks0601Mapper.selectList
                (new QueryWrapper<Xnhks0601>().eq("SECCODE", stockCode).orderByDesc("F001D"));
        if (CollectionUtils.isNotEmpty(xnhks0601List)) {
            xnhks0601 = xnhks0601List.get(0);
        }
        return xnhks0601;
    }

    @Override
    public ResultT<List<Xnhks0601>> getXnhks0601List() {
        return ResultT.success(xnhks0601Mapper.getS0601List());
    }

    @Override
    public ResultT<List<Xnhks0601>> getXnhks0601ListByCodes(Set<String> codes) {
        List<Xnhks0601> xnhks0601s = Lists.newArrayList();
        // 分批code查询，业务代码组合
        // 200个1批
        List<String> list = new ArrayList<>(codes);
        int size = list.size();
        int num = 100;
        int toIndex = 100;
        for (int i = 0; i < size; i += num) {
            if (i + 100 > size) {
                toIndex = size - i;
            }

            List<String> subCodes = list.subList(i, i + toIndex);
//            List<Xnhks0601> xnhks0601List = xnhks0601Mapper.getS0601ListByCodes(subCodes);
            List<Xnhks0601> xnhks0601ListSour = xnhks0601Mapper.selectList(new QueryWrapper<Xnhks0601>().in("SECCODE", subCodes));

            List<Xnhks0601> xnhks0601List = new ArrayList<>(
                    xnhks0601ListSour.stream()
                            .collect(Collectors.toMap(
                                    Xnhks0601::getSeccode,
                                    Function.identity(),
                                    (existing, newRecord) -> {
                                        return existing.getF001d().compareTo(newRecord.getF001d()) > 0 ? existing : newRecord;
                                    }
                            ))
                            .values() // 直接提取最新记录
            );
            if (CollUtil.isNotEmpty(xnhks0601List)) {
                xnhks0601s.addAll(xnhks0601List);
            }
        }
        return ResultT.success(xnhks0601s);
    }

    @Override
    public Xnhk0406 getPeg(String stockCode) {
        return xnhk0406Mapper.selectOne(new QueryWrapper<Xnhk0406>().eq("SECCODE", stockCode));
    }

    @Override
    public ResultT<List<Xnhk0406>> getXnhk0406List() {
        return ResultT.success(xnhk0406Mapper.selectList(null));
    }

    @Override
    public Xnhk0410 getGrowthRate(String stockCode) {
        return xnhk0410Mapper.selectOne(new QueryWrapper<Xnhk0410>().eq("SECCODE", stockCode));
    }

    @Override
    public ResultT<List<Xnhk0410>> getXnhk0410List() {
        return ResultT.success(xnhk0410Mapper.selectList(null));
    }

    @Override
    public Xnhks0101 xnhks0101(String stockCode) {
        return xnhks0101Mapper.selectOne(new QueryWrapper<Xnhks0101>().eq("SECCODE", stockCode));
    }

    @Override
    public ResultT<List<Xnhks0101>> getXnhks0101List(List<String> codeList) {
        return ResultT.success(xnhks0101Mapper.selectList(new QueryWrapper<Xnhks0101>().in("SECCODE", codeList)));
    }

    @Override
    public Xnhk0201 getXnhk0201(String stockCode) {
        return xnhk0201Mapper.selectOne(new QueryWrapper<Xnhk0201>().eq("SECCODE", stockCode)
                .orderByDesc("F002D").last("limit 1"));
    }

    @Override
    public Xnhk0202 getXnhk0202(String stockCode) {
        return xnhk0202Mapper.selectOne(new QueryWrapper<Xnhk0202>().eq("SECCODE", stockCode)
                .orderByDesc("F002D").last("limit 1"));
    }

    @Override
    public Xnhk0203 getXnhk0203(String stockCode) {
        return xnhk0203Mapper.selectOne(new QueryWrapper<Xnhk0203>().eq("SECCODE", stockCode)
                .orderByDesc("F002D").last("limit 1"));
    }

    @Override
    public Xnhk0204 getXnhk0204(String stockCode) {
        return xnhk0204Mapper.selectOne(new QueryWrapper<Xnhk0204>().eq("SECCODE", stockCode)
                .orderByDesc("F002D").last("limit 1"));
    }

    @Override
    public Xnhk0205 getXnhk0205(String stockCode) {
        return xnhk0205Mapper.selectOne(new QueryWrapper<Xnhk0205>().eq("SECCODE", stockCode)
                .orderByDesc("F002D").last("limit 1"));
    }

    @Override
    public Xnhk0206 getXnhk0206(String stockCode) {
        return xnhk0206Mapper.selectOne(new QueryWrapper<Xnhk0206>().eq("SECCODE", stockCode)
                .orderByDesc("F002D").last("limit 1"));
    }

    @Override
    public Xnhk0207 getXnhk0207(String stockCode) {
        return xnhk0207Mapper.selectOne(new QueryWrapper<Xnhk0207>().eq("SECCODE", stockCode)
                .orderByDesc("F002D").last("limit 1"));
    }

    @Override
    public Xnhk0208 getXnhk0208(String stockCode) {
        return xnhk0208Mapper.selectOne(new QueryWrapper<Xnhk0208>().eq("SECCODE", stockCode)
                .orderByDesc("F002D").last("limit 1"));
    }

    @Override
    public Xnhk0209 getXnhk0209(String stockCode) {
        return xnhk0209Mapper.selectOne(new QueryWrapper<Xnhk0209>().eq("SECCODE", stockCode)
                .orderByDesc("F002D").last("limit 1"));
    }

    @Override
    public Xnhk0702 getXnhk0702(String warrantCode) {
        return xnhk0702Mapper.selectOne(new QueryWrapper<Xnhk0702>().eq("SECCODE", warrantCode)
                .orderByDesc("F001D").last("limit 1"));
    }

    @Override
    public Xnhks0701 getXnhks0701(String warrantCode) {
        return xnhks0701Mapper.selectOne(new QueryWrapper<Xnhks0701>().eq("SECCODE", warrantCode));
    }


    @Override
    public SimplePageResp<HoldStockChange> holdStockChange(SimplePageReq pageReq) {

        Page<Xnhks0601> xnhks0601Page = xnhks0601Mapper.pageByLast(new Page<>(pageReq.getCurrentPage(), pageReq.getPageSize()));
        SimplePageResp simplePageResp = new SimplePageResp<HoldStockChange>();
        simplePageResp.setSize(xnhks0601Page.getSize());
        simplePageResp.setCurrent(xnhks0601Page.getCurrent());
        simplePageResp.setTotal(xnhks0601Page.getTotal());
        List<Xnhks0601> records = xnhks0601Page.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {

            List<HoldStockChange> collect = records.stream().map(xnhks0601 -> {
                StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(xnhks0601.getSeccode()));

                HoldStockChange holdStockChange = new HoldStockChange();
                if (snapshot != null) {
                    BeanUtil.copyProperties(snapshot, holdStockChange);
                }
                if (xnhks0601.getF007n() != null) {
                    holdStockChange.setChangeQuantity(xnhks0601.getF007n());
                    holdStockChange.setChangeAmount(xnhks0601.getF010n() == null ? null : xnhks0601.getF010n().multiply(xnhks0601.getF007n()));
                } else if (xnhks0601.getF018n() != null) {
                    holdStockChange.setChangeQuantity(xnhks0601.getF018n());
                    holdStockChange.setChangeAmount(xnhks0601.getF021n() == null ? null : xnhks0601.getF021n().multiply(xnhks0601.getF018n()));
                }
                holdStockChange.setChangeRate(BigDecimalUtil.rate100(xnhks0601.getF014n(), xnhks0601.getF027n()));
                holdStockChange.setChangeNames(xnhks0601.getF005v());
                return holdStockChange;
            }).collect(Collectors.toList());
            simplePageResp.setRecord(collect);

        }

        return simplePageResp;
    }

    @Override
    public SimplePageResp<ValuationGrowth> valuationGrowth(SimplePageReq pageReq) {
        Page<ValuationGrowth> valuationGrowthPage = xnhk0410Mapper.pageValuationGrowth(new Page<>(pageReq.getCurrentPage(), pageReq.getPageSize()));
        SimplePageResp simplePageResp = new SimplePageResp<HoldStockChange>();
        simplePageResp.setSize(valuationGrowthPage.getSize());
        simplePageResp.setCurrent(valuationGrowthPage.getCurrent());
        simplePageResp.setTotal(valuationGrowthPage.getTotal());
        valuationGrowthPage.getRecords().forEach(item -> {
            StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(item.getCode()));
            if (snapshot != null) {
                item.setName(snapshot.getName());
                item.setChg(snapshot.getChg());
                item.setLast(snapshot.getLast());
                item.setChgPct(snapshot.getChgPct());
            }


        });
        simplePageResp.setRecord(valuationGrowthPage.getRecords());
        return simplePageResp;
    }

    @Override
    public ResultT<List<String>> getXnhks0101sToday() {
        // 获取新股逻辑
        List<String> newCodes = new ArrayList<>();
        Long nowDate = Long.parseLong(DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()));
        List<Object> xnhks0501List = this.getXnhks0101(nowDate).getData();
        if (CollectionUtils.isNotEmpty(xnhks0501List)) {
            newCodes = xnhks0501List.stream().map(Objects::toString).collect(Collectors.toList());
        }
        log.info("今日新股数据code：{}", newCodes);

        String date = DateUtils.getDate("yyyyMMdd", new Date());
        Map<String, String> codeMap = this.getStockConversionMarket(date).getData();
        List<String> codes = codeMap.values().stream().collect(Collectors.toList());
        log.info("今日转板数据code：{}", codes);
        if(CollectionUtils.isNotEmpty(codes)){
            if(CollectionUtils.isNotEmpty(newCodes)){
                newCodes.addAll(codes);
            }else{
                return ResultT.success(codes.stream().distinct().collect(Collectors.toList()));
            }
        }
        return ResultT.success(newCodes.stream().distinct().collect(Collectors.toList()));
    }

    /**
     * 港股转板代码变更场景，获取指定日期发生的代码变更记录
     * @param date YYYYmmdd
     * @return key是变更前code，value是变更后code
     */
    @Override
    public ResultT<Map<String, String>> getStockConversionMarket(String date) {
        List<Xnhk0307> xnhk0307s = xnhk0307Mapper.selectList(new QueryWrapper<Xnhk0307>().eq("F003D", date));
        Map<String, String> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(xnhk0307s)) {
            map = xnhk0307s.stream().collect(Collectors.toMap(item -> ConcatCodeUtil.concatCodeAndHK(item.getF004v()), item -> ConcatCodeUtil.concatCodeAndHK(item.getF002v())));
        }
        log.info("获取指定日期发生的代码变更记录，入参日期：{}，变更记录：{}", date, JsonUtils.beanToJson(map));
        return ResultT.success(map);
    }

    @Override
    public List<String> getHkStockThroughList(String stockCode) {
        List<String> stockCodeList = new ArrayList<>();
        //查所有港股通的股票
        List<String> hkStockCodeThroughList = xnhks0101Mapper.getHkStockCodeThrough();
        if (StrUtil.isBlank(stockCode)) {
            stockCodeList.addAll(hkStockCodeThroughList);
        } else {
            if (!hkStockCodeThroughList.contains(stockCode)) {
                log.info("{}非港股通的股票，不处理", stockCode);
            } else {
                stockCodeList.add(stockCode);
            }
        }
        return stockCodeList;
    }
    /**
     * 保存市场对比股票数据
     *
     * @param date 交易日日期
     */
    @Override
    public void saveMarketStock(Date date) {
        List<Object> codes = xnhk0403Mapper.selectObjs(new QueryWrapper<Xnhk0403>().select("SECCODE").groupBy("SECCODE"));
        List<Xnhk0403> xnhk0403s = xnhk0403Mapper.selectList(new QueryWrapper<Xnhk0403>()
                .in("SECCODE", codes.toArray()));
//        Map<Object, Object> map = redisClient.hmget(RedisKeyConstants.COMPRESS_STOCK_MAP);
//
//        List<StockSnapshot> snapshotList = map.values().stream().map(item ->
//                JSON.parseObject(ZipUtil.gunzip((String) item), StockSnapshot.class)
//        ).filter(item -> item.getCode().contains(".hk")).collect(Collectors.toList());
//
//         Map<String, String> codeAndNameMap = snapshotList.stream().filter(s -> codes.contains(s.getCode()))
//                 .collect(Collectors.toMap(StockSnapshot::getCode, StockSnapshot::getName));
        Map<String, String> codeAndNameMap = stockCache.queryStockNameMap(null);
        xnhk0403s.forEach(x -> {
            if (codeAndNameMap.containsKey(x.getSeccode())) {
                MarketPresence marketPresence = MarketPresence.builder()
                        .time(date.getTime())
                        .code(x.getSeccode())
                        .name(codeAndNameMap.get(x.getSeccode()))
                        .todayChgPct(x.getF011n())
                        .weekChgPct(x.getF012n())
                        .monthChgPct(x.getF013n())
                        .nearThreeMonthChgPct(x.getF014n())
                        .nearSixMonthChgPct(x.getF015n())
                        .fiftyTwoWeeksChgPct(x.getF016n())
                        .yearToDateChgPct(x.getF017n())
                        .nearTwoYearChgPct(x.getF018n())
                        .nearThreeYearChgPct(x.getF019n())
                        .createTime(date)
                        .updateTime(date)
                        .type(1)
                        .strTime(DateUtils.formatDate(date, PATTERN))
                        .build();
                mongoTemplate.save(marketPresence, COLLECTION_NAME);
            }
        });

    }
    @Override
    public void saveF10Mongo(MarketPresence marketPresence) {
        mongoTemplate.save(marketPresence, COLLECTION_NAME);
    }

    @Override
    public List<SouthwardCapitalStatistics> selectByMarket(String market, int limit){
        return southwardCapitalStatisticsMapper.selectByMarket(market, limit);
    }

    @Override
    public int saveOrUpdateBatch(List<SouthwardCapitalStatistics> list){
        return southwardCapitalStatisticsMapper.saveOrUpdateBatch(list);
    }

}
