package com.vv.finance.investment.bg.api.impl.stock;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.base.utils.ZoneDateUtils;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.domain.PageWithCount;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.common.StockMoveTheme;
import com.vv.finance.common.enums.StockMoveEnum;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.stock.StockMoveApi;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.move.StockMove;
import com.vv.finance.investment.bg.stock.move.mapper.MoveThemeMapper;
import com.vv.finance.investment.bg.stock.move.service.impl.MoveThemeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName StockMoveApiImpl
 * @Deacription 异动对外接口
 * @Author lh.sz
 * @Date 2021年04月29日 14:00
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
public class StockMoveApiImpl implements StockMoveApi {
    @Resource
    private StockCache stockCache;
    @Resource
    HkTradingCalendarApi tradingCalendarApi;
    @Resource
    RedisClient redisClient;
    @Resource
    private MoveThemeMapper moveThemeMapper;

    @Override
    public Boolean saveBatch(Collection<StockMove> stockMoves) {
        moveThemeMapper.saveBatch(stockMoves);
//        if (b) {
        log.info("异动数据落库成功！股票代码");
//        } else {
//            log.info("异动数据落库失败！股票代码");
//        }
        return true;
    }

    @Override
    public Page<StockMove> pageList(Page page, Set<String> codes, Integer moveType, String lastStockCode, Long lastTimeStamp) {
        String queryDate;
        Page<StockMove> result = new Page<>();
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        if (!tradingCalendarApi.isTradingDay(LocalDate.now())) {
            queryDate = tradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate().toString();
        } else if (DateUtils.beforeNineHour()) {
            queryDate = tradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate().toString();
        } else if (DateUtils.bidding()) {
            return result;
        } else {
            queryDate = DateUtils.today();
        }
        List<Object> objects = redisClient.lGet(RedisKeyConstants.MOVE_SNAPSHOT_STOCK_LIST + queryDate, 0, -1);
        if (CollectionUtils.isEmpty(objects)) {
            result.setTotal(0);
            return result;
        }
        Supplier<Stream<StockMove>> stockMoveStream = () -> objects.stream().filter(obj -> {
            StockMoveTheme stockMoveTheme = (StockMoveTheme) obj;
            return (CollectionUtils.isEmpty(codes) || codes.contains(stockMoveTheme.getCode())) && (ObjectUtils.isEmpty(moveType) || moveType.equals(stockMoveTheme.getMoveType()));
        }).map(obj -> {
            StockMoveTheme stockMoveTheme = (StockMoveTheme) obj;
            StockMove stockMove = new StockMove();
            stockMove.setCode(stockMoveTheme.getCode());
            stockMove.setName(stockMoveTheme.getName());
            stockMove.setMoveType(stockMoveTheme.getMoveType());
            stockMove.setMoveData(JSON.toJSONString(stockMoveTheme.getMoveData()));
            stockMove.setTime(stockMoveTheme.getTime());
            return stockMove;
        });
        long count = stockMoveStream.get().count();
        if (count > 0) {
            //按时间戳排序
            List<StockMove> collect =
                    stockMoveStream.get().sorted(Comparator.comparing(StockMove::getTime).reversed()).collect(Collectors.toList());
            //剔除redis后加入的元素 避免滚动排序出错
            if (StringUtils.isNotBlank(lastStockCode) && lastTimeStamp != -1) {
                int index = 0;
                for (int i = 0; i < collect.size(); i++) {
                    StockMove move = collect.get(i);
                    if (lastStockCode.equals(move.getCode()) && lastTimeStamp.equals(move.getTime())) {
                        index = i;
                        break;
                    }
                }
                collect=collect.stream().skip(index+1).collect(Collectors.toList());
            }
            result.setTotal(collect.stream().count());
            //手动分页
            List<StockMove> res = collect.stream()
                    //.skip((page.getCurrent() - 1) * page.getSize())
                    .limit(page.getSize())
                    .collect(Collectors.toList());
            result.setRecords(res);
        }
        return result;
    }

