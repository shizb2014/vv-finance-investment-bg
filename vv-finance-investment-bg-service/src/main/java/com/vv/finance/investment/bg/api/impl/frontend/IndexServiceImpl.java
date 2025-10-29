package com.vv.finance.investment.bg.api.impl.frontend;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenlibao.security.sdk.ws.core.model.req.RankInduReq;
import com.fenlibao.security.sdk.ws.core.model.req.TrendReq;
import com.fenlibao.security.sdk.ws.core.model.resp.RankInduResp;
import com.fenlibao.security.sdk.ws.core.model.resp.TrendResp;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.OptionSortConstant;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.constants.omdc.OmdcKind;
import com.vv.finance.common.constants.omdc.OmdcMode;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.receiver.Index;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.investment.bg.api.frontend.IndexService;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.StockStatueEnum;
import com.vv.finance.investment.bg.dto.index.IndexDashBoardInfo;
import com.vv.finance.investment.bg.dto.info.IndustrySectorDTO;
import com.vv.finance.investment.bg.dto.stock.IndexConstituent;
import com.vv.finance.investment.bg.dto.stock.StockBaseDTO;
import com.vv.finance.investment.bg.dto.stock.StockRtKlineDTO;
import com.vv.finance.investment.bg.entity.uts.Xnhk1002;
import com.vv.finance.investment.bg.mapper.uts.Xnhk1002Mapper;
import com.vv.finance.investment.bg.stock.indicator.entity.BaseStockIndicator;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.kline.entity.RtStockKline;
import com.vv.finance.investment.bg.utils.CollectUtils;
import com.vv.finance.investment.gateway.api.index.IndexInfoServiceApi;
import com.vv.finance.investment.gateway.api.index.IndexKlineServiceApi;
import com.vv.finance.investment.gateway.api.stock.IStockBusinessApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

// import static com.vv.finance.investment.bg.cache.StockCache.getPinYinAbbr;

