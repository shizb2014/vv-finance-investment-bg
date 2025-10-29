package com.vv.finance.investment.bg.api.impl.stock;


import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fenlibao.security.sdk.ws.core.model.req.IndhktryReq;
import com.fenlibao.security.sdk.ws.core.model.req.RankInduReq;
import com.fenlibao.security.sdk.ws.core.model.req.RankMin5Req;
import com.fenlibao.security.sdk.ws.core.model.req.RankReq;
import com.fenlibao.security.sdk.ws.core.model.resp.IndhktryResp;
import com.fenlibao.security.sdk.ws.core.model.resp.RankInduResp;
import com.fenlibao.security.sdk.ws.core.model.resp.RankMin5Resp;
import com.fenlibao.security.sdk.ws.core.model.resp.RankResp;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.omdc.OmdcKind;
import com.vv.finance.common.constants.omdc.OmdcMode;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.investment.bg.api.stock.StockRankingApi;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.entity.uts.Xnhk0004;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0004Mapper;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.stock.rank.dto.StockIndustryDto;
import com.vv.finance.investment.bg.stock.rank.entity.IndustryRanking;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.entity.Stock5minRanking;
import com.vv.finance.investment.bg.stock.rank.entity.StockRanking;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import com.vv.finance.investment.gateway.api.stock.IStockBusinessApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2020/10/29 16:19
 */
@DubboService(group="${dubbo.investment.bg.service.group:bg}", registry="bgservice")
@Slf4j
public class StockRankingApiImpl implements StockRankingApi {
    @DubboReference(group = "${dubbo.investment.gateway.service.group:gateway}", registry = "gatewayservice")
    IStockBusinessApi stockBusinessApi;
    @Autowired
    private IStockDefineService stockDefineService;
    @Autowired
    private IIndustrySubsidiaryService industrySubsidiaryService;

    @Autowired
    private Xnhk0004Mapper xnhk0004Mapper;
    @Autowired
    private StockCache stockCache;

    @Override
    public ResultT<List<Stock5minRanking>> stock5minRanking(RankMin5Req rankMin5Req) {
        ResultT<List<RankMin5Resp>> listResultT = stockBusinessApi.rankMin5(rankMin5Req);
        List<Stock5minRanking> stock5minRankings=listResultT.getData().stream().map(input -> {
            Stock5minRanking stock5minRanking=new Stock5minRanking();
            BeanUtils.copyProperties(input,stock5minRanking);
            return stock5minRanking;
        }).collect(Collectors.toList());
        return ResultT.success(stock5minRankings);
    }

    @Override
    public ResultT<List<StockRanking>> stockRanking(RankReq rankReq) {
        ResultT<List<RankResp>> listResultT = stockBusinessApi.rank(rankReq);
        List<StockRanking> stockRankings = listResultT.getData().stream().map(rankResp -> {
            StockRanking stockRanking = new StockRanking();
            BeanUtils.copyProperties(rankResp, stockRanking);
            return stockRanking;
        }).collect(Collectors.toList());
        return ResultT.success(stockRankings);
    }

    @Override
    public ResultT<List<IndustryRanking>> industryRanking(RankInduReq rankInduReq) {
        ResultT<List<RankInduResp>> listResultT = stockBusinessApi.rankIndu(rankInduReq);
        List<IndustryRanking> rankingList=listResultT.getData().stream().map(rankInduResp -> {
            IndustryRanking industryRanking=new IndustryRanking();
            BeanUtils.copyProperties(rankInduResp,industryRanking);
            //融聚汇新版行业code 使用二级类别，需要手动处理成四位
            industryRanking.setSymbol(rankInduResp.getSymbol().substring(2,6));
            return industryRanking;
        }).collect(Collectors.toList());
        return ResultT.success(rankingList);
    }

    @Override
    public ResultT<IndustryRanking> industrySubsidiary(IndhktryReq indhktryReq) {
        ResultT<IndhktryResp> indhktry = stockBusinessApi.indhktry(indhktryReq);
        IndustryRanking industryRanking=new IndustryRanking();
        BeanUtils.copyProperties(indhktry.getData(),industryRanking);
        return ResultT.success(industryRanking);
    }

    @Override
    // @Cacheable(value = RedisKeyConstants.BG_STOCKINFO_STOCKDEFINE_INDUSTRY, keyGenerator = "keyGenerator")
    public ResultT<StockIndustryDto> queryStockIndustry(String code) {
        StockIndustryDto stockIndustryDto = new StockIndustryDto();
        stockIndustryDto.setStockCode(code);
        stockIndustryDto.setIndustrySubsidiary(industrySubsidiaryService.getStockIndustry(code));
        return ResultT.success(stockIndustryDto);
    }


    @Override
    public IndustrySubsidiary getIndustrySubsidiary(String code) {
        return industrySubsidiaryService.getStockIndustry(code);
    }

