package com.vv.finance.investment.bg.api.impl.f10;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.NumberChineseFormatter;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.stuxuhai.jpinyin.ChineseHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultCode;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.bean.FileBean;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.bean.SimplePageResp;
import com.vv.finance.common.constants.f10.HkTradeStateEnum;
import com.vv.finance.common.constants.info.HkMarketEnum;
import com.vv.finance.common.constants.kline.EventConstants;
import com.vv.finance.common.entity.quotation.f10.ComDirectorManager;
import com.vv.finance.common.enums.SortEnum;
import com.vv.finance.common.utils.BigDecimalUtil;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.investment.bg.api.f10.F10StockInformationApi;
import com.vv.finance.investment.bg.api.stock.StockRankingApi;
import com.vv.finance.investment.bg.cache.F10CommonCache;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.dto.f10.*;
import com.vv.finance.investment.bg.dto.info.EventDTO;
import com.vv.finance.investment.bg.dto.req.KlineReq;
import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.entity.f10.enums.TransactionParallelismReasonEnum;
import com.vv.finance.investment.bg.entity.information.CompanyEventVo;
import com.vv.finance.investment.bg.entity.req.CompanyEventReq;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.handler.uts.f10.DividendHandler;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.mongo.dao.F10KeyFiguresDao;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresInsuranceEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresNonFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.StockUtsNoticeEntity;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10SourceServiceImpl;
import com.vv.finance.investment.bg.stock.information.service.StockInformationServiceImpl;
import com.vv.finance.investment.bg.stock.rank.dto.StockIndustryDto;
import com.vv.finance.investment.bg.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.Collator;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/8/19 14:59
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice", validation = "true")
@RequiredArgsConstructor
@Slf4j
public class F10StockInformationApiImpl implements F10StockInformationApi {
    private final Xnhks0101Mapper xnhks0101Mapper;
    private final Xnhks0501Mapper xnhks0501Mapper;
    private final Xnhks0502Mapper xnhks0502Mapper;
    private final Xnhks0503Mapper xnhks0503Mapper;
    private final Xnhks0106Mapper xnhks0106Mapper;
    private final Xnhks0301Mapper xnhks0301Mapper;
    private final Xnhks0302Mapper xnhks0302Mapper;
    private final Xnhk0602Mapper xnhk0602Mapper;

    private final Xnhks0319Mapper xnhks0319Mapper;
    private final Xnhks0320Mapper xnhks0320Mapper;
    private final Xnhks0314Mapper xnhks0314Mapper;

    private final F10CommonCache f10CommonCache;
    private final MongoTemplate mongoTemplate;
    private final DividendHandler dividendHandler;
    private final StockRankingApi stockRankingApi;

    @Resource
    private Xnhk0127Mapper xnhk0127Mapper;

    @Resource
    F10KeyFiguresDao f10KeyFiguresDao;

    @Resource
    private StockInformationServiceImpl informationService;

    @Resource
    F10SourceServiceImpl f10SourceService;

    @Resource
    private StockCache stockCache;

    @Value("${stock.uts.notice.prefix}")
    String prefix;

    private static final Set<String> IGNORE_REPORT= Sets.newHashSet("P","Q5");

    @Override
    public ResultT<SecuritiesInformation> securityInfo(String code) {
        Xnhks0101 xnhks0101 = xnhks0101Mapper.selectOne(new QueryWrapper<Xnhks0101>().eq("seccode", code));
        if (ObjectUtil.isEmpty(xnhks0101)) {
            return ResultT.success();
        }

        // Xnhks0503 xnhks0503 = xnhks0503Mapper.queryIpoSummary(code);
        Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
        SecuritiesInformation information = SecuritiesInformation.builder()
                .code(xnhks0101.getSeccode())
                .name(StrUtil.blankToDefault(stockNameMap.get(xnhks0101.getSeccode()), xnhks0101.getF002v()))
                .listedExchange("香港交易所")
                .market(Objects.requireNonNull(HkMarketEnum.getByCode(xnhks0101.getF005v())).getDesc())
                .marketDate(DateUtils.dateFormat(xnhks0101.getF006d()))
                .tradeState(Objects.requireNonNull(HkTradeStateEnum.getByVal(xnhks0101.getF010v())).getDesc())
                .tradeCurrency(xnhks0101.getF011v())
                .lotSize(xnhks0101.getF012n())
                .type(f10CommonCache.simpleChineseType(xnhks0101.getF014v()))
                .shanghaiStockConnect(dealYN(xnhks0101.getF022v()))
                .shenGuTong(dealYN(xnhks0101.getF032v()))
                .aStockCode(xnhks0101.getF018v())
                .shortSelling(dealYN(xnhks0101.getF023v()))
                .hangSengMarker(dealYN(xnhks0101.getF019v()))
                .redChipMarker(dealYN(xnhks0101.getF021v()))
                .stateEnterpriseMarker(dealYN(xnhks0101.getF020v()))
                .ipoInformationList(buildIpoInformation(code))
                .instructionUrl(getStockInstructionUrl(code))
                .build();
        return ResultT.success(information);
    }

    private String getStockInstructionUrl(String code) {
        if (StrUtil.contains(code, "-")) {
            return null;
        }
        List<Integer> codes = Collections.singletonList(Integer.valueOf(code.replace(".hk", "")));
        List<StockUtsNoticeEntity> stockUtsNoticeEntities = informationService.listIpoPDF(codes);
        return informationService.buildPDF(stockUtsNoticeEntities, code);
    }

