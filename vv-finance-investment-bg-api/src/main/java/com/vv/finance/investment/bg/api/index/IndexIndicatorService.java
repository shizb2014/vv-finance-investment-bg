//package com.vv.finance.investment.bg.api.index;
//
//import com.vv.finance.base.dto.ResultT;
//import com.vv.finance.investment.bg.dto.req.IndexQueryReq;
//import com.vv.finance.investment.bg.entity.index.TIndexDailyIndicator;
//import com.vv.finance.investment.bg.entity.index.TIndexMonthlyIndicator;
//import com.vv.finance.investment.bg.entity.index.TIndexWeeklyIndicator;
//import com.vv.finance.investment.bg.entity.index.TIndexYearlyIndicator;
//
//import java.util.List;
//
///**
// * @author demon
// * @date 2020/10/27 11:48
// */
//public interface IndexIndicatorService {
//
//    /**
//     * 获取日指标
//     * @param req
//     * @return
//     */
//    @Deprecated
//    ResultT<List<TIndexDailyIndicator>> queryDailyIndicator(IndexQueryReq req);
//
//    /**
//     * 周指标
//     * @param req
//     * @return
//     */
//    @Deprecated
//    ResultT<List<TIndexWeeklyIndicator>> queryWeeklyIndicator(IndexQueryReq req);
//
//
//    /**
//     * 月指标
//     * @param req
//     * @return
//     */
//    @Deprecated
//    ResultT<List<TIndexMonthlyIndicator>> queryMonthlyIndicator(IndexQueryReq req);
//
//
//    /**
//     * 年指标
//     * @param req
//     * @return
//     */
//    @Deprecated
//    ResultT<List<TIndexYearlyIndicator>> queryYearlyIndicator(IndexQueryReq req);
//
//
//
//
//    /**
//     * 日指标
//     * @param indicator
//     * @return
//     */
//    @Deprecated
//    ResultT<Void> saveDailyIndicator(TIndexDailyIndicator indicator);
//
//    /**
//     * 周指标
//     * @param indicator
//     * @return
//     */
//    @Deprecated
//    ResultT<Void> saveWeeklyIndicator(TIndexWeeklyIndicator indicator);
//
//
//    /**
//     * 月指标
//     * @param indicator
//     * @return
//     */
//    @Deprecated
//    ResultT<Void> saveMonthlyIndicator(TIndexMonthlyIndicator indicator);
//
//
//    /**
//     * 年指标
//     * @param indicator
//     * @return
//     */
//    @Deprecated
//    ResultT<Void> saveYearlyIndicator(TIndexYearlyIndicator indicator);
//
//
//}
