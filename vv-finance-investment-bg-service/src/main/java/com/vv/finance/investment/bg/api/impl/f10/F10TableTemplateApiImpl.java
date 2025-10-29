package com.vv.finance.investment.bg.api.impl.f10;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.dto.resp.FinancialReportDto;
import com.vv.finance.investment.bg.api.f10.F10TableTemplateApi;
import com.vv.finance.investment.bg.entity.f10.*;
import com.vv.finance.investment.bg.entity.f10.chart.*;
import com.vv.finance.investment.bg.entity.f10.f10Profit.F10ProfitEntity;
import com.vv.finance.investment.bg.entity.f10.fintable.AssetsLiabilitiesChartEntity;
import com.vv.finance.investment.bg.entity.f10.fintable.F10CommonFinTable;
import com.vv.finance.investment.bg.stock.f10.service.IF10TableTemplateV2Service;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10ChartServiceImpl;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10SourceServiceImpl;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10TableTemplateServiceImpl;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @ClassName F10TableTemplateApiImpl
 * @Deacription F10接口实现
 * @Author lh.sz
 * @Date 2021年07月15日 16:24
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class F10TableTemplateApiImpl implements F10TableTemplateApi {

    @Resource
    F10TableTemplateServiceImpl f10TableTemplateService;
    @Resource
    IF10TableTemplateV2Service f10TableTemplateV2Service;
    @Resource
    F10SourceServiceImpl f10SourceService;
    @Resource
    F10ChartServiceImpl f10ChartService;


    @Override
    public List<F10TableTemplate> getList(int tableType) {
        return f10TableTemplateService.list(new QueryWrapper<F10TableTemplate>()
                .eq("sheet_type", tableType)
                .orderByAsc("sort_id")
        );
    }

    @Override
    public List<F10TableTemplateV2> getListV2(int tableType) {
        return f10TableTemplateV2Service.list(new QueryWrapper<F10TableTemplateV2>()
                .eq("sheet_type", tableType)
                .orderByAsc("sort_id")
        );
    }

    @Override
    public List<ReportTypeEntity> getReportType(String code, int reportId, int tableType) {
        return f10SourceService.getReportType(code, reportId, tableType);
    }

    @Override
    public List<RatingsTableEntity> getRatingsTable(String code, Integer size) {
        return f10SourceService.getRatingsTable(code, size);
    }

    @Override
    public F10CommonFinTable getFinancialTable(String code) {
        return f10SourceService.getFinancialTable(code);
    }

    @Override
    public RatingsDigestTableEntity getRatingsDigest(String code) {
        return f10SourceService.getLatestRatingsDigestTable(code);
    }

    @Override
    public List<F10ProfitEntity> getProfitChart(String code, Integer size) {
        return f10ChartService.getF10ProfitChart(code, size);
    }

    @Override
    public List<AssetsLiabilitiesChartEntity> getF10AssetsLiabilitiesChart(String code, Integer size) {
        return f10ChartService.getF10AssetsLiabilitiesChart(code, size);
    }

    @Override
    public List<List<F10TableTemplate>> getAppTableSource(String code, String reportType, long reportTime, int tableType) {
        return f10SourceService.getF10Table(code, reportType, reportTime, tableType);
    }

    @Override
    public List<List<F10TableTemplateV2>> getAppTableSourceV2(String code, String reportType, int tableType, long reportTime) {
        return f10SourceService.getF10TableV2(code, reportType, tableType, reportTime);
    }

    @Override
    public List<List<F10TableTemplate>> getPcTableSource(String code, int reportId, int tableType, long current, long pageSize) {
        return f10SourceService.getPCF10Table(code, reportId, tableType, current, pageSize);
    }

    @Override
    public PageDomain<List<F10TableTemplateV2>> getPcTableSourceV2(String code, int reportId, int tableType, long current, long pageSize) {
        return f10SourceService.getPCF10TableV2(code, reportId, tableType, current, pageSize);
    }

    @Override
    public List<FinancialReportDto> getFinancialReport(String id, int marketType, int pageSize, Long startTime) {
        return f10SourceService.getFinancialReport(id, marketType, pageSize, startTime);
    }

    @Override
    public List<F10CashCharEntity> getCashCharTable(String code, Integer size) {
        return f10SourceService.getCashCharTable(code, size);
    }

    @Override
    public ResultT<SubBusinessInfo> subBusinessInfo(String code) {
        return ResultT.success(f10SourceService.subBusinessInfo(code));
    }

    @Override
    public F10FinancialAnalysisCharVo getFinancialCharVo(String code, Integer size) {
        return f10SourceService.getFinancialCharVo(code, size);
    }

    @Override
    public List<F10ProfitabilityEntity> getProfitability(String code, Integer size) {
        return f10ChartService.getF10Profitability(code,size);
    }

    @Override
    public List<F10OperatingCapacityEntity> getOperatingCapacity(String code, Integer size) {
        return f10ChartService.getOperatingCapacity(code,size);
    }

    @Override
    public List<F10CashAbilityEntity> getCashAbility(String code, Integer size) {
        return f10ChartService.getCashAbilityEntity(code,size);
    }

    @Override
    public List<F10SolvencyEntity> getSolvency(String code, Integer size) {
        return f10ChartService.getSolvency(code,size);
    }

    @Override
    public List<F10CashFlowIndicatorEntity> getCashFlowIndicator(String code, Integer size) {
        return f10ChartService.getCashFlowIndicator(code,size);
    }

    @Override
    public List<F10CostProfitabilityEntity> getCostProfitability(String code, Integer size) {
        return f10ChartService.getCostProfitability(code,size);
    }

    @Override
    public MarketType getMarketType(String code) {
        return new MarketType(f10ChartService.getMarketType(code));
    }

    @Override
    public List<DuPontAnalysisEntity> duPontAnalyze(String stockCode) {
        return f10SourceService.getDuPontAnalyze(stockCode);
    }

    @Override
    public void createF10DataByCode(String stockCode) {
        f10SourceService.createF10DataByCode(stockCode);
    }

    @Override
    public void deleteF10DataByCode(String stockCode) {
        f10SourceService.deleteF10DataByCode(stockCode);
    }

    @Override
    public void updateF10DataByCode(String oldStockCode, String newStockCode) {
        f10SourceService.updateF10DataByCode(oldStockCode, newStockCode);
    }
}
