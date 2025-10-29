package com.vv.finance.investment.bg.api.index;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.dto.req.IndexQueryReq;
import com.vv.finance.investment.bg.entity.index.*;

import java.util.List;

/**
 * @author chenyu
 * @date 2020/10/27 16:02
 */
public interface IndexKlineService {


    /**
     * 获取分时图
     * @param req
     * @return
     */
    @Deprecated
    ResultT<List<TIndexMinChart>> queryMinChartKline(IndexQueryReq req);

    /**
     * 获取1分K
     * @param req
     * @return
     */
    @Deprecated
    ResultT<List<TIndex1minKline>> query1MinKline(IndexQueryReq req);

    /**
     * 获取5分K
     * @param req
     * @return
     */
    @Deprecated
    ResultT<List<TIndex5minKline>> query5MinKline(IndexQueryReq req);

    /**
     * 获取15分K
     * @param req
     * @return
     */
    @Deprecated
    ResultT<List<TIndex15minKline>> query15MinKline(IndexQueryReq req);

    /**
     * 获取30分K
     * @param req
     * @return
     */
    @Deprecated
    ResultT<List<TIndex30minKline>> query30MinKline(IndexQueryReq req);

    /**
     * 获取15分K
     * @param req
     * @return
     */
    @Deprecated
    ResultT<List<TIndex60minKline>> query60MinKline(IndexQueryReq req);

    /**
     * 获取30分K
     * @param req
     * @return
     */
    @Deprecated
    ResultT<List<TIndex120minKline>> query120MinKline(IndexQueryReq req);

    /**
     * 获取日K
     * @param req
     * @return
     */
    @Deprecated
    ResultT<List<TIndexDailyKline>> queryDailyKline(IndexQueryReq req);
    /**
     * 获取日恒生指数
     * @param startTime
     * @param endTime
     * @return
     */
//    @Deprecated
//    ResultT<List<DayHengsen>> queryDailyHengsen(Long startTime,Long endTime);

    /**
     * 获取周K
     * @param req
     * @return
     */
    @Deprecated
    ResultT<List<TIndexWeeklyKline>> queryWeekKline(IndexQueryReq req);

    /**
     * 获取月K
     * @param req
     * @return
     */
    @Deprecated
    ResultT<List<TIndexMonthlyKline>> queryMonthKline(IndexQueryReq req);

    /**
     * 获取年K
     * @param req
     * @return
     */
    @Deprecated
    ResultT<List<TIndexYearlyKline>> queryYearKline(IndexQueryReq req);


    /**
     * 分时图
     * @param minChart
     * @return
     */
    @Deprecated
    ResultT<Void> saveMinChartKline(TIndexMinChart minChart);

    /**
     * 1分K
     * @param kline
     * @return
     */
    @Deprecated
    ResultT<Void> save1MinKline(TIndex1minKline kline);

    /**
     * 5分K
     * @param kline
     * @return
     */
    @Deprecated
    ResultT<Void> save5MinKline(TIndex5minKline kline);

    /**
     * 15分K
     * @param kline
     * @return
     */
    @Deprecated
    ResultT<Void> save15MinKline(TIndex15minKline kline);

    /**
     * 30分K
     * @param kline
     * @return
     */
    @Deprecated
    ResultT<Void> save30MinKline(TIndex30minKline kline);
    /**
     * 60分K
     * @param kline
     * @return
     */
    @Deprecated
    ResultT<Void> save60MinKline(TIndex60minKline kline);
    /**
     * 30分K
     * @param kline
     * @return
     */
    @Deprecated
    ResultT<Void> save120MinKline(TIndex120minKline kline);

    /**
     * 日K
     * @param kline
     * @return
     */
    @Deprecated
    ResultT<Void> saveDailyKline(TIndexDailyKline kline);

    /**
     * 周K
     * @param kline
     * @return
     */
    @Deprecated
    ResultT<Void> saveWeekKline(TIndexWeeklyKline kline);

    /**
     * 月K
     * @param kline
     * @return
     */
    @Deprecated
    ResultT<Void> saveMonthKline(TIndexMonthlyKline kline);

    /**
     * 年K
     * @param kline
     * @return
     */
    @Deprecated
    ResultT<Void> saveYearKline(TIndexYearlyKline kline);



}
