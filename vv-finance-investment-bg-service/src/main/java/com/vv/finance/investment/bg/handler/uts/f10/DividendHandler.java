package com.vv.finance.investment.bg.handler.uts.f10;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.quotation.req.StockKlineRangeReq;
import com.vv.finance.common.entity.quotation.req.StockKlineReq;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.stock.StockRankingApi;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.f10.ChangeBeforeAfterDividend;
import com.vv.finance.investment.bg.dto.f10.Dividend;
import com.vv.finance.investment.bg.dto.f10.DividendComparison;
import com.vv.finance.investment.bg.dto.f10.F10PageBaseReq;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.uts.Xnhk0102;
import com.vv.finance.investment.bg.entity.uts.Xnhk0127;
import com.vv.finance.investment.bg.entity.uts.Xnhks0112;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0102Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0127Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0101Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0112Mapper;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/9/6 11:04
 */
@Component
@RequiredArgsConstructor
public class DividendHandler {
    private final Xnhks0112Mapper xnhks0112Mapper;
    private final Xnhks0101Mapper xnhks0101Mapper;
    private static final Pattern FIND_NUMBER = Pattern.compile("([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9])");
    private static final String DEL_BRACKETS = "\\([^)]*\\)";
    private final Xnhk0102Mapper xnhk0102Mapper;
    private final Xnhk0127Mapper xnhk0127Mapper;
    private final HkTradingCalendarApi hkTradingCalendarApi;
    private final RedisClient redisClient;
    private final IStockDefineService stockDefineService;
    private final StockRankingApi stockRankingApi;

    @Resource
    private StockCache stockCache;


    public ResultT<Dividend> dividend(F10PageBaseReq req) {
        Dividend dividend = new Dividend();
        Long listingDate = xnhks0101Mapper.getStockListingDate(req.getStockCode());

        Page<Xnhks0112> xnhks0112Page = xnhks0112Mapper.selectPage(new Page<>(req.getCurrentPage(), req.getPageSize()),
                new QueryWrapper<Xnhks0112>().orderByDesc("F016D", "XDBMASK").eq("seccode", req.getStockCode()).isNotNull("F016D").gt(ObjectUtil.isNotEmpty(listingDate), "F001D", listingDate)
                        .ne("F006V", "无派息").and(xnhks0112QueryWrapper -> xnhks0112QueryWrapper.ne("F003V", "SS").ne("F003V", "SC")));

        dividend.setCumulativeDividendTimes(xnhks0112Page.getTotal());
        dividend.setCumulativeDividendAmount(calCumulativeDividendAmount(req.getStockCode(), listingDate));

        List<Dividend.DividendDetail> dividendDetails = xnhks0112Page.getRecords().stream().map(item -> {
            Dividend.DividendDetail dividendDetail = new Dividend.DividendDetail();
            dividendDetail.setYear(getYear(item.getF004D()));
            dividendDetail.setAssignmentType(item.getF005v());
            dividendDetail.setDividendScheme(item.getF006v());
            dividendDetail.setPublicationDate(dateFormatSlash(item.getF001d()));
            dividendDetail.setExDate(dateFormatSlash(item.getF016d()));
            dividendDetail.setStockRightDate(dateFormatSlash(item.getF018d()));
            dividendDetail.setLastTransferDate(getLastTransferDate(item.getF007d(),item.getF008d()));
            dividendDetail.setDividendDay(parseDividendDay(item));
            return dividendDetail;
        }).collect(Collectors.toList());
        dividend.setRecord(dividendDetails);
        dividend.setTotal(xnhks0112Page.getTotal());
        dividend.setCurrent(xnhks0112Page.getCurrent());
        dividend.setSize(xnhks0112Page.getSize());
        return ResultT.success(dividend);
    }

