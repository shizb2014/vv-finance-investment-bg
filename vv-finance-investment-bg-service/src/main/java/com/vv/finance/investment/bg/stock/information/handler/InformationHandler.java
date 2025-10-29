package com.vv.finance.investment.bg.stock.information.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.entity.common.WarrantSnapshot;
import com.vv.finance.investment.bg.annotation.StatisticsCount;
import com.vv.finance.investment.bg.api.warrant.WarrantApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.PublishStatusEnum;
import com.vv.finance.investment.bg.entity.information.*;
import com.vv.finance.investment.bg.entity.uts.Xnhks0701;
import com.vv.finance.investment.bg.mapper.stock.quotes.StockNewsMapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0701Mapper;
import com.vv.finance.investment.bg.stock.f10.service.impl.AbstractBaseServiceImpl;
import com.vv.finance.investment.bg.stock.information.InformationConstant;
import com.vv.finance.investment.bg.stock.information.service.IStockNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName InformationHandler
 * @Deacription 资讯
 * @Author lh.sz
 * @Date 2021年09月14日 13:53
 **/
@Component
@Slf4j
public class InformationHandler extends AbstractBaseServiceImpl {
    @Resource
    IStockNewsService stockNewsService;

    private static final String STOCK_SUFFIX = ".hk";
    private static final String US_STOCK_SUFFIX = ".us";

    @Resource
    private StockNewsMapper stockNewsMapper;

    @Resource
    private Xnhks0701Mapper xnhk0701Mapper;

    @Resource
    private WarrantApi warrantApi;

    private List<TagDto> getTags() {
        List<TagDto> tagDtos = stockNewsMapper.listAllTags();
        return CollectionUtils.isEmpty(tagDtos) ? Collections.emptyList() : tagDtos;
    }

    private Map<String, String> getTagMap() {
        return getTags().stream().collect(Collectors.toMap(TagDto::getTagValue, TagDto::getCode));
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
    private FreeStockNewsVo buildFreeVo(StockNewsEntity record, List<StockSnapshot> snapshot, Map<String, String> tagMap) {
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
            return freeStockNewsVo;
        }
        String[] codes = record.getRelationStock().split(",");
        for (String s : codes) {
            String code = s + STOCK_SUFFIX;
            simpleStockVo = buildStockVo(code, snapshot, false);
            if (ObjectUtils.isNotEmpty(simpleStockVo)) {
                freeStockNewsVo.setSimpleStockVo(simpleStockVo);
                break;
            }
        }
        return freeStockNewsVo;
    }

