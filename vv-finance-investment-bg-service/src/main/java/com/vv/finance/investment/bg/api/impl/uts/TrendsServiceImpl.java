package com.vv.finance.investment.bg.api.impl.uts;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.MapUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taobao.api.internal.toplink.endpoint.Identity;
import com.vv.finance.auth.api.ISysDicDateService;
import com.vv.finance.auth.domain.vo.SysDictData;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.base.utils.ZoneDateUtils;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.quotation.f10.*;
import com.vv.finance.common.enums.ComCompanyTrendsType;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.common.us.utils.UsDateUtils;
import com.vv.finance.common.utils.ConcatCodeUtil;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.f10.F10StockInformationApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.uts.TrendsService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.dto.f10.IncrementCompanyEventDTO;
import com.vv.finance.investment.bg.dto.stock.TypeSxdbmask;
import com.vv.finance.investment.bg.entity.f10.trends.*;
import com.vv.finance.investment.bg.entity.information.*;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.handler.trends.*;
import com.vv.finance.investment.bg.mapper.stock.quotes.CompanyTrandsMergeMapper;
import com.vv.finance.investment.bg.mapper.stock.quotes.StockNewsMapper;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.stock.f10.service.impl.AbstractBaseServiceImpl;
import com.vv.finance.investment.bg.stock.info.BrokerStatistics;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.stock.information.InformationConstant;
import com.vv.finance.investment.bg.stock.information.enun.CompanyTrendsType;
import com.vv.finance.investment.bg.stock.information.factory.CompanyTrendsFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.curator.shaded.com.google.common.collect.Sets;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/20 11:40
 * @Version 1.0
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
public class TrendsServiceImpl extends AbstractBaseServiceImpl implements TrendsService {

    @Resource
    StopAndResumeMapper stopAndResumeMapper;
    @Resource
    F10EventMapper f10EventMapper;
    @Resource
    Xnhks0308Mapper xnhks0308Mapper;
    @Resource
    Xnhks0309Mapper xnhks0309Mapper;
    @Resource
    Xnhks0310Mapper xnhks0310Mapper;
    @Resource
    Xnhk0311Mapper xnhk0311Mapper;
    @Resource
    Xnhks0314Mapper xnhks0314Mapper;
    @Resource
    Xnhks0112Mapper xnhks0112Mapper;
    @Resource
    Xnhk0602Mapper xnhk0602Mapper;
    @Resource
    F10StockInformationApi f10StockInformationApi;
    @Resource
    Xnhks0320Mapper xnhks0320Mapper;
    @Resource
    CompanyTrandsMergeMapper companyTrandsMergeMapper;
    @Resource
    private Xnhks0317Mapper xnhks0317Mapper;

    @Autowired
    Xnhk0127Mapper xnhk0127Mapper;

    @Autowired
    Xnhk0201Mapper xnhk0201Mapper;

    @Autowired
    Xnhk0204Mapper xnhk0204Mapper;

    @Autowired
    Xnhk0207Mapper xnhk0207Mapper;

    @Resource
    Xnhks0101Mapper xnhks0101Mapper;

    @Autowired
    IStockDefineService stockDefineService;


    @DubboReference(group = "${dubbo.investment.auth.service.group:auth}", registry = "authservice")
    ISysDicDateService sysDicDateService;


    @Resource
    XNHK0127Handler xnhk0127Handler;

    @Resource
    XNHK0201Handler xnhk0201Handler;

    @Resource
    XNHK0204Handler xnhk0204Handler;

    @Resource
    XNHK0207Handler xnhk0207Handler;
    @Resource
    XNHK0311Handler xnhk0311Handler;
    @Resource
    XNHK0318Handler xnhk0318Handler;
    @Resource
    XNHKS0308Handler xnhks0308Handler;
    @Resource
    XNHKS0310Handler xnhks0310Handler;
    @Resource
    XNHKS0314Handler xnhks0314Handler;
    @Resource
    XNHKS0317Handler xnhks0317Handler;
    @Resource
    StockInfoApi stockInfoApi;
    @Resource
    private Xnhks0503Mapper xnhks0503Mapper;
    @Resource
    private StockNewsMapper stockNewsMapper;




    @Override
    public PageDomain<StopAndResume> getStopAndResume(
            List<String> stockCodes,
            SimplePageReq simplePageReq,
            Long time
    ) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();
        if (CollectionUtil.isEmpty(stockCodes) || !checkCodes(stockCodes)) {
            return new PageDomain<>();
        }
        Long date = null;
        if (time != null) {
            date = DateUtils.formatDateToLong(new Date(time), null);
        }
        Page<StopAndResume> stopAndResumePage = new Page<>();
        stopAndResumePage.setCurrent(currentPage);
        stopAndResumePage.setSize(pageSize);
        Page<StopAndResume> pageResult =
                stopAndResumeMapper.getStopAndResume(stopAndResumePage, arrayToStr(stockCodes), date);
        PageDomain<StopAndResume> pageDomain = new PageDomain<>();
        pageDomain.setCurrent(pageResult.getCurrent());
        pageDomain.setSize(pageResult.getSize());
        pageDomain.setTotal(pageResult.getTotal());
        pageDomain.setRecords(pageResult.getRecords());
        return pageDomain;
    }

    @Override
    public PageDomain<AcquisitionsAndMergers> getAcquisitionsAndMergers(
            List<String> stockCodes,
            SimplePageReq simplePageReq,
            Long time
    ) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();
        if (CollectionUtil.isEmpty(stockCodes) || !checkCodes(stockCodes)) {
            return new PageDomain<>();
        }

        Page<Xnhks0308> xnhks0308Page = new Page<>();
        xnhks0308Page.setCurrent(currentPage);
        xnhks0308Page.setSize(pageSize);
        QueryWrapper<Xnhks0308> queryWrapper =
                new QueryWrapper<Xnhks0308>().in("SECCODE", stockCodes).orderByDesc("F001D");
        if (time != null) {
            Long date = DateUtils.formatDateToLong(new Date(time), null);
            queryWrapper = queryWrapper.and(
                    wrapper -> wrapper.eq("F001D", date).or().eq("F002D", date).or().eq("F004D", date).or()
                            .eq("F005D", date));
        }
        Page<Xnhks0308> xnhks0308PageResult = xnhks0308Mapper.selectPage(xnhks0308Page, queryWrapper);
        List<Xnhks0308> xnhks0308List = xnhks0308PageResult.getRecords();

        List<AcquisitionsAndMergers> acquisitionsAndMergers = new ArrayList<>();
        xnhks0308List.forEach(t -> {
            AcquisitionsAndMergers andMergers = AcquisitionsAndMergers.builder()
                    .releaseDate(t.getF001d())
                    .documentsAndOfferDate(t.getF002d())
                    .offerClosingDate(t.getF004d())
                    .sendOfferAmountDate(t.getF005d())
                    .eventDetail(t.getF006v())
                    .stockCode(t.getSeccode())
                    .build();
            acquisitionsAndMergers.add(andMergers);
        });
        PageDomain<AcquisitionsAndMergers> reault = new PageDomain<>();
        reault.setTotal(xnhks0308PageResult.getTotal());
        reault.setCurrent(xnhks0308PageResult.getCurrent());
        reault.setSize(xnhks0308PageResult.getSize());
        reault.setRecords(acquisitionsAndMergers);
        return reault;
    }

    @Override
    public PageDomain<CompanyRecombination> getCompanyRecombination(
            List<String> stockCodes,
            SimplePageReq simplePageReq,
            Long time
    ) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();
        if (CollectionUtil.isEmpty(stockCodes) || !checkCodes(stockCodes)) {
            return null;
        }
        Page<Xnhks0309> xnhks0309Page = new Page<>();
        xnhks0309Page.setCurrent(currentPage);
        xnhks0309Page.setSize(pageSize);
        QueryWrapper<Xnhks0309> queryWrapper =
                new QueryWrapper<Xnhks0309>().in("SECCODE", stockCodes).orderByDesc("F001D");
        if (time != null) {
            Long date = DateUtils.formatDateToLong(new Date(time), null);
            queryWrapper =
                    queryWrapper.and(wrapper -> wrapper.eq("F001D", date).or().eq("F002D", date).or().eq("F003D", date));
        }
        Page<Xnhks0309> xnhks0309PageResult = xnhks0309Mapper.selectPage(xnhks0309Page, queryWrapper);
        List<Xnhks0309> xnhks0309List = xnhks0309PageResult.getRecords();

        List<CompanyRecombination> companyRecombinations = new ArrayList<>();
        xnhks0309List.forEach(t -> {
            CompanyRecombination companyRecombination = CompanyRecombination.builder()
                    .releaseDate(t.getF001d())
                    .adviseDate(t.getF002d())
                    .successDate(t.getF003d())
                    .eventDetail(t.getF004v())
                    .stockCode(t.getSeccode())
                    .build();
            companyRecombinations.add(companyRecombination);
        });
        PageDomain<CompanyRecombination> reault = new PageDomain<>();
        reault.setTotal(xnhks0309PageResult.getTotal());
        reault.setCurrent(xnhks0309PageResult.getCurrent());
        reault.setSize(xnhks0309PageResult.getSize());
        reault.setRecords(companyRecombinations);
        return reault;
    }

    @Override
    public PageDomain<GeneralMeeting> getGeneralMeeting(
            List<String> stockCodes,
            SimplePageReq simplePageReq,
            Long time
    ) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();
        if (CollectionUtil.isEmpty(stockCodes) || !checkCodes(stockCodes)) {
            return null;
        }
        Long date = null;
        if (time != null) {
            date = DateUtils.formatDateToLong(new Date(time), null);
        }

        Page<GeneralMeeting> generalMeetingPage = new Page<>();
        generalMeetingPage.setCurrent(currentPage);
        generalMeetingPage.setSize(pageSize);
        generalMeetingPage = xnhks0310Mapper.listJoin0009(generalMeetingPage, arrayToStr(stockCodes), date);

