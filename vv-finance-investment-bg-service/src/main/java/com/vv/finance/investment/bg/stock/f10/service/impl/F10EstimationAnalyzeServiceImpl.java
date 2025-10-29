package com.vv.finance.investment.bg.stock.f10.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.vv.finance.base.dto.ResultCode;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.constants.omdc.Adjhkt;
import com.vv.finance.common.constants.omdc.OmdcCommonConstant;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.utils.ZipUtil;
import com.vv.finance.investment.bg.api.HkTradingCalendarApi;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.entity.BgTradingCalendar;
import com.vv.finance.investment.bg.entity.f10.enums.EstimationDimensionEnum;
import com.vv.finance.investment.bg.entity.f10.enums.F10MarketTypeEnum;
import com.vv.finance.investment.bg.entity.f10.estimation.*;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresInsuranceEntity;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresNonFinancialEntity;
import com.vv.finance.investment.bg.stock.f10.handler.F10SourceHandlerV2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName F10EstimationAnalyzeServiceImpl
 * @Deacription F10估值分析
 * @Author lh.sz
 * @Date 2021年09月04日 11:21
 **/
@Component
@Slf4j
public class F10EstimationAnalyzeServiceImpl extends AbstractBaseServiceImpl {
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    RedisClient redisClient;

    @Resource
    private HkTradingCalendarApi hkTradingCalendarApi;

    @Resource
    private F10SourceHandlerV2 f10SourceHandlerV2;

    @Resource
    private StockCache stockCache;

    /**
     * 获取估值分析雷达图
     *
     * @param code
     * @return
     */
    public List<EstimationRadar> getEstimationRadarChar(String code) {
        // List<EstimationRadar> hget = redisClient.hget(RedisKeyConstants.F10_ESTIMATION_RADAR_CHAR_MAP, code);
        // if (CollectionUtils.isEmpty(hget) || StringUtils.isBlank(hget.get(0).getStockName())) {
            List<String> codes = getIndustryCodes(code);
            Map<String, String> codeAndNameMap =new HashMap<>();
            Map<String, List<EstimationChar>> map =new HashMap<>();
            if (CollectionUtils.isEmpty(codes)) {
                // Object data = redisClient.hget(RedisKeyConstants.COMPRESS_STOCK_MAP, code);
                // if (null!=data) {
                //     StockSnapshot stockSnapshot = JSON.parseObject(ZipUtil.gunzip((String) data), StockSnapshot.class);
                //     codeAndNameMap.put(code,stockSnapshot.getName());
                // }
            }else {
                // codeAndNameMap = getCodeAndName(codes);
                List<EstimationChar> estimationChars = getEstimationAnalyzeCharByCodes(codes);
                 map = estimationChars.stream().collect(Collectors.groupingBy(EstimationChar::getStockCode));

            }

            List<EstimationRadar> estimationRadars = buildEstimationRadarChar(map, code, stockCache.queryStockNameMap(null));
            // redisClient.hset(RedisKeyConstants.F10_ESTIMATION_RADAR_CHAR_MAP, code, estimationRadars, 1800L);
            return estimationRadars;
        }
        // return hget;
    // }

