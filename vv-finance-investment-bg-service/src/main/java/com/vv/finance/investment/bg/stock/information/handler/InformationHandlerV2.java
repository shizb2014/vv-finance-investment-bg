package com.vv.finance.investment.bg.stock.information.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.google.common.collect.Lists;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.info.RegionTypeEnum;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.dto.StringSplitDTO;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.common.WarrantSnapshot;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.common.us.constants.UsRedisKeyConstants;
import com.vv.finance.common.utils.DateUtils;
import com.vv.finance.common.utils.LocalDateTimeUtil;
import com.vv.finance.common.utils.StringUtil;
import com.vv.finance.investment.bg.api.warrant.WarrantApi;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.PublishStatusEnum;
import com.vv.finance.investment.bg.dto.info.StockSimpleInfo;
import com.vv.finance.investment.bg.entity.information.CalendarNewsPageReq;
import com.vv.finance.investment.bg.entity.information.CommonNewsPage;
import com.vv.finance.investment.bg.entity.information.FinancialEventVo;
import com.vv.finance.investment.bg.entity.information.FreeStockNewsVo;
import com.vv.finance.investment.bg.entity.information.GroupFinancialEventVo;
import com.vv.finance.investment.bg.entity.information.GroupTwentyFourHourNewsVo;
import com.vv.finance.investment.bg.entity.information.NewsPageReq;
import com.vv.finance.investment.bg.entity.information.PageWithTime;
import com.vv.finance.investment.bg.entity.information.SimpleStockVo;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.entity.information.StockNewsVo;
import com.vv.finance.investment.bg.entity.information.TagDto;
import com.vv.finance.investment.bg.entity.information.TwentyFourHourNewsVo;
import com.vv.finance.investment.bg.entity.information.enums.TwentyFourHoursTypeEnum;
import com.vv.finance.investment.bg.mapper.stock.quotes.StockNewsMapper;
import com.vv.finance.investment.bg.stock.f10.service.impl.AbstractBaseServiceImpl;
import com.vv.finance.investment.bg.stock.information.InformationConstant;
import com.vv.finance.investment.bg.stock.information.service.IStockNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ClassName InformationHandler
 * @Deacription 资讯
 * @Author lh.sz
 * @Date 2021年09月14日 13:53
 **/
@Component
@Slf4j
public class InformationHandlerV2 extends AbstractBaseServiceImpl {
    @Resource
    IStockNewsService stockNewsService;

    private static final String STOCK_SUFFIX = ".hk";
    private static final String US_STOCK_SUFFIX = ".us";

    private static final String QUERY_TIME_PATTERN = "yyyy-MM-dd";

    @Resource
    private StockNewsMapper stockNewsMapper;

    @Resource
    private WarrantApi warrantApi;

    @Resource
    private RedisClient redisClient;
    //资讯表与关联股票映射关系<code,list<id>>
    private ConcurrentHashMap<String,Map<String,Set<Long>>> relationCodeMap=new ConcurrentHashMap();

    private final String relationCodeKey="relation_code";

    @Value("${news.hk.maxNum:10000}")
    private Long maxNumber;

    /**
     * 初始化缓存所有资讯表与关联股票映射关系
     */
    @PostConstruct
    private void initAllRelationCodeMap(){
        log.info("=========初始化缓存所有资讯表与关联股票映射关系========== 开始");

        List<StockNewsEntity> stockNewsEntities = stockNewsMapper.findRelationCode();
        Map<String,Set<Long>> map =new HashMap<>();
        stockNewsEntities.forEach(o->{
            for (String code : o.getRelationStock().split(",")) {
                buildRelationCodeToIdMap(map, o, code);
            }
        });
//        redisClient.del(relationCodeKey);
        redisClient.hmset(RedisKeyConstants.BG_NEWS_RELATION_CODE_KEY,map);
//        relationCodeMap.put(relationCodeKey,map);
        log.info("=========初始化缓存所有资讯表与关联股票映射关系========== 结束 :{}");
    }

