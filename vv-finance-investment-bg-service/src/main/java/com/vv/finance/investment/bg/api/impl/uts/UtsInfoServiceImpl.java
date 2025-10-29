package com.vv.finance.investment.bg.api.impl.uts;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.stuxuhai.jpinyin.ChineseHelper;
import com.google.common.collect.Lists;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.info.ChinaRegionEnum;
import com.vv.finance.common.constants.info.GlobalMarketTypeEnum;
import com.vv.finance.common.constants.info.HkMarketEnum;
import com.vv.finance.common.entity.quotation.f10.ComCompanyTrendVo;
import com.vv.finance.common.entity.quotation.f10.ComFinancialNotifyVO;
import com.vv.finance.common.enums.StockRelationBizEnum;
import com.vv.finance.common.enums.StockRelationStatusEnum;
import com.vv.finance.common.us.entity.bg.StockNoticeTitle;
import com.vv.finance.common.utils.ConcatCodeUtil;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.quotation.IQuotationService;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.config.StockUtsNoticeConfig;
import com.vv.finance.investment.bg.constants.StockUtsNoticeEnum;
import com.vv.finance.investment.bg.dto.uts.resp.*;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.mongo.dao.StockUtsNoticeV2Dao;
import com.vv.finance.investment.bg.mongo.model.LineShapeMQEntityNew;
import com.vv.finance.investment.bg.stock.info.HkStockRelation;
import com.vv.finance.investment.bg.stock.info.HkStockScene;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.mapper.HkStockRelationMapper;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.info.mapper.StockSceneSimulateMapper;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import com.vv.finance.common.calc.hk.entity.StockKline;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chenyu
 * @date 2021/3/2 15:47
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
public class UtsInfoServiceImpl implements UtsInfoService {

    @Autowired
    Xnhk0127Mapper xnhk0127Mapper;

    @Autowired
    Xnhks0503Mapper xnhks0503Mapper;
    @Autowired
    Xnhk0201Mapper xnhk0201Mapper;
    @Autowired
    Xnhk0202Mapper xnhk0202Mapper;
    @Autowired
    Xnhk0203Mapper xnhk0203Mapper;
    @Autowired
    Xnhk0204Mapper xnhk0204Mapper;
    @Autowired
    Xnhk0205Mapper xnhk0205Mapper;
    @Autowired
    Xnhk0206Mapper xnhk0206Mapper;
    @Autowired
    Xnhk0207Mapper xnhk0207Mapper;
    @Autowired
    Xnhk0208Mapper xnhk0208Mapper;
    @Autowired
    Xnhk0209Mapper xnhk0209Mapper;
    @Autowired
    Xnhk0210Mapper xnhk0210Mapper;
    @Autowired
    Xnhk0211Mapper xnhk0211Mapper;

    @Autowired
    Xnhks0101Mapper xnhks0101Mapper;

    @Autowired
    Xnhke0101Mapper xnhke0101Mapper;

    @Autowired
    Xnhks0104Mapper xnhks0104Mapper;

    @Autowired
    Xnhks0105Mapper xnhks0105Mapper;

    @Autowired
    Xnhks0106Mapper xnhks0106Mapper;

    @Autowired
    Xnhks0401Mapper xnhks0401Mapper;

    @Autowired
    HkIisNewsAttachmentHistoricMapper attachmentHistoricMapper;

    @Autowired
    HkIisNewsAttachmentMapper attachmentMapper;

    @Autowired
    HkIisNewsCateRefHistoricMapper cateRefHistoricMapper;

    @Autowired
    HkIisNewsCateRefMapper cateRefMapper;

    @Autowired
    HkIisNewsHeadlineHistoricMapper headlineHistoricMapper;

    @Autowired
    HkIisNewsHeadlineMapper headlineMapper;

    @Autowired
    HkIisNewsSecurityRefHistoricMapper securityRefHistoricMapper;

    @Autowired
    HkIisNewsSecurityRefMapper securityRefMapper;

    @Autowired
    RedisClient redisClient;

    @Autowired
    StockUtsNoticeConfig stockUtsNoticeConfig;

    @Autowired
    StockUtsNoticeV2Dao stockUtsNoticeV2Dao;

    @Autowired
    Xnhk1002Mapper xnhk1002Mapper;

    @Autowired
    Xnhks0314Mapper xnhks0314Mapper;

    @Resource
    private Xnhks0301Mapper xnhks0301Mapper;

    @Resource
    private Xnhks0302Mapper xnhks0302Mapper;

    @Resource
    private Xnhk0307Mapper xnhk0307Mapper;

    @Resource
    private Xnhk0311Mapper xnhk0311Mapper;

    @Resource
    private Xnhks0308Mapper xnhks0308Mapper;

    @Resource
    private Xnhks0310Mapper xnhks0310Mapper;

    @Resource
    private Xnhks0317Mapper xnhks0317Mapper;

    @Resource
    private Xnhk0318Mapper xnhk0318Mapper;

    @Resource
    private Xnhks0110Mapper xnhks0110Mapper;

    @Resource
    private Xnhks0111Mapper xnhks0111Mapper;

    @Resource
    private Xnhk0129Mapper xnhk0129Mapper;

    @Resource
    private Xnhks0601Mapper xnhks0601Mapper;

    @Resource
    private Xnhk0602Mapper xnhk0602Mapper;

    @Resource
    private Xnhk0603Mapper xnhk0603Mapper;

    @Resource
    private Xnhk0605Mapper xnhk0605Mapper;

    @Resource
    private Xnhk0114Mapper xnhk0114Mapper;

    @Resource
    private Xnhks0112Mapper xnhks0112Mapper;

    @Resource
    private Xnhk0102Mapper xnhk0102Mapper;

    @Resource
    private Xnhks0319Mapper xnhks0319Mapper;

    @Resource
    private Xnhks0320Mapper xnhks0320Mapper;

    @Resource
    private Xnhk0406Mapper xnhk0406Mapper;

    @Resource
    private Xnhk0407Mapper xnhk0407Mapper;

    @Resource
    Xnhks0501Mapper xnhks0501Mapper;

    @Resource
    Xnhks0502Mapper xnhks0502Mapper;


    @Value("${stock.uts.notice.prefix}")
    String prefix;

    @Value("#{'${hk.index.code}'}")
    private String indexStr;

    @Autowired
    private Xnhks0313Mapper xnhks0313Mapper;

    @Resource
    private IQuotationService quotationService;

    @Resource
    private StockDefineMapper stockDefineMapper;

    @Resource
    private IIndustrySubsidiaryService industrySubsidiaryService;

    @Autowired
    private HkStockRelationMapper hkStockRelationMapper;

    @Autowired
    private HkTradingCalendarApi hkTradingCalendarApi;
//    @Resource
//    private UtsInfoService utsInfoService;

    @Resource
    StockSceneSimulateMapper stockSceneSimulateMapper;

    @Override
    public List<Xnhk0127> getXnhk0127History(Date date) {

        List<Xnhk0127> xnhk0127s = xnhk0127Mapper.getXnhk0127History(date);

//        List<Xnhk0127> xnhk0127s1 = xnhk0127Mapper.selectList(
//                new QueryWrapper<Xnhk0127>().le("F003D", date).eq("F007N", 1).orderByDesc("SECCODE").orderByAsc("F003D").orderByAsc("F001N"));
        return xnhk0127s;
    }

    @Override
    public List<Xnhk0127> getXnhk0127(Date date) {
        List<Xnhk0127> f003D = xnhk0127Mapper.selectList(new QueryWrapper<Xnhk0127>().eq("F003D", date).eq("F007N", 1).orderByAsc("F001N"));
        return f003D;
    }

    @Override
    public List<Xnhk0127> listXnhk0127(
            Date date,
            List<String> codes
    ) {
        List<Xnhk0127> f003D = xnhk0127Mapper
                .selectList(new QueryWrapper<Xnhk0127>().eq("F003D", date).in("SECCODE", codes).eq("F007N", 1));
        return f003D;
    }

