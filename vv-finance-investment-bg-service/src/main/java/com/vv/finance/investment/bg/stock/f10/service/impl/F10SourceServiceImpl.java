package com.vv.finance.investment.bg.stock.f10.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.dto.resp.FinancialReportDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.enums.ComReportTypeEnum;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.impl.f10.F10TableTemplateApiImpl;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.ReportTypeEnum;
import com.vv.finance.investment.bg.entity.f10.*;
import com.vv.finance.investment.bg.entity.f10.enums.F10MarketTypeEnum;
import com.vv.finance.investment.bg.entity.f10.fintable.*;
import com.vv.finance.investment.bg.entity.f10.shareholder.StockHolderChange;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.mongo.dao.F10AssetsLiabilitiesDao;
import com.vv.finance.investment.bg.mongo.dao.F10CashFlowDao;
import com.vv.finance.investment.bg.mongo.dao.F10KeyFiguresDao;
import com.vv.finance.investment.bg.mongo.dao.F10ProfitDao;
import com.vv.finance.investment.bg.mongo.model.*;
import com.vv.finance.investment.bg.stock.f10.handler.F10DuPontHandler;
import com.vv.finance.investment.bg.stock.f10.handler.F10SourceHandler;
import com.vv.finance.investment.bg.stock.f10.handler.F10SourceHandlerV2;
import com.vv.finance.investment.bg.stock.f10.service.IStockHolderChangeService;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName F10SourceServiceImpl
 * @Deacription F10 接口实现
 * @Author lh.sz
 * @Date 2021年07月22日 17:47
 **/
@Service
@Slf4j
public class F10SourceServiceImpl {

    @Resource
    Xnhks0101Mapper xnhks0101Mapper;
    @Resource
    Xnhk0203Mapper xnhk0203Mapper;
    @Resource
    Xnhk0204Mapper xnhk0204Mapper;
    @Resource
    Xnhk0206Mapper xnhk0206Mapper;
    @Resource
    Xnhk0205Mapper xnhk0205Mapper;
    @Resource
    Xnhk0207Mapper xnhk0207Mapper;
    @Resource
    Xnhk0208Mapper xnhk0208Mapper;
    @Resource
    Xnhk0201Mapper xnhk0201Mapper;
    @Resource
    Xnhk0202Mapper xnhk0202Mapper;
    @Resource
    Xnhk0210Mapper xnhk0210Mapper;
    @Resource
    F10KeyFiguresDao f10KeyFiguresDao;
    @Resource
    F10ProfitDao f10ProfitDao;
    @Resource
    F10AssetsLiabilitiesDao f10AssetsLiabilitiesDao;
    @Resource
    F10CashFlowDao f10CashFlowDao;
    @Resource
    F10TableTemplateApiImpl f10TableTemplateApi;
    @Resource
    F10SourceHandler f10SourceHandler;
    @Resource
    F10SourceHandlerV2 f10SourceHandlerV2;
    @Resource
    F10DuPontHandler f10DuPontHandler;
    private static final String TIME_PARSE = "yyyy";
    @Resource
    RedisClient redisClient;

    @Resource
    F10ChartServiceImpl f10ChartService;
    @Resource
    MongoTemplate mongoTemplate;
    @Resource
    IStockHolderChangeService stockHolderChangeService;
    @Resource
    StockCache stockCache;