//        Page<Xnhks0310> xnhks0310Page = new Page<>();
//        xnhks0310Page.setCurrent(currentPage);
//        xnhks0310Page.setSize(pageSize);
//        QueryWrapper<Xnhks0310> queryWrapper =
//            new QueryWrapper<Xnhks0310>().in("SECCODE", stockCodes).orderByDesc("F001D");
//        if (stockTime != null) {
//            queryWrapper = queryWrapper.and(wrapper -> wrapper.eq("F001D", date).or().eq("F003D", date));
//        }
//        Page<Xnhks0310> xnhks0310PageResult = xnhks0310Mapper.selectPage(xnhks0310Page, queryWrapper);
//        List<Xnhks0310> xnhks0310List = xnhks0310PageResult.getRecords();
//        List<GeneralMeeting> generalMeetingList = new ArrayList<>();
//        xnhks0310List.forEach(t -> {
//            GeneralMeeting generalMeeting =
//                GeneralMeeting.builder().releaseDate(t.getF001d()).eventNo(t.getF002v()).meetingDate(t.getF003d())
//                    .meetingType(t.getF005v()).eventDetail(t.getF006v()).stockCode(t.getSeccode()).build();
//            generalMeetingList.add(generalMeeting);
//        });
        PageDomain<GeneralMeeting> reault = new PageDomain<>();
        reault.setTotal(generalMeetingPage.getTotal());
        reault.setCurrent(generalMeetingPage.getCurrent());
        reault.setSize(generalMeetingPage.getSize());
        reault.setRecords(generalMeetingPage.getRecords());
        return reault;
    }

    @Override
    public PageDomain<TransactionAlert> getTransactionAlert(
            List<String> stockCodes,
            SimplePageReq simplePageReq,
            Long time
    ) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();
        if (CollectionUtil.isEmpty(stockCodes) || !checkCodes(stockCodes)) {
            return null;
        }
        Page<Xnhk0311> xnhk0311Page = new Page<>();
        xnhk0311Page.setCurrent(currentPage);
        xnhk0311Page.setSize(pageSize);

        Long date = null;
        if (time != null) {
            date = DateUtils.formatDateToLong(new Date(time), null);
        }
        Page<TransactionAlert> transactionAlertPage = new Page<>();
        transactionAlertPage.setCurrent(currentPage);
        transactionAlertPage.setSize(pageSize);
        transactionAlertPage = xnhk0311Mapper.listJoin0007And0006(transactionAlertPage, arrayToStr(stockCodes), date);