    @Override
    public PageWithCount<StockMove> pageListV2(Page page, Set<String> codes, Integer moveType, String lastStockCode, Long lastTimeStamp) {
        Page<StockMove> movePage = pageList(page, codes, moveType, lastStockCode, lastTimeStamp);
        return fillPageWithCount(movePage);
    }

    @Override
    public Page<StockMove> pageListByTypeList(Page page, Set<String> codes, Set<Integer> moveTypes, int sortType, String endTimeOrg) {
        log.info("入参codes:{},moveTypes:{},endTimeOrg:{}", codes, moveTypes, endTimeOrg);

        if(StringUtils.isEmpty(endTimeOrg)){
            LocalDateTime localTime = ZoneDateUtils.getHongKongDateTime();
            localTime = localTime.withSecond(59);
            endTimeOrg = String.valueOf(ZoneDateUtils.getUnixTimeByDate(localTime, ZoneDateUtils.Asia_Shanghai));
        }

        //查询redis入参时间需改为16位
        Long endTime = endTimeOrg.length() == 13 ? Long.valueOf(endTimeOrg + "999") : Long.valueOf(endTimeOrg);
        log.info("使用查询redis方法codes:{},endTime:{}", codes, endTime);

        Page<StockMove> result = new Page<>();
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        result.setTotal(0);
        String queryDate = getMoveQueryDate();
        if (StrUtil.equals(StrUtil.EMPTY, queryDate)) {
            return result;
        }
        List<Object> objects = redisClient.lGet(RedisKeyConstants.MOVE_SNAPSHOT_STOCK_LIST + queryDate, 0, -1);
        if (CollectionUtils.isEmpty(objects)) {
            return result;
        }
        Supplier<Stream<StockMove>> stockMoveStream = () -> objects.stream().filter(obj -> {
            StockMoveTheme stockMoveTheme = (StockMoveTheme) obj;
            return (CollectionUtils.isEmpty(codes) || codes.contains(stockMoveTheme.getCode()))
                    && (CollectionUtils.isEmpty(moveTypes) || moveTypes.contains(stockMoveTheme.getMoveType()))
                    && (Long.valueOf(StringUtils.isNotEmpty(stockMoveTheme.getSerialId()) ? stockMoveTheme.getSerialId() : "0" ) < endTime);
        }).map(obj -> {
            StockMoveTheme stockMoveTheme = (StockMoveTheme) obj;
            StockMove stockMove = new StockMove();
            stockMove.setCode(stockMoveTheme.getCode());
            stockMove.setName(stockMoveTheme.getName());
            stockMove.setMoveType(stockMoveTheme.getMoveType());
            stockMove.setMoveData(JSON.toJSONString(stockMoveTheme.getMoveData()));
            stockMove.setTime(stockMoveTheme.getTime());
            stockMove.setMoveNum(stockMoveTheme.getMoveNum());
            stockMove.setSerialId(stockMoveTheme.getSerialId());
            return stockMove;
        });
        long count = stockMoveStream.get().count();
        result.setTotal(count);
        if (count > 0) {
            List<StockMove> collect = stockMoveStream.get().sorted
                            ((o1, o2) -> sortType == 0 ? o1.getTime().compareTo(o2.getTime()) : o2.getTime().compareTo(o1.getTime())).skip((page.getCurrent() - 1) * page.getSize())
                    .limit(page.getSize()).collect(Collectors.toList());
            result.setRecords(collect);
        }
        return result;

    }

    @Override
    public PageWithCount<StockMove> pageListByTypeListV2(Page page, Set<String> codes, Set<Integer> moveTypes, int sortType, String endTimeOrg) {
        Page<StockMove> movePage = pageListByTypeList(page, codes, moveTypes, sortType, endTimeOrg);
        return fillPageWithCount(movePage);
    }

