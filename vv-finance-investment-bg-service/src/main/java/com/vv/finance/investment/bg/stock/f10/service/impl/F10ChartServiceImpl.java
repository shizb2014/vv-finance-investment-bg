package com.vv.finance.investment.bg.stock.f10.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10PageResp;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.f10.chart.*;
import com.vv.finance.investment.bg.entity.f10.enums.F10MarketTypeEnum;
import com.vv.finance.investment.bg.entity.f10.f10Profit.F10ProfitEntity;
import com.vv.finance.investment.bg.entity.f10.fintable.AssetsLiabilitiesChartEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhks0101;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0101Mapper;
import com.vv.finance.investment.bg.mongo.dao.F10AssetsLiabilitiesDao;
import com.vv.finance.investment.bg.mongo.dao.F10KeyFiguresDao;
import com.vv.finance.investment.bg.mongo.dao.F10ProfitDao;
import com.vv.finance.investment.bg.mongo.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/7/24 12:00
 * @Version 1.0
 */
@Service
@Slf4j
public class F10ChartServiceImpl {

    private static final String TIME_PARSE = "yyyy";

    @Resource
    Xnhks0101Mapper xnhks0101Mapper;

    @Resource
    F10ProfitDao f10ProfitDao;
    @Resource
    F10AssetsLiabilitiesDao f10AssetsLiabilitiesDao;
    @Resource
    F10KeyFiguresDao f10KeyFiguresDao;
    @Resource
    F10SourceServiceImpl f10SourceService;