    private List<SecuritiesInformation.IpoInformation> buildIpoInformation(String code) {
        List<Xnhks0501> xnhks0501s = xnhks0501Mapper.selectList(new QueryWrapper<Xnhks0501>().
                eq("seccode", code)
                .select("seccode", "f002d", "F001V", "F009V"));
        List<Xnhks0502> xnhks0502s = xnhks0502Mapper.selectList(new QueryWrapper<Xnhks0502>()
                .eq("seccode", code)
                .select("seccode", "f002d", "F005D", "F013D", "F016D"));
        List<Xnhks0503> xnhks0503s = xnhks0503Mapper.selectList(new QueryWrapper<Xnhks0503>().eq("seccode", code));
        Map<Long, Xnhks0502> xnhks0502Map = xnhks0502s.stream().collect(Collectors.toMap(Xnhks0502::getF002d, Function.identity()));
        Map<Long, Xnhks0503> xnhks0503Map = xnhks0503s.stream().collect(Collectors.toMap(Xnhks0503::getF002d, Function.identity()));

        return xnhks0501s.stream().map(xnhks0501 -> {
            SecuritiesInformation.IpoInformation ipoInformation = new SecuritiesInformation.IpoInformation();
            Xnhks0503 xnhks0503 = xnhks0503Map.get(xnhks0501.getF002d());
            Xnhks0502 xnhks0502 = xnhks0502Map.get(xnhks0501.getF002d());

            ipoInformation.setExpectedListingDate(DateUtils.dateFormat(xnhks0501.getF002d()));
            if(xnhks0502 != null){
                //时间
                ipoInformation.setApplyDate(DateUtils.dateFormat(xnhks0502.getF005d()));
                ipoInformation.setPricingDate(DateUtils.dateFormat(xnhks0502.getF013d()));
                ipoInformation.setIssueResultDate(DateUtils.dateFormat(xnhks0502.getF016d()));
            }
            if(xnhks0503 != null){
                //时间
                ipoInformation.setSubscriptionStartDate(DateUtils.dateFormat(xnhks0503.getF005d()));
                ipoInformation.setSubscriptionDeadline(DateUtils.dateFormat(xnhks0503.getF006d()));
                //发行资料
                ipoInformation.setIssueInformation(buildIssueInformation(xnhks0501, xnhks0503));
                //发行数量
                ipoInformation.setIssueInQuantity(buildIssueIssueInQuantity(xnhks0503));
                //募集金额
                ipoInformation.setAmountRaised(buildAmountRaised(xnhks0503));
            }
            return ipoInformation;
        }).collect(Collectors.toList());
    }

    private SecuritiesInformation.IssueInformation buildIssueInformation(Xnhks0501 xnhks0501, Xnhks0503 xnhks0503) {
        SecuritiesInformation.IssueInformation issueInformation = new SecuritiesInformation.IssueInformation();

        issueInformation.setTradeState("Y".equalsIgnoreCase(xnhks0501.getF001v()) ? "成功" : "取消");
        issueInformation.setMarket(Objects.requireNonNull(HkMarketEnum.getByCode(xnhks0503.getF003v())).getDesc());
        issueInformation.setIssueCurrency(xnhks0503.getF007v());
        issueInformation.setIssuePrice(xnhks0503.getF011n());
        issueInformation.setIssueType(xnhks0503.getF004v());
        issueInformation.setEntranceFee(xnhks0503.getF013n());
        issueInformation.setMaximumIssuePrice(xnhks0503.getF009n());
        issueInformation.setMinimumIssuePrice(xnhks0503.getF008n());
        issueInformation.setOneHandSigningRate(xnhks0503.getF014n());
        issueInformation.setPublicSubscriptionMultiple(xnhks0503.getF015n());
        issueInformation.setFirstLaunchLotSize(xnhks0503.getF021n());
        issueInformation.setSubIndustry(xnhks0503.getF025v());

        List<String> lineId = xnhks0503Mapper.getLineId(xnhks0503.getSeccode().replace(".hk", ""));
        if (CollUtil.isNotEmpty(lineId)) {
            Query query = Query.query(Criteria.where("lineId").in(lineId));
            List<StockUtsNoticeEntity> noticeEntities = mongoTemplate.find(query, StockUtsNoticeEntity.class);
            if (CollUtil.isNotEmpty(noticeEntities)) {
                Map<String, FileBean> fileBeanMap = noticeEntities.stream().filter(item -> !item.getFileName().toUpperCase().contains(".HTM")).map(item -> {
                    FileBean fileBean = new FileBean();
                    fileBean.setFileName(StringUtils.isNotBlank(item.getHeadLine()) ? ChineseHelper.convertToSimplifiedChinese(item.getHeadLine()) : "");
                    fileBean.setFilePath(prefix.concat(item.getDirs()).concat("-").concat(item.getFileName().toLowerCase()));
                    return fileBean;
                }).collect(Collectors.toMap(FileBean::getFilePath, Function.identity(), (o1, o2) -> o2));
                List<FileBean> collect = new ArrayList<>(fileBeanMap.values());
                Prospectus prospectus = Prospectus.builder()
                        .desc("招股说明书")
                        .prospectusList(collect).build();
                issueInformation.setProspectus(prospectus);
            }

        }
        issueInformation.setSponsor(xnhks0501.getF009v() == null ? xnhks0501.getF009v() : xnhks0501.getF009v().replace("<br>", "、"));


        return issueInformation;
    }