    /**
     * 获取f10所有表格数据
     *
     * @param code       股票代码
     * @param reportType 报告类型
     * @return
     */
    public List<List<F10TableTemplate>> getF10Table(
            String code,
            String reportType,
            Long reportTime,
            int tableType
    ) {
        List<List<F10TableTemplate>> respList = new ArrayList<>();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().stockCode(code).
                reportType(reportType).reportTime
                (reportTime).reportId(0).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(10)
                .currentPage(0)
                .build();
        //获取市场类型 0 =非金融 1=金融 2=保险
        int marketType = getMarketType(code);
        F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);
        if (null != f10MarketTypeEnum) {
            //获取不同种类的表格类型
            int tableSource = getTableSourceType(f10MarketTypeEnum, tableType);
            if (0 != tableSource) {
                List<F10TableTemplate> list = f10TableTemplateApi.getList(tableSource);
                respList = f10SourceHandler.builderF10Source(f10PageReq, tableSource, list);
            }
        }
        return respList;
    }

    public List<List<F10TableTemplateV2>> getF10TableV2(String code, String reportType, int tableType, long reportTime) {
        List<List<F10TableTemplateV2>> resultList = new ArrayList<>();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().stockCode(code).reportType(reportType).reportTime(reportTime).reportId(0).build();
        F10PageReq f10PageReq = F10PageReq.builder().params(f10CommonRequest).desc(true).pageSize(10).currentPage(0).build();
        //获取市场类型 0 =非金融 1=金融 2=保险
        int marketType = getMarketType(code);
        F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);

        if (null != f10MarketTypeEnum) {
            int tableSource = getTableSourceType(f10MarketTypeEnum, tableType);
            if (0 != tableSource) {
                List<F10TableTemplateV2> list = f10TableTemplateApi.getListV2(tableSource);
                F10PageResp<F10EntityBase> f10Resp = f10SourceHandlerV2.f10PageResp(f10PageReq, tableSource);
                if (CollUtil.isNotEmpty(f10Resp.getRecord())) {
                    return f10SourceHandlerV2.buildTables(list, f10Resp.getRecord());
                }
            }
        }

        return resultList;
    }

    /**
     * 获取PCf10财务数据
     *
     * @param code      股票代码
     * @param reportId  报告种类
     * @param tableType 表格类型
     * @param current   当前多少页
     * @param pageSize  多少条
     * @return
     */
    public List<List<F10TableTemplate>> getPCF10Table(
            String code,
            int reportId,
            int tableType,
            long current,
            long pageSize
    ) {

        F10CommonRequest f10CommonRequest = F10CommonRequest.builder()
                .stockCode(code)
                .reportId(reportId).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(Integer.parseInt(Long.toString(pageSize)))
                .currentPage(Integer.parseInt(Long.toString(current)) - 1)
                .build();
        //查询数据库表获取 市场类型
        int marketType = getMarketType(code);
        F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);
        if (null != f10MarketTypeEnum) {
            int tableSource = getTableSourceType(f10MarketTypeEnum, tableType);
            if (0 != tableSource) {
                List<F10TableTemplate> list = f10TableTemplateApi.getList(tableSource);
                List<List<F10TableTemplate>> lists = f10SourceHandler.builderF10Source(f10PageReq, tableSource, list);
//                //过滤 P 类报告
//                if (lists.size()>=5) {
//                    log.info("P 类报告"+lists.get(4).get(0).getFieldValue());
//                }
                return lists;
            }
        }
        return Lists.newArrayList();
    }

    public PageDomain<List<F10TableTemplateV2>> getPCF10TableV2(String code, int reportId, int tableType, long current, long pageSize) {
        PageDomain<List<F10TableTemplateV2>> pageDomain = new PageDomain<>();
        pageDomain.setCurrent(current);
        pageDomain.setSize(pageSize);
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().stockCode(code).reportId(reportId).filterPq(true).build();
        F10PageReq f10PageReq = F10PageReq.builder().params(f10CommonRequest).desc(true).pageSize(Integer.parseInt(Long.toString(pageSize))).currentPage(Integer.parseInt(Long.toString(current)) - 1).build();
        //查询数据库表获取 市场类型
        int marketType = getMarketType(code);
        F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);
        if (null != f10MarketTypeEnum) {
            int tableSource = getTableSourceType(f10MarketTypeEnum, tableType);
            if (0 != tableSource) {
                List<F10TableTemplateV2> list = f10TableTemplateApi.getListV2(tableSource);
                F10PageResp<F10EntityBase> f10Resp = f10SourceHandlerV2.f10PageResp(f10PageReq, tableSource);
                // List<List<F10TableTemplateV2>> lists = f10SourceHandlerV2.builderF10Source(f10PageReq, tableSource, list);
                // lists = lists.stream().filter(sub -> sub.stream().noneMatch(ss -> StrUtil.equals("reportType", ss.getMappedFields())
                //         && ReportTypeEnum.unResolveTypeList().contains(StrUtil.unWrap(ss.getFieldValue(), '"')))
                // ).collect(Collectors.toList());
                pageDomain.setTotal(f10Resp.getTotal());
                if (CollUtil.isNotEmpty(f10Resp.getRecord())) {
                    List<List<F10TableTemplateV2>> tableList = f10SourceHandlerV2.buildTables(list, f10Resp.getRecord());
                    pageDomain.setRecords(tableList);
                }
                return pageDomain;
            }
        }
        return pageDomain;
    }

    public List<FinancialReportDto> getFinancialReport(String id, int marketType, int pageSize, Long startTime) {
        log.info("唯一ID:{},金融类型：{}，数据大小：{}", id, marketType, pageSize);
        List<FinancialReportDto> resultList = new ArrayList<>();
        if(marketType==F10MarketTypeEnum.NO_FINANCIAL.getCode()){
            List<F10NoFinProfitEntity> list = f10KeyFiguresDao.listNonFinancial(id, pageSize, startTime);
            for(F10NoFinProfitEntity fee : list){
                FinancialReportDto financialReportDto = new FinancialReportDto();
                financialReportDto.setUniqueIdStr(fee.getId());
                financialReportDto.setCode(fee.getStockCode());
                financialReportDto.setReportStartDate(fee.getStartTimestamp());
                financialReportDto.setReportEndDate(fee.getEndTimestamp());
                financialReportDto.setReportDate(fee.getReleaseTimestamp());
                financialReportDto.setCurrencyType(fee.getCurrency());
                financialReportDto.setUpdateTime(ObjectUtils.isNotEmpty(fee.getUpdateTime()) ? fee.getUpdateTime().getTime() : null);
                financialReportDto.setReportType(fee.getReportType());
                financialReportDto.setOperatingRevenue(fee.getOperatingAndOtherRevenue().getVal());
                BigDecimal operatingCostsAndExpenses = ObjectUtils.isNotEmpty(fee.getOperatingCostsAndExpenses())
                        && ObjectUtils.isNotEmpty(fee.getOperatingCostsAndExpenses().getSellingCost()) ? fee.getOperatingCostsAndExpenses().getSellingCost().getVal() : BigDecimal.ZERO;
                financialReportDto.setOperatingCostsAndExpenses(operatingCostsAndExpenses);
                financialReportDto.setOperatingProfit(fee.getOperatingProfit().getVal());
                financialReportDto.setProfitAndLossDuringThePeriod(fee.getProfitAndLossDuringThePeriod().getVal());
                financialReportDto.setNetProfit(fee.getCommonStockholder().getVal());
                financialReportDto.setGrossMargin(fee.getOperatingCostsAndExpenses().getGrossProfit().getVal());
                financialReportDto.setBasicEarningsPerShare(fee.getBasicEarningsPerShare().getVal());
                resultList.add(financialReportDto);
            }
        }else if(marketType==F10MarketTypeEnum.FINANCIAL.getCode()){
            List<F10FinProfitEntity> list = f10KeyFiguresDao.listFinancial(id, pageSize, startTime);
            for(F10FinProfitEntity fee : list){
                FinancialReportDto financialReportDto = new FinancialReportDto();
                financialReportDto.setUniqueIdStr(fee.getId());
                financialReportDto.setCode(fee.getStockCode());
                financialReportDto.setReportStartDate(fee.getStartTimestamp());
                financialReportDto.setReportEndDate(fee.getEndTimestamp());
                financialReportDto.setReportDate(fee.getReleaseTimestamp());
                financialReportDto.setCurrencyType(fee.getCurrency());
                financialReportDto.setReportType(fee.getReportType());
                financialReportDto.setUpdateTime(ObjectUtils.isNotEmpty(fee.getUpdateTime()) ? fee.getUpdateTime().getTime() : null);
                financialReportDto.setOperatingRevenue(fee.getGrossRevenue().getVal());
//                financialReportDto.setOperatingCostsAndExpenses(fee);
                financialReportDto.setOperatingProfit(fee.getOperatingProfit().getVal());
                financialReportDto.setProfitAndLossDuringThePeriod(fee.getProfitLossDuringPeriod().getVal());
                financialReportDto.setNetProfit(fee.getCommonStockholder().getVal());
//                financialReportDto.setGrossMargin(fee.getOperatingCostsAndExpenses().getGrossProfit().getVal());
                financialReportDto.setBasicEarningsPerShare(fee.getBasicEarningsPerShare().getVal());
                resultList.add(financialReportDto);
            }
        }else if(marketType==F10MarketTypeEnum.INSURANCE.getCode()){
            List<F10InsureProfitEntity> list = f10KeyFiguresDao.listInsurance(id, pageSize, startTime);
            for(F10InsureProfitEntity fee : list){
                FinancialReportDto financialReportDto = new FinancialReportDto();
                financialReportDto.setUniqueIdStr(fee.getId());
                financialReportDto.setCode(fee.getStockCode());
                financialReportDto.setReportStartDate(fee.getStartTimestamp());
                financialReportDto.setReportEndDate(fee.getEndTimestamp());
                financialReportDto.setReportDate(fee.getReleaseTimestamp());
                financialReportDto.setCurrencyType(fee.getCurrency());
                financialReportDto.setReportType(fee.getReportType());
                financialReportDto.setUpdateTime(ObjectUtils.isNotEmpty(fee.getUpdateTime()) ? fee.getUpdateTime().getTime() : null);
                financialReportDto.setOperatingRevenue(fee.getGrossRevenue().getVal());
//                financialReportDto.setOperatingCostsAndExpenses(fee);
                financialReportDto.setOperatingProfit(fee.getOperatingProfit().getVal());
                financialReportDto.setProfitAndLossDuringThePeriod(fee.getProfitLossDuringPeriod().getVal());
                financialReportDto.setNetProfit(fee.getCommonStockholder().getVal());
//                financialReportDto.setGrossMargin(fee.getOperatingCostsAndExpenses().getGrossProfit().getVal());
                financialReportDto.setBasicEarningsPerShare(fee.getBasicEarningsPerShare().getVal());
                resultList.add(financialReportDto);
            }
        }
        List<String> codes = resultList.stream().map(item -> item.getCode()).collect(Collectors.toList());
        Map<String, ComStockSimpleDto> stockInfos = stockCache.queryLocalInfoMap(codes);
        resultList.stream().forEach(item -> {
            item.setStockId(ObjectUtils.isEmpty(stockInfos.get(item.getCode())) ? null : stockInfos.get(item.getCode()).getStockId());
        });
        return resultList;
    }

    /**
     * 获取不同种类的表格类型
     *
     * @param f10MarketTypeEnum 市场类型枚举
     * @param tableType         表格种类
     * @return
     */
    private int getTableSourceType(
            F10MarketTypeEnum f10MarketTypeEnum,
            int tableType

    ) {
        int tableSourceType = 0;
        switch (f10MarketTypeEnum) {
            case NO_FINANCIAL:
                if (tableType == F10TableTypeEnum.INDEX.getCode()) {
                    tableSourceType = TableSourceTypeEnum.INDEX_NO_FINANCE.getCode();
                }
                if (tableType == F10TableTypeEnum.PROFIT.getCode()) {
                    tableSourceType = TableSourceTypeEnum.PROFIT_NO_FINANCE.getCode();
                }
                if (tableType == F10TableTypeEnum.ASSET.getCode()) {
                    tableSourceType = TableSourceTypeEnum.ASSET_NO_FINANCE.getCode();
                }
                if (tableType == F10TableTypeEnum.CASH.getCode()) {
                    tableSourceType = TableSourceTypeEnum.CASH_FLOW.getCode();
                }
                break;
            case FINANCIAL:
                if (tableType == F10TableTypeEnum.INDEX.getCode()) {
                    tableSourceType = TableSourceTypeEnum.INDEX_FINANCE.getCode();
                }
                if (tableType == F10TableTypeEnum.PROFIT.getCode()) {
                    tableSourceType = TableSourceTypeEnum.PROFIT_FINANCE.getCode();
                }
                if (tableType == F10TableTypeEnum.ASSET.getCode()) {
                    tableSourceType = TableSourceTypeEnum.ASSET_FINANCE.getCode();
                }
                if (tableType == F10TableTypeEnum.CASH.getCode()) {
                    tableSourceType = TableSourceTypeEnum.CASH_FLOW.getCode();
                }
                break;
            case INSURANCE:
                if (tableType == F10TableTypeEnum.INDEX.getCode()) {
                    tableSourceType = TableSourceTypeEnum.INDEX_INSURANCE.getCode();
                }
                if (tableType == F10TableTypeEnum.PROFIT.getCode()) {
                    tableSourceType = TableSourceTypeEnum.PROFIT_INSURANCE.getCode();
                }
                if (tableType == F10TableTypeEnum.ASSET.getCode()) {
                    tableSourceType = TableSourceTypeEnum.ASSET_INSURANCE.getCode();
                }
                if (tableType == F10TableTypeEnum.CASH.getCode()) {
                    tableSourceType = TableSourceTypeEnum.CASH_FLOW.getCode();
                }
                break;
            default:
                break;
        }
        return tableSourceType;
    }


    /**
     * 获取主要指标图表
     *
     * @param code 股票代码
     * @param size 默认10
     * @return
     */
    public List<RatingsTableEntity> getRatingsTable(String code, Integer size) {
        int marketType = getMarketType(code);
        List<RatingsTableEntity> ratingsTableEntityList = new ArrayList<>();
        F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().reportType("F").stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(size == null ? 10 : size)
                .currentPage(0)
                .build();
        if (null != f10MarketTypeEnum) {
            switch (f10MarketTypeEnum) {
                case NO_FINANCIAL:
                    ratingsTableEntityList = getRatingsTableByNoFinancial(f10PageReq);
                    break;
                case FINANCIAL:
                    ratingsTableEntityList = getRatingsTableByFinancial(f10PageReq);
                    break;
                case INSURANCE:
                    ratingsTableEntityList = getRatingsTableByInsurance(f10PageReq);
                    break;
                default:
                    break;
            }
        }

        return ratingsTableEntityList;
    }

    /**
     * 获取现金流量表
     *
     * @param code
     * @param size 默认为10
     * @return
     */
    public List<F10CashCharEntity> getCashCharTable(String code, Integer size) {
        List<F10CashCharEntity> cashCharEntities = new ArrayList<>();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().reportType("F").stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(size == null ? 10 : size)
                .currentPage(0)
                .build();
        F10PageResp<F10CashFlowEntity> f10PageResp = f10CashFlowDao.pageCashFlow(f10PageReq);
        List<F10CashFlowEntity> entityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(entityList, f10PageReq.getPageSize());
        if (CollectionUtils.isNotEmpty(entityList)) {
            dateList.forEach(d -> {
                F10CashCharEntity f10CashCharEntity = new F10CashCharEntity();
                entityList.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        f10CashCharEntity.setFinancingCashFlow(f.getCashFlowFromFinancingActivites());
                        f10CashCharEntity.setInvestingCashFlow(f.getCashFlowFromInvestmentActivities());
                        f10CashCharEntity.setOperationalCashFlow(f.getCashFlowFromeOperations());
                        f10CashCharEntity.setCurrency(f.getCurrency());
                    }
                });
                f10CashCharEntity.setTime(DateUtils.parseDate(d).getTime());
                cashCharEntities.add(f10CashCharEntity);
            });
        }
        return cashCharEntities;
    }

    /**
     * 获取财务图表, 含类型
     *
     * @param code
     * @param size
     * @return
     */
    public F10FinancialAnalysisCharVo getFinancialCharVo(String code, Integer size) {
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().reportType("F").stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(size == null ? 10 : size)
                .currentPage(0)
                .build();
        F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(getMarketType(code));
        F10FinancialAnalysisCharVo vo = new F10FinancialAnalysisCharVo();
        vo.setMarketType(f10MarketTypeEnum == null ? null : f10MarketTypeEnum.getCode());
        vo.setFinancialAnalysisChars(listFinancialCharData(f10MarketTypeEnum, f10PageReq));
        return vo;
    }

    /**
     * 获取财务分析图表 list数据
     *
     * @param f10MarketTypeEnum
     * @param f10PageReq
     * @return
     */
    private List<F10FinancialAnalysisChar> listFinancialCharData(F10MarketTypeEnum f10MarketTypeEnum, F10PageReq f10PageReq) {
        List<F10FinancialAnalysisChar> f10FinancialAnalysisChars = new ArrayList<>();
        if (null != f10MarketTypeEnum) {
            switch (f10MarketTypeEnum) {
                case NO_FINANCIAL:
                    f10FinancialAnalysisChars = getFinancialCharBNoFin(f10PageReq);
                    break;
                case FINANCIAL:
                    f10FinancialAnalysisChars = getFinancialCharByFin(f10PageReq);
                    break;
                case INSURANCE:
                    f10FinancialAnalysisChars = getFinancialCharByInsurance(f10PageReq);
                    break;
                default:
                    break;
            }
        }
        return f10FinancialAnalysisChars;
    }

    /**
     * 获取财务分析非金融
     *
     * @param f10PageReq
     * @return
     */
    private List<F10FinancialAnalysisChar> getFinancialCharBNoFin(
            F10PageReq f10PageReq) {
        List<F10FinancialAnalysisChar> f10FinancialAnalysisChars = new ArrayList<>();
        F10PageResp<F10KeyFiguresNonFinancialEntity> f10PageResp = f10KeyFiguresDao.pageNonFinancial(f10PageReq);
        List<F10KeyFiguresNonFinancialEntity> entityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(entityList, f10PageReq.getPageSize());
        if (CollectionUtils.isNotEmpty(entityList)) {
            dateList.forEach(d -> {
                F10FinancialAnalysisChar aChar = new F10FinancialAnalysisChar();
                entityList.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        String[] profit = new String[]{
                                f.getProfitability() == null || f.getProfitability().getRoa() == null || f.getProfitability().getRoa().getVal() == null ? null : f.getProfitability().getRoa().getVal().toString(),
                                f.getProfitability() == null || f.getProfitability().getRoe() == null || f.getProfitability().getRoe().getVal() == null ? null : f.getProfitability().getRoe().getVal().toString(),
                                f.getProfitability() == null || f.getProfitability().getRoce() == null || f.getProfitability().getRoce().getVal() == null ? null : f.getProfitability().getRoce().getVal().toString()
                        };
                        String[] growth = new String[]{
                                f.getGrowthAbility() == null || f.getGrowthAbility().getOperatingRevenueGrowth() == null || f.getGrowthAbility().getOperatingRevenueGrowth().getVal() == null ? null : f.getGrowthAbility().getOperatingRevenueGrowth().getVal().toString(),
                                f.getGrowthAbility() == null || f.getGrowthAbility().getNetProfitGrowth() == null || f.getGrowthAbility().getNetProfitGrowth().getVal() == null ? null : f.getGrowthAbility().getNetProfitGrowth().getVal().toString(),
                                f.getGrowthAbility() == null || f.getGrowthAbility().getTotalAssetsGrowth() == null || f.getGrowthAbility().getTotalAssetsGrowth().getVal() == null ? null : f.getGrowthAbility().getTotalAssetsGrowth().getVal().toString()
                        };
                        String[] operating = new String[]{
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getTotalAssetsTurnover() == null || f.getOperatingCapacity().getTotalAssetsTurnover().getVal() == null ? null : f.getOperatingCapacity().getTotalAssetsTurnover().getVal().toString(),
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getInventoryTurnover() == null || f.getOperatingCapacity().getInventoryTurnover().getVal() == null ? null : f.getOperatingCapacity().getInventoryTurnover().getVal().toString(),
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getAccountsPayableTurnover() == null || f.getOperatingCapacity().getAccountsPayableTurnover().getVal() == null ? null : f.getOperatingCapacity().getAccountsPayableTurnover().getVal().toString()
                        };
                        String[] debt = new String[]{
                                f.getSolvency() == null || f.getSolvency().getTotalLiabilityAssets() == null || f.getSolvency().getTotalLiabilityAssets().getVal() == null ? null : f.getSolvency().getTotalLiabilityAssets().getVal().toString(),
                                f.getSolvency() == null || f.getSolvency().getLtLiabilityTotalAssets() == null || f.getSolvency().getLtLiabilityTotalAssets().getVal() == null ? null : f.getSolvency().getLtLiabilityTotalAssets().getVal().toString(),
                                f.getSolvency() == null || f.getSolvency().getStockholderEquityTotalAssets() == null || f.getSolvency().getStockholderEquityTotalAssets().getVal() == null ? null : f.getSolvency().getStockholderEquityTotalAssets().getVal().toString()
                        };
                        String[] cash = new String[]{
                                f.getCashability() == null || f.getCashability().getCurrentRatio() == null || f.getCashability().getCurrentRatio().getVal() == null ? null : f.getCashability().getCurrentRatio().getVal().toString(),
                                f.getCashability() == null || f.getCashability().getQuickRatio() == null || f.getCashability().getQuickRatio().getVal() == null ? null : f.getCashability().getQuickRatio().getVal().toString(),
                                f.getCashability() == null || f.getCashability().getCashRatio() == null || f.getCashability().getCashRatio().getVal() == null ? null : f.getCashability().getCashRatio().getVal().toString()
                        };

                        aChar.setProfit(profit);
                        aChar.setGrowth(growth);
                        aChar.setOperating(operating);
                        aChar.setDebt(debt);
                        aChar.setCash(cash);
                    }
                });
                aChar.setTime(DateUtils.parseDate(d).getTime());
                f10FinancialAnalysisChars.add(aChar);
            });
        }
        return f10FinancialAnalysisChars;
    }

    /**
     * 获取财务分析图表金融
     *
     * @param f10PageReq
     * @return
     */
    private List<F10FinancialAnalysisChar> getFinancialCharByFin(
            F10PageReq f10PageReq) {
        List<F10FinancialAnalysisChar> f10FinancialAnalysisChars = new ArrayList<>();
        F10PageResp<F10KeyFiguresFinancialEntity> f10PageResp = f10KeyFiguresDao.pageFinancial(f10PageReq);
        List<F10KeyFiguresFinancialEntity> entityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(entityList, f10PageReq.getPageSize());
        if (CollectionUtils.isNotEmpty(entityList)) {
            dateList.forEach(d -> {
                F10FinancialAnalysisChar aChar = new F10FinancialAnalysisChar();
                entityList.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        String[] profit = new String[]{
                                f.getProfitability() == null || f.getProfitability().getRoa() == null || f.getProfitability().getRoa().getVal() == null ? null : f.getProfitability().getRoa().getVal().toString(),
                                f.getProfitability() == null || f.getProfitability().getRoe() == null || f.getProfitability().getRoe().getVal() == null ? null : f.getProfitability().getRoe().getVal().toString(),
                                f.getProfitability() == null || f.getProfitability().getLoanReturn() == null || f.getProfitability().getLoanReturn().getVal() == null ? null : f.getProfitability().getLoanReturn().getVal().toString()
                        };
                        String[] growth = new String[]{
                                f.getGrowthAbility() == null || f.getGrowthAbility().getOperatingRevenueGrowth() == null || f.getGrowthAbility().getOperatingRevenueGrowth().getVal() == null ? null : f.getGrowthAbility().getOperatingRevenueGrowth().getVal().toString(),
                                f.getGrowthAbility() == null || f.getGrowthAbility().getNetProfitGrowth() == null || f.getGrowthAbility().getNetProfitGrowth().getVal() == null ? null : f.getGrowthAbility().getNetProfitGrowth().getVal().toString(),
                                f.getGrowthAbility() == null || f.getGrowthAbility().getTotalAssetsGrowth() == null || f.getGrowthAbility().getTotalAssetsGrowth().getVal() == null ? null : f.getGrowthAbility().getTotalAssetsGrowth().getVal().toString()
                        };
                        String[] operating = new String[]{
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getAverageLiquidityRatio().getVal() == null ? null : f.getOperatingCapacity().getAverageLiquidityRatio().getVal().toString(),
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getRecapitalizationRatio().getVal() == null ? null : f.getOperatingCapacity().getRecapitalizationRatio().getVal().toString(),
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getRestructuredLoanRatio().getVal() == null ? null : f.getOperatingCapacity().getRestructuredLoanRatio().getVal().toString()
                        };
                        String[] debt = new String[]{
                                f.getSolvency() == null || f.getSolvency().getLoansDeposits() == null || f.getSolvency().getLoansDeposits().getVal() == null ? null : f.getSolvency().getLoansDeposits().getVal().toString(),
                                f.getSolvency() == null || f.getSolvency().getDepositsTotalAssets() == null || f.getSolvency().getDepositsTotalAssets().getVal() == null ? null : f.getSolvency().getDepositsTotalAssets().getVal().toString(),
                                f.getSolvency() == null || f.getSolvency().getStockholderEquityTotalAssets() == null || f.getSolvency().getStockholderEquityTotalAssets().getVal() == null ? null : f.getSolvency().getStockholderEquityTotalAssets().getVal().toString()
                        };

                        aChar.setProfit(profit);
                        aChar.setGrowth(growth);
                        aChar.setOperating(operating);
                        aChar.setDebt(debt);
                    }
                });
                aChar.setTime(DateUtils.parseDate(d).getTime());
                f10FinancialAnalysisChars.add(aChar);
            });
        }
        return f10FinancialAnalysisChars;
    }

    /**
     * 获取财务分析保险
     *
     * @param f10PageReq
     * @return
     */
    private List<F10FinancialAnalysisChar> getFinancialCharByInsurance(
            F10PageReq f10PageReq) {
        List<F10FinancialAnalysisChar> f10FinancialAnalysisChars = new ArrayList<>();
        F10PageResp<F10KeyFiguresInsuranceEntity> f10PageResp = f10KeyFiguresDao.pageInsurance(f10PageReq);
        List<F10KeyFiguresInsuranceEntity> entityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(entityList, f10PageReq.getPageSize());
        if (CollectionUtils.isNotEmpty(entityList)) {
            dateList.forEach(d -> {
                F10FinancialAnalysisChar aChar = new F10FinancialAnalysisChar();
                entityList.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        String[] profit = new String[]{
                                f.getProfitability() == null || f.getProfitability().getRoa() == null || f.getProfitability().getRoa().getVal() == null ? null : f.getProfitability().getRoa().getVal().toString(),
                                f.getProfitability() == null || f.getProfitability().getRoe() == null || f.getProfitability().getRoe().getVal() == null ? null : f.getProfitability().getRoe().getVal().toString(),
                                f.getProfitability() == null || f.getProfitability().getNetProfitRatio() == null || f.getProfitability().getNetProfitRatio().getVal() == null ? null : f.getProfitability().getNetProfitRatio().getVal().toString()
                        };
                        String[] growth = new String[]{
                                f.getGrowthAbility() == null || f.getGrowthAbility().getOperatingRevenueGrowth() == null || f.getGrowthAbility().getOperatingRevenueGrowth().getVal() == null ? null : f.getGrowthAbility().getOperatingRevenueGrowth().getVal().toString(),
                                f.getGrowthAbility() == null || f.getGrowthAbility().getNetProfitGrowth() == null || f.getGrowthAbility().getNetProfitGrowth().getVal() == null ? null : f.getGrowthAbility().getNetProfitGrowth().getVal().toString(),
                                f.getGrowthAbility() == null || f.getGrowthAbility().getTotalAssetsGrowth() == null || f.getGrowthAbility().getTotalAssetsGrowth().getVal() == null ? null : f.getGrowthAbility().getTotalAssetsGrowth().getVal().toString()
                        };
                        String[] operating = new String[]{
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getNetInvestmentIncomeGrowth() == null || f.getOperatingCapacity().getNetInvestmentIncomeGrowth().getVal() == null ? null : f.getOperatingCapacity().getNetInvestmentIncomeGrowth().getVal().toString(),
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getInsuranceReserveGrowth() == null || f.getOperatingCapacity().getInsuranceReserveGrowth().getVal() == null ? null : f.getOperatingCapacity().getInsuranceReserveGrowth().getVal().toString(),
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getEarnedPremiumsPolicyFeeGrowth() == null || f.getOperatingCapacity().getEarnedPremiumsPolicyFeeGrowth().getVal() == null ? null : f.getOperatingCapacity().getEarnedPremiumsPolicyFeeGrowth().getVal().toString()
                        };
                        String[] debt = new String[]{
                                f.getSolvency() == null || f.getSolvency().getTotalInvestmentAssets() == null || f.getSolvency().getTotalInvestmentAssets().getVal() == null ? null : f.getSolvency().getTotalInvestmentAssets().getVal().toString(),
                                f.getSolvency() == null || f.getSolvency().getTotalEquityTotalAssets() == null || f.getSolvency().getTotalEquityTotalAssets().getVal() == null ? null : f.getSolvency().getTotalEquityTotalAssets().getVal().toString(),
                                f.getSolvency() == null || f.getSolvency().getStockholderEquityTotalAssets() == null || f.getSolvency().getStockholderEquityTotalAssets().getVal() == null ? null : f.getSolvency().getStockholderEquityTotalAssets().getVal().toString()
                        };

                        aChar.setProfit(profit);
                        aChar.setGrowth(growth);
                        aChar.setOperating(operating);
                        aChar.setDebt(debt);
                    }
                });
                aChar.setTime(DateUtils.parseDate(d).getTime());
                f10FinancialAnalysisChars.add(aChar);
            });
        }
        return f10FinancialAnalysisChars;
    }


    /**
     * 获取主要指标图表(非金融)
     *
     * @param f10PageReq
     * @return
     */
    private List<RatingsTableEntity> getRatingsTableByNoFinancial(F10PageReq f10PageReq) {
        List<RatingsTableEntity> list = new ArrayList<>();
        F10PageResp<F10KeyFiguresNonFinancialEntity> f10PageResp = f10KeyFiguresDao.pageNonFinancial(f10PageReq);
        List<F10KeyFiguresNonFinancialEntity> entityList = f10PageResp.getRecord();
        List<String> dateList = f10ChartService.getTenYearDate(entityList, f10PageReq.getPageSize());
        if (CollectionUtils.isNotEmpty(entityList)) {
            List<String> currencyList = entityList.stream().map(F10KeyFiguresNonFinancialEntity::getCurrency).distinct().collect(Collectors.toList());
            dateList.forEach(d -> {
                RatingsTableEntity ratingsTableEntity = new RatingsTableEntity();
                entityList.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        ratingsTableEntity.setEarningPerShare(null == f.getPerShareIndicator().getEarningPerShare() ? null : f.getPerShareIndicator().getEarningPerShare());
                        ratingsTableEntity.setNetAssetPerShare(null == f.getPerShareIndicator().getNetAssetPerShare() ? null : f.getPerShareIndicator().getNetAssetPerShare());
                        ratingsTableEntity.setCashFlowPerShare(null == f.getPerShareIndicator().getCashFlowPerShare() ? null : f.getPerShareIndicator().getCashFlowPerShare());
                        ratingsTableEntity.setOperatingRevenue(null == f.getKeyFigures().getOperatingRevenue() ? null : f.getKeyFigures().getOperatingRevenue());
                        ratingsTableEntity.setRoa(null == f.getProfitability().getRoa() ? null : f.getProfitability().getRoa());
                        ratingsTableEntity.setRoe(null == f.getProfitability().getRoe() ? null : f.getProfitability().getRoe());
                        ratingsTableEntity.setNetProfits(null == f.getKeyFigures().getNetProfits() ? null : f.getKeyFigures().getNetProfits());
                        ratingsTableEntity.setNetProfitRatio(f.getProfitability().getNetProfitRatio() == null ? null : f.getProfitability().getNetProfitRatio());
//                        ratingsTableEntity.setStockTime(f.getReleaseTimestamp());
                    }
                });
                ratingsTableEntity.setTime(DateUtils.parseDate(d).getTime());
                ratingsTableEntity.setMonetaryUnit(CollUtil.isEmpty(currencyList) ? null : currencyList.get(0));
                list.add(ratingsTableEntity);
            });
        }
        return list;
    }

    /**
     * 获取最新主要指标摘要数据
     *
     * @param code 股票代码
     * @return
     */
    public RatingsDigestTableEntity getLatestRatingsDigestTable(String code) {
        int marketType = getMarketType(code);
        RatingsDigestTableEntity tableEntity = new RatingsDigestTableEntity();
        F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);
        if (null != f10MarketTypeEnum) {
            tableEntity.setMarketType(f10MarketTypeEnum.getCode());
            switch (f10MarketTypeEnum) {
                case NO_FINANCIAL:
                    F10KeyFiguresNonFinancialEntity latestRatingsTableByNoFinancial = getLatestRatingsTableByNoFinancial(code);
                    if (latestRatingsTableByNoFinancial == null) {
                        break;
                    }
                    tableEntity.setCashability(latestRatingsTableByNoFinancial.getCashability() == null ? null : new CashabilityDigestVo(latestRatingsTableByNoFinancial.getCashability().getCurrentRatio(), latestRatingsTableByNoFinancial.getCashability().getQuickRatio()));
                    tableEntity.setGrowthAbility(latestRatingsTableByNoFinancial.getGrowthAbility() == null ? null : new GrowthAbilityDigestVo(latestRatingsTableByNoFinancial.getGrowthAbility().getOperatingRevenueGrowth(), latestRatingsTableByNoFinancial.getGrowthAbility().getNetProfitGrowth()));
                    tableEntity.setReportType(latestRatingsTableByNoFinancial.getReportType());
                    tableEntity.setKeyFigures(latestRatingsTableByNoFinancial.getKeyFigures() == null ? null : new KeyFiguresDigestVo(latestRatingsTableByNoFinancial.getKeyFigures().getOperatingRevenue(), latestRatingsTableByNoFinancial.getKeyFigures().getNetProfits()));
                    tableEntity.setOperatingCapacity(latestRatingsTableByNoFinancial.getOperatingCapacity() == null ? null : new OperatingCapacityDigestVo(latestRatingsTableByNoFinancial.getOperatingCapacity().getTotalAssetsTurnover(), latestRatingsTableByNoFinancial.getOperatingCapacity().getCurrentAssetsTurnover()));
                    tableEntity.setSolvency(latestRatingsTableByNoFinancial.getSolvency() == null ? null : new SolvencyDigestVo(latestRatingsTableByNoFinancial.getSolvency().getTotalLiabilityAssets(), latestRatingsTableByNoFinancial.getSolvency().getStockholderEquityTotalAssets()));
                    tableEntity.setPerShareIndicator(latestRatingsTableByNoFinancial.getPerShareIndicator() == null ? null : new PerShareIndicatorDigestVo(latestRatingsTableByNoFinancial.getPerShareIndicator().getEarningPerShare(), latestRatingsTableByNoFinancial.getPerShareIndicator().getNetAssetPerShare()));
                    tableEntity.setProfitability(latestRatingsTableByNoFinancial.getProfitability() == null ? null : new ProfitabilityDigestVo(latestRatingsTableByNoFinancial.getProfitability().getRoe(), latestRatingsTableByNoFinancial.getProfitability().getRoa()));
                    tableEntity.setTime(latestRatingsTableByNoFinancial.getReleaseTimestamp() == null ? null : latestRatingsTableByNoFinancial.getReleaseTimestamp());
                    break;
                case FINANCIAL:
                    F10KeyFiguresFinancialEntity latestRatingsTableByFinancial = getLatestRatingsTableByFinancial(code);
                    if (latestRatingsTableByFinancial == null) {
                        break;
                    }
                    tableEntity.setCashability(latestRatingsTableByFinancial.getCashability() == null ? null : new CashabilityDigestVo(latestRatingsTableByFinancial.getCashability().getCashRatio(), latestRatingsTableByFinancial.getCashability().getQuickRatio()));
                    tableEntity.setGrowthAbility(latestRatingsTableByFinancial.getGrowthAbility() == null ? null : new GrowthAbilityDigestVo(latestRatingsTableByFinancial.getGrowthAbility().getOperatingRevenueGrowth(), latestRatingsTableByFinancial.getGrowthAbility().getNetProfitGrowth()));
                    tableEntity.setReportType(latestRatingsTableByFinancial.getReportType());
                    tableEntity.setKeyFigures(latestRatingsTableByFinancial.getKeyFigures() == null ? null : new KeyFiguresDigestVo(latestRatingsTableByFinancial.getKeyFigures().getOperatingRevenue(), latestRatingsTableByFinancial.getKeyFigures().getNetProfits()));
                    tableEntity.setOperatingCapacity(latestRatingsTableByFinancial.getOperatingCapacity() == null ? null : new OperatingCapacityDigestVo(latestRatingsTableByFinancial.getOperatingCapacity().getProvisionForImpairmentToCustomerLoansRatio(), latestRatingsTableByFinancial.getOperatingCapacity().getRecapitalizationRatio()));
                    tableEntity.setSolvency(latestRatingsTableByFinancial.getSolvency() == null ? null : new SolvencyDigestVo(latestRatingsTableByFinancial.getSolvency().getLoansTotalAssets(), latestRatingsTableByFinancial.getSolvency().getStockholderEquityTotalAssets()));
                    tableEntity.setPerShareIndicator(latestRatingsTableByFinancial.getPerShareIndicator() == null ? null : new PerShareIndicatorDigestVo(latestRatingsTableByFinancial.getPerShareIndicator().getEarningPerShare(), latestRatingsTableByFinancial.getPerShareIndicator().getNetAssetPerShare()));
                    tableEntity.setProfitability(latestRatingsTableByFinancial.getProfitability() == null ? null : new ProfitabilityDigestVo(latestRatingsTableByFinancial.getProfitability().getRoe(), latestRatingsTableByFinancial.getProfitability().getRoa()));
                    tableEntity.setTime(latestRatingsTableByFinancial.getReleaseTimestamp() == null ? null : latestRatingsTableByFinancial.getReleaseTimestamp());
                    break;
                case INSURANCE:
                    F10KeyFiguresInsuranceEntity latestRatingsTableByInsurance = getLatestRatingsTableByInsurance(code);
                    if (latestRatingsTableByInsurance == null) {
                        break;
                    }
                    tableEntity.setCashability(latestRatingsTableByInsurance.getCashability() == null ? null : new CashabilityDigestVo(latestRatingsTableByInsurance.getCashability().getCashRatio(), latestRatingsTableByInsurance.getCashability().getQuickRatio()));
                    tableEntity.setGrowthAbility(latestRatingsTableByInsurance.getGrowthAbility() == null ? null : new GrowthAbilityDigestVo(latestRatingsTableByInsurance.getGrowthAbility().getOperatingRevenueGrowth(), latestRatingsTableByInsurance.getGrowthAbility().getNetProfitGrowth()));
                    tableEntity.setReportType(latestRatingsTableByInsurance.getReportType());
                    tableEntity.setKeyFigures(latestRatingsTableByInsurance.getKeyFigures() == null ? null : new KeyFiguresDigestVo(latestRatingsTableByInsurance.getKeyFigures().getOperatingRevenue(), latestRatingsTableByInsurance.getKeyFigures().getNetProfits()));
                    tableEntity.setOperatingCapacity(latestRatingsTableByInsurance.getOperatingCapacity() == null ? null : new OperatingCapacityDigestVo(latestRatingsTableByInsurance.getOperatingCapacity().getGrossPremiumsPolicyFeeGrowth(), latestRatingsTableByInsurance.getOperatingCapacity().getInsuranceReserveGrowth()));
                    tableEntity.setSolvency(latestRatingsTableByInsurance.getSolvency() == null ? null : new SolvencyDigestVo(latestRatingsTableByInsurance.getSolvency().getTotalInvestmentAssets(), latestRatingsTableByInsurance.getSolvency().getStockholderEquityTotalAssets()));
                    tableEntity.setPerShareIndicator(latestRatingsTableByInsurance.getPerShareIndicator() == null ? null : new PerShareIndicatorDigestVo(latestRatingsTableByInsurance.getPerShareIndicator().getEarningPerShare(), latestRatingsTableByInsurance.getPerShareIndicator().getNetAssetPerShare()));
                    tableEntity.setProfitability(latestRatingsTableByInsurance.getProfitability() == null ? null : new ProfitabilityDigestVo(latestRatingsTableByInsurance.getProfitability().getRoe(), latestRatingsTableByInsurance.getProfitability().getRoa()));
                    tableEntity.setTime(latestRatingsTableByInsurance.getReleaseTimestamp() == null ? null : latestRatingsTableByInsurance.getReleaseTimestamp());
                    break;
                default:
                    break;
            }
        }

        return tableEntity;
    }

    /**
     * 获取最新主要指标数据(非金融)
     *
     * @param code
     * @return
     */
    private F10KeyFiguresNonFinancialEntity getLatestRatingsTableByNoFinancial(String code) {
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(1)
                .currentPage(0)
                .build();
        F10PageResp<F10KeyFiguresNonFinancialEntity> f10PageResp = f10KeyFiguresDao.pageNonFinancial(f10PageReq);
        List<F10KeyFiguresNonFinancialEntity> entityList = f10PageResp.getRecord();
        if (CollectionUtils.isNotEmpty(entityList)) {
            return entityList.get(0);
        }
        return new F10KeyFiguresNonFinancialEntity();
    }

    /**
     * 获取最新主要指标数据(金融)
     *
     * @param code
     * @return
     */
    private F10KeyFiguresFinancialEntity getLatestRatingsTableByFinancial(String code) {
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(1)
                .currentPage(0)
                .build();
        F10PageResp<F10KeyFiguresFinancialEntity> f10PageResp = f10KeyFiguresDao.pageFinancial(f10PageReq);
        List<F10KeyFiguresFinancialEntity> entityList = f10PageResp.getRecord();
        if (CollectionUtils.isNotEmpty(entityList)) {
            return entityList.get(0);
        }
        return new F10KeyFiguresFinancialEntity();
    }

    /**
     * 获取最新主要指标数据(保险)
     *
     * @param code
     * @return
     */
    private F10KeyFiguresInsuranceEntity getLatestRatingsTableByInsurance(String code) {
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(1)
                .currentPage(0)
                .build();
        F10PageResp<F10KeyFiguresInsuranceEntity> f10PageResp = f10KeyFiguresDao.pageInsurance(f10PageReq);
        List<F10KeyFiguresInsuranceEntity> entityList = f10PageResp.getRecord();
        if (CollectionUtils.isNotEmpty(entityList)) {
            return entityList.get(0);
        }
        return new F10KeyFiguresInsuranceEntity();
    }

    /**
     * 获取主要指标图表(金融)
     *
     * @param f10PageReq
     * @return
     */
    private List<RatingsTableEntity> getRatingsTableByFinancial(F10PageReq f10PageReq) {
        List<RatingsTableEntity> list = new ArrayList<>();
        F10PageResp<F10KeyFiguresFinancialEntity> f10PageResp = f10KeyFiguresDao.pageFinancial(f10PageReq);
        List<F10KeyFiguresFinancialEntity> entityList = f10PageResp.getRecord();
        List<String> dateList = f10ChartService.getTenYearDate(entityList, f10PageReq.getPageSize());
        if (CollectionUtils.isNotEmpty(entityList)) {
            List<String> currencyList = entityList.stream().map(F10KeyFiguresFinancialEntity::getCurrency).distinct().collect(Collectors.toList());
            dateList.forEach(d -> {
                RatingsTableEntity ratingsTableEntity = new RatingsTableEntity();
                entityList.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        ratingsTableEntity.setEarningPerShare(null == f.getPerShareIndicator().getEarningPerShare() ? null : f.getPerShareIndicator().getEarningPerShare());
                        ratingsTableEntity.setNetAssetPerShare(null == f.getPerShareIndicator().getNetAssetPerShare() ? null : f.getPerShareIndicator().getNetAssetPerShare());
                        ratingsTableEntity.setCashFlowPerShare(null == f.getPerShareIndicator().getCashFlowPerShare() ? null : f.getPerShareIndicator().getCashFlowPerShare());
                        ratingsTableEntity.setOperatingRevenue(null == f.getKeyFigures().getOperatingRevenue() ? null : f.getKeyFigures().getOperatingRevenue());
                        ratingsTableEntity.setRoa(null == f.getProfitability().getRoa() ? null : f.getProfitability().getRoa());
                        ratingsTableEntity.setRoe(null == f.getProfitability().getRoe() ? null : f.getProfitability().getRoe());
                        ratingsTableEntity.setNetProfits(null == f.getKeyFigures().getNetProfits() ? null : f.getKeyFigures().getNetProfits());
                        // ratingsTableEntity.setMonetaryUnit(null == f.getCurrency() ? null : f.getCurrency());
                        ratingsTableEntity.setNetProfitRatio(f.getProfitability() == null || f.getProfitability().getNetProfitRatio() == null ? null : f.getProfitability().getNetProfitRatio());
                    }
                });
                ratingsTableEntity.setTime(DateUtils.parseDate(d).getTime());
                ratingsTableEntity.setMonetaryUnit(CollUtil.isEmpty(currencyList) ? null : currencyList.get(0));
                list.add(ratingsTableEntity);
            });
        }
        return list;
    }

    /**
     * 获取主要指标保险
     *
     * @param f10PageReq
     * @return
     */
    private List<RatingsTableEntity> getRatingsTableByInsurance(F10PageReq f10PageReq) {
        List<RatingsTableEntity> list = new ArrayList<>();
        F10PageResp<F10KeyFiguresInsuranceEntity> f10PageResp = f10KeyFiguresDao.pageInsurance(f10PageReq);
        List<F10KeyFiguresInsuranceEntity> entityList = f10PageResp.getRecord();
        List<String> dateList = f10ChartService.getTenYearDate(entityList, f10PageReq.getPageSize());
        if (CollectionUtils.isNotEmpty(entityList)) {
            List<String> currencyList = entityList.stream().map(F10KeyFiguresInsuranceEntity::getCurrency).distinct().collect(Collectors.toList());
            dateList.forEach(d -> {
                RatingsTableEntity ratingsTableEntity = new RatingsTableEntity();
                entityList.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        ratingsTableEntity.setEarningPerShare(null == f.getPerShareIndicator().getEarningPerShare() ? null : f.getPerShareIndicator().getEarningPerShare());
                        ratingsTableEntity.setNetAssetPerShare(null == f.getPerShareIndicator().getNetAssetPerShare() ? null : f.getPerShareIndicator().getNetAssetPerShare());
                        ratingsTableEntity.setCashFlowPerShare(f.getPerShareIndicator().getCashFlowPerShare() == null || f.getPerShareIndicator() == null ? null : f.getPerShareIndicator().getCashFlowPerShare());
                        ratingsTableEntity.setOperatingRevenue(null == f.getKeyFigures().getOperatingRevenue() ? null : f.getKeyFigures().getOperatingRevenue());
                        ratingsTableEntity.setRoa(null == f.getProfitability().getRoa() ? null : f.getProfitability().getRoa());
                        ratingsTableEntity.setRoe(null == f.getProfitability().getRoe() ? null : f.getProfitability().getRoe());
                        ratingsTableEntity.setNetProfits(null == f.getKeyFigures().getNetProfits() ? null : f.getKeyFigures().getNetProfits());
                        // ratingsTableEntity.setMonetaryUnit(null == f.getCurrency() ? null : f.getCurrency());
                        ratingsTableEntity.setNetProfitRatio(f.getProfitability().getNetProfitRatio() == null ? null : f.getProfitability().getNetProfitRatio());
                    }
                });
                ratingsTableEntity.setTime(DateUtils.parseDate(d).getTime());
                ratingsTableEntity.setMonetaryUnit(CollUtil.isEmpty(currencyList) ? null : currencyList.get(0));
                list.add(ratingsTableEntity);
            });
        }
        return list;
    }

    /**
     * 获取报告类型
     *
     * @param code      股票代码
     * @param reportId  报告种类
     * @param tableType 表格类型
     * @return
     */
    public List<ReportTypeEntity> getReportType(String code, int reportId, int tableType) {
        int marketType = getMarketType(code);
        List<ReportTypeEntity> reportTypeList = new ArrayList<>();
        switch (tableType) {
            //主要指标
            case 1:
                reportTypeList = getReportByKeyIndicators(marketType, code);
                break;
            //利润表
            case 2:
                reportTypeList = getReportProfit(marketType, code);
                break;
            //负债
            case 3:
                reportTypeList = getReportDept(marketType, code);
                break;
            //现金流量
            case 4:
                reportTypeList = getReportCash(marketType, code);
                break;
            default:
                break;
        }

        // 过滤掉P和Q5类型
        List<ReportTypeEntity> reportTypeEntities = CollUtil.filter(reportTypeList, rt -> !ReportTypeEnum.unResolveTypeList().contains(rt.getReportType()));
        return getReportTypeByReportId(reportTypeEntities, reportId);
    }

    public String getActualReportType(String type, Number number) {
        BigDecimal quarter = BigDecimal.valueOf(number.doubleValue());
        if (StrUtil.equals(ComReportTypeEnum.Q3.getCode(), type)) {
            if (NumberUtil.equals(BigDecimal.valueOf(3), quarter)) {
                // Q3类型
                return ComReportTypeEnum.Q3.getCode();
            } else if (NumberUtil.equals(BigDecimal.valueOf(9), quarter)) {
                // Q9类型
                return ComReportTypeEnum.Q9.getCode();
            }
        }
        return type;
    }

    /**
     * 获取财务报表
     *
     * @param code
     * @return
     */
    public F10CommonFinTable getFinancialTable(String code) {
        int marketType = getMarketType(code);
        F10CommonFinTable f10FinancialTable = new F10CommonFinTable();
        F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);
        if (null != f10MarketTypeEnum) {
            switch (f10MarketTypeEnum) {
                case NO_FINANCIAL:
                    f10FinancialTable = getFinancialTableByNoFinancial(code);
                    break;
                case FINANCIAL:
                    f10FinancialTable = getFinancialTableByFinancial(code);
                    break;
                case INSURANCE:
                    f10FinancialTable = getFinancialTableByInsurance(code);
                    break;
                default:
                    break;
            }
        }
        f10FinancialTable.setMarketType(f10MarketTypeEnum);
        return f10FinancialTable;
    }

    /**
     * 获取财务报表(非金融)
     *
     * @param code
     * @return
     */
    private F10CommonFinTable getFinancialTableByNoFinancial(String code) {
        F10CommonFinTable table = new F10CommonFinTable();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(1)
                .currentPage(0)
                .build();
        F10PageResp profit = f10ProfitDao.pageNonFinancial(f10PageReq);
        F10PageResp assets = f10AssetsLiabilitiesDao.pageNonFinancial(f10PageReq);
        F10PageResp cash = f10CashFlowDao.pageCashFlow(f10PageReq);
        if (CollectionUtils.isNotEmpty(profit.getRecord())) {
            F10NoFinProfitEntity profitEntity = (F10NoFinProfitEntity) profit.getRecord().get(0);
            F10ProfitNoFinTable profitTable = new F10ProfitNoFinTable();
            profitTable.setTime(profitEntity.getEndTimestamp());
            profitTable.setReportType(profitEntity.getReportType());
            profitTable.setOperatingAndOtherRevenue(profitEntity.getOperatingAndOtherRevenue().getVal());
            profitTable.setOperatingCostsAndExpenses(profitEntity.getOperatingCostsAndExpenses().getVal());
            profitTable.setOperatingProfit(profitEntity.getOperatingProfit().getVal());
            profitTable.setProfitBeforeTaxation(profitEntity.getProfitBeforeTaxation().getVal());
            profitTable.setProfitAndLossDuringThePeriod(profitEntity.getProfitAndLossDuringThePeriod().getVal());
            profitTable.setDilutedEarningsPerShare(profitEntity.getDilutedEarningsPerShare().getVal());
            profitTable.setBasicEarningsPerShare(profitEntity.getBasicEarningsPerShare().getVal());
            table.setProfit(profitTable);
        }

        if (CollectionUtils.isNotEmpty(assets.getRecord())) {
            F10AssetsLiabilitiesNonFinancialEntity assetsEntity = (F10AssetsLiabilitiesNonFinancialEntity) assets.getRecord().get(0);
            F10BalanceSheetNoFinTable balanceSheetTable = new F10BalanceSheetNoFinTable();
            balanceSheetTable.setTime(assetsEntity.getEndTimestamp());
            balanceSheetTable.setReportType(assetsEntity.getReportType());
            balanceSheetTable.setTotalAssets(assetsEntity.getTotalAssets().getVal());
            balanceSheetTable.setCurrentAsset(assetsEntity.getCurrentAsset().getVal());
            balanceSheetTable.setNonCurrentAssets(assetsEntity.getNonCurrentAssets().getVal());
            balanceSheetTable.setTotalLiabilities(assetsEntity.getTotalLiabilities().getVal());
            balanceSheetTable.setCurrentLiabilities(assetsEntity.getCurrentLiabilities().getVal());
            balanceSheetTable.setNonCurrentLiability(assetsEntity.getNonCurrentLiability().getVal());
            balanceSheetTable.setNetAssetValue(assetsEntity.getNetAssetValue().getVal());
            balanceSheetTable.setTotalEquityAndNonCurrentLiabilities(assetsEntity.getTotalEquityAndNonCurrentLiabilities().getVal());
            balanceSheetTable.setTotalEquity(assetsEntity.getTotalEquity().getVal());
            balanceSheetTable.setStockholdersEquity(assetsEntity.getStockholdersEquity().getVal());
            table.setBalanceSheet(balanceSheetTable);
        }

        if (CollectionUtils.isNotEmpty(cash.getRecord())) {
            F10CashFlowEntity f10CashFlowEntity = (F10CashFlowEntity) cash.getRecord().get(0);
            builderCashTable(f10CashFlowEntity, table);
        }
        return table;
    }

    /**
     * 构建现金流量表
     *
     * @param f10CashFlowEntity
     * @param table
     */
    private void builderCashTable(
            F10CashFlowEntity f10CashFlowEntity,
            F10CommonFinTable table
    ) {
        F10CashFlowTable flowTable = new F10CashFlowTable();
        flowTable.setReportType(f10CashFlowEntity.getReportType());
        flowTable.setTime(f10CashFlowEntity.getEndTimestamp());
        flowTable.setCashFlowFromOperations(f10CashFlowEntity.getCashFlowFromeOperations().getVal() == null ? null
                : f10CashFlowEntity.getCashFlowFromeOperations().getVal());
        flowTable.setCashFlowFromInvestmentActivities(f10CashFlowEntity.getCashFlowFromInvestmentActivities().getVal() == null ? null
                : f10CashFlowEntity.getCashFlowFromInvestmentActivities().getVal());
        flowTable.setCashFlowFromFinancingActivities(f10CashFlowEntity.getCashFlowFromFinancingActivites().getVal() == null ? null
                : f10CashFlowEntity.getCashFlowFromFinancingActivites().getVal());
        flowTable.setExchangeRateInfluence(f10CashFlowEntity.getExchangeRateInfluence().getVal() == null ? null
                : f10CashFlowEntity.getExchangeRateInfluence().getVal());
        flowTable.setNetCash(f10CashFlowEntity.getNetCash().getVal() == null ? null
                : f10CashFlowEntity.getNetCash().getVal());
        flowTable.setInitialCash(f10CashFlowEntity.getInitialCash().getVal() == null ? null
                : f10CashFlowEntity.getInitialCash().getVal());
        flowTable.setEndCash(f10CashFlowEntity.getFinalCash().getVal() == null ? null
                : f10CashFlowEntity.getFinalCash().getVal());
        table.setCurrency(f10CashFlowEntity.getCurrency());
        table.setCashFlow(flowTable);
    }

    /**
     * 获取财务报表(金融)
     *
     * @param code
     * @return
     */
    private F10CommonFinTable getFinancialTableByFinancial(String code) {
        F10CommonFinTable table = new F10CommonFinTable();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(1)
                .currentPage(0)
                .build();
        F10PageResp profit = f10ProfitDao.pageFinancial(f10PageReq);
        F10PageResp assets = f10AssetsLiabilitiesDao.pageFinancial(f10PageReq);
        F10PageResp cash = f10CashFlowDao.pageCashFlow(f10PageReq);
        if (CollectionUtils.isNotEmpty(profit.getRecord())) {
            F10FinProfitEntity profitEntity = (F10FinProfitEntity) profit.getRecord().get(0);
            F10ProfitFinTable profitTable = new F10ProfitFinTable();
            profitTable.setReportType(profitEntity.getReportType());
            profitTable.setTime(profitEntity.getEndTimestamp());
            profitTable.setGrossRevenue(profitEntity.getGrossRevenue().getVal() == null ? null
                    : profitEntity.getGrossRevenue().getVal());
            profitTable.setTotalOperatingExpenses(profitEntity.getTotalOperatingExpenses().getVal() == null ? null
                    : profitEntity.getTotalOperatingExpenses().getVal());
            profitTable.setOperatingProfit(profitEntity.getOperatingProfit().getVal() == null ? null
                    : profitEntity.getOperatingProfit().getVal());
            profitTable.setProfitBeforeTaxation(profitEntity.getProfitBeforeTaxation().getVal() == null ? null
                    : profitEntity.getProfitBeforeTaxation().getVal());
            profitTable.setProfitAndLossDuringThePeriod(profitEntity.getProfitLossDuringPeriod().getVal() == null ? null
                    : profitEntity.getProfitLossDuringPeriod().getVal());
            profitTable.setDilutedEarningsPerShare(profitEntity.getDilutedEarningsPerShare().getVal() == null ? null
                    : profitEntity.getDilutedEarningsPerShare().getVal());
            profitTable.setBasicEarningsPerShare(profitEntity.getBasicEarningsPerShare() == null ? null
                    : profitEntity.getBasicEarningsPerShare().getVal());
            table.setProfit(profitTable);
        }

        if (CollectionUtils.isNotEmpty(assets.getRecord())) {
            F10AssetsLiabilitiesFinancialEntity assetsEntity = (F10AssetsLiabilitiesFinancialEntity) assets.getRecord().get(0);
            F10BalanceSheetFinTable balanceSheetTable = new F10BalanceSheetFinTable();
            balanceSheetTable.setReportType(assetsEntity.getReportType());
            balanceSheetTable.setTime(assetsEntity.getEndTimestamp());
            balanceSheetTable.setNetAssetValue(assetsEntity.getNetAssetValue().getVal() == null ? null :
                    assetsEntity.getNetAssetValue().getVal());
            balanceSheetTable.setTotalAssets(assetsEntity.getTotalAssets().getVal() == null ? null
                    : assetsEntity.getTotalAssets().getVal());
            balanceSheetTable.setTotalLiabilities(assetsEntity.getTotalLiabilities().getVal() == null ? null
                    : assetsEntity.getTotalLiabilities().getVal());
            balanceSheetTable.setTotalEquity(assetsEntity.getTotalEquity().getVal() == null ? null
                    : assetsEntity.getTotalEquity().getVal());
            balanceSheetTable.setStockholdersEquity(assetsEntity.getStockholdersEquity().getVal() == null ? null
                    : assetsEntity.getStockholdersEquity().getVal());
            table.setBalanceSheet(balanceSheetTable);
        }

        if (CollectionUtils.isNotEmpty(cash.getRecord())) {
            F10CashFlowEntity f10CashFlowEntity = (F10CashFlowEntity) cash.getRecord().get(0);
            builderCashTable(f10CashFlowEntity, table);
        }
        return table;
    }

    /**
     * 获取财务报表(保险)
     *
     * @param code
     * @return
     */
    private F10CommonFinTable getFinancialTableByInsurance(String code) {
        F10CommonFinTable table = new F10CommonFinTable();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(1)
                .currentPage(0)
                .build();
        F10PageResp profit = f10ProfitDao.pageInsurance(f10PageReq);
        F10PageResp assets = f10AssetsLiabilitiesDao.pageInsurance(f10PageReq);
        F10PageResp cash = f10CashFlowDao.pageCashFlow(f10PageReq);
        if (CollectionUtils.isNotEmpty(profit.getRecord())) {
            F10InsureProfitEntity profitEntity = (F10InsureProfitEntity) profit.getRecord().get(0);
            F10ProfitInsuranceTable profitTable = new F10ProfitInsuranceTable();
            profitTable.setReportType(profitEntity.getReportType());
            profitTable.setTime(profitEntity.getEndTimestamp());
            profitTable.setGrossRevenue(profitEntity.getGrossRevenue().getVal() == null ? null
                    : profitEntity.getGrossRevenue().getVal());
            profitTable.setInsuranceExpensesTotalExpenses(profitEntity.getInsuranceExpensesTotalExpenses().getVal() == null ? null
                    : profitEntity.getInsuranceExpensesTotalExpenses().getVal());
            profitTable.setOperatingProfit(profitEntity.getOperatingProfit().getVal() == null ? null
                    : profitEntity.getOperatingProfit().getVal());
            profitTable.setProfitBeforeTaxation(profitEntity.getProfitBeforeTaxation().getVal() == null ? null
                    : profitEntity.getProfitBeforeTaxation().getVal());
            profitTable.setProfitAndLossDuringThePeriod(profitEntity.getProfitLossDuringPeriod().getVal() == null ? null
                    : profitEntity.getProfitLossDuringPeriod().getVal());
            profitTable.setDilutedEarningsPerShare(profitEntity.getDilutedEarningsPerShare().getVal() == null ? null
                    : profitEntity.getDilutedEarningsPerShare().getVal());
            profitTable.setBasicEarningsPerShare(profitEntity.getBasicEarningsPerShare().getVal() == null ? null
                    : profitEntity.getBasicEarningsPerShare().getVal());
            table.setProfit(profitTable);
        }

        if (CollectionUtils.isNotEmpty(assets.getRecord())) {
            F10AssetsLiabilitiesInsuranceEntity assetsEntity = (F10AssetsLiabilitiesInsuranceEntity) assets.getRecord().get(0);
            F10BalanceSheetInsuranceTable balanceSheetTable = new F10BalanceSheetInsuranceTable();
            balanceSheetTable.setReportType(assetsEntity.getReportType());
            balanceSheetTable.setTime(assetsEntity.getEndTimestamp());
            balanceSheetTable.setTotalAssets(assetsEntity.getTotalAssets().getVal() == null ? null
                    : assetsEntity.getTotalAssets().getVal());
            balanceSheetTable.setTotalLiabilities(assetsEntity.getTotalLiabilities().getVal() == null ? null
                    : assetsEntity.getTotalLiabilities().getVal());
            balanceSheetTable.setNetAssetValue(assetsEntity.getNetAssetValue().getVal() == null ? null
                    : assetsEntity.getNetAssetValue().getVal());
            balanceSheetTable.setTotalEquity(assetsEntity.getTotalEquity().getVal() == null ? null
                    : assetsEntity.getTotalEquity().getVal());
            balanceSheetTable.setStockholdersEquity(assetsEntity.getStockholdersEquity().getVal() == null ? null
                    : assetsEntity.getStockholdersEquity().getVal());
            table.setBalanceSheet(balanceSheetTable);
        }

        if (CollectionUtils.isNotEmpty(cash.getRecord())) {
            F10CashFlowEntity f10CashFlowEntity = (F10CashFlowEntity) cash.getRecord().get(0);
            builderCashTable(f10CashFlowEntity, table);
        }
        return table;
    }

    /**
     * 根据报告种类获取不同的报告类型
     *
     * @param reportTypeList 所有报告类型
     * @param reportId       报告种类
     * @return
     */
    private List<ReportTypeEntity> getReportTypeByReportId(
            List<ReportTypeEntity> reportTypeList,
            int reportId
    ) {
        if (reportId == 1) {
            return reportTypeList.stream().filter(s -> "F".equals(s.getReportType())).collect(Collectors.toList());
        }
        if (reportId == 2) {
            return reportTypeList.stream().filter(s -> ("I").equals(s.getReportType())).collect(Collectors.toList());
        }
        if (reportId == 3) {
            return reportTypeList.stream().filter(s -> s.getReportType().contains("Q")).collect(Collectors.toList());
        }
        return reportTypeList;
    }

    /**
     * 获取主要指标的所有报告类型
     *
     * @param marketType
     * @param code
     * @return
     */
    private List<ReportTypeEntity> getReportByKeyIndicators(
            int marketType,
            String code
    ) {
        List<ReportTypeEntity> reportTypeEntityList = new ArrayList<>();
        if (marketType == F10MarketTypeEnum.NO_FINANCIAL.getCode()) {
            List<Xnhk0203> list = xnhk0203Mapper.selectList(
                    new QueryWrapper<Xnhk0203>()
                            .eq("seccode", code)
                            .orderByDesc("f002d")
            );
            list.forEach(s -> {
                ReportTypeEntity reportTypeEntity = ReportTypeEntity.builder()
                        .reportType(getActualReportType(s.getF006v(), s.getF007d()))
                        .time(DateUtils.parseDate(s.getF002d()).getTime())
                        .build();
                reportTypeEntityList.add(reportTypeEntity);
            });
        }
        if (marketType == F10MarketTypeEnum.FINANCIAL.getCode()) {
            List<Xnhk0206> list = xnhk0206Mapper.selectList(
                    new QueryWrapper<Xnhk0206>()
                            .eq("seccode", code)
                            .orderByDesc("f002d")
            );
            list.forEach(s -> {
                ReportTypeEntity reportTypeEntity = ReportTypeEntity.builder()
                        .reportType(getActualReportType(s.getF006v(), s.getF007n()))
                        .time(DateUtils.parseDate(s.getF002d()).getTime())
                        .build();
                reportTypeEntityList.add(reportTypeEntity);
            });
        }
        if (marketType == F10MarketTypeEnum.INSURANCE.getCode()) {
            List<Xnhk0207> list = xnhk0207Mapper.selectList(
                    new QueryWrapper<Xnhk0207>()
                            .eq("seccode", code)
                            .orderByDesc("f002d")
            );
            list.forEach(s -> {
                ReportTypeEntity reportTypeEntity = ReportTypeEntity.builder()
                        .reportType(getActualReportType(s.getF006v(),s.getF007n()))
                        .time(DateUtils.parseDate(s.getF002d()).getTime())
                        .build();
                reportTypeEntityList.add(reportTypeEntity);
            });
        }

        return reportTypeEntityList;
    }

    /**
     * 获取利润表的报告类型
     *
     * @param marketType
     * @param code
     * @return
     */
    private List<ReportTypeEntity> getReportProfit(
            int marketType,
            String code
    ) {
        List<ReportTypeEntity> reportTypeEntityList = new ArrayList<>();
        if (marketType == F10MarketTypeEnum.NO_FINANCIAL.getCode()) {
            List<Xnhk0201> list = xnhk0201Mapper.selectList(
                    new QueryWrapper<Xnhk0201>()
                            .eq("seccode", code)
                            .orderByDesc("f002d")
            );
            list.forEach(s -> {
                ReportTypeEntity reportTypeEntity = ReportTypeEntity.builder()
                        .reportType(getActualReportType(s.getF006v(),s.getF007d()))
                        .time(DateUtils.parseDate(s.getF002d()).getTime())
                        .build();
                reportTypeEntityList.add(reportTypeEntity);
            });
        }
        if (marketType == F10MarketTypeEnum.FINANCIAL.getCode()) {
            List<Xnhk0204> list = xnhk0204Mapper.selectList(
                    new QueryWrapper<Xnhk0204>()
                            .eq("seccode", code)
                            .orderByDesc("f002d")
            );
            list.forEach(s -> {
                ReportTypeEntity reportTypeEntity = ReportTypeEntity.builder()
                        .reportType(getActualReportType(s.getF006v(),s.getF007d()))
                        .time(DateUtils.parseDate(s.getF002d()).getTime())
                        .build();
                reportTypeEntityList.add(reportTypeEntity);
            });
        }
        if (marketType == F10MarketTypeEnum.INSURANCE.getCode()) {
            List<Xnhk0207> list = xnhk0207Mapper.selectList(
                    new QueryWrapper<Xnhk0207>()
                            .eq("seccode", code)
                            .orderByDesc("f002d")
            );
            list.forEach(s -> {
                ReportTypeEntity reportTypeEntity = ReportTypeEntity.builder()
                        .reportType(getActualReportType(s.getF006v(),s.getF007n()))
                        .time(DateUtils.parseDate(s.getF002d()).getTime())
                        .build();
                reportTypeEntityList.add(reportTypeEntity);
            });
        }
        return reportTypeEntityList;
    }

    /**
     * 获取负债表的报告类型
     *
     * @param marketType
     * @param code
     * @return
     */
    private List<ReportTypeEntity> getReportDept(
            int marketType,
            String code
    ) {
        List<ReportTypeEntity> reportTypeEntityList = new ArrayList<>();
        if (marketType == F10MarketTypeEnum.NO_FINANCIAL.getCode()) {
            List<Xnhk0202> list = xnhk0202Mapper.selectList(
                    new QueryWrapper<Xnhk0202>()
                            .eq("seccode", code)
                            .orderByDesc("f002d")
            );
            list.forEach(s -> {
                ReportTypeEntity reportTypeEntity = ReportTypeEntity.builder()
                        .reportType(getActualReportType(s.getF006v(),s.getF007d()))
                        .time(DateUtils.parseDate(s.getF002d()).getTime())
                        .build();
                reportTypeEntityList.add(reportTypeEntity);
            });
        }
        if (marketType == F10MarketTypeEnum.FINANCIAL.getCode()) {
            List<Xnhk0205> list = xnhk0205Mapper.selectList(
                    new QueryWrapper<Xnhk0205>()
                            .eq("seccode", code)
                            .orderByDesc("f002d")
            );
            list.forEach(s -> {
                ReportTypeEntity reportTypeEntity = ReportTypeEntity.builder()
                        .reportType(getActualReportType(s.getF006v(),s.getF007d()))
                        .time(DateUtils.parseDate(s.getF002d()).getTime())
                        .build();
                reportTypeEntityList.add(reportTypeEntity);
            });
        }
        if (marketType == F10MarketTypeEnum.INSURANCE.getCode()) {
            List<Xnhk0208> list = xnhk0208Mapper.selectList(
                    new QueryWrapper<Xnhk0208>()
                            .eq("seccode", code)
                            .orderByDesc("f002d")
            );
            list.forEach(s -> {
                ReportTypeEntity reportTypeEntity = ReportTypeEntity.builder()
                        .reportType(getActualReportType(s.getF006v(),s.getF007n()))
                        .time(DateUtils.parseDate(s.getF002d()).getTime())
                        .build();
                reportTypeEntityList.add(reportTypeEntity);
            });
        }

        return reportTypeEntityList;
    }

    /**
     * 获取现金流量表的报告类型
     *
     * @param marketType
     * @param code
     * @return
     */
    private List<ReportTypeEntity> getReportCash(
            int marketType,
            String code
    ) {
        List<ReportTypeEntity> reportTypeEntityList = new ArrayList<>();
        List<Xnhk0210> list = xnhk0210Mapper.selectList(
                new QueryWrapper<Xnhk0210>()
                        .eq("seccode", code)
                        .orderByDesc("f002d")
        );
        list.forEach(s -> {
            ReportTypeEntity reportTypeEntity = ReportTypeEntity.builder()
                    .reportType(getActualReportType(s.getF006v(),s.getF007n()))
                    .time(DateUtils.parseDate(s.getF002d()).getTime())
                    .build();
            reportTypeEntityList.add(reportTypeEntity);
        });
        return reportTypeEntityList;
    }

    /**
     * 获取市场类型 0 =非金融 1=金融 2=保险
     *
     * @param code
     * @return
     */
    public int getMarketType(String code) {
        int marketType = -1;
        // List<Object> marketTypes = xnhks0101Mapper.selectObjs(new QueryWrapper<Xnhks0101>()
        //         .select("f026v")
        //         .eq("seccode", code)
        //         .groupBy("f026v")
        // );
        // if (CollectionUtils.isNotEmpty(marketTypes) && marketTypes.get(0) != null) {
        //     marketType = Integer.parseInt(marketTypes.get(0).toString());
        // }
        Integer value = redisClient.hget(RedisKeyConstants.STOCK_FINANCE_TYPE, code);
        if (ObjectUtil.isNotEmpty(value)) {
            marketType = value;
        }
        return marketType;
    }

    @Resource
    private StockDefineMapper stockDefineMapper;

    /**
     * 获取n年的数据
     *
     * @param years
     * @return
     */
    private List<String> getDateList(Integer years) {
        List<String> stringList = new LinkedList<>();
        for (int i = 1; i <= years; i++) {
            stringList.add(DateUtils.formatDate(DateUtils.getDate(new Date(), -i, Calendar.YEAR), TIME_PARSE));
        }
        return stringList;
    }

    public SubBusinessInfo subBusinessInfo(String code) {
        // 查询所属行业
        SubBusinessInfo subBusinessInfo = stockDefineMapper.queryStockWithIndustry(code);
        if (ObjectUtils.isEmpty(subBusinessInfo) || StringUtils.isBlank(subBusinessInfo.getBusCode())) {
            return null;
        }
        // 查询行业涨跌幅
        StockSnapshot snapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(subBusinessInfo.getBusCode()));
        if (ObjectUtils.isEmpty(snapshot)) {
            return subBusinessInfo;
        }
        subBusinessInfo.setIncrease(snapshot.getChgPct());
        return subBusinessInfo;
    }

    /**
     * 获取最新一条数据前n年年份
     * @param f10EntityBases
     * @param years
     * @param <T>
     * @return
     */
    private <T extends F10EntityBase> List<String> getTenYearDate(List<T> f10EntityBases,Integer years) {
        List<String> stringList = new LinkedList<>();
        if(CollectionUtils.isEmpty(f10EntityBases)){
            for (int i = 1; i <= years; i++) {
                stringList.add(DateUtils.formatDate(DateUtils.getDate(new Date(), -i, Calendar.YEAR), TIME_PARSE));
            }
            return stringList;
        }
        F10EntityBase last = f10EntityBases.stream().sorted(Comparator.comparing(F10EntityBase::getEndTimestamp).reversed()).findFirst().get();
        Date lastDate = new Date(last.getEndTimestamp());
        for (int i = 0; i < years; i++) {
            stringList.add(DateUtils.formatDate(DateUtils.getDate(lastDate, -i, Calendar.YEAR), TIME_PARSE));
        }
        return stringList;
    }

    public List<DuPontAnalysisEntity> getDuPontAnalyze(String stockCode) {
        //查询数据库表获取 市场类型
        int marketType = getMarketType(stockCode);
        F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);
        if (null != f10MarketTypeEnum) {
            // List<DuPontAnalysisEntity> lists = f10SourceHandlerV2.buildDuPontResult(f10MarketTypeEnum);
            List<DuPontAnalysisEntity> lists = f10DuPontHandler.buildDuPontResult(stockCode, f10MarketTypeEnum);
            return lists;
        }
        return Lists.newArrayList();
    }

    public void createF10DataByCode(String stockCode) {
        try {
            log.info("F10TableTemplateApi createF10DataByCode start, stockCode: {}", stockCode);
            TimeInterval timeInterval = new TimeInterval();
            //获取市场类型 0 =非金融 1=金融 2=保险
            String oldCode = StrUtil.replace(stockCode, "-t", "");
            int marketType = getMarketType(oldCode);

            F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);
            if (null != f10MarketTypeEnum) {
                // 股票金融类型
                redisClient.hset(RedisKeyConstants.STOCK_FINANCE_TYPE, stockCode, marketType);
                // 查询base股票最后4条
                Query query = Query.query(Criteria.where("stockCode").is(oldCode));
                List<F10CashFlowEntity> cashFlowList = mongoTemplate.find(query, F10CashFlowEntity.class);
                switch (f10MarketTypeEnum) {
                    case NO_FINANCIAL:
                        getAndSaveF10Data(stockCode, query, F10KeyFiguresNonFinancialEntity.class);
                        getAndSaveF10Data(stockCode, query, F10NoFinProfitEntity.class);
                        getAndSaveF10Data(stockCode, query, F10AssetsLiabilitiesNonFinancialEntity.class);
                        getAndSaveF10Data(stockCode, query, F10CashFlowEntity.class);
                        break;
                    case FINANCIAL:
                        getAndSaveF10Data(stockCode, query, F10KeyFiguresFinancialEntity.class);
                        getAndSaveF10Data(stockCode, query, F10FinProfitEntity.class);
                        getAndSaveF10Data(stockCode, query, F10AssetsLiabilitiesFinancialEntity.class);
                        getAndSaveF10Data(stockCode, query, F10CashFlowEntity.class);
                        break;
                    case INSURANCE:
                        getAndSaveF10Data(stockCode, query, F10KeyFiguresInsuranceEntity.class);
                        getAndSaveF10Data(stockCode, query, F10InsureProfitEntity.class);
                        getAndSaveF10Data(stockCode, query, F10AssetsLiabilitiesInsuranceEntity.class);
                        getAndSaveF10Data(stockCode, query, F10CashFlowEntity.class);
                        break;
                    default:
                        break;
                }
            }

            // 持股占比变化
            // t_stock_holder_change
            List<StockHolderChange> holderChanges = stockHolderChangeService.list(new QueryWrapper<StockHolderChange>().eq("code", oldCode));

            CollUtil.forEach(holderChanges, (ff, index) -> { ff.setId(null); ff.setCode(stockCode); });
            Opt.ofEmptyAble(holderChanges).peek(list -> stockHolderChangeService.saveBatch(list));
            log.info("F10TableTemplateApi createF10DataByCode end, stockCode: {}, cost: {}", stockCode, timeInterval.interval() / 1000.0);
        } catch (Exception e) {
            log.error("F10TableTemplateApi createF10DataByCode error, stockCode: {}", stockCode, e);
        }
    }

    private <T extends F10EntityBase> void getAndSaveF10Data(String stockCode, Query query, Class<T> clazz) {
        log.info("F10TableTemplateApi getAndSaveF10Data start, stockCode: {}, clazz: {}", stockCode, clazz.getSimpleName());
        TimeInterval timeInterval = new TimeInterval();
        List<T> dataList = mongoTemplate.find(query, clazz);

        if (CollUtil.isEmpty(dataList)) {
            log.warn("F10TableTemplateApi getAndSaveF10Data dataList is empty!");
            return;
        }

        List<T> addList = dataList.stream().peek(da -> da.setStockCode(stockCode)).collect(Collectors.toList());
        mongoTemplate.insert(addList, clazz);
        log.info("F10TableTemplateApi getAndSaveF10Data end, stockCode: {}, clazz: {}, cost: {}", stockCode, clazz.getSimpleName(), timeInterval.interval() / 1000.0);
    }

    public void deleteF10DataByCode(String stockCode) {
        try {
            log.info("F10TableTemplateApi deleteF10DataByCode start, stockCode: {}", stockCode);
            TimeInterval timeInterval = new TimeInterval();
            //获取市场类型 0 =非金融 1=金融 2=保险
            int marketType = getMarketType(stockCode);

            F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);
            if (null != f10MarketTypeEnum) {
                redisClient.hdel(RedisKeyConstants.STOCK_FINANCE_TYPE, stockCode);
                Query query = Query.query(Criteria.where("stockCode").is(stockCode));
                switch (f10MarketTypeEnum) {
                    case NO_FINANCIAL:
                        mongoTemplate.remove(query, F10KeyFiguresNonFinancialEntity.class);
                        mongoTemplate.remove(query, F10NoFinProfitEntity.class);
                        mongoTemplate.remove(query, F10AssetsLiabilitiesNonFinancialEntity.class);
                        mongoTemplate.remove(query, F10CashFlowEntity.class);
                        break;
                    case FINANCIAL:
                        mongoTemplate.remove(query, F10KeyFiguresFinancialEntity.class);
                        mongoTemplate.remove(query, F10FinProfitEntity.class);
                        mongoTemplate.remove(query, F10AssetsLiabilitiesFinancialEntity.class);
                        mongoTemplate.remove(query, F10CashFlowEntity.class);
                        break;
                    case INSURANCE:
                        mongoTemplate.remove(query, F10KeyFiguresInsuranceEntity.class);
                        mongoTemplate.remove(query, F10InsureProfitEntity.class);
                        mongoTemplate.remove(query, F10AssetsLiabilitiesInsuranceEntity.class);
                        mongoTemplate.remove(query, F10CashFlowEntity.class);
                        break;
                    default:
                        break;
                }
            }

            // 持股占比变化
            // t_stock_holder_change
            stockHolderChangeService.remove(new QueryWrapper<StockHolderChange>().eq("code", stockCode));
            log.info("F10TableTemplateApi deleteF10DataByCode end, stockCode: {}, cost: {}", stockCode, timeInterval.interval() / 1000.0);
        } catch (Exception e) {
            log.error("F10TableTemplateApi deleteF10DataByCode error, stockCode: {}", stockCode, e);
        }
    }

    public void updateF10DataByCode(String oldStockCode, String newStockCode) {
        try {
            log.info("F10TableTemplateApi updateF10DataByCode start, oldStockCode: {}, newStockCode: {}", oldStockCode, newStockCode);
            TimeInterval timeInterval = new TimeInterval();
            //获取市场类型 0 =非金融 1=金融 2=保险
            int marketType = getMarketType(oldStockCode);

            F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(marketType);
            if (null != f10MarketTypeEnum) {
                // 股票金融类型
                redisClient.hdel(RedisKeyConstants.STOCK_FINANCE_TYPE, oldStockCode);
                redisClient.hset(RedisKeyConstants.STOCK_FINANCE_TYPE, newStockCode, marketType);
                //更新条件
                Query query = Query.query(Criteria.where("stockCode").is(oldStockCode));
                //更新值
                Update update = new Update().set("stockCode", newStockCode);
                switch (f10MarketTypeEnum) {
                    case NO_FINANCIAL:
                        mongoTemplate.updateMulti(query, update, F10KeyFiguresNonFinancialEntity.class);
                        mongoTemplate.updateMulti(query, update, F10NoFinProfitEntity.class);
                        mongoTemplate.updateMulti(query, update, F10AssetsLiabilitiesNonFinancialEntity.class);
                        mongoTemplate.updateMulti(query, update, F10CashFlowEntity.class);
                        break;
                    case FINANCIAL:
                        mongoTemplate.updateMulti(query, update, F10KeyFiguresFinancialEntity.class);
                        mongoTemplate.updateMulti(query, update, F10FinProfitEntity.class);
                        mongoTemplate.updateMulti(query, update, F10AssetsLiabilitiesFinancialEntity.class);
                        mongoTemplate.updateMulti(query, update, F10CashFlowEntity.class);
                        break;
                    case INSURANCE:
                        mongoTemplate.updateMulti(query, update, F10KeyFiguresInsuranceEntity.class);
                        mongoTemplate.updateMulti(query, update, F10InsureProfitEntity.class);
                        mongoTemplate.updateMulti(query, update, F10AssetsLiabilitiesInsuranceEntity.class);
                        mongoTemplate.updateMulti(query, update, F10CashFlowEntity.class);
                        break;
                    default:
                        break;
                }
            }

            stockHolderChangeService.update(Wrappers.<StockHolderChange>lambdaUpdate().set(StockHolderChange::getCode, newStockCode).eq(StockHolderChange::getCode, oldStockCode));
            log.info("F10TableTemplateApi updateF10DataByCode end, oldStockCode: {}, newStockCode: {}, cost: {}", oldStockCode, newStockCode, timeInterval.interval() / 1000.0);
        } catch (NumberFormatException e) {
            log.error("F10TableTemplateApi updateF10DataByCode error, oldStockCode: {}, newStockCode: {}", oldStockCode, newStockCode, e);
        }
    }
}
