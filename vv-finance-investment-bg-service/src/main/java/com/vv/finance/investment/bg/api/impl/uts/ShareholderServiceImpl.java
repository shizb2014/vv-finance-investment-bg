package com.vv.finance.investment.bg.api.impl.uts;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.constants.omdc.Adjhkt;
import com.vv.finance.common.utils.BigDecimalUtil;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.uts.IShareholderService;
import com.vv.finance.investment.bg.dto.f10.MainShareholdingReqDTO;
import com.vv.finance.investment.bg.dto.f10.StockHolderChangeReqDTO;
import com.vv.finance.investment.bg.entity.f10.capitalstructure.CapitalStatistics;
import com.vv.finance.investment.bg.entity.f10.enums.F10MainholderTypeEnum;
import com.vv.finance.investment.bg.entity.f10.shareholder.*;
import com.vv.finance.investment.bg.entity.f10.stockMarket.TotalByType;
import com.vv.finance.investment.bg.entity.information.PageWithStockHolder;
import com.vv.finance.investment.bg.entity.uts.Xnhk0102;
import com.vv.finance.investment.bg.enums.StockHolderTypeEnum;
import com.vv.finance.investment.bg.enums.StockShareTypeEnum;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0102Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0129Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0605Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0601Mapper;
import com.vv.finance.investment.bg.stock.f10.mapper.StockHolderChangeMapper;
import com.vv.finance.investment.bg.utils.TimeConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: wsliang
 * @Date: 2021/9/1 10:07
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
public class ShareholderServiceImpl implements IShareholderService {

    @Resource
    private Xnhks0601Mapper xnhks0601Mapper;

    @Resource
    private Xnhk0129Mapper xnhk0129Mapper;

    @Resource
    private Xnhk0605Mapper xnhk0605Mapper;

    @Resource
    private StockHolderChangeMapper stockHolderChangeMapper;

    @Resource
    private HkTradingCalendarApi hkTradingCalendarApi;

    @Resource
    private Xnhk0102Mapper xnhk0102Mapper;

    @Override
    public PopChanges getPopChanges(String code) {
        // if (!checkCode(code)) {
        //     return null;
        // }
        List<TotalByType> totalByTypes = xnhks0601Mapper.totalQuantityByQuaAndType(code);
        List<Val> personalList = new ArrayList<>();
        List<Val> mechanismList = new ArrayList<>();
        List<Val> directorList = new ArrayList<>();
        PopChanges popChanges = PopChanges.builder().directorList(directorList).mechanismList(mechanismList).personalList(personalList).build();
        if (totalByTypes == null || totalByTypes.size() == 0) {
            return popChanges;
        }
        // 获取起止时间 计算横坐标（季度）
        Long startTime = totalByTypes.get(0).getDate();
        Long now = DateUtils.formatDateToLong(new Date(), "yyyyMM");
        Long endTime = Long.valueOf(DateUtils.quarterEndMonth());
        // 预存容器，备份上一季度的数据
        Val prePersonal = Val.builder().num(new BigDecimal(0)).build();
        Val preMechanism = Val.builder().num(new BigDecimal(0)).build();
        Val preDirector = Val.builder().num(new BigDecimal(0)).build();
        while (startTime <= endTime) {
            String type;
            Val personal = null;
            Val mechanism = null;
            Val director = null;
            Long timestamp = null;
            try {
                if (startTime >= now) {
                    timestamp = new SimpleDateFormat("yyyyMM").parse(now.toString()).getTime();
                } else {
                    timestamp = new SimpleDateFormat("yyyyMM").parse(startTime.toString()).getTime();
                }
            } catch (ParseException e) {
                log.error("时间转换异常");
            }
            for (TotalByType t : totalByTypes
            ) {
                type = t.getType();
                if (t.getDate() - startTime != 0) {
                    continue;
                }
                switch (type) {
                    case "1":
                        personal = Val.builder().num(t.getQuantity()).Date(timestamp).build();
                        break;
                    case "2":
                        mechanism = Val.builder().num(t.getQuantity()).Date(timestamp).build();
                        break;
                    case "3A":
                        director = Val.builder().num(t.getQuantity()).Date(timestamp).build();
                        break;
                    default:
                        break;
                }
            }
            if (personal == null) {
                personal = Val.builder().Date(timestamp).num(prePersonal.getNum()).build();
            }
            if (mechanism == null) {
                mechanism = Val.builder().Date(timestamp).num(preMechanism.getNum()).build();
            }
            if (director == null) {
                director = Val.builder().Date(timestamp).num(preDirector.getNum()).build();
            }

            BigDecimal totalNum = personal.getNum().add(mechanism.getNum()).add(director.getNum());
            if (totalNum.compareTo(BigDecimal.ZERO) != 0) {
                personal.setPop(personal.getNum().divide(totalNum, 6, RoundingMode.HALF_UP));
                mechanism.setPop(mechanism.getNum().divide(totalNum, 6, RoundingMode.HALF_UP));
                director.setPop(director.getNum().divide(totalNum, 6, RoundingMode.HALF_UP));
            }
            personalList.add(personal);
            mechanismList.add(mechanism);
            directorList.add(director);
            prePersonal = personal;
            preMechanism = mechanism;
            preDirector = director;
            popChanges.setUpdateDate(timestamp);
            startTime = Long.parseLong(DateUtils.nextQuarter(String.valueOf(startTime)));
        }

        return popChanges;
    }