    /**
     * 构建估值分析雷达图
     *
     * @param map  估值数据
     * @param code 股票代码
     * @return
     */
    private List<EstimationRadar> buildEstimationRadarChar(Map<String, List<EstimationChar>> map,
                                                           String code,
                                                           Map<String, String> codeAndNameMap) {
        List<EstimationRadar> list = new LinkedList<>();
        Map<String, RadarVal> allSourceMap = new HashMap<>();
        Map<String, RadarVal> valMap = new HashMap<>();
        Map<String, Long> stockCodeIdMap = stockCache.queryStockIdMap(null);
        map.forEach((s, estimationChars) -> {
            RadarVal radarVal = RadarVal.builder().build();
//            estimationChars.forEach(e -> {
//                radarVal.setProfit(calcCapacityVal(e.getProfit()));
//                radarVal.setGrowth(calcCapacityVal(e.getGrowth()));
//                radarVal.setOperating(calcCapacityVal(e.getOperating()));
//                radarVal.setDebt(calcCapacityVal(e.getDebt()));
//                radarVal.setCash(calcCapacityVal(e.getCash()));
//            });
            if (CollectionUtils.isNotEmpty(estimationChars)) {
                EstimationChar estimationChar = estimationChars.get(estimationChars.size() - 1);
                radarVal.setProfit(calcCapacityVal(estimationChar.getProfit()));
                radarVal.setGrowth(calcCapacityVal(estimationChar.getGrowth()));
                radarVal.setOperating(calcCapacityVal(estimationChar.getOperating()));
                radarVal.setDebt(calcCapacityVal(estimationChar.getDebt()));
                radarVal.setCash(calcCapacityVal(estimationChar.getCash()));
            }
            allSourceMap.put(s, radarVal);
        });
        double orElse = 0.0;
        BigDecimal profitMax = BigDecimal.valueOf(allSourceMap.values().stream().filter(s -> s.getProfit() != null).mapToDouble(s -> s.getProfit().doubleValue()).max().orElse(orElse));
        BigDecimal growthMax = BigDecimal.valueOf(allSourceMap.values().stream().filter(s -> s.getGrowth() != null).mapToDouble(s -> s.getGrowth().doubleValue()).max().orElse(orElse));
        BigDecimal operatingMax = BigDecimal.valueOf(allSourceMap.values().stream().filter(s -> s.getOperating() != null).mapToDouble(s -> s.getOperating().doubleValue()).max().orElse(orElse));
        BigDecimal debtMax = BigDecimal.valueOf(allSourceMap.values().stream().filter(s -> s.getDebt() != null).mapToDouble(s -> s.getDebt().doubleValue()).max().orElse(orElse));
        BigDecimal cashMax = BigDecimal.valueOf(allSourceMap.values().stream().filter(s -> s.getCash() != null).mapToDouble(s -> s.getCash().doubleValue()).max().orElse(orElse));

        BigDecimal profitMin = BigDecimal.valueOf(allSourceMap.values().stream().filter(s -> s.getProfit() != null).mapToDouble(s -> s.getProfit().doubleValue()).min().orElse(orElse));
        BigDecimal growthMin = BigDecimal.valueOf(allSourceMap.values().stream().filter(s -> s.getGrowth() != null).mapToDouble(s -> s.getGrowth().doubleValue()).min().orElse(orElse));
        BigDecimal operatingMin = BigDecimal.valueOf(allSourceMap.values().stream().filter(s -> s.getOperating() != null).mapToDouble(s -> s.getOperating().doubleValue()).min().orElse(orElse));
        BigDecimal debtMin = BigDecimal.valueOf(allSourceMap.values().stream().filter(s -> s.getDebt() != null).mapToDouble(s -> s.getDebt().doubleValue()).min().orElse(orElse));
        BigDecimal cashMin = BigDecimal.valueOf(allSourceMap.values().stream().filter(s -> s.getCash() != null).mapToDouble(s -> s.getCash().doubleValue()).min().orElse(orElse));

        allSourceMap.forEach((s, a) -> {
            BigDecimal profitVal = a.getProfit() == null ? BigDecimal.ZERO
                    : profitMin.compareTo(BigDecimal.ZERO) < 0 ? calcRadarVal(a.getProfit().add(profitMin.abs()), profitMax.add(profitMin.abs()))
                    : calcRadarVal(a.getProfit(), profitMax);
            BigDecimal growthVal = a.getGrowth() == null ? BigDecimal.ZERO
                    : growthMin.compareTo(BigDecimal.ZERO) < 0 ? calcRadarVal(a.getGrowth().add(growthMin.abs()), growthMax.add(growthMin.abs()))
                    : calcRadarVal(a.getGrowth(), growthMax);
            BigDecimal operatingVal = a.getOperating() == null ? BigDecimal.ZERO
                    : operatingMin.compareTo(BigDecimal.ZERO) < 0 ? calcRadarVal(a.getOperating().add(operatingMin.abs()), operatingMax.add(operatingMin.abs()))
                    : calcRadarVal(a.getOperating(), operatingMax);
            BigDecimal debtVal = a.getDebt() == null ? BigDecimal.ZERO
                    : debtMin.compareTo(BigDecimal.ZERO) < 0 ? calcRadarVal(a.getDebt().add(debtMin.abs()), debtMax.add(debtMin.abs()))
                    : calcRadarVal(a.getDebt(), debtMax);
            BigDecimal cashVal = a.getCash() == null ? BigDecimal.ZERO
                    : cashMin.compareTo(BigDecimal.ZERO) < 0 ? calcRadarVal(a.getCash().add(cashMin.abs()), cashMax.add(cashMin.abs()))
                    : calcRadarVal(a.getCash(), cashMax);
            RadarVal radarVal = RadarVal.builder().build();
            radarVal.setProfit(profitVal);
            radarVal.setGrowth(growthVal);
            radarVal.setOperating(operatingVal);
            radarVal.setDebt(debtVal);
            radarVal.setCash(cashVal);
            valMap.put(s, radarVal);
        });
        RadarVal val = valMap.getOrDefault(code,RadarVal.builder().build());
        Field[] fields = ReflectUtil.getFields(val.getClass());
        for (Field f : fields) {
            EstimationRadar estimationRadar = new EstimationRadar();
            if (!"serialVersionUID".equals(f.getName())) {
                BigDecimal sum = BigDecimal.valueOf(valMap.values().stream().mapToDouble
                        (s -> {
                            BigDecimal value = (BigDecimal) ReflectUtil.getFieldValue(s, f);
                            return value.doubleValue();
                        }).sum());
                BigDecimal count = BigDecimal.valueOf(valMap.values().stream().map
                        (s -> (BigDecimal) ReflectUtil.getFieldValue(s, f.getName())).count());
                // 股票对应值
                BigDecimal stockVal=(BigDecimal) ReflectUtil.getFieldValue(val, f);
                estimationRadar.setStockVal(stockVal==null?BigDecimal.valueOf(0):stockVal);
                estimationRadar.setStockName(codeAndNameMap.get(code));
                estimationRadar.setDimension(EstimationDimensionEnum.getValue(f.getName()));
                estimationRadar.setStockCode(code);
                estimationRadar.setStockId(stockCodeIdMap.get(code));
                // 行业平均值
                estimationRadar.setAvgVal(calcAvg(sum, count));
                list.add(estimationRadar);
            }
        }
        return list;
    }