//        QueryWrapper<Xnhk0311> queryWrapper =
//                new QueryWrapper<Xnhk0311>().in("SECCODE", stockCodes).orderByDesc("F001D");
//        if (stockTime != null) {
//            Long date = DateUtils.formatDateToLong(new Date(stockTime), null);
//            queryWrapper = queryWrapper.and(wrapper -> wrapper.eq("F001D", date));
//        }
//
//        Page<Xnhk0311> xnhk0311PageResult = xnhk0311Mapper.selectPage(xnhk0311Page, queryWrapper);
//        List<Xnhk0311> xnhk0311List = xnhk0311PageResult.getRecords();
//
//        List<TransactionAlert> transactionAlertList = new ArrayList<>();
//        xnhk0311List.forEach(t -> {
//            TransactionAlert transactionAlert =
//                    TransactionAlert.builder().stockCode(t.getSeccode()).releaseDate(t.getF001d()).alertType(t.getF002v())
//                            .alertCode(t.getF003v()).closingPrice(t.getF004n()).change(t.getF005n()).turnover(t.getF006n())
//                            .build();
//            transactionAlertList.add(transactionAlert);
//        });
        PageDomain<TransactionAlert> reault = new PageDomain<>();
        reault.setTotal(transactionAlertPage.getTotal());
        reault.setCurrent(transactionAlertPage.getCurrent());
        reault.setSize(transactionAlertPage.getSize());
        reault.setRecords(transactionAlertPage.getRecords());
        return reault;
    }

    @Override
    public PageDomain<TransactionParallelism> getTransactionParallelism(
            List<String> stockCodes,
            SimplePageReq simplePageReq,
            Long time
    ) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();
        if (CollectionUtil.isEmpty(stockCodes) || !checkCodes(stockCodes)) {
            return new PageDomain<>();
        }
        Page<Xnhks0314> xnhks0314Page = new Page<>();
        xnhks0314Page.setCurrent(currentPage);
        xnhks0314Page.setSize(pageSize);
        QueryWrapper<Xnhks0314> queryWrapper =
                new QueryWrapper<Xnhks0314>().in("SECCODE", stockCodes).orderByDesc("F001D");

        if (time != null) {
            Long date = DateUtils.formatDateToLong(new Date(time), null);
            queryWrapper = queryWrapper.and(wrapper -> wrapper.eq("F001D", date).or().eq("F006d", date));
        }
        Page<Xnhks0314> xnhks0314PageResult = xnhks0314Mapper.selectPage(xnhks0314Page, queryWrapper);
        List<Xnhks0314> xnhks0314List = xnhks0314PageResult.getRecords();

        List<TransactionParallelism> transactionParallelismList = new ArrayList<>();
        xnhks0314List.forEach(t -> {
            TransactionParallelism transactionParallelism =
                    TransactionParallelism.builder().stockCode(t.getSeccode()).releaseDate(t.getF001d())
                            .securitiesCode(ConcatCodeUtil.concatCode(t.getF003v())).securitiesName(t.getF005v())
                            .unit(t.getF004n()).reason(t.getF010v()).startDate(t.getF006d()).suspendDate(t.getF008v())
                            .tradingDay(t.getF009v()).build();
            transactionParallelismList.add(transactionParallelism);
        });
        PageDomain<TransactionParallelism> reault = new PageDomain<>();
        reault.setTotal(xnhks0314PageResult.getTotal());
        reault.setCurrent(xnhks0314PageResult.getCurrent());
        reault.setSize(xnhks0314PageResult.getSize());
        reault.setRecords(transactionParallelismList);
        return reault;
    }

    @Override
    public PageDomain<String> listByPageF10Event(
            List<String> stockCodes,
            SimplePageReq simplePageReq,
            Long time
    ) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();
        if (CollectionUtil.isEmpty(stockCodes) || !checkCodes(stockCodes)) {
            return null;
        }
        Page<String> f10Page = new Page<>();
        f10Page.setCurrent(currentPage);
        f10Page.setSize(pageSize);
        Long date = DateUtils.formatDateToLong(new Date(time), null);
        Page<String> pageResult = f10EventMapper.listByPageF10Event(f10Page, arrayToStr(stockCodes), date);
        PageDomain<String> res = new PageDomain<>();
        res.setTotal(pageResult.getTotal());
        res.setCurrent(pageResult.getCurrent());
        res.setSize(pageResult.getSize());
        res.setRecords(pageResult.getRecords());
        return res;
    }

    @Override
    public Set<String> listCompanyEventStockCodes(Long time) {
        Long date = DateUtils.formatDateToLong(new Date(time), null);
        Set<String> companyEventStockCodes = new HashSet<>();
        QueryWrapper<Xnhks0314> queryWrapper =
                new QueryWrapper<Xnhks0314>().and(wrapper -> wrapper.eq("F001D", date).or().eq("F006d", date));
        Set<String> transactionParallelismCodes =
                xnhks0314Mapper.selectList(queryWrapper).stream().map(Xnhks0314::getSeccode).collect(Collectors.toSet());

        Set<String> stopAndResumeStockCodes =
                stopAndResumeMapper.getStopAndResumeStockCodes(date).stream().map(StopAndResume::getStockCode)
                        .collect(Collectors.toSet());

        QueryWrapper<Xnhks0308> queryWrapper1 = new QueryWrapper<Xnhks0308>().and(
                wrapper -> wrapper.eq("F001D", date).or().eq("F002D", date).or().eq("F004D", date).or().eq("F005D", date));
        Set<String> acquisitionsAndMergersCodes =
                xnhks0308Mapper.selectList(queryWrapper1).stream().map(Xnhks0308::getSeccode).collect(Collectors.toSet());

        QueryWrapper<Xnhks0310> queryWrapper2 =
                new QueryWrapper<Xnhks0310>().and(wrapper -> wrapper.eq("F001D", date).or().eq("F003D", date));
        Set<String> generalMeetingCodes =
                xnhks0310Mapper.selectList(queryWrapper2).stream().map(Xnhks0310::getSeccode).collect(Collectors.toSet());

        QueryWrapper<Xnhk0311> queryWrapper3 = new QueryWrapper<Xnhk0311>().eq("F001D", date);
        Set<String> transactionAlertCodes =
                xnhk0311Mapper.selectList(queryWrapper3).stream().map(Xnhk0311::getSeccode).collect(Collectors.toSet());

        QueryWrapper<Xnhks0309> queryWrapper4 = new QueryWrapper<Xnhks0309>()
                .and(wrapper -> wrapper.eq("F001D", date).or().eq("F002D", date).or().eq("F003D", date));
        Set<String> companyRecombinationsCodes =
                xnhks0309Mapper.selectList(queryWrapper4).stream().map(Xnhks0309::getSeccode).collect(Collectors.toSet());

        List<String> f10EventCodes = f10EventMapper.listF10EventCodes(date);

        companyEventStockCodes.addAll(companyRecombinationsCodes);
        companyEventStockCodes.addAll(f10EventCodes);
        companyEventStockCodes.addAll(transactionAlertCodes);
        companyEventStockCodes.addAll(acquisitionsAndMergersCodes);
        companyEventStockCodes.addAll(transactionParallelismCodes);
        companyEventStockCodes.addAll(stopAndResumeStockCodes);
        companyEventStockCodes.addAll(generalMeetingCodes);
        return companyEventStockCodes;
    }

    @Override
    public List<IncrementCompanyEventDTO> listIncrementCompanyEventStockCodeAndTime(
            Long beginTime,
            Long endTime
    ) {
        Set<IncrementCompanyEventDTO> dtos = new TreeSet<>((dto1, dto2) -> {
            int count = 1;
            if (StringUtils.equals(dto1.getStockCode(), dto2.getStockCode()) && dto2.getTime().equals(dto2.getTime())) {
                count = 0;
            }
            return count;
        });
        handleIncrementTransactionParallelismCode(beginTime, endTime, dtos);

        handleIncrementStopAndResume(beginTime, endTime, dtos);

        handleIncrementAcquisitionsAndMergers(beginTime, endTime, dtos);

        handleIncrementGeneralMeeting(beginTime, endTime, dtos);

        handleTransactionAlert(beginTime, endTime, dtos);

        handleCompanyRecombinations(beginTime, endTime, dtos);

        f10EventMapper.listIncrementF10(new Date(beginTime), new Date(endTime)).forEach(dtos::add);

        return new ArrayList(dtos);
    }

    private void handleCompanyRecombinations(
            Long beginTime,
            Long endTime,
            Set<IncrementCompanyEventDTO> dtos
    ) {
        QueryWrapper<Xnhks0309> queryWrapper4 = new QueryWrapper<Xnhks0309>().and(
                a -> a.ge("Create_Date", new Date(beginTime)).le("Create_Date", new Date(endTime)).or()
                        .ge("Modified_Date", new Date(beginTime)).le("Modified_Date", new Date(endTime)));
        xnhks0309Mapper.selectList(queryWrapper4).forEach(it -> {
            IncrementCompanyEventDTO dto1 = new IncrementCompanyEventDTO();
            dto1.setStockCode(it.getSeccode());
            dto1.setTime(it.getF001d());

            IncrementCompanyEventDTO dto2 = new IncrementCompanyEventDTO();
            dto2.setStockCode(it.getSeccode());
            dto2.setTime(it.getF002d());

            IncrementCompanyEventDTO dto3 = new IncrementCompanyEventDTO();
            dto3.setStockCode(it.getSeccode());
            dto3.setTime(it.getF003d());

            dtos.add(dto1);
            dtos.add(dto2);
            dtos.add(dto3);
        });
    }

    private void handleTransactionAlert(
            Long beginTime,
            Long endTime,
            Set<IncrementCompanyEventDTO> dtos
    ) {
        QueryWrapper<Xnhk0311> queryWrapper3 = new QueryWrapper<Xnhk0311>().and(
                a -> a.ge("Create_Date", new Date(beginTime)).le("Create_Date", new Date(endTime)).or()
                        .ge("Modified_Date", new Date(beginTime)).le("Modified_Date", new Date(endTime)));
        xnhk0311Mapper.selectList(queryWrapper3).forEach(it -> {
            IncrementCompanyEventDTO dto1 = new IncrementCompanyEventDTO();
            dto1.setStockCode(it.getSeccode());
            dto1.setTime(it.getF001d());
            dtos.add(dto1);
        });
    }

    private void handleIncrementGeneralMeeting(
            Long beginTime,
            Long endTime,
            Set<IncrementCompanyEventDTO> dtos
    ) {
        QueryWrapper<Xnhks0310> queryWrapper2 = new QueryWrapper<Xnhks0310>().and(
                a -> a.ge("Create_Date", new Date(beginTime)).le("Create_Date", new Date(endTime)).or()
                        .ge("Modified_Date", new Date(beginTime)).le("Modified_Date", new Date(endTime)));
        xnhks0310Mapper.selectList(queryWrapper2).forEach(it -> {
            if (it.getF001d() != null) {
                IncrementCompanyEventDTO dto1 = new IncrementCompanyEventDTO();
                dto1.setStockCode(it.getSeccode());
                dto1.setTime(it.getF001d());
                dtos.add(dto1);
            }

            if (it.getF003d() != null) {
                IncrementCompanyEventDTO dto2 = new IncrementCompanyEventDTO();
                dto2.setStockCode(it.getSeccode());
                dto2.setTime(it.getF003d());
                dtos.add(dto2);
            }
        });
    }

    private void handleIncrementAcquisitionsAndMergers(
            Long beginTime,
            Long endTime,
            Set<IncrementCompanyEventDTO> dtos
    ) {
        QueryWrapper<Xnhks0308> queryWrapper1 = new QueryWrapper<Xnhks0308>().and(
                a -> a.ge("Create_Date", new Date(beginTime)).le("Create_Date", new Date(endTime)).or()
                        .ge("Modified_Date", new Date(beginTime)).le("Modified_Date", new Date(endTime)));
        xnhks0308Mapper.selectList(queryWrapper1).forEach(it -> {
            if (it.getF001d() != null) {
                IncrementCompanyEventDTO dto1 = new IncrementCompanyEventDTO();
                dto1.setStockCode(it.getSeccode());
                dto1.setTime(it.getF001d());
                dtos.add(dto1);
            }

            if (it.getF002d() != null) {
                IncrementCompanyEventDTO dto2 = new IncrementCompanyEventDTO();
                dto2.setStockCode(it.getSeccode());
                dto2.setTime(it.getF002d());
                dtos.add(dto2);
            }

            if (it.getF004d() != null) {
                IncrementCompanyEventDTO dto3 = new IncrementCompanyEventDTO();
                dto3.setStockCode(it.getSeccode());
                dto3.setTime(it.getF004d());
                dtos.add(dto3);
            }

            if (it.getF005d() != null) {
                IncrementCompanyEventDTO dto4 = new IncrementCompanyEventDTO();
                dto4.setStockCode(it.getSeccode());
                dto4.setTime(it.getF005d());
                dtos.add(dto4);
            }
        });
    }

    private void handleIncrementStopAndResume(
            Long beginTime,
            Long endTime,
            Set<IncrementCompanyEventDTO> dtos
    ) {
        stopAndResumeMapper.getIncrementStopAndResume(new Date(beginTime), new Date(endTime)).forEach(it -> {
            if (it.getResumeDate() != null) {
                IncrementCompanyEventDTO dto1 = new IncrementCompanyEventDTO();
                dto1.setStockCode(it.getStockCode());
                dto1.setTime(it.getResumeDate());
                dtos.add(dto1);
            }

            if (it.getReleaseDate() != null) {
                IncrementCompanyEventDTO dto2 = new IncrementCompanyEventDTO();
                dto2.setStockCode(it.getStockCode());
                dto2.setTime(it.getReleaseDate());
                dtos.add(dto2);
            }

            if (it.getStopDate() != null) {
                IncrementCompanyEventDTO dto3 = new IncrementCompanyEventDTO();
                dto3.setStockCode(it.getStockCode());
                dto3.setTime(it.getStopDate());
                dtos.add(dto3);
            }
        });
    }

    private void handleIncrementTransactionParallelismCode(
            Long beginTime,
            Long endTime,
            Set<IncrementCompanyEventDTO> dtos
    ) {
        QueryWrapper<Xnhks0314> queryWrapper = new QueryWrapper<Xnhks0314>().and(
                a -> a.ge("Create_Date", new Date(beginTime)).le("Create_Date", new Date(endTime)).or()
                        .ge("Modified_Date", new Date(beginTime)).le("Modified_Date", new Date(endTime)));
        xnhks0314Mapper.selectList(queryWrapper).forEach(it -> {
            if (it.getF001d() != null) {
                IncrementCompanyEventDTO dto1 = new IncrementCompanyEventDTO();
                dto1.setStockCode(it.getSeccode());
                dto1.setTime(it.getF001d());
                dtos.add(dto1);
            }

            if (it.getF006d() != null) {
                IncrementCompanyEventDTO dto2 = new IncrementCompanyEventDTO();
                dto2.setStockCode(it.getSeccode());
                dto2.setTime(it.getF006d());
                dtos.add(dto2);
            }
        });
    }

    private Boolean checkCodes(List<String> codes) {
        for (String code : codes) {
            if (!checkCode(code)) {
                return false;
            }
        }
        return true;
    }

    private Boolean checkCode(String code) {
        return code.matches("^\\d{5}.hk");
    }

    private String arrayToStr(List<String> codes) {
        String join = StringUtils.join(codes, "','");
        return "('" + join + "')";
    }

    @Override
    public List<CompanyTrendAppVo> getRecentByCode(String stockCode) {
        SimplePageReq simplePageReq = new SimplePageReq();
        simplePageReq.setPageSize(5L);
        simplePageReq.setCurrentPage(1L);
        ResultT<PageDomain<CompanyTrendAppVo>> pageDomainResultT = pageCompanyTrends(simplePageReq, stockCode);
        List<CompanyTrendAppVo> companyTrendDtos = new ArrayList<>();
        if (pageDomainResultT.getCode() == ResultT.success().getCode()) {
            companyTrendDtos = pageDomainResultT.getData().getRecords();
        }
        return companyTrendDtos;
    }

    @Override
    public ResultT<PageDomain<CompanyTrendAppVo>> pageCompanyTrends(SimplePageReq pageReq, String stockCode) {
        Long pageSize = pageReq.getPageSize();
        Long currentPage = pageReq.getCurrentPage();

        PageDomain<CompanyTrendAppVo> pageDomain = new PageDomain<>();
        pageDomain.setSize(pageSize);
        pageDomain.setCurrent(currentPage);

        // 根据type 股票code查询
        Page<CompanyTrendsMergeEntity> page = new Page<>(currentPage, pageSize);
        QueryWrapper<CompanyTrendsMergeEntity> wrapper = new QueryWrapper<CompanyTrendsMergeEntity>()
                .eq(InformationConstant.COLUMN_SECCODE, stockCode)
                .notIn(InformationConstant.COLUMN_TYPE, CompanyTrendsType.TEN.getCode(), CompanyTrendsType.ELEVEN.getCode())
                .orderByDesc(InformationConstant.COLUMN_DATE1, InformationConstant.COLUMN_ID);

        Page<CompanyTrendsMergeEntity> companyTrendsMergeEntityPage = companyTrandsMergeMapper.selectPage(page, wrapper);
        List<CompanyTrendsMergeEntity> records = companyTrendsMergeEntityPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return ResultT.success(pageDomain);
        }
        List<CompanyTrendAppVo> collect = records.stream().map(entity -> {
            CompanyTrendAppVo companyTrendDto = new CompanyTrendAppVo();
            CompanyTrendsType byCode = CompanyTrendsType.getByCode(entity.getType());
            companyTrendDto.setColDate1(dateFormatSlash(entity.getDate1()));
            companyTrendDto.setColDate2(dateFormatSlash(entity.getDate2()));
            if (CompanyTrendsType.REORGANIZE != CompanyTrendsType.getByCode(entity.getType())) {
                companyTrendDto.setContent1(entity.getContent());
                companyTrendDto.setContent2(entity.getContent2());
//            } else {
//                companyTrendDto.setContent1(entity.getDate1());
            }
            companyTrendDto.setType(byCode.getName());
            companyTrendDto.setTypeCode(entity.getType());
            return companyTrendDto;
        }).collect(Collectors.toList());

        pageDomain.setTotal(companyTrendsMergeEntityPage.getTotal());
        pageDomain.setRecords(collect);

        return ResultT.success(pageDomain);
    }

    public String dateFormatSlash(Long date) {
        if (date == null) {
            return "";
        }
        String dateStr = date.toString();
        return dateFormatSlash(dateStr);
    }

    public String dateFormatSlash(String date) {
        if (date == null) {
            return "";
        }
        String dateStr = date.toString();
        return dateStr.substring(0, 4) + "/" + dateStr.substring(4, 6) + "/" + dateStr.substring(6);
    }

    private String parseDividendDay(Xnhks0112 xnhks0112) {

        String f003v = xnhks0112.getF003v();
        Set<String> f003vSet = Sets.newHashSet(f003v.split(","));

        if (f003vSet.contains("OD")) {
            return dateFormatSlash(xnhks0112.getF015d());
        }
        if (f003vSet.contains("BS")) {
            return dateFormatSlash(xnhks0112.getF011d());
        }
        if (f003vSet.contains("BW")) {
            return dateFormatSlash(xnhks0112.getF012d());
        }
        return dateFormatSlash(xnhks0112.getF010d());
    }

    @Override
    public ResultT<ComPageWithTime<ComInformationGroupVo<ComCompanyTrendAppVo>>> pageF10CompanyTrendsApp(ComCalendarNewsPageReq pageReq, String stockCode) {
        Long pageSize = pageReq.getPageSize();
        Long currentPage = pageReq.getCurrentPage();

        // 根据type 股票code查询
        Page<CompanyTrendsMergeEntity> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<CompanyTrendsMergeEntity> wrapper = new LambdaQueryWrapper<CompanyTrendsMergeEntity>()
                .eq(CompanyTrendsMergeEntity::getSECCODE, stockCode)
                .in(CollUtil.isNotEmpty(pageReq.getTrendsType()), CompanyTrendsMergeEntity::getType, pageReq.getTrendsType())
                .notIn(CompanyTrendsMergeEntity::getType, Arrays.asList(ComCompanyTrendsType.TEN.getCode(), ComCompanyTrendsType.ELEVEN.getCode()))
                .orderByDesc(CompanyTrendsMergeEntity::getReleaseDate).orderByDesc(CompanyTrendsMergeEntity::getSxdbmask);

        Page<CompanyTrendsMergeEntity> trendsPage = companyTrandsMergeMapper.selectPage(page, wrapper);
        List<CompanyTrendsMergeEntity> records = trendsPage.getRecords();

        ComPageWithTime<ComInformationGroupVo<ComCompanyTrendAppVo>> pageDomain = new ComPageWithTime<>();
        pageDomain.setCurrent(currentPage);
        pageDomain.setTotal(trendsPage.getTotal());

        if (CollectionUtils.isEmpty(records)) {
            return ResultT.success(pageDomain);
        }

        List<ComInformationGroupVo<ComCompanyTrendAppVo>> result = records.stream().map(entity -> {
            ComCompanyTrendAppVo companyTrendVo = new ComCompanyTrendAppVo();

            String date1 = StringUtils.isBlank(entity.getDate1()) ? "" : DateUtils.formatDate(DateUtils.parseDate(entity.getDate1()).getTime(), "yyyy/MM/dd");
            String date2 = StringUtils.isBlank(entity.getDate2()) ? "" : DateUtils.formatDate(DateUtils.parseDate(entity.getDate2()).getTime(), "yyyy/MM/dd");

            String content = entity.getContent();
            String content2 = entity.getContent2();

            // 替换掉 <br />
            companyTrendVo.setContent1(content);
            companyTrendVo.setContent2(content2);
            companyTrendVo.setColDate1(date1);
            companyTrendVo.setColDate2(date2);
            companyTrendVo.setType(ComCompanyTrendsType.getCompanyTrendsType(entity.getType()));
            companyTrendVo.setTypeCode(entity.getType());

            List<Integer> severalDateTypes = Arrays.asList(ComCompanyTrendsType.PARALLEL.getCode(), ComCompanyTrendsType.TRADING.getCode());
            if (severalDateTypes.contains(entity.getType())) {
                companyTrendVo.setAxisDate(DateUtils.parseDate(entity.getReleaseDate()).getTime());
            } else {
                companyTrendVo.setAxisDate(DateUtils.parseDate(entity.getOrderDate()).getTime());
            }

            return companyTrendVo;
        }).collect(Collectors.groupingBy(cta -> {
            DateTime date = DateUtil.date(cta.getAxisDate());
            return DateUtil.beginOfYear(date).getTime();
        })).entrySet().stream().map(entry -> {
            ComInformationGroupVo<ComCompanyTrendAppVo> informationGroupVo = new ComInformationGroupVo<>();
            informationGroupVo.setDate(entry.getKey());
            informationGroupVo.setList(entry.getValue());
            return informationGroupVo;
        }).sorted(Comparator.comparing(item -> (Long) ReflectUtil.getFieldValue(item, "date")).reversed()).collect(Collectors.toList());

        pageDomain.setRecords(result);
        pageDomain.setSize(CollUtil.size(records));

        return ResultT.success(pageDomain);
    }

    /**
     * 公司动向数据合并
     */
    @Override
    public ResultT mergeCalendarInformation() {
        List<TypeSxdbmask> maxDbmaskGroupType = companyTrandsMergeMapper.findMaxDbmaskGroupType();
        Map<Integer, Long> typeSxdbmaskMap = maxDbmaskGroupType.stream().collect(Collectors.toMap(TypeSxdbmask::getType, TypeSxdbmask::getSxdbmask));
        CompanyTrendsType companyTrendsType;
        Integer num;
        // 交易警报
        companyTrendsType = CompanyTrendsType.ALARM;
        List<CompanyTrendsMergeEntity> entityListFromALARM = xnhk0311Mapper.listByDbmask(typeSxdbmaskMap.getOrDefault(companyTrendsType.getCode(), 0L), companyTrendsType.getCode());
        entityListFromALARM.forEach(entity -> companyTrandsMergeMapper.save(entity));
        num = entityListFromALARM.size();
        log.info("插入交易警报数据:{}条", num);
        // 并行交易
        companyTrendsType = CompanyTrendsType.PARALLEL;
        List<CompanyTrendsMergeEntity> entityListFromPARALLEL = xnhks0314Mapper.listByDbmask(typeSxdbmaskMap.getOrDefault(companyTrendsType.getCode(), 0L), companyTrendsType.getCode());
        entityListFromPARALLEL.forEach(entity -> companyTrandsMergeMapper.save(entity));
        num = entityListFromPARALLEL.size();
        log.info("插入并行交易数据:{}条", num);
        //停/复牌
        companyTrendsType = CompanyTrendsType.TRADING;
        List<CompanyTrendsMergeEntity> entityListFromTRADING = xnhks0317Mapper.listByDbmask(typeSxdbmaskMap.getOrDefault(companyTrendsType.getCode(), 0L), companyTrendsType.getCode());
        entityListFromTRADING.forEach(entity -> companyTrandsMergeMapper.save(entity));
        num = entityListFromTRADING.size();
        log.info("插入停/复牌数据:{}条", num);
        addSuspension();

        //股东大会
        companyTrendsType = CompanyTrendsType.METTING;
        List<CompanyTrendsMergeEntity> entityListFromMETTING = xnhks0310Mapper.listByDbmask(typeSxdbmaskMap.getOrDefault(companyTrendsType.getCode(), 0L), companyTrendsType.getCode());
        entityListFromMETTING.forEach(entity -> companyTrandsMergeMapper.save(entity));
        num = entityListFromMETTING.size();
        log.info("插入股东大会数据:{}条", num);
        //公司重组
        companyTrendsType = CompanyTrendsType.REORGANIZE;
        List<CompanyTrendsMergeEntity> entityListFromREORGANIZE = xnhks0309Mapper.listByDbmask(typeSxdbmaskMap.getOrDefault(companyTrendsType.getCode(), 0L), companyTrendsType.getCode());
        entityListFromREORGANIZE.forEach(entity -> companyTrandsMergeMapper.save(entity));
        num = entityListFromREORGANIZE.size();
        log.info("插入公司重组数据:{}条", num);
        //收购及合并
        companyTrendsType = CompanyTrendsType.PURCHASE;
        List<CompanyTrendsMergeEntity> entityListFromPURCHASE = xnhks0308Mapper.listByDbmask(typeSxdbmaskMap.getOrDefault(companyTrendsType.getCode(), 0L), companyTrendsType.getCode());
        entityListFromPURCHASE.forEach(entity -> companyTrandsMergeMapper.save(entity));
        num = entityListFromPURCHASE.size();
        log.info("插入收购及合并数据:{}条", num);

        // 除权
        companyTrendsType = CompanyTrendsType.TEN;
        List<CompanyTrendsMergeEntity> entityListFromExitRight = xnhk0127Mapper.listByDbmask(typeSxdbmaskMap.getOrDefault(companyTrendsType.getCode(), 0L), companyTrendsType.getCode());
        entityListFromExitRight.forEach(entity -> companyTrandsMergeMapper.save(entity));
        num = entityListFromExitRight.size();
        log.info("插入除权数据:{}条", num);
        // 财报-非金融
        companyTrendsType = CompanyTrendsType.ELEVEN;
        List<CompanyTrendsMergeEntity> entityListFromFinancialReport1 = xnhk0201Mapper.listByDbmask(typeSxdbmaskMap.getOrDefault(companyTrendsType.getCode(), 0L), companyTrendsType.getCode());
        entityListFromFinancialReport1.forEach(entity -> companyTrandsMergeMapper.save(entity));
        num = entityListFromFinancialReport1.size();
        log.info("插入财报-非金融数据:{}条", num);
        // 财报-金融
        List<CompanyTrendsMergeEntity> entityListFromFinancialReport2 = xnhk0204Mapper.listByDbmask(typeSxdbmaskMap.getOrDefault(companyTrendsType.getCode(), 0L), companyTrendsType.getCode());
        entityListFromFinancialReport2.forEach(entity -> companyTrandsMergeMapper.save(entity));
        num = entityListFromFinancialReport2.size();
        log.info("插入财报-金融数据:{}条", num);
        // 财报-保险
        List<CompanyTrendsMergeEntity> entityListFromFinancialReport3 = xnhk0207Mapper.listByDbmask(typeSxdbmaskMap.getOrDefault(companyTrendsType.getCode(), 0L), companyTrendsType.getCode());
        entityListFromFinancialReport3.forEach(entity -> companyTrandsMergeMapper.save(entity));
        num = entityListFromFinancialReport3.size();
        log.info("插入财报-保险数据:{}条", num);

        return ResultT.success();
    }

    /**
     * 补充停牌数据
     */
    private void addSuspension() {
        // 补充停牌数据
        List<CompanyTrendsMergeEntity> entityList = companyTrandsMergeMapper.selectList(new QueryWrapper<CompanyTrendsMergeEntity>()
                .eq("type", CompanyTrendsType.TRADING.getCode()).isNotNull("date1").isNull("date2").orderByDesc("releaseDate").last(" limit 500"));
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        List<CompanyTrendsMergeEntity> suspensions = xnhks0317Mapper.listSuspension("('" + entityList.stream().map(CompanyTrendsMergeEntity::getSECCODE).collect(Collectors.joining("','")) + "')",CompanyTrendsType.TRADING.getCode());
        if (CollectionUtils.isEmpty(suspensions)) {
            return;
        }
        for (CompanyTrendsMergeEntity entity : entityList) {
            CompanyTrendsMergeEntity suspension = suspensions.stream().filter(m -> entity.getSECCODE().equals(m.getSECCODE()) && entity.getDate1().equals(m.getDate1())).findFirst().orElse(null);
            if (suspension == null) {
                continue;
            }

            /**
             * 此处有两个更新操作，但是不需要使用事务，
             * 1. 插入停牌数据时，改成了仅插入停牌时间
             * 2. 由于没有复牌时间，此时会 addSuspension 方法的sql查询出来，然后 调用 listSuspension查询到复牌数据
             * 3. 如果复牌数据和停牌数据的发布日期不是同一天，则插入一条复牌数据。如果是宕机重入的情况，有唯一索引保证不会数据重复。
             * 4. 修改 停牌数据的复牌时间。这是最后一条sql.执行成功后下次selectList查询就不会有这条数据了
              */
            if (!entity.getReleaseDate().equals(suspension.getReleaseDate())) {
                // 停复牌公布日期不相同则 插入一条复牌的动向
                suspension.setSxdbmask(entity.getSxdbmask());
                suspension.setType(CompanyTrendsType.TRADING.getCode());
                companyTrandsMergeMapper.save(suspension);
            }
            // 同时更新停牌数据的复牌时间
            entity.setDate2(suspension.getDate2());
            companyTrandsMergeMapper.updateById(entity);
        }
    }

    /**
     * 公司动向数据合并
     */
    @Override
    public ResultT mergeCalendarInformationOld() {
        CompanyTrendsType companyTrendsType;
        Integer num;
        // 交易警报
        companyTrendsType = CompanyTrendsType.ALARM;
        num = companyTrandsMergeMapper.mergeAlarm(companyTrendsType.getCode());
        log.info("插入交易警报数据:{}条", num);
        // 并行交易
        companyTrendsType = CompanyTrendsType.PARALLEL;
        num = companyTrandsMergeMapper.mergeParallel(companyTrendsType.getCode());
        log.info("插入并行交易数据:{}条", num);
        //停/复牌
        companyTrendsType = CompanyTrendsType.TRADING;
        num = companyTrandsMergeMapper.mergeTrading(companyTrendsType.getCode());
        log.info("插入停/复牌数据:{}条", num);
        //公司重组
        companyTrendsType = CompanyTrendsType.REORGANIZE;
        num = companyTrandsMergeMapper.mergeReorganize(companyTrendsType.getCode());
        log.info("插入公司重组数据:{}条", num);
        //收购及合并
        companyTrendsType = CompanyTrendsType.PURCHASE;
        num = companyTrandsMergeMapper.mergePurchase(companyTrendsType.getCode());
        log.info("插入收购及合并数据:{}条", num);
        //股东大会
        companyTrendsType = CompanyTrendsType.METTING;
        num = companyTrandsMergeMapper.mergeMetting(companyTrendsType.getCode());
        log.info("插入股东大会数据:{}条", num);
        return ResultT.success();
    }

    @Override
    public ResultT<PageWithTime<InformationGroupVo<CompanyTrendPcVo>>> pageCompanyTrends(CalendarNewsPageReq pageReq, List<String> stocks) {
        Long pageSize = pageReq.getPageSize();
        Long currentPage = pageReq.getCurrentPage();
        String startTime = ObjectUtils.isEmpty(pageReq.getStartTime()) ? DateUtils.formatDate(new Date(), "yyyyMMdd") : DateUtils.formatDate(pageReq.getStartTime(), "yyyyMMdd");
        String endTime = ObjectUtils.isEmpty(pageReq.getEndTime()) ? "" : DateUtils.formatDate(pageReq.getEndTime(), "yyyyMMdd");
//        Integer queryType = pageReq.getQueryType();
//        Long id = pageReq.getId();
        List<Integer> trendsType = pageReq.getTrendsType();

        PageWithTime<InformationGroupVo<CompanyTrendPcVo>> pageDomain = new PageWithTime<>();
        pageDomain.setCurrent(currentPage);
        // List<StockSnapshot> snapshot = getSnapshot();
        // if (CollectionUtils.isEmpty(pageReq.getGroupTypes())) {
        //    stocks = snapshot.stream().map(StockSnapshot::getCode).collect(Collectors.toList());
        // }
        // List<StockDefine> stockDefineList = stockDefineService.list();
        List<StockDefine> stockDefineList = stockDefineService.listStockColumns(null);
         if (CollectionUtils.isEmpty(pageReq.getGroupTypes())) {
            stocks = stockDefineList.stream().map(StockDefine::getCode).collect(Collectors.toList());
         }
         // 要求按 字典顺序排序
        List<SysDictData> trendsTypeData = sysDicDateService.dictType("trends_type", null);
         StringBuilder sb = new StringBuilder();
         sb.append("order by order_date, ");
        if (CollUtil.isNotEmpty(trendsTypeData)) {
            sb.append("case type ");
        }
        Map<String, SysDictData> typeSortMap = trendsTypeData.stream().collect(Collectors.toMap(SysDictData::getDictValue, Function.identity()));
        for (CompanyTrendsType value : CompanyTrendsType.values()) {
            SysDictData sysDictData = typeSortMap.get((String.valueOf(value.getCode())));
            if (sysDictData != null && sysDictData.getStatus().equals("0")) {
                sb.append(String.format("when %s then %s ", value.getCode(), sysDictData.getDictSort()));
            }
        }
        sb.append("else 10 ");
        sb.append("END, id ");


        // 根据type 股票code查询
        Page<CompanyTrendsMergeEntity> page = new Page<>(currentPage, pageSize);
        QueryWrapper<CompanyTrendsMergeEntity> wrapper = new QueryWrapper<CompanyTrendsMergeEntity>()
                .in(CollectionUtils.isNotEmpty(stocks), InformationConstant.COLUMN_SECCODE, stocks)
                .in(CollectionUtils.isNotEmpty(trendsType), InformationConstant.COLUMN_TYPE, trendsType)
//                .le(queryType.equals(0) && ObjectUtils.isNotEmpty(id), InformationConstant.COLUMN_ID, id)
//                .gt(queryType.equals(1) && ObjectUtils.isNotEmpty(id), InformationConstant.COLUMN_ID, id)
                .ge(org.apache.commons.lang3.StringUtils.isNotBlank(startTime), InformationConstant.COLUMN_ORDER_DATE, startTime)
                .le(org.apache.commons.lang3.StringUtils.isNotBlank(endTime), InformationConstant.COLUMN_ORDER_DATE, endTime)
                .last(sb.toString());
//                .orderByAsc(InformationConstant.COLUMN_ORDER_DATE, InformationConstant.COLUMN_ID);

        Page<CompanyTrendsMergeEntity> companyTrendsMergeEntityPage = companyTrandsMergeMapper.selectPage(page, wrapper);
        List<CompanyTrendsMergeEntity> records = companyTrendsMergeEntityPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return ResultT.success(pageDomain);
        }
        List<InformationGroupVo<CompanyTrendPcVo>> result = new ArrayList<>();
        records.stream().map(entity -> {
            CompanyTrendPcVo companyTrendVo = new CompanyTrendPcVo();
            companyTrendVo.setSECCODE(entity.getSECCODE());
            companyTrendVo.setStockCode(entity.getSECCODE());
            companyTrendVo.setId(entity.getId());
            companyTrendVo.setReleaseDate(DateUtils.parseDate(entity.getReleaseDate()).getTime());
            companyTrendVo.setOrderDate(DateUtils.parseDate(entity.getOrderDate()).getTime());
            companyTrendVo.setDate1(StringUtils.isBlank(entity.getDate1()) ? null : DateUtils.parseDate(entity.getDate1()).getTime());
            companyTrendVo.setDate2(StringUtils.isBlank(entity.getDate2()) ? null : DateUtils.parseDate(entity.getDate2()).getTime());
//            if (CompanyTrendsType.PARALLEL.getCode().equals(entity.getType()) && StringUtils.isNotBlank(entity.getContent())) {
//                companyTrendVo.setContent2(entity.getContent().replace("并行证券代码：0", ""));
//                companyTrendVo.setContent1(entity.getContent2().replace("并行证券名称：", ""));
//            } else {
            // 去掉 <br>
                companyTrendVo.setContent1(StrUtil.replace(entity.getContent(), "<br>", ""));
                // companyTrendVo.setContent2(entity.getContent2().replace("<br>",""));
                companyTrendVo.setContent2(StrUtil.replace(entity.getContent2(), "<br>", ""));
//            }
            companyTrendVo.setTrendsCode(entity.getType());
//            StockSnapshot stockSnapshot = snapshot.stream().filter(s -> s.getCode().equals(entity.getSECCODE())).findFirst().orElse(null);
//            if (ObjectUtils.isNotEmpty(stockSnapshot)) {
//                companyTrendVo.setStockName(stockSnapshot.getName());
//            }
             stockDefineList.stream().filter(s -> s.getCode().equals(entity.getSECCODE())).findFirst().ifPresent(stockDefine -> companyTrendVo.setStockName(stockDefine.getStockName()));
            companyTrendVo.setTrendsType(CompanyTrendsType.getCompanyTrendsType(entity.getType()));
            return companyTrendVo;
        }).collect(Collectors.groupingBy(CompanyTrendPcVo::getOrderDate)).forEach((k, v) -> {
            result.add(buildGroupVo(k, v));
        });
        result.sort(Comparator.comparing(InformationGroupVo::getDate));
        pageDomain.setSize(records.size());
        pageDomain.setTotal(companyTrendsMergeEntityPage.getTotal());
        pageDomain.setRecords(result);

        return ResultT.success(pageDomain);
    }

    @Override
    public ResultT<ComPageWithTime<ComInformationGroupVo<ComCompanyTrendVo>>> pageCompanyTrendsNew(ComCalendarNewsPageReq pageReq, boolean isApp, List<String> stocks) {
        Long pageSize = pageReq.getPageSize();
        Long currentPage = pageReq.getCurrentPage();
        List<Integer> trendsType = pageReq.getTrendsType();
        String startTime = ObjectUtils.isEmpty(pageReq.getStartTime()) ? DateUtils.formatDate(new Date(), "yyyyMMdd") : StrUtil.replace(pageReq.getStartTime(), "/", "");
        String endTime = ObjectUtils.isEmpty(pageReq.getEndTime()) ? null : StrUtil.replace(pageReq.getEndTime(), "/", "");

        if (CollectionUtils.isEmpty(pageReq.getGroupTypes())) {
            // List<StockDefine> stockDefineList = stockDefineService.list(Wrappers.<StockDefine>lambdaQuery().select(StockDefine::getCode));
            List<StockDefine> stockDefineList = stockDefineService.listStockColumns(ListUtil.of("code"));
            stocks = stockDefineList.stream().map(StockDefine::getCode).collect(Collectors.toList());
        }
        // 要求按 字典顺序排序
        List<SysDictData> trendsTypeData = sysDicDateService.dictType("trends_type", null);
        StringBuilder sb = new StringBuilder().append("order by order_date, ").append(CollUtil.isNotEmpty(trendsTypeData) ? "case type " : "");
        Map<String, SysDictData> typeSortMap = trendsTypeData.stream().collect(Collectors.toMap(SysDictData::getDictValue, Function.identity()));
        for (CompanyTrendsType value : CompanyTrendsType.values()) {
            SysDictData sysDictData = typeSortMap.get((String.valueOf(value.getCode())));
            if (sysDictData != null && sysDictData.getStatus().equals("0")) {
                sb.append(String.format("when %s then %s ", value.getCode(), sysDictData.getDictSort()));
            }
        }
        sb.append("else 10 ").append("END, id ");

        // 根据type 股票code查询
        Page<CompanyTrendsMergeEntity> page = new Page<>(currentPage, pageSize);
        QueryWrapper<CompanyTrendsMergeEntity> wrapper = new QueryWrapper<CompanyTrendsMergeEntity>()
                .in(CollectionUtils.isNotEmpty(stocks), InformationConstant.COLUMN_SECCODE, stocks)
                .in(CollectionUtils.isNotEmpty(trendsType), InformationConstant.COLUMN_TYPE, trendsType)
                .notIn(BooleanUtil.isTrue(isApp) && CollUtil.isEmpty(trendsType), InformationConstant.COLUMN_TYPE, ListUtil.of(ComCompanyTrendsType.TEN.getCode(), ComCompanyTrendsType.ELEVEN.getCode()))
                .ge(StringUtils.isNotBlank(startTime), InformationConstant.COLUMN_ORDER_DATE, startTime)
                .le(StringUtils.isNotBlank(endTime), InformationConstant.COLUMN_ORDER_DATE, endTime)
                .last(sb.toString());

        Page<CompanyTrendsMergeEntity> companyTrendsMergeEntityPage = companyTrandsMergeMapper.selectPage(page, wrapper);
        List<CompanyTrendsMergeEntity> records = companyTrendsMergeEntityPage.getRecords();

        ComPageWithTime<ComInformationGroupVo<ComCompanyTrendVo>> pageDomain = new ComPageWithTime<>();
        pageDomain.setCurrent(currentPage);
        pageDomain.setTotal(companyTrendsMergeEntityPage.getTotal());

        if (CollectionUtils.isEmpty(records)) {
            return ResultT.success(pageDomain);
        }
        List<String> codes = records.stream().map(CompanyTrendsMergeEntity::getSECCODE).collect(Collectors.toList());
        Map<String, ComStockSimpleDto> stockNameMap = stockCache.queryStockInfoMap(codes);
        List<ComInformationGroupVo<ComCompanyTrendVo>> result = records.stream().map(entity -> {
            ComCompanyTrendVo companyTrendVo = new ComCompanyTrendVo();
            companyTrendVo.setSECCODE(entity.getSECCODE());
            companyTrendVo.setStockCode(entity.getSECCODE());
            companyTrendVo.setId(entity.getId());
            companyTrendVo.setReleaseDate(DateUtils.parseDate(entity.getReleaseDate()).getTime());
            companyTrendVo.setOrderDate(DateUtils.parseDate(entity.getOrderDate()).getTime());
            companyTrendVo.setStockType(StockTypeEnum.STOCK.getCode());
            companyTrendVo.setRegionType(RegionTypeEnum.HK.getCode());

            String date1 = StringUtils.isBlank(entity.getDate1()) ? "" : DateUtils.formatDate(DateUtils.parseDate(entity.getDate1()).getTime(), "yyyy/MM/dd");
            String date2 = StringUtils.isBlank(entity.getDate2()) ? "" : DateUtils.formatDate(DateUtils.parseDate(entity.getDate2()).getTime(), "yyyy/MM/dd");

            String content = entity.getContent();
            String content2 = entity.getContent2();

            if (ComCompanyTrendsType.ALARM.getCode().equals(entity.getType())) {
                companyTrendVo.setContent(content2);
            } else if (ComCompanyTrendsType.PARALLEL.getCode().equals(entity.getType())) {
                // if (StringUtils.isNotBlank(content)) {
                //     companyTrendVo.setContent(content.replace("并行证券代码：0", "") + "与" + date1 + "-" + date2 + content2.replace("并行证券名称：", "") + "-并行交易");
                // } else {
                //     companyTrendVo.setContent(content + "与" + date1 + "-" + date2 + content2 + "-并行交易");
                // }
                // 2023/05/17~2023/06/07  并行证券代码：02926  并行证券名称：智富资源投资(旧)
                companyTrendVo.setContent(date1 + "~" + date2 + "  " + content + "  " + content2);
            } else if (ComCompanyTrendsType.TRADING.getCode().equals(entity.getType())) {
                companyTrendVo.setContent((StringUtils.isBlank(date1) ? "" : "停牌日期：" + date1) + (StringUtils.isBlank(date2) ? "" : "复牌日期：" + date2));
            } else if (ComCompanyTrendsType.MEETING.getCode().equals(entity.getType())) {
                // companyTrendVo.setContent(date1 + "召开" + content);
                companyTrendVo.setContent("召开" + content);
            } else if (ComCompanyTrendsType.REORGANIZE.getCode().equals(entity.getType())) {
                companyTrendVo.setContent(content);
            } else if (ComCompanyTrendsType.PURCHASE.getCode().equals(entity.getType())) {
                companyTrendVo.setContent(content);
            } else if (ComCompanyTrendsType.TEN.getCode().equals(entity.getType())) {
                companyTrendVo.setContent(StrUtil.replace(content, "合并: ", "股份合并: "));
            } else if (ComCompanyTrendsType.ELEVEN.getCode().equals(entity.getType())) {
                companyTrendVo.setContent(content);
            }

            // 替换掉 <br />
            ComStockSimpleDto comStockSimpleDto = new ComStockSimpleDto();
            if(MapUtils.isNotEmpty(stockNameMap) && ObjectUtils.isNotEmpty(stockNameMap.get(entity.getSECCODE()))){
                comStockSimpleDto = stockNameMap.get(entity.getSECCODE());
            }
            companyTrendVo.setContent(StrUtil.replace(companyTrendVo.getContent(), "<br>", ""));
            companyTrendVo.setTrendsCode(entity.getType());
            companyTrendVo.setStockName(comStockSimpleDto.getStockName());
            companyTrendVo.setStockId(comStockSimpleDto.getStockId());
            companyTrendVo.setTrendsType(ComCompanyTrendsType.getCompanyTrendsType(entity.getType()));
            return companyTrendVo;
        }).collect(Collectors.groupingBy(ComCompanyTrendVo::getOrderDate)).entrySet().stream().map(entry -> {
            ComInformationGroupVo<ComCompanyTrendVo> informationGroupVo = new ComInformationGroupVo<>();
            informationGroupVo.setDate(entry.getKey());
            informationGroupVo.setList(entry.getValue());
            return informationGroupVo;
        }).sorted(Comparator.comparing(ComInformationGroupVo::getDate)).collect(Collectors.toList());

        pageDomain.setRecords(result);
        pageDomain.setSize(CollUtil.size(records));

        return ResultT.success(pageDomain);
    }

    @Override
    public ResultT<List<ComNewShareCalendarVO>> CompanyTrendsCalendar(ComNews comNews) {
        List<ComNewShareCalendarVO> resultList = new ArrayList<>();
        /**
         * 1、查询 1-公司动向、2-财经事件、3-新股、4-除权、5-财报 五个事件
         */
        String startTime = StringUtils.isEmpty(comNews.getStartTime()) ? DateUtils.formatDate(new Date(), "yyyyMMdd") : comNews.getStartTime().replace("/","");
        String endTime = StringUtils.isEmpty(comNews.getEndTime()) ? "" : comNews.getEndTime().replace("/","");

        // 将这个records 与全量码表过滤
        List<ComStockSimpleDto> stockSimpleInfos = stockInfoApi.getStockSimpleInfoLists();
        List<String> codeList = stockSimpleInfos.stream().map(ComStockSimpleDto::getCode).collect(Collectors.toList());
        // 新股
        List<ComNewShareVo> comNewShareVos = xnhks0503Mapper.listNewShareVoByType(startTime,endTime);
        // 判断 records 中 上市日期等于当天，需要再过滤全量码表
        Long time1 = ZoneDateUtils.localDate2Date(LocalDate.now(),ZoneDateUtils.Asia_HongKong).getTime();
        comNewShareVos = comNewShareVos.stream().filter(item -> {
            return (!time1.equals(item.getMarketDate()) || (time1.equals(item.getMarketDate()) && codeList.contains(item.getSECCODE())));
        }).collect(Collectors.toList());
//        comNewShareVos = comNewShareVos.stream().filter(item -> codeList.contains(item.getSECCODE())).collect(Collectors.toList());
        Map<Long,List<ComNewShareVo>> newShareMap = comNewShareVos.stream().collect(Collectors.groupingBy(vo -> vo.getMarketDate()));
        for(Long time : newShareMap.keySet()){
            ComNewShareCalendarVO calendarVO = new ComNewShareCalendarVO();
            calendarVO.setDate(time);
            calendarVO.setCalendarEventCode(3);
            calendarVO.setCalendarEventNum(newShareMap.get(time).size());
            resultList.add(calendarVO);
        }

        //财经事件
        List<ComNewShareCalendarVO> stockNews = stockNewsMapper.queryStockNewsByType(startTime,endTime);
        if(CollectionUtils.isNotEmpty(stockNews)){
            resultList.addAll(stockNews);
        }
        //公司行动
        List<CompanyTrendsMergeEntity> companyTrendsMergeEntities = companyTrandsMergeMapper.queryCompanyTrendsNum(startTime,endTime);
        companyTrendsMergeEntities = companyTrendsMergeEntities.stream().filter(item -> codeList.contains(item.getSECCODE())).collect(Collectors.toList());

        //公司动向
        List<CompanyTrendsMergeEntity> usTrendsMerges1 = companyTrendsMergeEntities.stream().filter(item -> !(item.getType().equals(10) || item.getType().equals(11))).collect(Collectors.toList());
        Map<String,List<CompanyTrendsMergeEntity>> usTrendsMerges1Map = usTrendsMerges1.stream().collect(Collectors.groupingBy(item -> item.getOrderDate()));
        for(String time : usTrendsMerges1Map.keySet()){
            ComNewShareCalendarVO calendarVO = new ComNewShareCalendarVO();
            calendarVO.setDate(Long.valueOf(time));
            calendarVO.setCalendarEventCode(1);
            calendarVO.setCalendarEventNum(usTrendsMerges1Map.get(time).size());
            resultList.add(calendarVO);
        }

        //除权
        List<CompanyTrendsMergeEntity> usTrendsMerges2 = companyTrendsMergeEntities.stream().filter(item -> item.getType().equals(10)).collect(Collectors.toList());
        Map<String,List<CompanyTrendsMergeEntity>> usTrendsMerges2Map = usTrendsMerges2.stream().collect(Collectors.groupingBy(item -> item.getOrderDate()));
        for(String time : usTrendsMerges2Map.keySet()){
            ComNewShareCalendarVO calendarVO = new ComNewShareCalendarVO();
            calendarVO.setDate(Long.valueOf(time));
            calendarVO.setCalendarEventCode(4);
            calendarVO.setCalendarEventNum(usTrendsMerges2Map.get(time).size());
            resultList.add(calendarVO);
        }

        //财报
        List<CompanyTrendsMergeEntity> usTrendsMerges3 = companyTrendsMergeEntities.stream().filter(item -> item.getType().equals(11)).collect(Collectors.toList());
        Map<String,List<CompanyTrendsMergeEntity>> usTrendsMerges3Map = usTrendsMerges3.stream().collect(Collectors.groupingBy(item -> item.getOrderDate()));
        for(String time : usTrendsMerges3Map.keySet()){
            ComNewShareCalendarVO calendarVO = new ComNewShareCalendarVO();
            calendarVO.setDate(Long.valueOf(time));
            calendarVO.setCalendarEventCode(5);
            calendarVO.setCalendarEventNum(usTrendsMerges3Map.get(time).size());
            resultList.add(calendarVO);
        }
        return ResultT.success(resultList);
    }
    /**
     * 删除临时股票公司动向
     *
     * @param stockCode
     */
    @Override
    public void delCompanyTrendByStockCode(String stockCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("删除临时股票公司动向 开始：stockCode：{}",stockCode);
            companyTrandsMergeMapper.delete(new QueryWrapper<CompanyTrendsMergeEntity>().eq("SECCODE",stockCode));
            log.info("删除临时股票公司动向 结束：stockCode：{} 耗时：{}",stockCode,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("删除临时股票公告数据：stockCode：{}  异常",stockCode,e);
        }
    }
    /**
     * 变更公司动向股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     * @return
     */
    @Override
    public void upCompanyTrendStockCode(String sourceCode, String targetCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("变更公司动向股票code 开始：sourceCode：{} targetCode：{}",sourceCode,targetCode);
            companyTrandsMergeMapper.update( null,Wrappers.<CompanyTrendsMergeEntity>lambdaUpdate().eq(CompanyTrendsMergeEntity::getSECCODE,sourceCode).set(CompanyTrendsMergeEntity::getSECCODE,targetCode));
            log.info("变更公司动向股票code 结束：sourceCode：{} targetCode：{} 耗时：{}",sourceCode,targetCode,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("变更公司动向股票code：sourceCode：{} targetCode：{} 异常",sourceCode,targetCode,e);
        }
    }

    @Override
    public void createCompanyTrendByStockCode(String stockCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("新增公司动向股票code 开始：stockCode：{} ", stockCode);
            String oldCode = StrUtil.replace(stockCode, "-t", "");
            List<CompanyTrendsMergeEntity> trendsMergeList = companyTrandsMergeMapper.selectList(Wrappers.<CompanyTrendsMergeEntity>lambdaQuery().eq(CompanyTrendsMergeEntity::getSECCODE, oldCode));
            CollUtil.forEach(trendsMergeList, (ff, index) -> { ff.setId(null);ff.setSECCODE(stockCode); });
            Opt.ofEmptyAble(trendsMergeList).peek(list -> companyTrandsMergeMapper.batchSaveDupUpdate(list));
            log.info("新增公司动向股票code 结束：stockCode：{} 耗时：{}", stockCode, System.currentTimeMillis() - l);
        } catch (Exception e) {
            log.info("新增公司动向股票code：stockCode：{}  异常", stockCode, e);
        }
    }

    @Override
    public ResultT<PageWithTime<InformationGroupVo<CompanyTrendAppVo2>>> pageCompanyTrends4App(CalendarNewsPageReq pageReq, List<String> stocks) {
        Long pageSize = pageReq.getPageSize();
        Long currentPage = pageReq.getCurrentPage();
        String startTime = ObjectUtils.isEmpty(pageReq.getStartTime()) ? DateUtils.formatDate(new Date(), "yyyyMMdd") : DateUtils.formatDate(pageReq.getStartTime(), "yyyyMMdd");
        String endTime = ObjectUtils.isEmpty(pageReq.getEndTime()) ? "" : DateUtils.formatDate(pageReq.getEndTime(), "yyyyMMdd");
        List<Integer> trendsType = pageReq.getTrendsType();
        if (CollectionUtils.isNotEmpty(trendsType) && trendsType.contains(0)) {
            trendsType = Arrays.asList(CompanyTrendsType.ALARM.getCode(), CompanyTrendsType.PARALLEL.getCode(), CompanyTrendsType.TRADING.getCode(), CompanyTrendsType.METTING.getCode(),
                    CompanyTrendsType.REORGANIZE.getCode(), CompanyTrendsType.PURCHASE.getCode());
        }

        PageWithTime<InformationGroupVo<CompanyTrendAppVo2>> pageDomain = new PageWithTime<>();
        pageDomain.setCurrent(currentPage);
        List<StockSnapshot> snapshot = getSnapshot();
        if (CollectionUtils.isEmpty(pageReq.getGroupTypes())) {
            stocks = snapshot.stream().map(StockSnapshot::getCode).collect(Collectors.toList());
        }

        Page<CompanyTrendsMergeEntity> page = new Page<>(currentPage, pageSize);
        QueryWrapper<CompanyTrendsMergeEntity> wrapper = new QueryWrapper<CompanyTrendsMergeEntity>()
                .in(CollectionUtils.isNotEmpty(stocks), InformationConstant.COLUMN_SECCODE, stocks)
                .in(CollectionUtils.isNotEmpty(trendsType), InformationConstant.COLUMN_TYPE, trendsType)
                .ge(org.apache.commons.lang3.StringUtils.isNotBlank(startTime), InformationConstant.COLUMN_RELEASE_DATE, startTime)
                .le(org.apache.commons.lang3.StringUtils.isNotBlank(endTime), InformationConstant.COLUMN_RELEASE_DATE, endTime)
                .orderByAsc(InformationConstant.COLUMN_RELEASE_DATE, InformationConstant.COLUMN_ID);

        Page<CompanyTrendsMergeEntity> companyTrendsMergeEntityPage = companyTrandsMergeMapper.selectPage(page, wrapper);
        List<CompanyTrendsMergeEntity> records = companyTrendsMergeEntityPage.getRecords();
        pageDomain.setTotal(companyTrendsMergeEntityPage.getTotal());
        if (CollectionUtils.isEmpty(records)) {
            return ResultT.success(pageDomain);
        }
        List<InformationGroupVo<CompanyTrendAppVo2>> result = new ArrayList<>();
        records.stream().map(entity -> {
            CompanyTrendAppVo2 companyTrendVo = new CompanyTrendAppVo2();
            companyTrendVo.setSECCODE(entity.getSECCODE());
            companyTrendVo.setStockCode(entity.getSECCODE());
            companyTrendVo.setId(entity.getId());
            companyTrendVo.setReleaseDate(DateUtils.parseDate(entity.getReleaseDate()).getTime());
            String date1 = StringUtils.isBlank(entity.getDate1()) ? "" : DateUtils.formatDate(DateUtils.parseDate(entity.getDate1()).getTime(), "yyyy/MM/dd");
            String date2 = StringUtils.isBlank(entity.getDate2()) ? "" : DateUtils.formatDate(DateUtils.parseDate(entity.getDate2()).getTime(), "yyyy/MM/dd");
            if (CompanyTrendsType.ALARM.getCode().equals(entity.getType())) {
                companyTrendVo.setContent(entity.getContent2());
            } else if (CompanyTrendsType.PARALLEL.getCode().equals(entity.getType())) {
                // if (StringUtils.isNotBlank(entity.getContent())) {
                //     companyTrendVo.setContent(entity.getContent().replace("并行证券代码：0", "")
                //             + "与" + date1 + "-" + date2 + entity.getContent2().replace("并行证券名称：", "") + "-并行交易");
                // } else {
                //     companyTrendVo.setContent(entity.getContent() + "与" + date1 + "-" + date2 + entity.getContent2() + "-并行交易");
                // }

                // 2023/05/17~2023/06/07  并行证券代码：02926  并行证券名称：智富资源投资(旧)
                companyTrendVo.setContent(date1 + "~" + date2 + "  " + entity.getContent() + "  " + entity.getContent2());
            } else if (CompanyTrendsType.TRADING.getCode().equals(entity.getType())) {
                companyTrendVo.setContent((StringUtils.isBlank(date1) ? "" : "停牌日期：" + date1) + (StringUtils.isBlank(date2) ? "" : "复牌日期：" + date2));
            } else if (CompanyTrendsType.METTING.getCode().equals(entity.getType())) {
                companyTrendVo.setContent(date1 + "召开" + entity.getContent());
            } else if (CompanyTrendsType.REORGANIZE.getCode().equals(entity.getType())) {
                companyTrendVo.setContent(entity.getContent());
            } else if (CompanyTrendsType.PURCHASE.getCode().equals(entity.getType())) {
                companyTrendVo.setContent(entity.getContent());
            }

            companyTrendVo.setTrendsCode(entity.getType());
            StockSnapshot stockSnapshot = snapshot.stream().filter(s -> s.getCode().equals(entity.getSECCODE())).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(stockSnapshot)) {
                companyTrendVo.setStockName(stockSnapshot.getName());
            }
            companyTrendVo.setTrendsType(CompanyTrendsType.getCompanyTrendsType(entity.getType()));
            return companyTrendVo;
        }).collect(Collectors.groupingBy(CompanyTrendAppVo2::getReleaseDate)).forEach((k, v) -> {
            result.add(buildGroupVo(k, v));
        });
        result.sort(Comparator.comparing(InformationGroupVo::getDate));
        pageDomain.setSize(records.size());
        pageDomain.setRecords(result);

        return ResultT.success(pageDomain);
    }

    @Override
    public void fullSyncCompanyTrends() {
        xnhk0127Handler.sync();
        xnhk0201Handler.sync();
        xnhk0204Handler.sync();
        xnhk0207Handler.sync();
        xnhk0311Handler.sync();
        xnhks0308Handler.sync();
        xnhks0310Handler.sync();
        xnhks0314Handler.sync();
        xnhks0317Handler.sync();
        xnhk0318Handler.sync();
    }

    private <T> InformationGroupVo<T> buildGroupVo(Long k, List<T> v) {
        InformationGroupVo<T> informationGroupVo = new InformationGroupVo<>();
        informationGroupVo.setList(v);
        informationGroupVo.setDate(k);
        return informationGroupVo;
    }

    @Override
    public PageDomain<StopAndResume> getStopAndResume2(String stockCode, SimplePageReq simplePageReq) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();

        Long date = getOffsetDate(stockCode);

        Page<StopAndResume> stopAndResumePage = new Page<>();
        stopAndResumePage.setCurrent(currentPage);
        stopAndResumePage.setSize(pageSize);
        Page<StopAndResume> pageResult = stopAndResumeMapper.getStopAndResume2(stopAndResumePage, stockCode, date);
        PageDomain<StopAndResume> pageDomain = new PageDomain<>();
        pageDomain.setCurrent(pageResult.getCurrent());
        pageDomain.setSize(pageResult.getSize());
        pageDomain.setTotal(pageResult.getTotal());
        pageDomain.setRecords(pageResult.getRecords());
        return pageDomain;
    }

    @Override
    public PageDomain<AcquisitionsAndMergers> getAcquisitionsAndMergers2(String stockCode, SimplePageReq simplePageReq) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();

        Long date = getOffsetDate(stockCode);
        Page<Xnhks0308> xnhks0308Page = new Page<>();
        xnhks0308Page.setCurrent(currentPage);
        xnhks0308Page.setSize(pageSize);
        QueryWrapper<Xnhks0308> queryWrapper =
                new QueryWrapper<Xnhks0308>().eq("SECCODE", stockCode).ge(ObjectUtil.isNotNull(date), "F001D", date).orderByDesc("F001D");

        Page<Xnhks0308> xnhks0308PageResult = xnhks0308Mapper.selectPage(xnhks0308Page, queryWrapper);
        List<Xnhks0308> xnhks0308List = xnhks0308PageResult.getRecords();

        List<AcquisitionsAndMergers> acquisitionsAndMergers = new ArrayList<>();
        xnhks0308List.forEach(t -> {
            AcquisitionsAndMergers andMergers = AcquisitionsAndMergers.builder()
                    .releaseDate(t.getF001d())
                    .documentsAndOfferDate(t.getF002d())
                    .offerClosingDate(t.getF004d())
                    .sendOfferAmountDate(t.getF005d())
                    .eventDetail(t.getF006v())
                    .stockCode(t.getSeccode())
                    .build();
            acquisitionsAndMergers.add(andMergers);
        });
        PageDomain<AcquisitionsAndMergers> reault = new PageDomain<>();
        reault.setTotal(xnhks0308PageResult.getTotal());
        reault.setCurrent(xnhks0308PageResult.getCurrent());
        reault.setSize(xnhks0308PageResult.getSize());
        reault.setRecords(acquisitionsAndMergers);
        return reault;
    }

    @Override
    public PageDomain<GeneralMeeting> getGeneralMeeting2(String stockCode, SimplePageReq simplePageReq) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();

        Long date = getOffsetDate(stockCode);

        Page<GeneralMeeting> generalMeetingPage = new Page<>();
        generalMeetingPage.setCurrent(currentPage);
        generalMeetingPage.setSize(pageSize);
        generalMeetingPage = xnhks0310Mapper.listJoin00092(generalMeetingPage, stockCode, date);

        PageDomain<GeneralMeeting> reault = new PageDomain<>();
        reault.setTotal(generalMeetingPage.getTotal());
        reault.setCurrent(generalMeetingPage.getCurrent());
        reault.setSize(generalMeetingPage.getSize());
        reault.setRecords(generalMeetingPage.getRecords());
        return reault;
    }

    @Override
    public PageDomain<TransactionAlert> getTransactionAlert2(String stockCode, SimplePageReq simplePageReq) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();

        Page<Xnhk0311> xnhk0311Page = new Page<>();
        xnhk0311Page.setCurrent(currentPage);
        xnhk0311Page.setSize(pageSize);

        Long date = getOffsetDate(stockCode);

        Page<TransactionAlert> transactionAlertPage = new Page<>();
        transactionAlertPage.setCurrent(currentPage);
        transactionAlertPage.setSize(pageSize);
        transactionAlertPage = xnhk0311Mapper.listJoin0007And00062(transactionAlertPage, stockCode, date);

        PageDomain<TransactionAlert> reault = new PageDomain<>();
        reault.setTotal(transactionAlertPage.getTotal());
        reault.setCurrent(transactionAlertPage.getCurrent());
        reault.setSize(transactionAlertPage.getSize());
        reault.setRecords(transactionAlertPage.getRecords());
        return reault;
    }

    @Override
    public PageDomain<TransactionParallelism> getTransactionParallelism2(String stockCode, SimplePageReq simplePageReq) {
        long currentPage = simplePageReq.getCurrentPage() == null ? 1L : simplePageReq.getCurrentPage();
        long pageSize = simplePageReq.getPageSize() == null ? 10L : simplePageReq.getPageSize();

        Long date = getOffsetDate(stockCode);

        Page<Xnhks0314> xnhks0314Page = new Page<>();
        xnhks0314Page.setCurrent(currentPage);
        xnhks0314Page.setSize(pageSize);
        QueryWrapper<Xnhks0314> queryWrapper =
                new QueryWrapper<Xnhks0314>().eq("SECCODE", stockCode).ge(ObjectUtil.isNotNull(date), "F001D", date).orderByDesc("F001D");

        Page<Xnhks0314> xnhks0314PageResult = xnhks0314Mapper.selectPage(xnhks0314Page, queryWrapper);
        List<Xnhks0314> xnhks0314List = xnhks0314PageResult.getRecords();

        List<TransactionParallelism> transactionParallelismList = new ArrayList<>();
        xnhks0314List.forEach(t -> {
            TransactionParallelism transactionParallelism =
                    TransactionParallelism.builder().stockCode(t.getSeccode()).releaseDate(t.getF001d())
                            .securitiesCode(ConcatCodeUtil.concatCode(t.getF003v())).securitiesName(t.getF005v())
                            .unit(t.getF004n()).reason(t.getF010v()).startDate(t.getF006d()).suspendDate(t.getF008v())
                            .tradingDay(t.getF009v()).build();
            transactionParallelismList.add(transactionParallelism);
        });
        PageDomain<TransactionParallelism> reault = new PageDomain<>();
        reault.setTotal(xnhks0314PageResult.getTotal());
        reault.setCurrent(xnhks0314PageResult.getCurrent());
        reault.setSize(xnhks0314PageResult.getSize());
        reault.setRecords(transactionParallelismList);
        return reault;
    }

    private Long getOffsetDate(String code) {
        Xnhks0101 xnhks0101 = xnhks0101Mapper.selectOne(Wrappers.<Xnhks0101>lambdaQuery().eq(Xnhks0101::getSeccode, code).last("limit 1"));
        return ObjectUtil.isNotNull(xnhks0101) ? xnhks0101.getF007d() : null;
    }
}
