package com.vv.finance.investment.bg.job.uts;

/**
 * @Author: szb
 * @CreateTime: 2024-09-18
 * @Description:
 * @Version: 1.0
 */

import cn.hutool.core.date.DateUtil;

import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.api.uts.Xnhks0112Service;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.entity.uts.Xnhks0112;
import com.vv.finance.investment.message.api.DingDingMessageApi;
import com.vv.finance.investment.message.dto.DingDingMessageDTO;
import com.vv.finance.investment.message.enums.DingDingServerEnum;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 1、检查 create_date 或 modified_date 近五个交易日(包含当天)的数据是否一致。（两个表查询出来数据量一致，通过条件join数量也相同）
 * 2、通过127表对比112s表， XNHKS0112缺少“SECCODE”除权日为年/月/日的数据
 * 3、通过112s表对比127表 告警文案：XNHK0127缺少“SECCODE”除权日为年/月/日的数据
 * 4、F002V包含CD或SD时，F005N>0，若小于等于0，则需告警
 * 5、检查 XNHK0127表F003D为空的数据需告警
 * 6、重复性，XNHK0127表
 */
@Component
@Slf4j
public class DataMonitorJob {

    @Resource
    HkTradingCalendarApi tradingCalendarApi;

    @Resource
    UtsInfoService utsInfoService;

    @Resource
    Xnhks0112Service xnhks0112Service;

    @DubboReference(group = "${dubbo.investment.message.service.group:message}", registry = "messageservice")
    private DingDingMessageApi dingDingMessageApi;

    @Resource
    StockInfoApi stockInfoApi;