    @Override
    public List<Xnhk0127> listXnhk0127ByDay(Date date, List<String> codes) {
        if (CollUtil.isEmpty(codes)) {
            return ListUtil.empty();
        }
        Long ymd = DateUtils.formatDateToLong(date, null);
        List<Xnhk0127> f003D = xnhk0127Mapper.selectList(new QueryWrapper<Xnhk0127>().eq("F003D", ymd).in("SECCODE", codes).eq("F007N", 1));
        return f003D;
    }

    @Override
    public List<Xnhk0127> listXnhk0127ByF002V(List<String> codes, List<String> f002Vs) {
        List<Xnhk0127> f003D = xnhk0127Mapper
                .selectList(new QueryWrapper<Xnhk0127>().in("F002V", f002Vs).in("SECCODE", codes).eq("F007N", 1));
        return f003D;
    }

    @Override
    public Xnhk0127 getXnhk0127ByF002V(String code, List<String> f002Vs) {
        Xnhk0127 f003D = xnhk0127Mapper
                .selectOne(new QueryWrapper<Xnhk0127>().in("F002V", f002Vs).eq("SECCODE", code).eq("F007N", 1)
                        .orderByDesc("F003D").last("limit 1"));
        return f003D;
    }

    @Override
    public List<Xnhk0127> getXnhk0127NotIncludeSS(Date startDate, Date endDate) {
        return xnhk0127Mapper.getXnhk0127NotIncludeSS(startDate, endDate);
    }

    @Override
    public List<Xnhk0127> getXnhk0127IncludeSS(Date startDate, Date endDate) {
        return xnhk0127Mapper.getXnhk0127IncludeSS(startDate, endDate);
    }
    @Override
    public List<Xnhk0127> getXnhk0127DuplicateData(Date startDate, Date endDate){
        return xnhk0127Mapper.getXnhk0127DuplicateData(startDate, endDate);
    }

    @Override
    public List<Xnhk0127> getXnhk0127sByDate(Date startDate, Date endDate){
        return xnhk0127Mapper.getXnhk0127sByDate(startDate, endDate);
    }

    @Override
    public List<Xnhks0503> getXnhks0503ByCodes(
            Set<String> codes,
            Long date
    ) {
        if (date != null) {
            date = Long.valueOf(DateUtils.formatDate(date, "yyyyMMdd"));
        }
        List<Xnhks0503> seccode = xnhks0503Mapper
                .selectList(new QueryWrapper<Xnhks0503>().in("SECCODE", codes).le(date != null, "F002D", date));
        return seccode;
    }

    @Override
    public Xnhks0503 getXnhks0503ByCode(String code) {
        Xnhks0503 seccode = xnhks0503Mapper.selectOne(new QueryWrapper<Xnhks0503>().orderByDesc("F002D").eq("SECCODE", code).last("limit 1"));
        return seccode;
    }

    @Override
    public List<Xnhks0503> getXnhks0503ByTime(Long date) {
        if (date != null) {
            date = Long.valueOf(DateUtils.formatDate(date, "yyyyMMdd"));
        }
        List<Xnhks0503> seccode =
                xnhks0503Mapper.selectList(new QueryWrapper<Xnhks0503>().eq(date != null, "F002D", date));
        return seccode;
    }

    @Override
    public List<Xnhk0127> getXnhk0127ByCode(String code) {
        List<Xnhk0127> f003D = xnhk0127Mapper.selectList(new QueryWrapper<Xnhk0127>().eq("SECCODE", code));
        return f003D;
    }

