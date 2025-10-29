package com.vv.finance.investment.bg.api.f10;

import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.dto.resp.FinancialReportDto;
import com.vv.finance.investment.bg.entity.f10.*;
import com.vv.finance.investment.bg.entity.f10.chart.*;
import com.vv.finance.investment.bg.entity.f10.f10Profit.F10ProfitEntity;
import com.vv.finance.investment.bg.entity.f10.fintable.AssetsLiabilitiesChartEntity;
import com.vv.finance.investment.bg.entity.f10.fintable.F10CommonFinTable;
import io.swagger.models.auth.In;

import java.util.List;
import java.util.Map;

/**
 * @ClassName F10TableTemplateApi
 * @Deacription f10模板
 * @Author lh.sz
 * @Date 2021年07月15日 16:11
 **/
public interface F10TableTemplateApi {
    /**
     * 获取表格类型
     *
     * @param tableType 表格类型
     * @return
     */
    List<F10TableTemplate> getList(int tableType);

    /**
     * 获取表格类型
     *
     * @param tableType 表格类型
     * @return
     */
    List<F10TableTemplateV2> getListV2(int tableType);

    /**
     * 获取报告类型
     *
     * @param code      股票代码
     * @param reportId  报告id
     * @param tableType 表格类型
     * @return
     */
    List<ReportTypeEntity> getReportType(String code, int reportId, int tableType);

    /**
     * 获取主要指标图表
     *
     * @param code 股票代码
     * @param size
     * @return
     */
    List<RatingsTableEntity> getRatingsTable(String code, Integer size);

    /**
     * 获取财务报表
     *
     * @param code 股票代码
     * @return
     */
    F10CommonFinTable getFinancialTable(String code);

    /**
     * 操盘必读-主要指标数据
     *
     * @param code
     * @return
     */
    RatingsDigestTableEntity getRatingsDigest(String code);

    /**
     * 获取利润表图表
     *
     * @param code 股票代码
     * @param size
     * @return
     */
    List<F10ProfitEntity> getProfitChart(String code, Integer size);

    /**
     * 获取资产负债表图表
     *
     * @param code 股票代码
     * @return
     */
    List<AssetsLiabilitiesChartEntity> getF10AssetsLiabilitiesChart(String code, Integer size);

    /**
     * app获取表格数据
     *
     * @param code
     * @param reportType
     * @param reportTime
     * @param tableType
     * @return
     */
    List<List<F10TableTemplate>> getAppTableSource(
            String code,
            String reportType,
            long reportTime,
            int tableType
    );

    /**
     * app获取表格数据
     *
     * @param code
     * @param reportType
     * @param reportTime
     * @param tableType
     * @return
     */
    List<List<F10TableTemplateV2>> getAppTableSourceV2(String code, String reportType, int tableType, long reportTime);

    /**
     * pc端获取表格数据
     *
     * @param code      股票代码
     * @param reportId  报告种类
     * @param tableType 表格类型
     * @param current   开始多少页
     * @param pageSize  总共多少页
     * @return
     */
    List<List<F10TableTemplate>> getPcTableSource(
            String code,
            int reportId,
            int tableType,
            long current,
            long pageSize
    );

    /**
     * pc端获取表格数据
     *
     * @param code      股票代码
     * @param reportId  报告类型 1:全部 ,2:年报 ,3:中报 4:季报
     * @param tableType 表格类型 1:主要指标 2:利润表 3：资产负债表 4:现金流量表
     * @param current   开始多少页
     * @param pageSize  总共多少页
     * @return
     */
    PageDomain<List<F10TableTemplateV2>> getPcTableSourceV2(String code, int reportId, int tableType, long current, long pageSize);

    List<FinancialReportDto> getFinancialReport(String id, int marketType, int pageSize, Long startTime);

    /**
     * 现金流量表
     * @param code
     * @param size
     * @return
     */
    List<F10CashCharEntity> getCashCharTable(String code, Integer size);

    /**
     * 订阅行业涨跌幅
     * @param code
     * @return
     */
    ResultT<SubBusinessInfo> subBusinessInfo(String code);

    /**
     * 返回财务分析图表 含股票类型
     * @param code
     * @param size
     * @return
     */
    F10FinancialAnalysisCharVo getFinancialCharVo(String code, Integer size);

    /**
     * 获得盈利能力图表
     * @param code
     * @param size
     * @return
     */
    List<F10ProfitabilityEntity> getProfitability(String code, Integer size);

    /**
     * 获得运营能力图表
     * @param code
     * @param size
     * @return
     */
    List<F10OperatingCapacityEntity> getOperatingCapacity(String code,Integer size);

    /**
     * 获得变现能力图表
     * @param code
     * @param size
     * @return
     */
    List<F10CashAbilityEntity> getCashAbility(String code,Integer size);

    /**
     * 获得长期债偿能力图表
     * @param code
     * @param size
     * @return
     */
    List<F10SolvencyEntity> getSolvency(String code,Integer size);

    /**
     * 获得现金流量指标图表
     * @param code
     * @param size
     * @return
     */
    List<F10CashFlowIndicatorEntity> getCashFlowIndicator(String code,Integer size);

    /**
     * 获得成本盈利能力图表
     * @param code
     * @param size
     * @return
     */
    List<F10CostProfitabilityEntity> getCostProfitability(String code, Integer size);

    /**
     * 获得股票市场类型
     * @param code
     * @return
     */
    MarketType getMarketType(String code);

    /**
     * 杜邦分析
     * @param stockCode
     * @return {@link List}<{@link DuPontAnalysisEntity}>
     */
    List<DuPontAnalysisEntity> duPontAnalyze(String stockCode);

    /**
     * 新增F10数据
     *
     * @param stockCode 代码
     */
    void createF10DataByCode(String stockCode);

    /**
     * 删除F10数据
     *
     * @param stockCode 代码
     */
    void deleteF10DataByCode(String stockCode);

    /**
     * 更新F10数据
     *
     * @param oldStockCode 老股票股票代码
     * @param newStockCode 新增功能股票股票代码
     */
    void updateF10DataByCode(String oldStockCode, String newStockCode);
}
