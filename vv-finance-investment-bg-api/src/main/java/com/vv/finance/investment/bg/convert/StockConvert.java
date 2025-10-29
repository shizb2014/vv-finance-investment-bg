package com.vv.finance.investment.bg.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.stock.indicator.entity.BaseStockIndicator;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import com.vv.finance.investment.gateway.dto.resp.AllMaKlineResp;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2020/12/26 15:27
 */
public class StockConvert {

    public static<E extends StockKline> List<StockKline> convertStockKline(Page<E> ePage){
        return ePage.getRecords().stream().map(stockKlineEntity -> {
            stockKlineEntity.setDate(new Date(stockKlineEntity.getTime()));
            return (StockKline) stockKlineEntity;
        }).collect(Collectors.toList());
    }
    public static<E extends StockKline> List<StockKline> convertStockKlineList(List<E> list){
        return list.stream().map(stockKlineEntity -> {
            stockKlineEntity.setDate(new Date(stockKlineEntity.getTime()));
            return (StockKline) stockKlineEntity;
        }).collect(Collectors.toList());
    }
    public static<E extends BaseStockIndicator> List<BaseStockIndicator> convertStockIndicator(List<E> list){
        return list.stream().map(stockIndicatorEntity -> {
            stockIndicatorEntity.setDateNotes(new Date(stockIndicatorEntity.getDate()));
            return (BaseStockIndicator) stockIndicatorEntity;
        }).collect(Collectors.toList());
    }
    public static List<StockKline> convertStockKline(List<AllMaKlineResp> allMaKlineResps){
        return  allMaKlineResps.stream().map(allMaKlineResp -> {
            StockKline stockKline = new StockKline();
            BeanUtils.copyProperties(allMaKlineResp, stockKline);
            return stockKline;
        }).collect(Collectors.toList());
    }


}
