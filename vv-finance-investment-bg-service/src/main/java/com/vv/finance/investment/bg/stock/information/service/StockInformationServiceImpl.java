package com.vv.finance.investment.bg.stock.information.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.stuxuhai.jpinyin.ChineseHelper;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.base.utils.ZoneDateUtils;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.dto.QueryComSimpleStockDefineDto;
import com.vv.finance.common.entity.quotation.common.ComSimpleStockDefine;
import com.vv.finance.common.entity.quotation.f10.*;
import com.vv.finance.common.enums.CurrencyTypeEnum;
import com.vv.finance.common.us.utils.UsDateUtils;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.config.StockUtsNoticeConfig;
import com.vv.finance.investment.bg.constants.PublishStatusEnum;
import com.vv.finance.investment.bg.constants.StockUtsNoticeEnum;
import com.vv.finance.investment.bg.dto.info.StockSimpleInfo;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.information.*;
import com.vv.finance.investment.bg.entity.information.enums.TwentyFourHoursTypeEnum;
import com.vv.finance.investment.bg.mapper.stock.quotes.CompanyTrandsMergeMapper;
import com.vv.finance.investment.bg.mapper.stock.quotes.StockNewsMapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0101Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0112Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0503Mapper;
import com.vv.finance.investment.bg.mongo.model.StockUtsNoticeEntity;
import com.vv.finance.investment.bg.stock.f10.service.impl.AbstractBaseServiceImpl;
import com.vv.finance.investment.bg.stock.information.InformationConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.vv.finance.common.constants.RedisKeyConstants.RECEIVER_NEWEST_STOCK_CODE_NAME_SET;

/**
 * @Author: wsliang
 * @Date: 2021/9/14 14:20
 **/
@Service
@Slf4j
public class StockInformationServiceImpl extends AbstractBaseServiceImpl {

    @Resource
    private StockNewsMapper stockNewsMapper;

    @Resource
    private Xnhks0503Mapper xnhks0503Mapper;

    @Resource
    private Xnhks0101Mapper xnhks0101Mapper;

    @Resource
    private RedisClient redisClient;

    private static final String STOCK_SUFFIX = ".hk";

    private static final String QUERY_TIME_PATTERN = "yyyy-MM-dd";

    private static final String UNDERWRITER_MARK = "包销商";

    private static final String SPACE_MARK = "<br>";

    @Value("${stock.uts.notice.prefix}")
    String prefix;

    @Value("${free.maxFreeStockNum:10}")
    private Integer maxFreeStockNum;

    @Resource
    private Xnhks0112Mapper xnhks0112Mapper;

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    private StockUtsNoticeConfig stockUtsNoticeConfig;

    @Resource
    private CompanyTrandsMergeMapper companyTrandsMergeMapper;

    @Resource
    StockInfoApi stockInfoApi;

    @Resource
    HkTradingCalendarApi hkTradingCalendarApi;

    public PageWithTime<GroupTwentyFourHourNewsVo> pageTwentyFourHourNews(NewsPageReq pageReq, Integer newsType) {
        Long id = pageReq.getId();
        Long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        Long pageSize = pageReq.getPageSize() == null ? 20 : pageReq.getPageSize();

        // 查询条件 1：资讯类型
        // 查询方式 1：查询最新资讯 2：翻页查询历史数据
        Page<StockNewsEntity> page = new Page(currentPage, pageSize);
        Page<StockNewsEntity> result;

        QueryWrapper<StockNewsEntity> wrapper = new QueryWrapper<StockNewsEntity>()
                .eq(InformationConstant.PUBLISH_STATUS, PublishStatusEnum.YES)
                .eq(InformationConstant.TOP_CATEGORY, InformationConstant.TABLE_NEWS24HOURS)
                .le(ObjectUtils.isNotEmpty(id), InformationConstant.COLUMN_ID, id)
//                .gt(ObjectUtils.isNotEmpty(id) && queryType.equals(QueryType.NEW.getType()), InformationConstant.COLUMN_ID, id)
                .eq(ObjectUtils.isNotEmpty(newsType) && !newsType.equals(TwentyFourHoursTypeEnum.ZERO.getCode()), InformationConstant.SECOND_CATEGORY, TwentyFourHoursTypeEnum.getValue(newsType))
                .orderByDesc(InformationConstant.DATE_TIME); //根据dateTime 倒排
        result = stockNewsMapper.selectPage(page, wrapper);

        PageWithTime<GroupTwentyFourHourNewsVo> pageDomain = new PageWithTime<>();
        pageDomain.setTotal(result.getTotal());
        pageDomain.setCurrent(currentPage);
        pageDomain.setSize(result.getSize());
        if (CollectionUtils.isNotEmpty(result.getRecords())) {
            List<GroupTwentyFourHourNewsVo> records = new ArrayList<>();
            result.getRecords().stream().collect(Collectors.groupingBy(StockNewsEntity::getDate)).forEach((k, v) -> {
                records.add(buildTwentyFourHourNewsByEntity(k, v));
            });
            records.sort(Comparator.comparing(GroupTwentyFourHourNewsVo::getDate, Comparator.reverseOrder()));
            pageDomain.setRecords(records);
        }
        return pageDomain;
    }

