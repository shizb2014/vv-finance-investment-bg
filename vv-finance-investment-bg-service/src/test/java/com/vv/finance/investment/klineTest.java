package com.vv.finance.investment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.common.bean.SimplePageResp;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.investment.bg.BGServiceApplication;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.f10.F10StockInformationApi;
import com.vv.finance.investment.bg.api.frontend.IStockKlineService;
import com.vv.finance.investment.bg.api.impl.f10.F10TableTemplateApiImpl;
import com.vv.finance.investment.bg.api.impl.stock.StockMoveApiImpl;
import com.vv.finance.investment.bg.api.uts.IStockMarketService;
import com.vv.finance.investment.bg.api.uts.TrendsService;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.f10.DirectorManager;
import com.vv.finance.investment.bg.dto.f10.F10PageBaseReq;
import com.vv.finance.investment.bg.dto.f10.SecuritiesInformation;
import com.vv.finance.investment.bg.dto.kline.BaseKlineDTO;
import com.vv.finance.investment.bg.dto.req.RtKlineReq;
import com.vv.finance.investment.bg.dto.uts.resp.StockUtsNoticeListResp;
import com.vv.finance.investment.bg.dto.uts.resp.ValuationGrowth;
import com.vv.finance.investment.bg.entity.f10.F10CommonRequest;
import com.vv.finance.investment.bg.entity.f10.F10PageReq;
import com.vv.finance.investment.bg.entity.f10.F10TableTemplate;
import com.vv.finance.investment.bg.entity.f10.RatingsTableEntity;
import com.vv.finance.investment.bg.entity.f10.f10Profit.F10ProfitEntity;
import com.vv.finance.investment.bg.entity.f10.fintable.F10CommonFinTable;
import com.vv.finance.investment.bg.entity.move.StockMove;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.entity.uts.Xnhk0205;
import com.vv.finance.investment.bg.mapper.uts.StopAndResumeMapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0205Mapper;
import com.vv.finance.investment.bg.mongo.dao.F10CashFlowDao;
import com.vv.finance.investment.bg.mongo.dao.F10KeyFiguresDao;
import com.vv.finance.investment.bg.mongo.model.F10CashFlowEntity;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10ChartServiceImpl;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10SourceServiceImpl;
import com.vv.finance.investment.gateway.api.broker.BrokerInfoServiceApi;
import com.vv.finance.investment.gateway.dto.req.BrokerStatisticsParams;
import com.vv.finance.investment.gateway.dto.resp.broker.BrokerDetailsRes;
import com.vv.finance.investment.gateway.dto.resp.broker.BrokerStatisticsRes;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author hamilton
 * @date 2020/10/30 14:27
 */
@Slf4j
@SpringBootTest(classes = BGServiceApplication.class)
public class klineTest {

    @Autowired
    private IStockKlineService iStockKlineService;

    @Test
    public void queryTimeChart() {
        RtKlineReq req = new RtKlineReq();
        req.setType("fiveDay");
        req.setCode("00700.hk");
//        List<BaseKlineDTO> baseKlineDTOS = iStockKlineService.queryTimeChart(req);
    }
}
