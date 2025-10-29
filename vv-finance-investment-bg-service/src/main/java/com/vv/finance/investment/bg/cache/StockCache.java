package com.vv.finance.investment.bg.cache;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.enums.ExchangeEnum;
import com.vv.finance.common.enums.StockSecurityStatusEnum;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.common.utils.MyStringUtil;
import com.vv.finance.common.utils.ZipUtil;
import com.vv.finance.investment.bg.api.broker.BrokerAnalysisApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.uts.resp.ReuseTempDTO;
import com.vv.finance.investment.bg.dto.uts.resp.StockRightsDTO;
import com.vv.finance.investment.bg.mapper.index.TIndexInfoMapper;
import com.vv.finance.investment.bg.stock.info.HkStockRelation;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.service.*;
import com.vv.finance.investment.bg.util.ConvertUtil;
import com.vv.finance.investment.bg.util.StackUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/4/21 15:27
 */
@Slf4j
@Component
// @RequiredArgsConstructor
public class StockCache {

    private static volatile int CACHE_SIZE = 0;

    private static final Map<String, ComStockSimpleDto> STOCK_INFO_MAP = new ConcurrentHashMap<>(4000);

    @Value("#{'${hk.query.stock}'.toLowerCase().split(',')}")
    private List<String> hotStock;

    @Value("${stock.query.all.size:100}")
    private Integer queryAllSize;

    @Resource
    RedisClient redisClient;

    @Resource
    private UtsInfoService utsInfoService;

    @Value("${index.new.code:{\"0000100\":\"HSI\",\"0001400\":\"HSCEI\",\"0001500\":\"HSCCI\",\"0000101\":\"HSF\",\"0000102\":\"HSU\",\"0000103\":\"HSP\",\"0000104\":\"HSC\",\"0208300\":\"HTI\",\"0105000\":\"HVSI\"}}")
    private String indexNewCode;

    @Value("${index.code.name:{\"0000100\":\"恒生指数\", \"0001400\":\"国企指数\", \"0001500\":\"红筹指数\"}}")
    private String indexCodeName;

    private static final String STOCK_CODE_KEY = "stockCodeKey";
    private static final String HY_CODE_CACHE = "HY_CODE_CACHE";

    @Resource
    private IStockDefineService stockDefineService;

    @Resource
    private HkStockRelationService hkStockRelationService;
    @Resource
    BrokerAnalysisApi brokerAnalysisApi;

    /**
     * 初始化股票缓存
     */
    @PostConstruct
    private void initStockCacheCodes() {
        log.info("=========初始化股票缓存========== 开始");
        // List<ComStockSimpleDto> stockAll = getStockAll(STOCK_CODE_KEY);
        List<ComStockSimpleDto> stockAll = this.getStockByCodes(null);
        // stockCacheCodes.clear();
        // stockCacheCodes.put(STOCK_CODE_KEY,stockAll);
        saveAllStockDtoToRedis(stockAll);
        log.info("=========初始化股票缓存========== 结束 :{}",STOCK_CODE_KEY);
        brokerAnalysisApi.updateBrokerInfoJob();

        //缓存股票code 跟名称映射关系
        // refreshNameStockMap();
        //加载拼音缩写
        // setUpPinYin();
    }

    /**
     * 定时更新股票缓存 每20分支执行一次
     */
    @XxlJob(value = "upStockCacheCodes", cron = "0 0/20 * * * ? ", author = "杨鹏", desc = "刷新股票缓存")
    public ReturnT<String> upStockCacheCodes(String param) {
        log.info("=========刷新股票缓存========== 开始");
        initStockCacheCodes();
        log.info("=========刷新股票缓存========== 结束");
        return ReturnT.SUCCESS;
    }

    //更新缓存
    public void updateStockSimpleInfo() {
//        stockCodes.refresh(STOCK_CODE_KEY);
        log.info("=========更新股票缓存========== 开始");
        initStockCacheCodes();
        log.info("=========更新股票缓存========== 结束");
    }

