package com.vv.finance.investment.bg.job.stock;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2020/11/19 14:47
 */
@Service
@RequiredArgsConstructor
public class StockCommonService {

   private final StockInfoApi stockInfoApi;
    public List<String> getStockDefine(String param){
        int size=0;
        int page=0;
        //param size#page
        if(StringUtils.isNotEmpty(param)){
            JSONObject jsonObject = JSONObject.parseObject(param);
            String codes=jsonObject.getString("codes");
            if (StringUtils.isNotBlank(codes)){
                return Lists.newArrayList(codes.split(","));
            }
            String sizeStr=jsonObject.getString("size");
            String pageStr=jsonObject.getString("page");

            if(StringUtils.isNumeric(sizeStr)){
                size=Integer.parseInt(sizeStr);
            }
            if(StringUtils.isNumeric(pageStr)){
                page=Integer.parseInt(pageStr);
            }
        }

        ResultT<Page<StockDefine>> pageResultT = stockInfoApi.listStockDefine(size, page, "MAIN");
        return pageResultT.getData().getRecords().stream().map(StockDefine::getCode).collect(Collectors.toList());
    }
    public List<String> getStockDefineCodes(String param){

        if(StringUtils.isNotEmpty(param)){
            JSONObject jsonObject = JSONObject.parseObject(param);
            String codes=jsonObject.getString("codes");
            if (StringUtils.isNotBlank(codes)){
                return Lists.newArrayList(codes.split(","));
            }

        }

        ResultT<List<String>> pageResultT = stockInfoApi.allStockDefineCodes();
        return pageResultT.getData();
    }
}
