package com.vv.finance.investment.bg.api.impl.frontend;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenlibao.security.sdk.ws.core.model.req.*;
import com.fenlibao.security.sdk.ws.core.model.resp.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultCode;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.base.utils.ZoneDateUtils;
import com.vv.finance.common.constants.OptionSortConstant;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.TradeDirectionConstants;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.constants.omdc.*;
import com.vv.finance.common.dto.ComSearchStockDto;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.common.OrderBrokerDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.common.WarrantSnapshot;
import com.vv.finance.common.entity.quotation.common.ComSimpleStockDefine;
import com.vv.finance.common.entity.quotation.common.ComStockBaseDTO;
import com.vv.finance.common.entity.quotation.common.ComStockBaseInfoDTO;
import com.vv.finance.common.entity.receiver.SnapshotQuery;
import com.vv.finance.common.enums.*;
import com.vv.finance.common.us.constants.UsRedisKeyConstants;
import com.vv.finance.common.us.utils.UsDateUtils;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.common.utils.SearchUtil;
import com.vv.finance.common.utils.ZipUtil;
import com.vv.finance.investment.bg.api.frontend.IndexService;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.stock.StockRankingApi;
import com.vv.finance.investment.bg.api.stock.StockTradeStatisticsApi;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.StockStatueEnum;
import com.vv.finance.investment.bg.dto.StockBaseInfoDTO;
import com.vv.finance.investment.bg.dto.info.InduBaseRankDTO;
import com.vv.finance.investment.bg.dto.info.TotalCapitalInflowsDTO;
import com.vv.finance.investment.bg.dto.stock.StockBaseDTO;
import com.vv.finance.investment.bg.dto.stock.StockQueryDTO;
import com.vv.finance.investment.bg.entity.req.KlineQueryReq;
import com.vv.finance.investment.bg.entity.uts.Xnhk1002;
import com.vv.finance.investment.bg.mapper.uts.Xnhk1002Mapper;
import com.vv.finance.investment.bg.stock.indicator.entity.BaseStockIndicator;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.StockRelatedDetails;
import com.vv.finance.investment.bg.stock.info.entity.StockTrade;
import com.vv.finance.investment.bg.stock.info.mapper.HkStockRelationMapper;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.stock.info.service.IStockRelatedDetailsService;
import com.vv.finance.investment.bg.stock.kline.entity.RtStockKline;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import com.vv.finance.investment.bg.utils.CollectUtils;
import com.vv.finance.investment.gateway.api.stock.IKlineBusinessApi;
import com.vv.finance.investment.gateway.api.stock.IStockBusinessApi;
import com.vv.finance.investment.gateway.api.stock.ITargetBusinessApi;
import com.vv.finance.investment.gateway.dto.req.AllMaKlineReq;
import com.vv.finance.investment.gateway.dto.req.FiuBaseReq;
import com.vv.finance.investment.gateway.dto.resp.AllMaKlineResp;
import com.vv.finance.investment.gateway.dto.resp.HKIndexQuoteResp;
import com.vv.finance.investment.gateway.dto.resp.HkCapitalFlowResp;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.vv.finance.common.constants.RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_NAME_SET;

// import static com.vv.finance.investment.bg.cache.StockCache.getPinYinAbbr;

