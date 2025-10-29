package com.vv.finance.investment.bg.stock.select;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.strategy.filter.IndexConstants;
import com.vv.finance.common.domain.filter.EnumValues;
import com.vv.finance.common.enums.StockTypeEnum;
import com.vv.finance.investment.bg.api.quotation.IQuotationService;
import com.vv.finance.investment.bg.api.stock.select.StockFilterRelatedApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.uts.Xnhk1002;
import com.vv.finance.investment.bg.entity.uts.Xnhk1301;
import com.vv.finance.investment.bg.entity.uts.Xnhks0101;
import com.vv.finance.investment.bg.mapper.uts.Xnhk1002Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk1301Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0101Mapper;
import com.vv.finance.investment.bg.mongo.model.F10CashFlowEntity;
import com.vv.finance.investment.bg.mongo.model.F10FinProfitEntity;
import com.vv.finance.investment.bg.mongo.model.F10InsureProfitEntity;
import com.vv.finance.investment.bg.mongo.model.F10NoFinProfitEntity;
import com.vv.finance.investment.bg.stock.f10.handler.F10SourceHandler;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.vv.finance.investment.bg.stock.select.dto.CashFlowSelectDto;
import com.vv.finance.investment.bg.stock.select.dto.ProfitSelectDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author wsliang
 * @date 2022/2/17 14:51
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
public class StockFilterRelatedApiImpl implements StockFilterRelatedApi {

    @Resource
    private Xnhks0101Mapper xnhks0101Mapper;

    @Resource
    private Xnhk1002Mapper xnhk1002Mapper;

    @Resource
    private Xnhk1301Mapper xnhk1301Mapper;

    @Resource
    private IStockDefineService stockDefineService;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private F10SourceHandler f10SourceHandler;

    @Autowired
    private Executor asyncServiceExecutor;

    private final String[] F014V_FILTERS = {"OS", "PC", "RS"};

    private final String[] FINANCE_TYPES = {"I", "F", "Q1", "Q3"};

    private final String CREATE_TIME = "CREATE_TIME";

    private final long EXPIRE_TIME = 300000;

    private final String COLLECTION_FIN_PROFIT = "f10_profit_financial";
    private final String COLLECTION_NO_FIN_PROFIT = "f10_profit_no_financial";
    private final String COLLECTION_INSURE_PROFIT = "f10_profit_insure";
    private final String COLLECTION_CASH_FLOW = "f10_cash_flow";

    @Value("#{'${hk.index.code}'}")
    private String indexStr;


    @DubboReference(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
    private IQuotationService quotationService;

    @Override
    public ResultT<Map<String, Object>> getPlateMap() {
        Map<String, Object> hmget = redisClient.hmget(RedisKeyConstants.BG_MAP_STOCK_PLATE);
        if (CollectionUtils.isEmpty(hmget)) {
            List<Xnhks0101> xnhks0101s = xnhks0101Mapper.selectList(Wrappers.lambdaQuery(Xnhks0101.class).select(Xnhks0101::getSeccode, Xnhks0101::getF015v)
                    .isNotNull(Xnhks0101::getF015v)
                    .in(Xnhks0101::getF014v, F014V_FILTERS));
            hmget = xnhks0101s.stream().collect(Collectors.toMap(Xnhks0101::getSeccode, Xnhks0101::getF015v, (v1, v2) -> v1));
            // 有效期一天
            redisClient.hmset(RedisKeyConstants.BG_MAP_STOCK_PLATE, hmget, 86400);
        }
        return ResultT.success(hmget);
    }

    @Override
    public ResultT<List<EnumValues>> enumsPlate() {
        List<EnumValues> hmget = Objects.requireNonNull(redisClient.lGet(RedisKeyConstants.BG_LIST_PLATES, 0, -1)).stream().map(item -> (EnumValues) item).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(hmget)) {
            hmget = xnhks0101Mapper.listPlates();
            List<Object> collect = Objects.requireNonNull(hmget).stream().map(item -> (Object) item).collect(Collectors.toList());
            // 有效期一天
            redisClient.lSet(RedisKeyConstants.BG_LIST_PLATES, collect, 86400);
        }
        return ResultT.success(hmget);
    }