    //缓存所有资讯表与关联股票映射关系
    @Scheduled(cron = "0 0/2 * * * ?")
    public void updateAllRelationCodeMap(){
        initAllRelationCodeMap();
    }
    //更新资讯表与关联股票映射关系
    public void updateRelationCodeMap(StockNewsEntity stockNewsEntity){
        if (StringUtils.isNoneBlank(stockNewsEntity.getRelationStock())){
            log.info("updateAllRelationCodeMap 单个资讯 开始 id:{} relationStock:{}",stockNewsEntity.getId(),stockNewsEntity.getRelationStock());
            Map<String, Set<Long>> relationCodeToIdMap = redisClient.hmget(RedisKeyConstants.BG_NEWS_RELATION_CODE_KEY);
            for (String code : stockNewsEntity.getRelationStock().split(",")) {
                buildRelationCodeToIdMap(relationCodeToIdMap, stockNewsEntity, code);
            }
            log.info("updateAllRelationCodeMap 单个资讯 结束 id:{} relationStock:{}",stockNewsEntity.getId(),stockNewsEntity.getRelationStock());
        }
    }
    private void buildRelationCodeToIdMap(Map<String, Set<Long>> map, StockNewsEntity o, String code) {
        if (StringUtils.isNoneBlank(code)) {
            Set<Long> ids = map.get(code);
            if (CollUtil.isEmpty(ids)) {
                ids=new HashSet<Long>();
            }
            ids.add(o.getId());
            map.put(code,ids);
        }
    }


    @Autowired
    StockCache stockCache;
    private List<TagDto> getTags() {
        List<TagDto> tagDtos = stockNewsMapper.listAllTags();
        return CollectionUtils.isEmpty(tagDtos) ? Collections.emptyList() : tagDtos;
    }

    private Map<String, String> getTagMap() {
        return getTags().stream().collect(Collectors.toMap(TagDto::getTagValue, TagDto::getCode));
    }


