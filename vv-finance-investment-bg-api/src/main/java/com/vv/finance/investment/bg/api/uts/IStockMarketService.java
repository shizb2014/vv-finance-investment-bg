package com.vv.finance.investment.bg.api.uts;


import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.bean.SimplePageResp;
import com.vv.finance.investment.bg.dto.uts.resp.HoldStockChange;
import com.vv.finance.investment.bg.dto.uts.resp.ValuationGrowth;
import com.vv.finance.investment.bg.entity.f10.industry.MarketPresence;
import com.vv.finance.investment.bg.entity.southward.SouthwardCapitalStatistics;
import com.vv.finance.investment.bg.entity.southward.StockSouthwardCapitalStatistics;
import com.vv.finance.investment.bg.entity.uts.*;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: IStockMarketService
 * @Description: 股票市场数据
 * @Author: Demon
 * @Datetime: 2020/11/13   16:58
 */
public interface IStockMarketService {

    /**
     * 根据股票代码获取市场数据
     *
     * @param stockCode
     * @return
     */
    Xnhk0102 getStockMarketData(String stockCode);

    ResultT<List<Xnhks0314>> getXnhks0314List();



    ResultT<List<Object>> getXnhks0501(long time);

    ResultT<List<Object>> getXnhks0101(long time);


    ResultT<List<Xnhk0901>> getXnhk0901List(List<String> codes);

    /**
     * 获取所有的xnhk0102
     *
     * @return
     */
    ResultT<List<Xnhk0102>> getXnhk0102List();

    /**
     * 获取所有退市的股票代码
     * @return
     */
    ResultT<List<String>> getQuitCode();

    /**
     * 获取所有停牌的股票代码
     * @return
     */
    ResultT<List<String>> getCloseCode();

    /**
     * 获取所有停牌的码表代码
     * @return
     */
    ResultT<List<String>> getCloseDefineCode();

    /**
     * 获取股东增减持
     *
     * @param stockCode
     * @return
     */
    Xnhks0601 getStockholderAddOrSubtract(String stockCode);

    /**
     * 获取所有股东增减持
     *
     * @return
     */
    ResultT<List<Xnhks0601>> getXnhks0601List();

    /**
     * 获取所有股东增减持-批量code
     *
     * @return
     */
    ResultT<List<Xnhks0601>> getXnhks0601ListByCodes(Set<String> codes);

    /**
     * peg
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhk0406 getPeg(String stockCode);

    /**
     * 获取到0406List
     *
     * @return
     */
    ResultT<List<Xnhk0406>> getXnhk0406List();

    /**
     * 增长率
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhk0410 getGrowthRate(String stockCode);

    /**
     * 获取xnhk0410List
     *
     * @return
     */
    ResultT<List<Xnhk0410>> getXnhk0410List();


    /**
     * xnhks0101
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhks0101 xnhks0101(String stockCode);

    /**
     * 获取所有的xnhks0101
     *
     * @param codeList
     * @return
     */
    ResultT<List<Xnhks0101>> getXnhks0101List(List<String> codeList);

    /**
     * xnhk0201
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhk0201 getXnhk0201(String stockCode);

    /**
     * xnhk0202
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhk0202 getXnhk0202(String stockCode);

    /**
     * xnhk0203
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhk0203 getXnhk0203(String stockCode);

    /**
     * xnhk0204
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhk0204 getXnhk0204(String stockCode);

    /**
     * xnhk0205
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhk0205 getXnhk0205(String stockCode);

    /**
     * xnhk0206
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhk0206 getXnhk0206(String stockCode);

    /**
     * xnhk0207
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhk0207 getXnhk0207(String stockCode);

    /**
     * xnhk0208
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhk0208 getXnhk0208(String stockCode);

    /**
     * xnhk0209
     *
     * @param stockCode 股票代码
     * @return
     */
    Xnhk0209 getXnhk0209(String stockCode);

    /**
     * Xnhk0702
     *
     * @param warrantCode 权证代码
     * @return
     */
    Xnhk0702 getXnhk0702(String warrantCode);

    /**
     * Xnhks0701
     *
     * @param warrantCode 权证代码
     * @return
     */
    Xnhks0701 getXnhks0701(String warrantCode);


    /**
     * 股东增减持接口
     *
     * @param pageReq
     * @return
     */
    SimplePageResp<HoldStockChange> holdStockChange(SimplePageReq pageReq);

    /**
     * 估值与成长
     *
     * @param pageReq
     * @return
     */
    SimplePageResp<ValuationGrowth> valuationGrowth(SimplePageReq pageReq);

    /**
     * 代码复用场景：
     * 获取当天新股
     */
    ResultT<List<String>> getXnhks0101sToday();

    /**
     * 港股转板代码变更场景，获取指定日期发生的代码变更记录
     * @param date YYYYmmdd
     * @return key是变更前code，value是变更后code
     */
    ResultT<Map<String, String>> getStockConversionMarket(String date);

    List<String> getHkStockThroughList(String stockCode);

    void saveMarketStock(Date date);

    void saveF10Mongo(MarketPresence marketPresence);

    List<SouthwardCapitalStatistics> selectByMarket(String market, int limit);

    int saveOrUpdateBatch(List<SouthwardCapitalStatistics> list);
}