    /**
     * 计算平均值
     *
     * @param sum   总数
     * @param count 条数
     * @return
     */
    private BigDecimal calcAvg(BigDecimal sum,
                               BigDecimal count) {
        if (count.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }
        return sum.divide(count, 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算能力值
     *
     * @param valList
     * @return
     */
    private BigDecimal calcCapacityVal(List<BigDecimal> valList) {
        return BigDecimal.valueOf(valList.stream().filter(Objects::nonNull).mapToDouble
                        (v -> v.multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP).doubleValue()).sum())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 分位点
     *
     * @param val  当前值
     * @param val1 最大值
     * @return (当前值 / 最大值) * 100
     */
    private BigDecimal calcRadarVal(BigDecimal val, BigDecimal val1) {
        if (val == null || ObjectUtils.isEmpty(val1) || val1.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return val.divide(val1, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取估值分析
     *
     * @param code 股票代码
     * @return
     */
    public List<EstimationChar> getEstimationAnalyzeChar(String code) {
        List<EstimationChar> f10EstimationAnalyzeChar = new ArrayList<>();
        Query query = Query.query(Criteria.where("reportType").is("F").and("stockCode").is(code)).with(Sort.by(Sort.Direction.ASC, "endTimestamp"));
        F10MarketTypeEnum f10MarketTypeEnum = F10MarketTypeEnum.getByCode(getMarketType(code));
        if (null != f10MarketTypeEnum) {
            switch (f10MarketTypeEnum) {
                case NO_FINANCIAL:
                    return getEstimationAnalyzeChaByNoFin(query);
                case FINANCIAL:
                    return getEstimationAnalyzeChaByFin(query);
                case INSURANCE:
                    return getEstimationAnalyzeChaByIn(query);
                default:
                    break;
            }
        }
        return f10EstimationAnalyzeChar;
    }

    /**
     * 获取估值分析
     *
     * @param codes 股票代码
     * @return
     */
    public List<EstimationChar> getEstimationAnalyzeCharByCodes(List<String> codes) {
        List<EstimationChar> f10EstimationAnalyzeChar = new ArrayList<>();
        Query query = Query.query(Criteria.where("reportType").is("F").and("stockCode").in(codes)).with(Sort.by(Sort.Direction.ASC, "endTimestamp"));
        f10EstimationAnalyzeChar.addAll(getEstimationAnalyzeChaByNoFin(query));
        f10EstimationAnalyzeChar.addAll(getEstimationAnalyzeChaByFin(query));
        f10EstimationAnalyzeChar.addAll(getEstimationAnalyzeChaByIn(query));
        return f10EstimationAnalyzeChar;
    }

    /**
     * 获取估值分析非金融
     *
     * @param query
     * @return
     */
    private List<EstimationChar> getEstimationAnalyzeChaByNoFin(Query query) {
        List<EstimationChar> estimationChars = new ArrayList<>();
        List<F10KeyFiguresNonFinancialEntity> entityList = mongoTemplate.find(query, F10KeyFiguresNonFinancialEntity.class);
        if (CollectionUtils.isNotEmpty(entityList)) {
            entityList.forEach(f -> {
                EstimationChar aChar = new EstimationChar();
                buildNoFin(aChar, f);
                estimationChars.add(aChar);
            });
        }
        return estimationChars;
    }

    /**
     * 获取估值分析金融
     *
     * @param query
     * @return
     */
    private List<EstimationChar> getEstimationAnalyzeChaByFin(Query query) {
        List<EstimationChar> estimationChars = new ArrayList<>();
        List<F10KeyFiguresFinancialEntity> entityList = mongoTemplate.find(query, F10KeyFiguresFinancialEntity.class);
        if (CollectionUtils.isNotEmpty(entityList)) {
            entityList.forEach(f -> {
                EstimationChar aChar = new EstimationChar();
                buildFin(aChar, f);
                estimationChars.add(aChar);
            });
        }
        return estimationChars;
    }

    /**
     * 获取估值分析保险
     *
     * @param query
     * @return
     */
    private List<EstimationChar> getEstimationAnalyzeChaByIn(Query query) {
        List<EstimationChar> estimationChars = new ArrayList<>();
        List<F10KeyFiguresInsuranceEntity> entityList = mongoTemplate.find(query, F10KeyFiguresInsuranceEntity.class);
        if (CollectionUtils.isNotEmpty(entityList)) {
            entityList.forEach(f -> {
                EstimationChar aChar = new EstimationChar();
                buildIn(aChar, f);
                estimationChars.add(aChar);
            });
        }
        return estimationChars;
    }

    static void buildIn(EstimationChar aChar, F10KeyFiguresInsuranceEntity f) {
        List<BigDecimal> profit = Arrays.asList(
                f.getProfitability() == null || f.getProfitability().getRoe() == null ? null : f.getProfitability().getRoe().getVal(),
                f.getProfitability() == null || f.getProfitability().getRoa() == null ? null : f.getProfitability().getRoa().getVal(),
                f.getProfitability() == null || f.getProfitability().getNetProfitRatio() == null ? null : f.getProfitability().getNetProfitRatio().getVal()
        );
        List<BigDecimal> growth = Arrays.asList(
                f.getGrowthAbility() == null || f.getGrowthAbility().getOperatingRevenueGrowth() == null ? null : f.getGrowthAbility().getOperatingRevenueGrowth().getVal(),
                f.getGrowthAbility() == null || f.getGrowthAbility().getNetProfitGrowth() == null ? null : f.getGrowthAbility().getNetProfitGrowth().getVal(),
                f.getGrowthAbility() == null || f.getGrowthAbility().getTotalAssetsGrowth() == null ? null : f.getGrowthAbility().getTotalAssetsGrowth().getVal()
        );
        List<BigDecimal> operating = Arrays.asList(
                f.getOperatingCapacity() == null || f.getOperatingCapacity().getNetInvestmentIncomeGrowth() == null ? null : f.getOperatingCapacity().getNetInvestmentIncomeGrowth().getVal(),
                f.getOperatingCapacity() == null || f.getOperatingCapacity().getInsuranceReserveGrowth() == null ? null : f.getOperatingCapacity().getInsuranceReserveGrowth().getVal(),
                f.getOperatingCapacity() == null || f.getOperatingCapacity().getEarnedPremiumsPolicyFeeGrowth() == null ? null : f.getOperatingCapacity().getEarnedPremiumsPolicyFeeGrowth().getVal()
        );
        List<BigDecimal> debt = Arrays.asList(
                f.getSolvency() == null || f.getSolvency().getTotalInvestmentAssets() == null ? null : f.getSolvency().getTotalInvestmentAssets().getVal(),
                f.getSolvency() == null || f.getSolvency().getTotalEquityTotalAssets() == null ? null : f.getSolvency().getTotalEquityTotalAssets().getVal(),
                f.getSolvency() == null || f.getSolvency().getStockholderEquityTotalAssets() == null ? null : f.getSolvency().getStockholderEquityTotalAssets().getVal()
        );
        List<BigDecimal> cash = Arrays.asList(
                f.getCashability() == null || f.getCashability().getCurrentRatio() == null ? null : f.getCashability().getCurrentRatio().getVal(),
                f.getCashability() == null || f.getCashability().getQuickRatio() == null ? null : f.getCashability().getQuickRatio().getVal(),
                f.getCashability() == null || f.getCashability().getCashRatio() == null ? null : f.getCashability().getCashRatio().getVal()
        );
        aChar.setStockCode(f.getStockCode());
        aChar.setProfit(profit);
        aChar.setGrowth(growth);
        aChar.setOperating(operating);
        aChar.setDebt(debt);
        aChar.setCash(cash);
        aChar.setTime(f.getEndTimestamp());
    }


    static void buildFin(EstimationChar aChar, F10KeyFiguresFinancialEntity f) {
        List<BigDecimal> profit = Arrays.asList(
                f.getProfitability() == null || f.getProfitability().getRoe() == null ? null : f.getProfitability().getRoe().getVal(),
                f.getProfitability() == null || f.getProfitability().getRoa() == null ? null : f.getProfitability().getRoa().getVal(),
                f.getProfitability() == null || f.getProfitability().getLoanReturn() == null ? null : f.getProfitability().getLoanReturn().getVal()
        );
        List<BigDecimal> growth = Arrays.asList(
                f.getGrowthAbility() == null || f.getGrowthAbility().getOperatingRevenueGrowth() == null ? null : f.getGrowthAbility().getOperatingRevenueGrowth().getVal(),
                f.getGrowthAbility() == null || f.getGrowthAbility().getNetProfitGrowth() == null ? null : f.getGrowthAbility().getNetProfitGrowth().getVal(),
                f.getGrowthAbility() == null || f.getGrowthAbility().getTotalAssetsGrowth() == null ? null : f.getGrowthAbility().getTotalAssetsGrowth().getVal()
        );
        List<BigDecimal> operating = Arrays.asList(
                f.getOperatingCapacity() == null ? null : f.getOperatingCapacity().getAverageLiquidityRatio().getVal(),
                f.getOperatingCapacity() == null ? null : f.getOperatingCapacity().getRecapitalizationRatio().getVal(),
                f.getOperatingCapacity() == null ? null : f.getOperatingCapacity().getRestructuredLoanRatio().getVal()
        );
        List<BigDecimal> debt = Arrays.asList(
                f.getSolvency() == null || f.getSolvency().getLoansDeposits() == null ? null : f.getSolvency().getLoansDeposits().getVal(),
                f.getSolvency() == null || f.getSolvency().getDepositsTotalAssets() == null ? null : f.getSolvency().getDepositsTotalAssets().getVal(),
                f.getSolvency() == null || f.getSolvency().getStockholderEquityTotalAssets() == null ? null : f.getSolvency().getStockholderEquityTotalAssets().getVal()
        );

        List<BigDecimal> cash = Arrays.asList(
                f.getCashability() == null || f.getCashability().getCurrentRatio() == null ? null : f.getCashability().getCurrentRatio().getVal(),
                f.getCashability() == null || f.getCashability().getQuickRatio() == null ? null : f.getCashability().getQuickRatio().getVal(),
                f.getCashability() == null || f.getCashability().getCashRatio() == null ? null : f.getCashability().getCashRatio().getVal()
        );
        aChar.setStockCode(f.getStockCode());
        aChar.setProfit(profit);
        aChar.setGrowth(growth);
        aChar.setOperating(operating);
        aChar.setDebt(debt);
        aChar.setCash(cash);
        aChar.setTime(f.getEndTimestamp());
    }


    /**
     * 非金融
     *
     * @param aChar
     * @param f
     */
    static void buildNoFin(EstimationChar aChar, F10KeyFiguresNonFinancialEntity f) {
        List<BigDecimal> profit = Arrays.asList(
                f.getProfitability() == null || f.getProfitability().getRoa() == null ? null : f.getProfitability().getRoa().getVal(),
                f.getProfitability() == null || f.getProfitability().getRoe() == null ? null : f.getProfitability().getRoe().getVal(),
                f.getProfitability() == null || f.getProfitability().getRoce() == null ? null : f.getProfitability().getRoce().getVal());
        List<BigDecimal> growth = Arrays.asList(
                f.getGrowthAbility() == null || f.getGrowthAbility().getOperatingRevenueGrowth() == null ? null : f.getGrowthAbility().getOperatingRevenueGrowth().getVal(),
                f.getGrowthAbility() == null || f.getGrowthAbility().getNetProfitGrowth() == null ? null : f.getGrowthAbility().getNetProfitGrowth().getVal(),
                f.getGrowthAbility() == null || f.getGrowthAbility().getTotalAssetsGrowth() == null ? null : f.getGrowthAbility().getTotalAssetsGrowth().getVal());
        List<BigDecimal> operating = Arrays.asList(
                f.getOperatingCapacity() == null || f.getOperatingCapacity().getTotalAssetsTurnover() == null ? null : f.getOperatingCapacity().getTotalAssetsTurnover().getVal(),
                f.getOperatingCapacity() == null || f.getOperatingCapacity().getInventoryTurnover() == null ? null : f.getOperatingCapacity().getInventoryTurnover().getVal(),
                f.getOperatingCapacity() == null || f.getOperatingCapacity().getAccountsPayableTurnover() == null ? null : f.getOperatingCapacity().getAccountsPayableTurnover().getVal());
        List<BigDecimal> debt = Arrays.asList(
                f.getSolvency() == null || f.getSolvency().getTotalLiabilityAssets() == null ? null : f.getSolvency().getTotalLiabilityAssets().getVal(),
                f.getSolvency() == null || f.getSolvency().getLtLiabilityTotalAssets() == null ? null : f.getSolvency().getLtLiabilityTotalAssets().getVal(),
                f.getSolvency() == null || f.getSolvency().getStockholderEquityTotalAssets() == null ? null : f.getSolvency().getStockholderEquityTotalAssets().getVal());
        List<BigDecimal> cash = Arrays.asList(
                f.getCashability() == null || f.getCashability().getCurrentRatio() == null ? null : f.getCashability().getCurrentRatio().getVal(),
                f.getCashability() == null || f.getCashability().getQuickRatio() == null ? null : f.getCashability().getQuickRatio().getVal(),
                f.getCashability() == null || f.getCashability().getCashRatio() == null ? null : f.getCashability().getCashRatio().getVal());
        aChar.setStockCode(f.getStockCode());
        aChar.setProfit(profit);
        aChar.setGrowth(growth);
        aChar.setOperating(operating);
        aChar.setDebt(debt);
        aChar.setCash(cash);
        aChar.setTime(f.getEndTimestamp());
    }

}