    /**
     * 获取利润表图表
     *
     * @param code 股票代码
     * @param size
     * @return
     */
    public List<F10ProfitEntity> getF10ProfitChart(String code, Integer size) {
        int marketType = getMarketType(code);
        List<F10ProfitEntity> f10ProfitEntityList = null;
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
                    f10ProfitEntityList = getProfitNonFinancialChart(f10PageReq);
                    break;
                case FINANCIAL:
                    f10ProfitEntityList = getProfitFinancialChart(f10PageReq);
                    break;
                case INSURANCE:
                    f10ProfitEntityList = getProfitInsuranceChart(f10PageReq);
                    break;
                default:
                    break;
            }
        }
        if (CollectionUtils.isEmpty(f10ProfitEntityList)) {
            f10ProfitEntityList = new ArrayList<>();
        }
        return f10ProfitEntityList;
    }

    /**
     * 获取利润（非金融）图表数据
     *
     * @param f10PageReq
     * @return
     */
    public List<F10ProfitEntity> getProfitNonFinancialChart(F10PageReq f10PageReq) {
        List<F10ProfitEntity> f10ProfitEntityList = new ArrayList<>();
        F10PageResp<F10NoFinProfitEntity> f10PageResp = f10ProfitDao.pageNonFinancial(f10PageReq);
        List<F10NoFinProfitEntity> f10NoFinProfitEntityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(f10NoFinProfitEntityList,f10PageReq.getPageSize());
        if (CollectionUtils.isNotEmpty(f10NoFinProfitEntityList)) {
            dateList.forEach(d -> {
                F10ProfitEntity f10ProfitEntity = new F10ProfitEntity();
                f10NoFinProfitEntityList.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        f10ProfitEntity.setNetProfits(f.getHoldersOfShareCapitalOfTheCompany());
                        f10ProfitEntity.setOperatingProfit(F10Val.builder().val(f.getOperatingProfit().getVal()).yoy(f.getOperatingProfit().getYoy()).build());
                        f10ProfitEntity.setTaking(F10Val.builder().val(f.getOperatingAndOtherRevenue().getVal()).yoy(f.getOperatingAndOtherRevenue().getYoy()).build());
                        f10ProfitEntity.setCurrency(f.getCurrency());
                    }
                });
                f10ProfitEntity.setTime(DateUtils.parseDate(d).getTime());
                f10ProfitEntityList.add(f10ProfitEntity);
            });
        }
        return f10ProfitEntityList;
    }

    /**
     * 获取利润（金融）图表数据
     *
     * @param f10PageReq
     * @return
     */
    public List<F10ProfitEntity> getProfitFinancialChart(F10PageReq f10PageReq) {
        List<F10ProfitEntity> f10ProfitEntityList = new ArrayList<>();
        F10PageResp<F10FinProfitEntity> f10PageResp = f10ProfitDao.pageFinancial(f10PageReq);
        List<F10FinProfitEntity> f10FinProfitEntities = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(f10FinProfitEntities,f10PageReq.getPageSize());
        if (CollectionUtils.isNotEmpty(f10FinProfitEntities)) {
            dateList.forEach(d -> {
                F10ProfitEntity f10ProfitEntity = new F10ProfitEntity();
                f10FinProfitEntities.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        f10ProfitEntity.setNetProfits(f.getHoldersShareCapitalCompany());
                        f10ProfitEntity.setOperatingProfit(f.getOperatingProfit());
                        f10ProfitEntity.setTaking(f.getGrossRevenue());
                        f10ProfitEntity.setCurrency(f.getCurrency());
                    }
                });
                f10ProfitEntity.setTime(DateUtils.parseDate(d).getTime());
                f10ProfitEntityList.add(f10ProfitEntity);
            });
        }
        return f10ProfitEntityList;
    }

    /**
     * 获取利润（保险）图表数据
     *
     * @param f10PageReq
     * @return
     */
    public List<F10ProfitEntity> getProfitInsuranceChart(F10PageReq f10PageReq) {
        List<F10ProfitEntity> f10ProfitEntityList = new ArrayList<>();
        F10PageResp<F10InsureProfitEntity> f10PageResp = f10ProfitDao.pageInsurance(f10PageReq);
        List<F10InsureProfitEntity> f10InsureProfitEntities = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(f10InsureProfitEntities,f10PageReq.getPageSize());
        if (CollectionUtils.isNotEmpty(f10InsureProfitEntities)) {
            dateList.forEach(d -> {
                F10ProfitEntity f10ProfitEntity = new F10ProfitEntity();
                f10InsureProfitEntities.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        f10ProfitEntity.setNetProfits(f.getHoldersShareCapitalCompany());
                        f10ProfitEntity.setOperatingProfit(f.getOperatingProfit());
                        f10ProfitEntity.setTaking(f.getGrossRevenue());
                        f10ProfitEntity.setCurrency(f.getCurrency());
                    }
                });
                f10ProfitEntity.setTime(DateUtils.parseDate(d).getTime());
                f10ProfitEntityList.add(f10ProfitEntity);
            });
        }
        return f10ProfitEntityList;
    }

    /**
     * 获取资产负债图表
     *
     * @param code 股票代码
     * @return
     */
    public List<AssetsLiabilitiesChartEntity> getF10AssetsLiabilitiesChart(String code, Integer size) {
        int marketType = getMarketType(code);
        List<AssetsLiabilitiesChartEntity> assetsLiabilitiesChartEntityList = null;
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
                    assetsLiabilitiesChartEntityList = getNoFinAssetsLiabilitiesChart(f10PageReq);
                    break;
                case FINANCIAL:
                    assetsLiabilitiesChartEntityList = getFinAssetsLiabilitiesChart(f10PageReq);
                    break;
                case INSURANCE:
                    assetsLiabilitiesChartEntityList = getInsAssetsLiabilitiesChart(f10PageReq);
                    break;
                default:
                    break;
            }
        }
        if (CollectionUtils.isEmpty(assetsLiabilitiesChartEntityList)) {
            assetsLiabilitiesChartEntityList = new ArrayList<>();
        }
        return assetsLiabilitiesChartEntityList;
    }

    /**
     * 获取资产负债图表数据（非金融）
     *
     * @return
     */
    private List<AssetsLiabilitiesChartEntity> getNoFinAssetsLiabilitiesChart(F10PageReq f10PageReq) {
        List<AssetsLiabilitiesChartEntity> assetsLiabilitiesChartEntityList = new ArrayList<>();
        F10PageResp<F10AssetsLiabilitiesNonFinancialEntity> f10PageResp = f10AssetsLiabilitiesDao.pageNonFinancial(f10PageReq);
        List<F10AssetsLiabilitiesNonFinancialEntity> assetsLiabilitiesNonFinancialEntities = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(assetsLiabilitiesNonFinancialEntities,f10PageResp.getPageSize());
        if (CollectionUtils.isNotEmpty(assetsLiabilitiesNonFinancialEntities)) {
            dateList.forEach(d -> {
                AssetsLiabilitiesChartEntity assetsLiabilitiesChartEntity = new AssetsLiabilitiesChartEntity();
                assetsLiabilitiesNonFinancialEntities.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        assetsLiabilitiesChartEntity.setTotalAssets(f.getTotalAssets().getVal());
                        assetsLiabilitiesChartEntity.setTotalIndebtedness(f.getTotalLiabilities().getVal());
                        assetsLiabilitiesChartEntity.setDebtRatio(getDebtRatio(f.getTotalAssets().getVal(), f.getTotalLiabilities().getVal()));
                        assetsLiabilitiesChartEntity.setCurrency(f.getCurrency());
                    }
                });
                assetsLiabilitiesChartEntity.setTime(DateUtils.parseDate(d).getTime());
                assetsLiabilitiesChartEntityList.add(assetsLiabilitiesChartEntity);
            });
        }
        return assetsLiabilitiesChartEntityList;
    }

    /**
     * 获取资产负债图表数据（金融）
     *
     * @return
     */
    private List<AssetsLiabilitiesChartEntity> getFinAssetsLiabilitiesChart(F10PageReq f10PageReq) {
        List<AssetsLiabilitiesChartEntity> assetsLiabilitiesChartEntityList = new ArrayList<>();

        F10PageResp<F10AssetsLiabilitiesFinancialEntity> f10PageResp = f10AssetsLiabilitiesDao.pageFinancial(f10PageReq);
        List<F10AssetsLiabilitiesFinancialEntity> assetsLiabilitiesFinancialEntityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(assetsLiabilitiesFinancialEntityList,f10PageResp.getPageSize());
        if (CollectionUtils.isNotEmpty(assetsLiabilitiesFinancialEntityList)) {
            dateList.forEach(d -> {
                AssetsLiabilitiesChartEntity assetsLiabilitiesChartEntity = new AssetsLiabilitiesChartEntity();
                assetsLiabilitiesFinancialEntityList.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        assetsLiabilitiesChartEntity.setTotalAssets(f.getTotalAssets().getVal());
                        assetsLiabilitiesChartEntity.setTotalIndebtedness(f.getTotalLiabilities().getVal());
                        assetsLiabilitiesChartEntity.setDebtRatio(getDebtRatio(f.getTotalAssets().getVal(), f.getTotalLiabilities().getVal()));
                        assetsLiabilitiesChartEntity.setCurrency(f.getCurrency());
                    }
                });
                assetsLiabilitiesChartEntity.setTime(DateUtils.parseDate(d).getTime());
                assetsLiabilitiesChartEntityList.add(assetsLiabilitiesChartEntity);
            });
        }

        return assetsLiabilitiesChartEntityList;
    }

    /**
     * 获取资产负债图表数据（保险）
     *
     * @return
     */
    private List<AssetsLiabilitiesChartEntity> getInsAssetsLiabilitiesChart(F10PageReq f10PageReq) {
        List<AssetsLiabilitiesChartEntity> assetsLiabilitiesChartEntityList = new ArrayList<>();
        F10PageResp<F10AssetsLiabilitiesInsuranceEntity> f10PageResp = f10AssetsLiabilitiesDao.pageInsurance(f10PageReq);
        List<F10AssetsLiabilitiesInsuranceEntity> assetsLiabilitiesInsuranceEntityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(assetsLiabilitiesInsuranceEntityList,f10PageReq.getPageSize());
        if (CollectionUtils.isNotEmpty(assetsLiabilitiesInsuranceEntityList)) {
            dateList.forEach(d -> {
                AssetsLiabilitiesChartEntity assetsLiabilitiesChartEntity = new AssetsLiabilitiesChartEntity();
                assetsLiabilitiesInsuranceEntityList.forEach(f -> {
                    if (DateUtils.formatDate(new Date(f.getEndTimestamp()), TIME_PARSE).equals(d)) {
                        assetsLiabilitiesChartEntity.setTotalAssets(f.getTotalAssets().getVal());
                        assetsLiabilitiesChartEntity.setTotalIndebtedness(f.getTotalLiabilities().getVal());
                        assetsLiabilitiesChartEntity.setDebtRatio(getDebtRatio(f.getTotalAssets().getVal(), f.getTotalLiabilities().getVal()));
                        assetsLiabilitiesChartEntity.setCurrency(f.getCurrency());
                    }
                });
                assetsLiabilitiesChartEntity.setTime(DateUtils.parseDate(d).getTime());
                assetsLiabilitiesChartEntityList.add(assetsLiabilitiesChartEntity);
            });
        }

        return assetsLiabilitiesChartEntityList;
    }

    /**
     * 获得盈利能力图表
     *
     * @param code
     * @param size
     * @return
     */
    public List<F10ProfitabilityEntity> getF10Profitability(String code, Integer size){
        List<F10ProfitabilityEntity> f10ProfitabilityEntityList = new ArrayList<>();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().reportType("F").stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(size == null ? 10 : size)
                .currentPage(0)
                .build();

        F10PageResp<F10KeyFiguresNonFinancialEntity> f10PageResp = f10KeyFiguresDao.pageNonFinancial(f10PageReq);
        List<F10KeyFiguresNonFinancialEntity> f10KeyFiguresNonFinancialEntityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(f10KeyFiguresNonFinancialEntityList,f10PageReq.getPageSize());
        if(CollectionUtils.isNotEmpty(f10KeyFiguresNonFinancialEntityList)){
            dateList.forEach(d ->{
                F10ProfitabilityEntity f10ProfitabilityEntity = new F10ProfitabilityEntity();
                f10KeyFiguresNonFinancialEntityList.forEach(f -> {
                    if(DateUtils.formatDate(new Date(f.getEndTimestamp()),TIME_PARSE).equals(d)){
                        String[] earningsIndicator = new String[]{
                                f.getProfitability() == null || f.getProfitability().getRoa() == null || f.getProfitability().getRoa().getVal() == null ? null : f.getProfitability().getRoa().getVal().toString(),
                                f.getProfitability() == null || f.getProfitability().getRoe() == null || f.getProfitability().getRoe().getVal() == null ? null : f.getProfitability().getRoe().getVal().toString(),
                                f.getProfitability() == null || f.getProfitability().getRoce() == null || f.getProfitability().getRoce().getVal() == null ? null : f.getProfitability().getRoce().getVal().toString()
                        };
                        String[] salesProfitRatio = new String[]{
                                f.getProfitability() == null || f.getProfitability().getProfitRatio() == null || f.getProfitability().getProfitRatio().getVal() == null ? null : f.getProfitability().getProfitRatio().getVal().toString(),
                                f.getProfitability() == null || f.getProfitability().getNpoms() == null || f.getProfitability().getNpoms().getVal() == null ? null : f.getProfitability().getNpoms().getVal().toString(),
                                f.getProfitability() == null || f.getProfitability().getNetProfitRatio() == null || f.getProfitability().getNetProfitRatio().getVal() == null ? null : f.getProfitability().getNetProfitRatio().getVal().toString()
                        };
                        String[] evebitda = new String[]{
                                f.getProfitability() == null || f.getProfitability().getEvebitda() == null ? null : f.getProfitability().getEvebitda().toString()
                        };
                        f10ProfitabilityEntity.setEarningsIndicator(earningsIndicator);
                        f10ProfitabilityEntity.setSalesProfitRatio(salesProfitRatio);
                        f10ProfitabilityEntity.setEvebitda(evebitda);
                    }
                });
                f10ProfitabilityEntity.setTime(DateUtils.parseDate(d).getTime());
                f10ProfitabilityEntityList.add(f10ProfitabilityEntity);
            });
        }
        return f10ProfitabilityEntityList;
    }

    /**
     * 获得运营能力图表
     *
     * @param code
     * @param size
     * @return
     */
    public List<F10OperatingCapacityEntity> getOperatingCapacity(String code,Integer size){
        List<F10OperatingCapacityEntity> f10OperatingCapacityEntityList = new ArrayList<>();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().reportType("F").stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(size == null ? 10 : size)
                .currentPage(0)
                .build();

        F10PageResp<F10KeyFiguresNonFinancialEntity> f10PageResp = f10KeyFiguresDao.pageNonFinancial(f10PageReq);
        List<F10KeyFiguresNonFinancialEntity> f10KeyFiguresNonFinancialEntityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(f10KeyFiguresNonFinancialEntityList,f10PageReq.getPageSize());
        if(CollectionUtils.isNotEmpty(f10KeyFiguresNonFinancialEntityList)){
            dateList.forEach(d -> {
                F10OperatingCapacityEntity f10OperatingCapacityEntity = new F10OperatingCapacityEntity();
                f10KeyFiguresNonFinancialEntityList.forEach(f -> {
                    if(DateUtils.formatDate(new Date(f.getEndTimestamp()),TIME_PARSE).equals(d)){
                        String[] totalAssetsTurnover = new String[]{
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getTotalAssetsTurnover() == null || f.getOperatingCapacity().getTotalAssetsTurnover().getVal() == null ? null : f.getOperatingCapacity().getTotalAssetsTurnover().getVal().toString()
                        };
                        String[] tose = new String[]{
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getTose() == null || f.getOperatingCapacity().getTose().getVal() == null ? null : f.getOperatingCapacity().getTose().getVal().toString()
                        };
                        String[] operationalTurnover = new String[]{
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getItr() == null || f.getOperatingCapacity().getItr().getVal() == null ? null : f.getOperatingCapacity().getItr().getVal().toString(),
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getToar() == null || f.getOperatingCapacity().getToar().getVal() == null ? null : f.getOperatingCapacity().getToar().getVal().toString(),
                                f.getOperatingCapacity() == null || f.getOperatingCapacity().getTroap() == null || f.getOperatingCapacity().getTroap().getVal() == null ? null : f.getOperatingCapacity().getTroap().getVal().toString()
                        };
                        f10OperatingCapacityEntity.setTotalAssetsTurnover(totalAssetsTurnover);
                        f10OperatingCapacityEntity.setTose(tose);
                        f10OperatingCapacityEntity.setOperationalTurnover(operationalTurnover);
                    }
                });
                f10OperatingCapacityEntity.setTime(DateUtils.parseDate(d).getTime());
                f10OperatingCapacityEntityList.add(f10OperatingCapacityEntity);
            });
        }
        return f10OperatingCapacityEntityList;
    }

    /**
     * 获得长期偿债能力图表
     *
     * @param code
     * @param size
     * @return
     */
    public List<F10SolvencyEntity> getSolvency(String code,Integer size){
        List<F10SolvencyEntity> f10SolvencyEntityList = new ArrayList<>();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().reportType("F").stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(size == null ? 10 : size)
                .currentPage(0)
                .build();

        F10PageResp<F10KeyFiguresNonFinancialEntity> f10PageResp = f10KeyFiguresDao.pageNonFinancial(f10PageReq);
        List<F10KeyFiguresNonFinancialEntity> f10KeyFiguresNonFinancialEntityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(f10KeyFiguresNonFinancialEntityList,f10PageReq.getPageSize());
        if(CollectionUtils.isNotEmpty(f10KeyFiguresNonFinancialEntityList)){
            dateList.forEach(d -> {
                F10SolvencyEntity f10SolvencyEntity = new F10SolvencyEntity();
                f10KeyFiguresNonFinancialEntityList.forEach(f -> {
                    if(DateUtils.formatDate(new Date(f.getEndTimestamp()),TIME_PARSE).equals(d)){
                        String[] operatingCashFlowRatio = new String[]{
                                f.getSolvency() == null || f.getSolvency().getOperatingCashFlowRatio() == null ||f.getSolvency().getOperatingCashFlowRatio().getVal() == null ? null : f.getSolvency().getOperatingCashFlowRatio().getVal().toString(),
                                f.getSolvency() == null || f.getSolvency().getNetCashFlowGearingRatio() == null || f.getSolvency().getNetCashFlowGearingRatio().getVal() == null ? null : f.getSolvency().getNetCashFlowGearingRatio().getVal().toString()
                        };
                        String[] equityRatio = new String[]{
                                f.getSolvency() == null || f.getSolvency().getEquityRatio() == null || f.getSolvency().getEquityRatio().getVal() == null ? null : f.getSolvency().getEquityRatio().getVal().toString()
                        };
                        f10SolvencyEntity.setEquityRatio(equityRatio);
                        f10SolvencyEntity.setEquityMultiplier(f.getSolvency() == null || f.getSolvency().getEquityMultiplier() == null ? null : f.getSolvency().getEquityMultiplier());
                        f10SolvencyEntity.setDebtCoverageRatio(f.getSolvency() == null || f.getSolvency().getDebtCoverageRatio() == null ? null : f.getSolvency().getDebtCoverageRatio());
                        f10SolvencyEntity.setInterestCoverageRatio(f.getSolvency() == null || f.getSolvency().getInterestCoverageRatio() == null ? null : f.getSolvency().getInterestCoverageRatio());
                        f10SolvencyEntity.setOperatingCashFlowRatio(operatingCashFlowRatio);
                    }
                });
                f10SolvencyEntity.setTime(DateUtils.parseDate(d).getTime());
                f10SolvencyEntityList.add(f10SolvencyEntity);
            });
        }
        return f10SolvencyEntityList;
    }

    /**
     * 获得现金流量指标图表
     *
     * @param code
     * @param size
     * @return
     */
    public List<F10CashFlowIndicatorEntity> getCashFlowIndicator(String code,Integer size){
        List<F10CashFlowIndicatorEntity> f10CashFlowIndicatorEntityList = new ArrayList<>();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().reportType("F").stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(size == null ? 10 : size)
                .currentPage(0)
                .build();

        F10PageResp<F10KeyFiguresNonFinancialEntity> f10PageResp = f10KeyFiguresDao.pageNonFinancial(f10PageReq);
        List<F10KeyFiguresNonFinancialEntity> f10KeyFiguresNonFinancialEntityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(f10KeyFiguresNonFinancialEntityList,f10PageReq.getPageSize());
        if(CollectionUtils.isNotEmpty(f10KeyFiguresNonFinancialEntityList)){
            dateList.forEach(d -> {
                F10CashFlowIndicatorEntity f10CashFlowIndicatorEntity = new F10CashFlowIndicatorEntity();
                f10KeyFiguresNonFinancialEntityList.forEach(f -> {
                    if(DateUtils.formatDate(new Date(f.getEndTimestamp()),TIME_PARSE).equals(d)){
                        String[] cashNetFlowProfit = new String[]{
                                f.getCashFlowIndicator() == null || f.getCashFlowIndicator().getNetOperatingCashFlowAndTotalProfit() == null || f.getCashFlowIndicator().getNetOperatingCashFlowAndTotalProfit().getVal() == null ? null : f.getCashFlowIndicator().getNetOperatingCashFlowAndTotalProfit().getVal().toString(),
                                f.getCashFlowIndicator() == null || f.getCashFlowIndicator().getNetOperatingCashFlowAndGrossOperatingIncome() == null || f.getCashFlowIndicator().getNetOperatingCashFlowAndGrossOperatingIncome().getVal() == null ? null : f.getCashFlowIndicator().getNetOperatingCashFlowAndGrossOperatingIncome().getVal().toString()
                        };
                        String[] cashContent = new String[]{
                                f.getCashFlowIndicator() == null || f.getCashFlowIndicator().getNetProfitCashContent() == null || f.getCashFlowIndicator().getNetProfitCashContent().getVal() == null ? null : f.getCashFlowIndicator().getNetProfitCashContent().getVal().toString(),
                                f.getCashFlowIndicator() == null || f.getCashFlowIndicator().getCashContentRevenueIncome() == null || f.getCashFlowIndicator().getCashContentRevenueIncome().getVal() == null ? null : f.getCashFlowIndicator().getCashContentRevenueIncome().getVal().toString()
                        };
                        String[] salesCashRatio = new String[]{
                                f.getCashFlowIndicator() == null || f.getCashFlowIndicator().getSalesCashRatio() == null || f.getCashFlowIndicator().getSalesCashRatio().getVal() == null ? null : f.getCashFlowIndicator().getSalesCashRatio().getVal().toString()
                        };
                        f10CashFlowIndicatorEntity.setSalesCashRatio(salesCashRatio);
                        f10CashFlowIndicatorEntity.setCashNetFlowProfit(cashNetFlowProfit);
                        f10CashFlowIndicatorEntity.setCashContent(cashContent);
                    }
                });
                f10CashFlowIndicatorEntity.setTime(DateUtils.parseDate(d).getTime());
                f10CashFlowIndicatorEntityList.add(f10CashFlowIndicatorEntity);
            });
        }
        return f10CashFlowIndicatorEntityList;
    }

    /**
     * 获得成本盈利能力图表
     *
     * @param code
     * @param size
     * @return
     */
    public List<F10CostProfitabilityEntity> getCostProfitability(String code,Integer size){
        List<F10CostProfitabilityEntity> f10CostProfitabilityEntityList = new ArrayList<>();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().reportType("F").stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(size == null ? 10 : size)
                .currentPage(0)
                .build();

        F10PageResp<F10KeyFiguresNonFinancialEntity> f10PageResp = f10KeyFiguresDao.pageNonFinancial(f10PageReq);
        List<F10KeyFiguresNonFinancialEntity> f10KeyFiguresNonFinancialEntityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(f10KeyFiguresNonFinancialEntityList,f10PageReq.getPageSize());
        if(CollectionUtils.isNotEmpty(f10KeyFiguresNonFinancialEntityList)){
            dateList.forEach(d -> {
                F10CostProfitabilityEntity f10CostProfitabilityEntity = new F10CostProfitabilityEntity();
                f10KeyFiguresNonFinancialEntityList.forEach(f -> {
                    if(DateUtils.formatDate(new Date(f.getEndTimestamp()),TIME_PARSE).equals(d)){
                        String[] costProfit = new String[]{
                                f.getCostProfitability() == null || f.getCostProfitability().getCostProfitMargin() == null || f.getCostProfitability().getCostProfitMargin().getVal() == null ? null : f.getCostProfitability().getCostProfitMargin().getVal().toString(),
                                f.getCostProfitability() == null || f.getCostProfitability().getCostRate() == null || f.getCostProfitability().getCostRate().getVal() == null ? null : f.getCostProfitability().getCostRate().getVal().toString()
                        };
                        String[] majorCostRatio = new String[]{
                                f.getCostProfitability() == null || f.getCostProfitability().getMajorCostRatio() == null || f.getCostProfitability().getMajorCostRatio().getVal() == null ? null : f.getCostProfitability().getMajorCostRatio().getVal().toString()
                        };
                        String[] periodCostRatio = new String[]{
                                f.getCostProfitability() == null || f.getCostProfitability().getSalesExpenseRatio() == null || f.getCostProfitability().getSalesExpenseRatio().getVal() == null ? null : f.getCostProfitability().getSalesExpenseRatio().getVal().toString(),
                                f.getCostProfitability() == null || f.getCostProfitability().getAdministrativeCostsRatio() == null || f.getCostProfitability().getAdministrativeCostsRatio().getVal() == null ? null : f.getCostProfitability().getAdministrativeCostsRatio().getVal().toString(),
                                f.getCostProfitability() == null || f.getCostProfitability().getFinancialCostsRatio() == null || f.getCostProfitability().getFinancialCostsRatio().getVal() == null ? null : f.getCostProfitability().getFinancialCostsRatio().getVal().toString()
                        };
                        f10CostProfitabilityEntity.setCostProfit(costProfit);
                        f10CostProfitabilityEntity.setMajorCostRatio(majorCostRatio);
                        f10CostProfitabilityEntity.setPeriodCostRatio(periodCostRatio);
                    }
                });
                f10CostProfitabilityEntity.setTime(DateUtils.parseDate(d).getTime());
                f10CostProfitabilityEntityList.add(f10CostProfitabilityEntity);
            });
        }
        return f10CostProfitabilityEntityList;
    }

    /**
     * 获得变现能力图表
     *
     * @param code
     * @param size
     * @return
     */
    public List<F10CashAbilityEntity> getCashAbilityEntity(String code,Integer size){
        List<F10CashAbilityEntity> cashAbilityEntityList = new ArrayList<>();
        F10CommonRequest f10CommonRequest = F10CommonRequest.builder().reportType("F").stockCode(code).build();
        F10PageReq f10PageReq = F10PageReq.builder()
                .params(f10CommonRequest)
                .desc(true)
                .pageSize(size == null ? 10 : size)
                .currentPage(0)
                .build();

        F10PageResp<F10KeyFiguresNonFinancialEntity> f10PageResp = f10KeyFiguresDao.pageNonFinancial(f10PageReq);
        List<F10KeyFiguresNonFinancialEntity> f10KeyFiguresNonFinancialEntityList = f10PageResp.getRecord();
        List<String> dateList = getTenYearDate(f10KeyFiguresNonFinancialEntityList,f10PageReq.getPageSize());
        if(CollectionUtils.isNotEmpty(f10KeyFiguresNonFinancialEntityList)){
            dateList.forEach(d -> {
                F10CashAbilityEntity f10CashAbilityEntity = new F10CashAbilityEntity();
                f10KeyFiguresNonFinancialEntityList.forEach(f -> {
                    if(DateUtils.formatDate(new Date(f.getEndTimestamp()),TIME_PARSE).equals(d)){
                        f10CashAbilityEntity.setCurrentRatio(f.getCashability() == null || f.getCashability().getCurrentRatio() == null || f.getCashability().getCurrentRatio().getVal() == null ? null : f.getCashability().getCurrentRatio().getVal());
                        f10CashAbilityEntity.setQuickRatio(f.getCashability() == null || f.getCashability().getQuickRatio() == null || f.getCashability().getQuickRatio().getVal() == null ? null : f.getCashability().getQuickRatio().getVal());
                        f10CashAbilityEntity.setCashRatio(f.getCashability() == null || f.getCashability().getCashRatio() == null || f.getCashability().getCashRatio().getVal() == null ? null : f.getCashability().getCashRatio().getVal());
                     }
                });
                f10CashAbilityEntity.setTime(DateUtils.parseDate(d).getTime());
                cashAbilityEntityList.add(f10CashAbilityEntity);
            });
        }
        return cashAbilityEntityList;
    }


    /**
     * 计算负债率
     *
     * @param totalAssets
     * @param totalLiabilities
     * @return
     */
    private BigDecimal getDebtRatio(BigDecimal totalAssets, BigDecimal totalLiabilities) {
        BigDecimal deptRatio = new BigDecimal(0);
        if (totalAssets != null && totalLiabilities != null) {
            deptRatio = totalAssets.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal(1) :
                    totalLiabilities.divide(totalAssets, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        }
        return deptRatio;
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
        // return marketType;
        return f10SourceService.getMarketType(code);
    }

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

    /**
     * 获取最新一条数据前n年年份
     * @param f10EntityBases
     * @param years
     * @param <T>
     * @return
     */
    public  <T extends F10EntityBase> List<String> getTenYearDate(List<T> f10EntityBases,Integer years) {
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
}