    private GroupTwentyFourHourNewsVo buildTwentyFourHourNewsByEntity(LocalDate k, List<StockNewsEntity> v) {
        GroupTwentyFourHourNewsVo twentyFourHourNewsVo1 = new GroupTwentyFourHourNewsVo();
        List<TwentyFourHourNewsVo> voList = new ArrayList<>();
        twentyFourHourNewsVo1.setDate(k.atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        twentyFourHourNewsVo1.setList(voList);
        v.forEach(stockNewsEntity -> {
            TwentyFourHourNewsVo vo = new TwentyFourHourNewsVo();
            vo.setNewsid(stockNewsEntity.getNewsId());
            vo.setTime(stockNewsEntity.getDateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli() + "");
            vo.setText(stockNewsEntity.getContent());
            vo.setType(stockNewsEntity.getSecondCategory());
            vo.setId(String.valueOf(stockNewsEntity.getId()));
            voList.add(vo);
        });
        return twentyFourHourNewsVo1;
    }

    /**
     * 获取财经资讯列表
     *
     * @return
     */
    public PageWithTime<GroupFinancialEventVo> pageFinancialEvent(ComCalendarNewsPageReq pageReq) {
        Long id = pageReq.getId();
        List<String> areaCodes = pageReq.getArea();
        List<String> levelValue = InformationConstant.getLevelValue(pageReq.getLevel());
        String startTime = StringUtils.isEmpty(pageReq.getStartTime()) ? "" : pageReq.getStartTime().replace("/","-");
        String endTime = StringUtils.isEmpty(pageReq.getEndTime()) ? "" : pageReq.getEndTime().replace("/","-");
        long pageSize = pageReq.getPageSize();
        long currentPage = pageReq.getCurrentPage();

        // 查询条件 1：地区 2：重要等级 3：起止时间
        // 查询方式 1：查询最新资讯 2：翻页查询历史数据
        // 排序 1：datetime
        Page<StockNewsEntity> page = new Page<>(currentPage, pageSize);
        QueryWrapper<StockNewsEntity> wrapper = new QueryWrapper<StockNewsEntity>()
                .eq(InformationConstant.PUBLISH_STATUS, PublishStatusEnum.YES)
                .eq(InformationConstant.TOP_CATEGORY, InformationConstant.TABLE_CALENDAR)
                .eq(InformationConstant.SECOND_CATEGORY, InformationConstant.SECOND_CATEGORY_0)
                .ge(StringUtils.isNotBlank(startTime), InformationConstant.DATE, startTime)
                .le(StringUtils.isNotBlank(endTime), InformationConstant.DATE, endTime)
                .le(ObjectUtils.isNotEmpty(id), InformationConstant.COLUMN_ID, id)
                .orderByAsc(InformationConstant.NEWS_ID);
        if (CollectionUtils.isNotEmpty(levelValue)) {
            wrapper.lambda().and(wq -> {
                for (String value : levelValue) {
                    wq.like(StringUtils.isNotBlank(value), StockNewsEntity::getContent, value).or();
                }
            });
        }
        if (CollectionUtils.isNotEmpty(areaCodes)) {
            List<String> strings = stockNewsMapper.listCountryName(areaCodes);
            if (CollectionUtils.isNotEmpty(strings)) {
                wrapper.lambda().and(wq -> {
                    for (String value : strings) {
                        wq.like(StringUtils.isNotBlank(value), StockNewsEntity::getContent, value).or();
                    }
                });
            }
        }
        Page<StockNewsEntity> result = stockNewsMapper.selectPage(page, wrapper);
        PageWithTime<GroupFinancialEventVo> pageDomain = new PageWithTime<>();
        pageDomain.setTotal(result.getTotal());
        pageDomain.setCurrent(currentPage);
        List<StockNewsEntity> entityList = result.getRecords();

        List<Long> ids = entityList.stream().map(StockNewsEntity::getId).collect(Collectors.toList());
        List<String> areaByIds = stockNewsMapper.queryAreaByIds(arrayToStr(ids));
        Map<String, String> areaMap = areaByIds.stream().filter(item -> StringUtils.isNotBlank(item)).map(item -> item.split(StringPool.COMMA)).collect(Collectors.toMap(item -> item[0], item -> item[1]));

        pageDomain.setSize(entityList.size());
        List<GroupFinancialEventVo> records = new ArrayList<>();
        entityList.stream().collect(Collectors.groupingBy(StockNewsEntity::getDate)).forEach((k, v) -> {
            records.add(buildFinancialEventByEntity(k, v, areaMap));
        });
//        records.sort(Comparator.comparing(GroupFinancialEventVo::getDate, Comparator.reverseOrder()));
        records.sort(Comparator.comparing(GroupFinancialEventVo::getDate));
        pageDomain.setRecords(records);
        return pageDomain;
    }

    private GroupFinancialEventVo buildFinancialEventByEntity(LocalDate k, List<StockNewsEntity> v, Map<String, String> areaMap) {
        GroupFinancialEventVo groupFinancialEventVo = new GroupFinancialEventVo();
        List<FinancialEventVo> financialEventVos = new ArrayList<>();
        groupFinancialEventVo.setList(financialEventVos);
        groupFinancialEventVo.setDate(k.atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        v.forEach(stockNewsEntity -> {
            FinancialEventVo vo = new FinancialEventVo();
            vo.setNewsid(stockNewsEntity.getNewsId());
            vo.setTime(stockNewsEntity.getDateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
            analyzeContent(vo, stockNewsEntity.getContent());
            vo.setId(String.valueOf(stockNewsEntity.getId()));
            vo.setAreaName(areaMap.getOrDefault(vo.getId(), ""));
            financialEventVos.add(vo);
        });
        return groupFinancialEventVo;
    }

    /**
     * 文本解析
     *
     * @param vo
     * @param content
     */
    private static void analyzeContent(FinancialEventVo vo, String content) {
        if (StringUtils.isBlank(content)) {
            return;
        }
        int endIndex = content.indexOf("【重要性】");
        vo.setText(content.substring(0, endIndex));
        int endIndex0 = content.indexOf("；【市场预测】");
        String substring0 = content.substring(endIndex, endIndex0);
        vo.setLevel(substring0.contains("低") ? 1L : substring0.contains("中") ? 2L : substring0.contains("高") ? 3L : 0L);
        int endIndex1 = content.indexOf("；【前值】");
        String substring1 = content.substring(endIndex0, endIndex1);
        vo.setPrediction(substring1.split("：")[1]);
        int endIndex2 = content.indexOf("；【公布值】");
        String substring2 = content.substring(endIndex1, endIndex2);
        vo.setLastValue(substring2.split("：")[1]);
        String substring3 = content.substring(endIndex2, content.lastIndexOf("。"));
        vo.setPublishValue(substring3.split("：")[1]);
    }

    public ComPageNewStockRes<ComNewShareVo> pageNewShare(ComCalendarNewsPageReq pageReq) {
        long pageSize = pageReq.getPageSize() == null ? 20 : pageReq.getPageSize();
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        Page<ComNewShareVo> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        // 将这个records 与全量码表过滤
        List<ComStockSimpleDto> stockSimpleInfos =  stockInfoApi.getStockSimpleInfoLists();
        Map<String, ComStockSimpleDto> stockSimpleInfoMap = stockSimpleInfos.stream().collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
        List<String> codeList = stockSimpleInfos.stream().map(ComStockSimpleDto::getCode).collect(Collectors.toList());

        List<ComNewShareVo> records = xnhks0503Mapper.listNewShareVo(codeList);
        // 判断 records 中 上市日期等于当天，需要再过滤全量码表
        Long time = ZoneDateUtils.localDate2Date(LocalDate.now(),ZoneDateUtils.Asia_HongKong).getTime();
        records = records.stream().filter(item -> {
            return (!time.equals(item.getMarketDate()) || (time.equals(item.getMarketDate()) && codeList.contains(item.getStockCode())));
        }).collect(Collectors.toList());

        ComPageNewStockRes<ComNewShareVo> pageDomain = new ComPageNewStockRes<>();
        pageDomain.setCurrent(currentPage);
        pageDomain.setSize(records.size());
        pageDomain.setTotal(records.size());
        if (CollectionUtils.isNotEmpty(records)) {
            records = records.stream().distinct().collect(Collectors.toList());
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minutes = cal.get(Calendar.MINUTE);
            Boolean openPlate = !(hour < 9 || (hour == 9 && minutes <= 30));
            List<Long> longs = new ArrayList<>();
            List<Integer> codes = records.stream().map(r -> Integer.valueOf(r.getSECCODE().replace(STOCK_SUFFIX, ""))).collect(Collectors.toList());
            List<StockUtsNoticeEntity> stockUtsNoticeEntities = listIpoPDF(codes);
            for (ComNewShareVo record : records) {
//                record.setStockName(stockSimpleInfoMap.get(record.getStockCode()) != null ? stockSimpleInfoMap.get(record.getStockCode()).getStockName() : "");
                record.setExchange(stockSimpleInfoMap.get(record.getStockCode()) != null ? stockSimpleInfoMap.get(record.getStockCode()).getExchange() : "");
                record.setStockType(stockSimpleInfoMap.get(record.getStockCode()) != null ? stockSimpleInfoMap.get(record.getStockCode()).getStockType() : null);
                record.setRegionType(RegionTypeEnum.HK.getCode());

                // 设置股票名称，码表优先，否则使用uts表
                if(CollUtil.isNotEmpty(stockSimpleInfoMap)){
                    ComStockSimpleDto comStockSimpleDto = stockSimpleInfoMap.get(record.getStockCode());
                    if(Objects.nonNull(comStockSimpleDto) && StringUtils.isNotBlank(comStockSimpleDto.getStockName())){
                        record.setStockName(comStockSimpleDto.getStockName());
                        record.setStockId(comStockSimpleDto.getStockId());
                    }
                }
                //1.2.54 F005D（开始时间）~F006D(结束时间)
                //当其中某一值为空时，显示规则：
                //F005D为空：F006D~F006D
                //F006D为空：F005D~F005D
                if (record.getSubscribeStartDate() == null && record.getSubscribeEndDate() != null) {
                    record.setSubscribeStartDate(record.getSubscribeEndDate());
                } else if (record.getSubscribeStartDate() != null && record.getSubscribeEndDate() == null) {
                    record.setSubscribeEndDate(record.getSubscribeStartDate());
                }
                boolean add = record.getSubscribeStartDate() <= 0L ? false : longs.add(record.getSubscribeStartDate());
                add = record.getSubscribeEndDate() <= 0L ? false : longs.add(record.getSubscribeEndDate());
                if (record.getPublicityDate() != null) {
                    add = record.getPublicityDate() <= 0L ? false : longs.add(record.getPublicityDate());
                }
                add = record.getMarketDate() != null && record.getMarketDate() <= 0L ? false : longs.add(record.getMarketDate());
                getMarketStatus(record, LocalDateTimeUtil.getTimestamp(LocalDate.now(), ZoneOffset.of("+8")), openPlate);
                record.setStockLink(buildPDF(stockUtsNoticeEntities, record.getSECCODE()));
            }
            if (CollectionUtils.isNotEmpty(longs)) {
                Collections.sort(longs);
                pageDomain.setDateList(listEveryDay(longs.get(0), longs.get(longs.size() - 1)));
            }
            pageDomain.setSize(records.size());
            pageDomain.setTotal(records.size());
        }
        pageDomain.setRecords(records);
        return pageDomain;
    }

    public ComPageWithTime<ComInformationGroupVo<ComNewShareVo>> pageNewShare4App(ComCalendarNewsPageReq pageReq) {
        ComPageWithTime<ComInformationGroupVo<ComNewShareVo>> pageDomain = new ComPageNewStockRes<>();


        long pageSize = pageReq.getPageSize() == null ? 20 : pageReq.getPageSize();
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        Page<NewShareVo> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        String startTime = StringUtils.isEmpty(pageReq.getStartTime()) ? DateUtils.formatDate(new Date(), "yyyyMMdd") : pageReq.getStartTime().replace("/", "");
        String endTime = StringUtils.isEmpty(pageReq.getEndTime()) ? "" : pageReq.getEndTime().replace("/", "");
        // 将这个records 与全量码表过滤
        List<ComStockSimpleDto> stockSimpleInfos = stockInfoApi.getStockSimpleInfoLists();
        Map<String, ComStockSimpleDto> stockSimpleInfoMap = stockSimpleInfos.stream().collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
        List<String> codeList = stockSimpleInfos.stream().map(ComStockSimpleDto::getCode).collect(Collectors.toList());

        Page<ComNewShareVo> newShareVoPage = xnhks0503Mapper.listNewShareVoByTime(startTime, endTime, codeList, page);
        List<ComNewShareVo> records = newShareVoPage.getRecords();
        // 判断 records 中 上市日期等于当天，需要再过滤全量码表
        Long time = ZoneDateUtils.localDate2Date(LocalDate.now(),ZoneDateUtils.Asia_HongKong).getTime();
        records = records.stream().filter(item -> {
            return (!time.equals(item.getMarketDate()) || (time.equals(item.getMarketDate()) && codeList.contains(item.getStockCode())));
        }).collect(Collectors.toList());
        pageDomain.setTotal(newShareVoPage.getTotal());
        if (CollectionUtils.isEmpty(records)) {
            return pageDomain;
        }
        List<ComInformationGroupVo<ComNewShareVo>> result = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        Boolean openPlate = !(hour < 9 || (hour == 9 && minutes <= 30));
        List<Long> longs = new ArrayList<>();
        List<Integer> codes = records.stream().map(r -> Integer.valueOf(r.getSECCODE().replace(STOCK_SUFFIX, ""))).collect(Collectors.toList());
        List<StockUtsNoticeEntity> stockUtsNoticeEntities = listIpoPDF(codes);
        records.stream().map(record -> {

            record.setExchange(stockSimpleInfoMap.get(record.getStockCode()) != null ? stockSimpleInfoMap.get(record.getStockCode()).getExchange() : "");
            record.setStockType(stockSimpleInfoMap.get(record.getStockCode()) != null ? stockSimpleInfoMap.get(record.getStockCode()).getStockType() : null);
            record.setRegionType(RegionTypeEnum.HK.getCode());

            // 设置股票名称，码表优先，否则使用uts表
            if(CollUtil.isNotEmpty(stockSimpleInfoMap)){
                ComStockSimpleDto comStockSimpleDto = stockSimpleInfoMap.get(record.getStockCode());
                if(Objects.nonNull(comStockSimpleDto) && StringUtils.isNotBlank(comStockSimpleDto.getStockName())){
                    record.setStockName(comStockSimpleDto.getStockName());
                }
            }
            //1.2.54 F005D（开始时间）~F006D(结束时间)
            //当其中某一值为空时，显示规则：
            //F005D为空：F006D~F006D
            //F006D为空：F005D~F005D
            if (record.getSubscribeStartDate() == null && record.getSubscribeEndDate() != null) {
                record.setSubscribeStartDate(record.getSubscribeEndDate());
            } else if (record.getSubscribeStartDate() != null && record.getSubscribeEndDate() == null) {
                record.setSubscribeEndDate(record.getSubscribeStartDate());
            }
//                boolean add = record.getSubscribeStartDate() <= 0L ? false : longs.add(record.getSubscribeStartDate());
//                add = record.getSubscribeEndDate() <= 0L ? false : longs.add(record.getSubscribeEndDate());
//                if (record.getPublicityDate() != null) {
//                    add = record.getPublicityDate() <= 0L ? false : longs.add(record.getPublicityDate());
//                }
//                add = record.getMarketDate() != null && record.getMarketDate() <= 0L ? false : longs.add(record.getMarketDate());
            getComMarketStatus(record, record.getOrderDate(), openPlate);
            record.setStockLink(buildPDF(stockUtsNoticeEntities, record.getSECCODE()));
            return record;
        }).collect(Collectors.groupingBy(ComNewShareVo::getOrderDate)).forEach((k, v) -> {
            result.add(buildGroupVo(k, v));
        });
        result.sort(Comparator.comparing(ComInformationGroupVo::getDate));
        pageDomain.setSize(records.size());
        pageDomain.setRecords(result);
        return pageDomain;
    }

    private <T> ComInformationGroupVo<T> buildGroupVo(Long k, List<T> v) {
        ComInformationGroupVo<T> informationGroupVo = new ComInformationGroupVo<>();
        informationGroupVo.setList(v);
        informationGroupVo.setDate(k);
        return informationGroupVo;
    }

    public List<ComNewShareVo> listNewShare4Check() {
        BgTradingCalendar tradingCalendar = hkTradingCalendarApi.getNextTradingCalendar(LocalDate.now());
        String dateStr = tradingCalendar.getDate().toString().replaceAll("-", "");
        return xnhks0503Mapper.listNewShare(dateStr);
    }

    /**
     * 拼接pdf链接
     *
     * @param stockUtsNoticeEntities
     * @param seccode
     * @return
     */
    public String buildPDF(List<StockUtsNoticeEntity> stockUtsNoticeEntities, String seccode) {
        if (CollectionUtils.isEmpty(stockUtsNoticeEntities)) {
            return "";
        }
        StockUtsNoticeEntity entity = stockUtsNoticeEntities.stream().filter(s -> s.getStockCode().equals(Integer.valueOf(seccode.replace(STOCK_SUFFIX, "")))).findFirst().orElse(null);
        if (ObjectUtils.isNotEmpty(entity) && !entity.getFileName().contains(".HTM")) {
            return prefix.concat(entity.getDirs()).concat("-").concat(entity.getFileName().toLowerCase());
        }
        return "";
    }

    /**
     * 获取ipo招股说明书
     *
     * @param codes
     * @return
     */
    public List<StockUtsNoticeEntity> listIpoPDF(List<Integer> codes) {
        String[] strs = {"白色申請表格", "黃色申請表格", "綠色申請表格", "藍色申請表格", "粉紅色申請表格"};
        Query pageRequest = Query.query(Criteria.where("stockCode").in(codes).and("categoryId").is("30000").and("attachmentNum").ne(0).and("headLine").nin(strs).and("language").is(2)).with(Sort.by(Sort.Direction.DESC, "id"));
        List<StockUtsNoticeEntity> noticeEntities = mongoTemplate.find(pageRequest, StockUtsNoticeEntity.class);
        return noticeEntities;
    }


    /**
     * 组建投资者信息
     */
    public NewStockInvestors getInvestorsInformation(String stockCode) {
        // 基石投资者 0507
        List<NewShareInvestorInfo> newShareInvestorInfos = xnhks0503Mapper.queryInvestorsByStockCodes(stockCode);
        // 保荐人 0501
        String sponsors = xnhks0503Mapper.querySponsor(stockCode);
        // 承销商 0505
        Map<String, String> underwriterInfos = xnhks0503Mapper.queryUnderwriters(stockCode);
        NewStockInvestors newStockInvestors = NewStockInvestors.builder().investorInfos(newShareInvestorInfos)
                .sponsor(StringUtils.isBlank(sponsors) ? Collections.emptyList() : Arrays.asList(sponsors.split(SPACE_MARK)))
                .underwriter(underwriterInfos != null ? getUnderwriterInfos(underwriterInfos) : Collections.emptyList())
                .build();
        return newStockInvestors;
    }

    private List<String> getUnderwriterInfos(Map<String, String> underwriterInfos) {
        List<String> underwriter = new ArrayList<>();
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F004V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F005V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F006V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F007V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F008V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F009V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F010V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F011V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F012V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F013V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F014V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F015V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F016V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F017V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F018V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F019V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F020V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F021V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F022V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F023V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F024V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F025V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F026V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F027V", "").split(SPACE_MARK)));
        }
        if (UNDERWRITER_MARK.equals(underwriterInfos.getOrDefault("F028V", ""))) {
            underwriter.addAll(Arrays.asList(underwriterInfos.getOrDefault("F029V", "").split(SPACE_MARK)));
        }
        return underwriter;
    }

    /**
     * 返回每一天
     *
     * @return
     */
    private List<Long> listEveryDay(Long pageMinTime, Long pageMaxTime) {
        List<Long> dateList = new ArrayList<>();
        Long date = pageMinTime;
        Integer i = 0;
        // 最少天数
        Integer minDays = 10;
        while ((date <= pageMaxTime && i < 2) || dateList.size() < minDays) {
            if (DateUtils.isWorkingDay(date)) {
                dateList.add(date);
                if (date.equals(pageMaxTime)) {
                    i++;
                }
            }
            if (date.equals(pageMaxTime)) {
                pageMaxTime += 86400000;
            }
            date += 86400000;
        }
        return dateList;
    }

    /**
     * 插入上市状态以及跳转状态
     */
    private void getMarketStatus(ComNewShareVo newShareVo, Long currentTimeMillis, Boolean openPlate) {
        Long subscribeStartDate = newShareVo.getSubscribeStartDate();
        Long subscribeEndDate = newShareVo.getSubscribeEndDate();
        Long publicityDate = newShareVo.getPublicityDate();
        Long marketDate = newShareVo.getMarketDate();
        if (subscribeStartDate <= currentTimeMillis && subscribeEndDate >= currentTimeMillis) {
            newShareVo.setMarketStatus(NewShareVo.MarketStatus.ZERO.getCode());
        } else if (publicityDate == null || subscribeEndDate < currentTimeMillis && publicityDate > currentTimeMillis) {
            newShareVo.setMarketStatus(NewShareVo.MarketStatus.ONE.getCode());
        } else if (publicityDate <= currentTimeMillis && marketDate > currentTimeMillis) {
            newShareVo.setMarketStatus(NewShareVo.MarketStatus.TWO.getCode());
        } else if (marketDate <= currentTimeMillis) {
            newShareVo.setMarketStatus(NewShareVo.MarketStatus.THREE.getCode());
            newShareVo.setIsMarket(openPlate);
        }

    }

    /**
     * 插入上市状态以及跳转状态
     */
    private void getComMarketStatus(ComNewShareVo newShareVo, Long currentTimeMillis, Boolean openPlate) {
        Long subscribeStartDate = newShareVo.getSubscribeStartDate();
        Long subscribeEndDate = newShareVo.getSubscribeEndDate();
        Long publicityDate = newShareVo.getPublicityDate();
        Long marketDate = newShareVo.getMarketDate();
        if (subscribeStartDate.equals(currentTimeMillis)) {
            newShareVo.setMarketStatus(ComNewShareVo.MarketStatus.SUBSCRIBESTART.getCode());
        }else if (subscribeEndDate.equals(currentTimeMillis)) {
            newShareVo.setMarketStatus(ComNewShareVo.MarketStatus.ONE.getCode());
        } else if (publicityDate.equals(currentTimeMillis)) {
            newShareVo.setMarketStatus(ComNewShareVo.MarketStatus.TWO.getCode());
        } else if (marketDate.equals(currentTimeMillis)) {
            newShareVo.setMarketStatus(ComNewShareVo.MarketStatus.THREE.getCode());
            newShareVo.setIsMarket(openPlate);
        }

    }

    /**
     * 查询除权数据
     *
     * @param pageReq
     * @param stocks
     * @return
     */
    public PageWithTime<GroupExitRightNewsVo> pageExitRightNews(CalendarNewsPageReq pageReq, List<String> stocks) {
        Long startTime = ObjectUtils.isEmpty(pageReq.getStartTime()) ? LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.of("+8")) : pageReq.getStartTime() / 1000;
        Long endTime = ObjectUtils.isEmpty(pageReq.getEndTime()) ? null : pageReq.getEndTime() / 1000;
        long pageSize = pageReq.getPageSize();
        long currentPage = pageReq.getCurrentPage();

        // 查询条件 1：地区 2：重要等级 3：起止时间
        // 查询方式 1：查询最新资讯 2：翻页查询历史数据
        // 排序 1：datetime
        Page<ExitRightVo> page = new Page<>(currentPage, pageSize);
        Page<ExitRightVo> exitRightVoPage;
        Map<String, String> stringStringMap = selectStockNameList();
        if (CollectionUtils.isEmpty(pageReq.getGroupTypes()) || pageReq.getGroupTypes().contains(100)) {
            exitRightVoPage = xnhks0112Mapper.pageOldExitRight(page, startTime, endTime, arrayToStr(stringStringMap.keySet()));
        } else {
            exitRightVoPage = xnhks0112Mapper.pageOldExitRight(page, startTime, endTime, arrayToStr(stocks));
        }

        List<ExitRightVo> exitRightVos = exitRightVoPage.getRecords();
        List<GroupExitRightNewsVo> records = new ArrayList<>();
        exitRightVos.stream().collect(Collectors.groupingBy(ExitRightVo::getDividendDate)).forEach((k, v) -> {
            records.add(buildGroupExitRightVo(k, v, stringStringMap));
        });
        records.sort(Comparator.comparing(GroupExitRightNewsVo::getDate));

        PageWithTime<GroupExitRightNewsVo> pageDomain = new PageWithTime<>();
        pageDomain.setTotal(exitRightVoPage.getTotal());
        pageDomain.setCurrent(currentPage);
        pageDomain.setSize(exitRightVos.size());
        pageDomain.setRecords(records);
        return pageDomain;
    }

    private GroupExitRightNewsVo buildGroupExitRightVo(Long k, List<ExitRightVo> v, Map<String, String> stockNameMap) {
        GroupExitRightNewsVo groupExitRightNewsVo = new GroupExitRightNewsVo();
        groupExitRightNewsVo.setList(v);
        Long date = Long.valueOf(k + "000");
        groupExitRightNewsVo.setDate(date);
        v.forEach(vo -> {
            vo.setStockName(stockNameMap.get(vo.getSECCODE()));
            vo.setDividendDate(date);
            vo.setDividend(StringUtils.isNotBlank(vo.getDividend()) ? vo.getDividend().replace(SPACE_MARK, " ") : "");
            vo.setDate(date);
        });
        return groupExitRightNewsVo;
    }

    private <T> String arrayToStr(Collection<T> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return "('')";
        }
        String join = StringUtils.join(codes, "','");
        return "('" + join + "')";
    }