    //获取 累计分红金额
    private BigDecimal calCumulativeDividendAmount(String code, Long listingDate) {
        Xnhk0102 xnhk0102 = xnhk0102Mapper.selectOne(new QueryWrapper<Xnhk0102>().select("f069n").eq("seccode", code));
        String date = ObjectUtil.isEmpty(listingDate) ? null : DateUtil.formatDate(DateUtil.parse(String.valueOf(listingDate)));
        if (xnhk0102 == null) return null;
        List<Xnhk0127> f005n = xnhk0127Mapper.selectList(new QueryWrapper<Xnhk0127>()
                .select("f005n").eq("seccode", code).gt(ObjectUtil.isNotEmpty(date), "F003D", date).and(xnhks0127QueryWrapper -> xnhks0127QueryWrapper.like("F002V", "CD").or().like("F002V", "SD")));
        return f005n.stream().map(item -> calDividend(item.getF005n(), xnhk0102.getF069n())).reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    public ResultT<List<Xnhks0112>> changeBeforeAfterDividend(String code) {
        Long listingDate = xnhks0101Mapper.getStockListingDate(code);
        List<Xnhks0112> xnhks0112s = xnhks0112Mapper.selectList(new QueryWrapper<Xnhks0112>().select("F016D", "seccode").isNotNull("F016D").gt(ObjectUtil.isNotEmpty(listingDate), "F001D", listingDate)
                .eq("seccode", code).ge("F007D", 19000101).le("F007D", DateUtils.formatDateToLong(new Date(),null)).orderByDesc("F016D").last("limit 10"));
        return ResultT.success(xnhks0112s);
    }

    private String parseDividendDay(Xnhks0112 xnhks0112) {
//        String f003v = xnhks0112.getF003v();
//        Set<String> f003vSet = Sets.newHashSet(f003v.split(","));
//
//        if (f003vSet.contains("OD")) {
//            return dateFormatSlash(xnhks0112.getF015d());
//        }
//        if (f003vSet.contains("BS")) {
//            return dateFormatSlash(xnhks0112.getF011d());
//        }
//        if (f003vSet.contains("BW")) {
//            return dateFormatSlash(xnhks0112.getF012d());
//        }

        // 2021年10月20日 根据需求修改 分红权益-分红派息的除息日按照F010D\F011D\F012D\F013D\F014D\F015D\F017D 的顺序进行判断
        Long date = ObjectUtils.isNotEmpty(xnhks0112.getF010d()) ? xnhks0112.getF010d() :
                ObjectUtils.isNotEmpty(xnhks0112.getF011d()) ? xnhks0112.getF011d() :
                        ObjectUtils.isNotEmpty(xnhks0112.getF012d()) ? xnhks0112.getF012d() :
                                ObjectUtils.isNotEmpty(xnhks0112.getF013d()) ? xnhks0112.getF013d() :
                                        ObjectUtils.isNotEmpty(xnhks0112.getF014d()) ? xnhks0112.getF014d() :
                                                ObjectUtils.isNotEmpty(xnhks0112.getF015d()) ? xnhks0112.getF015d() :
                                                        ObjectUtils.isNotEmpty(xnhks0112.getF017d()) ? xnhks0112.getF017d() : null;

        return dateFormatSlash(date);
    }

    //相乘
    private BigDecimal calDividend(BigDecimal f005n, BigDecimal f069n) {
        if (f005n == null || f069n == null) {
            return BigDecimal.ZERO;
        }
        return f005n.multiply(f069n);
    }

    private BigDecimal calDividend(String f006v, BigDecimal f069n) {
        return parseF006v(f006v).multiply(f069n);
    }

    private BigDecimal parseF006v(String f006v) {
        f006v = f006v.replaceAll(DEL_BRACKETS, "");
        Matcher matcher = FIND_NUMBER.matcher(f006v);
        List<String> result = Lists.newArrayList();
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result.stream().map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String dateFormatSlash(Long date) {
        if (date == null) {
            return "";
        }
        String dateStr = date.toString();
        return dateFormatSlash(dateStr);
    }


    public String dateFormatSlash(String date) {
        if (date == null) {
            return "";
        }
        if (date.startsWith("1900")) {
            return "";
        }

        if (date.contains("-")) {
            date = date.replace("-","/");
        }else {
            date = date.substring(0, 4) + "/" + date.substring(4, 6) + "/" + date.substring(6);
        }
        return date;
    }

    private String getYear(Long date) {
        String dateStr = date.toString();
        return dateStr.substring(0, 4);
    }

    @Scheduled(fixedRate = 150000)
    public void dividendComparisonScheduled() {
        stockRankingApi.listIndustrySubsidiary().getData().forEach(industrySubsidiary -> dividendComparison(industrySubsidiary.getCode()));

    }

    public List<DividendComparison.DividendComparisonDetail> dividendComparison(String industryCode) {

        List<DividendComparison.DividendComparisonDetail> details = redisClient.get(RedisKeyConstants.DIVIDEND_COMPARISON.concat(industryCode));
        if (details != null) {
            return details;
        }
        List<StockDefine> stockDefines = stockDefineService.list(new QueryWrapper<StockDefine>().
                eq("industry_code", industryCode).eq("stock_type", StockTypeEnum.STOCK.getCode()));

        Map<String, String> stockNameMap = stockCache.queryStockNameMap(null);
        details = stockDefines.stream().filter(define -> stockNameMap.containsKey(define.getCode())).map(item -> {
            DividendComparison.DividendComparisonDetail comparisonDetail = new DividendComparison.DividendComparisonDetail();
            comparisonDetail.setCode(item.getCode());
            comparisonDetail.setName(stockNameMap.get(item.getCode()));
            comparisonDetail.setListingDate(dateFormatSlash(item.getListingdate()));
            StockSnapshot stockSnapshot = redisClient.get(RedisKeyConstants.RECEIVER_STOCK_SNAPSHOT_BEAN.concat(item.getCode()));
            if (stockSnapshot != null) {
                comparisonDetail.setCirculationMarketValue(stockSnapshot.getHkMarketValue());
                if (stockSnapshot.getTotalValue() != null) {
                    comparisonDetail.setTotalMarketValue(stockSnapshot.getTotalValue());
                }
                comparisonDetail.setIndustry(stockSnapshot.getIndustryName());
                comparisonDetail.setIndustryCode(stockSnapshot.getIndustryCode());
            }

            comparisonDetail.setStockType(StockTypeEnum.STOCK.getCode());
            comparisonDetail.setRegionType(RegionTypeEnum.HK.getCode());
            Long listingDate = xnhks0101Mapper.getStockListingDate(item.getCode());
            comparisonDetail.setCumulativeDividendAmount(calCumulativeDividendAmount(item.getCode(), listingDate));
            return comparisonDetail;
        }).collect(Collectors.toList());
        redisClient.set(RedisKeyConstants.DIVIDEND_COMPARISON.concat(industryCode), details, 300);
        return details;
    }

    public static void main(String[] args) {
        String reg = "股息: 港元 1.2<br>(待股份拆细23後每股拆细股份HKD 0.024)kkkk2.5\n";
        reg = reg.replaceAll(DEL_BRACKETS, "");
        Matcher matcher = FIND_NUMBER.matcher(reg);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }

        System.out.println(String.format("%05d", 99).concat(".hk"));

    }

    /**
     * 获取截止过户日
     */
    private String getLastTransferDate(Long startDate,Long endDate){
        if(startDate == null || endDate == null){
            return null;
        }
        return dateFormatSlash(startDate).concat("-").concat(dateFormatSlash(endDate));
    }


}