    public List<ComStockSimpleDto> getStockSimpleInfo(String key) {
//        List<ComStockSimpleDto> stockSimpleInfos = stockCodes.get(STOCK_CODE_KEY);
        List<ComStockSimpleDto> stockSimpleInfos = queryStockInfoList(null);
        if (key == null) {
            List<String> collect = hotStock.stream().map(item -> item.split("/")[0]).collect(Collectors.toList());
            return stockSimpleInfos.stream().filter(item -> collect.contains(item.getCode())).collect(Collectors.toList());
        }
        String lowerKey = key.toLowerCase();
        List<String> collect = hotStock.stream().filter(item -> item.contains(lowerKey)).map(item -> item.split("/")[0]).collect(Collectors.toList());
        List<ComStockSimpleDto> hotStockSimpleInfo = stockSimpleInfos.stream().filter(stockSimpleInfo -> collect.contains(stockSimpleInfo.getCode())).limit(10).collect(Collectors.toList());
        // 新增简写和自定义名称的搜索
        List<ComStockSimpleDto> simpleInfos = stockSimpleInfos.stream().filter(stockSimpleInfo ->
                !collect.contains(stockSimpleInfo.getCode())
                        && (stockSimpleInfo.getCode().contains(lowerKey)
                        || (StringUtils.isNotBlank(stockSimpleInfo.getName()) && stockSimpleInfo.getName().toLowerCase().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getShortname()) && stockSimpleInfo.getShortname().toLowerCase().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getStockName()) && stockSimpleInfo.getStockName().toLowerCase().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getNameInitials()) && stockSimpleInfo.getNameInitials().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getNamePinyin()) && stockSimpleInfo.getNamePinyin().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getStockNameInitials()) && stockSimpleInfo.getStockNameInitials().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getStockNamePinyin()) && stockSimpleInfo.getStockNamePinyin().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getShortNameInitials()) && stockSimpleInfo.getShortNameInitials().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getShortNamePinyin()) && stockSimpleInfo.getShortNamePinyin().contains(lowerKey))))
                .limit(10 - hotStockSimpleInfo.size()).collect(Collectors.toList()
        );
        hotStockSimpleInfo.addAll(simpleInfos);
        return hotStockSimpleInfo;
    }

    public List<ComStockSimpleDto> getStockSimpleInfoAll(String key) {
//        List<ComStockSimpleDto> stockSimpleInfos = stockCodes.get(STOCK_CODE_KEY);
        List<ComStockSimpleDto> stockSimpleInfos = queryStockInfoList(null);
        String lowerKey = key.toLowerCase();

        // 新增简写和自定义名称的搜索
        List<ComStockSimpleDto> simpleInfos = stockSimpleInfos.stream().filter(stockSimpleInfo ->
                stockSimpleInfo.getType() == 0 && (stockSimpleInfo.getCode().contains(lowerKey)
                        || (StringUtils.isNotBlank(stockSimpleInfo.getName()) && stockSimpleInfo.getName().toLowerCase().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getShortname()) && stockSimpleInfo.getShortname().toLowerCase().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getStockName()) && stockSimpleInfo.getStockName().toLowerCase().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getNameInitials()) && stockSimpleInfo.getNameInitials().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getNamePinyin()) && stockSimpleInfo.getNamePinyin().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getStockNameInitials()) && stockSimpleInfo.getStockNameInitials().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getStockNamePinyin()) && stockSimpleInfo.getStockNamePinyin().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getShortNameInitials()) && stockSimpleInfo.getShortNameInitials().contains(lowerKey))
                        || (StringUtils.isNotBlank(stockSimpleInfo.getShortNamePinyin()) && stockSimpleInfo.getShortNamePinyin().contains(lowerKey))))
//                .limit(10 - hotStockSimpleInfo.size())
                .collect(Collectors.toList()
        );
        return simpleInfos;
    }