    private PageWithCount<StockMove> fillPageWithCount(Page<StockMove> movePage) {

        String queryDate = getMoveQueryDate();
        PageWithCount<StockMove> result = new PageWithCount<>();
        if (StrUtil.equals(StrUtil.EMPTY, queryDate)) {
            return result;
        }

        Integer positiveCount = redisClient.hget(RedisKeyConstants.MOVE_SNAPSHOT_STOCK_COUNT.concat(queryDate), StockMoveEnum.POSITIVE.getName());
        Integer negativeCount = redisClient.hget(RedisKeyConstants.MOVE_SNAPSHOT_STOCK_COUNT.concat(queryDate), StockMoveEnum.NEGATIVE.getName());

        BeanUtil.copyProperties(movePage, result);
        result.setPositiveCount(positiveCount);
        result.setNegativeCount(negativeCount);
        result.setTotalCount(ObjectUtil.defaultIfNull(positiveCount, 0) + ObjectUtil.defaultIfNull(negativeCount, 0));

        return result;
    }

    private String getMoveQueryDate() {
        String queryDate;
        if (!tradingCalendarApi.isTradingDay(LocalDate.now())) {
            queryDate = tradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate().toString();
        } else if (DateUtils.beforeNineHour()) {
            queryDate = tradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate().toString();
        } else if (DateUtils.bidding()) {
            return StrUtil.EMPTY;
        } else {
            queryDate = DateUtils.today();
        }
        return queryDate;
    }

    @Override
    public PageWithCount<StockMove> pageListByTypeListPc(Page page, Set<String> codes, Set<Integer> moveTypes, int sortType, String endTimeOrg, Integer pageTurnType) {
        Page<StockMove> movePage = pageListByTypeListFromRedis(page, codes, moveTypes, sortType, endTimeOrg, pageTurnType);
        return fillPageWithCount(movePage);
    }

    /**
     * 删除临时股票异动数据
     *
     * @param stockCode
     */
    @Override
    public void delStockMoveByStockCode(String stockCode) {
        try {
            Set<String> keys3 = redisClient.getRedisKeys(RedisKeyConstants.MOVE_SNAPSHOT_STOCK_LIST.concat("*"));
            for (String redisKey : keys3) {
                log.info("删除异动数据开始：stockCode：{},redis:{}", stockCode, redisKey);
                List<Object> objects = redisClient.lGet(redisKey, 0, -1);
                if (CollectionUtils.isEmpty(objects)) {
                    return;
                }
                for (Object obj : objects) {
                    StockMoveTheme stockMoveTheme = (StockMoveTheme) obj;
                    if (stockCode.equals(stockMoveTheme.getCode())) {
                        redisClient.lRemove(redisKey, 1, stockMoveTheme);
                    }
                }
            }
            log.info("删除异动数据结束");
        } catch (Exception e) {
            log.info("删除临时股票异动数据：stockCode：{} 异常", stockCode, e);
        }
    }

