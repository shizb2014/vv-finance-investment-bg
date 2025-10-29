package com.vv.finance.investment.bg.handler.information;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.entity.uts.NewsCalendar;
import com.vv.finance.investment.bg.mapper.uts.NewsCalendarMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@CanalTable("news_calendar")
@Slf4j
public class NewsCalendarHandler extends AbstractStockNewsHandler implements EntryHandler<NewsCalendar> {

    private final NewsCalendarMapper newsCalendarMapper;



    @Override
    public void insert(NewsCalendar t) {
        log.info("NewsCalendar add={}", t);
        if("0".equals(t.getType())) {
            StockNewsEntity stockNewsEntity = newsCalendarToStockNewsEntity(t);
            save(stockNewsEntity);
        }
    }

    @Override
    public void update(NewsCalendar before, NewsCalendar after) {
        log.info("NewsCalendar update before={},after={}", before, after);
        if("0".equals(after.getType())) {
            updateByNewsIdAndTopCategory(newsCalendarToStockNewsEntity(after));
        }
    }

    @Override
    public void delete(NewsCalendar t) {
        log.info("NewsCalendar delete={}", t);
        remove(t.getId().longValue(), "news_calendar");
    }

    @Override
    public void sync() {
        long page = 1;
        long size = 1000;
        while (true) {
            List<NewsCalendar> records = newsCalendarMapper.selectPage(new Page<>(page, size), new QueryWrapper<NewsCalendar>().eq("type","0")).getRecords();

            if (CollUtil.isNotEmpty(records)) {
                List<StockNewsEntity> stockNewsEntities = records.stream().map(this::newsCalendarToStockNewsEntity).collect(Collectors.toList());
                batchSave(stockNewsEntities);
            }
            if (records.size() < size) {
                break;
            }
            page++;
        }
    }

    private StockNewsEntity newsCalendarToStockNewsEntity(NewsCalendar item) {
        StockNewsEntity stockNewsEntity = new StockNewsEntity();
        stockNewsEntity.setNewsId(item.getId().longValue());
        stockNewsEntity.setTopCategory("news_calendar");
        stockNewsEntity.setSecondCategory("经济事件");
//        stockNewsEntity.setPublishStatus(0);
//        stockNewsEntity.setPublishTerminal(2);
        stockNewsEntity.setDate(item.getDate());
        stockNewsEntity.setTime(item.getTime());
        stockNewsEntity.setReadingVolume(0L);
        stockNewsEntity.setContent(item.getContent());
        stockNewsEntity.setXdbmask(item.getXdbmask());
        LocalTime localTime = LocalTime.parse(item.getTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        stockNewsEntity.setDateTime(LocalDateTime.of(item.getDate(), localTime));
        return stockNewsEntity;
    }
}
