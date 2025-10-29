package com.vv.finance.investment.bg.handler.information;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.entity.uts.NewsHk;
import com.vv.finance.investment.bg.mapper.uts.NewsHkMapper;
import com.vv.finance.investment.bg.stock.information.handler.InformationHandlerV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/9/15 11:09
 */
@RequiredArgsConstructor
@Component
@CanalTable("news_hk")
@Slf4j
public class NewsHkHandler extends AbstractStockNewsHandler  implements EntryHandler<NewsHk> {

    private final NewsHkMapper newsHkMapper;


    private final String news_hk="news_hk";
    private final String news_us="news_us";
    private final String market_hk="港股";
    private final String market_us="美股";

    @Autowired
    private InformationHandlerV2 informationHandlerV2;
    @Override
    public void insert(NewsHk t) {
        log.info("NewsHk add={}",t);
        StockNewsEntity stockNewsEntity = newsHkToStockNewsEntity(t);
        save(stockNewsEntity);
        //更新缓存资讯表与关联股票映射关系
        informationHandlerV2.updateRelationCodeMap(stockNewsEntity);
    }
    @Override
    public void update(NewsHk before, NewsHk after) {
        log.info("NewsHk update before={},after={}",before,after);
        updateByNewsIdAndTopCategory(newsHkToStockNewsEntity(after));
    }
    @Override
    public void delete(NewsHk t) {
        log.info("NewsHk delete={}",t);
        remove(t.getNewsid(),"news_hk");
    }


    @Override
    public void sync() {
        long page=1;
        long size=1000;
        while (true) {
            List<NewsHk> records = newsHkMapper.selectPage(new Page<>(page, size), new QueryWrapper<>()).getRecords();

            if(CollUtil.isNotEmpty(records)) {
                List<StockNewsEntity> stockNewsEntities = records.stream().map(this::newsHkToStockNewsEntity).collect(Collectors.toList());
                batchSave(stockNewsEntities);
            }
            if(records.size()<size){
                break;
            }
            page++;
        }
    }
    private StockNewsEntity newsHkToStockNewsEntity(NewsHk item){
        StockNewsEntity stockNewsEntity=new StockNewsEntity();
        stockNewsEntity.setNewsId(item.getNewsid());
        if(StringUtils.isNotBlank(item.getMarket())&&item.getMarket().contains(market_us)){
            stockNewsEntity.setTopCategory(news_us);
        }else {
            stockNewsEntity.setTopCategory(news_hk);
        }

        stockNewsEntity.setSecondCategory(item.getNewstype());
        stockNewsEntity.setTertiaryCategory(item.getNewstype2());
        stockNewsEntity.setSource(item.getSource());
        stockNewsEntity.setKeyword(item.getKeyword());
        stockNewsEntity.setNewsTitle(item.getNewstitle());
        if(StringUtils.isNotBlank(item.getRelatesymbol())) {
            stockNewsEntity.setRelationStock(item.getRelatesymbol().replace("、",","));
        }
        stockNewsEntity.setDate(item.getDate());
        stockNewsEntity.setTime(item.getTime());
        stockNewsEntity.setContent(item.getContent());
        stockNewsEntity.setAuthor(item.getAuthor());
        stockNewsEntity.setMarket(item.getMarket());
        stockNewsEntity.setXdbmask(item.getXdbmask());
        stockNewsEntity.setImageUrl(item.getImageUrl());
        LocalTime localTime=LocalTime.parse(item.getTime(), DateTimeFormatter.ofPattern("HH:mm"));
        stockNewsEntity.setDateTime(LocalDateTime.of(item.getDate(),localTime));
        return stockNewsEntity;
    }
}
