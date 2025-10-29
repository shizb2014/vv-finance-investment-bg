//package com.vv.finance.investment.bg.api.impl.index;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.vv.finance.base.dto.ResultT;
//import com.vv.finance.investment.bg.api.index.IndexIndicatorService;
//import com.vv.finance.investment.bg.dto.req.IndexQueryReq;
//import com.vv.finance.investment.bg.entity.index.TIndexDailyIndicator;
//import com.vv.finance.investment.bg.entity.index.TIndexMonthlyIndicator;
//import com.vv.finance.investment.bg.entity.index.TIndexWeeklyIndicator;
//import com.vv.finance.investment.bg.entity.index.TIndexYearlyIndicator;
//import com.vv.finance.investment.bg.mapper.index.TIndexDailyIndicatorMapper;
//import com.vv.finance.investment.bg.mapper.index.TIndexMonthlyIndicatorMapper;
//import com.vv.finance.investment.bg.mapper.index.TIndexWeeklyIndicatorMapper;
//import com.vv.finance.investment.bg.mapper.index.TIndexYearlyIndicatorMapper;
//import org.apache.dubbo.config.annotation.DubboService;
//
//import javax.annotation.Resource;
//import java.util.List;
//
///**
// * @ClassName: IndexIndicatorServiceImpl
// * @Description:
// * @Author: Demon
// * @Datetime: 2020/10/28   17:53
// */
//@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
//public class IndexIndicatorServiceImpl extends IndexBaseServiceImpl implements IndexIndicatorService {
//
//    @Resource
//    private TIndexDailyIndicatorMapper dailyIndicatorMapper;
//
//    @Resource
//    private TIndexWeeklyIndicatorMapper weeklyIndicatorMapper;
//
//    @Resource
//    private TIndexMonthlyIndicatorMapper monthlyIndicatorMapper;
//
//    @Resource
//    private TIndexYearlyIndicatorMapper yearlyIndicatorMapper;
//
//    @Override
//    public ResultT<List<TIndexDailyIndicator>> queryDailyIndicator(IndexQueryReq req) {
//        List<TIndexDailyIndicator> listEntity = getPageListEntity(dailyIndicatorMapper,new Page<>(1, req.getNum()), new QueryWrapper<TIndexDailyIndicator>()
//                .eq(TIndexDailyIndicator.COL_CODE, req.getCode())
//                .le(req.getDate()!= null, TIndexDailyIndicator.COL_DATE, req.getDate()).orderByDesc(TIndexDailyIndicator.COL_DATE));
//        return ResultT.success(listEntity);
//    }
//
//    @Override
//    public ResultT<List<TIndexWeeklyIndicator>> queryWeeklyIndicator(IndexQueryReq req) {
//        List<TIndexWeeklyIndicator> listEntity = getPageListEntity(weeklyIndicatorMapper,new Page<>(1, req.getNum()), new QueryWrapper<TIndexWeeklyIndicator>()
//                .eq(TIndexWeeklyIndicator.COL_CODE, req.getCode())
//                .le(req.getDate()!= null, TIndexWeeklyIndicator.COL_DATE, req.getDate()).orderByDesc(TIndexWeeklyIndicator.COL_DATE));
//        return ResultT.success(listEntity);
//    }
//
//    @Override
//    public ResultT<List<TIndexMonthlyIndicator>> queryMonthlyIndicator(IndexQueryReq req) {
//        List<TIndexMonthlyIndicator> listEntity = getPageListEntity(monthlyIndicatorMapper,new Page<>(1, req.getNum()), new QueryWrapper<TIndexMonthlyIndicator>()
//                .eq(TIndexMonthlyIndicator.COL_CODE, req.getCode())
//                .le(req.getDate()!= null, TIndexMonthlyIndicator.COL_DATE, req.getDate()).orderByDesc(TIndexMonthlyIndicator.COL_DATE));
//        return ResultT.success(listEntity);
//    }
//
//    @Override
//    public ResultT<List<TIndexYearlyIndicator>> queryYearlyIndicator(IndexQueryReq req) {
//        List<TIndexYearlyIndicator> listEntity = getPageListEntity(yearlyIndicatorMapper,new Page<>(1, req.getNum()), new QueryWrapper<TIndexYearlyIndicator>()
//                .eq(TIndexYearlyIndicator.COL_CODE, req.getCode())
//                .le(req.getDate()!= null, TIndexYearlyIndicator.COL_DATE, req.getDate()).orderByDesc(TIndexYearlyIndicator.COL_DATE));
//        return ResultT.success(listEntity);
//    }
//
//    @Override
//    public ResultT<Void> saveDailyIndicator(TIndexDailyIndicator indicator) {
//        saveOrUpdate(indicator, dailyIndicatorMapper, new UpdateWrapper<TIndexDailyIndicator>()
//                .eq(TIndexDailyIndicator.COL_CODE, indicator.getCode())
//                .eq(TIndexDailyIndicator.COL_DATE, indicator.getDate()).orderByDesc(TIndexDailyIndicator.COL_DATE));
//        return ResultT.success();
//    }
//
//    @Override
//    public ResultT<Void> saveWeeklyIndicator(TIndexWeeklyIndicator indicator) {
//        saveOrUpdate(indicator, weeklyIndicatorMapper, new UpdateWrapper<TIndexWeeklyIndicator>()
//                .eq(TIndexWeeklyIndicator.COL_CODE, indicator.getCode())
//                .eq(TIndexWeeklyIndicator.COL_DATE, indicator.getDate()).orderByDesc(TIndexWeeklyIndicator.COL_DATE));
//        return ResultT.success();
//    }
//
//    @Override
//    public ResultT<Void> saveMonthlyIndicator(TIndexMonthlyIndicator indicator) {
//        saveOrUpdate(indicator, monthlyIndicatorMapper, new UpdateWrapper<TIndexMonthlyIndicator>()
//                .eq(TIndexMonthlyIndicator.COL_CODE, indicator.getCode())
//                .eq(TIndexMonthlyIndicator.COL_DATE, indicator.getDate()).orderByDesc(TIndexMonthlyIndicator.COL_DATE));
//        return ResultT.success();
//    }
//
//    @Override
//    public ResultT<Void> saveYearlyIndicator(TIndexYearlyIndicator indicator) {
//        saveOrUpdate(indicator, yearlyIndicatorMapper, new UpdateWrapper<TIndexYearlyIndicator>()
//                .eq(TIndexYearlyIndicator.COL_CODE, indicator.getCode())
//                .eq(TIndexYearlyIndicator.COL_DATE, indicator.getDate()).orderByDesc(TIndexYearlyIndicator.COL_DATE));
//        return ResultT.success();
//    }
//}
