package com.vv.finance.investment.bg.api.impl.index;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Function;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.entity.receiver.Index;
import com.vv.finance.investment.bg.api.index.IndexKlineService;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.req.IndexQueryReq;
import com.vv.finance.investment.bg.entity.index.*;
import com.vv.finance.investment.bg.mapper.index.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @ClassName: IndexKlineServiceImpl
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/28   10:22
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class IndexKlineServiceImpl extends IndexBaseServiceImpl implements IndexKlineService {

    @Resource
    private TIndexMinChartMapper minChartMapper;

    @Resource
    private TIndex1minKlineMapper index1minKlineMapper;

    @Resource
    private TIndex5minKlineMapper index5minKlineMapper;

    @Resource
    private TIndex15minKlineMapper index15minKlineMapper;

    @Resource
    private TIndex30minKlineMapper index30minKlineMapper;
    @Resource
    private TIndex60minKlineMapper index60minKlineMapper;
    @Resource
    private TIndex120minKlineMapper index120minKlineMapper;

    @Resource
    private TIndexDailyKlineMapper dailyKlineMapper;

    @Resource
    private TIndexMonthlyKlineMapper monthlyKlineMapper;

    @Resource
    private TIndexWeeklyKlineMapper weeklyKlineMapper;
    @Resource
    private RedisClient redisClient;
    @Resource
    private TIndexYearlyKlineMapper yearlyKlineMapper;


    @Override
    public ResultT<List<TIndexMinChart>> queryMinChartKline(IndexQueryReq req) {
        List<TIndexMinChart> listEntity = getPageListEntity(minChartMapper, new Page<>(1, req.getNum()), new QueryWrapper<TIndexMinChart>()
                .eq(TIndexMinChart.COL_CODE, req.getCode())
                .le(req.getDate() != null, TIndexMinChart.COL_TIME, req.getDate()).orderByDesc(TIndexMinChart.COL_TIME));
        return ResultT.success(listEntity);
    }

    @Override
    public ResultT<List<TIndex1minKline>> query1MinKline(IndexQueryReq req) {

        Page<TIndex1minKline> tIndex1minKlinePage = index1minKlineMapper.selectPage(new Page<>(1, req.getNum()), new QueryWrapper<TIndex1minKline>()
                .eq(TIndex1minKline.COL_CODE, req.getCode())
                .eq(req.getAdjhkt() != null, TIndex1minKline.COL_ADJHKT, req.getAdjhkt())
                .le(req.getDate() != null, TIndex1minKline.COL_TIME, req.getDate()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success(tIndex1minKlinePage.getRecords());
    }

    @Override
    public ResultT<List<TIndex5minKline>> query5MinKline(IndexQueryReq req) {
        List<TIndex5minKline> listEntity = getPageListEntity(index5minKlineMapper, new Page<>(1, req.getNum()), new QueryWrapper<TIndex5minKline>()
                .eq(TIndex5minKline.COL_CODE, req.getCode())
                .eq(req.getAdjhkt() != null, TIndex5minKline.COL_ADJHKT, req.getAdjhkt())
                .le(req.getDate() != null, TIndex5minKline.COL_TIME, req.getDate()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success(listEntity);
    }

    @Override
    public ResultT<List<TIndex15minKline>> query15MinKline(IndexQueryReq req) {
        List<TIndex15minKline> listEntity = getPageListEntity(index15minKlineMapper, new Page<>(1, req.getNum()), new QueryWrapper<TIndex15minKline>()
                .eq(TIndex15minKline.COL_CODE, req.getCode())
                .eq(req.getAdjhkt() != null, TIndex15minKline.COL_ADJHKT, req.getAdjhkt())
                .le(req.getDate() != null, TIndex15minKline.COL_TIME, req.getDate()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success(listEntity);
    }

    @Override
    public ResultT<List<TIndex30minKline>> query30MinKline(IndexQueryReq req) {
        List<TIndex30minKline> listEntity = getPageListEntity(index30minKlineMapper, new Page<>(1, req.getNum()), new QueryWrapper<TIndex30minKline>()
                .eq(TIndex30minKline.COL_CODE, req.getCode())
                .eq(req.getAdjhkt() != null, TIndex30minKline.COL_ADJHKT, req.getAdjhkt())
                .le(req.getDate() != null, TIndex30minKline.COL_TIME, req.getDate()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success(listEntity);
    }

    @Override
    public ResultT<List<TIndex60minKline>> query60MinKline(IndexQueryReq req) {
        List<TIndex60minKline> listEntity = getPageListEntity(index60minKlineMapper, new Page<>(1, req.getNum()), new QueryWrapper<TIndex60minKline>()
                .eq(IndexBaseMinKline.COL_CODE, req.getCode())
                .eq(req.getAdjhkt() != null, IndexBaseMinKline.COL_ADJHKT, req.getAdjhkt())
                .le(req.getDate() != null, IndexBaseMinKline.COL_TIME, req.getDate()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success(listEntity);
    }

    @Override
    public ResultT<List<TIndex120minKline>> query120MinKline(IndexQueryReq req) {
        List<TIndex120minKline> listEntity = getPageListEntity(index120minKlineMapper, new Page<>(1, req.getNum()), new QueryWrapper<TIndex120minKline>()
                .eq(IndexBaseMinKline.COL_CODE, req.getCode())
                .eq(req.getAdjhkt() != null, IndexBaseMinKline.COL_ADJHKT, req.getAdjhkt())
                .le(req.getDate() != null, IndexBaseMinKline.COL_TIME, req.getDate()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success(listEntity);
    }

    @Override
    public ResultT<List<TIndexDailyKline>> queryDailyKline(IndexQueryReq req) {
        List<TIndexDailyKline> listEntity = getPageListEntity(dailyKlineMapper, new Page<>(1, req.getNum()), new QueryWrapper<TIndexDailyKline>()
                .eq(TIndexDailyKline.COL_CODE, req.getCode())
                .le(req.getDate() != null, TIndexDailyKline.COL_TIME, req.getDate()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success(listEntity);
    }

//    @Override
//    public ResultT<List<DayHengsen>> queryDailyHengsen(Long startTime, Long endTime) {
//        StockKlineRangeReq stockKlineRangeReq = new StockKlineRangeReq();
//        stockKlineRangeReq.setEndTime(endTime);
//        stockKlineRangeReq.setStartTime(startTime);
//        stockKlineRangeReq.setType("day");
//        stockKlineRangeReq.setAdjhkt("");
//        stockKlineRangeReq.setCode("0000100");
//        List<KlineEntity> klineEntities = compositeApi.selectKlineList(stockKlineRangeReq);
//        List<DayHengsen> list = klineEntities.stream().map(klineEntity -> {
//            DayHengsen dayHengsen = new DayHengsen();
//            dayHengsen.setDateTime(klineEntity.getTime());
//            dayHengsen.setIndexRate(klineEntity.getAmount().doubleValue() / 100);
//            return dayHengsen;
//        }).collect(Collectors.toList());
//
//        Long time = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toInstant(ZoneOffset.of("+8")).toEpochMilli();
//        if (time.equals(endTime)) {
//            Index indexSnapshot = (Index) redisClient.get(RedisKeyConstants.RECEIVER_INDEX_SNAPSHOT_BEAN.concat("0000100"));
//            if (indexSnapshot != null) {
//                DayHengsen hengsen = new DayHengsen();
//                hengsen.setDateTime(time);
//                hengsen.setIndexRate(Double.parseDouble(indexSnapshot.getNetchgpredaypct()) / 100);
//                list.add(hengsen);
//            }
//        }
//
//        return ResultT.success(list.stream().filter(distinctByKey(DayHengsen::getDateTime)).collect(Collectors.toList()));
//    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Override
    public ResultT<List<TIndexWeeklyKline>> queryWeekKline(IndexQueryReq req) {
        List<TIndexWeeklyKline> listEntity = getPageListEntity(weeklyKlineMapper, new Page<>(1, req.getNum()), new QueryWrapper<TIndexWeeklyKline>()
                .eq(TIndexWeeklyKline.COL_CODE, req.getCode())
                .le(req.getDate() != null, TIndexWeeklyKline.COL_TIME, req.getDate()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success(listEntity);
    }

    @Override
    public ResultT<List<TIndexMonthlyKline>> queryMonthKline(IndexQueryReq req) {
        List<TIndexMonthlyKline> listEntity = getPageListEntity(monthlyKlineMapper, new Page<>(1, req.getNum()), new QueryWrapper<TIndexMonthlyKline>()
                .eq(TIndexMonthlyKline.COL_CODE, req.getCode())
                .le(req.getDate() != null, TIndexMonthlyKline.COL_TIME, req.getDate()).orderByDesc(IndexBaseMinKline.COL_TIME));

        return ResultT.success(listEntity);
    }

    @Override
    public ResultT<List<TIndexYearlyKline>> queryYearKline(IndexQueryReq req) {
        List<TIndexYearlyKline> listEntity = getPageListEntity(yearlyKlineMapper, new Page<>(1, req.getNum()), new QueryWrapper<TIndexYearlyKline>()
                .eq(TIndexYearlyKline.COL_CODE, req.getCode())
                .le(req.getDate() != null, TIndexYearlyKline.COL_TIME, req.getDate()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success(listEntity);
    }

    @Override
    public ResultT<Void> saveMinChartKline(TIndexMinChart minChart) {
        saveOrUpdate(minChart, minChartMapper, new UpdateWrapper<TIndexMinChart>()
                .eq(TIndexMinChart.COL_CODE, minChart.getCode())
                .eq(TIndexMinChart.COL_TIME, minChart.getTime()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success();
    }

    @Override
    public ResultT<Void> save1MinKline(TIndex1minKline kline) {
//        batchSaveOrUpdate(klines, index1minKlineMapper);
        saveOrUpdate(kline, index1minKlineMapper, new UpdateWrapper<TIndex1minKline>()
                .eq(TIndex1minKline.COL_CODE, kline.getCode())
                .eq(TIndex1minKline.COL_TIME, kline.getTime())
                .eq(TIndex1minKline.COL_ADJHKT, kline.getAdjhkt()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success();
    }

    @Override
    public ResultT<Void> save5MinKline(TIndex5minKline kline) {
        saveOrUpdate(kline, index5minKlineMapper, new UpdateWrapper<TIndex5minKline>()
                .eq(TIndex5minKline.COL_CODE, kline.getCode())
                .eq(TIndex5minKline.COL_TIME, kline.getTime())
                .eq(TIndex5minKline.COL_ADJHKT, kline.getAdjhkt()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success();
    }

    @Override
    public ResultT<Void> save15MinKline(TIndex15minKline kline) {
        saveOrUpdate(kline, index15minKlineMapper, new UpdateWrapper<TIndex15minKline>()
                .eq(TIndex15minKline.COL_CODE, kline.getCode())
                .eq(TIndex15minKline.COL_TIME, kline.getTime())
                .eq(TIndex15minKline.COL_ADJHKT, kline.getAdjhkt()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success();
    }

    @Override
    public ResultT<Void> save30MinKline(TIndex30minKline kline) {
        saveOrUpdate(kline, index30minKlineMapper, new UpdateWrapper<TIndex30minKline>()
                .eq(TIndex30minKline.COL_CODE, kline.getCode())
                .eq(TIndex30minKline.COL_TIME, kline.getTime())
                .eq(TIndex30minKline.COL_ADJHKT, kline.getAdjhkt()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success();
    }

    @Override
    public ResultT<Void> save60MinKline(TIndex60minKline kline) {
        saveOrUpdate(kline, index60minKlineMapper, new UpdateWrapper<TIndex60minKline>()
                .eq(TIndex60minKline.COL_CODE, kline.getCode())
                .eq(TIndex60minKline.COL_TIME, kline.getTime())
                .eq(TIndex60minKline.COL_ADJHKT, kline.getAdjhkt()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success();
    }

    @Override
    public ResultT<Void> save120MinKline(TIndex120minKline kline) {
        saveOrUpdate(kline, index120minKlineMapper, new UpdateWrapper<TIndex120minKline>()
                .eq(TIndex120minKline.COL_CODE, kline.getCode())
                .eq(TIndex120minKline.COL_TIME, kline.getTime())
                .eq(TIndex120minKline.COL_ADJHKT, kline.getAdjhkt()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success();
    }

    @Override
    public ResultT<Void> saveDailyKline(TIndexDailyKline kline) {
        saveOrUpdate(kline, dailyKlineMapper, new UpdateWrapper<TIndexDailyKline>()
                .eq(TIndexDailyKline.COL_CODE, kline.getCode())
                .eq(TIndexDailyKline.COL_TIME, kline.getTime()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success();
    }

    @Override
    public ResultT<Void> saveWeekKline(TIndexWeeklyKline kline) {
        saveOrUpdate(kline, weeklyKlineMapper, new UpdateWrapper<TIndexWeeklyKline>()
                .eq(TIndexWeeklyKline.COL_CODE, kline.getCode())
                .eq(TIndexWeeklyKline.COL_TIME, kline.getTime()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success();
    }

    @Override
    public ResultT<Void> saveMonthKline(TIndexMonthlyKline kline) {
        saveOrUpdate(kline, monthlyKlineMapper, new UpdateWrapper<TIndexMonthlyKline>()
                .eq(TIndexMonthlyKline.COL_CODE, kline.getCode())
                .eq(TIndexMonthlyKline.COL_TIME, kline.getTime()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success();
    }

    @Override
    public ResultT<Void> saveYearKline(TIndexYearlyKline kline) {
        saveOrUpdate(kline, yearlyKlineMapper, new UpdateWrapper<TIndexYearlyKline>()
                .eq(TIndexYearlyKline.COL_CODE, kline.getCode())
                .eq(TIndexYearlyKline.COL_TIME, kline.getTime()).orderByDesc(IndexBaseMinKline.COL_TIME));
        return ResultT.success();
    }
}