    @Override
    // @Cacheable(value = RedisKeyConstants.BG_STOCKINFO_STOCKDEFINE_INDUSTRY,keyGenerator = "keyGenerator")
    public ResultT<List<IndustrySubsidiary>> listIndustrySubsidiary() {

        // return ResultT.success(industrySubsidiaryService.list(new QueryWrapper<IndustrySubsidiary>().orderByAsc("first_rym")));
        return ResultT.success(industrySubsidiaryService.getAllIndustry());
    }

    @Override
    @CacheEvict (value = RedisKeyConstants.BG_STOCKINFO_STOCKDEFINE_INDUSTRY,allEntries = true)
    public void initIndustrySubsidiary() {

        RankInduReq rankInduReq=new RankInduReq();
        rankInduReq.setKind(OmdcKind.UP); //涨幅
        rankInduReq.setLevel("1");
        rankInduReq.setMode(OmdcMode.RT);  //实时
        rankInduReq.setNumber(1000);
        //行业排行榜
        ResultT<List<RankInduResp>> rankIndu = stockBusinessApi.rankIndu(rankInduReq);
        //股票code 行业code
        Map<String,String> stockIndustryMap=new ConcurrentHashMap<>(2600);
        //获取行业信息
        // List<Xnhk0004> xnhk0004s = xnhk0004Mapper.selectList(new QueryWrapper<Xnhk0004>());
        // Map<String,Xnhk0004> xnhk0004Map =
        //         xnhk0004s.stream().collect(Collectors.toMap(item -> item.getCode(), Function.identity()));
        List<IndustrySubsidiary> allIndustry = industrySubsidiaryService.getAllIndustry();
        Map<String, String> industryNameMap = allIndustry.stream().collect(Collectors.toMap(IndustrySubsidiary::getCode, IndustrySubsidiary::getName, (o, v) -> v));
        rankIndu.getData().forEach(rankInduResp -> {
            IndhktryReq indhktryReq=new IndhktryReq();
            indhktryReq.setCode(rankInduResp.getSymbol());
            indhktryReq.setKind(OmdcKind.UP);
            indhktryReq.setMode(OmdcMode.RT);
            indhktryReq.setNumber(1000);
            //行业明细
            ResultT<IndhktryResp> indhktry = stockBusinessApi.indhktryV3(indhktryReq);
            IndhktryResp indhktryResp=indhktry.getData();
            if(ObjectUtils.isEmpty(indhktryResp)){
                return;
            }
            IndustrySubsidiary industrySubsidiary=new IndustrySubsidiary();
            industrySubsidiary.setLevel(indhktryResp.getLevel());

            industrySubsidiary.setAmount(new BigDecimal(indhktryResp.getAmount()));
            industrySubsidiary.setChgPct(new BigDecimal(indhktryResp.getChg_pct()));
            industrySubsidiary.setMktTime(LocalDateTime.parse(indhktryResp.getMkt_time(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            //融聚汇新版行业code 使用二级类别，需要手动处理成四位
            industrySubsidiary.setCode(rankInduResp.getSymbol());
            //行业名称还是使用老版本的，从v1接口获取
            industrySubsidiary.setName(industryNameMap.get(industrySubsidiary.getCode()));
            if (StrUtil.isNotBlank(industrySubsidiary.getName())) {
                String pym= PinyinUtil.getFirstLetter(industrySubsidiary.getName().substring(0,1),"");
                industrySubsidiary.setFirstRym(pym.toUpperCase());
            }
            industrySubsidiaryService.saveOrUpdate(industrySubsidiary,new UpdateWrapper<IndustrySubsidiary>().eq("code",industrySubsidiary.getCode()));
            //RankResp 是IndhktryResp 的属性
            Map<String, String> collect = indhktryResp.getStock_list().stream().collect(Collectors.toMap(RankResp::getSymbol, rankResp -> rankInduResp.getSymbol()));
            stockIndustryMap.putAll(collect);

        });

//        stockIndustryMap.forEach((key, value) -> {
//           StockDefine stockDefine = new StockDefine();
//           stockDefine.setIndustryCode(value);
//           stockDefineService.update(stockDefine, new UpdateWrapper<StockDefine>().eq("code", key));
//       });
    }


    @Override
    @CacheEvict (value = RedisKeyConstants.BG_STOCKINFO_STOCKDEFINE_INDUSTRY,allEntries = true)
    public ResultT<?> updateIndustryPreClose(List<IndustrySubsidiary> industrySubsidiaryList) {
        industrySubsidiaryList.forEach(industrySubsidiary -> industrySubsidiaryService.update(industrySubsidiary,new UpdateWrapper<IndustrySubsidiary>().eq("code",industrySubsidiary.getCode())));
        return ResultT.success();
    }


}