@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class StockServiceImpl implements StockService {


    @Resource
    StockDefineMapper stockDefineMapper;
    @Resource
    IStockDefineService stockDefineService;
    @Resource
    IIndustrySubsidiaryService industrySubsidiaryService;
    @Resource
    RedisClient redisClient;
    @Resource
    IStockMarketService stockMarketService;
    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    IStockBusinessApi stockBusinessApi;
    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    ITargetBusinessApi targetBusinessApi;
    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    IKlineBusinessApi klineBusinessApi;
    @DubboReference(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
    StockRankingApi stockRankingApi;
//    @DubboReference(group = "${dubbo.hk.investment.quotation.min.service.group:min}", registry = "quotationminservice")
//    HkStockMinApi hkStockMinApi;
//    @DubboReference(group = "${dubbo.investment.composite.service.group:composite}", registry = "compositeservice")
//    HkStockCompositeApi compositeApi;
    @Resource
    IStockRelatedDetailsService stockRelatedDetailsService;
    @Resource
    private StockInfoApi stockInfoApi;

    @Resource
    private HkStockRelationMapper hkStockRelationMapper;


    ExecutorService KlineExecutorService = TtlExecutors.getTtlExecutorService(new ThreadPoolExecutor(5, 8, 0,TimeUnit.SECONDS, new LinkedBlockingQueue<>(3000), new CustomizableThreadFactory("rtIndicator")));

    ExecutorService RankExecutorService = TtlExecutors.getTtlExecutorService(new ThreadPoolExecutor(4, 8, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(3000), new CustomizableThreadFactory("stockRank")));

    //实时行情字段
    private static final String[] field = {"snapshot", "order", "trade"};

    @Value("#{'${hk.hf.date}'.split(',')}")
    private List<String> halfDate;
    @Value("${hk.receiver.snapshot.ip:172.16.6.124}")
    private String hkReceiverSnapshotIp;

    @Value("${hk.receiver.snapshot.url:/getSnapshotList}")
    private String hkReceiverSnapshotUrl;

    @Value("${hk.receiver.port:9099}")
    private String hkReceiverPort;
    @Value("${hk.receiver.manual.init.password:1CD1E940717385C15A119208AF3FC1CE}")
    private String hkReceiverPassword;

//    private final static String YMS_PATTERN = "yyyyMMddHHmmssSSS";

    @Resource
    StockCache stockCache;

    @Resource
    private Xnhk1002Mapper xnhk1002Mapper;

    //快照字段名集合
    private static List<String> snapshotFieldList;

    @Resource
    IndexService indexService;

    @Resource
    StockTradeStatisticsApi stockTradeStatisticsApi;

    @PostConstruct
    public void init() {
        Class<StockSnapshot> snapshotClass = StockSnapshot.class;
        snapshotFieldList = Arrays.stream(snapshotClass.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
    }


    @Override
    public List<StockQueryDTO> queryStock(String key, boolean isGroup,boolean isPc) {
        Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
        List<StockQueryDTO> dtoList = stockCache.getStockSimpleInfo(key).stream().map(stockInfo -> {
            StockQueryDTO stockQueryDTO = new StockQueryDTO();
            BeanUtils.copyProperties(stockInfo, stockQueryDTO);
            stockQueryDTO.setName(stockNameMap.get(stockQueryDTO.getCode()));
            return stockQueryDTO;
        }).collect(Collectors.toList());
        if (isGroup) {
            dtoList = dtoList.stream().filter(s -> s.getType() != 2).collect(Collectors.toList());
        }
        if (isPc){
            dtoList = dtoList.stream().filter(s->s.getType() != 2).collect(Collectors.toList());
        }
        return dtoList;
    }

    @Override
    public List<ComSearchStockDto> queryStockV2(String key, Integer stockType, boolean isGroup, boolean isPc) {
        return queryStockV3(key, ObjectUtil.defaultIfNull(stockType, String::valueOf, null), isGroup, isPc);
    }

    @Override
    public List<ComSearchStockDto> queryStockV3(String key, String stockType, boolean isGroup, boolean isPc) {
        List<ComStockSimpleDto> filterList = SearchUtil.filterStockByKey(key, stockType, 10, stockCache.queryStockInfoList(null));
        // Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
        List<ComSearchStockDto> dtoList = filterList.stream().map(stockInfo -> {
            ComSearchStockDto stockDto = new ComSearchStockDto();
            BeanUtils.copyProperties(stockInfo, stockDto);
            stockDto.setName(stockDto.getStockName());
            stockDto.setRegionType(RegionTypeEnum.HK.getCode());
            stockDto.setFinanceType(stockInfo.getType());
            stockDto.setExchange(ExchangeEnum.HK_EX.getCode());
            return stockDto;
        }).collect(Collectors.toList());

        if (isGroup || isPc) {
            dtoList = dtoList.stream().filter(s -> s.getType() != 2).collect(Collectors.toList());
        }

        return dtoList;
    }

    @Override
    public List<IndustrySubsidiary> getAllIndustry() {
        // List<IndustrySubsidiary> data = stockRankingApi.listIndustrySubsidiary().getData();
        // if (data != null || data.size() < 1) {
        //     return data;
        // }
        // return Collections.EMPTY_LIST;
        return industrySubsidiaryService.getAllIndustry();
    }

    @Override
    public PageDomain<StockSnapshot> getAllStock(Integer current, Integer size, String sort, String sortKey) {

        Map<Object, Object> hmget = redisClient.hmget(RedisKeyConstants.COMPRESS_STOCK_MAP);
        PageDomain<StockSnapshot> pageDomain = new PageDomain<>();
        if (hmget.size() < 1) {
            return pageDomain;
        }
        List<StockSnapshot> stockSnapshots = hmget.values().stream().map(item ->
                JSON.parseObject(ZipUtil.gunzip((String) item), StockSnapshot.class)
        ).filter(item -> item.getCode().contains(".hk")).collect(Collectors.toList());
        Map<String, String> simpleNameMap = stockCache.queryStockNameMap(null);
        if (!StringUtils.isEmpty(sort) || !StringUtils.isEmpty(sortKey)) {
            if (!StringUtils.isEmpty(sort)) {
                stockSnapshots = sort(stockSnapshots, simpleNameMap, sortKey, OptionSortConstant.UP.equals(sort));
            }
        }
        pageDomain.setRecords(stockSnapshots);
        pageDomain.setTotal(hmget.size());
        getPageList(pageDomain, current, size);
        return pageDomain;

    }

    private void getPageList(PageDomain<StockSnapshot> pageDomain, Integer current, Integer size) {
        if (current == null && size == null) {
            return;
        }

        List<StockSnapshot> records = pageDomain.getRecords();
        int ListSize = records.size();

        if (ListSize < size || ListSize < 1) {
            return;
        }

        int i = (current - 1) * size;

        if (ListSize < i) {
            pageDomain.setRecords(Collections.emptyList());
            return;
        }

        if (ListSize < (i + size)) {
            List<StockSnapshot> collect = Lists.newArrayList(records.subList(i, ListSize));
            pageDomain.setRecords(collect);
            return;
        }
        pageDomain.setRecords(new ArrayList<>(records.subList(i, i + size)));
    }

    @Override
    public PageDomain<StockSnapshot> getStockByIndustry(Integer current, Integer size, String code, String sort, String sortKey) {
        List<StockSnapshot> stockSnapshots = new LinkedList<>();
        List<StockDefine> stockDefines = stockDefineMapper.selectList(new QueryWrapper<StockDefine>().eq("industry_code", code).eq("suspension", 0).eq("stock_type", StockTypeEnum.STOCK.getCode()));
        PageDomain<StockSnapshot> pageDomain = new PageDomain<>();
        for (StockDefine stockDefine : stockDefines) {
            StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(stockDefine.getCode()));
            if (snapshot != null) {
                stockSnapshots.add(snapshot);
            }
        }
        Map<String, String> simpleNameMap = stockCache.queryStockNameMap(null);
        if (!StringUtils.isEmpty(sort) || !StringUtils.isEmpty(sortKey)) {
            if (!StringUtils.isEmpty(sort)) {
                stockSnapshots = sort(stockSnapshots, simpleNameMap, sortKey, OptionSortConstant.UP.equals(sort));
            }
        } else {
            stockSnapshots = sort(stockSnapshots, simpleNameMap, "chgPct", false);
        }
        pageDomain.setRecords(stockSnapshots);
        pageDomain.setTotal(stockSnapshots.size());
        getPageList(pageDomain, current, size);
        return pageDomain;
    }


    @Override
    public Set<String> getAllStockCode() {
        Set<String> codes = new HashSet<>();
        // List<StockDefine> stockDefines = stockDefineMapper.selectList(null);
        List<StockDefine> stockDefines = stockDefineService.listStockColumns(null);
        stockDefines.forEach(s -> codes.add(s.getCode()));
        return codes;
    }

    @Override
    public Map<String, String> getAllStockCodeAndName() {
        Map<String, String> map = new HashMap<>();
        List<StockDefine> stockDefines = stockDefineMapper.selectList(null);
        stockDefines.forEach(s -> map.put(s.getCode(), s.getName()));
        return map;
    }

    @Override
    public StockDefine getStockDefine(String code) {
        return stockDefineMapper.selectOne(new QueryWrapper<StockDefine>().eq("code", code));
    }


//    private List<StockQueryDTO> buildStockQuery(List<StockDefine> selectList) {
//        List<StockQueryDTO> stockQueryDTOS = new LinkedList<>();
//        if (selectList.size() < 1) {
//            return stockQueryDTOS;
//        }
//        for (StockDefine stockDefine : selectList) {
//            StockQueryDTO stockQueryDTO = new StockQueryDTO();
//            BeanUtils.copyProperties(stockDefine, stockQueryDTO);
//            stockQueryDTO.setType(Integer.valueOf(stockDefine.getFreetext()));
//            stockQueryDTOS.add(stockQueryDTO);
//        }
//        return stockQueryDTOS;
//    }

    @Override
    public List<StockBaseInfoDTO> getStockBaseInfoList(String industryCode, String sort, String sortKey, String market) {
        List<StockSnapshot> snapshotList = new ArrayList<>();
        //根据行业获取股票以及快照
        if (!StringUtils.isEmpty(industryCode)) {
            List<String> codeList = Arrays.asList(industryCode.split(StrPool.COMMA));
            List<StockDefine> stockDefines = stockDefineMapper.selectList(new QueryWrapper<StockDefine>()
                    .select(StockDefine.COL_CODE, StockDefine.COL_NAME, StockDefine.COL_STOCK_NAME)
                    .in("industry_code", codeList).eq("suspension", 0).eq("stock_type", StockTypeEnum.STOCK.getCode()));
            Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
            if (StringUtils.isEmpty(sort) || StringUtils.isEmpty(sortKey)) {
                return stockDefines.stream().map(stockDefine -> {
                    StockBaseInfoDTO baseInfoDTO = new StockBaseInfoDTO();
                    baseInfoDTO.setStockCode(stockDefine.getCode());
                    baseInfoDTO.setStockName(stockNameMap.get(stockDefine.getCode()));
                    baseInfoDTO.setType(0);
                    return baseInfoDTO;
                }).collect(Collectors.toList());
            }
            for (StockDefine stockDefine : stockDefines) {
                StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(stockDefine.getCode()));
                if (snapshot != null) {
                    snapshotList.add(snapshot);
                }
            }
        }
        //获取全部股票以及快照
        else {
            Map<Object, Object> hmget = redisClient.hmget(RedisKeyConstants.COMPRESS_STOCK_MAP);
            snapshotList = hmget.values().stream().map(item ->
                    JSON.parseObject(ZipUtil.gunzip((String) item), StockSnapshot.class)
            ).filter(item -> item.getCode().contains(".hk")).collect(Collectors.toList());
        }
        if (market != null) {
            snapshotList = snapshotList.stream().filter(item -> item.getMarketCode() != null && item.getMarketCode().equals(market.toUpperCase())).collect(Collectors.toList());
        }
        return getStockBaseInfoList(sort, sortKey, snapshotList);
    }

    @Override
    public List<StockBaseInfoDTO> getStockList(String industryCode, String sort, String sortKey, String market) {

        List<StockSnapshot> snapshotList = new ArrayList<>();
        if (StrUtil.isBlank(industryCode)) {
            QueryWrapper<StockDefine> queryWrapper = new QueryWrapper<StockDefine>()
                    .select(StockDefine.COL_CODE, StockDefine.COL_NAME, StockDefine.COL_STOCK_NAME)
                    .in("suspension", Arrays.asList(0)).eq("stock_type", StockTypeEnum.STOCK.getCode())
                    .in(StrUtil.isNotBlank(industryCode), "industry_code", StrUtil.split(industryCode, StrPool.COMMA));

            List<StockDefine> stockDefines = stockDefineMapper.selectList(queryWrapper);

            if (StrUtil.isNotBlank(industryCode) && (StringUtils.isEmpty(sort) || StringUtils.isEmpty(sortKey))) {
                Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
                return stockDefines.stream().map(stockDefine -> {
                    StockBaseInfoDTO baseInfoDTO = new StockBaseInfoDTO();
                    baseInfoDTO.setStockCode(stockDefine.getCode());
                    baseInfoDTO.setStockName(stockNameMap.get(stockDefine.getCode()));
                    baseInfoDTO.setType(0);
                    baseInfoDTO.setStockType(StockTypeEnum.STOCK.getCode());
                    baseInfoDTO.setRegionType(RegionTypeEnum.HK.getCode());
                    return baseInfoDTO;
                }).collect(Collectors.toList());
            }
            Set<String> codes = stockDefines.stream().map(item -> item.getCode()).collect(Collectors.toSet());
            snapshotList = getSnapshotListBySet(codes) ;
        } else {
            snapshotList = getStockSnapshotByBlockCode(industryCode);
        }

        if (market != null) {
            snapshotList = snapshotList.stream().filter(item -> item.getMarketCode() != null && item.getMarketCode().equals(market.toUpperCase())).collect(Collectors.toList());
        }

        return getStockBaseInfoList(sort, sortKey, snapshotList);
    }

    @Override
    public List<StockBaseInfoDTO> getStockListIncludeClose(String sort, String sortKey) {
        long time1 = System.currentTimeMillis();
        //获取当日码表数据
        Set<String> hkStockNameSet = redisClient.get(RECEIVER_NEWEST_STOCK_CODE_NAME_SET);

        List<String> hkStockCodeList = hkStockNameSet.stream()
                .map(item -> item.split(",")[0]).collect(Collectors.toList());
        long time2 = System.currentTimeMillis();
        log.info("获取港股码表数据耗时={},hkStockCodeList={}", time2 - time1, hkStockCodeList.size());
        List<String> quitCodeList = stockMarketService.getCloseDefineCode().getData();
        log.info("获取港股停牌股票数据耗时={},quitCodeList={}", System.currentTimeMillis() - time2, quitCodeList.size());
        hkStockCodeList.addAll(quitCodeList);
        List<StockSnapshot> snapshotList = getSnapshotListBySet(new HashSet<>(hkStockCodeList));
        return getStockBaseInfoList(sort, sortKey, snapshotList);
    }

    @Override
    public List<StockBaseInfoDTO> getSortStockList(List<String> codes, String sort, String sortKey) {
        if (CollUtil.isEmpty(codes)) {
            return new ArrayList<>();
        }
        long t = System.currentTimeMillis();
        codes = codes.stream().distinct().collect(Collectors.toList());
        int minCount = 500;
        List<StockSnapshot> snapshotList = new ArrayList<>(codes.size());
        if (codes.size() > minCount) {
            // 股票数量过多，用多线程查询快照
            List<List<String>> partition = Lists.partition(codes, minCount);
            List<FutureTask<List<StockSnapshot>>> futures = new ArrayList<>(partition.size());
            for (List<String> codeList : partition) {
                FutureTask<List<StockSnapshot>> future = new FutureTask<>(() -> getSnapshotListBySet(new HashSet<>(codeList)));
                futures.add(future);
                new Thread(future).start();
            }
            for (Future<List<StockSnapshot>> future : futures) {
                try {
                    snapshotList.addAll(future.get());
                } catch (Exception e) {
                    log.warn("查询快照异常", e);
                }
            }
        } else {
            snapshotList = getSnapshotListBySet(new HashSet<>(codes));
        }
        log.info("查询快照耗时:{}", System.currentTimeMillis() - t);
        return getStockBaseInfoList(sort, sortKey, snapshotList);
    }

    public List<StockBaseInfoDTO> getStockBaseInfoList(String sort, String sortKey, List<StockSnapshot> snapshotList) {
        Map<String, String> simpleNameMap = stockCache.queryStockNameMap(null);
        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(sortKey)) {
            snapshotList = sort(snapshotList, simpleNameMap, sortKey, SortEnum.ASC.getValue().equals(sort));
        } else {
            //默认按涨跌幅倒序排列
            snapshotList = sort(snapshotList, simpleNameMap, "chgPct", false);
        }
        return snapshotList.stream().map(snapshot -> {
            StockBaseInfoDTO baseInfoDTO = new StockBaseInfoDTO();
            baseInfoDTO.setStockId(snapshot.getStockId());
            baseInfoDTO.setStockCode(snapshot.getCode());
            baseInfoDTO.setStockName(simpleNameMap.get(snapshot.getCode()));
            baseInfoDTO.setType(snapshot.getType());
            baseInfoDTO.setStockType(snapshot.getStockType());
            baseInfoDTO.setRegionType(RegionTypeEnum.HK.getCode());
            return baseInfoDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<StockSnapshot> getSnapshotList(String[] stockCodeList) {
        List<StockSnapshot> snapshotList = new ArrayList<>();
        List<String> codeList = Arrays.asList(stockCodeList.clone());
        //如果使用多线程可能会发生数据覆盖的情况
        if (CollectionUtils.isNotEmpty(codeList)){
//            Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
            List<ComStockSimpleDto> stockSimpleInfos = stockCache.queryStockInfoList(codeList);
            Map<String, Long> stockIdMap = stockInfoApi.selectStockIdAllByCodes(codeList);
            Map<String, ComStockSimpleDto> stockDefineMap = stockSimpleInfos.stream().filter(stockSimple -> codeList.contains(stockSimple.getCode())).collect(Collectors.toMap(stockSimple -> stockSimple.getCode(), Function.identity(), (k1, k2) -> k1));
            //批量获取港股快照数据
            List<StockSnapshot> snapshots = getHkSnapshotList(codeList,stockDefineMap);
            Map<String, StockSnapshot> snapshotMap = snapshots.stream().collect(Collectors.toMap(snapshot -> snapshot.getCode(), Function.identity(), (k1, k2) -> k1));
            codeList.forEach(code->{
                StockSnapshot stockSnapshot = snapshotMap.get(code);
                ComStockSimpleDto comStockSimpleDto = new ComStockSimpleDto();
                if(MapUtils.isNotEmpty(stockDefineMap) && ObjectUtils.isNotEmpty(stockDefineMap.get(code))){
                    comStockSimpleDto = stockDefineMap.get(code);
                }
                if (ObjectUtils.isNotEmpty(stockSnapshot)){
                    stockSnapshot.setName(comStockSimpleDto.getStockName());
                    stockSnapshot.setRegionType(RegionTypeEnum.HK.getCode());
                    stockSnapshot.setStockRights(comStockSimpleDto.getStockRights());
                    //部分股票type可能为null，默认设置type为股票类型
                    if(stockSnapshot.getType() == null){
                        stockSnapshot.setType(0);
                    }
                    if (null==stockSnapshot.getStockId()){
                        stockSnapshot.setStockId(stockIdMap.get(code));
                    }
                    stockSnapshot.setMarketCode(comStockSimpleDto.getMarketCode());
                    stockSnapshot.setDomainCode(comStockSimpleDto.getDomainCode());
                    stockSnapshot.setIndustryCode(comStockSimpleDto.getIndustryCode());
                    stockSnapshot.setIndustryName(comStockSimpleDto.getIndustryName());
                    stockSnapshot.setLotSize(comStockSimpleDto.getLotsize());
                    snapshotList.add(stockSnapshot) ;
                }
            });
        }
        return snapshotList;
    }
    //批量获取港股快照数据
    private List<StockSnapshot> getHkSnapshotList(List<String> stockCodeList,Map<String, ComStockSimpleDto> stockDefineMap) {
        long time1=System.currentTimeMillis();
        if (CollUtil.isEmpty(stockCodeList)) {
            return Lists.newArrayList();
        }
        //批量获取receiver快照缓存数据
        List<StockSnapshot> receiverLocalSnapshots = getSnapshotByReceiverLocal(stockCodeList);
        long time2 = System.currentTimeMillis();
        log.info("getSnapshotByReceiverLocal 耗时 {}",time2-time1);
        //receiver本地缓存拿不到的code再到redis取
        List<String> receiverLocalStockCodes = receiverLocalSnapshots.stream().map(stockSnapshot -> stockSnapshot.getCode()).collect(Collectors.toList());
        List<String> unReceiverLocalStockCodes = stockCodeList.stream().filter(stockCode -> !receiverLocalStockCodes.contains(stockCode)).collect(Collectors.toList());
        long time3 = System.currentTimeMillis();
        log.info("unReceiverLocalStockCodes 耗时 {}",time3-time2);
        if (CollUtil.isNotEmpty(unReceiverLocalStockCodes)) {
            List<String> keys = unReceiverLocalStockCodes.stream().filter(code -> StringUtils.isNotEmpty(code)).map(code -> RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(code)).collect(Collectors.toList());
            List<StockSnapshot> receiverSnapshots = redisClient.batchGet(keys);
            long time4 = System.currentTimeMillis();
            log.info("快照redis缓存batchGet 耗时 {}",time4-time3);
            receiverLocalSnapshots.addAll(receiverSnapshots);
            //没有快照的股票用码表数据补偿
            buildSnapshotByStockDefine(stockDefineMap, receiverLocalSnapshots, unReceiverLocalStockCodes, receiverSnapshots);
            long time5 = System.currentTimeMillis();
            log.info("没有快照的股票用码表数据补偿 耗时 {}",time5-time4);
        }

        return receiverLocalSnapshots;
    }

    //没有快照的股票用码表数据补偿
    private void buildSnapshotByStockDefine(Map<String, ComStockSimpleDto> stockDefineMap,
                                            List<StockSnapshot> receiverLocalSnapshots,
                                            List<String> unReceiverLocalStockCodes,
                                            List<StockSnapshot> receiverSnapshots) {
        List<String> receiveStockCodes = receiverSnapshots.stream().map(stockSnapshot -> stockSnapshot.getCode()).collect(Collectors.toList());
        List<String> unKnowCodes = unReceiverLocalStockCodes.stream().filter(stockCode -> !receiveStockCodes.contains(stockCode)).collect(Collectors.toList());
        unKnowCodes.forEach(unKnowCode->{
            //缓存中没有的时候从数据库重新构建快照信息
            buildStockSnapshot(unKnowCode, receiverLocalSnapshots);
            StockRelatedDetails details = stockRelatedDetailsService.getOne(new QueryWrapper<StockRelatedDetails>().eq("code", unKnowCode));
            if (details != null ) {
                if (details.getType()==0){
                    StockSnapshot snapshot = JSON.parseObject(details.getSnapshotDetails(), StockSnapshot.class);
                    log.info("数据库填充快照 stockSnapshot：{} ",snapshot);
                    receiverLocalSnapshots.add(snapshot);
                }

            }else {
                //数据库没有则用码表填充
                ComStockSimpleDto comSimpleStockDefine = stockDefineMap.get(unKnowCode);
                if (ObjectUtil.isNotEmpty(comSimpleStockDefine)) {
                    StockSnapshot stockSnapshot = new StockSnapshot();
                    stockSnapshot.setCode(comSimpleStockDefine.getCode());
                    stockSnapshot.setStockType(comSimpleStockDefine.getStockType());
                    stockSnapshot.setName(comSimpleStockDefine.getStockName());
                    Integer suspension =null;
                    if (StockSecurityStatusEnum.STOP.getCode().equals(comSimpleStockDefine.getSecurityStatus())) {
                        suspension= ComStockStatueEnum.STOP.getCode();
                    }else  {
                        suspension=ComStockStatueEnum.UNLISTED.getCode();
                    }
                    stockSnapshot.setSuspension(suspension);
                    stockSnapshot.setRegionType(RegionTypeEnum.HK.getCode());
                    log.info("码表填充快照 stockSnapshot：{} ",stockSnapshot);
                    receiverLocalSnapshots.add(stockSnapshot);
                }
            }
        });
    }


    /**
     * 批量获取receiver快照缓存数据
     * @param codes
     */
    private List<StockSnapshot> getSnapshotByReceiverLocal(List<String> codes) {
        List<StockSnapshot> snapshotList = new ArrayList<>();
        try {
            StringBuilder ipStr = new StringBuilder();
            //根据codes批量获取快照数据
            ipStr.append("http://").append(hkReceiverSnapshotIp).append(":").append(hkReceiverPort).append(hkReceiverSnapshotUrl);
            SnapshotQuery query = new SnapshotQuery();
            query.setCodes(codes);
            query.setPsw(hkReceiverPassword);
            String data = HttpRequest.post(ipStr.toString()).header("Content-Type", "application/json").body(JSONObject.toJSONString(query)).execute().body();
            ResultT<List<StockSnapshot>> resultT = JSONUtil.toBean(data, ResultT.class);
            if (resultT.getCode() == ResultCode.SUCCESS.code()) {
                String zip = resultT.getToastMessage();
                List<StockSnapshot> snapshots = JSON.parseArray(ZipUtil.gunzip(zip), StockSnapshot.class);
                snapshotList.addAll(snapshots);
            }
        } catch (Exception e) {
            log.info("查询本地快照异常！ip:{} codes:{}", hkReceiverSnapshotIp, codes, e);
        }
        return snapshotList;
    }
    @Override
    public List<StockSnapshot> getSnapshotListBySet(Set<String> stockCodeList) {
        String[] codes = new String[stockCodeList.size()];
        stockCodeList.toArray(codes);
        return getSnapshotList(codes);
    }

    @Override
    public List<StockSnapshot> getSnapshotListSort(Set<String> stockCodeList, String sortKey, String sort) {
        List<StockSnapshot> snapshotList = getSnapshotListBySet(stockCodeList);
        // 只有根据名称和行业名称排序时，才查询名称缓存
        Map<String, String> simpleNameMap = ListUtil.of("name", "industryName").contains(sortKey) ? stockCache.queryStockNameMap(null) : MapUtil.empty();
        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(sortKey)) {
            snapshotList = sort(snapshotList, simpleNameMap, sortKey, SortEnum.ASC.getValue().equals(sort));
        } else {
            //默认按涨跌幅倒序排列
            snapshotList = sort(snapshotList, simpleNameMap, "chgPct", false);
        }
        return snapshotList;
    }



    @Override
    public StockSnapshot getOnlyStockSnapshot(String code) {
        StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(code));
        if (snapshot == null) {
            return new StockSnapshot();
        }
        return snapshot;
    }

    private List<StockSnapshot> sort(List<StockSnapshot> snapshotList, Map<String, String> simpleNameMap, String sortKey, boolean isUp) {
        if ("name".equals(sortKey)) {
            snapshotList.forEach(snap -> snap.setName(simpleNameMap.get(snap.getCode())));
            if (isUp) {
                snapshotList.sort(Comparator.comparing(o -> simpleNameMap.get(o.getName()), Comparator.nullsFirst(String::compareTo)));
            } else {
                Comparator<StockSnapshot> comparing = Comparator.comparing(o -> simpleNameMap.get(o.getName()), Comparator.nullsFirst(String::compareTo));
                snapshotList.sort(comparing.reversed());
            }
        } else if ("code".equals(sortKey)) {
            if (isUp) {
                snapshotList.sort(Comparator.comparing(StockSnapshot::getCode, Comparator.nullsFirst(String::compareTo)));
            } else {
                snapshotList.sort(Comparator.comparing(StockSnapshot::getCode, Comparator.nullsFirst(String::compareTo)).reversed());
            }
        } else if ("industryName".equals(sortKey)) {
            Comparator<StockSnapshot> firstComparator = Comparator.comparing(o -> simpleNameMap.get(o.getIndustryName()), Comparator.nullsFirst(String::compareTo));
            Comparator<StockSnapshot> secondComparator = firstComparator.thenComparing(StockSnapshot::getCode);
            if (isUp) {
                if (CollUtil.isNotEmpty(snapshotList)) {
                    snapshotList.sort(secondComparator);
                }
            } else {
                if (CollUtil.isNotEmpty(snapshotList)) {
                    snapshotList.sort(secondComparator.reversed());
                }
            }
        } else if ("chgPct".equals(sortKey)) {
            // 按涨跌幅排序
            return sortSnapshotsByChgPct(isUp, snapshotList);
        } else if (snapshotFieldList.contains(sortKey)) {
            try {
                PropertyDescriptor descriptor = new PropertyDescriptor(sortKey, StockSnapshot.class);
                Method readMethod = descriptor.getReadMethod();
                // snapshotList.sort(Comparator.comparing(StockSnapshot::getCode));
                Comparator<StockSnapshot> firstComp = Comparator.comparing(o -> {
                    try {
                        Object result = readMethod.invoke(o);
                        return ObjectUtil.isNotEmpty(result) ? new BigDecimal(result.toString()) : null;
                    } catch (Exception e) {
                        log.error("执行方法失败");
                        return null;
                    }
                }, Comparator.nullsFirst(BigDecimal::compareTo));
                Comparator<StockSnapshot> secondComp = firstComp.thenComparing(StockSnapshot::getCode);

                snapshotList.sort(isUp ? secondComp : secondComp.reversed());
            } catch (Exception e) {
                log.error("排序失败", e);
            }
        }
        return snapshotList;
    }

    public List<StockSnapshot> sortSnapshotsByChgPct(boolean isUp, List<StockSnapshot> snapshotList) {

        // 待上市(--) -> 负数 -> 停牌 -> 0 -> 正数
        // 停牌
        List<StockSnapshot> stoppedSnapshotList = snapshotList.stream().filter(ss -> StockStatueEnum.STOP.getCode() == ss.getSuspension()).collect(Collectors.toList());
        // 其他状态（港股3种状态：正常、停牌、退市）
        List<StockSnapshot> otherSnapshotList = CollUtil.subtractToList(snapshotList, stoppedSnapshotList);

        // 停牌记录，按照股票代码正序
        List<StockSnapshot> soredStopSnapshots = stoppedSnapshotList.stream().sorted(Comparator.comparing(StockSnapshot::getCode, Comparator.nullsFirst(String::compareTo))).collect(Collectors.toList());
        // 其他状态，按照涨跌幅排序
        List<StockSnapshot> sortedOtherSnapshots = otherSnapshotList.stream().sorted(Comparator.comparing(StockSnapshot::getChgPct, Comparator.nullsFirst(BigDecimal::compareTo)).thenComparing(StockSnapshot::getCode)).collect(Collectors.toList());

        // 非负数
        List<StockSnapshot> nonNegativeSnapshots = sortedOtherSnapshots.stream().filter(snap -> ObjectUtil.isNotEmpty(snap.getChgPct()) && NumberUtil.isGreaterOrEqual(snap.getChgPct(), BigDecimal.ZERO)).collect(Collectors.toList());
        // 负数
        List<StockSnapshot> negativeSnapshots = CollUtil.subtractToList(sortedOtherSnapshots, nonNegativeSnapshots);

        // 待上市(--) -> 负数 -> 停牌 -> 0 -> 正数
        List<StockSnapshot> sortedUnionSnapshots = CollUtil.unionAll(negativeSnapshots, soredStopSnapshots, nonNegativeSnapshots);

        return isUp ? sortedUnionSnapshots : ListUtil.reverse(sortedUnionSnapshots);
    }

    @Override
    public List<StockBaseDTO> getStockSort(
            String sortKey,
            Integer num
    ) {
        RankReq rankReq = new RankReq();
        rankReq.setMode(OmdcMode.RT);
        rankReq.setMarket(OmdcMarket.MAIN);
        rankReq.setType(SecurityType.EQTY);
        if (num == null) {
            num = 10;
        }
        rankReq.setNumber(num);
        rankReq.setKind(sortKey);
        List<RankResp> data = stockBusinessApi.rank(rankReq).getData();
        LinkedList<StockBaseDTO> stockBaseDTOS = new LinkedList<>();
        for (RankResp datum : data) {
            StockBaseDTO stockBaseDTO = new StockBaseDTO();
            stockBaseDTO.setCode(datum.getSymbol());
            stockBaseDTO.setName(datum.getName());
            stockBaseDTO.setLast(datum.getLast());
            stockBaseDTO.setChg(datum.getChg());
            stockBaseDTO.setChgPct(datum.getChg_pct());
            stockBaseDTOS.add(stockBaseDTO);
        }

        return stockBaseDTOS;
    }

    @Override
    public List<StockBaseDTO> getRank5Min(String sortKey, Integer num) {
        RankMin5Req rankReq = new RankMin5Req();
        rankReq.setMode(OmdcMode.RT);
        rankReq.setMarket(OmdcMarket.MAIN);
        rankReq.setType(SecurityType.EQTY);
        if (num == null) {
            num = 10;
        }
        rankReq.setNumber(num);
        rankReq.setKind(sortKey);
        ResultT<List<RankMin5Resp>> listResultT = stockBusinessApi.rankMin5(rankReq);
        List<StockBaseDTO> stockBaseDTOS = new LinkedList<>();
        if (listResultT.getCode() == ResultT.success().getCode()) {
            stockBaseDTOS = listResultT.getData().stream().map(item -> {
                StockBaseDTO stockBaseDTO = new StockBaseDTO();
                stockBaseDTO.setCode(item.getSymbol());
                stockBaseDTO.setName(item.getName());
                stockBaseDTO.setLast(item.getLast());
                stockBaseDTO.setChg(item.getChg());
                stockBaseDTO.setChgPct(item.getChg_pct());
                stockBaseDTO.setFiveMinutesChgPct(item.getMin5_pct());
                return stockBaseDTO;
            }).collect(Collectors.toList());
        }
        return stockBaseDTOS;
    }

    @Override
    public MarketStatisticsResp getMarketStatistics() {
        //todo 存入缓存
        MarketStatisticsReq marketStatisticsReq = new MarketStatisticsReq();
        marketStatisticsReq.setMarket(OmdcMarket.MAIN);
        ResultT<MarketStatisticsResp> marketStatistics = stockBusinessApi.getMarketStatistics(marketStatisticsReq);
        if (marketStatistics.getCode() == ResultT.success().getCode()) {
            return marketStatistics.getData();
        }
        return new MarketStatisticsResp();
    }

    @Override
    public List<InduBaseRankDTO> getIndustryRankDetail() {
        RankInduReq rankInduReq = new RankInduReq();
        rankInduReq.setKind("up");
        rankInduReq.setLevel("1");
        rankInduReq.setNumber(6);
        rankInduReq.setMode("rt");
        List<InduBaseRankDTO> result = new LinkedList<>();
        ResultT<List<RankInduResp>> listResultT = stockBusinessApi.rankIndu(rankInduReq);
        if (listResultT.getCode() == ResultT.success().getCode()) {
            result = listResultT.getData().stream().map(item -> {
                InduBaseRankDTO induBaseRankDTO = new InduBaseRankDTO();
                BeanUtils.copyProperties(item, induBaseRankDTO);
                induBaseRankDTO.setChg_pct(item.getChg_pct().toString());
                calcIndustryComponent(induBaseRankDTO);
                return induBaseRankDTO;
            }).collect(Collectors.toList());
        }
        buildIndustryRank(result);
        //融聚汇新版行业code 使用二级类别，需要手动处理成四位
        List<InduBaseRankDTO> result2 = result.stream().map(item -> {
                    item.setSymbol(item.getSymbol().substring(2, 6));
                    return item;
                }
        ).collect(Collectors.toList());
        return result2;
    }

    private void calcIndustryComponent(InduBaseRankDTO induBaseRankDTO) {
        IndhktryResp data = stockBusinessApi.indhktryV3(IndhktryReq.builder().code(induBaseRankDTO.getSymbol()).kind(OmdcKind.UP)
                .mode(OmdcMode.RT).number(10000).build()).getData();
        if (data != null && data.getStock_list() != null) {
            AtomicInteger up = new AtomicInteger();
            AtomicInteger balance = new AtomicInteger();
            AtomicInteger down = new AtomicInteger();
            data.getStock_list().forEach(item -> {
                BigDecimal chg = item.getChg();
                if (chg.compareTo(BigDecimal.ZERO) < 0) {
                    down.getAndIncrement();
                } else if (chg.compareTo(BigDecimal.ZERO) == 0) {
                    balance.getAndIncrement();
                } else {
                    up.getAndIncrement();
                }
            });
            induBaseRankDTO.setRise(up.get());
            induBaseRankDTO.setFlat(balance.get());
            induBaseRankDTO.setFall(down.get());
        }

    }

    /**
     * 构建行业排行数据
     *
     * @param result 行业数据
     */
    private void buildIndustryRank(List<InduBaseRankDTO> result) {
        try {
            if (CollectionUtils.isEmpty(result)) {
                return;
            }
            List<Future<IndhktryResp>> futures = new LinkedList<>();
            for (InduBaseRankDTO induBaseRankDTO : result) {
                Future<IndhktryResp> submit = RankExecutorService.submit(new IndustryRank(induBaseRankDTO.getSymbol()));
                futures.add(submit);
            }
            for (int i = 0; i < result.size(); i++) {
                result.get(i).setStocks(futures.get(i).get().getStock_list());
            }
        } catch (Exception e) {
            log.error("构建行业排行榜失败!", e);
        }

    }

    class IndustryRank implements Callable<IndhktryResp> {

        private String code;

        public IndustryRank(String code) {
            this.code = code;
        }

        @Override
        public IndhktryResp call() {
            return getIndhktry(code);
        }
    }


//    @Override
//    public StockKlineDTO getStockKLine(KlineQueryReq klineQueryReq) {
//
//        List<StockKline> list;
//        List<BaseStockIndicator> indicators;
//        List<BaseStockIndicator> rtIndicator = new LinkedList<>();
//        IndicatorQueryReq indicatorQueryReq = new IndicatorQueryReq();
//        indicatorQueryReq.setCode(klineQueryReq.getCode());
//        indicatorQueryReq.setNumber(klineQueryReq.getNum());
//        indicatorQueryReq.setDay(new Date(klineQueryReq.getDate()));
//        List<StockKline> rtList = new LinkedList<>();
//        boolean haveToday = true;
//        Xnhk0102 xnhk0102 = stockMarketService.getStockMarketData(klineQueryReq.getCode());
//        boolean today = isToday(klineQueryReq.getDate());
//        StockKlineReq stockMinQueryReq = new StockKlineReq();
//        StockKlineReq stockKlineReq = new StockKlineReq();
//        stockKlineReq.setAdjhkt(StringUtils.isEmpty(klineQueryReq.getAdjhkt()) ? "not" : klineQueryReq.getAdjhkt());
//        stockKlineReq.setCode(klineQueryReq.getCode());
//        stockKlineReq.setNum(klineQueryReq.getNum());
//        stockKlineReq.setTime(klineQueryReq.getDate());
//        stockKlineReq.setType(klineQueryReq.getType());
//        // 获取今日最新数据
//        if (klineQueryReq.getType().contains("min") && today) {
//            String type = klineQueryReq.getType().substring(0, klineQueryReq.getType().lastIndexOf("min"));
//            stockMinQueryReq.setType(type);
//            int count = getCount(klineQueryReq);
//            if (count == 0) {
//                haveToday = false;
//            } else {
//                MinuteKReq minuteKReq = new MinuteKReq();
//                BeanUtils.copyProperties(klineQueryReq, minuteKReq);
//                minuteKReq.setDate(DateUtils.formatDate(new Date(klineQueryReq.getDate())));
//                minuteKReq.setMinc(Integer.valueOf(type));
//                minuteKReq.setMode(OmdcMode.RT);
//                minuteKReq.setAdjhkt("");
//                List<AllMaKlineResp> data = klineBusinessApi.listMaMinuteK(minuteKReq).getData();
//                if (data != null) {
//                    rtList = data.stream().map(item -> {
//                        StockKline stockKline = new StockKline();
//                        BeanUtils.copyProperties(item, stockKline);
//                        //计算换手率
//                        if (xnhk0102 != null) {
//                            stockKline.setChangeRate(xnhk0102.getF070n() != null ? (item.getVolume().divide(xnhk0102.getF070n(), 6, RoundingMode.UP)).toString() : "");
//                        }
//                        return stockKline;
//                    }).collect(Collectors.toList());
//                }
//
//                rtIndicator = getRtIndicator(1, klineQueryReq, type, haveToday);
//            }
//            stockMinQueryReq.setAdjhkt(klineQueryReq.getAdjhkt());
//            stockMinQueryReq.setCode(klineQueryReq.getCode());
//            stockMinQueryReq.setNum(klineQueryReq.getNum());
//            stockMinQueryReq.setTime(klineQueryReq.getDate());
//
//        }
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        long dateTime = Long.parseLong(sdf.format(new Date(klineQueryReq.getDate())));
//        if (klineQueryReq.getType().contains("min")) {
//            List<StockKline> stockKlines = hkStockMinApi.listKline(stockMinQueryReq).stream().map(item -> {
//                StockKline stockKline = new StockKline();
//                BeanUtils.copyProperties(item, stockKline);
//                if (xnhk0102 != null) {
//                    stockKline.setChangeRate(xnhk0102.getF070n() != null ? (item.getVolume().divide(xnhk0102.getF070n(), 6, RoundingMode.UP)).toString() : "");
//                }
//                return stockKline;
//            }).collect(Collectors.toList());
//            stockKlines.addAll(rtList);
//            list = stockKlines;
//            indicators = buildBaseInditor(hkStockMinApi.listTechnicalIndicators(stockMinQueryReq));
//            indicators.addAll(rtIndicator);
//        } else {
//            list = compositeApi.selectKlineList(stockKlineReq).stream().map(item -> {
//                StockKline stockKline = new StockKline();
//                BeanUtils.copyProperties(item, stockKline);
//                if (xnhk0102 != null) {
//                    stockKline.setChangeRate(xnhk0102.getF070n() != null ? (item.getVolume().divide(xnhk0102.getF070n(), 6, RoundingMode.UP)).toString() : "");
//                }
//                return stockKline;
//            }).collect(Collectors.toList());
//            if (today) {
//                switch (klineQueryReq.getType()) {
//                    case OmdcCommonConstant.DAY:
//                        List<StockKline> day = klineBusinessApi.listAllMaDaily(AllMaKlineReq.builder().adjhkt(klineQueryReq.getAdjhkt()).code(klineQueryReq.getCode()).number(1).day(dateTime).mode(OmdcMode.RT).build()).getData().stream().map(item -> {
//                            StockKline stockKline = new StockKline();
//                            BeanUtils.copyProperties(item, stockKline);
//                            return stockKline;
//                        }).collect(Collectors.toList());
//                        list.addAll(day);
//                        break;
//                    case OmdcCommonConstant.MONTH:
//                        List<StockKline> month = klineBusinessApi.listAllMaMonthly(AllMaKlineReq.builder().adjhkt(klineQueryReq.getAdjhkt()).code(klineQueryReq.getCode()).number(1).day(dateTime).mode(OmdcMode.RT).build()).getData().stream().map(item -> {
//                            StockKline stockKline = new StockKline();
//                            BeanUtils.copyProperties(item, stockKline);
//                            return stockKline;
//                        }).collect(Collectors.toList());
//                        list.addAll(month);
//                        break;
//                    case OmdcCommonConstant.WEEK:
//                        List<StockKline> week = klineBusinessApi.listAllMaWeekly(AllMaKlineReq.builder().adjhkt(klineQueryReq.getAdjhkt()).code(klineQueryReq.getCode()).number(1).day(dateTime).mode(OmdcMode.RT).build()).getData().stream().map(item -> {
//                            StockKline stockKline = new StockKline();
//                            BeanUtils.copyProperties(item, stockKline);
//                            return stockKline;
//                        }).collect(Collectors.toList());
//                        list.addAll(week);
//                        break;
//                    case OmdcCommonConstant.YEAR:
//                        List<StockKline> year = klineBusinessApi.listAllMaYearly(AllMaKlineReq.builder().adjhkt(klineQueryReq.getAdjhkt()).code(klineQueryReq.getCode()).number(1).day(dateTime).mode(OmdcMode.RT).build()).getData().stream().map(item -> {
//                            StockKline stockKline = new StockKline();
//                            BeanUtils.copyProperties(item, stockKline);
//                            return stockKline;
//                        }).collect(Collectors.toList());
//                        list.addAll(year);
//                        break;
//                    default:
//                        break;
//                }
//            }
//            indicators = buildBaseInditor(compositeApi.selectIndicatorList(stockKlineReq));
//        }
//
//        Map<Long, StockKline> collect = list.stream().collect(Collectors.toMap(StockKline::getTime, Function.identity(), (stockKline, stockKline2) -> stockKline));
//        list = Lists.newArrayList(collect.values());
//        list.sort(Comparator.comparing(StockKline::getTime).reversed());
//        if (indicators != null) {
//            Map<Long, BaseStockIndicator> indicatorsCollect = indicators.stream().collect(Collectors.toMap(BaseStockIndicator::getDate, Function.identity(), (V1, V2) -> V2));
//            indicators = Lists.newArrayList(indicatorsCollect.values());
//            indicators.sort(Comparator.comparing(BaseStockIndicator::getDate).reversed());
//        }
//        StockKlineDTO stockKlineDTO = new StockKlineDTO();
//        stockKlineDTO.setKlines(list);
//        stockKlineDTO.setIndicatorList(indicators);
//        return stockKlineDTO;
//    }

//    private List<BaseStockIndicator> buildBaseInditor(List<TechnicalIndicatorsEntity> listTechnicalIndicators) {
//        if (listTechnicalIndicators == null || listTechnicalIndicators.size() < 1) {
//            return Collections.emptyList();
//        }
//        return listTechnicalIndicators.stream().map(item -> {
//            BaseStockIndicator baseStockIndicator = new BaseStockIndicator();
//            BeanUtils.copyProperties(item, baseStockIndicator);
//            baseStockIndicator.setDate(item.getTime());
//            if (item.getBias() != null) {
//                BeanUtils.copyProperties(item.getBias(), baseStockIndicator);
//            }
//            if (item.getBoll() != null) {
//                BeanUtils.copyProperties(item.getBoll(), baseStockIndicator);
//            }
//            if (item.getCci() != null) {
//                BeanUtils.copyProperties(item.getCci(), baseStockIndicator);
//            }
//            if (item.getCdp() != null) {
//                BeanUtils.copyProperties(item.getCdp(), baseStockIndicator);
//            }
//            if (item.getDma() != null) {
//                BeanUtils.copyProperties(item.getDma(), baseStockIndicator);
//            }
//            if (item.getDmi() != null) {
//                BeanUtils.copyProperties(item.getDmi(), baseStockIndicator);
//            }
//            if (item.getEma() != null) {
//                BeanUtils.copyProperties(item.getEma(), baseStockIndicator);
//            }
//            if (item.getKdj() != null) {
//                BeanUtils.copyProperties(item.getKdj(), baseStockIndicator);
//            }
//            if (item.getMa() != null) {
//                BeanUtils.copyProperties(item.getMa(), baseStockIndicator);
//            }
//            if (item.getMacd() != null) {
//                BeanUtils.copyProperties(item.getMacd(), baseStockIndicator);
//            }
//            if (item.getMavol() != null) {
//                BeanUtils.copyProperties(item.getMavol(), baseStockIndicator);
//            }
//            if (item.getObv() != null) {
//                BeanUtils.copyProperties(item.getObv(), baseStockIndicator);
//            }
//            if (item.getOsc() != null) {
//                BeanUtils.copyProperties(item.getOsc(), baseStockIndicator);
//            }
//            if (item.getPsy() != null) {
//                BeanUtils.copyProperties(item.getPsy(), baseStockIndicator);
//            }
//            if (item.getRoc() != null) {
//                BeanUtils.copyProperties(item.getRoc(), baseStockIndicator);
//            }
//            if (item.getRsi() != null) {
//                BeanUtils.copyProperties(item.getRsi(), baseStockIndicator);
//            }
//            if (item.getSar() != null) {
//                BeanUtils.copyProperties(item.getSar(), baseStockIndicator);
//            }
//            if (item.getVroc() != null) {
//                BeanUtils.copyProperties(item.getVroc(), baseStockIndicator);
//            }
//            if (item.getWr() != null) {
//                BeanUtils.copyProperties(item.getWr(), baseStockIndicator);
//            }
//            return baseStockIndicator;
//        }).collect(Collectors.toList());
//
//    }

//    @Override
//    public StockRtKlineDTO getRtStockKline(String stockCode, String type) throws ExecutionException, InterruptedException {
//        Map<String, List<RtStockKline>> list = new TreeMap<>();
//        Map<String, List<BaseStockIndicator>> indicator = new TreeMap<>();
//        List<Future<List<BaseStockIndicator>>> futures = new LinkedList<>();
//        int size = 1;
//        switch (type) {
//            case "fiveDay":
//                list = buildBaseStockDetailByTrend(klineBusinessApi.trendFive(TrendReq.builder().symbol(stockCode).build()).getData(), 5);
//                break;
//            case OmdcMode.RT:
//                list = buildBaseStockDetailByTrend(klineBusinessApi.trend(TrendReq.builder().symbol(stockCode).build()).getData(), 1);
//                break;
//            default:
//                break;
//        }
//        list.keySet().forEach(item -> {
//            Future<List<BaseStockIndicator>> submit = KlineExecutorService.submit(new rtIndicator(stockCode, item));
//            futures.add(submit);
//        });
//        ArrayList<String> strings = new ArrayList<>(list.keySet());
//        for (int i = 0; i < futures.size(); i++) {
//            Future<List<BaseStockIndicator>> future = futures.get(i);
//            indicator.put(strings.get(i), future.get());
//        }
//        StockRtKlineDTO stockRtKlineDTO = new StockRtKlineDTO();
//        List<Map<String, List<RtStockKline>>> maps = CollectUtils.splitMap(list, size);
//        List<Map<String, List<BaseStockIndicator>>> indicatorList = CollectUtils.splitMap(indicator, size);
//        stockRtKlineDTO.setKlines(maps);
//        stockRtKlineDTO.setIndicatorList(indicatorList);
//        StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(stockCode));
//        if (snapshot != null) {
//            stockRtKlineDTO.setPreClose(snapshot.getPreClose().toString());
//            stockRtKlineDTO.setOpen(snapshot.getOpen().toString());
//        }
//        return stockRtKlineDTO;
//    }


    class rtIndicator implements Callable<List<BaseStockIndicator>> {

        private String code;
        private String date;

        public rtIndicator(String code, String date) {
            this.code = code;
            this.date = date;
        }

        @Override
        public List<BaseStockIndicator> call() {
            MinkTargetReq minkTargetReq = new MinkTargetReq();
            minkTargetReq.setCode(code);
            minkTargetReq.setDate(date);
            minkTargetReq.setType(1);
            return targetBusinessApi.stockMniTarget(minkTargetReq).getData().stream().map(temp -> {
                BaseStockIndicator baseStockIndicator = new BaseStockIndicator();
                BeanUtils.copyProperties(temp, baseStockIndicator);
                return baseStockIndicator;
            }).collect(Collectors.toList());
        }
    }


    private Map<String, List<RtStockKline>> buildBaseStockDetailByTrend(List<TrendResp> data, int num) {
        Map<String, List<RtStockKline>> list = new TreeMap<>();
        if (data.size() < 1) {
            return list;
        }
        int begin;
        int end = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<RtStockKline> collect = data.stream().map(item -> {
            RtStockKline stockKline = new RtStockKline();
            try {
                stockKline.setTime(simpleDateFormat.parse(item.getTime()).getTime());
            } catch (ParseException e) {
                log.error("parse time error", e);
            }
            stockKline.setVolume(item.getVolume());
            stockKline.setPrice(item.getPrice().toString());
            stockKline.setChg(item.getChange());
            stockKline.setChgPct(item.getChangeRate());
            stockKline.setAvg_price(item.getAvgPrice().toString());
            stockKline.setAmount(item.getAmount());
            return stockKline;
        }).collect(Collectors.toList());
        int count = collect.size() % 331 == 0 ? collect.size() / 331 == 0 ? 1 : collect.size() / 331 : (collect.size() / 331) + 1;
        boolean hf = false;
        Date now = new Date();
        for (int i = 0; i < count; i++) {
            begin = end;
            int temp = i + 1;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            RtStockKline rtStockKline = collect.get(begin);
            String date = sdf.format(new Date(rtStockKline.getTime()));
            String nowStr = sdf.format(now);
            if (halfDate.contains(date)) {
                end += 151;
                hf = true;
            } else {
                end += 331;
            }
            List<RtStockKline> klines = new LinkedList<>();
            if (temp == count) {
                klines.addAll(collect.subList(begin, data.size()));
            } else {
                klines.addAll(collect.subList(begin, end));
            }
            // 上半日交易进行数据补点（五日分时图）
            if (hf && !nowStr.equals(date)) {
                RtStockKline rtStockKline1 = collect.get(end - 1);
                rtStockKline1.setVolume(new BigDecimal("0"));
                long time = rtStockKline1.getTime() + 60 * 60 * 1000;
                for (int j = 0; j < 180; j++) {
                    time += 60 * 1000;
                    RtStockKline demo = new RtStockKline();
                    BeanUtils.copyProperties(rtStockKline1, demo);
                    demo.setTime(time);
                    klines.add(demo);
                }
            }
            list.put(date, klines);
            hf = false;
        }
        return list;
    }


    private boolean isToday(Long date) {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        long time = instance.getTime().getTime();
        return date > time;
    }


    private List<BaseStockIndicator> getRtIndicator(int i, KlineQueryReq klineQueryReq, String type, Boolean flag) {
        if (flag) {
            --i;
        }
        MinkTargetReq minkTargetReq = new MinkTargetReq();
        minkTargetReq.setCode(klineQueryReq.getCode());
        minkTargetReq.setDate(DateUtils.formatDate(new Date(klineQueryReq.getDate())));
        minkTargetReq.setType(Integer.valueOf(type));
        return targetBusinessApi.stockMniTarget(minkTargetReq).getData().stream().map(item -> {
            BaseStockIndicator baseStockIndicator = new BaseStockIndicator();
            BeanUtils.copyProperties(item, baseStockIndicator);
            return baseStockIndicator;
        }).collect(Collectors.toList());
    }

    private int getCount(KlineQueryReq klineQueryReq) {
        Calendar instance = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        Date date = new Date(klineQueryReq.getDate());
        String format = sdf.format(date);
        if (instance.get(Calendar.DAY_OF_WEEK) <= 7 && instance.get(Calendar.DAY_OF_WEEK) > 5) {
            return 0;
        }
        if (format.startsWith("0")) {
            format = format.substring(1);
        }
        int aLong = Integer.parseInt(format);
        if (aLong <= 160000 && aLong >= 93000) {
            instance.setTime(date);
            instance.set(Calendar.HOUR_OF_DAY, 9);
            instance.set(Calendar.MINUTE, 30);
            instance.set(Calendar.SECOND, 0);
            Date time = instance.getTime();
            long diff = (date.getTime() - time.getTime()) / 60 / 1000;
            return (int) diff + 1;
        } else if (aLong > 160000 && aLong <= 235959) {
            return 332;
        }
        return 0;
    }

//    @Override
//    public List<HkCapitalFlowResp> getCapitalFlow(String stockCode) {
//
//        List<HkCapitalFlowResp> data = stockBusinessApi.getCapitalFlow(FiuBaseReq.builder().code(stockCode).build()).getData();
//        if (CollUtil.isEmpty(data)) {
//            return Lists.newArrayList();
//        }
//        return data;
//    }


    @Override
    public OrderBrokerDto getEconomy(String code, String type) {
        //经纪席位查不到的时候从数据库里面查
        OrderBrokerDto dto = new OrderBrokerDto();
        if (TradeDirectionConstants.OUT.equals(type)) {
            dto = redisClient.get(RedisKeyConstants.RECEIVER_ORDER_BROKER_SELL.concat(code));
//            if (dto == null) {
//                StockRelatedDetails details = stockRelatedDetailsService.getOne(new QueryWrapper<StockRelatedDetails>().eq("code", code));
//                if (details != null && !StringUtils.isEmpty(details.getOrderBrokerSellDetails())) {
//                    dto = JSON.parseObject(details.getOrderBrokerSellDetails(), OrderBrokerDto.class);
//                    redisClient.set(RedisKeyConstants.RECEIVER_ORDER_BROKER_SELL.concat(code), dto);
//                }
//            }
        }
        if (TradeDirectionConstants.IN.equals(type)) {
            dto = redisClient.get(RedisKeyConstants.RECEIVER_ORDER_BROKER_BUY.concat(code));
//            if (dto == null) {
//                StockRelatedDetails details = stockRelatedDetailsService.getOne(new QueryWrapper<StockRelatedDetails>().eq("code", code));
//                if (details != null && !StringUtils.isEmpty(details.getOrderBrokerBuyDetails())) {
//                    dto = JSON.parseObject(details.getOrderBrokerBuyDetails(), OrderBrokerDto.class);
//                    redisClient.set(RedisKeyConstants.RECEIVER_ORDER_BROKER_BUY.concat(code), dto);
//                }
//            }
        }
        return dto;
    }

    @Override
    public IndhktryResp getIndhktry(String code) {
        IndhktryReq indhktryReq = new IndhktryReq();
        indhktryReq.setCode(code);
        indhktryReq.setKind("up");
        indhktryReq.setNumber(30);
        indhktryReq.setMode("rt");
        ResultT<IndhktryResp> indhktry = stockBusinessApi.indhktryV3(indhktryReq);
        if (indhktry.getCode() == ResultT.success().getCode()) {
            return indhktry.getData();
        }
        return new IndhktryResp();
    }

    @Override
    public List<StockQueryDTO> queryStockByHistory(List<String> stockHistory) {
        Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
        return stockCache.queryStockInfoList(stockHistory).stream().map(stockInfo -> {
            StockQueryDTO stockQueryDTO = new StockQueryDTO();
            BeanUtils.copyProperties(stockInfo, stockQueryDTO);
            String stockName = stockNameMap.get(stockInfo.getCode());
            stockQueryDTO.setName(stockName);
            stockQueryDTO.setStockName(stockName);
            return stockQueryDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ComSearchStockDto> queryStockByHistoryV2(List<String> stockHistory) {
        Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
        return CollUtil.isEmpty(stockHistory) ? ListUtil.empty() : stockCache.queryStockInfoList(stockHistory).stream().map(stockInfo -> {
            ComSearchStockDto stockDto = new ComSearchStockDto();
            BeanUtils.copyProperties(stockInfo, stockDto);
            String stockName = stockNameMap.get(stockInfo.getCode());
            stockDto.setName(stockName);
            stockDto.setStockName(stockName);
            stockDto.setFinanceType(stockInfo.getType());
            stockDto.setRegionType(RegionTypeEnum.HK.getCode());
            stockDto.setExchange(ExchangeEnum.HK_EX.getCode());
            return stockDto;
        }).collect(Collectors.toList());
    }

    @Override
    public PageDomain<StockQueryDTO> queryStockByPage(String key, Long current, Long size) {
        List<StockQueryDTO> dtoList = stockCache.getStockSimpleInfoAll(key).stream().map(stockInfo -> {
            StockQueryDTO stockQueryDTO = new StockQueryDTO();
            BeanUtils.copyProperties(stockInfo, stockQueryDTO);
            return stockQueryDTO;
        }).collect(Collectors.toList());
        List<StockQueryDTO> dto = dtoList.stream().filter(t->t.getType()==0).sorted((t1, t2) -> {
            String code1 = t1.getCode();
            String stockCode1 = code1.substring(0, code1.indexOf("."));
            Integer codeNum1 = Integer.valueOf(stockCode1);

            String code2 = t2.getCode();
            String stockCode2 = code2.substring(0, code2.indexOf("."));
            Integer codeNum2 = Integer.valueOf(stockCode2);
            return   codeNum1 - codeNum2;
        }).skip((current-1)*size).limit(size).collect(Collectors.toList());
        PageDomain<StockQueryDTO> pageDomain = new PageDomain<>();
        pageDomain.setRecords(dto);
        pageDomain.setTotal(dtoList.size());
        pageDomain.setCurrent(current);
        pageDomain.setSize(dto.size());
        return pageDomain;
    }

    @Override
    public List<StockQueryDTO> queryStockSort(String key) {
        List<StockQueryDTO> dtoList = stockCache.getStockSimpleInfoAll(key).stream().map(stockInfo -> {
            StockQueryDTO stockQueryDTO = new StockQueryDTO();
            BeanUtils.copyProperties(stockInfo, stockQueryDTO);
            return stockQueryDTO;
        }).collect(Collectors.toList());

        List<StockQueryDTO> dto = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dtoList)){
            dto = dtoList.stream().filter(t->t.getType()==0).sorted((t1, t2) -> {
                String code1 = t1.getCode();
                String stockCode1 = code1.substring(0, code1.indexOf("."));
                Integer codeNum1 = Integer.valueOf(stockCode1);

                String code2 = t2.getCode();
                String stockCode2 = code2.substring(0, code2.indexOf("."));
                Integer codeNum2 = Integer.valueOf(stockCode2);
                return   codeNum1 - codeNum2;
            }).collect(Collectors.toList());
        }

        return dto;
    }


    public List<ComStockBaseInfoDTO> gatIndexByType(Integer type, String sort, String sortKey) {
        //HSI = '0000100', // 恒生指数
        // HSCEI = '0001400', // 国企
        //HSCCI = '0001500', // 红筹
        List<StockBaseDTO> stockBaseDTOS = new ArrayList<>();
        if(type == 14){
            stockBaseDTOS = indexService.getIndexComponent("0000100",0,sort,sortKey);
        }else if(type == 15){
            stockBaseDTOS = indexService.getIndexComponent("0001400",0,sort,sortKey);
        }else if(type == 16){
            stockBaseDTOS = indexService.getIndexComponent("0001500",0,sort,sortKey);
        }
        List<ComStockBaseInfoDTO> comStockBaseDTOS =new ArrayList<>();
        List<String> stockCodes = CollUtil.map(stockBaseDTOS, StockBaseDTO::getCode, true);
        Map<String, Long> stockCodeIdMap = stockInfoApi.selectStockIdByCodes(stockCodes);

        if (CollectionUtils.isNotEmpty(stockBaseDTOS)) {
                comStockBaseDTOS = stockBaseDTOS.stream().map(o->{
                    ComStockBaseInfoDTO comStockBaseDTO = new ComStockBaseInfoDTO();
                    comStockBaseDTO.setStockType(o.getStockType());
                    comStockBaseDTO.setStockName(o.getName());
                    comStockBaseDTO.setStockCode(o.getCode());
                    comStockBaseDTO.setRegionType(o.getRegionType());
                    comStockBaseDTO.setStockId(stockCodeIdMap.get(o.getCode()));
                    return comStockBaseDTO;
                }).collect(Collectors.toList());
        }
        return comStockBaseDTOS;
    }

    @Override
    public List<ComStockBaseInfoDTO> gatStockByType(Integer type, String sort, String sortKey) {
        List<ComStockBaseInfoDTO> comStockBaseDTOS = new ArrayList<>();
        if (type == 12) {
            Map<String, ComStockSimpleDto> stockSimpleDtoMap = stockCache.queryStockInfoList(null).stream().filter(o -> o.getStockType().equals(type)).collect(Collectors.toMap(o -> o.getCode(), o -> o, (k1, k2) -> k1));
            if (MapUtil.isNotEmpty(stockSimpleDtoMap)) {
                comStockBaseDTOS = getSnapshotListSort(stockSimpleDtoMap.keySet(), sortKey, sort).stream().map(o -> {
                    ComStockBaseInfoDTO comStockBaseDTO = new ComStockBaseInfoDTO();
                    BeanUtil.copyProperties(o, comStockBaseDTO);
                    comStockBaseDTO.setStockName(stockSimpleDtoMap.get(o.getCode()).getName());
                    comStockBaseDTO.setStockCode(o.getCode());
                    return comStockBaseDTO;
                }).collect(Collectors.toList());
            }
            return comStockBaseDTOS;
        }else{
            return gatIndexByType(type,sort,sortKey);
        }
    }

    /**
     * 当缓存未空的时候直接查询数据库并补全数据
     *
     * @param code         股票代码
     * @param snapshotList 行情快照
     */
    private void buildStockSnapshot(String code, List<StockSnapshot> snapshotList) {
        StockRelatedDetails details = stockRelatedDetailsService.getOne(new QueryWrapper<StockRelatedDetails>()
                .eq("code", code));
        if (details != null && details.getType()==0) {
            StockSnapshot snapshot = JSON.parseObject(details.getSnapshotDetails(), StockSnapshot.class);
            snapshotList.add(snapshot);
        }
    }

    /**
     * 通过K线反补快照信息
     *
     * @param code         股票代码
     * @param snapshotList 快照列表
     */
//    private void buildSnapshotByKline(String code, List<StockSnapshot> snapshotList) {
//        //获取最新的日K数据
//        StockKlineReq klineReq = new StockKlineReq();
//        klineReq.setType(OmdcCommonConstant.DAY);
//        klineReq.setTime(System.currentTimeMillis());
//        klineReq.setNum(1);
//        klineReq.setAdjhkt("not");
//        klineReq.setCode(code);
//        List<com.vv.finance.common.calc.hk.entity.StockKline> klineList =
//                compositeApi.selectStockKlineList(klineReq);
//        if (CollectionUtils.isNotEmpty(klineList)) {
//            StockDefine stockDefine = stockDefineMapper.selectOne(new QueryWrapper<StockDefine>().eq("code", code));
//            String stockName = stockDefine == null ? null : stockDefine.getShortname();
//            com.vv.finance.common.calc.hk.entity.StockKline stockKline = klineList.get(0);
//            StockSnapshot snapshot = new StockSnapshot();
//            snapshot.setLotSize(stockDefine == null ? null : stockDefine.getLotsize());
//            snapshot.setCode(stockKline.getCode());
//            snapshot.setName(stockName);
//            snapshot.setOpen(stockKline.getOpen());
//            snapshot.setClose(stockKline.getClose());
//            snapshot.setHigh(stockKline.getHigh());
//            snapshot.setLow(stockKline.getLow());
//            snapshot.setPreClose(stockKline.getPreClose());
//            snapshot.setChg(stockKline.getChg());
//            snapshot.setChgPct(stockKline.getChgPct());
//            //收盘价补充最新价
//            snapshot.setLast(stockKline.getClose());
//            snapshot.setType(0);
//            snapshot.setTime(stockKline.getTime());
////            redisClient.set(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(code), snapshot);
////            redisClient.hset(RedisKeyConstants.COMPRESS_STOCK_MAP, code, ZipUtil.gzip(JSON.toJSONString(snapshot)));
//            snapshotList.add(snapshot);
//        }
//    }

    @Override
    public List<StockSnapshot> getStockSnapshotByBlockCode(String blockCode) {
        log.info("getStockSnapshotByBlockCode start, blockCode [{}]", blockCode);
        List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockInfoList(ListUtil.of(blockCode));
        if (CollUtil.isEmpty(simpleDtoList)) {
            return Collections.emptyList();
        }
        List<String> codeList = new ArrayList<>();
        ComStockSimpleDto simpleDto = CollUtil.getFirst(simpleDtoList);
        if (ObjectUtil.equal(StockTypeEnum.INDEX.getCode(), simpleDto.getStockType())) {
            List<Xnhk1002> xnhk1002s = xnhk1002Mapper.listMembersAndIndex("(".concat(blockCode).concat(")"));
            codeList = CollUtil.map(xnhk1002s, Xnhk1002::getF001v, true);
        } else if (ObjectUtil.equal(StockTypeEnum.HY.getCode(), simpleDto.getStockType())) {
            // 行业成分股过滤掉状态为1和4的股票
            List<Integer> suspensions = CollUtil.map(ListUtil.of(StockSecurityStatusEnum.NORMAL, StockSecurityStatusEnum.FUSING, StockSecurityStatusEnum.STOP), StockSecurityStatusEnum::getCode, true);
            List<StockDefine> hyDefines = stockDefineMapper.selectList(Wrappers.lambdaQuery(StockDefine.class).eq(StockDefine::getIndustryCode, blockCode)
                    .in(StockDefine::getSuspension, suspensions).eq(StockDefine::getStockType, StockTypeEnum.STOCK.getCode()));
            codeList = CollUtil.map(hyDefines, StockDefine::getCode, true);
        }

        List<String> keys = codeList.parallelStream().map(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN::concat).collect(Collectors.toList());
        List<StockSnapshot> objects = redisClient.batchGet(keys);
        log.info("getStockSnapshotByBlockCode end, blockCode [{}], size [{}]", blockCode, CollUtil.size(objects));
        return CollUtil.defaultIfEmpty(objects, Collections.emptyList());
    }

    /**
     * 删除临时股票快照数据
     *
     * @param stockCode
     */
    @Override
    public void delSnapshotByStockCode(String stockCode) {
        log.info("删除临时股票快照数据开始：stockCode：{} ",stockCode);
        try {
            redisClient.del(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(stockCode));
            redisClient.hdel(RedisKeyConstants.COMPRESS_STOCK_MAP.concat(stockCode), stockCode);

            Set<String> todayCodeSet = redisClient.get(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_SET);
            if (CollUtil.isNotEmpty(todayCodeSet)) {
                todayCodeSet.remove(stockCode);
                redisClient.set(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_SET, todayCodeSet);
            }

            Set<String> codeNameSet = redisClient.get(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_NAME_SET);
            if (CollUtil.isNotEmpty(codeNameSet)) {
                codeNameSet = CollUtil.filter(codeNameSet, cn -> !StrUtil.containsIgnoreCase(cn, stockCode));
                redisClient.set(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_NAME_SET, codeNameSet);
            }
        } catch (Exception e) {
            log.info("删除临时股票快照数据code：stockCode：{} 异常",stockCode,e);
        }
    }

    /**
     * 变更快照数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    @Override
    public void updateSnapshotStockCode(String sourceCode, String targetCode) {
        copySnapshotStockCode(sourceCode, targetCode);
        delSnapshotByStockCode(sourceCode);
    }

    /**
     * 变更快照数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    @Override
    public void copySnapshotStockCode(String sourceCode, String targetCode) {
        Long stockId = hkStockRelationMapper.selectStockIdByInnerCode(targetCode);
        try {
            log.info("变更快照数据开始：sourceCode：{} targetCode：{}", sourceCode, targetCode);
            HKIndexQuoteResp hkIndexQuoteResp = redisClient.get(RedisKeyConstants.BG_RJH_BLOCK_SNAPSHOT_BEAN.concat(sourceCode));
            if (ObjectUtils.isNotEmpty(hkIndexQuoteResp)) {
                hkIndexQuoteResp.setSymbol(targetCode);
                redisClient.set(RedisKeyConstants.BG_RJH_BLOCK_SNAPSHOT_BEAN.concat(targetCode), hkIndexQuoteResp);
            }


            StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(sourceCode));
            if (ObjectUtils.isNotEmpty(snapshot)) {
                snapshot.setCode(targetCode);
                snapshot.setStockId(stockId);
                redisClient.set(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(snapshot.getCode()), snapshot);
                redisClient.hset(RedisKeyConstants.COMPRESS_STOCK_MAP, snapshot.getCode(), ZipUtil.gzip(JSON.toJSONString(snapshot)));
            }

            Set<String> todayCodeSet = redisClient.get(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_SET);
            if (CollUtil.isNotEmpty(todayCodeSet)) {
                todayCodeSet.add(targetCode);
                redisClient.set(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_SET, todayCodeSet);
            }

            Set<String> codeNameSet = redisClient.get(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_NAME_SET);
            if (CollUtil.isNotEmpty(codeNameSet)) {
                Map<String, String> stockNameMap = stockCache.queryStockNameMap(ListUtil.of(targetCode));
                codeNameSet.add(targetCode.concat(StrUtil.COMMA).concat(stockNameMap.get(targetCode)));
                redisClient.set(RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_NAME_SET, codeNameSet);
            }
        } catch (Exception e) {
            log.info("变更快照股票code：sourceCode：{} targetCode：{} 异常",sourceCode,targetCode,e);
        }
    }


    /**
     * 获取所有股票的成交量
     *
     */
    @Override
    public ResultT<Map<String, BigDecimal>> queryStockVol() {
        Map<String, ComStockSimpleDto> stockInfoMap = stockCache.queryLocalInfoMap(null);
        List<String> codes = stockInfoMap.values().stream().filter(item -> StockTypeEnum.STOCK.getCode().equals(item.getStockType())
                || StockTypeEnum.INDEX.getCode().equals(item.getStockType())).map(ComStockSimpleDto::getCode).collect(Collectors.toList());
        List<StockSnapshot> snapshotList = getHkSnapshotList(codes ,stockInfoMap);
        Map<String, BigDecimal> stockVolMap = snapshotList.stream().collect(Collectors.toMap(StockSnapshot::getCode, v -> ObjectUtils.isEmpty(v.getSharesTraded()) ? BigDecimal.ZERO : v.getSharesTraded(), (o, v) -> v));
        log.info("总共查询到{}只股票的成交量数据",stockVolMap.size());
        return ResultT.success(stockVolMap);
    }

    /**
     * 获取所有股票的 9.40之前的成交笔数
     *
     */
    @Override
    public ResultT<Map<String, Integer>> queryTradeNum() {
        Map<String, Integer> hmget = redisClient.hmget(RedisKeyConstants.HK_STOCK_TRADE_NUM);
        if (CollectionUtils.isEmptyMap(hmget)) {
            log.info("HK_STOCK_TRADE_NUM is empty!");
        }
        log.info("总共查询到{}只股票的成交量数据",hmget.size());
        return ResultT.success(hmget);
    }
}
