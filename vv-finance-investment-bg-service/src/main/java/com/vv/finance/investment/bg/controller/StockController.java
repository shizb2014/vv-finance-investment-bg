package com.vv.finance.investment.bg.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.domain.PageDomain;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.dto.resp.FinancialReportDto;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.investment.bg.api.f10.F10TableTemplateApi;
import com.vv.finance.investment.bg.api.uts.UtsInfoService;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.f10.F10TableTemplateV2;
import com.vv.finance.investment.bg.handler.stock.StockHandler;
import com.vv.finance.investment.bg.handler.uts.sync.AbstractF10CommonHandler;
import com.vv.finance.investment.bg.job.uts.FinancialAnalysisJob;
import com.vv.finance.investment.bg.stock.f10.service.IStockHolderChangeService;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.quotation.integration.api.uts.UtsInfoApi;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/bg/stock")
@Slf4j
public class StockController {

    @Resource
    private RedisClient redisClient;

    @Resource
    FinancialAnalysisJob financialAnalysisJob;

    @Resource
    F10TableTemplateApi f10TableTemplateApi;

    @Resource
    StockDefineMapper stockDefineMapper;

    @Resource
    IStockHolderChangeService stockHolderChangeService;

    @Resource
    StockHandler stockHandler;

    @Resource
    StockCache stockCache;

    @Resource
    UtsInfoService utsInfoApi;

    @GetMapping("/queryLocalInfoMap")
    @ApiOperation(value = "查询内存中股票缓存", notes = "查询内存中股票缓存")
    public ResultT<Map<String, ComStockSimpleDto>> queryLocalInfoMap() {
        Map<String, ComStockSimpleDto> stockInfos = stockCache.queryLocalInfoMap(null);
        return ResultT.success(stockInfos);
    }

    @GetMapping("/queryStockDtoList")
    @ApiOperation(value = "根据stockId批量查询", notes = "根据stockId批量查询")
    public ResultT<List<ComStockSimpleDto>> queryStockDtoList(String stockIds) {
        List<Long> idList = StrUtil.split(stockIds, ",").stream().map(Long::valueOf).collect(Collectors.toList());
        List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockDtoList(idList);
        return ResultT.success(simpleDtoList);
    }

    @GetMapping("/querySouthwardCapitalList2")
    @ApiOperation(value = "根据stockId批量查询", notes = "根据stockId批量查询")
    public ResultT<List<ComStockSimpleDto>> querySouthwardCapitalList2(String stockIds) {
        List<Long> idList = StrUtil.split(stockIds, ",").stream().map(Long::valueOf).collect(Collectors.toList());
        List<ComStockSimpleDto> simpleDtoList = stockCache.queryStockDtoList(idList);
        return ResultT.success(simpleDtoList);
    }

    @GetMapping("/getPcTableSourceV2")
    @ApiOperation(value = "财务报表查询", notes = "财务报表查询")
    public ResultT<PageDomain<List<F10TableTemplateV2>>> getPcTableSourceV2(String stockIds) {
        PageDomain<List<F10TableTemplateV2>> financialTable = f10TableTemplateApi.getPcTableSourceV2("00700.hk", 3, 2, 1, 6);

        return ResultT.success(financialTable);
    }

    @GetMapping("/getFinancialReport")
    @ApiOperation(value = "财务报表查询", notes = "财务报表查询")
    public ResultT<List<FinancialReportDto>> getFinancialReport(String id, int marketType, int pageSize, Long startTime) {
        List<FinancialReportDto> financialTable = f10TableTemplateApi.getFinancialReport(id, marketType, pageSize, startTime);
        return ResultT.success(financialTable);
    }

    @GetMapping("/getXnhk0127")
    public ResultT getXnhk0127(){
        return ResultT.success(utsInfoApi.getXnhk0127(DateUtil.date(LocalDate.now())));
    }
}