/**
 * @author chenyu
 * @date 2020/10/29 11:00
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class IndexServiceImpl implements IndexService {

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    IStockBusinessApi stockBusinessApi;
    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    IndexKlineServiceApi indexKlineServiceApi;
    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    IndexInfoServiceApi indexInfoServiceApi;
    @Resource
    Xnhk1002Mapper xnhk1002Mapper;
    @Resource
    private RedisClient redisClient;
    @Resource
    private StockService stockService;
    @Resource
    private StockCache stockCache;
    @Resource
    private StockDefineMapper stockDefineMapper;
    @Value("#{'${hk.index.code}'.split(',')}")
    private List<String> indexs;
    @Value("#{'${hk.hf.date}'.split(',')}")
    private List<String> halfDate;
    private static final String name = "name";
    private static final String code = "code";


    //快照字段名集合
    private static List<String> snapshotFieldList;
    /**
     * 指数字段
     */
    private static List<String> indexFileList;

    @PostConstruct
    public void init() {
        Class<StockSnapshot> snapshotClass = StockSnapshot.class;
        snapshotFieldList = Arrays.stream(snapshotClass.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
        indexFileList = Arrays.stream(IndexConstituent.class.getDeclaredFields()).map(Field::getName).collect(Collectors.toList());
    }


    @Override
    public List<IndustrySectorDTO> getIndustrySector(Integer num) {
        num = ObjectUtil.defaultIfNull(num, 9);
        List<ComStockSimpleDto> hyStocks = CollUtil.filter(stockCache.queryStockInfoList(null), css -> ObjectUtil.equal(StockTypeEnum.HY.getCode(), css.getStockType()));
        List<String> codeList = CollUtil.map(hyStocks, ComStockSimpleDto::getCode, true);
        List<StockSnapshot> snapshotList = stockService.getSnapshotList(Convert.toStrArray(codeList));
        return CollUtil.sort(snapshotList, Comparator.comparing(StockSnapshot::getChgPct).reversed()).stream().limit(num).map(snap -> {
            IndustrySectorDTO sectorDTO = new IndustrySectorDTO();
            sectorDTO.setName(snap.getName());
            sectorDTO.setCode(snap.getCode());
            sectorDTO.setChg_pct(snap.getChgPct());
            sectorDTO.setStockId(snap.getStockId());
            return sectorDTO;
        }).collect(Collectors.toList());
    }

    private boolean isToday(Long date) {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        long time = instance.getTime().getTime();
        return date > time;
    }

    @Override
    public StockRtKlineDTO getRtStockKline(String code, String type) {
        Map<String, List<RtStockKline>> list = new TreeMap<>();
        Map<String, List<BaseStockIndicator>> indicator = new TreeMap<>();
        int size = 1;
        switch (type) {
            case "fiveDay":
                list = buildBaseStockDetailByTrend(indexKlineServiceApi.indexTrendFive(TrendReq.builder().symbol(code).build()).getData(), 5);
                break;
            case OmdcMode.RT:
                list = buildBaseStockDetailByTrend(indexKlineServiceApi.indexTrend(TrendReq.builder().symbol(code).build()).getData(), 1);
                break;
            default:
                break;
        }
        StockRtKlineDTO stockRtKlineDTO = new StockRtKlineDTO();
        List<Map<String, List<RtStockKline>>> maps = CollectUtils.splitMap(list, size);
        stockRtKlineDTO.setKlines(maps);
        Index index = (Index) redisClient.get(RedisKeyConstants.RECEIVER_INDEX_SNAPSHOT_BEAN.concat(code));
        if (index != null) {
            stockRtKlineDTO.setPreClose(index.getPreclose());
            stockRtKlineDTO.setOpen(index.getOpen());
        }
        return stockRtKlineDTO;
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
            if (temp == num) {
                klines.addAll(collect.subList(begin, data.size()));
            } else {
                klines.addAll(collect.subList(begin, end));
            }
            // 上半日交易进行数据补点（五日分时图）
            if (hf && !nowStr.equals(date)) {
                RtStockKline rtStockKline1 = collect.get(end - 1);
                rtStockKline1.setVolume(new BigDecimal("0"));
                Long time = rtStockKline1.getTime() + 60 * 60 * 1000;
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


    @Override
    public List<StockBaseDTO> getIndexComponent(String code, Integer pageSize, String sort, String sortKey) {
        // if (code.contains("hk")){
        //     return Collections.emptyList();
        // }
        Map<String, String> simpleNameMap = stockCache.queryStockNameMap(null);
        List<StockSnapshot> snapshotList = stockService.getStockSnapshotByBlockCode(code);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(sortKey)) {
            snapshotList = sortSnapshots(snapshotList, simpleNameMap, sortKey, "asc".equals(sort));
        } else {
            //默认按涨跌幅倒序排列
            snapshotList = sortSnapshots(snapshotList, simpleNameMap, "chgPct", false);
        }

        List<StockBaseDTO> collect = snapshotList.stream().map(s -> {
            StockBaseDTO stockBaseDTO = new StockBaseDTO();
            stockBaseDTO.setCode(s.getCode());
            stockBaseDTO.setChgPct(s.getChgPct());
            stockBaseDTO.setChg(s.getChg());
            stockBaseDTO.setName(simpleNameMap.get(s.getCode()));
            stockBaseDTO.setLast(s.getLast());
            stockBaseDTO.setStockType(StockTypeEnum.STOCK.getCode());
            stockBaseDTO.setRegionType(RegionTypeEnum.HK.getCode());
            stockBaseDTO.setSuspension(s.getSuspension());
            return stockBaseDTO;

        }).collect(Collectors.toList());
        // sort(collect, sortKey, "asc".equals(sort));
        return collect;

        /*IndexComponentReq indexComponentReq = new IndexComponentReq();
        indexComponentReq.setIndexcode(code);
        ResultT<PageInfo<IndexComponentResp>> pageInfoResultT = indexInfoServiceApi.pageComponent(indexComponentReq);
        if (ResultCode.SUCCESS.code() != pageInfoResultT.getCode() || pageInfoResultT.getData().getList().isEmpty()) {
            log.error("调用融聚汇接口--获取指数成分股错误");
            return Collections.emptyList();
        }
        List<StockBaseDTO> collect = pageInfoResultT.getData().getList().stream().map(item -> {
            StockBaseDTO stockBaseDTO = new StockBaseDTO();
            stockBaseDTO.setCode(item.getCode());
            stockBaseDTO.setChgPct(item.getChg_pct());
            stockBaseDTO.setChg(item.getChg());
            stockBaseDTO.setName(item.getName());
            stockBaseDTO.setLast(item.getPrice());
            return stockBaseDTO;
        }).sorted(Comparator.comparing(StockBaseDTO::getCode)).collect(Collectors.toList());
        sort(collect, sortKey, OptionSortConstant.UP.equals(sort));
        return collect;*/
    }

    private List<StockSnapshot> sortSnapshots(List<StockSnapshot> snapshotList, Map<String, String> simpleNameMap, String sortKey, boolean isUp) {
        if ("name".equals(sortKey)) {
            if (isUp) {
                snapshotList.sort(Comparator.comparing(o -> simpleNameMap.get(o.getName()), Comparator.nullsFirst(String::compareTo)));
            } else {
                Comparator<StockSnapshot> comparing = Comparator.comparing(o -> simpleNameMap.get(o.getName()), Comparator.nullsLast(String::compareTo));
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

    private void sort(List<StockBaseDTO> snapshotList, String sortKey, boolean isUp) {
        if ("name".equals(sortKey)) {
            if (isUp) {
                Comparator coll = Collator.getInstance(Locale.CHINESE);
                snapshotList.sort(new Comparator<StockBaseDTO>() {
                    @Override
                    public int compare(StockBaseDTO o1, StockBaseDTO o2) {
                        return coll.compare(o1.getName(), o2.getName());
                    }
                });
            } else {
                Collator coll = Collator.getInstance(Locale.CHINESE);
                Collections.sort(snapshotList, new Comparator<StockBaseDTO>() {
                    @Override
                    public int compare(StockBaseDTO o1, StockBaseDTO o2) {
                        return coll.compare(o2.getName(), o1.getName());
                    }
                });
            }
            return;
        }
        if ("code".equals(sortKey)) {
            if (isUp) {
                snapshotList.sort(Comparator.comparing(StockBaseDTO::getCode));
            } else {
                snapshotList.sort(Comparator.comparing(StockBaseDTO::getCode).reversed());
            }
            return;
        }

        if (snapshotFieldList.contains(sortKey)) {
            try {
                PropertyDescriptor descriptor = new PropertyDescriptor(sortKey, StockBaseDTO.class);
                Method readMethod = descriptor.getReadMethod();
                snapshotList.sort((t1, t2) -> {
                    try {
                        Object result1 = readMethod.invoke(t1);
                        BigDecimal decimal1 = result1 == null ? BigDecimal.ZERO : new BigDecimal(result1.toString());
                        Object result2 = readMethod.invoke(t2);
                        BigDecimal decimal2 = result2 == null ? BigDecimal.ZERO : new BigDecimal(result2.toString());
                        if (isUp) {
                            return decimal1.compareTo(decimal2);
                        } else {
                            return decimal2.compareTo(decimal1);
                        }
                    } catch (Exception e) {
                        log.error("执行方法失败");
                        return 0;
                    }
                });
            } catch (Exception e) {
                log.error("排序失败", e);
            }
        }
    }

    @Override
    public List<IndexDashBoardInfo> getIndexList() {
        List result = new LinkedList<IndexDashBoardInfo>();
        indexs.forEach(item -> {
            IndexDashBoardInfo indexDashBoardInfo = new IndexDashBoardInfo();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<RtStockKline> collect = indexKlineServiceApi.indexTrend(TrendReq.builder().symbol(item).build()).getData().stream().map(it -> {
                RtStockKline stockKline = new RtStockKline();
                try {
                    stockKline.setTime(simpleDateFormat.parse(it.getTime()).getTime());
                } catch (ParseException e) {
                    log.error("parse time error", e);
                }
                stockKline.setVolume(it.getVolume());
                stockKline.setPrice(it.getPrice().toString());
                stockKline.setChg(it.getChange());
                stockKline.setChgPct(it.getChangeRate());
                stockKline.setAvg_price(it.getAvgPrice().toString());
                stockKline.setAmount(it.getAmount());
                return stockKline;
            }).collect(Collectors.toList());
            indexDashBoardInfo.setTrendResp(collect);
            StockSnapshot o = (StockSnapshot) redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(item));
            if (o != null) {
                indexDashBoardInfo.setCode(o.getCode() == null ? "" : o.getCode());
                indexDashBoardInfo.setName(o.getName() == null ? "" : o.getName());
                indexDashBoardInfo.setPreClose(o.getPreClose());
                indexDashBoardInfo.setLast(o.getLast());
                indexDashBoardInfo.setIncrease(o.getChgPct());
                indexDashBoardInfo.setOpen(o.getOpen());
            }
            result.add(indexDashBoardInfo);
        });
        return result;
    }

    @Override
    public List<TrendResp> getLastTrend(String code) {
        List<TrendResp> last = indexKlineServiceApi.indexTrendFive(TrendReq.builder().symbol(code).type("last").build()).getData();
        return last;
    }

    /**
     * 根据指数代码获取对应的指数成分股
     *
     * @param indexCode 指数代码
     * @param sortKey   排序字段
     * @param sort      升序降序 0升序 1降序
     * @return
     */
    @Override
    @Deprecated
    public ResultT<List<IndexConstituent>> getIndexComponentCode(String indexCode,
                                                                 String sortKey,
                                                                 String sort) {
        List<Xnhk1002> xnhk1002s = xnhk1002Mapper.listMembersAndIndex("(".concat(indexCode).concat(")"));
        List<StockSnapshot> snapshotList = xnhk1002s.stream().map(s -> {
            StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(s.getF001v()));
            if (snapshot == null) {
                return new StockSnapshot();
            }
            return snapshot;
        }).filter(s -> s.getCode() != null).collect(Collectors.toList());
        Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
        List<IndexConstituent> indexConstituentList = snapshotList.stream().map(s -> {
            IndexConstituent constituent = new IndexConstituent();
            constituent.setCode(s.getCode());
            constituent.setName(stockNameMap.get(s.getCode()));
            constituent.setLast(s.getLast());
            constituent.setChg(s.getChg());
            constituent.setChgPct(s.getChgPct());
            constituent.setSharesTraded(s.getSharesTraded() == null ? null : s.getSharesTraded());
            constituent.setTurnover(s.getTurnover() == null ? null : s.getTurnover());
            constituent.setFiveMinutesChgPct(s.getFiveMinutesChgPct() == null ? null : s.getFiveMinutesChgPct());
            constituent.setYearToNowChgPct(s.getYearToNowChgPct() == null ? null : s.getYearToNowChgPct());
            constituent.setTurnoverRate(s.getTurnoverRate() == null ? null : s.getTurnoverRate());
            constituent.setQuantityRelativeRatio(s.getQuantityRelativeRatio() == null ? null : s.getQuantityRelativeRatio());
            constituent.setAppointThan(s.getAppointThan() == null ? null : s.getAppointThan());
            constituent.setSwing(s.getSwing() == null ? null : s.getSwing());
            constituent.setShortShares(s.getShortShares() == null ? null : s.getShortShares());
            constituent.setShortTurnover(s.getShortTurnover() == null ? null : s.getShortTurnover());
            return constituent;
        }).collect(Collectors.toList());
        indexConstituentSort(indexConstituentList, sortKey, OptionSortConstant.UP.equals(sort));
        return ResultT.success(indexConstituentList);
    }

    /**
     * 指数成分股排期
     *
     * @param snapshotList 股票代码
     * @param sortKey      排序字段
     * @param isUp         正序倒叙
     */
    private void indexConstituentSort(List<IndexConstituent> snapshotList,
                                      String sortKey,
                                      boolean isUp) {
        if (name.equals(sortKey)) {
            if (isUp) {
                Collator coll = Collator.getInstance(Locale.CHINESE);
                snapshotList.sort((o1, o2) -> coll.compare(o1.getName(), o2.getName()));
            } else {
                Collator coll = Collator.getInstance(Locale.CHINESE);
                snapshotList.sort((o1, o2) -> coll.compare(o2.getName(), o1.getName()));
            }
            return;
        }
        if (code.equals(sortKey)) {
            if (isUp) {
                snapshotList.sort(Comparator.comparing(IndexConstituent::getCode));
            } else {
                snapshotList.sort(Comparator.comparing(IndexConstituent::getCode).reversed());
            }
            return;
        }
        if (indexFileList.contains(sortKey)) {
            try {
                PropertyDescriptor descriptor = new PropertyDescriptor(sortKey, IndexConstituent.class);
                Method readMethod = descriptor.getReadMethod();
                snapshotList.sort((t1, t2) -> {
                    try {
                        Object result1 = readMethod.invoke(t1);
                        BigDecimal decimal1 = result1 == null ? BigDecimal.ZERO : new BigDecimal(result1.toString());
                        Object result2 = readMethod.invoke(t2);
                        BigDecimal decimal2 = result2 == null ? BigDecimal.ZERO : new BigDecimal(result2.toString());
                        if (isUp) {
                            return decimal1.compareTo(decimal2);
                        } else {
                            return decimal2.compareTo(decimal1);
                        }
                    } catch (Exception e) {
                        log.error("执行方法失败");
                        return 0;
                    }
                });
            } catch (Exception e) {
                log.error("排序失败", e);
            }
        }
    }


}
