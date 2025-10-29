//package com.vv.finance.investment.bg.api.impl.stock.cache;
//
//import com.vv.finance.common.utils.DateUtils;
//import com.vv.finance.hk.quotation.common.stock.entity.KlineEntity;
//import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
//import com.vv.finance.investment.bg.api.stock.cache.StockKlineApi;
//import com.vv.finance.investment.bg.stock.kline.entity.BaseStockKlineEntity;
//import com.vv.finance.investment.composite.api.kline.KlineDailyApi;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.dubbo.common.utils.CollectionUtils;
//import org.apache.dubbo.config.annotation.DubboReference;
//import org.apache.dubbo.config.annotation.DubboService;
//import org.springframework.beans.BeanUtils;
//
//import javax.annotation.Resource;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * @author hamilton
// * @date 2020/12/10 11:14
// */
//@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
//@Slf4j
//@RequiredArgsConstructor
//public class StockKlineApiImpl implements StockKlineApi {
//
//    @DubboReference(group = "${dubbo.investment.gateway.service.group:composite}", registry = "compositeservice", timeout = 8000)
//    private KlineDailyApi klineDailyApi;
//
//    @Resource
//    private HkTradingCalendarApi tradingCalendarApi;
//
//
//}