    @Override
    public List<String> listFinCodes(
            Date date,
            List<String> codes
    ) {
        if (CollectionUtils.isEmpty(codes)) {
            return Collections.emptyList();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Long dateTime = Long.valueOf(sdf.format(date));
        Set<String> resultCodes = new HashSet<>();
        resultCodes.addAll(xnhk0201Mapper
                .selectList(new QueryWrapper<Xnhk0201>().select("SECCODE").eq("F001D", dateTime).in("SECCODE", codes))
                .stream().map(Xnhk0201::getSeccode).collect(Collectors.toSet()));
        resultCodes.addAll(xnhk0202Mapper
                .selectList(new QueryWrapper<Xnhk0202>().select("SECCODE").eq("F001D", dateTime).in("SECCODE", codes))
                .stream().map(Xnhk0202::getSeccode).collect(Collectors.toSet()));
        resultCodes.addAll(xnhk0204Mapper
                .selectList(new QueryWrapper<Xnhk0204>().select("SECCODE").eq("F001D", dateTime).in("SECCODE", codes))
                .stream().map(Xnhk0204::getSeccode).collect(Collectors.toSet()));
        resultCodes.addAll(xnhk0205Mapper
                .selectList(new QueryWrapper<Xnhk0205>().select("SECCODE").eq("F001D", dateTime).in("SECCODE", codes))
                .stream().map(Xnhk0205::getSeccode).collect(Collectors.toSet()));
        resultCodes.addAll(xnhk0207Mapper
                .selectList(new QueryWrapper<Xnhk0207>().select("SECCODE").eq("F001D", dateTime).in("SECCODE", codes))
                .stream().map(Xnhk0207::getSeccode).collect(Collectors.toSet()));
        resultCodes.addAll(xnhk0208Mapper
                .selectList(new QueryWrapper<Xnhk0208>().select("SECCODE").eq("F001D", dateTime).in("SECCODE", codes))
                .stream().map(Xnhk0208::getSeccode).collect(Collectors.toSet()));
        resultCodes.addAll(xnhk0210Mapper
                .selectList(new QueryWrapper<Xnhk0210>().select("SECCODE").eq("F001D", dateTime).in("SECCODE", codes))
                .stream().map(Xnhk0210::getSeccode).collect(Collectors.toSet()));


        return new ArrayList<>(resultCodes);
    }

    @Override
    public List<String> listFinCodesV2(
            Date date,
            List<String> codes
    ) {
        if (CollectionUtils.isEmpty(codes)) {
            return Collections.emptyList();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Long dateTime = Long.valueOf(sdf.format(date));
        return new ArrayList<>(new HashSet<>(xnhks0313Mapper
                .selectList(new QueryWrapper<Xnhks0313>().select("SECCODE").eq("F001D", dateTime).in("SECCODE", codes))
                .stream().map(Xnhks0313::getSeccode).collect(Collectors.toSet())));
    }

    @SneakyThrows
    @Override
    public StockUtsBasicFactsResp getBasicFacts(String code) {

        // if (!checkCode(code)) {
        //     return null;
        // }

        //处于并行交易的临时股票，需展示原股票的公司概况
        List<ReuseTempDTO> tempTradeList = this.findTradingTempStockByTime(new Date());
        Map<String, ReuseTempDTO> tempTradeMap = tempTradeList.stream().collect(Collectors.toMap(ReuseTempDTO::getCode, Function.identity()));
        ReuseTempDTO reuseTempDTO = tempTradeMap.get(code);
        if(reuseTempDTO != null){
            code = reuseTempDTO.getRelationCode();
        }

        StockUtsBasicFactsResp hget = redisClient.hget(RedisKeyConstants.BASIC_FACTS_MAP, code);

        if (hget != null) {
            return hget;
        }

        Xnhks0101 xnhks0101 = xnhks0101Mapper.selectOne(new QueryWrapper<Xnhks0101>().eq(Xnhks0101.SECCODE, code));
        xnhks0101 = xnhks0101 == null ? new Xnhks0101() : xnhks0101;
        Xnhke0101 xnhke0101 = xnhke0101Mapper.selectOne(new QueryWrapper<Xnhke0101>().eq(Xnhke0101.SECCODE, code));
        xnhke0101 = xnhke0101 == null ? new Xnhke0101() : xnhke0101;
        Xnhks0104 xnhks0104 = xnhks0104Mapper.selectOne(new QueryWrapper<Xnhks0104>().eq(Xnhks0104.SECCODE, code));
        xnhks0104 = xnhks0104 == null ? new Xnhks0104() : xnhks0104;
        IndustrySubsidiary subsidiary = industrySubsidiaryService.getStockIndustry(code);

        Xnhks0401 xnhks0401 = xnhks0401Mapper.selectOne(new QueryWrapper<Xnhks0401>().eq(Xnhks0401.SECCODE, code));
        xnhks0401 = xnhks0401 == null ? new Xnhks0401() : xnhks0401;
        Xnhks0106 xnhks0106 = xnhks0106Mapper.selectOne(new QueryWrapper<Xnhks0106>().eq(Xnhks0106.SECCODE, code).orderByDesc(Xnhks0106.F006D).last("limit 0,1"));
        xnhks0106 = xnhks0106 == null ? new Xnhks0106() : xnhks0106;
        Xnhks0105 xnhks0105 = xnhks0105Mapper.selectOne(new QueryWrapper<Xnhks0105>().eq(Xnhks0105.SECCODE, code));
        xnhks0105 = xnhks0105 == null ? new Xnhks0105() : xnhks0105;

        StockDefine stockDefine = stockDefineMapper.selectOne(Wrappers.lambdaQuery(StockDefine.class).eq(StockDefine::getCode, code).last("limit 1"));

        StockUtsBasicFactsResp stockUtsBasicFactsResp = StockUtsBasicFactsResp.builder()
                .name(xnhks0101.getF003v())
                .englishName(xnhke0101.getF003v())
                .marketType(buildMarket(xnhks0101.getF004v(), xnhks0101.getF005v()))
                .industry(subsidiary.getName())
                .ipoTime(xnhks0101.getF006d() == null ? null : DateUtils.parseDate(String.valueOf(xnhks0101.getF006d()), "yyyyMMdd").getTime())
                .registeredAddress(checkRegion(xnhks0104.getF008v()))
                .chairmanName(xnhks0104.getF015v())
                .companySecretary(dislodgeHtmlTag(xnhks0104.getF003v()))
                .staffNum(xnhks0401.getF008n())
                .isIn(ObjectUtil.isNotEmpty(stockDefine) ? stockDefine.getIsincode() : null)
                .auditingBody(StrUtil.replace(xnhks0104.getF006v(), "<br>", "、"))
                .accountingDate(xnhks0106.getF006d() == null ? null : DateUtils.parseDate(String.valueOf(xnhks0106.getF006d()), "yyyyMMdd").getTime())
                .accountingDateStr(xnhks0106.getF006d() == null ? null : DateUtil.format(DateUtil.parse(String.valueOf(xnhks0106.getF006d())), "MM/dd"))
                .businessAddress(xnhks0104.getF007v())
                .phoneNum(xnhks0104.getF012v())
                .fox(xnhks0104.getF013v())
                .email(xnhks0104.getF011v())
                .website(xnhks0104.getF010v())
                .mainBusiness(dislodgeHtmlTag(xnhks0105.getF001v()))
                .build();

        redisClient.hset(RedisKeyConstants.BASIC_FACTS_MAP, code, stockUtsBasicFactsResp, 2 * 60 * 60);

        return stockUtsBasicFactsResp;
    }

    private Boolean checkCode(String code) {
        return code.matches("^\\d{5}.hk");
    }

    /**
     * 去除html标签
     *
     * @param document
     * @return
     */
    private String dislodgeHtmlTag(String document) {
        if (StringUtils.isBlank(document)) {
            return "";
        }
        return document.replaceAll("<[^>]+>", " ");
    }

    private String checkRegion(String f015v) {
        if (StringUtils.isEmpty(f015v)) {
            return "";
        }
        ChinaRegionEnum byCode = ChinaRegionEnum.getByCode(f015v);
        if (byCode == null) {
            return f015v;
        }
        return byCode.getDesc();
    }

    private String buildMarket(String f004v, String f005v) {
        if (StringUtils.isEmpty(f004v) || StringUtils.isEmpty(f005v)) {
            return "";
        }
        String region = GlobalMarketTypeEnum.getByCode(f004v) != null ? GlobalMarketTypeEnum.getByCode(f004v).getDesc() : "";
        String market = HkMarketEnum.getByCode(f005v) != null ? HkMarketEnum.getByCode(f005v).getDesc() : "";
        return region.concat(market);
    }

    @SneakyThrows
    @Override
    public StockUtsBriefingResp getBriefing(String code) {

        // if (!checkCode(code)) {
        //     return null;
        // }

        //处于并行交易的临时股票，需展示原股票的公司概况
        List<ReuseTempDTO> tempTradeList = this.findTradingTempStockByTime(new Date());
        Map<String, ReuseTempDTO> tempTradeMap = tempTradeList.stream().collect(Collectors.toMap(ReuseTempDTO::getCode, Function.identity()));
        ReuseTempDTO reuseTempDTO = tempTradeMap.get(code);
        if(reuseTempDTO != null){
            code = reuseTempDTO.getRelationCode();
        }

        StockUtsBriefingResp stockBriefing = redisClient.hget(RedisKeyConstants.BRIEFING_MAP, code);

        if (stockBriefing != null) {
            return stockBriefing;
        }

        Xnhks0101 xnhks0101 = xnhks0101Mapper.selectOne(new QueryWrapper<Xnhks0101>().eq(Xnhks0101.SECCODE, code));
        xnhks0101 = xnhks0101 == null ? new Xnhks0101() : xnhks0101;
        Xnhks0503 xnhks0503 = xnhks0503Mapper.selectOne(new QueryWrapper<Xnhks0503>().eq(Xnhks0101.SECCODE, code).orderByDesc(Xnhks0106.MODIFIED_DATE).last(" limit 1"));
        Xnhks0104 xnhks0104 = xnhks0104Mapper.selectOne(new QueryWrapper<Xnhks0104>().eq(Xnhks0104.SECCODE, code));
        Xnhks0105 xnhks0105 = xnhks0105Mapper.selectOne(new QueryWrapper<Xnhks0105>().eq(Xnhks0105.SECCODE, code));


        StockUtsBriefingResp stockUtsBriefingResp = StockUtsBriefingResp.builder()
                .chairmanName(xnhks0104 != null ? xnhks0104.getF015v() : null)
                .ipoNum(xnhks0503 != null ? xnhks0503.getF035n() : null)
                .ipoPrice(xnhks0503 != null ? xnhks0503.getF011n() : null)
                .ipoTime(xnhks0101.getF006d() != null ? DateUtils.parseDate(String.valueOf(xnhks0101.getF006d()), "yyyyMMdd").getTime() : null)
                .mainBusiness(dislodgeHtmlTag(xnhks0105 != null ? xnhks0105.getF001v() : null))
                .marketType(buildMarket(xnhks0101.getF004v(), xnhks0101.getF005v()))
                .name(xnhks0101.getF003v()).build();

        redisClient.hset(RedisKeyConstants.BRIEFING_MAP, code, stockUtsBriefingResp, 2 * 60 * 60);

        return stockUtsBriefingResp;
    }

    @Override
    public PageDomain<StockUtsNoticeListResp> listNotice(Integer type, String code, Integer currentPage, Integer pageSize) {
        Page<HkIisNewsHeadlineBase> headlinePageDomain = new Page<>();
        headlinePageDomain.setSize(pageSize == null ? 20L : pageSize);
        headlinePageDomain.setCurrent(currentPage == null ? 1L : currentPage);
        if (!checkCode(code)) {
            return null;
        }
        code = simpleCode(code);
        if (type == null) {
            type = StockUtsNoticeEnum.ALL.getCode();
        }

        List<String> types = null;
        if (type != StockUtsNoticeEnum.ALL.getCode()) {
            Map<String, List<String>> cacheMap = stockUtsNoticeConfig.getCacheMap();
            types = cacheMap.get(Objects.requireNonNull(StockUtsNoticeEnum.getByCode(type)).getOperation());
        }

        Page<HkIisNewsHeadlineBase> hkIisNewsHeadlinePage = headlineMapper.selectNotice(headlinePageDomain, code, type, types);

        PageDomain<StockUtsNoticeListResp> stockUtsNoticeListRespPageDomain = new PageDomain<>();

        stockUtsNoticeListRespPageDomain.setTotal(hkIisNewsHeadlinePage.getTotal());
        stockUtsNoticeListRespPageDomain.setSize(hkIisNewsHeadlinePage.getSize());
        stockUtsNoticeListRespPageDomain.setCurrent(hkIisNewsHeadlinePage.getCurrent());

        List<HkIisNewsHeadlineBase> records = hkIisNewsHeadlinePage.getRecords();

        List<StockUtsNoticeListResp> stockUtsNoticeListResps = records.stream().map(item -> {
            StockUtsNoticeListResp stockUtsNoticeListResp = new StockUtsNoticeListResp();
            stockUtsNoticeListResp.setTitle(ChineseHelper.convertToSimplifiedChinese(item.getHeadline()));
            try {
                stockUtsNoticeListResp.setTimestamps(DateUtils.parseDate(StringUtils.substringBefore(item.getDateLine(), "T"), "yyyyMMdd").getTime());
            } catch (ParseException e) {
                log.error("parse date error!!!", e);
            }
            return stockUtsNoticeListResp;
        }).collect(Collectors.toList());

        stockUtsNoticeListRespPageDomain.setRecords(stockUtsNoticeListResps);

        return stockUtsNoticeListRespPageDomain;
    }

    @Override
    public PageDomain<StockUtsNoticeListResp> listNoticeByMongo(Integer type, String code, Integer currentPage, Integer pageSize) {

        pageSize = pageSize == null ? 20 : pageSize;
        currentPage = currentPage == null ? 1 : currentPage;
        if (!checkCode(code)&&!code.contains("-t")) {
            return null;
        }
        code = simpleCode(code);
        if (type == null) {
            type = StockUtsNoticeEnum.ALL.getCode();
        }

        List<String> types = null;
        if (type != StockUtsNoticeEnum.ALL.getCode()) {
            //cacheMap存的是？-- stock.uts.notice 在配置中心进行配置
            Map<String, List<String>> cacheMap = stockUtsNoticeConfig.getCacheMap();
            types = cacheMap.get(Objects.requireNonNull(StockUtsNoticeEnum.getByCode(type)).getOperation());
        }

        NoticeMongoPageResp noticeMongoPageResp = stockUtsNoticeV2Dao.queryNotice(types, type, code, pageSize, currentPage);

        PageDomain<StockUtsNoticeListResp> stockUtsNoticeListRespPageDomain = new PageDomain<>();

        Long total = noticeMongoPageResp.getTotal();
        stockUtsNoticeListRespPageDomain.setTotal(total);
        stockUtsNoticeListRespPageDomain.setSize(pageSize);
        stockUtsNoticeListRespPageDomain.setCurrent(currentPage);

        if (total == 0) {
            return stockUtsNoticeListRespPageDomain;
        }

        List<StockUtsNoticeResp> data = noticeMongoPageResp.getData();

        Map<String, List<StockUtsNoticeResp>> listMap = data.stream().collect(Collectors.groupingBy(StockUtsNoticeResp::getDateLine));
        LinkedList<StockUtsNoticeListResp> noticeListResps = new LinkedList<>();
        AtomicReference<String> categoryId = new AtomicReference<>("");
        listMap.forEach((key, value) -> {
            StockUtsNoticeListResp stockUtsNoticeListResp = new StockUtsNoticeListResp();
            LinkedList<StockUtsNoticeEnclosureResp> resps = new LinkedList<>();
            int size = value.size();
            for (int i = 0; i < size; i++) {
                StockUtsNoticeResp utsNoticeResp = value.get(i);
                if (i == 0) {
                    categoryId.set(utsNoticeResp.getCategoryId());
                    stockUtsNoticeListResp.setTitle(ChineseHelper.convertToSimplifiedChinese(utsNoticeResp.getHeadLine()));
                    try {
                        String timeStr = StringUtils.substringBefore(utsNoticeResp.getDateLine(), "+").replace("T","");
                        stockUtsNoticeListResp.setTimestamps(DateUtils.parseDate(timeStr, "yyyyMMddHHmmss").getTime());
                    } catch (ParseException e) {
                        log.error("parse date error!!!", e);
                    }
                    if (size == 1 || !StringUtils.isEmpty(utsNoticeResp.getFileDesc()) || utsNoticeResp.getAttachmentNum() == 1 || !utsNoticeResp.getFileName().contains(".HTM")) {
                        resps.add(StockUtsNoticeEnclosureResp.builder().name(utsNoticeResp.getFileDesc()).path(prefix.concat(utsNoticeResp.getDirs()).concat("-").concat(utsNoticeResp.getFileName().toLowerCase())).build());
                    }
                }
                if (i > 0 && categoryId.get().equals(utsNoticeResp.getCategoryId()) && !StringUtils.isEmpty(utsNoticeResp.getFileDesc()) && !utsNoticeResp.getFileName().contains(".HTM")) {
                    resps.add(StockUtsNoticeEnclosureResp.builder().name(utsNoticeResp.getFileDesc()).path(prefix.concat(utsNoticeResp.getDirs()).concat("-").concat(utsNoticeResp.getFileName().toLowerCase())).build());
                }
            }
            categoryId.set("");
            stockUtsNoticeListResp.setEnclosure(resps);
            noticeListResps.add(stockUtsNoticeListResp);
        });
        noticeListResps.sort(Comparator.comparing(StockUtsNoticeListResp::getTimestamps).reversed());
        stockUtsNoticeListRespPageDomain.setTotal(noticeListResps.size());
        List<StockUtsNoticeListResp> list = getPageList(noticeListResps, currentPage, pageSize);
        stockUtsNoticeListRespPageDomain.setRecords(list);
        return stockUtsNoticeListRespPageDomain;
    }

    private List<StockUtsNoticeListResp> getPageList(
            List<StockUtsNoticeListResp> notices,
            Integer current,
            Integer size
    ) {

        if (size == null || size <= 0) {
            size = 20;
        }
        if (current == null || current <= 0) {
            current = 1;
        }
        int listSize = notices.size();

        if (listSize < size) {
            return notices;
        }

        int i = (current - 1) * size;

        if (listSize < i) {
            return Collections.emptyList();
        }

        if (listSize < (i + size)) {
            return new ArrayList<>(notices.subList(i, listSize));
        }
        return new ArrayList<>(notices.subList(i, i + size));
    }

    private String simpleCode(String code) {
        String temp = code.replaceAll(".hk", "");
        return temp.replaceFirst("^0*", "");
    }

    @Override
    public ResultT<List<Xnhk1002>> listMembersAndIndex() {
        List<Xnhk1002> xnhk1002s = xnhk1002Mapper.listMembersAndIndex("(".concat(indexStr).concat(")"));
        if (CollectionUtils.isEmpty(xnhk1002s)) {
            log.info("指数成分股码表为空!index:{}", indexStr);
            return ResultT.fail();
        }
        return ResultT.success(xnhk1002s);
    }

    @Override
    public ResultT<List<ReuseTempDTO>> listResultTempList() {
        List<Xnhks0314> xnhks0314s = xnhks0314Mapper.listResultTemp();
        List<ReuseTempDTO> collect = buildReuseTemp(xnhks0314s);

        return ResultT.success(collect);
    }

    @Override
    public ResultT<List<Xnhk0127>> queryDividendStock(Long queryDate) {

        log.info("获取指定日期的所有派息的股票入参：{}", queryDate);
        List<Xnhk0127> xnhk0127s = xnhk0127Mapper.queryDividendStock(queryDate);

        return ResultT.success(xnhk0127s);
    }

    /**
     * 获取指定结束交易日期的临时股票集合
     * @return
     */
    @Override
    public ResultT<List<ReuseTempDTO>> findEndTradeTempStock(Date time) {
        List<Xnhks0314> xnhks0314s = xnhks0314Mapper.findEndTradeTempStock(DateUtils.formatDateToLong(time,null));
        List<ReuseTempDTO> reuseTemps = buildReuseTemp(xnhks0314s);
        //临时股票stockId赋值
        buildStockId(reuseTemps);

        return ResultT.success(reuseTemps);
    }
    //临时股票stockId赋值
    private void buildStockId(List<ReuseTempDTO> reuseTemps) {
        if (CollUtil.isNotEmpty(reuseTemps)) {
            List<String> codes = reuseTemps.stream().map(reuseTemp -> reuseTemp.getCode()).collect(Collectors.toList());
            List<HkStockRelation> stockRelations = hkStockRelationMapper.selectList(Wrappers.<HkStockRelation>lambdaQuery().in(HkStockRelation::getSourceCode, codes));
            if (CollUtil.isNotEmpty(stockRelations)) {
                //原code为临时股票或者代码复用时会有多条数据
                Map<String, List<HkStockRelation>> sourceCodeMap = stockRelations.stream().collect(Collectors.groupingBy(o -> o.getSourceCode()));
                Date now = DateUtils.getOfDayFirst(new Date());
                reuseTemps.forEach(reuseTemp->{
                    List<HkStockRelation> tempStockRelations = sourceCodeMap.get(reuseTemp.getCode());
                    if (CollUtil.isNotEmpty(tempStockRelations)) {
                        List<HkStockRelation> relations = tempStockRelations.stream().filter(tempRelation -> {
                            if ((StringUtils.isNotBlank(tempRelation.getBizTime()) && tempRelation.getBizTime().equals(DateUtils.formatDate(reuseTemp.getEndTime(), "yyyyMMdd"))) ||
                                    (StringUtils.isBlank(tempRelation.getBizTime()) &&  reuseTemp.getStartTime() <= now.getTime() && reuseTemp.getEndTime() >= now.getTime()) ||
                                       (StringUtils.isNotBlank(tempRelation.getBizTime()) && tempRelation.getBizTime().equals(DateUtils.formatDate(reuseTemp.getStartTime(), "yyyyMMdd"))) ){
                                return true;
                            }
                            return false;
                        }).collect(Collectors.toList());
                        if (CollUtil.isNotEmpty(relations)) {
                            HkStockRelation stockRelation = relations.get(0);
                            reuseTemp.setStockId(stockRelation.getStockId());
                        }
                    }
                });
            }
        }
    }

    private List<ReuseTempDTO> buildReuseTemp(List<Xnhks0314> xnhks0314s) {
        List<ReuseTempDTO> collect = xnhks0314s.stream().map(item -> {
            ReuseTempDTO reuseTemp = new ReuseTempDTO();
            reuseTemp.setRelationCode(item.getSeccode());
            reuseTemp.setCode(String.format("%05d", Integer.parseInt(item.getF003v())).concat(".hk"));
            reuseTemp.setStartTime(LocalDateTimeUtil.getTimestamp(LocalDate.parse(item.getF006d().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))));
            reuseTemp.setEndTime(LocalDateTimeUtil.getTimestamp(LocalDate.parse(item.getF007d().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))));
            reuseTemp.setStockName(item.getF005v());
            reuseTemp.setRelationStockName(item.getF002v());
             return reuseTemp;
        }).collect(Collectors.toList());
        return collect;
    }
    /**
     * 获取指定交易日期内有交易的临时股票集合
     * @return
     */
    @Override
    public List<ReuseTempDTO> findTradingTempStockByTime(Date time) {
        long currentDate = DateUtils.formatDateToLong(time,null);
        List<Xnhks0314> xnhks0314s = xnhks0314Mapper.findTradingTempStockByTime(currentDate);
        if(CollUtil.isEmpty(xnhks0314s)){
            return Lists.newArrayList();
        }

        List<ReuseTempDTO> reuseTempDTOS = Lists.newArrayList();
        for (Xnhks0314 xnhks0314 : xnhks0314s) {
            Xnhks0314 xnhks0314ForLast = xnhks0314Mapper.queryOneByF003V(xnhks0314.getF003v());
            if(Objects.isNull(xnhks0314ForLast)){
                continue;
            }

            long f006d = xnhks0314.getF006d();
            long f007d = xnhks0314.getF007d();

            long f006dLast = xnhks0314ForLast.getF006d();
            long f007dLast = xnhks0314ForLast.getF007d();
            // 同一条，取任意一条
            if(f006d == f006dLast && f007d == f007dLast){
                this.buildReuseTempDTOS(reuseTempDTOS, xnhks0314ForLast);
                continue;
            }
            if(f007d >= f006dLast){
                // 有交叉，判断最新的那一条是否符合当前日期
                if(f006dLast <= currentDate && f007dLast >= currentDate){
                    this.buildReuseTempDTOS(reuseTempDTOS, xnhks0314ForLast);
                }
            } else {
                // 无交叉，取符合日期的那条
                this.buildReuseTempDTOS(reuseTempDTOS, xnhks0314);
            }
        }
        //临时股票stockId赋值
        buildStockId(reuseTempDTOS);
        return reuseTempDTOS;
    }