    private SecuritiesInformation.AmountRaised buildAmountRaised(Xnhks0503 xnhks0503) {
        SecuritiesInformation.AmountRaised amountRaised = new SecuritiesInformation.AmountRaised();
        amountRaised.setAfterSaleMarketValue(regAmount(xnhks0503.getF039v()));
        amountRaised.setTotalInitialFundRaising(regAmount(xnhks0503.getF040v()));
        amountRaised.setSaleNetAmount(regAmount(xnhks0503.getF041v()));
        amountRaised.setInitialParValue(xnhks0503.getF023n());
        amountRaised.setCurrency(xnhks0503.getF022v());

        return amountRaised;
    }

    private SecuritiesInformation.IssueInQuantity buildIssueIssueInQuantity(Xnhks0503 xnhks0503) {
        SecuritiesInformation.IssueInQuantity issueInQuantity = new SecuritiesInformation.IssueInQuantity();
        issueInQuantity.setActualSale(xnhks0503.getF035n());
        issueInQuantity.setPlanSale(xnhks0503.getF034n());
        issueInQuantity.setOverAllotment(xnhks0503.getF033n());
        issueInQuantity.setSellingStockholder(xnhks0503.getF032n());
        issueInQuantity.setHongKongSale(xnhks0503.getF027n());
        issueInQuantity.setInternationalSale(xnhks0503.getF029n());
        issueInQuantity.setPrioritySale(xnhks0503.getF030n());

        return issueInQuantity;
    }

    private String regAmount(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        if (content.contains("不适用")) {
            return "--";
        }
        String pattern = "\\([^)]*\\)";
        content = content.replaceAll(pattern, "");
        return content.replace("港元", "").trim();
    }

    private String dealYN(String val) {
        if (null == val) {
            return null;
        }
        switch (val.toUpperCase()) {
            case "Y":
                return "是";
            case "N":
                return "否";
            default:
                return val;
        }
    }

    private String dealYNDelay(String val) {
        if (null == val) {
            return null;
        }
        switch (val.toUpperCase()) {
            case "Y":
                return "延期";
            case "N":
                return "正常";
            default:
                return val;
        }
    }

    @Override
    public ResultT<List<DirectorManager>> directorManager(String code) {
        List<Xnhks0106> xnhks0106s = xnhks0106Mapper.selectList(new QueryWrapper<Xnhks0106>().eq("seccode", code).orderByDesc("xdbmask"));
        List<DirectorManager> directorManagers = xnhks0106ToDirectorManager(xnhks0106s, code);
        return ResultT.success(directorManagers);
    }

    @Override
    public ResultT<PageDomain<DirectorManager>> directorManagerPage(String code, SimplePageReq pageReq) {
        ResultT<List<DirectorManager>> listResultT = directorManager(code);
        if (ResultCode.SUCCESS.code() != listResultT.getCode() || CollUtil.isEmpty(listResultT.getData())) {
            log.warn("directorManagerPageV2 getDirectorList failed! code|resultT: {}|{}", code, listResultT);
            return ResultT.success();
        }

        long currentPage = pageReq.getCurrentPage() == null ? 1L : pageReq.getCurrentPage();
        long pageSize = pageReq.getPageSize() == null ? 10L : pageReq.getPageSize();
        List<DirectorManager> directorManagers = listResultT.getData();

        List<DirectorManager> pageRecords = ListUtil.page(Math.toIntExact(currentPage - 1), Math.toIntExact(pageSize), directorManagers);

        PageDomain<DirectorManager> pageDomain = new PageDomain<>();
        pageDomain.setSize(pageSize);
        pageDomain.setCurrent(currentPage);
        pageDomain.setRecords(pageRecords);
        pageDomain.setTotal(CollUtil.size(directorManagers));

        return ResultT.success(pageDomain);
    }