    /**
     * 变更异动数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    @Override
    public void updateStockMoveStockCode(String sourceCode, String targetCode) {
        try {
            //处理上一个交易日的数据
            LocalDate localDate = LocalDate.now();
            String redisKey = RedisKeyConstants.MOVE_SNAPSHOT_STOCK_LIST + localDate;
            log.info("变更异动数据开始：sourceCode：{} targetCode：{},redis:{}", sourceCode, targetCode, redisKey);
            List<Object> objects = redisClient.lGet(redisKey, 0, -1);
            if (CollectionUtils.isEmpty(objects)) {
                return;
            }
            for (Object obj : objects) {
                StockMoveTheme stockMoveTheme = (StockMoveTheme) obj;
                if (sourceCode.equals(stockMoveTheme.getCode())) {
                    redisClient.lRemove(redisKey,1, stockMoveTheme);
                    stockMoveTheme.setCode(targetCode);
                    redisClient.lSet(redisKey, stockMoveTheme);
                }
            }
            log.info("变更异动数据结束");
        } catch (Exception e) {
            log.info("变更异动数据股票code：sourceCode：{} targetCode：{} 异常", sourceCode, targetCode, e);
        }
    }

    /**
     * 变更异动数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    @Override
    public void copyStockMoveStockCode(String sourceCode, String targetCode) {
        try {
            LocalDate localDate = LocalDate.now();
            //处理上一个交易日的数据
            String redisKey = RedisKeyConstants.MOVE_SNAPSHOT_STOCK_LIST + localDate;
            log.info("变更异动数据开始：sourceCode：{} targetCode：{},redis:{}", sourceCode, targetCode, redisKey);
            List<Object> objects = redisClient.lGet(redisKey, 0, -1);
            if (CollectionUtils.isEmpty(objects)) {
                return;
            }
            for (Object obj : objects) {
                StockMoveTheme stockMoveTheme = (StockMoveTheme) obj;
                if (sourceCode.equals(stockMoveTheme.getCode())) {
                    stockMoveTheme.setCode(targetCode);
                    redisClient.lSet(redisKey, stockMoveTheme);
                }
            }
            log.info("变更异动数据结束");
        } catch (Exception e) {
            log.info("变更异动数据股票code：sourceCode：{} targetCode：{} 异常", sourceCode, targetCode, e);
        }

    }

    public Page<StockMove> pageListByTypeListFromRedis(Page page, Set<String> codes, Set<Integer> moveTypes, int sortType, String endTimeOrg, Integer pageTurnType) {
        log.info("入参codes:{},moveTypes:{},endTimeOrg:{}", codes, moveTypes, endTimeOrg);

        if(StringUtils.isEmpty(endTimeOrg)){
            LocalDateTime localTime = ZoneDateUtils.getHongKongDateTime();
            localTime = localTime.withSecond(59);
            endTimeOrg = String.valueOf(ZoneDateUtils.getUnixTimeByDate(localTime, ZoneDateUtils.Asia_Shanghai));
        }
        Long endTime;
        if (endTimeOrg.length() == 13) {
            if (pageTurnType == null || pageTurnType == 1) {
                endTime = Long.valueOf(endTimeOrg.substring(0, 10) + "999999");
            } else {
                endTime = Long.valueOf(endTimeOrg + "999");
            }
        } else {
            endTime = Long.valueOf(endTimeOrg);
        }


        //查询redis入参时间需改为16位
//        Long endTime = endTimeOrg.length() == 13 ? Long.valueOf(endTimeOrg + "999") : Long.valueOf(endTimeOrg);
        log.info("使用查询redis方法codes:{},endTime:{}", codes, endTime);

        String queryDate;
        Page<StockMove> result = new Page<>();
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        result.setTotal(0);
        if (!tradingCalendarApi.isTradingDay(LocalDate.now())) {
            queryDate = tradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate().toString();
        } else if (DateUtils.beforeNineHour()) {
            queryDate = tradingCalendarApi.getBeforeTradingCalendar(LocalDate.now()).getDate().toString();
        } else if (DateUtils.bidding()) {
            return result;
        } else {
            queryDate = DateUtils.today();
        }
        List<Object> objects = redisClient.lGet(RedisKeyConstants.MOVE_SNAPSHOT_STOCK_LIST + queryDate, 0, -1);
        if (CollectionUtils.isEmpty(objects)) {
            return result;
        }
        Map<String, ComStockSimpleDto> comStockSimpleDtoMap = stockCache.queryStockInfoMap(null);
        if (pageTurnType == null || pageTurnType == 1) {
            Supplier<Stream<StockMove>> stockMoveStream = () -> objects.stream().filter(obj -> {
                StockMoveTheme stockMoveTheme = (StockMoveTheme) obj;
                return (CollectionUtils.isEmpty(codes) || codes.contains(stockMoveTheme.getCode()))
                        && (CollectionUtils.isEmpty(moveTypes) || moveTypes.contains(stockMoveTheme.getMoveType()))
                        && (Long.valueOf(StringUtils.isNotEmpty(stockMoveTheme.getSerialId()) ? stockMoveTheme.getSerialId() : "0" ) < endTime);
            }).map(obj -> {
                StockMoveTheme stockMoveTheme = (StockMoveTheme) obj;
                ComStockSimpleDto comStockSimpleDto = new ComStockSimpleDto();
                if(ObjectUtils.isNotEmpty(comStockSimpleDtoMap.get(stockMoveTheme.getCode()))){
                    comStockSimpleDto = comStockSimpleDtoMap.get(stockMoveTheme.getCode());
                }
                StockMove stockMove = new StockMove();
                stockMove.setCode(stockMoveTheme.getCode());
                stockMove.setName(comStockSimpleDto.getStockName());
                stockMove.setMoveType(stockMoveTheme.getMoveType());
                stockMove.setMoveData(JSON.toJSONString(stockMoveTheme.getMoveData()));
                stockMove.setTime(stockMoveTheme.getTime());
                stockMove.setMoveNum(stockMoveTheme.getMoveNum());
                stockMove.setSerialId(stockMoveTheme.getSerialId());
                stockMove.setStockId(comStockSimpleDto.getStockId());
                return stockMove;
            });
            long count = stockMoveStream.get().count();
            result.setTotal(count);
            if (count > 0) {
                List<StockMove> collect = stockMoveStream.get().sorted
                                ((o1, o2) -> sortType == 0 ? o1.getSerialId().compareTo(o2.getSerialId()) : o2.getSerialId().compareTo(o1.getSerialId())).skip((page.getCurrent() - 1) * page.getSize())
                        .limit(page.getSize()).collect(Collectors.toList());
                result.setRecords(collect);
            }
        }else{
            Supplier<Stream<StockMove>> stockMoveStream = () -> objects.stream().filter(obj -> {
                StockMoveTheme stockMoveTheme = (StockMoveTheme) obj;
                return (CollectionUtils.isEmpty(codes) || codes.contains(stockMoveTheme.getCode()))
                        && (CollectionUtils.isEmpty(moveTypes) || moveTypes.contains(stockMoveTheme.getMoveType()))
                        && (Long.valueOf(StringUtils.isNotEmpty(stockMoveTheme.getSerialId()) ? stockMoveTheme.getSerialId() : "0" ) > endTime);
            }).map(obj -> {
                StockMoveTheme stockMoveTheme = (StockMoveTheme) obj;
                ComStockSimpleDto comStockSimpleDto = new ComStockSimpleDto();
                if(ObjectUtils.isNotEmpty(comStockSimpleDtoMap.get(stockMoveTheme.getCode()))){
                    comStockSimpleDto = comStockSimpleDtoMap.get(stockMoveTheme.getCode());
                }
                StockMove stockMove = new StockMove();
                stockMove.setCode(stockMoveTheme.getCode());
                stockMove.setName(comStockSimpleDto.getStockName());
                stockMove.setMoveType(stockMoveTheme.getMoveType());
                stockMove.setMoveData(JSON.toJSONString(stockMoveTheme.getMoveData()));
                stockMove.setTime(stockMoveTheme.getTime());
                stockMove.setMoveNum(stockMoveTheme.getMoveNum());
                stockMove.setSerialId(stockMoveTheme.getSerialId());
                stockMove.setStockId(comStockSimpleDto.getStockId());
                return stockMove;
            });
            long count = stockMoveStream.get().count();
            result.setTotal(count);
            if (count > 0) {

                List<StockMove> collect = stockMoveStream.get().sorted(Comparator.comparing(StockMove::getSerialId)).
                        skip((page.getCurrent() - 1) * page.getSize())
                        .limit(page.getSize()).collect(Collectors.toList());

//                List<StockMove> collect = stockMoveStream.get().sorted
//                                (  (o1, o2) -> sortType == 0 ? o1.getSerialId().compareTo(o2.getSerialId()) : o2.getSerialId().compareTo(o1.getSerialId())).
//                        skip((page.getCurrent() - 1) * page.getSize())
//                        .limit(page.getSize()).collect(Collectors.toList());
                result.setRecords(collect.stream().sorted(Comparator.comparing(StockMove::getSerialId).reversed()).collect(Collectors.toList()));
            }
        }
        return result;

    }
}