    @Override
    public List<ReuseTempDTO> findAllTempStocks() {
        List<Xnhks0314> xnhks0314s = xnhks0314Mapper.queryAllTempStocks();
        List<ReuseTempDTO> resultReuseTempDTOS= Lists.newArrayList();
        if(CollUtil.isEmpty(xnhks0314s)){
            return resultReuseTempDTOS;
        }
        xnhks0314s.forEach(xnhks0314 -> {
            ReuseTempDTO reuseTempDTO = new ReuseTempDTO();
            reuseTempDTO.setCode(String.format("%05d", Integer.parseInt(xnhks0314.getF003v())).concat(".hk"));
            reuseTempDTO.setEndTime(LocalDateTimeUtil.getTimestamp(LocalDate.parse(xnhks0314.getF007d().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))));
            resultReuseTempDTOS.add(reuseTempDTO);
        });
//        //临时股票stockId赋值
//        buildStockId(resultReuseTempDTOS);
        //临时股票过滤股权股票
        return tempStockFliterStockRights2(resultReuseTempDTOS);
    }

    @Override
    public List<String> findAllUnTradeTempSocks(Date time) {
        // 查询所有临时股票
        List<ReuseTempDTO> allTempStocks = findAllTempStocks();
        if (CollUtil.isEmpty(allTempStocks)) {
            return Collections.emptyList();
        }
        // 查询正在交易的临时股票
        List<ReuseTempDTO> tradeTempStocks = findTradingTempStockByTime(time);
        List<String> tradeTempCodes = CollUtil.defaultIfEmpty(tradeTempStocks, Collections.emptyList()).stream().map(ReuseTempDTO::getCode).collect(Collectors.toList());

        // 非正在交易的临时股票
        List<String> unTradeTempCodes =allTempStocks.stream().filter(tempDto -> !tradeTempCodes.contains(tempDto.getCode())).map(ReuseTempDTO::getCode).collect(Collectors.toList());
        //临时股票过滤股权股票
        tempStockFliterStockRights(unTradeTempCodes);
        // 当日码表
        Set<String> newCodes = quotationService.selectStockCodeList();

        if (CollUtil.isNotEmpty(newCodes)) {
            // 交集
            Collection<String> intersection = CollUtil.intersection(unTradeTempCodes, newCodes);
            unTradeTempCodes = CollUtil.subtractToList(unTradeTempCodes, intersection);
        }

        return unTradeTempCodes;
    }
    //临时股票过滤股权股票
    private void tempStockFliterStockRights(List<String> unTradeTempCodes) {
        List<StockRightsDTO> allStockRightsDTOs = xnhks0101Mapper.getAllStockRights();
        if (CollUtil.isNotEmpty(unTradeTempCodes) && CollUtil.isNotEmpty(allStockRightsDTOs)) {
            List<String> stockRightsCodes = allStockRightsDTOs.stream().map(stockRightsDTO -> stockRightsDTO.getCode()).collect(Collectors.toList());
            unTradeTempCodes.removeAll(stockRightsCodes);
        }
    }
    //临时股票过滤股权股票
    private List<ReuseTempDTO> tempStockFliterStockRights2(List<ReuseTempDTO> resultReuseTempDTOS) {
        List<StockRightsDTO> allStockRightsDTOs = xnhks0101Mapper.getAllStockRights();
        if (CollUtil.isNotEmpty(resultReuseTempDTOS) && CollUtil.isNotEmpty(allStockRightsDTOs)) {
            List<String> stockRightsCodes = allStockRightsDTOs.stream().map(stockRightsDTO -> stockRightsDTO.getCode()).collect(Collectors.toList());
            return resultReuseTempDTOS.stream().filter(temp->!stockRightsCodes.contains(temp.getCode())).collect(Collectors.toList());
        }
        return resultReuseTempDTOS;
    }

    @Override
    public List<Xnhk0127> getXnhk0127HistoryByCode(Date date, String code) {
        List<Xnhk0127> xnhk0127s = xnhk0127Mapper.selectList(
                new QueryWrapper<Xnhk0127>().le("F003D", date).eq("F007N", 1).eq("SECCODE",code).orderByAsc("F003D").orderByAsc("F001N"));
        return xnhk0127s;
    }
    /**
     * 获取公告类型列表
     * @return
     */
    @Override
    public List<StockNoticeTitle> getNoticeTitle() {
        List<StockNoticeTitle> stockNoticeTitles = Arrays.stream(StockUtsNoticeEnum.values()).map(value -> {
            StockNoticeTitle stockNoticeTitle = new StockNoticeTitle();
            stockNoticeTitle.setNoticeTitleName(value.getDesc());
            stockNoticeTitle.setType((long) value.getCode());
            return stockNoticeTitle;
        }).collect(Collectors.toList());

        return stockNoticeTitles;
    }
    /**
     * 删除临时股票公告数据
     * @param stockCode
     * @return
     */
    @Async
    @Override
    public void delNoticeByStockCode(String stockCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("删除临时股票公告数据 开始：stockCode：{}",stockCode);
            stockUtsNoticeV2Dao.delByStockCode(stockCode);
            log.info("删除临时股票公告数据 结束：stockCode：{} 耗时：{}",stockCode,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("删除临时股票公告数据：stockCode：{}  异常",stockCode,e);
        }

    }
    /**
     * 变更公告股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     * @return
     */
    @Override
    public void upNoticeStockCode(String sourceCode, String targetCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("变更公告股票code数据 开始：sourceCode：{} targetCode：{}",sourceCode,targetCode);
            stockUtsNoticeV2Dao.upNoticeStockCode(sourceCode,targetCode);
            log.info("变更公告股票code数据 结束：sourceCode：{} targetCode：{} 耗时：{}",sourceCode,targetCode,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("变更公告股票code数据：sourceCode：{} targetCode：{} 异常",sourceCode,targetCode,e);
        }

    }
    /**
     * 获取code集合获取临时交易信息集合
     * @return
     */
    @Override
    public List<ReuseTempDTO> findTempStockInfoByCodes(List<String> codes) {
        codes = codes.stream().map(code ->Integer.valueOf(code.replace(".hk","")).toString()).collect(Collectors.toList());
        List<Xnhks0314> xnhks0314s = xnhks0314Mapper.findTempStockInfoByCodes(arrayToStr(codes));
        List<ReuseTempDTO> reuseTempDTOS = new ArrayList<>();
        if (CollUtil.isNotEmpty(xnhks0314s)) {
            reuseTempDTOS = buildReuseTemp(xnhks0314s);
        }

        return reuseTempDTOS;
    }

    @Override
    public void saveSimulateNoticeInfo(String simulateCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("新增模拟股票形态数据 开始：simulateCode：{} ", simulateCode);
            stockUtsNoticeV2Dao.saveSimulateNoticeInfo(simulateCode);
            log.info("新增模拟股票形态数据 结束：simulateCode：{} 耗时：{}", simulateCode,  System.currentTimeMillis() - l);

        } catch (Exception e) {
            log.error("新增模拟股票形态数据：simulateCode：{} 异常", simulateCode, e);
        }
    }

    private <T> String arrayToStr(Collection<T> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return "('')";
        }
        String join = StringUtils.join(codes, "','");
        return "('" + join + "')";
    }
    private void buildReuseTempDTOS(List<ReuseTempDTO> reuseTempDTOS, Xnhks0314 xnhks0314ForLast) {
        ReuseTempDTO reuseTemp = new ReuseTempDTO();
        reuseTemp.setRelationCode(xnhks0314ForLast.getSeccode());
        reuseTemp.setCode(String.format("%05d", Integer.parseInt(xnhks0314ForLast.getF003v())).concat(".hk"));
        reuseTemp.setStartTime(LocalDateTimeUtil.getTimestamp(LocalDate.parse(xnhks0314ForLast.getF006d().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))));
        reuseTemp.setEndTime(LocalDateTimeUtil.getTimestamp(LocalDate.parse(xnhks0314ForLast.getF007d().toString(), DateTimeFormatter.ofPattern("yyyyMMdd"))));
        reuseTemp.setStockName(xnhks0314ForLast.getF005v());
        reuseTemp.setRelationStockName(xnhks0314ForLast.getF002v());
        reuseTemp.setStopTradeTime(xnhks0314ForLast.getF008v());
        reuseTemp.setRestoreTradeTime(xnhks0314ForLast.getF009v());

        if(CollUtil.isNotEmpty(reuseTempDTOS)){
            for (ReuseTempDTO reuseTempDTO : reuseTempDTOS) {
                if(StringUtils.equals(reuseTempDTO.getCode(), reuseTemp.getCode()) && Objects.equals(reuseTempDTO.getStartTime(),reuseTemp.getStartTime()) && Objects.equals(reuseTempDTO.getEndTime(), reuseTemp.getEndTime())){
                    return;
                }
            }
        }
        reuseTempDTOS.add(reuseTemp);
    }

    @Override
    public void handleUtsDataByCodeAndType(String oldStockCode, String newStockCode, String commandType) {
        log.info("BrokerAnalysisApi handleUtsDataByCode start, oldStockCode: {}, newStockCode: {}, commandType: {}", oldStockCode, newStockCode, commandType);
        TimeInterval timeInterval = new TimeInterval();

        List<BaseMapper<?>> mappers = ListUtil.of(
                // 1. 简况 (Xnhks0101  Xnhks0104 Xnhks0503 Xnhks0105)
                xnhks0101Mapper, xnhks0503Mapper, xnhks0104Mapper, xnhks0105Mapper,
                // 2. 公司概况 (Xnhks0101 Xnhke0101 Xnhks0104 Xnhks0401 Xnhks0106 Xnhks0105)
                xnhke0101Mapper, xnhks0401Mapper, xnhks0106Mapper,
                // 3. 证券资料	(Xnhks0101 Xnhks0501 Xnhks0502 Xnhks0503)
                xnhks0501Mapper, xnhks0502Mapper,
                // 4. 董事高管 (Xnhks0106 Xnhks0301 Xnhks0302)
                xnhks0301Mapper, xnhks0302Mapper,
                // 5. 主营业务 (Xnhks0111 Xnhks0110)
                xnhks0111Mapper, xnhks0110Mapper,
                // 6. 杜邦分析 (Xnhk0201 Xnhk0202 Xnhk0203 Xnhk0204 Xnhk0205 Xnhk0206 Xnhk0207 Xnhk0208 Xnhk0209 Xnhk0210)
                xnhk0201Mapper, xnhk0202Mapper, xnhk0203Mapper, xnhk0204Mapper, xnhk0205Mapper, xnhk0206Mapper, xnhk0207Mapper, xnhk0208Mapper, xnhk0209Mapper, xnhk0210Mapper,
                // 7. 主要股东 (xnhk0129)
                xnhk0129Mapper,
                // 8. 股权变动、主要股东增减持 (xnhks0601)
                xnhks0601Mapper,
                // 9. 股票回购 (Xnhk0602)
                xnhk0602Mapper,
                // 10. 股本结构、股本变动 (xnhk0605 xnhk0114)
                xnhk0605Mapper, xnhk0114Mapper,
                // 11. 分红派息、分红前后10次涨跌幅变化、同行业分红对比 (Xnhks0101 Xnhks0112 Xnhk0102 Xnhk0127)
                xnhks0112Mapper, xnhk0102Mapper, xnhk0127Mapper,
                // 12. 拆股并股 (Xnhks0101 Xnhks0314 Xnhks0319 Xnhks0320)
                xnhks0314Mapper, xnhks0319Mapper, xnhks0320Mapper,
                // 13. 估值对比 (Xnhk0406)
                xnhk0406Mapper,
                // 14. 规模对比、投资回报对比 (Xnhk0406 Xnhk0407)
                xnhk0407Mapper,
                // 15. 公司行动 (xnhk0127 xnhk0201 xnhk0204 xnhk0207 xnhk0307)
                xnhk0307Mapper,
                // 16. 公司动向 (Xnhk0127 Xnhk0201 Xnhk0204 Xnhk0207 Xnhk0311 Xnhks0308 Xnhks0310 Xnhks0314 Xnhks0317 Xnhk0318)
                xnhk0311Mapper, xnhks0308Mapper, xnhks0310Mapper, xnhks0317Mapper, xnhk0318Mapper,
                // 17. 沽空 (Xnhks0101 Xnhk0603)
                xnhk0603Mapper

        );

        if (String.valueOf(SqlCommandType.INSERT).equals(commandType)) {
            mappers.forEach(mapper -> copyUtsDataByMapper(oldStockCode, mapper));
        } else if (String.valueOf(SqlCommandType.UPDATE).equals(commandType)) {
            mappers.forEach(mapper -> updateUtsDataByMapper(oldStockCode, newStockCode, mapper));
        } else if (String.valueOf(SqlCommandType.DELETE).equals(commandType)) {
            mappers.forEach(mapper -> deleteUtsDataByMapper(oldStockCode, mapper));
        }
        log.info("BrokerAnalysisApi handleUtsDataByCode end, oldStockCode: {}, newStockCode: {}, commandType: {}, cost: {}", oldStockCode, newStockCode, commandType, timeInterval.interval() / 1000.0);
    }

    private <T, M extends BaseMapper<T>> void copyUtsDataByMapper(String stockCode, M mapper) {
        try {
            log.info("BrokerAnalysisApi copyUtsDataByMapper start, stockCode: {}, mapper: {}", stockCode, mapper.getClass());
            TimeInterval timeInterval = new TimeInterval();
            String oldCode = StrUtil.replace(stockCode, "-t", "");
            List<T> utsList = mapper.selectList(new QueryWrapper<T>().eq("seccode", oldCode));
            CollUtil.forEach(utsList, (ff, index) -> {
                ReflectUtil.setFieldValue(ff, "seccode", stockCode);
                if (ff instanceof Xnhks0601) {
                    // 防止出现主键重复
                    ReflectUtil.setFieldValue(ff, "f004n", ReflectUtil.getFieldValue(ff, "xdbmask"));
                }
                if (ff instanceof Xnhk0127) {
                    // 防止出现主键重复
                    ReflectUtil.setFieldValue(ff, "id", ReflectUtil.getFieldValue(ff, "xdbmask"));
                }
            });
            Opt.ofEmptyAble(utsList).peek(list -> list.forEach(mapper::insert));
            log.info("BrokerAnalysisApi copyUtsDataByMapper end, stockCode: {}, mapper: {}, cost: {}", stockCode, mapper.getClass(), timeInterval.interval() / 1000.0);
        } catch (Exception e) {
            log.error("BrokerAnalysisApi copyUtsDataByMapper error, stockCode: {}, mapper: {}", stockCode, mapper.getClass(), e);
        }
    }

    private <T, M extends BaseMapper<T>> void updateUtsDataByMapper(String oldCode, String newCode, M mapper) {
        try {
            log.info("BrokerAnalysisApi updateUtsDataByMapper start, oldCode: {}, newCode: {}, mapper: {}", oldCode, newCode, mapper.getClass());
            TimeInterval timeInterval = new TimeInterval();
            int count = mapper.update(null, new UpdateWrapper<T>().set("seccode", newCode).eq("seccode", oldCode));
            log.info("BrokerAnalysisApi updateUtsDataByMapper end, oldCode: {}, newCode: {}, mapper: {}, count:{}, cost: {}", oldCode, newCode, mapper.getClass(), count, timeInterval.interval() / 1000.0);
        } catch (Exception e) {
            log.error("BrokerAnalysisApi updateUtsDataByMapper error, oldCode: {}, newCode: {}, mapper: {}", oldCode, newCode, mapper.getClass(), e);
        }
    }

    private <T, M extends BaseMapper<T>> void deleteUtsDataByMapper(String oldCode, M mapper) {
        try {
            log.info("BrokerAnalysisApi deleteUtsDataByMapper start, oldCode: {}, mapper: {}", oldCode, mapper.getClass());
            TimeInterval timeInterval = new TimeInterval();
            int count = mapper.delete(new QueryWrapper<T>().eq("seccode", oldCode));
            log.info("BrokerAnalysisApi deleteUtsDataByMapper end, oldCode: {}, mapper: {}, count:{}, cost: {}", oldCode, mapper.getClass(), count, timeInterval.interval() / 1000.0);
        } catch (Exception e) {
            log.error("BrokerAnalysisApi deleteUtsDataByMapper error, oldCode: {}, mapper: {}", oldCode, mapper.getClass(), e);
        }
    }

    @Override
    public List<ComFinancialNotifyVO> listFinancialReportList(Date startDate, Date endDate, List<String> codes) {
        if (CollUtil.isEmpty(codes)) {
            return ListUtil.empty();
        }
        return xnhk0201Mapper.listFinancialReportList(DateUtil.formatDateTime(startDate), DateUtil.formatDateTime(endDate), codes);
    }

    @Override
    public List<ComCompanyTrendVo> listNotifyGeneralMeeting(Date date, List<String> codes) {
        if (CollUtil.isEmpty(codes)) {
            return ListUtil.empty();
        }
        Long ymd = DateUtils.formatDateToLong(date, null);
        return xnhks0310Mapper.listNotifyGeneralMeeting(arrayToStr(codes), ymd);
    }

    @Override
    public List<ComCompanyTrendVo> listNotifySuspension(Date startDate, Date endDate, List<String> codes) {
        if (CollUtil.isEmpty(codes)) {
            return ListUtil.empty();
        }
        return xnhks0317Mapper.listNotifySuspension(DateUtil.formatDateTime(startDate), DateUtil.formatDateTime(endDate), codes);
    }

    @Override
    public List<ComCompanyTrendVo> listNotifyResumption(Date startDate, Date endDate, List<String> codes) {
        if (CollUtil.isEmpty(codes)) {
            return ListUtil.empty();
        }
        return xnhk0318Mapper.listNotifyResumption(DateUtil.formatDateTime(startDate), DateUtil.formatDateTime(endDate), codes);
    }

    @Override
    public List<ComCompanyTrendVo> listNotifyParallel(Date date, List<String> codes) {
        if (CollUtil.isEmpty(codes)) {
            return ListUtil.empty();
        }
        // 查询所有并行交易
        Long today = DateUtils.formatDateToLong(date, null);
        // 上个交易日
        BgTradingCalendar beforeTradingCalendar = hkTradingCalendarApi.getBeforeTradingCalendar(DateUtil.toLocalDateTime(date).toLocalDate());
        Long yesterday = Long.valueOf(beforeTradingCalendar.getDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        List<Xnhks0314> xnhks0314s = xnhks0314Mapper.selectList(Wrappers.<Xnhks0314>lambdaQuery().in(Xnhks0314::getSeccode, codes));
        return xnhks0314s.stream().map(xnhk -> {
            ComCompanyTrendVo trendVo = new ComCompanyTrendVo();
            String[] array = StrUtil.splitToArray(xnhk.getF009v(), "-");
            trendVo.setStockCode(xnhk.getSeccode());
            if (ObjectUtil.equal(today, xnhk.getF006d())) {
                // 进入暂停买卖期
                trendVo.setReleaseDate(xnhk.getF006d());
                trendVo.setContent("进入暂停买卖期，临时代码：" + ConcatCodeUtil.concatCodeAndHK(xnhk.getF003v()));
            } else if (ObjectUtil.equal(yesterday, xnhk.getF007d())) {
                // 并行交易结束
                trendVo.setReleaseDate(xnhk.getF007d());
                trendVo.setContent("并行交易结束");
            } else if (ArrayUtil.length(array) >= 1 && StrUtil.equals(String.valueOf(today), StrUtil.replace(array[0], "/", ""))) {
                // 进入并行买卖期
                trendVo.setReleaseDate(Long.valueOf(StrUtil.replace(array[0], "/", "")));
                trendVo.setContent("进入并行买卖期，临时代码：" + ConcatCodeUtil.concatCodeAndHK(xnhk.getF003v()));
            } else {
                return null;
            }
            return trendVo;
        }).filter(ObjectUtil::isNotEmpty).collect(Collectors.toList());
    }
    /**
     * 获取全部股权股票 包含模拟股票
     * @return
     */
    @Override
    public List<StockRightsDTO> getAllStockRightsAndMock() {
        List<StockRightsDTO> allStockRights = xnhks0101Mapper.getAllStockRights();

        List<HkStockScene> hkStockScenes = stockSceneSimulateMapper.selectList(new QueryWrapper<HkStockScene>()
                .eq(HkStockScene.SCENE_TYPE, StockRelationBizEnum.SHAREHOLD.getCode()));
        if(CollectionUtils.isNotEmpty(hkStockScenes)){
            for(HkStockScene hkStockScene : hkStockScenes){
                StockRightsDTO stockRightsDTO = new StockRightsDTO();
                stockRightsDTO.setStartListingDate(hkStockScene.getSceneStartDate());
                stockRightsDTO.setEndListingDate(hkStockScene.getSceneEndDate());
                stockRightsDTO.setCode(hkStockScene.getCode());
                stockRightsDTO.setName(hkStockScene.getCode());
                allStockRights.add(stockRightsDTO);
            }
        }
        buildStockRightsStockId(allStockRights);
        allStockRights.forEach(o->{
            o.setStartListingDate(DateUtils.parseDate(o.getStartListingDate().toString()).getTime());
            o.setEndListingDate(DateUtils.parseDate(o.getEndListingDate().toString()).getTime());
        });
        return allStockRights;
    }

    /**
     * 获取全部股权股票
     * @return
     */
    @Override
    public List<StockRightsDTO> getAllStockRights() {
        List<StockRightsDTO> allStockRights = xnhks0101Mapper.getAllStockRights();
        buildStockRightsStockId(allStockRights);
        allStockRights.forEach(o->{
            o.setStartListingDate(DateUtils.parseDate(o.getStartListingDate().toString()).getTime());
            o.setEndListingDate(DateUtils.parseDate(o.getEndListingDate().toString()).getTime());
        });
        return allStockRights;
    }
    /**
     * 获取指定日期正在交易的股权股票
     * @return
     */
    @Override
    public List<StockRightsDTO> getTradingStockRights(Date time) {
        long currentDate = DateUtils.formatDateToLong(time,null);
        List<StockRightsDTO> tradingStockRights = xnhks0101Mapper.getTradingStockRights(currentDate);
        buildStockRightsStockId(tradingStockRights);
        tradingStockRights.forEach(o->{
            o.setStartListingDate(DateUtils.parseDate(o.getStartListingDate().toString()).getTime());
            o.setEndListingDate(DateUtils.parseDate(o.getEndListingDate().toString()).getTime());
        });
        return tradingStockRights;
    }
    /**
     * 获取指定日期开始交易的股权股票
     * @return
     */
    @Override
    public List<StockRightsDTO> getStartTradingStockRights(Date time) {
        long currentDate = DateUtils.formatDateToLong(time,null);
        List<StockRightsDTO> startTradingStockRights = xnhks0101Mapper.getStartTradingStockRights(currentDate);
        buildStockRightsStockId(startTradingStockRights);
        startTradingStockRights.forEach(o->{
            o.setStartListingDate(DateUtils.parseDate(o.getStartListingDate().toString()).getTime());
            o.setEndListingDate(DateUtils.parseDate(o.getEndListingDate().toString()).getTime());
        });
        return startTradingStockRights;
    }
    /**
     * 获取指定日期结束交易的股权股票
     * @return
     */
    @Override
    public List<StockRightsDTO> getEndTradingStockRights(Date time) {
        long currentDate = DateUtils.formatDateToLong(time,null);
        List<StockRightsDTO> endTradingStockRights = xnhks0101Mapper.getEndTradingStockRights(currentDate);
        buildStockRightsStockId(endTradingStockRights);
        endTradingStockRights.forEach(o->{
            o.setStartListingDate(DateUtils.parseDate(o.getStartListingDate().toString()).getTime());
            o.setEndListingDate(DateUtils.parseDate(o.getEndListingDate().toString()).getTime());
        });
        return endTradingStockRights;
    }
    /**
     * 获取指定日期不再进行交易的股权股票
     * @return
     */
    @Override
    public List<StockRightsDTO> getUnTradingStockRights(Date time) {
        List<StockRightsDTO> allStockRights = xnhks0101Mapper.getAllStockRights();
        long currentDate = DateUtils.formatDateToLong(time,null);
        List<StockRightsDTO> tradingStockRightsList = xnhks0101Mapper.getTradingStockRights(currentDate);
        Set<String> tradingCodes = tradingStockRightsList.stream().map(tradingStockRights -> tradingStockRights.getCode()).collect(Collectors.toSet());
        // 当日码表
        Set<String> newCodes = quotationService.selectStockCodeList();
        allStockRights = allStockRights.stream().filter(o->!tradingCodes.contains(o.getCode()) && (CollUtil.isEmpty(newCodes)||(CollUtil.isNotEmpty(newCodes) &&!newCodes.contains(o.getCode())))).collect(Collectors.toList());
        buildStockRightsStockId(allStockRights);
        allStockRights.forEach(o->{
            o.setStartListingDate(DateUtils.parseDate(o.getStartListingDate().toString()).getTime());
            o.setEndListingDate(DateUtils.parseDate(o.getEndListingDate().toString()).getTime());
        });
        return allStockRights;
    }

    //股权股票stockId赋值
    private void buildStockRightsStockId(List<StockRightsDTO> stockRightsList) {
        if (CollUtil.isNotEmpty(stockRightsList)) {
            List<String> codes = stockRightsList.stream().map(stockRightsDTO -> stockRightsDTO.getCode()).collect(Collectors.toList());
            List<HkStockRelation> stockRelations = hkStockRelationMapper.selectList(Wrappers.<HkStockRelation>lambdaQuery().in(HkStockRelation::getSourceCode, codes));
            if (CollUtil.isNotEmpty(stockRelations)) {
                //原code为股权股票或者代码复用时会有多条数据
                Map<String, List<HkStockRelation>> sourceCodeMap = stockRelations.stream().collect(Collectors.groupingBy(o -> o.getSourceCode()));
                long currentDate = DateUtils.formatDateToLong(new Date(),null);
                stockRightsList.forEach(stockRights->{
                    List<HkStockRelation> stockRightsRelations = sourceCodeMap.get(stockRights.getCode());
                    if (CollUtil.isNotEmpty(stockRightsRelations)) {
                        List<HkStockRelation> relations = stockRightsRelations.stream().filter(stockRightsRelation -> {
                            if ((StringUtils.isNotBlank(stockRightsRelation.getBizTime()) && stockRightsRelation.getBizTime().equals(stockRights.getEndListingDate().toString())) ||
                                    (StringUtils.isBlank(stockRightsRelation.getBizTime()) &&  stockRights.getStartListingDate() <= currentDate && stockRights.getEndListingDate() >= currentDate) ||
                                    (StringUtils.isNotBlank(stockRightsRelation.getBizTime()) && stockRightsRelation.getBizTime().equals(stockRights.getStartListingDate().toString())) ){
                                return true;
                            }
                            return false;
                        }).collect(Collectors.toList());
                        if (CollUtil.isNotEmpty(relations)) {
                            HkStockRelation stockRelation = relations.get(0);
                            stockRights.setStockId(stockRelation.getStockId());
                        }
                    }
                });
            }
        }
    }
}