    @Override
    public StockHolderPopChange getPopChangesV2(String code) {

        // if (!checkCode(code)) {
        //     return null;
        // }

        List<StockHolderChange> holderChanges = stockHolderChangeMapper.listHolderChangesByCode(code);

        if (CollUtil.isEmpty(holderChanges)) {
            return null;
        }

        // 20240102 补点： 当前季度 + 前面7个季度
        List<Long> timestamps = TimeConvertUtil.getEndTimestampsForQuarter(7);
        // 升序排序
        Collections.sort(timestamps);

        // 最大季度
        Long maxQua = holderChanges.stream().max(Comparator.comparing(StockHolderChange::getQuaDate)).map(StockHolderChange::getQuaDate).orElse(TimeConvertUtil.getEndDayOfQuarter());
        // 最新季度设置为当前日期
        holderChanges.stream().filter(hc -> ObjectUtil.equals(maxQua, hc.getQuaDate())).forEach(hc -> hc.setQuaDate(TimeConvertUtil.getYmdByDate()));

        Set<String> holderTypeSet = holderChanges.stream().map(StockHolderChange::getHolderType).collect(Collectors.toSet());
        String holderTypes = StrUtil.join(StrUtil.COMMA, holderTypeSet);

        List<StockHolder> stockHolders = holderChanges.stream().map(hc -> {
            return StockHolder.builder().holderType(hc.getHolderType()).shareType(hc.getShareType()).num(hc.getNum()).pop(hc.getPop()).holderTypes(holderTypes).date(TimeConvertUtil.getTimeStampByYmd(hc.getQuaDate())).build();
        }).collect(Collectors.toList());

        Long updateTime = CollUtil.getLast(stockHolders).getDate();

        Map<String, List<StockHolder>> holderListMap =  stockHolders.stream().collect(
                Collectors.groupingBy(StockHolder::getHolderType,
                        HashMap::new, Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream().sorted(Comparator.comparing(StockHolder::getDate)).collect(Collectors.toList())
                        )
                )
        );

        holderListMap.forEach((type, holders) -> {
            Map<Long, StockHolder> dateHolderMap = holders.stream().collect(Collectors.toMap(StockHolder::getDate, v -> v, (o, v) -> v));
            List<StockHolder> addHolders = timestamps.stream().filter(ts -> !dateHolderMap.containsKey(ts)).map(ts -> StockHolder.builder().shareType(type).date(ts).build()).collect(Collectors.toList());
            List<StockHolder> newHolders = CollUtil.unionAll(addHolders, holders);
            holderListMap.put(type, newHolders);
        });