    public Map<String, String> selectStockNameList() {

        Set<String> strings = redisClient.get(RECEIVER_NEWEST_STOCK_CODE_NAME_SET);
        Map<String, String> stockCodeMap = strings.stream()
                .collect(Collectors.toMap(item -> item.split(",")[0], item -> item.split(",")[1]));
        return stockCodeMap;
    }

    private Set<Integer> getNoticeCode(Collection<String> codes) {
        Set<Integer> collect = codes.stream().map(str -> Integer.valueOf(str.replaceFirst(STOCK_SUFFIX, ""))).collect(Collectors.toSet());
        return collect;
    }

    /**
     * 查询财报
     */
    public PageWithTime<InformationGroupVo<FinancialReportVo>> queryNotice(List<String> code, CalendarNewsPageReq pageReq) {
        Long pageSize = pageReq.getPageSize();
        Long currentPage = pageReq.getCurrentPage() - 1;
        String startTime = ObjectUtils.isEmpty(pageReq.getStartTime()) ? null : DateUtils.formatDate(pageReq.getStartTime(), QUERY_TIME_PATTERN);
        String endTime = ObjectUtils.isEmpty(pageReq.getEndTime()) ? null : DateUtils.formatDate(pageReq.getEndTime(), QUERY_TIME_PATTERN);
        Map<String, List<String>> cacheMap = stockUtsNoticeConfig.getCacheMap();
        List<String> types = cacheMap.get(Objects.requireNonNull(StockUtsNoticeEnum.getByCode(StockUtsNoticeEnum.FINANCIAL_REPORT.getCode())).getOperation());

        Criteria criteria = Criteria.where(InformationConstant.COL_LANGUAGE).is(2).and(InformationConstant.COL_CATEGORT).in(types);

        Map<String, String> stockNameList = selectStockNameList();
        if (CollectionUtils.isNotEmpty(pageReq.getGroupTypes()) && CollectionUtils.isNotEmpty(code)) {
            criteria.and(InformationConstant.COL_STOCKCODE).in(getNoticeCode(code));
//        } else if (CollectionUtils.isEmpty(pageReq.getGroupTypes())) {
//            criteria.and(InformationConstant.COL_STOCKCODE).in(getNoticeCode(stockNameList.keySet()));
        }

        Criteria gte = null;
        if (StringUtils.isNotBlank(startTime)) {
            Date date = DateUtils.parseDate(startTime);
            String yyyyMMdd = DateUtils.formatDate(date, "yyyyMMdd");
            String HHmmss = "000000";
            gte = Criteria.where(InformationConstant.COL_DATE_LINE).gte(yyyyMMdd + "T" + HHmmss);
        }
        Criteria lt = null;
        if (StringUtils.isNotBlank(endTime)) {
            Date date = DateUtils.parseDate(endTime);
            String yyyyMMdd = DateUtils.formatDate(date, "yyyyMMdd");
            String HHmmss = "235959";
            lt = Criteria.where(InformationConstant.COL_DATE_LINE).lte(yyyyMMdd + "T" + HHmmss);
        }
        if (gte != null && lt != null) {
            criteria.andOperator(gte, lt);
        } else if (gte != null) {
            criteria.andOperator(gte);
        } else if (lt != null) {
            criteria.andOperator(lt);
        }

        MatchOperation match = Aggregation.match(criteria);
        GroupOperation group = Aggregation.group("lineId");
        Field[] fields = StockUtsNoticeEntity.class.getDeclaredFields();
        List<String> fieldNames = new ArrayList<>(11);
        for (Field field : fields) {
            if (!"serialVersionUID".equals(field.getName())) {
                group = group.first(field.getName()).as(field.getName());
                fieldNames.add(field.getName());
            }
        }

        ProjectionOperation resultFields = Aggregation.project(fieldNames.toArray(new String[fieldNames.size()]));
        SortOperation sort = Aggregation.sort(Sort.Direction.DESC, InformationConstant.COL_DATE_LINE);
        SkipOperation skip = Aggregation.skip(currentPage * pageSize);
        LimitOperation limit = Aggregation.limit(pageSize);
        AggregationResults<StockUtsNoticeEntity> aggregationResults =
                mongoTemplate.aggregate(Aggregation.newAggregation(match, group, resultFields, sort, skip, limit), "stock_uts_notice", StockUtsNoticeEntity.class);
        PageWithTime<InformationGroupVo<FinancialReportVo>> pageDomain = new PageWithTime<>();
        pageDomain.setCurrent(currentPage + 1);
        List<StockUtsNoticeEntity> noticeEntities = aggregationResults.getMappedResults();
        if (CollectionUtils.isEmpty(noticeEntities)) {
            return pageDomain;
        }
        GroupOperation group1 = Aggregation.group("lineId");
        GroupOperation group2 = Aggregation.group("lineId").count().as("total");
        ProjectionOperation resultTotal = Aggregation.project("total");
        AggregationResults<Map> countResults =
                mongoTemplate.aggregate(Aggregation.newAggregation(match, group1, group2, resultTotal), "stock_uts_notice", Map.class);
        long count = Long.valueOf(countResults.getMappedResults().get(0).getOrDefault("total", 0) + "");
        pageDomain.setTotal(count);

        DecimalFormat df = new DecimalFormat("00000");
        List<InformationGroupVo<FinancialReportVo>> records = new ArrayList<>();
        noticeEntities.stream()/*.filter(entity -> stockNameList.containsKey(df.format(entity.getStockCode()) + STOCK_SUFFIX))*/.map(entity -> {
            FinancialReportVo financialReportVo = new FinancialReportVo();
            financialReportVo.setText(StringUtils.isBlank(entity.getHeadLine()) ? "" : ChineseHelper.convertToSimplifiedChinese(entity.getHeadLine()));
            if (!entity.getFileName().contains(".HTM")) {
                financialReportVo.setFinancialReportLink(prefix.concat(entity.getDirs()).concat("-").concat(entity.getFileName().toLowerCase()));
            }
            financialReportVo.setDateTime(StringUtils.substringBefore(entity.getDateLine(), "T"));
            financialReportVo.setDate(DateUtils.parseDate(financialReportVo.getDateTime()).getTime());
            String seccode = df.format(entity.getStockCode()) + STOCK_SUFFIX;
            financialReportVo.setStockName(stockNameList.getOrDefault(seccode,""));
            financialReportVo.setSECCODE(seccode);
            financialReportVo.setNewsid(entity.getLineId());
            return financialReportVo;
        }).collect(Collectors.groupingBy(FinancialReportVo::getDate)).forEach((k, v) -> {
            records.add(buildGroupFinancialReportVo(k, v));
        });
        records.sort(Comparator.comparing(InformationGroupVo::getDate, Comparator.reverseOrder()));
        pageDomain.setSize(noticeEntities.size());
        pageDomain.setRecords(records);
        pageDomain.setSize(noticeEntities.size());
        return pageDomain;
    }

    private <T> InformationGroupVo<T> buildGroupFinancialReportVo(Long k, List<T> v) {
        InformationGroupVo<T> groupFinancialReportVo = new InformationGroupVo<>();
        groupFinancialReportVo.setList(v);
        groupFinancialReportVo.setDate(k);
        return groupFinancialReportVo;
    }

}