    private SimpleStockVo buildStockVo(String stockCode, List<StockSnapshot> snapshot, boolean needWarrant) {
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
            simpleStockVo.setStockId(stockSnapshot.getStockId());
            return simpleStockVo;
        }
//        else if (needWarrant) {
//            List<WarrantSnapshot> data = warrantApi.getWarrantSnapshotList(new String[]{stockCode}).getData();
//            if (CollectionUtils.isNotEmpty(data)) {
//                SimpleStockVo warrantStockVo = new SimpleStockVo();
//                warrantStockVo.setStockCode(data.get(0).getStockCode());
//                warrantStockVo.setChg(data.get(0).getChg());
//                warrantStockVo.setPrice(data.get(0).getLast());
//                warrantStockVo.setLast(data.get(0).getLast());
//                warrantStockVo.setName(data.get(0).getWarrantName());
//                warrantStockVo.setChgPct(data.get(0).getChgPct());
//                warrantStockVo.setWarrantCode(data.get(0).getWarrantCode());
//                warrantStockVo.setWarrantName(data.get(0).getWarrantName());
//                warrantStockVo.setStockId(stockSnapshot.getStockId());
//                return warrantStockVo;
//            }
//        }
        return null;
    }

    /**
     * 分页获取异动资讯
     *
     * @param pageReq 分页组件
     * @return
     */
    public PageWithTime<FreeStockNewsVo> transactionInformationPage(CommonNewsPage pageReq) {
        PageWithTime<FreeStockNewsVo> transactionInformationPage = new PageWithTime<>();
        Page<StockNewsEntity> page = new Page<>();
        page.setCurrent(pageReq.getCurrentPage());
        page.setSize(pageReq.getPageSize());
        List<TagDto> tags = getTags();
        List<String> newsTag = tags.stream().filter(tagDto -> CommonNewsPage.QueryCodeEnum.TRANSACTION.getCode().equals(tagDto.getCategory())).map(TagDto::getTagValue).collect(Collectors.toList());
        Page<StockNewsEntity> hkHqPage = stockNewsService.page(page, new QueryWrapper<StockNewsEntity>()
                .eq(InformationConstant.PUBLISH_STATUS, PublishStatusEnum.YES)
                .eq(InformationConstant.TOP_CATEGORY, InformationConstant.NEWS_HK_HQ)
                .in(CollectionUtils.isNotEmpty(newsTag), InformationConstant.SECOND_CATEGORY, newsTag)
                .le(ObjectUtils.isNotEmpty(pageReq.getId()), InformationConstant.COLUMN_ID, pageReq.getId())
                .orderByDesc(InformationConstant.DATE_TIME));
        List<StockNewsEntity> newsHkHqList = hkHqPage.getRecords();
        if (CollectionUtils.isNotEmpty(newsHkHqList)) {
            List<StockSnapshot> snapshot = this.getSnapshot();
            Map<String, String> tagMap = getTagMap();
            List<FreeStockNewsVo> collect = newsHkHqList.stream().map(record -> {
                return buildFreeVo(record, snapshot, tagMap);
            }).collect(Collectors.toList());
            transactionInformationPage.setRecords(collect);
        }
        transactionInformationPage.setSize(hkHqPage.getSize());
        transactionInformationPage.setCurrent(hkHqPage.getCurrent());
        transactionInformationPage.setTotal(hkHqPage.getTotal());
        return transactionInformationPage;
    }

    /**
     * 分页获取新股资讯
     *
     * @param pageReq
     * @return
     */
    public PageWithTime<FreeStockNewsVo> newShareInformationPage(CommonNewsPage pageReq) {
        PageWithTime<FreeStockNewsVo> newShareInformationPage = new PageWithTime<>();
        long pageSize = pageReq.getPageSize() == null ? 20 : pageReq.getPageSize();
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        Page<StockNewsEntity> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        List<TagDto> tags = getTags();
        List<String> newsTag = tags.stream().filter(tagDto -> CommonNewsPage.QueryCodeEnum.NEW.getCode().equals(tagDto.getCategory())).map(TagDto::getTagValue).collect(Collectors.toList());
        Page<StockNewsEntity> newsHkPage = stockNewsService.page(page, new QueryWrapper<StockNewsEntity>()
                .eq(InformationConstant.PUBLISH_STATUS, PublishStatusEnum.YES)
                .eq(InformationConstant.TOP_CATEGORY, InformationConstant.NEWS_HK)
                .in(CollectionUtils.isNotEmpty(newsTag), InformationConstant.SECOND_CATEGORY, newsTag)
                .le(ObjectUtils.isNotEmpty(pageReq.getId()), InformationConstant.COLUMN_ID, pageReq.getId())
                .orderByDesc(InformationConstant.NEWS_ID));
        List<StockNewsEntity> newsHkList = newsHkPage.getRecords();
        if (CollectionUtils.isNotEmpty(newsHkList)) {
            List<StockSnapshot> snapshot = this.getSnapshot();
            Map<String, String> tagMap = tags.stream().collect(Collectors.toMap(TagDto::getTagValue, TagDto::getCode));
            List<FreeStockNewsVo> collect = newsHkList.stream().map(record -> {
                return buildFreeVo(record, snapshot, tagMap);
            }).collect(Collectors.toList());
            newShareInformationPage.setRecords(collect);
        }
        newShareInformationPage.setTotal(newsHkPage.getTotal());
        newShareInformationPage.setSize(newsHkPage.getSize());
        newShareInformationPage.setCurrent(newsHkPage.getCurrent());
        return newShareInformationPage;
    }

    /**
     * 分页获取港美股资讯
     *
     * @param pageReq 分页
     * @param type    类型
     * @return
     */
    public PageWithTime<FreeStockNewsVo> hkOrAmericanInformationPage(CommonNewsPage pageReq,
                                                                     CommonNewsPage.QueryCodeEnum type) {
        PageWithTime<FreeStockNewsVo> hkOrAmericanInformationPage = new PageWithTime<>();
        long pageSize = pageReq.getPageSize() == null ? 20 : pageReq.getPageSize();
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        Page<StockNewsEntity> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);
        LocalDateTime dateTime = null;
        if (ObjectUtils.isNotEmpty(pageReq.getId())) {
            StockNewsEntity byNewsIdAndTop = stockNewsMapper.selectById(pageReq.getId());
            dateTime = byNewsIdAndTop.getDateTime();
        }
        String topCategory = "";
        List<String> tags = Collections.EMPTY_LIST;
        if (type == CommonNewsPage.QueryCodeEnum.HK) {
            topCategory = InformationConstant.NEWS_HK;
            tags = Arrays.asList("异动", "公告", "机构观点", "特约观点", "盘报", "新股", "其它","港股通","要闻", "经济数据");
        } else if (type == CommonNewsPage.QueryCodeEnum.US) {
            topCategory = InformationConstant.NEWS_US;
            tags = Arrays.asList("财报", "公告", "动向", "异动", "盘报", "分析", "要闻", "经济数据");
        }

        Page<StockNewsEntity> newsHkPage = stockNewsService.page(page, new QueryWrapper<StockNewsEntity>()
                .eq(InformationConstant.PUBLISH_STATUS, PublishStatusEnum.YES)
                .eq(InformationConstant.TOP_CATEGORY, topCategory)
                .in(InformationConstant.SECOND_CATEGORY, tags)
                .le(ObjectUtils.isNotEmpty(pageReq.getId()), InformationConstant.COLUMN_ID, pageReq.getId())
                .le(dateTime != null, InformationConstant.DATE_TIME, dateTime)
//                .likeRight(InformationConstant.MARKET, type.getMarket())
                .orderByDesc(InformationConstant.DATE_TIME, InformationConstant.COLUMN_ID));
        List<StockNewsEntity> newsHkList = newsHkPage.getRecords();
        if (CollectionUtils.isNotEmpty(newsHkList)) {
            List<StockSnapshot> snapshot = this.getSnapshot();
            Map<String, String> tagMap = getTagMap();
            List<FreeStockNewsVo> collect = newsHkList.stream().map(record -> {
                return buildFreeVo(record, snapshot, tagMap);
            }).collect(Collectors.toList());
            hkOrAmericanInformationPage.setRecords(collect);
        }
        hkOrAmericanInformationPage.setTotal(newsHkPage.getTotal());
        hkOrAmericanInformationPage.setSize(newsHkPage.getSize());
        hkOrAmericanInformationPage.setCurrent(newsHkPage.getCurrent());
        return hkOrAmericanInformationPage;
    }

    public PageWithTime<FreeStockNewsVo> pageFreeVo(CommonNewsPage pageReq, List<String> stockCodes) {
        PageWithTime<FreeStockNewsVo> pageDomain = new PageWithTime<>();
        if (CollectionUtils.isEmpty(stockCodes)) {
            return pageDomain;
        }
        String stockCollect = stockCodes.stream().map(str -> {
            return str.replace(STOCK_SUFFIX, "");
        }).collect(Collectors.joining(",|,"));
        long pageSize = pageReq.getPageSize() == null ? 20 : pageReq.getPageSize();
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        Long id = pageReq.getId() == null ? 0L : pageReq.getId();
//        Integer queryType = pageReq.getQueryType();

        Page<StockNewsEntity> page = new Page<>();
        page.setCurrent(currentPage);
        page.setSize(pageSize);

        Page<StockNewsEntity> stockNewsEntityPage;
//        if (id > 0 && queryType.equals(QueryType.NEW.getType())) {
//            stockNewsEntityPage = stockNewsMapper.pageByStocksInHkAndHq(page, stockCollect, id);
//        } else {
        //====平均耗时====1334ms
        stockNewsEntityPage = stockNewsMapper.pageOldByStocksInHkAndHq(page, stockCollect, id);
//        }
        pageDomain.setSize(pageSize);
        pageDomain.setCurrent(currentPage);
        if (stockNewsEntityPage == null) {
            return pageDomain;
        }
        List<StockNewsEntity> records = stockNewsEntityPage.getRecords();
        pageDomain.setTotal(stockNewsEntityPage.getTotal());
        // 码表
        List<StockSnapshot> snapshot = this.getSnapshot();
        // 自选股码表
        List<StockSnapshot> freeSnapshots = snapshot.stream().filter(s -> stockCodes.contains(s.getCode())).collect(Collectors.toList());
        Map<String, String> tagMap = getTagMap();
        if (CollectionUtils.isNotEmpty(records)) {
            List<FreeStockNewsVo> collect = records.stream().map(record -> {
                return buildFreeVo(record, freeSnapshots, tagMap);
            }).collect(Collectors.toList());
            pageDomain.setRecords(collect);
        }
        return pageDomain;
    }

    /**
     * 资讯详情
     *
     * @param id
     * @return
     */
    @StatisticsCount
    public StockNewsDetailVo findByNewsidInHk(Long id, boolean needWarrant) {
        if (id == 0) {
            return null;
        }
        StockNewsEntity byNewsIdAndTop = stockNewsMapper.selectById(id);
        if (byNewsIdAndTop == null || PublishStatusEnum.NO == byNewsIdAndTop.getPublishStatus()) {
            return null;
        }
        StockNewsDetailVo byNewsid = new StockNewsDetailVo();
//        byNewsid.setNewsid(byNewsIdAndTop.getNewsId());
        Map<String, String> tagMap = getTagMap();
        byNewsid.setNewsType(tagMap.get(byNewsIdAndTop.getSecondCategory()));
        byNewsid.setNewsTypeName(byNewsIdAndTop.getSecondCategory());
        byNewsid.setContent(byNewsIdAndTop.getContent());
        byNewsid.setTitle(byNewsIdAndTop.getNewsTitle());
        byNewsid.setImage(byNewsIdAndTop.getImageUrl());
        byNewsid.setMarket(byNewsIdAndTop.getMarket());
        byNewsid.setSource(byNewsIdAndTop.getSource());
        byNewsid.setDateTime(byNewsIdAndTop.getDateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        byNewsid.setXdbmask(byNewsIdAndTop.getXdbmask().toString());
        byNewsid.setId(byNewsIdAndTop.getId());
        byNewsid.setStockCode(byNewsIdAndTop.getRelationStock());

        boolean add = StringUtils.isBlank(byNewsIdAndTop.getRelationStock());
        if (!add) {
            List<SimpleStockVo> simpleStockVos = new ArrayList<>();
            String[] split = byNewsIdAndTop.getRelationStock().split(",");
            List<StockSnapshot> snapshot = this.getSnapshot();
            List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockInfoList(null);
            Map<String, ComStockSimpleDto> codeStockDtoMap = simpleDtoList.stream().collect(Collectors.toMap(ComStockSimpleDto::getCode, v -> v, (o, v) -> v));
            for (String stock : split) {
                String code = stock + STOCK_SUFFIX;
                //判断股票是否为港股，非港股则转为美股
                ComStockSimpleDto stockSimpleInfo = codeStockDtoMap.get(code);
                if (ObjectUtils.isEmpty(stockSimpleInfo)){
                    code=code.replace(STOCK_SUFFIX,US_STOCK_SUFFIX);
                }
                SimpleStockVo simpleStockVo = buildStockVo(code, snapshot, needWarrant);
                add = ObjectUtils.isEmpty(simpleStockVo) ? false : simpleStockVos.add(simpleStockVo);
            }
            byNewsid.setSimpleStockVos(simpleStockVos);
        }
        return byNewsid;
    }

    public PageWithTime<FreeStockNewsVo> listNewsBySimpleStockVo(CommonNewsPage pageReq) {
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        long pageSize = pageReq.getPageSize() == null ? 10 : pageReq.getPageSize();
        String stockCode = pageReq.getStockCode();
        if (StringUtils.isBlank(stockCode) || !stockCode.contains(StringPool.DOT)) {
            log.info("stockCode传参错误，{}", stockCode);
            return null;
        }
        stockCode = stockCode.substring(0, stockCode.indexOf(StringPool.DOT));
        // 查询条件 1：股票代码 数据库中多个数据用的逗号隔开 （用FIND_IN_SET）
        Long Id = pageReq.getId() == null ? 0L : pageReq.getId();
        Page<StockNewsVo> stockNewsVoPage = new Page<>();
        stockNewsVoPage.setCurrent(currentPage);
        stockNewsVoPage.setSize(pageSize);
        Page<StockNewsVo> pageResult = stockNewsMapper.pageOldStockNewsVoInHkAndHq(stockNewsVoPage, Id, stockCode);
        stockNewsVoPage.setCurrent(currentPage);
        stockNewsVoPage.setSize(pageSize);
        PageWithTime<FreeStockNewsVo> pageDomain = new PageWithTime<>();
        pageDomain.setCurrent(pageResult.getCurrent());
        pageDomain.setSize(pageResult.getSize());
        pageDomain.setTotal(pageResult.getTotal());
        List<StockNewsVo> records = pageResult.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            Map<String, String> tagMap = getTagMap();
            List<FreeStockNewsVo> collect = records.stream().map(record -> {
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

    /**
     * 根据权证股票查询相关正股
     *
     * @param warrantCode
     * @return 正股与权证股
     */
    private List<String> getStockByWarrantStock(String warrantCode) {
        // 获取权证股票的信息
        Xnhks0701 xnhks0701 = xnhk0701Mapper.selectOne(new QueryWrapper<Xnhks0701>().eq("SECCODE", warrantCode)
                .orderByDesc("XDBMASK").last("limit 1"));

        warrantCode = warrantCode.replace(STOCK_SUFFIX, "");
        return xnhks0701 == null ? Arrays.asList(warrantCode) : xnhks0701.getF015v() == null ?
                Arrays.asList(warrantCode, autoGenericCode(xnhks0701.getF016v())) : Arrays.asList(warrantCode, autoGenericCode(xnhks0701.getF015v()));
    }

    private String autoGenericCode(String code) {
        return String.format("%05d", Integer.parseInt(code));
    }

    public PageWithTime<FreeStockNewsVo> listNewsByWarrantStockVo(CommonNewsPage pageReq) {
        long currentPage = pageReq.getCurrentPage() == null ? 1 : pageReq.getCurrentPage();
        long pageSize = pageReq.getPageSize() == null ? 10 : pageReq.getPageSize();
        String stockCode = pageReq.getStockCode();
        if (StringUtils.isBlank(stockCode) || !stockCode.contains(StringPool.DOT)) {
            log.info("stockCode传参错误，{}", stockCode);
            return null;
        }

        List<String> stockCodes = getStockByWarrantStock(stockCode);
//        stockCode = stockCode.substring(0, stockCode.indexOf(StringPool.DOT));
        String stockCollect = StringUtils.join(stockCodes, ",|,");
        Long id = pageReq.getId() == null ? 0L : pageReq.getId();
        Page<StockNewsVo> stockNewsVoPage = new Page<>();
        stockNewsVoPage.setCurrent(currentPage);
        stockNewsVoPage.setSize(pageSize);
        Page<StockNewsVo> pageResult = stockNewsMapper.pageOldStockNewsVoInHkAndHq2(stockNewsVoPage, id, stockCollect);
        stockNewsVoPage.setCurrent(currentPage);
        stockNewsVoPage.setSize(pageSize);
        PageWithTime<FreeStockNewsVo> pageDomain = new PageWithTime<>();
        pageDomain.setCurrent(pageResult.getCurrent());
        pageDomain.setSize(pageResult.getSize());
        pageDomain.setTotal(pageResult.getTotal());
        List<StockNewsVo> records = pageResult.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            Map<String, String> tagMap = getTagMap();
            List<FreeStockNewsVo> collect = records.stream().map(record -> {
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

    @Resource
    private RedisClient redisClient;

    /**
     * 获取股票快照
     * 若未开盘则剔除今日上市股票
     *
     * @return
     */
    @Override
    public List<StockSnapshot> getSnapshot() {
        //TODO-luoyj 当redis压力大时，此处查询耗时较长 1-6s
        List<StockSnapshot> snapshot = super.getSnapshot();
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        Boolean openPlate = !(hour < 9 || (hour == 9 && minutes <= 30));
        if (openPlate) {
            return snapshot;
        }
        Map<String, BigDecimal> map = redisClient.get(RedisKeyConstants.NEW_STOCK_DETAIL_MAP);
        if (MapUtil.isEmpty(map)) {
            return snapshot;
        }
        Set<String> newStocks = map.keySet();
        return snapshot.stream().filter(stockSnapshot -> !newStocks.contains(stockSnapshot.getCode())).collect(Collectors.toList());
    }
    /**
     * 删除临时股票资讯
     *
     * @param stockCode
     */
    public void delByStockCode(String stockCode) {
        String relationStockCode = fixStockNewsCode(stockCode);
        List<StockNewsEntity> stockNewsEntities = stockNewsMapper.selectList(new QueryWrapper<StockNewsEntity>().select("id,relation_stock").like("relation_stock", relationStockCode));
        if (CollUtil.isNotEmpty(stockNewsEntities)){
            stockNewsEntities.forEach(o->{
                ArrayList<String> codes = Lists.newArrayList(o.getRelationStock().split(","));
                codes.removeIf(code->code.equals(relationStockCode));
                o.setRelationStock(String.join(",",codes));
                stockNewsMapper.updateById(o);
            });
        }

    }

    private String fixStockNewsCode(String stockCode) {
        if (stockCode.contains(".hk")){
            stockCode = stockCode.replace(".hk", "");
        }else if (stockCode.contains(".us")){
            stockCode = stockCode.replace(".us", "");
        }
        return stockCode;
    }

    /**
     * 变更资讯股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    public void upInformationStockCode(String sourceCode, String targetCode) {
        String sourceRelationStockCode = fixStockNewsCode(sourceCode);
        String targetRelationStockCode = fixStockNewsCode(targetCode);
        List<StockNewsEntity> stockNewsEntities = stockNewsMapper.selectList(new QueryWrapper<StockNewsEntity>().select("id,relation_stock").like("relation_stock", sourceRelationStockCode));
        if (CollUtil.isNotEmpty(stockNewsEntities)){
            stockNewsEntities.forEach(o->{
                ArrayList<String> codes = Lists.newArrayList(o.getRelationStock().split(","));
                codes.removeIf(code->code.equals(sourceRelationStockCode));
                codes.add(targetRelationStockCode);
                o.setRelationStock(String.join(",",codes));
                stockNewsMapper.updateById(o);
            });
        }
    }

    /**
     * 新增模拟股票资讯数据
     *
     * @param simulateCode 模拟股票code
     */
    public void saveSimulateInformation(String simulateCode) {
        String realCode = simulateCode.replace("-test", "").replace("-t","" );
        realCode = fixStockNewsCode(realCode);
        String targetSimulateCode = fixStockNewsCode(simulateCode);
        List<StockNewsEntity> stockNewsEntities = stockNewsMapper.selectList(new QueryWrapper<StockNewsEntity>().like("relation_stock", realCode).orderByDesc("id").last("limit 20"));
        if (CollUtil.isNotEmpty(stockNewsEntities)){
            stockNewsEntities.forEach(o->{
                ArrayList<String> codes = Lists.newArrayList(o.getRelationStock().split(","));
                codes.add(targetSimulateCode);
                o.setRelationStock(String.join(",",codes));
                stockNewsMapper.updateById(o);
            });
        }
    }
}