    @XxlJob(value = "dataMonitor", author = "szb", desc = "除权数据监控", cron = "0 30 16 ? * *")
    public ReturnT<String> dataMonitor(String param) {
        //获取包含当前日期的5个交易日
        Date endDate = DateUtil.date(LocalDate.now().plusDays(1));
        Date startDate = endDate;
        DateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        Long rdate = tradingCalendarApi.queryBeginTradingCalendars(5).getRdate();
        try {
            startDate = fmt.parse(String.valueOf(rdate));
        } catch (Exception e) {
            log.error("格式化日期报错:{}", rdate);
        }
        List<Object> codes = stockInfoApi.allStockCodes();


        List<String> messags = new ArrayList<>();
        String message = "";
        log.info("检查开始日期:{}，结束日期:{}", startDate, endDate);
        // 1、通过127表对比112s表， XNHKS0112缺少;   XNHKS0112缺少“00383.hk”除权日为2024/09/13的数据 包含SS或SC时
        List<Xnhk0127> xnhk0127NotIncludeSS = utsInfoService.getXnhk0127NotIncludeSS(startDate, endDate);
        if(!CollectionUtils.isEmpty(xnhk0127NotIncludeSS)){
            for(Xnhk0127 xnhk0127 : xnhk0127NotIncludeSS){
                if(ObjectUtils.isEmpty(xnhk0127.getF003d()) || !codes.contains(xnhk0127.getSeccode())){
                    log.info("xnhk0127表 F003d字段为空，code:{}", xnhk0127.getSeccode());
                    continue;
                }
                message = "XNHKS0112缺少“".concat(xnhk0127.getSeccode()).concat("”除权日为").concat(String.valueOf(xnhk0127.getF003d()).replace("-", "/")).concat("的数据");
                messags.add(message);
            }
        }

        // 1、通过127表对比112s表， XNHKS0112缺少;   XNHKS0112缺少“00383.hk”除权日为2024/09/13的数据 不包含SS或SC时
        List<Xnhk0127> xnhk0127IncludeSS = utsInfoService.getXnhk0127IncludeSS(startDate, endDate);
        if(!CollectionUtils.isEmpty(xnhk0127IncludeSS)){
            for(Xnhk0127 xnhk0127 : xnhk0127IncludeSS){
                if(ObjectUtils.isEmpty(xnhk0127.getF003d()) || !codes.contains(xnhk0127.getSeccode())){
                    log.info("xnhk0127表 F003d字段为空，code:{}", xnhk0127.getSeccode());
                    continue;
                }
                message = "XNHKS0112缺少“".concat(xnhk0127.getSeccode()).concat("”除权日为").concat(String.valueOf(xnhk0127.getF003d()).replace("-", "/")).concat("的数据");
                messags.add(message);
            }
        }

        // 2、通过112s表对比127表，Xnhk0127表缺少;   XNHK0127缺少“00383.hk”除权日为2024/09/13的数据
        List<Xnhks0112> xnhk0112sIncludeSS = xnhks0112Service.getXnhk0112sIncludeSS(startDate, endDate);
        if(!CollectionUtils.isEmpty(xnhk0112sIncludeSS)){
            for(Xnhks0112 xnhks0112 : xnhk0112sIncludeSS){
                if(ObjectUtils.isEmpty(xnhks0112.getF017d()) || !codes.contains(xnhks0112.getSeccode())){
                    log.info("xnhks0112表 F0017d字段为空，code:{}", xnhks0112.getSeccode());
                    continue;
                }
                String date = LocalDate.parse(xnhks0112.getF017d().toString(), DateTimeFormatter.BASIC_ISO_DATE).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                message = "XNHK0127缺少“".concat(xnhks0112.getSeccode()).concat("”除权日为").concat(date).concat("的数据");
                messags.add(message);
            }
        }

        List<Xnhks0112> xnhk0112sNotIncludeSS = xnhks0112Service.getXnhk0112sNotIncludeSS(startDate, endDate);
        if(!CollectionUtils.isEmpty(xnhk0112sNotIncludeSS)){
            for(Xnhks0112 xnhks0112 : xnhk0112sNotIncludeSS){
                if(ObjectUtils.isEmpty(xnhks0112.getF016d()) || !codes.contains(xnhks0112.getSeccode())){
                    log.info("xnhks0112表 F0016d字段为空，code:{}", xnhks0112.getSeccode());
                    continue;
                }
                String date = LocalDate.parse(xnhks0112.getF016d().toString(), DateTimeFormatter.BASIC_ISO_DATE).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                message = "XNHK0127缺少“".concat(xnhks0112.getSeccode()).concat("”除权日为").concat(date).concat("的数据");
                messags.add(message);
            }
        }

        List<Xnhk0127> xnhk0127Data = utsInfoService.getXnhk0127sByDate(startDate, endDate);
        for(Xnhk0127 xnhk0127 :  xnhk0127Data){
            // 检查 XNHK0127表F003D为空的数据
            if(ObjectUtils.isEmpty(xnhk0127.getF003d()) && codes.contains(xnhk0127.getSeccode())){
                message = "XNHK0127“".concat(xnhk0127.getSeccode()).concat("”的除权日为空");
                messags.add(message);
            }
            // F002V包含CD或SD时，F005N>0，若小于等于0 00383.hk”除权日为2024/09/13的F005N小于等于0
            if(StringUtils.isNotEmpty(xnhk0127.getF002v())
                    && (xnhk0127.getF002v().contains("CD") || xnhk0127.getF002v().contains("SD"))
                    && ObjectUtils.isNotEmpty(xnhk0127.getF005n())
                    && xnhk0127.getF005n().compareTo(BigDecimal.ZERO) <= 0
                    && codes.contains(xnhk0127.getSeccode())){
                message = "XNHK0127“".concat(xnhk0127.getSeccode()).concat("”除权日为").concat(String.valueOf(xnhk0127.getF003d()).replace("-", "/")).concat("的F005N小于等于0");
                messags.add(message);
            }
        }

        // 重复性，XNHK0127表 00383.hk”CD,SD类型除权日为2024/09/13的数据重复
        List<Xnhk0127> xnhk0127DuplicateData = utsInfoService.getXnhk0127DuplicateData(startDate, endDate);
        if(!CollectionUtils.isEmpty(xnhk0127DuplicateData)){
            for(Xnhk0127 xnhk0127 : xnhk0127DuplicateData){
                if(ObjectUtils.isEmpty(xnhk0127.getF003d()) || !codes.contains(xnhk0127.getSeccode())){
                    log.info("xnhk0127表 F003d字段为空，code:{}", xnhk0127.getSeccode());
                    continue;
                }
                message = "XNHK0127“".concat(xnhk0127.getSeccode()).concat("”").concat(xnhk0127.getF002v()).concat("类型除权日为").concat(String.valueOf(xnhk0127.getF003d()).replace("-", "/")).concat("的数据重复");
                messags.add(message);
            }
        }


        if(CollectionUtils.isEmpty(messags)){
            return ReturnT.SUCCESS;
        }

        StringBuilder sb = new StringBuilder();

        for (String s : messags) {
            sb.append(s).append("\n");
        }

        DingDingMessageDTO dto = new DingDingMessageDTO();
        dto.setDDingServerEnum(DingDingServerEnum.SERVER_DATA_MONITOR);
        dto.setMessageContent(sb.toString());
        dingDingMessageApi.sendDingDingMessage(dto);
        return ReturnT.SUCCESS;
    }

}