//     /**
//      * 获取所有股票
//      *
//      * @param key
//      * @return
//      */
//     private List<ComStockSimpleDto> getStockAll(String key) {
//         if (!STOCK_CODE_KEY.equals(key)) {
//             return Lists.newArrayList();
//         }
//         List<ComStockSimpleDto> stockSimpleInfos = getStockByCodes(null);
//         // 指数
//         // stockSimpleInfos.addAll(buildIndex());
// //        stockSimpleInfos.addAll(buildWarrant());
//         return stockSimpleInfos;
//     }

    private List<ComStockSimpleDto> getStockByCodes(List<String> codes) {
        TimeInterval timeInterval = new TimeInterval();
        // 正股 + 行业，确认只展示正股+行业
        List<StockDefine> stockDefineList = stockDefineService.list(Wrappers.<StockDefine>lambdaQuery()
                .in(StockDefine::getStockType, ListUtil.of(StockTypeEnum.STOCK.getCode(), StockTypeEnum.INDEX.getCode(), StockTypeEnum.HY.getCode()))
                .in(CollUtil.isNotEmpty(codes), StockDefine::getCode, codes)
                .ne(StockDefine::getSuspension, StockSecurityStatusEnum.UNLISTED.getCode())
                .orderByAsc(StockDefine::getCode));

        // 查询股票关联关系
        Map<String, Long> stockCodeIdMap = hkStockRelationService.selectStockIdByCodes(null);
        //权限股票
        List<StockRightsDTO> allStockRights = utsInfoService.getAllStockRightsAndMock();
        long todayTime = DateUtils.getOfDayFirst(new Date()).getTime();
        Map<Boolean, Set<String>> StockRightsDTOMap = allStockRights.stream().collect(Collectors.groupingBy(o -> o.getStartListingDate() <= todayTime && o.getEndListingDate() >= todayTime, Collectors.mapping(StockRightsDTO::getCode, Collectors.toSet())));
        //正在交易的权限股票
        Set<String> tradingStockRightsCodes = StockRightsDTOMap.getOrDefault(true, new HashSet<>());
        //结束交易的权限股票
        Set<String> unTradStockRightsCods = StockRightsDTOMap.getOrDefault(false, new HashSet<>());
        unTradStockRightsCods.removeAll(tradingStockRightsCodes);

        List<String> unTradeTempSocks = CollUtil.map(utsInfoService.findAllTempStocks(), ReuseTempDTO::getCode, true);

        if (CollUtil.isNotEmpty(unTradeTempSocks)) {
            // 1. 未上市股票不支持搜索和展示
            // 2. 已退市股票，如果出现在 xnhks0314 中，不支持搜索和展示
            stockDefineList = stockDefineList.stream().filter(ssi -> (ObjectUtil.notEqual(ssi.getSuspension(), StockSecurityStatusEnum.QUIT.getCode()))
                    || (ObjectUtil.equal(ssi.getSuspension(), StockSecurityStatusEnum.QUIT.getCode()) && !unTradeTempSocks.contains(ssi.getCode()))).collect(Collectors.toList());
        }
        //码表过滤结束交易的权限股票
        if (CollUtil.isNotEmpty(unTradStockRightsCods)) {
            stockDefineList = stockDefineList.stream().filter(stockDefine ->!unTradStockRightsCods.contains(stockDefine.getCode())).collect(Collectors.toList());
        }
        // 3. 股票并行交易结束次日不允许搜索
        stockDefineList = CollUtil.filter(stockDefineList, ssi -> stockCodeIdMap.containsKey(ssi.getCode()));

        // 行业名称
        Map<String, String> industryNameMap = stockDefineList.stream().filter(sd -> ObjectUtil.equal(StockTypeEnum.HY.getCode(), sd.getStockType())).collect(Collectors.toMap(StockDefine::getCode, StockDefine::getStockName, (o, v) -> v));
        // 正股 + 行业 + 指数
        JSONObject indexJson = JSON.parseObject(indexNewCode);
        List<ComStockSimpleDto> stockDtoList = stockDefineList.stream().map(sd -> convertDefineToSimpleDto(sd, indexJson, industryNameMap, stockCodeIdMap,tradingStockRightsCodes))
                .filter(css -> ObjectUtil.isNotNull(css) && StrUtil.isNotBlank(css.getCode()) && ObjectUtil.isNotEmpty(css.getStockId()))
                .sorted(Comparator.comparing(ComStockSimpleDto::getCode)).collect(Collectors.toList());

        // 给双柜台股票打上标识
        for (ComStockSimpleDto comStockSimpleDto:stockDtoList) {
            for (StockDefine stockDefine:stockDefineList) {
               if(StringUtils.isNotBlank(stockDefine.getDomainCode()) && StringUtils.equals(comStockSimpleDto.getCode(), stockDefine.getDomainCode())){
                   comStockSimpleDto.setDomainCode(true);
                   break;
               }
            }
        }

        log.info("stockCache getStockByCodes size|cost: {}|{}s", CollUtil.size(stockDtoList), timeInterval.interval() / 1000.0);
        return stockDtoList;
    }

    private ComStockSimpleDto convertDefineToSimpleDto(StockDefine stockDefine, JSONObject indexJson, Map<String, String> industryNameMap, Map<String, Long> stockCodeIdMap,Set<String> tradingStockRightsCodes) {
        ComStockSimpleDto stockSimpleInfo = new ComStockSimpleDto();
        BeanUtils.copyProperties(stockDefine, stockSimpleInfo);
        // 维护名称
        String stockName = StrUtil.blankToDefault(stockDefine.getStockName(), StrUtil.blankToDefault(stockDefine.getShortname(), stockDefine.getName()));
        stockSimpleInfo.setStockId(stockCodeIdMap.get(stockDefine.getCode()));
        stockSimpleInfo.setStockName(MyStringUtil.toDBC(stockName));
        stockSimpleInfo.setPrefixCode(StrUtil.subBefore(stockDefine.getCode(), ".", true));
        //全角转半角
        stockSimpleInfo.setName(MyStringUtil.toDBC(stockSimpleInfo.getName()));
        stockSimpleInfo.setShortname(MyStringUtil.toDBC(stockSimpleInfo.getShortname()));
        stockSimpleInfo.setNameInitials(ConvertUtil.getFirstLetter(stockSimpleInfo.getName()));
        stockSimpleInfo.setNamePinyin(ConvertUtil.getPinyin(stockSimpleInfo.getName()));
        if (StringUtils.isNotBlank(stockSimpleInfo.getStockName())) {
            stockSimpleInfo.setStockNameInitials(ConvertUtil.getFirstLetter(stockSimpleInfo.getStockName()));
            stockSimpleInfo.setStockNamePinyin(ConvertUtil.getPinyin(stockSimpleInfo.getStockName()));
        }
        if (StringUtils.isNotBlank(stockSimpleInfo.getShortname())) {
            stockSimpleInfo.setShortNameInitials(ConvertUtil.getFirstLetter(stockSimpleInfo.getShortname()));
            stockSimpleInfo.setShortNamePinyin(ConvertUtil.getPinyin(stockSimpleInfo.getShortname()));
        }
        if (ObjectUtil.equal(StockTypeEnum.INDEX.getCode(), stockDefine.getStockType())) {
            stockSimpleInfo.setType(1);
            stockSimpleInfo.setShortname(indexJson.getString(stockDefine.getCode()));
            stockSimpleInfo.setExtField(indexJson.getString(stockDefine.getCode()));
        } else {
            stockSimpleInfo.setType(0);
        }
        stockSimpleInfo.setStockType(stockDefine.getStockType());
        stockSimpleInfo.setExchange(ExchangeEnum.HK_EX.getCode());
        stockSimpleInfo.setRegionType(RegionTypeEnum.HK.getCode());
        stockSimpleInfo.setSecurityStatus(stockDefine.getSuspension());
        stockSimpleInfo.setIndustryId(StrUtil.isNotBlank(stockDefine.getIndustryCode()) ? stockCodeIdMap.get(stockDefine.getIndustryCode()) : null);
        stockSimpleInfo.setIndustryCode(stockDefine.getIndustryCode());
        stockSimpleInfo.setIndustryName(industryNameMap.get(stockDefine.getIndustryCode()));
        if (StringUtils.isNotBlank(stockSimpleInfo.getIndustryName())) {
            stockSimpleInfo.setIndustryNameInitials(ConvertUtil.getFirstLetter(stockSimpleInfo.getIndustryName()));
            stockSimpleInfo.setIndustryNamePinyin(ConvertUtil.getPinyin(stockSimpleInfo.getIndustryName()));
        }
        stockSimpleInfo.setCurrency(stockDefine.getCurrency());
        stockSimpleInfo.setLotsize(stockDefine.getLotsize());
        stockSimpleInfo.setStockRights(tradingStockRightsCodes.contains(stockDefine.getCode()));
        stockSimpleInfo.setMarketCode(stockDefine.getMarketcode());
        return stockSimpleInfo;
    }

    // /**
    //  * 返回历史查询记录
    //  *
    //  * @param stockHistory
    //  * @return
    //  */
    // @Deprecated
    // public List<ComStockSimpleDto> queryStockByHistory(List<String> stockHistory) {
    //     TimeInterval timeInterval = new TimeInterval();
    //     List<ComStockSimpleDto> stockSimpleInfos = getStockInfoMapFromRedis(stockHistory);
    //     List<ComStockSimpleDto> simpleDtos = stockSimpleInfos.stream().filter(item -> CollUtil.isEmpty(stockHistory) || stockHistory.contains(item.getCode())).collect(Collectors.toList());
    //     List<String> codes = CollUtil.map(simpleDtos, ComStockSimpleDto::getCode, true);
    //     List<String> subColl = CollUtil.subtractToList(stockHistory, codes);
    //     if (CollUtil.isNotEmpty(subColl)) {
    //         List<ComStockSimpleDto> subCollDots = getStockByCodes(subColl);
    //         if (CollUtil.isNotEmpty(subCollDots)) {
    //             saveAllStockDtoToRedis(CollUtil.unionAll(stockSimpleInfos, subCollDots));
    //             CollUtil.forEach(subCollDots, (dto, index) -> simpleDtos.add(dto));
    //         }
    //     }
    //     log.info("stockCache queryStockByHistory stockHistory|subColl|cost: {}|{}|{}s", CollUtil.size(stockHistory), CollUtil.size(subColl), timeInterval.interval() / 1000.0);
    //     return simpleDtos;
    // }

    public List<ComStockSimpleDto> queryStockDtoList(List<Long> stockIds) {
        if (CollUtil.isNotEmpty(stockIds)) {
            if (MapUtil.isNotEmpty(STOCK_INFO_MAP)) {
                return new ArrayList<>(CollUtil.filterNew(STOCK_INFO_MAP.values(), dto -> stockIds.contains(dto.getStockId())));
            } else {
                List<HkStockRelation> stockRelations = hkStockRelationService.list(Wrappers.<HkStockRelation>lambdaQuery().in(HkStockRelation::getStockId, stockIds));
                List<String> codeList = CollUtil.map(stockRelations, HkStockRelation::getInnerCode, true);
                if (CollUtil.isNotEmpty(codeList)) {
                    return this.queryStockInfoList(codeList);
                }
            }
        }
        return new ArrayList<>();
    }

    public List<ComStockSimpleDto> queryStockInfoList(List<String> stockCodes) {
        return ListUtil.toList(this.queryStockInfoMap(stockCodes).values());
    }

    public Map<String, ComStockSimpleDto> queryStockInfoMap(List<String> stockCodes) {
        return queryStockInfoByCodes(stockCodes);
    }

    public Map<String, Long> queryStockIdMap(List<String> stockCodes) {
        Map<String, ComStockSimpleDto> stockInfoMap = this.queryStockInfoMap(stockCodes);
        return CollUtil.toMap(stockInfoMap.values(), new HashMap<>(), ComStockSimpleDto::getCode, ComStockSimpleDto::getStockId);
    }

    public Map<String, String> queryStockNameMap(List<String> stockCodes) {
        Map<String, ComStockSimpleDto> stockInfoMap = this.queryStockInfoMap(stockCodes);
        return buildSimpleNameMap(ListUtil.toList(stockInfoMap.values()));
    }

    private <R> Map<String, ComStockSimpleDto> queryStockInfoByCodes(List<String> codeList) {
        TimeInterval timeInterval = new TimeInterval();
        List<String> stockCodes = codeList;

        if (null != stockCodes) {
            // 参数不为nul时，过滤掉无效code
            List<String> filterCOdes = stockCodes.stream().filter(code -> StrUtil.isNotBlank(code) && !StrUtil.endWithIgnoreCase(code, ".us")).collect(Collectors.toList());
            // 过滤掉的code
            Collection<String> invalidCodes = CollUtil.subtract(stockCodes, filterCOdes);
            stockCodes = filterCOdes;
            if (CollUtil.isNotEmpty(invalidCodes)) {
                log.info("stockCache queryStockInfoByCodes invalidCodes: {}", invalidCodes);
                log.info("stockCache queryStockInfoByCodes stackTrace: {}", StackUtil.getStackTraceString());
            }
        }
        // 从本地缓存获取数据
        Map<String, ComStockSimpleDto> resultMap = this.queryLocalInfoMap(stockCodes);
        // 判断本地缓存中是否都能查到
        List<String> subColl = CollUtil.subtractToList(CollUtil.defaultIfEmpty(stockCodes, ListUtil.empty()), resultMap.keySet());
        if (CollUtil.isNotEmpty(subColl)) {
            // 从redis中查询数据
            Map<String, ComStockSimpleDto> rdsFunMap = this.getStockInfoMapFromRedis(subColl);
            resultMap.putAll(rdsFunMap);
            // 判断redis中是否都能查到
            subColl = CollUtil.subtractToList(CollUtil.defaultIfEmpty(stockCodes, ListUtil.empty()), resultMap.keySet());
            if (CollUtil.isNotEmpty(subColl)) {
                List<ComStockSimpleDto> subCollDots = getStockByCodes(subColl);
                if (CollUtil.isNotEmpty(subCollDots)) {
                    // 异步刷新缓存
                    ThreadUtil.execAsync(this::updateStockSimpleInfo);
                    // 汇总结果集
                    CollUtil.forEach(subCollDots, (dto, index) -> resultMap.put(dto.getCode(), dto));
                }
            }
        }

        log.info("stockCache queryStockInfoByCodes stockCodes|subColl|cost: {}|{}|{}s", CollUtil.size(stockCodes), CollUtil.size(subColl), timeInterval.interval() / 1000.0);

        return MapUtil.filter(resultMap, entry -> CollUtil.isEmpty(codeList) || codeList.contains(entry.getKey()));
    }

    // public void updateStockAndName(String code, String stockName) {
    //     log.info("stockCache updateStockAndName start code|stockName: {}|{}", code, stockName);
    //     saveNewStockDtoToRedis(this.getStockByCodes(ListUtil.of(code)));
    //     log.info("stockCache updateStockAndName end code|stockName: {}|{}", code, stockName);
    // }

    // private static StockCache getInstance() {
    //     return SpringUtil.getBean(StockCache.class);
    // }

    private Map<String, Long> getStockCodeIdMapFromRedis(List<String> codes) {
        // 从redis获取数据
        String compress = redisClient.get(RedisKeyConstants.STOCK_SIMPLE_ID_MAP);
        JSONObject jsonObject = StrUtil.isNotBlank(compress) ? JSON.parseObject(ZipUtil.gunzip(compress)) : new JSONObject();
        // 转成map结构
        return jsonObject.toJavaObject(new TypeReference<Map<String, Long>>() {});
    }

    private Map<String, String> getStockNameMapFromRedis(List<String> codes) {
        // 从redis获取数据
        String compress = redisClient.get(RedisKeyConstants.STOCK_SIMPLE_NAME_MAP);
        JSONObject jsonObject = StrUtil.isNotBlank(compress) ? JSON.parseObject(ZipUtil.gunzip(compress)) : new JSONObject();
        // 转成map结构
        return jsonObject.toJavaObject(new TypeReference<Map<String, String>>() {});
    }

    private Map<String, ComStockSimpleDto> getStockInfoMapFromRedis(List<String> codes) {
        int size = CollUtil.size(codes);
        List<ComStockSimpleDto> redisStocks = new ArrayList<>();
        log.info("stockCache getStockInfoMapFromRedis codes size: {}", size);
        // 从redis获取数据
        if (size > 0 && size < queryAllSize) {
            redisStocks = redisClient.strGetHashPipelined(RedisKeyConstants.STOCK_SIMPLE_INFO_BEAN, codes);
        } else {
            String compress = redisClient.get(RedisKeyConstants.STOCK_SIMPLE_INFO_MAP);
            redisStocks = StrUtil.isNotBlank(compress) ? JSON.parseArray(ZipUtil.gunzip(compress), ComStockSimpleDto.class) : new ArrayList<>();
            redisStocks = redisStocks.stream().filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        }
        Map<String, ComStockSimpleDto> simpleDtoMap = CollUtil.toMap(CollUtil.defaultIfEmpty(redisStocks, Collections.emptyList()), new HashMap<>(), ComStockSimpleDto::getCode);
        // 将redis结果保存到本地
        STOCK_INFO_MAP.putAll(simpleDtoMap);
        return simpleDtoMap;
    }

    public Map<String, ComStockSimpleDto> queryLocalInfoMap(List<String> stockCodes) {
        log.info("stockCache queryLocalInfoMap codes: {}", stockCodes);
        if (CollUtil.isNotEmpty(stockCodes) && ObjectUtil.isNotEmpty(CollUtil.findOne(stockCodes, code -> StrUtil.endWithIgnoreCase(code, ".us")))) {
            // 如果有美股股票调用了该方法，打印出调用链路
            log.info("stockCache queryLocalInfoMap stackTrace: {}", StackUtil.getStackTraceString());
        }
        return MapUtil.filter(STOCK_INFO_MAP, Convert.toStrArray(stockCodes));
    }

    private void saveNewStockDtoToRedis(List<ComStockSimpleDto> newDtoList) {
        // 缓存的记录
        List<ComStockSimpleDto> stockFromRedis = getStockByCodes(null);
        List<ComStockSimpleDto> addDtoList = CollUtil.defaultIfEmpty(newDtoList, new ArrayList<>());
        // 保存到redis
        saveAllStockDtoToRedis(CollUtil.unionAll(stockFromRedis, addDtoList));
    }

    private void saveAllStockDtoToRedis(List<ComStockSimpleDto> simpleDtoList) {
        // 保存之前去重
        List<ComStockSimpleDto> distinctDtoList = CollUtil.distinct(simpleDtoList, ComStockSimpleDto::getCode, true);
        long start = System.currentTimeMillis();
        log.info("stockCache saveAllStockDtoToRedis start size: {}", CollUtil.size(distinctDtoList));

        if (CollUtil.isNotEmpty(distinctDtoList)) {
            redisClient.set(RedisKeyConstants.STOCK_SIMPLE_INFO_MAP, ZipUtil.gzip(JSON.toJSONString(distinctDtoList)));
            // redisClient.set(RedisKeyConstants.STOCK_SIMPLE_NAME_MAP, ZipUtil.gzip(JSON.toJSONString(buildSimpleNameMap(distinctDtoList))));
            Map<String, ComStockSimpleDto> codeSimpleMap = distinctDtoList.stream().collect(Collectors.toMap(ComStockSimpleDto::getCode, v -> v, (o, v) -> v));
            Map<String, ComStockSimpleDto> redisBeanMap = redisClient.hmget(RedisKeyConstants.STOCK_SIMPLE_INFO_BEAN);
            List<String> invalidRedisCodes = redisBeanMap.keySet().stream().filter(code -> !codeSimpleMap.containsKey(code)).collect(Collectors.toList());
            redisClient.strSetHashPipelined(RedisKeyConstants.STOCK_SIMPLE_INFO_BEAN, codeSimpleMap);
            if (CollUtil.isNotEmpty(invalidRedisCodes)) {
                redisClient.hdel(RedisKeyConstants.STOCK_SIMPLE_INFO_BEAN, invalidRedisCodes.toArray());
            }
            // Map<String, Long> codeIdMap = distinctDtoList.stream().collect(Collectors.toMap(ComStockSimpleDto::getCode, ComStockSimpleDto::getStockId, (o, v) -> v));
            // redisClient.set(RedisKeyConstants.STOCK_SIMPLE_ID_MAP, ZipUtil.gzip(JSON.toJSONString(codeIdMap)));
            List<String> invalidLocalCodes = STOCK_INFO_MAP.keySet().stream().filter(code -> !codeSimpleMap.containsKey(code)).collect(Collectors.toList());
            STOCK_INFO_MAP.putAll(codeSimpleMap);
            if (CollUtil.isNotEmpty(invalidLocalCodes)) {
                MapUtil.removeAny(STOCK_INFO_MAP, Convert.toStrArray(invalidLocalCodes));
            }
            log.info("stockCache saveAllStockDtoToRedis end, cost: {}", (System.currentTimeMillis() - start));
        }
    }

    private Map<String, String> buildSimpleNameMap(List<ComStockSimpleDto> simpleDtoList) {
        Map<String, String> simpleNameMap = new HashMap<>(20000);
        CollUtil.defaultIfEmpty(simpleDtoList, Collections.emptyList()).forEach(dto -> {
            if (StrUtil.isNotBlank(dto.getStockName())) {
                simpleNameMap.put(dto.getCode(), dto.getStockName());
                simpleNameMap.put(dto.getStockName(), dto.getStockNameInitials());
            }
            if (StrUtil.isNotBlank(dto.getIndustryName())) {
                simpleNameMap.put(dto.getIndustryName(), dto.getIndustryNameInitials());
            }
        });
        return simpleNameMap;
    }


    private final LoadingCache<String, List<String>> hkHYCodeMap = Caffeine.newBuilder()
            .refreshAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build(k -> {
                return stockDefineService.listDefinesByCodeType(null, StockTypeEnum.HY.getCode()).stream().map(StockDefine::getCode).collect(Collectors.toList());
            });

    public List<String> getHYCodes() {
        return hkHYCodeMap.get(HY_CODE_CACHE);
    }
}