    /**
     * 公司高管信息转换成 DirectorManager 实体对象
     *
     * @param xnhks0106s
     * @param code
     * @return
     */
    private List<DirectorManager> xnhks0106ToDirectorManager(List<Xnhks0106> xnhks0106s, String code) {
        ArrayList<Xnhks0106> lastLists = xnhks0106s.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Xnhks0106::getF001v))), ArrayList::new));
        // Map<String, String> map01 = xnhks0301Mapper.listF003V(code).stream().collect(Collectors.toMap(Xnhks0301::getF004v,Xnhks0301::getF003v));
        // Map<String, String> map02 = xnhks0302Mapper.listF003V(code).stream().collect(Collectors.toMap(Xnhks0302::getF004v,Xnhks0302::getF003v));
        // List<Xnhks0106> collect2 = collect.stream().filter(a -> map01.get(a.getF001v()) != null && map01.get(a.getF001v()).equals("A")).collect(Collectors.toList());
        // List<Xnhks0106> lastLists = collect2.stream().filter(a -> map02.get(a.getF001v()) == null || map02.get(a.getF001v()).equals("A")).collect(Collectors.toList());

        List<Xnhks0301> xnhks0301s = xnhks0301Mapper.selectList(new QueryWrapper<Xnhks0301>().eq("seccode", code)
                .eq("f003v", "A")
                .select("f004v", "MAX(f002d) f002d").groupBy("f004v"));
        List<Xnhks0302> xnhks0302s = xnhks0302Mapper.selectList(new QueryWrapper<Xnhks0302>().eq("seccode", code)
                .eq("f003v", "A")
                .select("f004v", "MAX(f002d) f002d").groupBy("f004v"));
        Map<String, Xnhks0301> xnhks0301Map = xnhks0301s.stream().collect(Collectors.toMap(Xnhks0301::getF004v, Function.identity()));
        Map<String, Xnhks0302> xnhks0302Map = xnhks0302s.stream().collect(Collectors.toMap(Xnhks0302::getF004v, Function.identity()));
        List<DirectorManager> directorManagers = lastLists.stream().map(item -> {
            DirectorManager directorManager = new DirectorManager();
            directorManager.setName(item.getF001v());
            directorManager.setPost(item.getF003v());
            directorManager.setYearlySalary(item.getF007n());
            directorManager.setYearlySalaryUnit(item.getF005v());
            directorManager.setAge(item.getF010n());
            if (item.getModifiedDate() != null) {
                directorManager.setUpdateDate(item.getModifiedDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
            }
            directorManager.setDetail(item.getF004v());
            Xnhks0301 xnhks0301 = xnhks0301Map.get(item.getF001v());
            boolean havePostDate = false;
            if (xnhks0301 != null) {
                directorManager.setHoldPostDate(DateUtils.dateFormat(xnhks0301.getF002d()));
                directorManager.setHoldPostDateLong(xnhks0301.getF002d());
                havePostDate = true;
            } else {
                Xnhks0302 xnhks0302 = xnhks0302Map.get(item.getF001v());
                if (xnhks0302 != null) {
                    directorManager.setHoldPostDate(DateUtils.dateFormat(xnhks0302.getF002d()));
                    directorManager.setHoldPostDateLong(xnhks0302.getF002d());
                    havePostDate = true;
                }

            }
            if (!havePostDate) {
                directorManager.setHoldPostDateLong(0L);
            }
            return directorManager;
        }).sorted(Comparator.comparing(DirectorManager::getHoldPostDateLong)).collect(Collectors.toList());

        Iterator<DirectorManager> iterator = directorManagers.iterator();
        List<DirectorManager> directorManagersNoDate = new ArrayList<>();
        List<DirectorManager> masters = new ArrayList<>();
        while (iterator.hasNext()) {
            DirectorManager next = iterator.next();
            String post = next.getPost();
            if ((post.contains("董事长") && !post.contains("副董事长")) || (post.contains("主席") && !post.contains("副主席"))) {
                masters.add(next);
                iterator.remove();
                continue;
            }
            if (next.getHoldPostDateLong() == 0L) {
                directorManagersNoDate.add(next);
                iterator.remove();
            }
        }
        if (CollectionUtils.isNotEmpty(masters)) {
            directorManagers.addAll(0, masters);
        }
        if (CollectionUtils.isNotEmpty(directorManagersNoDate)) {
            Collator collator = Collator.getInstance(Locale.CHINA);
            directorManagersNoDate.sort((o1, o2) -> {
                return collator.compare(o1.getName(), o2.getName());
            });
            directorManagers.addAll(directorManagersNoDate);
        }

        // 按任职日期正序排列，若任职任期为空，则排在后面并按照姓名首字母正序排序
        Comparator<DirectorManager> comparator = Comparator.comparing(DirectorManager::getHoldPostDateLong, Comparator.nullsLast(Long::compareTo))
                .thenComparing(cdm -> PinyinUtil.getFirstLetter(cdm.getName(), ""));
        List<DirectorManager> dateDms = directorManagers.stream().filter(dm -> ObjectUtil.isNotEmpty(dm.getHoldPostDateLong()) && dm.getHoldPostDateLong() > 0).collect(Collectors.toList());
        List<DirectorManager> noDateDms = CollUtil.subtractToList(directorManagers, dateDms);
        dateDms.sort(comparator);
        noDateDms.sort(comparator);

        List<DirectorManager> sortedDirectors = CollUtil.unionAll(dateDms, noDateDms);
        return sortedDirectors;
    }

    @Override
    public ResultT<Dividend> dividend(@Valid F10PageBaseReq req) {
        return dividendHandler.dividend(req);
    }

    @Override
    public ResultT<List<Xnhks0112>> changeBeforeAfterDividend(String code) {
        return dividendHandler.changeBeforeAfterDividend(code);
    }

    @Override
    public ResultT<DividendComparison> dividendComparison(@Valid F10PageBaseReq req) {
        StockIndustryDto data = stockRankingApi.queryStockIndustry(req.getStockCode()).getData();
        DividendComparison dividendComparison = new DividendComparison();
        if (data == null || ObjectUtil.isEmpty(data.getIndustrySubsidiary()) || StrUtil.isBlank(data.getIndustrySubsidiary().getCode())) {
            return ResultT.success(dividendComparison);
        }
        dividendComparison.setIndustry(data.getIndustrySubsidiary().getName());
        dividendComparison.setIndustryCode(data.getIndustrySubsidiary().getCode());
        List<DividendComparison.DividendComparisonDetail> comparisonDetailList = dividendHandler.dividendComparison(data.getIndustrySubsidiary().getCode());
        Long start = (req.getCurrentPage() - 1) * req.getPageSize();
        if (start > comparisonDetailList.size()) {
            return ResultT.success(dividendComparison);
        }
        // if (StringUtils.isEmpty(req.getSortKey())) {
        //     req.setSortKey("code");
        // }
        long end = comparisonDetailList.size() < (start + req.getPageSize()) ? comparisonDetailList.size() : start + req.getPageSize();
        if (StringUtils.isNotBlank(req.getSortKey())) {
            comparisonDetailList.sort((o1, o2) -> {
                Field[] fields = ReflectUtil.getFields(DividendComparison.DividendComparisonDetail.class);
                int compare = 0;

                for (Field field : fields) {
                    if (field.getName().equals(req.getSortKey())) {
                        Object fieldValue1 = ReflectUtil.getFieldValue(o1, field);
                        Object fieldValue2 = ReflectUtil.getFieldValue(o2, field);
                        if (fieldValue1 == null) {
                            compare = -1;
                            break;
                        }
                        if (fieldValue2 == null) {
                            compare = 1;
                            break;
                        }
                        if (fieldValue1 instanceof String) {
                            String str1 = (String) fieldValue1;
                            String str2 = (String) fieldValue2;
                            if ("code".equals(req.getSortKey())) {
                                if (o1.getCode().equals(req.getStockCode())) {
                                    compare = -1;
                                    break;
                                }
                                if (o2.getCode().equals(req.getStockCode())) {
                                    compare = 1;
                                    break;
                                }
                            }
                            compare = str1.compareTo(str2);
                            break;
                        }
                        if (fieldValue1 instanceof BigDecimal) {
                            BigDecimal v1 = (BigDecimal) fieldValue1;
                            BigDecimal v2 = (BigDecimal) fieldValue2;
                            compare = v1.compareTo(v2);
                            break;
                        }

                    }
                }
                return "desc".equals(req.getSort()) ? -compare : compare;
            });
        }
        // 当前股票固定第一个位置，后续按照分红金额倒序
        DividendComparison.DividendComparisonDetail current = CollUtil.findOne(comparisonDetailList, cd -> StrUtil.equals(req.getStockCode(), cd.getCode()));
        Collection<DividendComparison.DividendComparisonDetail> otherStockDividends = CollUtil.filterNew(comparisonDetailList, cd -> !StrUtil.equals(req.getStockCode(), cd.getCode()));

        String sortKey = StrUtil.blankToDefault(req.getSortKey(), "cumulativeDividendAmount");
        String sort = StrUtil.blankToDefault(req.getSort(), SortEnum.DESC.getValue());
        // 按照上市以来累计分红金额倒序排列
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(sortKey, DividendComparison.DividendComparisonDetail.class);
            Method readMethod = descriptor.getReadMethod();
            Comparator<DividendComparison.DividendComparisonDetail> comparator = Comparator.comparing(o -> {
                try {
                    Object result = readMethod.invoke(o);
                    // 如果是根据上市以来累计分红金额排序，null作为0处理
                    return ObjectUtil.defaultIfNull(result, s -> new BigDecimal(s.toString()), StrUtil.equals("cumulativeDividendAmount", sortKey) ? BigDecimal.ZERO : null);
                } catch (Exception e) {
                    log.error("执行方法失败");
                    return null;
                }
            }, Comparator.nullsFirst(BigDecimal::compareTo));
            Comparator<DividendComparison.DividendComparisonDetail> secondComparator = comparator.thenComparing(Comparator.comparing(StockBaseInfo::getCode));
            otherStockDividends = CollUtil.sort(otherStockDividends, StrUtil.equals(SortEnum.ASC.getValue(), sort) ? secondComparator : secondComparator.reversed());
        } catch (Exception e) {
            log.error("排序失败", e);
        }

        comparisonDetailList = CollUtil.unionAll(Collections.singletonList(current), otherStockDividends);
        List<DividendComparison.DividendComparisonDetail> dividendComparisonDetails =comparisonDetailList.stream().skip((req.getCurrentPage() - 1) * req.getPageSize()).limit(req.getPageSize()).collect(Collectors.toList());
        // List<DividendComparison.DividendComparisonDetail> dividendComparisonDetails = comparisonDetailList.subList(start.intValue(), (int) end);

        dividendComparison.setRecord(dividendComparisonDetails);
        dividendComparison.setCurrent(req.getCurrentPage());
        dividendComparison.setSize(req.getPageSize());
        dividendComparison.setTotal((long) comparisonDetailList.size());
        return ResultT.success(dividendComparison);
    }

    @Override
    public ResultT<SimplePageResp<StockRepurchase>> stockRepurchase(@Valid F10PageBaseReq req) {
        // Page<Xnhk0602> xnhk0602Page = xnhk0602Mapper.selectPage(new Page<>(req.getCurrentPage(), req.getPageSize()),
        //         new QueryWrapper<Xnhk0602>().eq("seccode", req.getStockCode()).orderByDesc("F001D"));
        //
        // List<StockRepurchase> collect = xnhk0602Page.getRecords().stream().map(xnhk0602 -> {
        //     StockRepurchase stockRepurchase = new StockRepurchase();
        //
        //     stockRepurchase.setRepurchaseDate(dividendHandler.dateFormatSlash(xnhk0602.getF001d()));
        //     stockRepurchase.setRepurchaseAmount(xnhk0602.getF011n());
        //     stockRepurchase.setRepurchaseQuantity(xnhk0602.getF009n());
        //     stockRepurchase.setRepurchaseHighestPrice(xnhk0602.getF003n());
        //     stockRepurchase.setRepurchaseLowestPrice(xnhk0602.getF005n());
        //     stockRepurchase.setRepurchaseAvgPrice(xnhk0602.getF007n());
        //     stockRepurchase.setCumulativeRepurchaseQuantity(xnhk0602.getF013n());
        //     stockRepurchase.setCurrency(xnhk0602.getF002v());
        //     return stockRepurchase;
        //
        // }).collect(Collectors.toList());

        List<Xnhk0602> xnhk0602s = xnhk0602Mapper.selectList(Wrappers.<Xnhk0602>lambdaQuery().eq(Xnhk0602::getSeccode, req.getStockCode()).orderByDesc(Xnhk0602::getF001d));

        List<StockRepurchase> repurchaseList = CollUtil.defaultIfEmpty(xnhk0602s, Collections.emptyList()).stream().map(xnhk0602 -> {
            StockRepurchase stockRepurchase = new StockRepurchase();
            stockRepurchase.setRepurchaseDate(dividendHandler.dateFormatSlash(xnhk0602.getF001d()));
            stockRepurchase.setRepurchaseAmount(xnhk0602.getF011n());
            stockRepurchase.setRepurchaseQuantity(xnhk0602.getF009n());
            stockRepurchase.setRepurchaseHighestPrice(xnhk0602.getF003n());
            stockRepurchase.setRepurchaseLowestPrice(xnhk0602.getF005n());
            stockRepurchase.setRepurchaseAvgPrice(xnhk0602.getF007n());
            stockRepurchase.setCumulativeRepurchaseQuantity(xnhk0602.getF013n());
            stockRepurchase.setCurrency(xnhk0602.getF002v());
            return stockRepurchase;
        }).collect(Collectors.toList());

        HashMap<String, List<StockRepurchase>> yearListMap = repurchaseList.stream().collect(
                Collectors.groupingBy(t -> StrUtil.sub(t.getRepurchaseDate(), 0, 4),
                        HashMap::new, Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream().sorted(Comparator.comparing(StockRepurchase::getRepurchaseDate)).collect(Collectors.toList())
                        )
                )
        );

        List<StockRepurchase> records = yearListMap.values().stream().peek(list -> {
            BigDecimal increase = BigDecimal.ZERO;
            for (StockRepurchase repurchase : list) {
                increase = NumberUtil.add(increase, repurchase.getRepurchaseQuantity());
                // 年初至今累计回购数量 从年初进行统计
                repurchase.setCumulativeRepurchaseQuantity(increase);
            }
        }).reduce(new ArrayList<>(), CollUtil::unionAll)
                .stream().sorted(Comparator.comparing(StockRepurchase::getRepurchaseDate).reversed())
                .skip((req.getCurrentPage() - 1) * req.getPageSize()).limit(req.getPageSize()).collect(Collectors.toList());;

        SimplePageResp<StockRepurchase> simplePageResp = new SimplePageResp<>();
        simplePageResp.setTotal((long) CollUtil.size(repurchaseList));
        simplePageResp.setRecord(records);
        simplePageResp.setSize(simplePageResp.getSize());
        simplePageResp.setCurrent(simplePageResp.getCurrent());
        return ResultT.success(simplePageResp);
    }

    @Override
    public ResultT<SimplePageResp<SplitMerger>> splitMerger(@Valid F10PageBaseReq req) {

        Long listingDate = xnhks0101Mapper.getStockListingDate(req.getStockCode());
        List<Xnhks0314> xnhks0314s = xnhks0314Mapper.selectList(new QueryWrapper<Xnhks0314>().eq("seccode", req.getStockCode()));
        // 按除净日倒序
        List<Xnhks0319> xnhks0319s = xnhks0319Mapper.selectList(new QueryWrapper<Xnhks0319>().eq("seccode", req.getStockCode()).gt(ObjectUtil.isNotEmpty(listingDate), "F001D", listingDate).isNotNull("F002D").orderByDesc("F002D", "XDBMASK"));
        List<Xnhks0320> xnhks0320s = xnhks0320Mapper.selectList(new QueryWrapper<Xnhks0320>().eq("seccode", req.getStockCode()).gt(ObjectUtil.isNotEmpty(listingDate), "F001D", listingDate).isNotNull("F002D").orderByDesc("F002D", "XDBMASK"));
        Map<Long, Xnhks0314> xnhks0314Map = xnhks0314s.stream().collect(Collectors.toMap(Xnhks0314::getF001d, Function.identity(), (v1, v2) -> v1));
        List<SplitMerger> splitMergers = xnhks0319s.stream().map(item -> {
            SplitMerger splitMerger = new SplitMerger();
            splitMerger.setPublicationDate(dividendHandler.dateFormatSlash(item.getF001d()));
            splitMerger.setPublicationDateLong(item.getF001d());
            splitMerger.setRecombinationType("拆股");

            splitMerger.setSchemeDescription(item.getF003v());

            splitMerger.setEventStatus(dealYNDelay(item.getF004v()));

            splitMerger.setExDate(dividendHandler.dateFormatSlash(item.getF002d()));
            splitMerger.setYear(StrUtil.sub(splitMerger.getExDate(), 0, 4));

            Xnhks0314 xnhks0314 = xnhks0314Map.get(item.getF001d());

            splitMerger(splitMerger, xnhks0314);
            return splitMerger;
        }).collect(Collectors.toList());

        List<SplitMerger> splitMergerList = xnhks0320s.stream().map(item -> {
            SplitMerger splitMerger = new SplitMerger();
            splitMerger.setPublicationDate(dividendHandler.dateFormatSlash(item.getF001d()));
            splitMerger.setPublicationDateLong(item.getF001d());
            splitMerger.setRecombinationType("并股");

            splitMerger.setSchemeDescription(item.getF003v());

            splitMerger.setEventStatus(dealYNDelay(item.getF004v()));

            splitMerger.setExDate(dividendHandler.dateFormatSlash(item.getF002d()));
            splitMerger.setYear(StrUtil.sub(splitMerger.getExDate(), 0, 4));
            Xnhks0314 xnhks0314 = xnhks0314Map.get(item.getF001d());

            splitMerger(splitMerger, xnhks0314);
            return splitMerger;
        }).collect(Collectors.toList());

        splitMergerList.addAll(splitMergers);
        splitMergerList.sort(Comparator.comparing(SplitMerger::getPublicationDateLong).reversed());
        long start = (req.getCurrentPage() - 1) * req.getPageSize();
        if (start > splitMergerList.size()) {
            return ResultT.success();
        }
        long end = splitMergerList.size() < (start + req.getPageSize()) ? splitMergerList.size() : start + req.getPageSize();
        List<SplitMerger> mergerList = splitMergerList.subList((int) start, (int) end);
        SimplePageResp<SplitMerger> simplePageResp = new SimplePageResp();
        simplePageResp.setCurrent(req.getCurrentPage());
        simplePageResp.setSize(req.getPageSize());
        simplePageResp.setRecord(mergerList);
        simplePageResp.setTotal((long) splitMergerList.size());
        return ResultT.success(simplePageResp);
    }

    @Override
    public List<CompanyEventVo> hkCompanyEvent(CompanyEventReq req) {
        List<EventDTO> eventList = new ArrayList<>();

        String code = req.getCode();
        //获取除权事件
        List<Xnhk0127> xnhk0127ByCode = xnhk0127Mapper.selectList(new QueryWrapper<Xnhk0127>().eq("SECCODE",code)
                .ne("F007N",0));
        if (CollUtil.isNotEmpty(xnhk0127ByCode)) {
            List<EventDTO> divident = xnhk0127ByCode.stream().filter(item -> item.getF003d() != null)
                    .map(xnhk0127 -> {
                        EventDTO eventDTO = new EventDTO();
                        eventDTO.setTime(LocalDateTimeUtil.getTimestamp(xnhk0127.getF003d(), ZoneOffset.ofHours(8)));
                        eventDTO.setExContent(xnhk0127.getF006v());
                        String f002v = xnhk0127.getF002v().split(",")[0];
                        eventDTO.setEventType(EventConstants.EVENT_RELATION.get(f002v));
                        return eventDTO;
                    }).collect(Collectors.toList());
            eventList.addAll(divident);
        }

        //财报事件 0 =非金融 1=金融 2=保险
        int marketType = getMarketType(code);
        List<EventDTO> finEvent = null;
        switch (marketType){
            case 0:
                finEvent = noFinEvent(code);
                break;
            case 1:
                finEvent = finEvent(code);
                break;
            case 2:
                finEvent = insEvent(code);
                break;
            default :
                finEvent = Lists.newArrayList();
        }
        eventList.addAll(finEvent);

        //根据请求时间去过滤数据
        Long startTime = req.getStartTime();
        Long endTime = req.getEndTime();
        eventList = eventList.stream().filter(item -> {
            return (startTime == null || item.getTime() >= startTime) && (endTime == null || item.getTime() <= endTime);
        }).collect(Collectors.toList());

        //根据类型去划分
        Map<Long, List<EventDTO>> eventMap = eventList.stream().collect(Collectors.groupingBy(item -> convertTime(req.getType(), LocalDateTimeUtil.getLocalDate(item.getTime()))));
        List<CompanyEventVo> result = Lists.newArrayList();
        for (Map.Entry<Long, List<EventDTO>> entry : eventMap.entrySet()) {
            CompanyEventVo companyEventVo = new CompanyEventVo();
            companyEventVo.setTime(entry.getKey());
            companyEventVo.setEventList(entry.getValue());
            result.add(companyEventVo);
        }
        result = result.stream().sorted(Comparator.comparing(CompanyEventVo::getTime).reversed()).collect(Collectors.toList());

        return result;
    }

    /**
     * 获取市场类型 0 =非金融 1=金融 2=保险
     *
     * @param code
     * @return
     */
    private int getMarketType(String code) {
        // int marketType = -1;
        // Xnhks0101 xnhks0101 = xnhks0101Mapper.selectOne(new QueryWrapper<Xnhks0101>()
        //         .eq("seccode", code));
        // if (xnhks0101 != null && org.apache.dubbo.common.utils.StringUtils.isNotEmpty(xnhks0101.getF026v())) {
        //     marketType = Integer.parseInt(xnhks0101.getF026v());
        // }
        // return marketType;
        return f10SourceService.getMarketType(code);
    }

    //非金融财报数据
    private List<EventDTO> noFinEvent(String code){
        F10PageResp<F10KeyFiguresNonFinancialEntity> resp = f10KeyFiguresDao.pageNonFinancial(F10PageReq.<F10CommonRequest>builder()
                .currentPage(0)
                .pageSize(Integer.MAX_VALUE)
                .params(F10CommonRequest.builder()
                        .reportId(0)
                        .stockCode(code)
                        .build()).build());
        List<F10KeyFiguresNonFinancialEntity> record = resp.getRecord();
        if(CollUtil.isEmpty(record)){
            return Lists.newArrayList();
        }
        Predicate<F10KeyFiguresNonFinancialEntity> predicate = item -> !IGNORE_REPORT.contains(item.getReportType())
                && (Objects.nonNull(item.getKeyFigures().getNetProfits()) && Objects.nonNull(item.getKeyFigures().getNetProfits()));
        return record.stream().
                filter(predicate).
                map(item -> {
                    EventDTO eventDTO= new EventDTO();
                    eventDTO.setTime(item.getReleaseTimestamp());
                    eventDTO.setEventType(EventConstants.EVENT_RELATION.get(item.getReportType()));
                    eventDTO.setNetProfits(item.getKeyFigures().getNetProfits().getVal());
                    eventDTO.setOperatingRevenue(item.getKeyFigures().getOperatingRevenue().getVal());
                    return eventDTO;
                }).collect(Collectors.toList());
    }

    //金融财报数据
    private List<EventDTO> finEvent(String code){
        F10PageResp<F10KeyFiguresFinancialEntity> resp = f10KeyFiguresDao.pageFinancial(F10PageReq.<F10CommonRequest>builder()
                .currentPage(0)
                .pageSize(Integer.MAX_VALUE)
                .params(F10CommonRequest.builder()
                        .reportId(0)
                        .stockCode(code)
                        .build()).build());
        List<F10KeyFiguresFinancialEntity> record = resp.getRecord();
        if(CollUtil.isEmpty(record)){
            return Lists.newArrayList();
        }
        return record.stream().filter(item -> !IGNORE_REPORT.contains(item.getReportType())).map(item -> {
            EventDTO eventDTO= new EventDTO();
            eventDTO.setTime(item.getEndTimestamp());
            eventDTO.setEventType(EventConstants.EVENT_RELATION.get(item.getReportType()));
            eventDTO.setNetProfits(item.getKeyFigures().getNetProfits().getVal());
            eventDTO.setOperatingRevenue(item.getKeyFigures().getOperatingRevenue().getVal());
            return eventDTO;
        }).collect(Collectors.toList());
    }

    //保险财报数据
    private List<EventDTO> insEvent(String code){
        F10PageResp<F10KeyFiguresInsuranceEntity> resp = f10KeyFiguresDao.pageInsurance(F10PageReq.<F10CommonRequest>builder()
                .currentPage(0)
                .pageSize(Integer.MAX_VALUE)
                .params(F10CommonRequest.builder()
                        .reportId(0)
                        .stockCode(code)
                        .build()).build());
        List<F10KeyFiguresInsuranceEntity> record = resp.getRecord();
        if(CollUtil.isEmpty(record)){
            return Lists.newArrayList();
        }
        return record.stream().filter(item -> !IGNORE_REPORT.contains(item.getReportType())).map(item -> {
            EventDTO eventDTO= new EventDTO();
            eventDTO.setTime(item.getEndTimestamp());
            eventDTO.setEventType(EventConstants.EVENT_RELATION.get(item.getReportType()));
            eventDTO.setNetProfits(item.getKeyFigures().getNetProfits().getVal());
            eventDTO.setOperatingRevenue(item.getKeyFigures().getOperatingRevenue().getVal());
            return eventDTO;
        }).collect(Collectors.toList());
    }

    //根据不同的k线类型去划分
    private long convertTime(String type, LocalDate time){
        if(StringUtils.isEmpty(type)){
            return LocalDateTimeUtil.getTimestamp(time);
        }
        switch (type){
            case "week":
                return LocalDateTimeUtil
                        .getTimestamp(time.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
            case "month":
                return LocalDateTimeUtil
                        .getTimestamp(time.withDayOfMonth(1));

            case "quarter":
                return LocalDateTimeUtil
                        .getTimestamp(LocalDateTimeUtil.getStartDayOfQuarter(time));

            case "year":
                return LocalDateTimeUtil
                        .getTimestamp(time.with(TemporalAdjusters.firstDayOfYear()));
            default :
                return LocalDateTimeUtil.getTimestamp(time);

        }
    }


    private String codeDeal(String code) {
        return String.format("%05d", Integer.parseInt(code)).concat(".hk");
    }

    private void splitMerger(SplitMerger splitMerger, Xnhks0314 xnhks0314) {
        if (xnhks0314 != null) {

            splitMerger.setParallelSecuritiesCode(codeDeal(xnhks0314.getF003v()));

            splitMerger.setParallelSecuritiesName(xnhks0314.getF005v());

            splitMerger.setParallelUnit(xnhks0314.getF004n());

            splitMerger.setParallelReason(TransactionParallelismReasonEnum.getValue(xnhks0314.getF010v()));

            splitMerger.setParallelStartDate(dividendHandler.dateFormatSlash(xnhks0314.getF006d()));

            splitMerger.setParallelSuspendDate(xnhks0314.getF008v());
            splitMerger.setParallelDate(xnhks0314.getF009v());

        }
    }
}
