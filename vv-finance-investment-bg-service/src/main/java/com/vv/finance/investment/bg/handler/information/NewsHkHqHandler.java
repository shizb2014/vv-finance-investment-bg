package com.vv.finance.investment.bg.handler.information;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.entity.uts.NewsHkHq;
import com.vv.finance.investment.bg.mapper.uts.NewsHkHqMapper;
import com.vv.finance.investment.bg.stock.information.handler.InformationHandlerV2;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/9/15 11:09
 */
@RequiredArgsConstructor
@Component
@CanalTable("news_hk_hq")
@Slf4j
public class NewsHkHqHandler extends AbstractStockNewsHandler implements EntryHandler<NewsHkHq> {

    private final NewsHkHqMapper newsHkHqMapper;

    private static String NEWS_TYPE = "板块异动";

    @Autowired
    private IIndustrySubsidiaryService industrySubsidiaryService;

    @Autowired
    private InformationHandlerV2 informationHandlerV2;

    @Override
    public void insert(NewsHkHq t) {
        log.info("NewsHkHq add={}",t);
        StockNewsEntity stockNewsEntity = newsHkHqToStockNewsEntity(t);
        save(stockNewsEntity);
        //更新缓存资讯表与关联股票映射关系
        informationHandlerV2.updateRelationCodeMap(stockNewsEntity);
    }
    @Override
    public void update(NewsHkHq before, NewsHkHq after) {
        log.info("NewsHkHq update before={},after={}",before,after);
        updateByNewsIdAndTopCategory(newsHkHqToStockNewsEntity(after));
    }
    @Override
    public void delete(NewsHkHq t) {
        log.info("NewsHkHq delete={}",t);
        remove(t.getNewsid(),"news_hk_hq");
    }



    @Override
    public void sync() {
        long t = System.currentTimeMillis();
        long page=1;
        long size=1000;
        while (true) {
            List<NewsHkHq> records = newsHkHqMapper.selectPage(new Page<>(page, size), new QueryWrapper<>()).getRecords();
            if(CollUtil.isNotEmpty(records)) {
                List<StockNewsEntity> stockNewsEntities = records.stream().map(this::newsHkHqToStockNewsEntity).collect(Collectors.toList());
                batchSave(stockNewsEntities);
            }
            if(records.size()<size){
                break;
            }
            page++;
        }
        log.info("同步news_hk_hq结束：{} s", (System.currentTimeMillis() - t)/1000 );
    }
    private StockNewsEntity newsHkHqToStockNewsEntity(NewsHkHq newsHkHq){
        StockNewsEntity stockNewsEntity=new StockNewsEntity();
        stockNewsEntity.setNewsId(newsHkHq.getNewsid());
        stockNewsEntity.setTopCategory("news_hk_hq");
        stockNewsEntity.setSecondCategory(newsHkHq.getNewstype());
        stockNewsEntity.setSource(newsHkHq.getSource());
        stockNewsEntity.setKeyword(newsHkHq.getKeyword());
        String title  = newsHkHq.getNewstitle();
        if(title.length() > 0 && title.startsWith("<")) {
            int index = title.indexOf(">");
            String oldChar = title.substring(0,index + 1);
            title = title.replace(oldChar,"");
        }
        stockNewsEntity.setNewsTitle(title);
        if(StringUtils.isNotBlank(newsHkHq.getRelatesymbol())) {
            stockNewsEntity.setRelationStock(newsHkHq.getRelatesymbol().replace("、",","));
        }
        List<IndustrySubsidiary> industrySubsidiaryList = industrySubsidiaryService.getAllIndustry();
        // List<IndustrySubsidiary> industrySubsidiaryList = industrySubsidiaryService.list(new QueryWrapper<IndustrySubsidiary>().orderByAsc("first_rym"));
        industrySubsidiaryList.sort(Comparator.comparing(IndustrySubsidiary::getName).reversed());
        List<String> relationIndustry = new ArrayList<>();
        List<String> relationCodeName = new ArrayList<>();
        if (newsHkHq.getNewstype().equals(NEWS_TYPE)){
            stockNewsEntity.setRelationStock(null);
            industrySubsidiaryList.forEach(vo -> {
                List<String> collects = relationCodeName.stream().filter(t -> t.contains(vo.getName())).collect(Collectors.toList());
                if (newsHkHq.getContent().contains(vo.getName()) && (
                        CollectionUtils.isEmpty(relationCodeName) || CollectionUtils.isEmpty(collects))) {
                    relationIndustry.add(vo.getCode());
                    relationCodeName.add(vo.getName());
                }
            });
        }
        stockNewsEntity.setRelationIndustry(CollectionUtils.isNotEmpty(relationIndustry)? String.join(",", relationIndustry):null);
        stockNewsEntity.setDate(newsHkHq.getDate());
        stockNewsEntity.setTime(newsHkHq.getTime());
        stockNewsEntity.setContent(newsHkHq.getContent());
        stockNewsEntity.setAuthor(newsHkHq.getAuthor());
        stockNewsEntity.setMarket(newsHkHq.getMarket());
        stockNewsEntity.setXdbmask(newsHkHq.getXdbmask());
        LocalTime localTime=LocalTime.parse(newsHkHq.getTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        stockNewsEntity.setDateTime(LocalDateTime.of(newsHkHq.getDate(),localTime));
        return stockNewsEntity;
    }
}