    @Override
    public ResultT<List<EnumValues>> enumsConcept() {
        List<EnumValues> list = Objects.requireNonNull(redisClient.lGet(RedisKeyConstants.BG_LIST_CONCEPTS, 0, -1)).stream().map(item -> (EnumValues) item).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            list = xnhk1301Mapper.listConcepts();
            list.stream().forEach(l -> l.setFeature(PinyinUtil.getFirstLetter(l.getName(), "").substring(0, 1).toUpperCase()));
            list.sort(Comparator.comparing(EnumValues::getFeature));
            List<Object> collect = Objects.requireNonNull(list).stream().map(item -> (Object) item).collect(Collectors.toList());
            // 有效期一天
            redisClient.lSet(RedisKeyConstants.BG_LIST_CONCEPTS, collect, 86400);
        }
        return ResultT.success(list);
    }

    @Override
    public ResultT<Map<String, Object>> getIndexStocks() {
        Map<String, Object> hmget = redisClient.hmget(RedisKeyConstants.BG_MAP_INDEX_STOCK);
        if (MapUtils.isEmpty(hmget)) {
            List<String> condition = new ArrayList<>(Arrays.asList("OS", "PC", "RS"));
            List<Xnhks0101> xnhks0101s = xnhks0101Mapper.selectList(new QueryWrapper<Xnhks0101>()
                    .in("F014V", condition));
            List<String> hsList = xnhks0101s.stream().filter(t -> "Y".equals(t.getF019v())).map(Xnhks0101::getSeccode).collect(Collectors.toList());
            List<String> gqList = xnhks0101s.stream().filter(t -> "Y".equals(t.getF020v())).map(Xnhks0101::getSeccode).collect(Collectors.toList());
            List<String> hcList = xnhks0101s.stream().filter(t -> "Y".equals(t.getF021v())).map(Xnhks0101::getSeccode).collect(Collectors.toList());
            hmget.put(IndexConstants.HS, hsList);
            hmget.put(IndexConstants.GQ, gqList);
            hmget.put(IndexConstants.HC, hcList);
            // 有效期一天
            redisClient.hmset(RedisKeyConstants.BG_MAP_INDEX_STOCK, hmget, 86400);
        }
        return ResultT.success(hmget);
    }

    @Override
    public ResultT<Map<String, List<String>>> getIndexStockMap() {
        Map<String, List<String>> hmget = redisClient.hmget(RedisKeyConstants.BG_MAP_INDEX_STOCK_MAP);
        if (MapUtils.isEmpty(hmget)) {
            List<Xnhk1002> xnhk1002s = xnhk1002Mapper.listMembersAndIndex("(".concat(indexStr).concat(")"));
            List<String> hsList = xnhk1002s.stream().filter(t -> "0000100".equals(t.getSeccode())).map(Xnhk1002::getF001v).collect(Collectors.toList());
            List<String> gqList = xnhk1002s.stream().filter(t -> "0001400".equals(t.getSeccode())).map(Xnhk1002::getF001v).collect(Collectors.toList());
            List<String> hcList = xnhk1002s.stream().filter(t -> "0001500".equals(t.getSeccode())).map(Xnhk1002::getF001v).collect(Collectors.toList());
            hmget.put(IndexConstants.HS, hsList);
            hmget.put(IndexConstants.GQ, gqList);
            hmget.put(IndexConstants.HC, hcList);
            // 有效期一天
            redisClient.hmset(RedisKeyConstants.BG_MAP_INDEX_STOCK_MAP, hmget, 86400);
        }
        return ResultT.success(hmget);
    }

    @Override
    public ResultT<Map<String, List<ProfitSelectDto>>> getFinProfitDate() {
        Map<String, Object> profitSelectMap = redisClient.hmget(RedisKeyConstants.BG_MAP_SELECT_PROFIT);
        // 判断是否更新数据 异步更新缓存
        Long createTime = (Long) profitSelectMap.get(CREATE_TIME);
        if (createTime == null || createTime + EXPIRE_TIME < System.currentTimeMillis()) {
//            asyncServiceExecutor.execute(this::rebuildProfitCache);
            rebuildProfitCache();
        }
        Map<String, List<ProfitSelectDto>> resultMap = new HashMap<>(FINANCE_TYPES.length);
        for (String financeType : FINANCE_TYPES) {
            resultMap.put(financeType, (List<ProfitSelectDto>) profitSelectMap.getOrDefault(financeType, Collections.emptyList()));
        }
        return ResultT.success(resultMap);
    }

    /**
     * 利润缓存过期重新构建缓存
     */
    @Async
    @Override
    public void rebuildProfitCache() {
        if (!redisClient.setNxExpire(RedisKeyConstants.BG_LOCK_SELECT_CASH_FLOW, RedisKeyConstants.BG_LOCK_SELECT_CASH_FLOW, 60)) {
            return;
        }
        Set<String> allStock = Optional.ofNullable(quotationService.selectStockNameList()).orElseGet(HashSet::new);
        Map<String, String> stockCodeMap = allStock.stream()
                .collect(Collectors.toMap(item -> item.split(",")[0], item -> item.split(",")[1]));
        Set<String> codeSet = stockCodeMap.keySet();
        try {
            // 查询源数据
            List<F10FinProfitEntity> f10FinProfitEntities = f10SourceHandler.listFinanceEachCodeAndType(F10FinProfitEntity.class, COLLECTION_FIN_PROFIT, FINANCE_TYPES);
            List<F10NoFinProfitEntity> f10NoFinProfitEntities = f10SourceHandler.listFinanceEachCodeAndType(F10NoFinProfitEntity.class, COLLECTION_NO_FIN_PROFIT, FINANCE_TYPES);
            List<F10InsureProfitEntity> f10InsureProfitEntities = f10SourceHandler.listFinanceEachCodeAndType(F10InsureProfitEntity.class, COLLECTION_INSURE_PROFIT, FINANCE_TYPES);
            // 源数据转换
            Map<String, List<ProfitSelectDto>> finProfitMap = f10FinProfitEntities.parallelStream().map(entity -> buildProfitSelectDto(entity)).collect(Collectors.groupingBy(ProfitSelectDto::getReportType));
            Map<String, List<ProfitSelectDto>> noFinProfitMap = f10NoFinProfitEntities.parallelStream().map(entity -> buildProfitSelectDto(entity)).collect(Collectors.groupingBy(ProfitSelectDto::getReportType));
            Map<String, List<ProfitSelectDto>> insureProfitMap = f10InsureProfitEntities.parallelStream().map(entity -> buildProfitSelectDto(entity)).collect(Collectors.groupingBy(ProfitSelectDto::getReportType));

            // 更新redis
            Map<String, Object> redisMap = new HashMap<>(FINANCE_TYPES.length + 1);
            for (String financeType : FINANCE_TYPES) {
                List<ProfitSelectDto> list = new ArrayList<>();
                list.addAll(finProfitMap.getOrDefault(financeType, Collections.emptyList()));
                list.addAll(noFinProfitMap.getOrDefault(financeType, Collections.emptyList()));
                list.addAll(insureProfitMap.getOrDefault(financeType, Collections.emptyList()));
                list = list.stream().filter(v -> codeSet.contains(v.getStockCode()))
                        .sorted(Comparator.comparing(ProfitSelectDto::getEndTime).reversed()).distinct().collect(Collectors.toList());
                redisMap.put(financeType, list);
            }
            redisMap.put(CREATE_TIME, System.currentTimeMillis());
            redisClient.hmset(RedisKeyConstants.BG_MAP_SELECT_PROFIT, redisMap);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public ResultT<List<ProfitSelectDto>> getAllFinProfitData(Collection<String> codeSet, List<String> reportTypeList) {
        log.info("getAllFinProfitData start");
        List<F10FinProfitEntity> f10FinProfitEntities = f10SourceHandler.allFinanceEachCodeAndType(codeSet, F10FinProfitEntity.class, COLLECTION_FIN_PROFIT, reportTypeList);
        List<F10NoFinProfitEntity> f10NoFinProfitEntities = f10SourceHandler.allFinanceEachCodeAndType(codeSet,F10NoFinProfitEntity.class, COLLECTION_NO_FIN_PROFIT, reportTypeList);
        List<F10InsureProfitEntity> f10InsureProfitEntities = f10SourceHandler.allFinanceEachCodeAndType(codeSet,F10InsureProfitEntity.class, COLLECTION_INSURE_PROFIT, reportTypeList);
        // 源数据转换
        List<ProfitSelectDto> finProfitList = f10FinProfitEntities.parallelStream().map(this::buildProfitSelectDto).collect(Collectors.toList());
        List<ProfitSelectDto> noFinProfitList = f10NoFinProfitEntities.parallelStream().map(this::buildProfitSelectDto).collect(Collectors.toList());
        List<ProfitSelectDto> insureProfitList = f10InsureProfitEntities.parallelStream().map(this::buildProfitSelectDto).collect(Collectors.toList());
        List<ProfitSelectDto> list = new ArrayList<>();
        list.addAll(finProfitList);
        list.addAll(noFinProfitList);
        list.addAll(insureProfitList);
        log.info("getAllFinProfitData end");
        return ResultT.success(list);
    }

    @Override
    public ResultT<List<CashFlowSelectDto>> getAllCashFlowData(Collection<String> codeSet, List<String> reportTypeList) {
        log.info("getAllCashFlowData start");
        List<F10CashFlowEntity> f10CashFlowEntities = f10SourceHandler.allFinanceEachCodeAndType(codeSet, F10CashFlowEntity.class, COLLECTION_CASH_FLOW, reportTypeList);
        // 源数据转换
        List<CashFlowSelectDto> cashFlowSelectDtos = f10CashFlowEntities.parallelStream().map(this::buildCashFlowSelectDto).collect(Collectors.toList());
        log.info("getAllCashFlowData end");
        return ResultT.success(cashFlowSelectDtos);
    }

    @Override
    public ResultT<Map<String, List<String>>> getIndustryCodeMap() {
        List<StockDefine> list = stockDefineService.listStockColumns(ListUtil.of("code", "industry_code"));
        // List<StockDefine> list = stockDefineService.list(new QueryWrapper<StockDefine>().select("code", "industry_code").eq("stock_type", StockTypeEnum.STOCK.getCode()));
        Map<String, List<String>> industryCodeMap = list.stream().filter(stockDefine ->
                        StringUtils.isNotEmpty(stockDefine.getIndustryCode()))
                .collect(Collectors.groupingBy(StockDefine::getIndustryCode, Collectors.mapping(StockDefine::getCode, Collectors.toList())));
        return ResultT.success(industryCodeMap);
    }

    @Override
    public ResultT<Map<String, List<String>>> getConceptCodeMap() {
        Map<String, List<String>> conceptCodeMap = xnhk1301Mapper.selectList(new QueryWrapper<Xnhk1301>().select("F001V", "F003V"))
                .stream().filter(Xnhk1301 -> StringUtils.isNotEmpty(Xnhk1301.getF001v()))
                .collect(Collectors.groupingBy(Xnhk1301::getF001v, Collectors.mapping(Xnhk1301::getF003v, Collectors.toList())));
        return ResultT.success(conceptCodeMap);
    }

    private <T> ProfitSelectDto buildProfitSelectDto(T obj) {
        ProfitSelectDto profitSelectDto = new ProfitSelectDto();
        if (obj instanceof F10NoFinProfitEntity){
            F10NoFinProfitEntity entity = (F10NoFinProfitEntity) obj;
            profitSelectDto.setStockCode(entity.getStockCode());
            profitSelectDto.setReportType(entity.getReportType());
            profitSelectDto.setEndTime(entity.getEndTimestamp());
            BigDecimal exchangeRate = entity.getExchangeRate() == null ? BigDecimal.ONE : entity.getExchangeRate();
            profitSelectDto.setNetProfit(entity.getHoldersOfShareCapitalOfTheCompany().getVal() == null ? null : entity.getHoldersOfShareCapitalOfTheCompany().getVal().multiply(exchangeRate));
            profitSelectDto.setTax(entity.getTax().getVal() == null ? null : entity.getTax().getVal().multiply(exchangeRate));
            profitSelectDto.setOperatingProfit(entity.getOperatingProfit().getVal() == null ? null : entity.getOperatingProfit().getVal().multiply(exchangeRate));
            profitSelectDto.setFinancingCost(entity.getOperatingProfit().getFinancingCost().getVal() == null ? null : entity.getOperatingProfit().getFinancingCost().getVal().multiply(exchangeRate));
            profitSelectDto.setOperatingCost(entity.getOperatingCostsAndExpenses().getVal() == null ? null : entity.getOperatingCostsAndExpenses().getVal().multiply(exchangeRate));
            profitSelectDto.setOperatingRevenue(entity.getOperatingAndOtherRevenue().getVal() == null ? null : entity.getOperatingAndOtherRevenue().getVal().multiply(exchangeRate));
            profitSelectDto.setProfitLossDuringPeriod(entity.getProfitAndLossDuringThePeriod().getVal() == null ? null : entity.getProfitAndLossDuringThePeriod().getVal().multiply(exchangeRate));
        } else if (obj instanceof F10FinProfitEntity){
            F10FinProfitEntity entity = (F10FinProfitEntity) obj;
            profitSelectDto.setStockCode(entity.getStockCode());
            profitSelectDto.setEndTime(entity.getEndTimestamp());
            profitSelectDto.setReportType(entity.getReportType());
            BigDecimal exchangeRate = entity.getExchangeRate() == null ? BigDecimal.ONE : entity.getExchangeRate();
            profitSelectDto.setNetProfit(entity.getHoldersShareCapitalCompany().getVal() == null ? null : entity.getHoldersShareCapitalCompany().getVal().multiply(exchangeRate));
            profitSelectDto.setTax(entity.getTax().getVal() == null ? null : entity.getTax().getVal().multiply(exchangeRate));
            profitSelectDto.setOperatingProfit(entity.getOperatingProfit().getVal() == null ? null : entity.getOperatingProfit().getVal().multiply(exchangeRate));
            profitSelectDto.setOperatingCost(entity.getTotalOperatingExpenses().getVal() == null ? null : entity.getTotalOperatingExpenses().getVal().multiply(exchangeRate));
            profitSelectDto.setOperatingRevenue(entity.getGrossRevenue().getVal() == null ? null : entity.getGrossRevenue().getVal().multiply(exchangeRate));
            profitSelectDto.setProfitLossDuringPeriod(entity.getProfitLossDuringPeriod().getVal() == null ? null : entity.getProfitLossDuringPeriod().getVal().multiply(exchangeRate));
        } else if (obj instanceof F10InsureProfitEntity){
            F10InsureProfitEntity entity = (F10InsureProfitEntity) obj;
            profitSelectDto.setStockCode(entity.getStockCode());
            profitSelectDto.setReportType(entity.getReportType());
            profitSelectDto.setEndTime(entity.getEndTimestamp());
            BigDecimal exchangeRate = entity.getExchangeRate() == null ? BigDecimal.ONE : entity.getExchangeRate();
            profitSelectDto.setNetProfit(entity.getHoldersShareCapitalCompany().getVal() == null ? null : entity.getHoldersShareCapitalCompany().getVal().multiply(exchangeRate));
            profitSelectDto.setTax(entity.getTax().getVal() == null ? null : entity.getTax().getVal().multiply(exchangeRate));
            profitSelectDto.setOperatingProfit(entity.getOperatingProfit().getVal() == null ? null : entity.getOperatingProfit().getVal().multiply(exchangeRate));
            profitSelectDto.setOperatingRevenue(entity.getGrossRevenue().getVal() == null ? null : entity.getGrossRevenue().getVal().multiply(exchangeRate));
            profitSelectDto.setProfitLossDuringPeriod(entity.getProfitLossDuringPeriod().getVal() == null ? null : entity.getProfitLossDuringPeriod().getVal().multiply(exchangeRate));
        }
        return profitSelectDto;
    }

    @Override
    public ResultT<List<ProfitSelectDto>> getFinProfitDateByType(String reportType) {
        ResultT<Map<String, List<ProfitSelectDto>>> finProfitDate = getFinProfitDate();

        return ResultT.success(finProfitDate.getData().getOrDefault(reportType, Collections.emptyList()));
    }

    /**
     * 现金流量指标缓存
     * @return
     */
    @Override
    public ResultT<Map<String, List<CashFlowSelectDto>>> getCashFlowDate() {
        Map<String, Object> cashFlowSelectMap = redisClient.hmget(RedisKeyConstants.BG_MAP_SELECT_CASH_FLOW);
        // 判断是否更新数据 异步更新缓存
        Long createTime = (Long) cashFlowSelectMap.get(CREATE_TIME);
        if (createTime == null || createTime + EXPIRE_TIME < System.currentTimeMillis()) {
            rebuildCashFlowCache();
        }
        Map<String, List<CashFlowSelectDto>> resultMap = new HashMap<>(FINANCE_TYPES.length);
        for (String financeType : FINANCE_TYPES) {
            resultMap.put(financeType, (List<CashFlowSelectDto>) cashFlowSelectMap.getOrDefault(financeType, Collections.emptyList()));
        }
        return ResultT.success(resultMap);
    }

    @Override
    public ResultT<List<CashFlowSelectDto>> getCashFlowDateByType(String reportType) {
        ResultT<Map<String, List<CashFlowSelectDto>>> cashFlowDate = getCashFlowDate();

        return ResultT.success(cashFlowDate.getData().getOrDefault(reportType, Collections.emptyList()));
    }
    /**
     * 现金流缓存过期重新构建缓存
     */
    @Async
    @Override
    public void rebuildCashFlowCache() {
        if (!redisClient.setNxExpire(RedisKeyConstants.BG_LOCK_SELECT_CASH_FLOW, RedisKeyConstants.BG_LOCK_SELECT_CASH_FLOW, 60)) {
            return;
        }
        Set<String> allStock = Optional.ofNullable(quotationService.selectStockNameList()).orElseGet(HashSet::new);
        Map<String, String> stockCodeMap = allStock.stream()
                .collect(Collectors.toMap(item -> item.split(",")[0], item -> item.split(",")[1]));
        Set<String> codeSet = stockCodeMap.keySet();
        try {
            // 查询源数据
            List<F10CashFlowEntity> f10CashFlowEntities = f10SourceHandler.listFinanceEachCodeAndType(F10CashFlowEntity.class, COLLECTION_CASH_FLOW, FINANCE_TYPES);
            // 源数据转换
            Map<String, List<CashFlowSelectDto>> finProfitMap = f10CashFlowEntities.parallelStream().map(entity -> buildCashFlowSelectDto(entity)).collect(Collectors.groupingBy(CashFlowSelectDto::getReportType));

            // 更新redis
            Map<String, Object> redisMap = new HashMap<>(FINANCE_TYPES.length + 1);
            for (String financeType : FINANCE_TYPES) {
                List<CashFlowSelectDto> list = new ArrayList<>();
                list.addAll(finProfitMap.getOrDefault(financeType, Collections.emptyList()));
                list = list.stream().filter(v -> codeSet.contains(v.getStockCode()))
                        .sorted(Comparator.comparing(CashFlowSelectDto::getEndTime).reversed()).distinct().collect(Collectors.toList());
                redisMap.put(financeType, list);
            }
            redisMap.put(CREATE_TIME, System.currentTimeMillis());
            redisClient.hmset(RedisKeyConstants.BG_MAP_SELECT_CASH_FLOW, redisMap);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private CashFlowSelectDto buildCashFlowSelectDto(F10CashFlowEntity entity) {
        CashFlowSelectDto profitSelectDto = new CashFlowSelectDto();
        profitSelectDto.setStockCode(entity.getStockCode());
        profitSelectDto.setEndTime(entity.getEndTimestamp());
        profitSelectDto.setReportType(entity.getReportType());
        BigDecimal exchangeRate = entity.getExchangeRate() == null ? BigDecimal.ONE : entity.getExchangeRate();
        profitSelectDto.setCashFlowFromeOperations(entity.getCashFlowFromeOperations().getVal() == null ? null : entity.getCashFlowFromeOperations().getVal().multiply(exchangeRate));
        profitSelectDto.setCashFlowFromInvestmentActivities(entity.getCashFlowFromInvestmentActivities().getVal() == null ? null : entity.getCashFlowFromInvestmentActivities().getVal().multiply(exchangeRate));
        profitSelectDto.setCashFlowFromFinancingActivites(entity.getCashFlowFromFinancingActivites().getVal() == null ? null : entity.getCashFlowFromFinancingActivites().getVal().multiply(exchangeRate));
        return profitSelectDto;
    }

}
