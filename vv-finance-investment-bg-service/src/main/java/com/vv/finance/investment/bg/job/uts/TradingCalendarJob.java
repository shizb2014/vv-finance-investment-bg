package com.vv.finance.investment.bg.job.uts;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.uts.HkexTd;
import com.vv.finance.investment.bg.mapper.uts.HkexTdMapper;
import com.vv.finance.investment.bg.stock.info.TradeCalendar;
import com.vv.finance.investment.bg.stock.trade.mapper.TradeCalendarMapper;
import com.vv.finance.investment.bg.utils.DateUtils;
import com.vv.finance.investment.gateway.api.stock.IStockBusinessApi;
import com.vv.finance.investment.gateway.dto.req.HkTradingSessionStatusReq;
import com.vv.finance.investment.gateway.dto.resp.HkTradingSessionStatusResp;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@Slf4j
public class TradingCalendarJob {

    @Resource
    TradeCalendarMapper tradeCalendarMapper;

    @Resource
    HkexTdMapper hkexTdMapper;

    @Resource
    RedisClient redisClient;

    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    IStockBusinessApi iStockBusinessApi;

    //上午延迟开市交易状态
    public final static String TRADING_CLOSE_AM = "10-102";
    //下午延迟开市交易状态
    public final static String TRADING_CLOSE_PM = "100-0";

    @XxlJob(value = "syncTradingCalendar", cron = "0 0 2 ? * 2,3,4,5,6 *", author = "王彦", desc = "同步交易日历")
    public ReturnT<String> syncTradingCalendar(String param) {
        TradeCalendar tradeCalendar = tradeCalendarMapper.selectOne(new QueryWrapper<TradeCalendar>().orderByDesc("RDATE").last("limit 1"));
        Long lastDay = tradeCalendar == null ? null : tradeCalendar.getRdate();
        List<HkexTd> hkexTds = hkexTdMapper.selectList(new QueryWrapper<HkexTd>().gt(lastDay != null,"RDATE", lastDay).orderByAsc("RDATE"));
        if(CollUtil.isNotEmpty(hkexTds)){
            log.info("更新交易日历，开始日期:{},结束日期:{}",hkexTds.get(0).getRdate(),hkexTds.get(hkexTds.size()-1).getRdate());
            List<TradeCalendar> tradeCalendars = BeanUtil.copyToList(hkexTds, TradeCalendar.class);
            for (TradeCalendar calandar : tradeCalendars) {
                tradeCalendarMapper.insert(calandar);
            }
        }else {
            log.info("交易日历增量为0，无需更新");
        }
        return ReturnT.SUCCESS;
    }

    @XxlJob(value = "updateTradingStatus", cron = "0 30 7,8,12,13 ? * 2,3,4,5,6 *", author = "王彦", desc = "更新当日交易状态")
    public ReturnT<String> updateTradingStatus(String param) {
        HkTradingSessionStatusReq req  = new HkTradingSessionStatusReq();
        req.setMarket("MAIN");
        req.setTimeMode(0);
        ResultT<List<HkTradingSessionStatusResp>> tradingSessionStatus = null;
        try {
            tradingSessionStatus = iStockBusinessApi.getTradingSessionStatus(req);
        }catch (Exception e){
            log.error("sdk获取交易状态失败,异常:",e);
            return ReturnT.FAIL;
        }
        if(tradingSessionStatus.getCode() == 200){
            //sdk获取交易状态成功，拼接状态后存到redis
            List<HkTradingSessionStatusResp> data = tradingSessionStatus.getData();
            HkTradingSessionStatusResp resp = data.get(0);
            String status = new StringBuilder().append(resp.getStatus()).append("-").append(resp.getSubId()).toString();
            log.info("job获取当日交易状态为：{}", JSON.toJSONString(resp));
            if(LocalTime.now().isBefore(LocalTime.of(11,59,59))){
                redisClient.set(RedisKeyConstants.HK_TRADING_STATUS_AM,status,DateUtils.nowToDayFinish());
            }else {
                redisClient.set(RedisKeyConstants.HK_TRADING_STATUS_PM,status,DateUtils.nowToDayFinish());
            }
        }else {
            log.error("当日交易状态接口请求错误!");
            return ReturnT.FAIL;
        }
        return ReturnT.SUCCESS;
    }

    @XxlJob(value = "modifyCalendarForClose", cron = "0 10 16 ? * 2,3,4,5,6 *", author = "王彦", desc = "全天休市修改交易日历")
    public ReturnT<String> modifyCalendarForClose(String param) {
        //从redis获取上午交易状态
        String statusAM = redisClient.get(RedisKeyConstants.HK_TRADING_STATUS_AM);
        if(StringUtils.isNotEmpty(statusAM) && statusAM.equals(TRADING_CLOSE_AM)){
            //获取下午交易状态
            String statusPM = redisClient.get(RedisKeyConstants.HK_TRADING_STATUS_PM);
            if(StringUtils.isNotEmpty(statusPM) && statusPM.equals(TRADING_CLOSE_PM)){
                Long rdate = DateUtils.localDateToF001D(LocalDate.now());
                TradeCalendar modifyDay = tradeCalendarMapper.selectOne(new QueryWrapper<TradeCalendar>().eq("RDATE", rdate));
                modifyDay.setIstrade("N");
                tradeCalendarMapper.update(modifyDay,new QueryWrapper<TradeCalendar>().eq("RDATE", rdate));
                log.info("全天休市,修改当天为非交易日,日期:{}",rdate);
            }
        }
        return ReturnT.SUCCESS;
    }

    @XxlJob(value = "getTradingStatus", author = "王彦", desc = "获取当日交易状态")
    public ReturnT<String> getTradingStatus(String param) {
        String tradingStatusAM = redisClient.get(RedisKeyConstants.HK_TRADING_STATUS_AM);
        String tradingStatusPM = redisClient.get(RedisKeyConstants.HK_TRADING_STATUS_PM);
        log.info("当日交易状态,上午:{},下午:{}",tradingStatusAM,tradingStatusPM);
        return new ReturnT(tradingStatusAM + "," + tradingStatusPM);
    }

    @XxlJob(value = "setTradingStatus", author = "王彦", desc = "修改当日交易状态")
    public ReturnT<String> setTradingStatus(String param) {
        if(StringUtils.isEmpty(param)){
            param = "10-102,100-0";
        }
        String[] split = param.split(",");
        redisClient.set(RedisKeyConstants.HK_TRADING_STATUS_AM,split[0]);
        log.info("手动修改当日交易状态,上午:{}",split[0]);
        if(split.length == 2){
            redisClient.set(RedisKeyConstants.HK_TRADING_STATUS_PM,split[1]);
            log.info("手动修改当日交易状态,下午:{}",split[1]);
        }
        return ReturnT.SUCCESS;
    }
}
