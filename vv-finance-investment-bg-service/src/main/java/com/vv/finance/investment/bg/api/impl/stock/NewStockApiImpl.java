package com.vv.finance.investment.bg.api.impl.stock;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.ibm.icu.text.Collator;
import com.vv.finance.base.dto.ResultCode;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.common.ComNewStockInfoVo;
import com.vv.finance.common.entity.common.ComNewStockProspectusVo;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.enums.MarketStatusEnum;
import com.vv.finance.common.utils.*;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.stock.NewStockApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.StockCodeNameBaseDTO;
import com.vv.finance.investment.bg.dto.newcode.resp.NewStockListResp;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0503Mapper;
import com.vv.finance.investment.bg.mongo.model.StockUtsNoticeEntity;
import com.vv.finance.investment.bg.stock.information.service.StockInformationServiceImpl;
import com.vv.finance.investment.gateway.dto.resp.HKNewStockResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author liuxing
 * @Create 2023/6/29 14:27
 */
@Slf4j
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class NewStockApiImpl implements NewStockApi {

    @Autowired
    private RedisClient redisClient;
    @Resource
    private StockService stockService;
    @Resource
    private StockCache stockCache;
    @Resource
    private StockInfoApi stockInfoApi;

    @Resource
    private Xnhks0503Mapper xnhks0503Mapper;

    private final static String NAME = "name";
    private final static String CODE = "code";
    private final static String INDUSTRYNAME = "industryName";
    private static final String STOCK_SUFFIX = ".hk";
    @Resource
    private StockInformationServiceImpl informationService;
    @Override
    public ResultT<List<StockCodeNameBaseDTO>> getNewStockCodeList(String sort, String sortKey) {
        List<StockCodeNameBaseDTO> dtoList = new ArrayList<>();
        try {
            List<HKNewStockResp> list = redisClient.get(RedisKeyConstants.BG_HK_NEW_STOCK_LIST);
            if (CollectionUtil.isEmpty(list)) {
                log.warn("redis未查询到新股");
                return ResultT.success(dtoList);
            }
            List<NewStockListResp> allFieldList = this.buildAllFieldList(list);

            if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(sortKey)) {
                boolean ascFlag = SortListUtil.ASC.equalsIgnoreCase(sort);
                if (NAME.equals(sortKey)) {
                        allFieldList.sort((o1, o2) -> {
                            Collator chinaCollator = Collator.getInstance(Locale.CHINESE);
                            return ascFlag ? chinaCollator.compare(o1.getName(), o2.getName()) : chinaCollator.compare(o2.getName(), o1.getName());
                        });
                } else if (CODE.equals(sortKey)) {
                    Comparator<NewStockListResp> comparing = Comparator.comparing(NewStockListResp::getCode);
                    allFieldList.sort(ascFlag ? comparing : comparing.reversed());
                } else if (INDUSTRYNAME.equals(sortKey)) {
                    Map<String, String> simpleNameMap = stockCache.queryStockNameMap(null);
                    Comparator<NewStockListResp> firstComparator = Comparator.comparing(o -> simpleNameMap.get(o.getIndustryName()), Comparator.nullsFirst(String::compareTo));
                    Comparator<NewStockListResp> secondComparator = firstComparator.thenComparing(NewStockListResp::getCode);
                    allFieldList.sort(ascFlag ? secondComparator : secondComparator.reversed());
                }else {
//                        allFieldList = (List<NewStockListResp>) SortListUtil.sortIncludingNull(allFieldList, sortKey, sort);
                    nullFirstSort(allFieldList, sortKey, sort);
                }
                allFieldList.forEach(dto -> {
                    StockCodeNameBaseDTO baseDTO = new StockCodeNameBaseDTO();
                    baseDTO.setStockId(dto.getStockId());
                    baseDTO.setCode(dto.getCode());
                    baseDTO.setName(dto.getName());
                    baseDTO.setStockType(dto.getStockType());
                    baseDTO.setRegionType(dto.getRegionType());
                    baseDTO.setStockId(dto.getStockId());
                    dtoList.add(baseDTO);
                });
            } else {
                allFieldList.sort(Comparator.comparing(NewStockListResp::getChgPct, Comparator.nullsFirst(BigDecimal::compareTo)).reversed());
//                allFieldList.sort(Comparator.comparing(NewStockListResp::getChgPct).reversed());
                allFieldList.forEach(dto -> {
                    StockCodeNameBaseDTO baseDTO = new StockCodeNameBaseDTO();
                    baseDTO.setStockId(dto.getStockId());
                    baseDTO.setCode(dto.getCode());
                    baseDTO.setName(dto.getName());
                    baseDTO.setStockType(dto.getStockType());
                    baseDTO.setRegionType(dto.getRegionType());
                    baseDTO.setStockId(dto.getStockId());
                    dtoList.add(baseDTO);
                });
            }
        } catch (Exception e) {
        log.error("新股列表获取异常", e);
        }
        return ResultT.success(dtoList);
    }

    /**
     * 构建全部字段
     *
     * @param list
     * @return
     */
    private List<NewStockListResp> buildAllFieldList(List<HKNewStockResp> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<String> stockCodeList = list.stream().map(HKNewStockResp::getSymbol).collect(Collectors.toList());
//        Map<String, ComStockSimpleDto> comStockSimpleDtoMap = stockCache.queryStockInfoList(stockCodeList).stream().collect(Collectors.toMap(o -> o.getCode(), o -> o, (k1, k2) -> k1));
        List<StockSnapshot> stockSnapshotList = stockService.getSnapshotList(stockCodeList.toArray(new String[0]));
        Map<String, StockSnapshot> stockSnapshotMap = stockSnapshotList.stream().collect(Collectors.toMap(StockSnapshot::getCode, Function.identity()));
        return BeanCopyUtil.copyListProperties(list, NewStockListResp::new, (s, t) -> {
            StockSnapshot stockSnapshot = stockSnapshotMap.get(s.getSymbol());
            if (Objects.isNull(stockSnapshot)) {
                log.info("未查询到[{}]的股票快照！", s.getSymbol());
                stockSnapshot = new StockSnapshot();
            }
            t.setStockId(stockSnapshot.getStockId());
            t.setName(stockSnapshot.getName());
            t.setCode(s.getSymbol());
            t.setSharesTraded(stockSnapshot.getSharesTraded());
            t.setTurnover(stockSnapshot.getTurnover());
//            t.setApplyLotsFor1Lot();
            t.setLast(stockSnapshot.getLast());
            t.setChgPct(stockSnapshot.getChgPct());
            t.setChg(stockSnapshot.getChg());
            t.setPeTtm(stockSnapshot.getPeTtm());
            t.setTotalValue(stockSnapshot.getTotalValue());
            //价格相关的取快照里面的价格，涨跌幅也按快照最新价计算
            BigDecimal issuePrice = s.getIssuePrice();
            BigDecimal totalChangeRate = BigDecimalUtil.divideSaveSix(BigDecimalUtil.calcSubtract(stockSnapshot.getLast(), issuePrice), issuePrice);
            LocalDate listingDate = LocalDateTimeUtil.getLocalDate(s.getListingDate(), "yyyy-MM-dd");
            if(listingDate.equals(LocalDate.now())){
                //上市当日，用最新价计算的累计涨跌幅来算
                t.setFirstDayChangeRate(totalChangeRate);
            }else {
                t.setFirstDayChangeRate(BigDecimalUtil.getDivide100Result(s.getFirstDayChangeRate(),6));
            }
            t.setTotalChangeRate(totalChangeRate);
            t.setYearToNowChgPct(stockSnapshot.getYearToNowChgPct());
            t.setOneLotSuccRate(BigDecimalUtil.getDivide100Result(s.getOneLotSuccRate(),6));

            t.setListingDateTimestamp(listingDate.atStartOfDay().toEpochSecond(ZoneOffset.ofHours(8)) * 1000);
            t.setTurnoverRate(stockSnapshot.getTurnoverRate());
            t.setIndustryCode(stockSnapshot.getIndustryCode());
            t.setIndustryName(stockSnapshot.getIndustryName());
            t.setStockType(stockSnapshot.getStockType());
            t.setRegionType(stockSnapshot.getRegionType());
            t.setStockId(stockSnapshot.getStockId());
        });
    }

    @Override
    public ResultT<List<NewStockListResp>> getNewStockInfoList(String[] stockCodeList) {
        List<HKNewStockResp> list = new ArrayList<>();
        if (Objects.isNull(stockCodeList)) {
            return ResultT.success(Collections.emptyList());
        }

        List<HKNewStockResp> redisList = redisClient.get(RedisKeyConstants.BG_HK_NEW_STOCK_LIST);
        if (Objects.isNull(redisList)) {
            return ResultT.success(Collections.emptyList());
        }

        Map<String, HKNewStockResp> stockRespMap = redisList.stream().collect(Collectors.toMap(HKNewStockResp::getSymbol, Function.identity()));

        for (String stockCode : stockCodeList) {
            HKNewStockResp hkNewStockResp = stockRespMap.get(stockCode);
            if(Objects.nonNull(hkNewStockResp)){
                list.add(hkNewStockResp);
            }
        }

        List<NewStockListResp> allFieldList = this.buildAllFieldList(list);
//        allFieldList.sort(Comparator.comparing(NewStockListResp::getChgPct).reversed());
        return ResultT.success(allFieldList);
    }

    @Override
    public Map<String, BigDecimal> getNewStockPrice() {
        Map<String, BigDecimal> map = redisClient.get(RedisKeyConstants.NEW_STOCK_DETAIL_MAP);
        if (MapUtil.isEmpty(map)) {
            return Collections.emptyMap();
        }
        return map;
    }
    /**
     * 获取新股上市状态信息
     * @param marketStatus 状态 0:认购中 1:待公布中签 2:公布中签 3:待上市
     * @return
     */
    @Override
    public List<ComNewStockProspectusVo> getNewStockProspectusList(Integer marketStatus) {
        List<ComNewStockProspectusVo> newStockProspectusVos=new ArrayList<>();
        Integer now = Integer.valueOf(DateUtils.formatDate(new Date(), "yyyyMMdd"));
        //获取认购中、待公布中签、公布中签新股信息
        if (MarketStatusEnum.SUBSCRIBE.getCode().equals(marketStatus)||MarketStatusEnum.WAIT_PUBLISH.getCode().equals(marketStatus)||MarketStatusEnum.PUBLISH.getCode().equals(marketStatus)) {
            newStockProspectusVos= xnhks0503Mapper.findNewStockProspectusInfo(now,marketStatus);
            if (CollUtil.isNotEmpty(newStockProspectusVos)) {
                List<Integer> codes = newStockProspectusVos.stream().map(r -> Integer.valueOf(r.getStockCode().replace(STOCK_SUFFIX, ""))).collect(Collectors.toList());
                //获取招股书
                List<StockUtsNoticeEntity> stockUtsNoticeEntities = informationService.listIpoPDF(codes);
                Map<Integer, List<StockUtsNoticeEntity>> stockUtsNoticeEntityMap = stockUtsNoticeEntities.stream().collect(Collectors.groupingBy(stockUtsNoticeEntity -> stockUtsNoticeEntity.getStockCode()));
                newStockProspectusVos.forEach(o->{
                    List<StockUtsNoticeEntity> stockUtsNoticeEntityList = stockUtsNoticeEntityMap.get(Integer.valueOf(o.getStockCode().replace(STOCK_SUFFIX, "")));
                    if (CollUtil.isNotEmpty(stockUtsNoticeEntityList)) {
                        o.setStockLink(informationService.buildPDF(stockUtsNoticeEntityList,o.getStockCode()));
                    }
                    o.setRegionType(RegionTypeEnum.HK.getCode());
                    o.setMarketStatus(marketStatus);
                });
            }

        }

        return newStockProspectusVos;
    }
    /**
     * 获取新股信息列表
     * @param stockCodes
     * @return
     */
    @Override
    public List<ComNewStockInfoVo> getNewStockInfos(Set<String> stockCodes) {
        List<ComNewStockInfoVo> comNewStockInfoVos=new ArrayList<>();
        if (CollUtil.isNotEmpty(stockCodes)) {
            String[] codes= stockCodes.toArray(new String[stockCodes.size()]);
            ResultT<List<NewStockListResp>> newStockInfoResultT = getNewStockInfoList(codes);
            if (ResultCode.SUCCESS.code()== newStockInfoResultT.getCode()) {
                List<NewStockListResp> usNewStocks = newStockInfoResultT.getData();
                if (CollUtil.isNotEmpty(usNewStocks)) {
                    comNewStockInfoVos = BeanUtil.copyToList(usNewStocks, ComNewStockInfoVo.class);
                }
            }
        }

        return comNewStockInfoVos;
    }

    /**
     * 排序，空字段升序排最前
     */
    private void nullFirstSort(List<NewStockListResp> list,String sortKey,String sort){
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(sortKey, NewStockListResp.class);
            Method readMethod = descriptor.getReadMethod();
            list.sort((t1, t2) -> {
                try {
                    Object result1 = readMethod.invoke(t1);
                    BigDecimal decimal1 = result1 == null ? BigDecimal.valueOf(Long.MIN_VALUE) : new BigDecimal(result1.toString());
                    Object result2 = readMethod.invoke(t2);
                    BigDecimal decimal2 = result2 == null ? BigDecimal.valueOf(Long.MIN_VALUE) : new BigDecimal(result2.toString());
                    if (SortListUtil.ASC.equalsIgnoreCase(sort)) {
                        return decimal1.compareTo(decimal2);
                    } else {
                        return decimal2.compareTo(decimal1);
                    }
                } catch (Exception e) {
                    log.error("执行排序方法失败",e);
                    return 0;
                }
            });
        } catch (Exception e) {
            log.error("排序失败", e);
        }
    }
}