    public PageWithTime<GroupTwentyFourHourNewsVo> pageTwentyFourHourNewsV2(NewsPageReq pageReq, Integer newsType) {
        Long id = pageReq.getId();
        Long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        long num = pageReq.getPageSize() == null ? 20 : pageReq.getPageSize();
        LocalDateTime time = Objects.nonNull(pageReq.getTime()) ? LocalDateTimeUtil.getLocalDateTime(pageReq.getTime(), ZoneOffset.of("+8")) : null;

        // 查询条件 1：资讯类型
        // 查询方式 1：查询最新资讯 2：翻页查询历史数据
        QueryWrapper<StockNewsEntity> wrapper = new QueryWrapper<StockNewsEntity>()
                .select("id", "news_id", "top_category", "second_category", "date_time", "content", "date")
                .eq(InformationConstant.PUBLISH_STATUS, PublishStatusEnum.YES)
                .eq(InformationConstant.TOP_CATEGORY, InformationConstant.TABLE_NEWS24HOURS)
                .lt(ObjectUtils.isNotEmpty(id), InformationConstant.COLUMN_ID, id)
                .le(ObjectUtils.isNotEmpty(time), InformationConstant.DATE_TIME, time)
//                .gt(ObjectUtils.isNotEmpty(id) && queryType.equals(QueryType.NEW.getType()), InformationConstant.COLUMN_ID, id)
                .eq(ObjectUtils.isNotEmpty(newsType) && !newsType.equals(TwentyFourHoursTypeEnum.ZERO.getCode()), InformationConstant.SECOND_CATEGORY, TwentyFourHoursTypeEnum.getValue(newsType))
                .orderByDesc(InformationConstant.DATE_TIME, InformationConstant.COLUMN_ID) //根据dateTime 倒排
                .last("limit " + num);

        List<StockNewsEntity> entityList = stockNewsMapper.selectList(wrapper);

        PageWithTime<GroupTwentyFourHourNewsVo> pageDomain = new PageWithTime<>();
        pageDomain.setTotal(entityList.size());
        pageDomain.setCurrent(currentPage);
        pageDomain.setSize(entityList.size());
        if (CollectionUtils.isNotEmpty(entityList)) {
            List<GroupTwentyFourHourNewsVo> records = new ArrayList<>();
            entityList.stream().collect(Collectors.groupingBy(StockNewsEntity::getDate)).forEach((k, v) -> {
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
    public PageWithTime<GroupFinancialEventVo> pageFinancialEventV2(CalendarNewsPageReq pageReq) {
        Long id = pageReq.getId();
        List<String> areaCodes = pageReq.getArea();
        List<String> levelValue = InformationConstant.getLevelValue(pageReq.getLevel());
        String startTime = ObjectUtils.isEmpty(pageReq.getStartTime()) ? "" : DateUtils.formatDate(pageReq.getStartTime(), QUERY_TIME_PATTERN);
        String endTime = ObjectUtils.isEmpty(pageReq.getStartTime()) ? "" : DateUtils.formatDate(pageReq.getEndTime(), QUERY_TIME_PATTERN);
        LocalDateTime time = Objects.nonNull(pageReq.getTime()) ? LocalDateTimeUtil.getLocalDateTime(pageReq.getTime(), ZoneOffset.of("+8")) : null;
        long num = pageReq.getPageSize();
        long currentPage = pageReq.getCurrentPage();

        // 查询条件 1：地区 2：重要等级 3：起止时间
        // 查询方式 1：查询最新资讯 2：翻页查询历史数据
        // 排序 1：datetime
        QueryWrapper<StockNewsEntity> wrapper = new QueryWrapper<StockNewsEntity>()
                .select("id", "news_id", "top_category", "second_category", "date_time", "content", "date")
                .eq(InformationConstant.PUBLISH_STATUS, PublishStatusEnum.YES)
                .eq(InformationConstant.TOP_CATEGORY, InformationConstant.TABLE_CALENDAR)
                .eq(InformationConstant.SECOND_CATEGORY, InformationConstant.SECOND_CATEGORY_0)
                .ge(StringUtils.isNotBlank(startTime), InformationConstant.DATE, startTime)
                .le(StringUtils.isNotBlank(endTime), InformationConstant.DATE, endTime)
                .le(ObjectUtils.isNotEmpty(time), InformationConstant.DATE_TIME, time)
                .lt(ObjectUtils.isNotEmpty(id), InformationConstant.COLUMN_ID, id)
                .orderByDesc(InformationConstant.DATE_TIME, InformationConstant.COLUMN_ID)
                .last("limit " + num);
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
        List<StockNewsEntity> entityList = stockNewsMapper.selectList(wrapper);
        PageWithTime<GroupFinancialEventVo> pageDomain = new PageWithTime<>();
        pageDomain.setTotal(entityList.size());
        pageDomain.setCurrent(currentPage);

        List<Long> ids = entityList.stream().map(StockNewsEntity::getId).collect(Collectors.toList());
        List<String> areaByIds = stockNewsMapper.queryAreaByIds(arrayToStr(ids));
        Map<String, String> areaMap = areaByIds.stream().filter(item -> StringUtils.isNotBlank(item)).map(item -> item.split(StringPool.COMMA)).collect(Collectors.toMap(item -> item[0], item -> item[1]));

        pageDomain.setSize(entityList.size());
        List<GroupFinancialEventVo> records = new ArrayList<>();
        entityList.stream().collect(Collectors.groupingBy(StockNewsEntity::getDate)).forEach((k, v) -> {
            records.add(buildFinancialEventByEntity(k, v, areaMap));
        });
        records.sort(Comparator.comparing(GroupFinancialEventVo::getDate, Comparator.reverseOrder()));
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

    /**
     * 自选,异动,港美股,新股
     * 通用列表vo
     *
     * @param record
     * @param snapshot
     * @param tagMap   <code,value>
     * @return
     */
    private FreeStockNewsVo buildFreeVo(StockNewsEntity record, List<StockSnapshot> snapshot, Map<String, String> tagMap, List<ComStockSimpleDto> simpleDtoList) {
        FreeStockNewsVo freeStockNewsVo = new FreeStockNewsVo();
        freeStockNewsVo.setSource(record.getSource());
        freeStockNewsVo.setNewsid(record.getId());
        freeStockNewsVo.setStockCode(record.getRelationStock());
        freeStockNewsVo.setNewsType(tagMap.getOrDefault(record.getSecondCategory(), record.getSecondCategory()));
        freeStockNewsVo.setNewsTypeName(record.getSecondCategory());
        freeStockNewsVo.setImage(record.getImageUrl());
        freeStockNewsVo.setMarket(record.getMarket());
        freeStockNewsVo.setDateTime(record.getDateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        freeStockNewsVo.setXdbmask(record.getXdbmask().toString());
        freeStockNewsVo.setTitle(record.getNewsTitle());
        freeStockNewsVo.setId(record.getId() + "");
        SimpleStockVo simpleStockVo;
        if (StringUtils.isBlank(record.getRelationStock())) {
            freeStockNewsVo.setRemainRelationStockNumber(0);
            return freeStockNewsVo;
        }
        String[] codes = record.getRelationStock().split(",");
        freeStockNewsVo.setRemainRelationStockNumber(codes.length - 1);
        Map<String, ComStockSimpleDto> codeStockDtoMap = simpleDtoList.stream().collect(Collectors.toMap(ComStockSimpleDto::getCode, v -> v, (o, v) -> v));
        for (String s : codes) {
            String code = s + STOCK_SUFFIX;
            //判断股票是否为港股，非港股则转为美股
            ComStockSimpleDto stockSimpleInfo = codeStockDtoMap.get(code);
            if (ObjectUtils.isEmpty(stockSimpleInfo)){
                code=code.replace(STOCK_SUFFIX,US_STOCK_SUFFIX);
            }
            simpleStockVo = buildStockVo(code, snapshot, false);
            if (ObjectUtils.isNotEmpty(simpleStockVo)) {
                freeStockNewsVo.setSimpleStockVo(simpleStockVo);
                break;
            }
        }
        return freeStockNewsVo;
    }

    private SimpleStockVo buildStockVo(String stockCode, List<StockSnapshot> snapshot, boolean needWarrant) {

//        if (stockCode.contains(US_STOCK_SUFFIX)) {
//            //美股
//            StockSnapshot usStockSnapshot = redisClient.get(UsRedisKeyConstants.US_RECEIVER_STOCK_SNAPSHOT_BEAN.concat(stockCode));
//            if (usStockSnapshot !=null) {
//                SimpleStockVo simpleUsStockVo = new SimpleStockVo();
//                simpleUsStockVo.setStockCode(usStockSnapshot.getCode());
//                simpleUsStockVo.setName(usStockSnapshot.getName());
//                simpleUsStockVo.setChgPct(usStockSnapshot.getChgPct());
//                simpleUsStockVo.setPrice(usStockSnapshot.getLast());
//                simpleUsStockVo.setChg(usStockSnapshot.getChg());
//                simpleUsStockVo.setStockType(usStockSnapshot.getStockType());
//                simpleUsStockVo.setRegionType(usStockSnapshot.getRegionType());
//                return simpleUsStockVo;
//            }
//        }

        StockSnapshot stockSnapshot = snapshot.stream().filter(s -> s.getCode().equals(stockCode)).findFirst().orElse(null);
        if (stockSnapshot != null) {
            SimpleStockVo simpleStockVo = new SimpleStockVo();
            simpleStockVo.setStockCode(stockSnapshot.getCode());
            simpleStockVo.setName(stockSnapshot.getName());
            simpleStockVo.setChgPct(stockSnapshot.getChgPct());
            simpleStockVo.setPrice(stockSnapshot.getLast());
            simpleStockVo.setLast(stockSnapshot.getLast());
            simpleStockVo.setChg(stockSnapshot.getChg());
            simpleStockVo.setStockType(stockSnapshot.getStockType());
            simpleStockVo.setRegionType(stockSnapshot.getRegionType());
            simpleStockVo.setStockId(stockSnapshot.getStockId());
            return simpleStockVo;
        } else if (needWarrant) {
            List<WarrantSnapshot> data = warrantApi.getWarrantSnapshotList(new String[]{stockCode}).getData();
            if (CollectionUtils.isNotEmpty(data)) {
                SimpleStockVo warrantStockVo = new SimpleStockVo();
                warrantStockVo.setStockCode(data.get(0).getStockCode());
                warrantStockVo.setChg(data.get(0).getChg());
                warrantStockVo.setPrice(data.get(0).getLast());
                warrantStockVo.setLast(data.get(0).getLast());
                warrantStockVo.setName(data.get(0).getWarrantName());
                warrantStockVo.setChgPct(data.get(0).getChgPct());
                warrantStockVo.setWarrantCode(data.get(0).getWarrantCode());
                warrantStockVo.setWarrantName(data.get(0).getWarrantName());
                warrantStockVo.setRegionType(RegionTypeEnum.HK.getCode());
                warrantStockVo.setStockType(StockTypeEnum.WARRANT.getCode());
                warrantStockVo.setStockId(stockSnapshot.getStockId());
                return warrantStockVo;
            }
        }
        return null;
    }

    /**
     * 分页获取异动资讯
     *
     * @param pageReq 分页组件
     * @return
     */
    public PageWithTime<FreeStockNewsVo> transactionInformationPageV2(CommonNewsPage pageReq) {
        PageWithTime<FreeStockNewsVo> transactionInformationPage = new PageWithTime<>();
        LocalDateTime time = Objects.nonNull(pageReq.getTime()) ? LocalDateTimeUtil.getLocalDateTime(pageReq.getTime(), ZoneOffset.of("+8")) : null;
        long num = pageReq.getPageSize();
        List<TagDto> tags = getTags();
        List<String> newsTag = tags.stream().filter(tagDto -> CommonNewsPage.QueryCodeEnum.TRANSACTION.getCode().equals(tagDto.getCategory())).map(TagDto::getTagValue).collect(Collectors.toList());
        List<StockNewsEntity> entityList = stockNewsService.list(new QueryWrapper<StockNewsEntity>()
                .eq(InformationConstant.PUBLISH_STATUS, PublishStatusEnum.YES)
                .eq(InformationConstant.TOP_CATEGORY, InformationConstant.NEWS_HK_HQ)
                .in(CollectionUtils.isNotEmpty(newsTag), InformationConstant.SECOND_CATEGORY, newsTag)
                .lt(ObjectUtils.isNotEmpty(pageReq.getId()), InformationConstant.COLUMN_ID, pageReq.getId())
                .le(ObjectUtils.isNotEmpty(time), InformationConstant.DATE_TIME, time)
                .orderByDesc(InformationConstant.DATE_TIME, InformationConstant.COLUMN_ID)
                .last("limit " + num));
        if (CollectionUtils.isNotEmpty(entityList)) {
            List<StockSnapshot> snapshot = this.getSnapshot();
            Map<String, String> tagMap = getTagMap();
            List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockInfoList(null);
            List<FreeStockNewsVo> collect = entityList.stream().map(record -> {
                return buildFreeVo(record, snapshot, tagMap, simpleDtoList);
            }).collect(Collectors.toList());
            transactionInformationPage.setRecords(collect);
        }
        transactionInformationPage.setSize(entityList.size());
        transactionInformationPage.setCurrent(pageReq.getCurrentPage());
        transactionInformationPage.setTotal(entityList.size());
        return transactionInformationPage;
    }

    /**
     * 分页获取新股资讯
     *
     * @param pageReq
     * @return
     */
    public PageWithTime<FreeStockNewsVo> newShareInformationPageV2(CommonNewsPage pageReq) {
        PageWithTime<FreeStockNewsVo> newShareInformationPage = new PageWithTime<>();
        long num = pageReq.getPageSize() == null ? 20 : pageReq.getPageSize();
        LocalDateTime time = Objects.nonNull(pageReq.getTime()) ? LocalDateTimeUtil.getLocalDateTime(pageReq.getTime(), ZoneOffset.of("+8")) : null;
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        List<TagDto> tags = getTags();
        List<String> newsTag = new ArrayList<>();
        String topCategory = InformationConstant.NEWS_HK;
        List<Long> allIds = new ArrayList<>();
        if (Objects.isNull(pageReq.getRegionType()) || pageReq.getRegionType() == RegionTypeEnum.HK.getCode()) {
            topCategory = InformationConstant.NEWS_HK;
            newsTag = tags.stream().filter(tagDto -> CommonNewsPage.QueryCodeEnum.NEW.getCode().equals(tagDto.getCategory())).map(TagDto::getTagValue).collect(Collectors.toList());
        } else if (pageReq.getRegionType() == RegionTypeEnum.US.getCode()) {
            topCategory = InformationConstant.NEWS_US;
            //获取资讯表与关联股票映射关系
            allIds = getIdsByCodes(pageReq.getUsNewStocks());
            allIds.sort(Comparator.comparing(Long::longValue).reversed());
            if (null != pageReq.getId()){
                allIds = allIds.stream().filter(id -> id < pageReq.getId()).collect(Collectors.toList());
            }
        }
        List<StockNewsEntity> entityList = stockNewsService.list(new QueryWrapper<StockNewsEntity>()
                .eq(InformationConstant.PUBLISH_STATUS, PublishStatusEnum.YES)
                .in(CollUtil.isNotEmpty(allIds), "id", allIds)
                .eq(InformationConstant.TOP_CATEGORY, topCategory)
                .in(CollectionUtils.isNotEmpty(newsTag), InformationConstant.SECOND_CATEGORY, newsTag)
                .lt(ObjectUtils.isNotEmpty(pageReq.getId()), InformationConstant.COLUMN_ID, pageReq.getId())
                .le(ObjectUtils.isNotEmpty(time), InformationConstant.DATE_TIME, time)
                .orderByDesc(InformationConstant.DATE_TIME, InformationConstant.COLUMN_ID).last("limit " + num));
        if (CollectionUtils.isNotEmpty(entityList)) {
            List<StockSnapshot> snapshot = this.getSnapshot();
            Map<String, String> tagMap = tags.stream().collect(Collectors.toMap(TagDto::getTagValue, TagDto::getCode));
            List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockInfoList(null);
            List<FreeStockNewsVo> collect = entityList.stream().map(record -> {
                return buildFreeVo(record, snapshot, tagMap, simpleDtoList);
            }).collect(Collectors.toList());
            newShareInformationPage.setRecords(collect);
        }
        newShareInformationPage.setTotal(entityList.size());
        newShareInformationPage.setSize(entityList.size());
        newShareInformationPage.setCurrent(currentPage);
        return newShareInformationPage;
    }

    /**
     * 分页获取港美股资讯
     *
     * @param pageReq 分页
     * @param type    类型
     * @return
     */
    public PageWithTime<FreeStockNewsVo> hkOrAmericanInformationPageV2(CommonNewsPage pageReq, CommonNewsPage.QueryCodeEnum type) {
        PageWithTime<FreeStockNewsVo> hkOrAmericanInformationPage = new PageWithTime<>();
        long num = pageReq.getPageSize() == null ? 20 : pageReq.getPageSize();
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        LocalDateTime dateTime = Objects.nonNull(pageReq.getTime()) ? LocalDateTimeUtil.getLocalDateTime(pageReq.getTime(), ZoneOffset.of("+8")) : null;
        List<String> topCategory = new ArrayList<>();
        List<String> tags = new ArrayList<>();
        if (type == CommonNewsPage.QueryCodeEnum.HK) {
            tags.addAll(Arrays.asList("异动", "公告", "机构观点", "特约观点", "盘报", "新股", "其它","港股通","要闻", "经济数据"));
            if (Objects.isNull(pageReq.getRegionType())) {
                // 旧版本逻辑
                topCategory = Collections.singletonList(InformationConstant.NEWS_HK);
            } else {
                // 77迭代异动放入港股下
                topCategory = Arrays.asList(InformationConstant.NEWS_HK, InformationConstant.NEWS_HK_HQ);
                // 108迭代新股放入港股下
                tags.addAll(getTags().stream().filter(tagDto -> CommonNewsPage.QueryCodeEnum.TRANSACTION.getCode().equals(tagDto.getCategory()) || CommonNewsPage.QueryCodeEnum.NEW.getCode().equals(tagDto.getCategory())).map(TagDto::getTagValue).collect(Collectors.toList()));
            }
        } else if (type == CommonNewsPage.QueryCodeEnum.US) {
            topCategory = Collections.singletonList(InformationConstant.NEWS_US);
            tags.addAll(Arrays.asList("财报", "公告", "动向", "异动", "盘报", "分析", "要闻", "经济数据"));
        }
        Long id = pageReq.getId() == null ? 0L : pageReq.getId();
        List<StockNewsEntity> entityList = stockNewsMapper.queryHKAndHQNews(arrayToStr(topCategory), arrayToStr(tags), id, dateTime, maxNumber, num);

//        List<StockNewsEntity> entityList = stockNewsService.list(new QueryWrapper<StockNewsEntity>()
//                .eq(InformationConstant.PUBLISH_STATUS, PublishStatusEnum.YES)
//                .in(InformationConstant.TOP_CATEGORY, topCategory)
//                .in(InformationConstant.SECOND_CATEGORY, tags)
//                .lt(ObjectUtils.isNotEmpty(pageReq.getId()), InformationConstant.COLUMN_ID, pageReq.getId())
//                .le(dateTime != null, InformationConstant.DATE_TIME, dateTime)
////                .likeRight(InformationConstant.MARKET, type.getMarket())
//                .orderByDesc(InformationConstant.DATE_TIME, InformationConstant.COLUMN_ID)
//                .last("limit " + num));
        if (CollectionUtils.isNotEmpty(entityList)) {
            List<StockSnapshot> snapshot = this.getSnapshot();
            Map<String, String> tagMap = getTagMap();
            List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockInfoList(null);
            List<FreeStockNewsVo> collect = entityList.stream().map(record -> {
                return buildFreeVo(record, snapshot, tagMap, simpleDtoList);
            }).collect(Collectors.toList());
            hkOrAmericanInformationPage.setRecords(collect);
        }
        hkOrAmericanInformationPage.setTotal(entityList.size());
        hkOrAmericanInformationPage.setSize(entityList.size());
        hkOrAmericanInformationPage.setCurrent(currentPage);
        return hkOrAmericanInformationPage;
    }

    public PageWithTime<FreeStockNewsVo> pageFreeVoV2(CommonNewsPage pageReq, List<String> stockCodes) {
        log.info("查询自选资讯 pageFreeVoV2 开始 stockCodes：{}",stockCodes);
        long monitorTime = System.currentTimeMillis();
        PageWithTime<FreeStockNewsVo> pageDomain = new PageWithTime<>();
        if (CollectionUtils.isEmpty(stockCodes)) {
            return pageDomain;
        }
        //获取资讯表与关联股票映射关系
        List<Long> allIds = getIdsByCodes(stockCodes);
        allIds.sort(Comparator.comparing(Long::longValue).reversed());
        if (null != pageReq.getId()){
            allIds = allIds.stream().filter(id -> id < pageReq.getId()).collect(Collectors.toList());
        }

        long monitorTime1 = System.currentTimeMillis();
        log.info("查询自选资讯 allIds 耗时：{}",monitorTime1-monitorTime);
        long num = pageReq.getPageSize() == null ? 20 : pageReq.getPageSize();
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        List<StockNewsEntity> entityList=new ArrayList<>();
        if (CollUtil.isNotEmpty(allIds)){
            int maxNum = (int) (num + 5);
            if (allIds.size()<maxNum) {
                maxNum =allIds.size();
            }
            allIds = allIds.subList(0,maxNum );
            LocalDateTime time = Objects.nonNull(pageReq.getTime()) ? LocalDateTimeUtil.getLocalDateTime(pageReq.getTime(), ZoneOffset.of("+8")) : LocalDateTime.now();
            entityList = stockNewsMapper.selectList(new QueryWrapper<StockNewsEntity>().in("id", allIds).le("date_time", time).orderByDesc("date_time", "id").last("limit "+num));
        }
        long monitorTime2 = System.currentTimeMillis();
        log.info("查询自选资讯 entityList 耗时：{}",monitorTime2-monitorTime1);
        pageDomain.setSize(entityList.size());
        pageDomain.setCurrent(currentPage);
        pageDomain.setTotal(entityList.size());
        // 码表
        List<StockSnapshot> snapshot = this.getSnapshot();
        long monitorTime3 = System.currentTimeMillis();
        log.info("查询自选资讯 snapshot 耗时：{}",monitorTime3-monitorTime2);
        // 自选股码表
        List<StockSnapshot> freeSnapshots = snapshot.stream().filter(s -> stockCodes.contains(s.getCode())).collect(Collectors.toList());
        Map<String, String> tagMap = getTagMap();
        if (CollectionUtils.isNotEmpty(entityList)) {
            List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockInfoList(null);
            List<FreeStockNewsVo> collect = entityList.stream().map(record -> {
                return buildFreeVo(record, freeSnapshots, tagMap, simpleDtoList);
            }).collect(Collectors.toList());
            pageDomain.setRecords(collect);
        }
        log.info("查询自选资讯 pageFreeVoV2 结束 耗时：{}  stockCodes：{}",System.currentTimeMillis()-monitorTime,stockCodes);
        return pageDomain;
    }
    //获取资讯表与关联股票映射关系
    private List<Long> getIdsByCodes(List<String> stockCodes) {
        Map<String, Set<Long>> relationCodeToIdMap = redisClient.hmget(RedisKeyConstants.BG_NEWS_RELATION_CODE_KEY);;
        Set<Long> allIds=new HashSet<>();
        stockCodes.forEach(code->{
            if (code.contains(STOCK_SUFFIX)){
                code = code.replace(STOCK_SUFFIX, "");
            }else if (code.contains(US_STOCK_SUFFIX)){
                code = code.replace(US_STOCK_SUFFIX, "");
            }
            Set<Long> ids = relationCodeToIdMap.get(code);
            if (CollUtil.isNotEmpty(ids)) {
                allIds.addAll(ids);
            }
        });
        return Lists.newArrayList(allIds);
    }

    public PageWithTime<FreeStockNewsVo> listNewsBySimpleStockVoV2(CommonNewsPage pageReq) {
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        long num = pageReq.getPageSize() == null ? 10 : pageReq.getPageSize();
        LocalDateTime time = Objects.nonNull(pageReq.getTime()) ? LocalDateTimeUtil.getLocalDateTime(pageReq.getTime()) : LocalDateTime.now();
        String stockCode = pageReq.getStockCode();
        if (StringUtils.isBlank(stockCode) || !stockCode.contains(StringPool.DOT)) {
            log.info("stockCode传参错误，{}", stockCode);
            return null;
        }
        StringSplitDTO stringSplitDTO = StringUtil.splitByCode(stockCode);
        stockCode = stockCode.substring(0, stockCode.indexOf(StringPool.DOT));
        List<String> topCategorys = new ArrayList<>();
        if (RegionTypeEnum.HK.getCode()==stringSplitDTO.getRegionType()) {
            topCategorys.add(InformationConstant.NEWS_HK);
            topCategorys.add(InformationConstant.NEWS_HK_HQ);
        }else if (RegionTypeEnum.US.getCode()==stringSplitDTO.getRegionType()) {
            topCategorys.add(InformationConstant.NEWS_US);
        }
        // 查询条件 1：股票代码 数据库中多个数据用的逗号隔开 （用FIND_IN_SET）
        Long Id = pageReq.getId() == null ? 0L : pageReq.getId();
        List<StockNewsVo> entityList = stockNewsMapper.pageOldStockNewsVoInHkAndHq3(Id, stockCode, time, num,arrayToStr(topCategorys));
        PageWithTime<FreeStockNewsVo> pageDomain = new PageWithTime<>();
        pageDomain.setCurrent(currentPage);
        pageDomain.setSize(entityList.size());
        pageDomain.setTotal(entityList.size());
        if (CollectionUtils.isNotEmpty(entityList)) {
            Map<String, String> tagMap = getTagMap();
            List<FreeStockNewsVo> collect = entityList.stream().map(record -> {
                FreeStockNewsVo freeStockNewsVo = new FreeStockNewsVo();
                BeanUtils.copyProperties(record, freeStockNewsVo);
                freeStockNewsVo.setNewsType(tagMap.getOrDefault(record.getNewsType(), record.getNewsType()));
                freeStockNewsVo.setNewsTypeName(record.getNewsType());
                freeStockNewsVo.setId(String.valueOf(record.getId()));
                return freeStockNewsVo;
            }).collect(Collectors.toList());
            pageDomain.setRecords(collect);
        }
        return pageDomain;
    }

    private <T> String arrayToStr(Collection<T> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return "('')";
        }
        String join = StringUtils.join(codes, "','");
        return "('" + join + "')";
    }

    public PageWithTime<FreeStockNewsVo> assignInformationPageV2 (CommonNewsPage pageReq, List<String> stockCodes) {
        log.info("查询分配股票资讯 pageFreeVoV2 开始 stockCodes：{}",stockCodes);
        long monitorTime = System.currentTimeMillis();
        PageWithTime<FreeStockNewsVo> pageDomain = new PageWithTime<>();
        if (CollectionUtils.isEmpty(stockCodes)) {
            return pageDomain;
        }
        //获取资讯表与关联股票映射关系
        List<Long> allIds = getIdsByCodes(stockCodes);
        allIds.sort(Comparator.comparing(Long::longValue).reversed());
        if (null != pageReq.getId()){
            allIds = allIds.stream().filter(id -> id < pageReq.getId()).collect(Collectors.toList());
        }

        long monitorTime1 = System.currentTimeMillis();
        log.info("查询分配股票资讯 allIds 耗时：{}",monitorTime1-monitorTime);
        long num = pageReq.getPageSize() == null ? 20 : pageReq.getPageSize();
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        List<StockNewsEntity> entityList=new ArrayList<>();
        if (CollUtil.isNotEmpty(allIds)){
            int maxNum = (int) (num + 5);
            if (allIds.size()<maxNum) {
                maxNum =allIds.size();
            }
            allIds = allIds.subList(0,maxNum );
            LocalDateTime time = Objects.nonNull(pageReq.getTime()) ? LocalDateTimeUtil.getLocalDateTime(pageReq.getTime(), ZoneOffset.of("+8")) : LocalDateTime.now();
            entityList = stockNewsMapper.selectList(new QueryWrapper<StockNewsEntity>().in("id", allIds).le("date_time", time).orderByDesc("date_time", "id").last("limit "+num));
        }
        long monitorTime2 = System.currentTimeMillis();
        log.info("查询分配股票资讯 entityList 耗时：{}",monitorTime2-monitorTime1);
        pageDomain.setSize(entityList.size());
        pageDomain.setCurrent(currentPage);
        pageDomain.setTotal(entityList.size());
        // 码表
        List<StockSnapshot> snapshot = this.getSnapshot();
        long monitorTime3 = System.currentTimeMillis();
        log.info("查询分配股票资讯 snapshot 耗时：{}",monitorTime3-monitorTime2);
        // 分配股票股码表
        List<StockSnapshot> freeSnapshots = snapshot.stream().filter(s -> stockCodes.contains(s.getCode())).collect(Collectors.toList());
        Map<String, String> tagMap = getTagMap();
        if (CollectionUtils.isNotEmpty(entityList)) {
            List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockInfoList(null);
            List<FreeStockNewsVo> collect = entityList.stream().map(record -> {
                return buildFreeVo(record, freeSnapshots, tagMap, simpleDtoList);
            }).collect(Collectors.toList());
            pageDomain.setRecords(collect);
        }
        log.info("查询分配股票资讯 pageFreeVoV2 结束 耗时：{}  stockCodes：{}",System.currentTimeMillis()-monitorTime,stockCodes);
        return pageDomain;
    }
}
