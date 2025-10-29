package com.vv.finance.investment.bg.handler.information;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.entity.uts.News24hours;
import com.vv.finance.investment.bg.mapper.uts.News24hoursMapper;
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
@CanalTable("news_24hours")
@Slf4j
public class News24HoursHandler extends AbstractStockNewsHandler implements EntryHandler<News24hours> {

    private final News24hoursMapper news24hoursMapper;
    private final String IGNORE_TYPE="财经日历";


    @Override
    public void insert(News24hours t) {
      log.info("News24hours add={}",t);
      if(!IGNORE_TYPE.equals(t.getLevel1())) {
          StockNewsEntity stockNewsEntity = news24hoursToStockNewsEntity(t);
          save(stockNewsEntity);
      }
    }
    @Override
    public void update(News24hours before, News24hours after) {
        log.info("News24hours update before={},after={}",before,after);
        if(!IGNORE_TYPE.equals(after.getLevel1())) {
            updateByNewsIdAndTopCategory(news24hoursToStockNewsEntity(after));
        }
    }
    @Override
    public void delete(News24hours t) {
        log.info("News24hours delete={}",t);
        remove(t.getId(),"news_24hours");
    }

    @Override
    public void sync() {
        long page=1;
        long size=1000;
        while (true) {
            List<News24hours> records = news24hoursMapper.selectPage(new Page<>(page, size), new QueryWrapper<News24hours>().ne("level1",IGNORE_TYPE)).getRecords();

            if(CollUtil.isNotEmpty(records)) {
                List<StockNewsEntity> stockNewsEntities = records.stream().map(this::news24hoursToStockNewsEntity).collect(Collectors.toList());
                batchSave(stockNewsEntities);
            }
            if(records.size()<size){
                break;
            }
            page++;
        }
    }
    private StockNewsEntity news24hoursToStockNewsEntity(News24hours item){
        StockNewsEntity stockNewsEntity=new StockNewsEntity();
        stockNewsEntity.setNewsId(item.getId());
        stockNewsEntity.setTopCategory("news_24hours");
        stockNewsEntity.setSecondCategory(item.getLevel1());
        stockNewsEntity.setTertiaryCategory(item.getLevel2());
        stockNewsEntity.setDate(item.getDate());
        stockNewsEntity.setTime(item.getTime());

        stockNewsEntity.setContent(item.getContent());
        stockNewsEntity.setXdbmask(item.getXdbmask());
        LocalTime localTime=LocalTime.parse(item.getTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        stockNewsEntity.setDateTime(LocalDateTime.of(item.getDate(),localTime));
        return stockNewsEntity;
    }
}
