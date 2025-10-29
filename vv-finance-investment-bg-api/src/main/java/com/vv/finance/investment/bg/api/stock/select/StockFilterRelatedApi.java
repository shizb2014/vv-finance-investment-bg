package com.vv.finance.investment.bg.api.stock.select;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.domain.filter.EnumValues;
import com.vv.finance.investment.bg.stock.select.dto.CashFlowSelectDto;
import com.vv.finance.investment.bg.stock.select.dto.ProfitSelectDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author wsliang
 * 策略选股相关dubbo接口
 * @date 2022/2/17 14:28
 **/
public interface StockFilterRelatedApi {

    /**
     * 获取股票和板块映射关系
     * key -> stockCode
     * value -> plateCode
     *
     * @return
     */
    ResultT<Map<String, Object>> getPlateMap();

    /**
     * 获取板块枚举
     *
     * @return
     */
    ResultT<List<EnumValues>> enumsPlate();

    /**
     * 获取概念枚举
     *
     * @return
     */
    ResultT<List<EnumValues>> enumsConcept();

    /**
     * 获取指数成分股票
     *
     * @return
     */
    @Deprecated
    ResultT<Map<String, Object>> getIndexStocks();

    /**
     * 获取指数成分股票
     *
     * @return
     */
    ResultT<Map<String, List<String>>> getIndexStockMap();

    /**
     * 获取利润表所有数据
     *
     * @return
     */
    ResultT<Map<String, List<ProfitSelectDto>>> getFinProfitDate();

    /**
     * 获取财报数据
     * @param reportType 财报类型 年/半年/Q1/Q2/Q3/Q4
     * @return
     */
    ResultT<List<ProfitSelectDto>> getFinProfitDateByType(String reportType);

    /**
     * 获取现金流量表所有数据
     *
     * @return
     */
    ResultT<Map<String, List<CashFlowSelectDto>>> getCashFlowDate();

    /**
     * 获取现金流量表所有数据
     * @param reportType 财报类型 年/半年/Q1/Q2/Q3/Q4
     * @return
     */
    ResultT<List<CashFlowSelectDto>> getCashFlowDateByType(String reportType);

    /**
     * 利润缓存过期重新构建缓存
     */
    void rebuildProfitCache();
    /**
     * 现金流缓存过期重新构建缓存
     */
    void rebuildCashFlowCache();

    /**
     * 获取全量的财务数据
     * @param reportTypeList 报表类型
     * @return
     */
    ResultT<List<ProfitSelectDto>> getAllFinProfitData(Collection<String> codeSet, List<String> reportTypeList);

    /**
     * 获取行业代码key对应的股票代码list
     *
     * @return
     */
    ResultT<Map<String, List<String>>> getIndustryCodeMap();

    /**
     * 获取概念代码key对应的股票代码list
     *
     * @return
     */
    ResultT<Map<String, List<String>>> getConceptCodeMap();


    /**
     * 获取全量的现金数据
     * @param reportTypeList 报表类型
     * @return
     */
    ResultT<List<CashFlowSelectDto>> getAllCashFlowData(Collection<String> codeSet,List<String> reportTypeList);

}