        return StockHolderPopChange.builder().date(updateTime).records(holderListMap).build();
    }

    @Override
    public StockPopAndChange getStockPopAndChanges(String code, Integer quaSize) {
        List<StockHolderChange> holderChanges = stockHolderChangeMapper.listHolderChangesByCode(code);

        if (CollUtil.isEmpty(holderChanges)) {
            return null;
        }

        // 20240331
        Map<Long, List<StockHolderChange>> holderListMap =  holderChanges.stream().collect(Collectors.groupingBy(StockHolderChange::getQuaDate));

        // 所有类型
        List<String> holderTypeSet = holderChanges.stream().sorted(Comparator.comparing(StockHolderChange::getHolderType)).map(StockHolderChange::getHolderType).distinct().collect(Collectors.toList());

        // 最大季度
        Long maxQua = Opt.ofNullable(CollUtil.max(holderListMap.keySet())).orElse(TimeConvertUtil.getEndDayOfQuarter());

        // 获取最近8个季度
        List<Long> quaDayList = TimeConvertUtil.getEndDayOfQuartersForDate(maxQua, quaSize);

        // 升序排序
        Collections.sort(quaDayList);

        // 遍历并补点
        List<StockPopAndChange.TimeStockHolder> recordList = quaDayList.stream().map(quaDay -> {
            List<StockHolderChange> holderList = holderListMap.getOrDefault(quaDay, new ArrayList<>());
            if (CollUtil.isEmpty(holderList)) {
                // 历史时间点，补点
                holderList.addAll(holderTypeSet.stream().map(type -> StockHolderChange.builder().code(code).quaDate(quaDay).holderType(type).build()).collect(Collectors.toList()));
            } else {
                // 补点，保持长度一致
                Set<String> holderSet = holderList.stream().map(StockHolderChange::getHolderType).collect(Collectors.toSet());
                Collection<String> subtract = CollUtil.subtract(holderTypeSet, holderSet);
                if (CollUtil.isNotEmpty(subtract)) {
                    holderList.addAll(subtract.stream().map(type -> StockHolderChange.builder().code(code).quaDate(quaDay).holderType(type).build()).collect(Collectors.toList()));
                }
            }
            if (ObjectUtil.equals(quaDay, maxQua)) {
                // 最新季度设置为当前日期
                holderList.forEach(hc -> hc.setQuaDate(TimeConvertUtil.getYmdByDate()));
            }
            Long quaStamp = TimeConvertUtil.getTimeStampByYmd(quaDay);
            List<StockHolder> stockHolders = holderList.stream().map(hc -> StockHolder.builder().holderType(hc.getHolderType()).shareType(hc.getShareType()).num(hc.getNum()).pop(hc.getPop()).date(TimeConvertUtil.getTimeStampByYmd(hc.getQuaDate())).build()).collect(Collectors.toList());
            // 排序，使每个日期中类型顺序一致
            List<StockHolder> sortedHolders = CollUtil.sort(stockHolders, Comparator.comparing(sh -> holderTypeSet.indexOf(sh.getHolderType())));
            return StockPopAndChange.TimeStockHolder.builder().time(quaStamp).stockHolders(sortedHolders).build();
        }).collect(Collectors.toList());

        // Long updateTime = TimeConvertUtil.getTimeStampByYmd(CollUtil.getLast(quaDayList));
        Long updateTime = ObjectUtil.defaultIfNull(xnhk0129Mapper.lastUpdateTime(code), TimeConvertUtil::getTimeStampByYmd, System.currentTimeMillis());

        return StockPopAndChange.builder().date(updateTime).records(recordList).holderTypes(new ArrayList<>(holderTypeSet)).build();
    }

    @Override
    public ShareholdingPop percentageByType(String stockCode) {
        // if (!checkCode(stockCode)) {
        //     return null;
        // }
        List<TotalByType> totalByTypes = xnhks0601Mapper.totalQuantityByType(stockCode);
        if (CollectionUtils.isEmpty(totalByTypes)){
            return null;
        }
        Val personal = Val.builder().num(new BigDecimal("0")).build();
        Val mechanism = Val.builder().num(new BigDecimal("0")).build();
        Val director = Val.builder().num(new BigDecimal("0")).build();
        BigDecimal totalNum = BigDecimal.valueOf(0.0);
        Long maxData = 0L;
        String type;
        for (TotalByType t : totalByTypes
        ) {
            type = t.getType();
            totalNum = totalNum.add(t.getQuantity());
            Long tDate = null;
            try {
                tDate = new SimpleDateFormat("yyyyMMdd").parse(t.getDate().toString()).getTime();
            } catch (ParseException e) {
                log.error("时间格式转换异常");
                tDate = System.currentTimeMillis();
            }
            maxData = tDate > maxData ? tDate : maxData;
            switch (type) {
                case "1":
                    personal.setNum(t.getQuantity());
                    personal.setDate(tDate);
                    break;
                case "2":
                    mechanism.setNum(t.getQuantity());
                    mechanism.setDate(tDate);
                    break;
                case "3A":
                    director.setNum(t.getQuantity());
                    director.setDate(tDate);
                    break;
                default:
                    break;
            }
        }
        if (totalNum.compareTo(BigDecimal.ZERO) != 0) {
            personal.setPop(personal.getNum().divide(totalNum, 6, BigDecimal.ROUND_HALF_UP));
            director.setPop(director.getNum().divide(totalNum, 6, BigDecimal.ROUND_HALF_UP));
            mechanism.setPop(mechanism.getNum().divide(totalNum, 6, BigDecimal.ROUND_HALF_UP));
        }
        ShareholdingPop shareholdingPop = ShareholdingPop.builder().personal(personal)
                .mechanism(mechanism).updateDate(maxData)
                .director(director).build();
        return shareholdingPop;
    }

    @Override
    public StockHolderPop percentageByTypeV2(String stockCode) {
        // if (!checkCode(stockCode)) {
        //     return null;
        // }
        List<StockHolder> stockHolders = xnhk0129Mapper.totalQuantityByType(stockCode);

        List<StockHolder> buildHolders = buildStockHolderList(stockCode, false, true, true, stockHolders);

        long maxDateTime = buildHolders.stream().max(Comparator.comparing(StockHolder::getDate)).map(StockHolder::getDate).orElse(System.currentTimeMillis());

        if (CollUtil.isNotEmpty(buildHolders)) {
            Set<String> holderTypeSet = buildHolders.stream().map(StockHolder::getHolderType).collect(Collectors.toSet());
            String holderTypes = StrUtil.join(StrUtil.COMMA, holderTypeSet);
            buildHolders.forEach(bh -> bh.setHolderTypes(holderTypes));
        }

        return StockHolderPop.builder().date(maxDateTime).records(buildHolders).build();
    }

    @Override
    public PageDomain<MainShareholding> getMainShareholding(MainShareholdingReqDTO mainShareholdingReqDTO) {
        long currentPage = mainShareholdingReqDTO.getCurrentPage() == null ? 1L : mainShareholdingReqDTO.getCurrentPage();
        long pageSize = mainShareholdingReqDTO.getPageSize() == null ? 10L : mainShareholdingReqDTO.getPageSize();
        PageDomain<MainShareholding> pageDomain = new PageDomain<>();
        Page<MainShareholding> mainShareholdingPage = new Page<>();
        pageDomain.setCurrent(currentPage);
        pageDomain.setSize(pageSize);
        mainShareholdingPage.setCurrent(currentPage);
        mainShareholdingPage.setSize(pageSize);
        String stockCode = mainShareholdingReqDTO.getStockCode();
        if (!checkCode(stockCode)) {
            return pageDomain;
        }
        Page<MainShareholding> pageResult = xnhks0601Mapper.getMainShareholding(mainShareholdingPage, stockCode);
        pageDomain.setTotal(pageResult.getTotal());
        List<MainShareholding> records = pageResult.getRecords();
        // 插入最新修改时间
        Long updateDate = xnhks0601Mapper.lastUpdateTime(stockCode);
        if (CollectionUtils.isNotEmpty(records) && ObjectUtils.isNotEmpty(updateDate)) {
            try {
                String yyyyMMdd = DateUtils.formatDate(DateUtils.parseDate(updateDate.toString(), "yyyyMMdd"), "yyyy/MM/dd");
                records.stream().forEach(record -> {
                    record.setUpdateDate(yyyyMMdd);
                    record.setType(F10MainholderTypeEnum.getValue(record.getType()));
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        pageDomain.setRecords(records);
        return pageDomain;
    }

    @Override
    public PageWithStockHolder<StockHolder> getMainShareholdingV2(MainShareholdingReqDTO holdingReq) {
        // 所有股东
        String stockCode = holdingReq.getStockCode();
        String holderType = holdingReq.getHolderType();

        long currentPage = holdingReq.getCurrentPage() == null ? 1L : holdingReq.getCurrentPage();
        long pageSize = holdingReq.getPageSize() == null ? 10L : holdingReq.getPageSize();

        PageWithStockHolder<StockHolder> pageDomain = new PageWithStockHolder<>();
        pageDomain.setCurrent(currentPage);
        pageDomain.setSize(pageSize);

        // if (!checkCode(stockCode)) {
        //     return pageDomain;
        // }

        Page<StockHolder> holderPage = new Page<>();
        holderPage.setCurrent(currentPage);
        holderPage.setSize(pageSize);

        Page<StockHolder> pageResult = xnhk0129Mapper.getMainShareholding(holderPage, stockCode, holderType);
        List<StockHolder> records = pageResult.getRecords();
        // 插入最新修改时间
        Long updateDate = xnhk0129Mapper.lastUpdateTime(stockCode);
        if (CollectionUtils.isNotEmpty(records) && ObjectUtils.isNotEmpty(updateDate)) {
            Long updateTimestamp = TimeConvertUtil.getTimeStampByYmd(updateDate);
            Set<String> holderTypeSet = records.stream().map(StockHolder::getHolderType).collect(Collectors.toSet());
            String holderTypes = StrUtil.join(StrUtil.COMMA, holderTypeSet);
            records.forEach(sh -> {
                sh.setDate(updateTimestamp);
                sh.setHolderTypes(holderTypes);
                sh.setPop(NumberUtil.div(sh.getPop(), BigDecimal.valueOf(100)));
            });
            pageDomain.setDate(updateTimestamp);
            pageDomain.setHolderTypes(new ArrayList<>(holderTypeSet));
        }

        pageDomain.setRecords(records);
        pageDomain.setTotal(pageResult.getTotal());
        return pageDomain;
    }

    @Override
    public List<StockHolder> buildStockHolderList(String stockCode, boolean setShare, boolean formatDate, boolean addOther, List<StockHolder> stockHolders) {
        // CapitalStatistics capitalStructure = xnhk0605Mapper.getCapitalStructure(stockCode);
        Xnhk0102 xnhk0102 = xnhk0102Mapper.selectStockMarketData(stockCode);
        if (ObjectUtil.isEmpty(xnhk0102) || BigDecimalUtil.isNullOrZero(xnhk0102.getF070n())) {
            log.warn("ShareholderService buildStockHolderList capitalStructure|issuedCirculating is null or zero! code: {}", stockCode);
            return stockHolders;
        }

        // 已发行股本数量
        BigDecimal issuedCirculating = xnhk0102.getF070n();
        // String updateDate = capitalStructure.getUpdateDate();
        Long updateDate = stockHolders.stream().max(Comparator.comparing(StockHolder::getDate)).map(StockHolder::getDate).orElse(Long.parseLong(DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_FORMAT)));

        // 过滤掉持股数量为0的股东
        stockHolders = stockHolders.stream().filter(sh -> ObjectUtil.isNotEmpty(sh.getNum()) && NumberUtil.isGreater(sh.getNum(), BigDecimal.ZERO)).collect(Collectors.toList());

        // 计算占比
        stockHolders.forEach(sh -> {
            sh.setPop(sh.getNum().divide(issuedCirculating, 4, RoundingMode.HALF_UP));
            if (formatDate) {
                // sh.setDate(DateUtil.parse(String.valueOf(sh.getDate()), DatePattern.PURE_DATE_FORMAT).getTime());
                sh.setDate(TimeConvertUtil.getTimeStampByYmd(sh.getDate()));
            }
        });

        BigDecimal totalQuantity = BigDecimal.valueOf(stockHolders.stream().mapToDouble(sh -> sh.getNum().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPercent = BigDecimal.valueOf(stockHolders.stream().mapToDouble(sh -> sh.getPop().doubleValue()).sum()).setScale(4, RoundingMode.HALF_UP);

        BigDecimal otherQuantity = NumberUtil.sub(issuedCirculating, totalQuantity);
        BigDecimal otherPercent = NumberUtil.sub(BigDecimal.ONE, totalPercent);

        // 其他类型
        if (addOther && NumberUtil.isGreater(otherQuantity, BigDecimal.ZERO)) {
            long date = formatDate ? TimeConvertUtil.getTimeStampByYmd(updateDate) : updateDate;
            stockHolders.add(StockHolder.builder().num(otherQuantity).pop(otherPercent).holderType(StockHolderTypeEnum.OTHER.getCode())
                    .shareType(setShare ? StockHolderTypeEnum.OTHER.getCode() : null).date(date).build());
        }

        return stockHolders;
    }

    @Override
    public PageDomain<MainShareholding> getMainShareholding(String stockCode, SimplePageReq simplePageReq) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();
        PageDomain<MainShareholding> pageDomain = new PageDomain<>();
        Page<MainShareholding> mainShareholdingPage = new Page<>();
        pageDomain.setCurrent(currentPage);
        pageDomain.setSize(pageSize);
        mainShareholdingPage.setCurrent(currentPage);
        mainShareholdingPage.setSize(pageSize);
        if (!checkCode(stockCode)) {
            return pageDomain;
        }
        Page<MainShareholding> pageResult = xnhks0601Mapper.getMainShareholding(mainShareholdingPage, stockCode);
        pageDomain.setTotal(pageResult.getTotal());
        List<MainShareholding> records = pageResult.getRecords();
        // 插入最新修改时间
        Long updateDate = xnhks0601Mapper.lastUpdateTime(stockCode);
        if (CollectionUtils.isNotEmpty(records) && ObjectUtils.isNotEmpty(updateDate)) {
            try {
                String yyyyMMdd = DateUtils.formatDate(DateUtils.parseDate(updateDate.toString(), "yyyyMMdd"), "yyyy/MM/dd");
                records.stream().forEach(record -> {
                    record.setUpdateDate(yyyyMMdd);
                    record.setType(F10MainholderTypeEnum.getValue(record.getType()));
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        pageDomain.setRecords(records);
        return pageDomain;
    }

    @Override
    public PageDomain<EquityChange> listEquityChanges(String stockCode, SimplePageReq simplePageReq) {
        PageDomain<EquityChange> pageDomain = new PageDomain<>();
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();
        if (!checkCode(stockCode)) {
            return pageDomain;
        }
        Page<EquityChange> equityChangePage = new Page<>();
        equityChangePage.setCurrent(currentPage);
        equityChangePage.setSize(pageSize);
        Page<EquityChange> pageResult = xnhks0601Mapper.listEquityChanges(equityChangePage, stockCode);

        pageDomain.setCurrent(pageResult.getCurrent());
        pageDomain.setSize(pageResult.getSize());
        pageDomain.setTotal(pageResult.getTotal());
        List<EquityChange> records = pageResult.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            for (EquityChange record : records) {
                try {
                    record.setReleaseDate(DateUtils.formatDate(DateUtils.parseDate(record.getReleaseDate() + "", "yyyyMMdd"), "yyyy/MM/dd"));
                } catch (ParseException e) {
                    log.error("时间转换异常！");
                }
            }
        }
        pageDomain.setRecords(records);

        return pageDomain;
    }

    @Override
    public PageDomain<EquityChange> listEquityChangesV2(StockHolderChangeReqDTO changeReqDTO) {
        PageDomain<EquityChange> pageDomain = new PageDomain<>();
        long currentPage = changeReqDTO.getCurrentPage() == null ? 1L : changeReqDTO.getCurrentPage();
        long pageSize = changeReqDTO.getPageSize() == null ? 10L : changeReqDTO.getPageSize();
        String stockCode = changeReqDTO.getStockCode();
        String positionKey = changeReqDTO.getPositionKey();
        // if (!checkCode(stockCode)) {
        //     return pageDomain;
        // }
        Page<EquityChange> equityChangePage = new Page<>();
        equityChangePage.setCurrent(currentPage);
        equityChangePage.setSize(pageSize);
        Page<EquityChange> pageResult = xnhks0601Mapper.listEquityChangesV2(equityChangePage, stockCode, positionKey);

        pageDomain.setCurrent(pageResult.getCurrent());
        pageDomain.setSize(pageResult.getSize());
        pageDomain.setTotal(pageResult.getTotal());
        List<EquityChange> records = pageResult.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            for (EquityChange record : records) {
                try {
                    record.setReleaseDate(DateUtils.formatDate(DateUtils.parseDate(record.getReleaseDate() + "", "yyyyMMdd"), "yyyy/MM/dd"));
                } catch (ParseException e) {
                    log.error("时间转换异常！");
                }
            }
        }
        pageDomain.setRecords(records);

        return pageDomain;
    }

    private Boolean checkCode(String code) {
        return code.matches("^\\d{5}.hk");
    }

    @Override
    public List<StockHoldingRiseFall> getTotalStockHolderChangeAmount(String stockCode, Long startQua, Long endQua) {
        return xnhks0601Mapper.totalStockHolderChangeAmount(stockCode, startQua, endQua);
    }
}
