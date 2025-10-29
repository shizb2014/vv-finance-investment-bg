package com.vv.finance.investment.bg.handler.uts.sync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.f10.financial.*;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresFinancialEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/7/20 14:20
 * 金融--主要指标摘要数据（图表、估值分析数据）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class F10KeyFiguresFinancialHandler extends AbstractF10CommonHandler {

    private final Xnhk0206Mapper xnhk0206Mapper;
    private final Xnhk0205Mapper xnhk0205Mapper;
    private final Xnhk0204Mapper xnhk0204Mapper;
    private final Xnhk0210Mapper xnhk0210Mapper;
    private final Xnhk0102Mapper xnhk0102Mapper;


    @Override
    public void sync(){
        List<Xnhk0206> selectList = xnhk0206Mapper.selectList(new QueryWrapper<Xnhk0206>().select("SECCODE", "max(Modified_Date) Modified_Date").groupBy("seccode"));
        selectList.forEach(item -> {
            syncCheck(item.getSeccode(),item.getModifiedDate(), F10KeyFiguresFinancialEntity.class);
        });
    }

    @Override
    public void syncAll() {
        List<Xnhk0206> selectList = xnhk0206Mapper.selectList(new QueryWrapper<Xnhk0206>().select("SECCODE").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(), null, F10KeyFiguresFinancialEntity.class));
    }

    @Override
    @SneakyThrows
    public void doSync(String code, Date updateTime) {
        log.info("执行  F10KeyFiguresFinancialHandler {}",code);
        CompletableFuture<Map<String, F10KeyFiguresFinancialEntity>> xnhk0206Future = CompletableFuture.supplyAsync(() -> xnhk0206Sync(code, updateTime));
        CompletableFuture<Map<String, F10KeyFiguresFinancialEntity>> xnhk0205Future = CompletableFuture.supplyAsync(() -> xnhk0205Sync(code, updateTime));
        CompletableFuture<Map<String, F10KeyFiguresFinancialEntity>> xnhk0204Future = CompletableFuture.supplyAsync(() -> xnhk0204Sync(code, updateTime));
        CompletableFuture<Map<String, F10KeyFiguresFinancialEntity>> xnhk0210Future = CompletableFuture.supplyAsync(() -> xnhk0210Sync(code, updateTime));
        Map<String, F10KeyFiguresFinancialEntity> xnhk0206 = xnhk0206Future.get();
        Map<String, F10KeyFiguresFinancialEntity> xnhk0204 = xnhk0204Future.get();
        Map<String, F10KeyFiguresFinancialEntity> xnhk0205 = xnhk0205Future.get();
        Map<String, F10KeyFiguresFinancialEntity> xnhk0210 = xnhk0210Future.get();
        xnhk0206.forEach((k, v) -> {
            F10KeyFiguresFinancialEntity xnhk0204Val = xnhk0204.get(k);
            if (xnhk0204Val != null) {
                v.setKeyFigures(xnhk0204Val.getKeyFigures());
                GrowthAbilityFinancial growthAbility = xnhk0204Val.getGrowthAbility();
                F10KeyFiguresFinancialEntity xnhk0205Val = xnhk0205.get(k);
                if (xnhk0205Val != null && growthAbility != null && xnhk0205Val.getGrowthAbility() != null) {
                    growthAbility.setTotalAssetsGrowth(xnhk0205Val.getGrowthAbility().getTotalAssetsGrowth());
                }
                v.setGrowthAbility(growthAbility);
                //净利润
                BigDecimal netProfits = xnhk0204Val.getKeyFigures() == null || xnhk0204Val.getKeyFigures().getNetProfits() == null ? null : xnhk0204Val.getKeyFigures().getNetProfits().getVal();
                //营业收入
                BigDecimal operatingRevenue = xnhk0204Val.getKeyFigures() == null || xnhk0204Val.getKeyFigures().getOperatingRevenue() == null ? null : xnhk0204Val.getKeyFigures().getOperatingRevenue().getVal();
                BigDecimal netProfitRatioVal = calcNetProfitRatio(netProfits, operatingRevenue);
                F10Val netProfitRatio = F10Val.builder().val(netProfitRatioVal).build();
                v.getProfitability().setNetProfitRatio(netProfitRatio);
            }
            F10KeyFiguresFinancialEntity entity0210 = xnhk0210.get(k);
            if (entity0210 != null) {
                F10Val cash = entity0210.getPerShareIndicator().getCashFlowPerShare();
                if (v.getPerShareIndicator() != null) {
                    v.getPerShareIndicator().setCashFlowPerShare(cash);
                }
            }
        });

        List<F10KeyFiguresFinancialEntity> lists = new ArrayList<>();
        xnhk0206.forEach((k, v) -> {

            String reportType = v.getReportType();
            F10KeyFiguresFinancialEntity yoyEntity = xnhk0206.values().stream().
                    filter(x -> x.getEndTimestamp() < v.getEndTimestamp()&& reportType.equals(x.getReportType())).max(Comparator.comparing(F10KeyFiguresFinancialEntity::getEndTimestamp)).orElse(null);
            if (yoyEntity != null) {
                BigDecimal val = v.getProfitability() == null || v.getProfitability().getNetProfitRatio() == null ? null : v.getProfitability().getNetProfitRatio().getVal();
                if (val != null) {
                    BigDecimal lastVal = yoyEntity.getProfitability() == null || yoyEntity.getProfitability().getNetProfitRatio() == null ? null : yoyEntity.getProfitability().getNetProfitRatio().getVal();
                    BigDecimal netProfitRatioYoy = calYoy(val, lastVal);
                    v.getProfitability().getNetProfitRatio().setYoy(netProfitRatioYoy);
                }

            }
            lists.add(v);
//            save(v, F10KeyFiguresFinancialEntity.class);
        });
        saveBulk(lists, F10KeyFiguresFinancialEntity.class);

    }

    private Map<String, F10KeyFiguresFinancialEntity> xnhk0206Sync(String code, Date updateTime) {
        List<Xnhk0206> xnhk0206s = xnhk0206Mapper.selectList(new QueryWrapper<Xnhk0206>().eq("seccode", code));
        List<Xnhk0206> xnhk0206Update = xnhk0206s.stream().filter(item -> {
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());

        }).collect(Collectors.toList());
        xnhk0206Update = convertAndFilterReports(xnhk0206Update, Xnhk0206::getF006v, Xnhk0206::setF006v, Xnhk0206::getF007n);
        Map<String, Xnhk0206> collect = xnhk0206Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
        Map<String, F10KeyFiguresFinancialEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10KeyFiguresFinancialEntity figuresFinancialEntity = new F10KeyFiguresFinancialEntity();
            figuresFinancialEntity.setReportType(v.getF006v());
            figuresFinancialEntity.setStockCode(v.getSeccode());
            figuresFinancialEntity.setCurrency(v.getF003v());
            figuresFinancialEntity.setReleaseDate(dateFormat(v.getF001d()));
            figuresFinancialEntity.setReleaseTimestamp(dateStrToLong(figuresFinancialEntity.getReleaseDate()));
            figuresFinancialEntity.setEndDate(dateFormat(v.getF002d()));
            figuresFinancialEntity.setEndTimestamp(dateStrToLong(figuresFinancialEntity.getEndDate()));
            figuresFinancialEntity.setStartDate(calStartDate(figuresFinancialEntity.getEndDate(), v.getF007n()));
            figuresFinancialEntity.setStartTimestamp(dateStrToLong(figuresFinancialEntity.getStartDate()));
            figuresFinancialEntity.setUpdateTime(v.getModifiedDate());
            Xnhk0206 yoyXnhk0206 = findYoyXnhk0206(v, xnhk0206s);
            profitabilityCal(figuresFinancialEntity, v, yoyXnhk0206);
            operatingCapacityCal(figuresFinancialEntity, v, yoyXnhk0206);
            solvencyCal(figuresFinancialEntity, v, yoyXnhk0206);
            perShareIndicator(figuresFinancialEntity, v, yoyXnhk0206);
            result.put(k, figuresFinancialEntity);
        });
        return result;

    }

    private void perShareIndicator(F10KeyFiguresFinancialEntity figuresFinancialEntity, Xnhk0206 xnhk0206, Xnhk0206 yoy) {

        F10Val earningPerShare = F10Val.builder().val(divideRate(xnhk0206.getF048n(),xnhk0206.getF004n())).build();
        F10Val netAssetPerShare = F10Val.builder().val(divideRate(xnhk0206.getF049n(),xnhk0206.getF004n())).build();

        if (yoy != null) {
            earningPerShare.setYoy(calYoy(earningPerShare.getVal(), divideRate(yoy.getF048n(),yoy.getF004n())));
            netAssetPerShare.setYoy(calYoy(netAssetPerShare.getVal(), divideRate(yoy.getF049n(),yoy.getF004n())));

        }
        PerShareIndicatorFinancial perShareIndicatorFinancial = PerShareIndicatorFinancial.builder()
                .earningPerShare(earningPerShare)
                .netAssetPerShare(netAssetPerShare).build();
        figuresFinancialEntity.setPerShareIndicator(perShareIndicatorFinancial);

    }

    /**
     * 偿债能力
     *
     * @param figuresFinancialEntity
     * @param xnhk0206
     * @param yoy
     */
    private void solvencyCal(F10KeyFiguresFinancialEntity figuresFinancialEntity, Xnhk0206 xnhk0206, Xnhk0206 yoy) {

        F10Val loansDeposits = F10Val.builder().val(xnhk0206.getF021n()).build();

        F10Val loansTotalAssets = F10Val.builder().val(xnhk0206.getF024n()).build();

        F10Val loansStockholderEquity = F10Val.builder().val(xnhk0206.getF022n()).build();

        F10Val loansTotalEquity = F10Val.builder().val(xnhk0206.getF023n()).build();

        F10Val depositsTotalAssets = F10Val.builder().val(xnhk0206.getF027n()).build();

        F10Val depositsStockholderEquity = F10Val.builder().val(xnhk0206.getF025n()).build();

        F10Val depositsTotalEquity = F10Val.builder().val(xnhk0206.getF026n()).build();

        F10Val stockholderEquityTotalAssets = F10Val.builder().val(xnhk0206.getF028n()).build();

        F10Val totalEquityTotalAssets = F10Val.builder().val(xnhk0206.getF029n()).build();
        if (yoy != null) {
            loansDeposits.setYoy(calYoy(xnhk0206.getF021n(), yoy.getF021n()));
            loansTotalAssets.setYoy(calYoy(xnhk0206.getF024n(), yoy.getF024n()));
            loansStockholderEquity.setYoy(calYoy(xnhk0206.getF022n(), yoy.getF022n()));
            loansTotalEquity.setYoy(calYoy(xnhk0206.getF023n(), yoy.getF023n()));
            depositsTotalAssets.setYoy(calYoy(xnhk0206.getF027n(), yoy.getF027n()));
            depositsStockholderEquity.setYoy(calYoy(xnhk0206.getF025n(), yoy.getF025n()));
            depositsTotalEquity.setYoy(calYoy(xnhk0206.getF026n(), yoy.getF026n()));
            stockholderEquityTotalAssets.setYoy(calYoy(xnhk0206.getF028n(), yoy.getF028n()));
            totalEquityTotalAssets.setYoy(calYoy(xnhk0206.getF029n(), yoy.getF029n()));

        }
        SolvencyFinancial solvencyFinancial = SolvencyFinancial.builder().loansDeposits(loansDeposits)
                .loansTotalAssets(loansTotalAssets)
                .loansStockholderEquity(loansStockholderEquity)
                .loansTotalEquity(loansTotalEquity)
                .depositsTotalAssets(depositsTotalAssets)
                .depositsStockholderEquity(depositsStockholderEquity)
                .depositsTotalEquity(depositsTotalEquity)
                .stockholderEquityTotalAssets(stockholderEquityTotalAssets)
                .totalEquityTotalAssets(totalEquityTotalAssets)
                .build();
        figuresFinancialEntity.setSolvency(solvencyFinancial);
    }

    /**
     * 运营能力
     *
     * @param figuresFinancialEntity
     * @param xnhk0206
     * @param yoy
     */
    private void operatingCapacityCal(F10KeyFiguresFinancialEntity figuresFinancialEntity, Xnhk0206 xnhk0206, Xnhk0206 yoy) {

        F10Val provisionForImpairmentToCustomerLoansRatio = F10Val.builder().val(xnhk0206.getF032n()).build();

        F10Val overdueLoanRatio = F10Val.builder().val(xnhk0206.getF033n()).build();

        F10Val restructuredLoanRatio = F10Val.builder().val(xnhk0206.getF034n()).build();

        F10Val recapitalizationRatio = F10Val.builder().val(xnhk0206.getF019n()).build();

        F10Val averageLiquidityRatio = F10Val.builder().val(xnhk0206.getF020n()).build();

        if (yoy != null) {
            provisionForImpairmentToCustomerLoansRatio.setYoy(calYoy(xnhk0206.getF032n(), yoy.getF032n()));
            overdueLoanRatio.setYoy(calYoy(xnhk0206.getF033n(), yoy.getF033n()));
            restructuredLoanRatio.setYoy(calYoy(xnhk0206.getF034n(), yoy.getF034n()));
            recapitalizationRatio.setYoy(calYoy(xnhk0206.getF019n(), yoy.getF019n()));
            averageLiquidityRatio.setYoy(calYoy(xnhk0206.getF020n(), yoy.getF020n()));
        }
        OperatingCapacityFinancial operatingCapacityFinancial = OperatingCapacityFinancial.builder()
                .provisionForImpairmentToCustomerLoansRatio(provisionForImpairmentToCustomerLoansRatio)
                .overdueLoanRatio(overdueLoanRatio)
                .recapitalizationRatio(recapitalizationRatio)
                .restructuredLoanRatio(restructuredLoanRatio)
                .averageLiquidityRatio(averageLiquidityRatio).build();
        figuresFinancialEntity.setOperatingCapacity(operatingCapacityFinancial);
    }

    /**
     * 成长能力
     *
     * @param figuresFinancialEntity
     * @param xnhk0204
     * @param yoy
     */
    private void growthAbilityCal(F10KeyFiguresFinancialEntity figuresFinancialEntity, Xnhk0204 xnhk0204, Xnhk0204 yoy, Xnhk0204 lastYoy) {
        if (yoy == null) {
            return;
        }
        F10Val operatingRevenueGrowth = F10Val.builder().val(calYoy(xnhk0204.getF017n(), yoy.getF017n())).build();
        F10Val netProfitGrowth = F10Val.builder().val(calYoy(xnhk0204.getF032n(), yoy.getF032n())).build();
        F10Val grossIncomeGrowth = F10Val.builder().val(calYoy(xnhk0204.getF027n(), yoy.getF027n())).build();
        F10Val earningPerShareGrowth = F10Val.builder().val(calYoy(xnhk0204.getF040n(), yoy.getF040n())).build();

        if (lastYoy != null) {
            BigDecimal f017nYoy = calYoy(operatingRevenueGrowth.getVal(), calYoy(yoy.getF017n(), lastYoy.getF017n()));
            BigDecimal f033nYoy = calYoy(netProfitGrowth.getVal(), calYoy(yoy.getF032n(), lastYoy.getF032n()));
            BigDecimal f027nYoy = calYoy(grossIncomeGrowth.getVal(), calYoy(yoy.getF027n(), lastYoy.getF027n()));
            BigDecimal f040nYoy = calYoy(earningPerShareGrowth.getVal(), calYoy(yoy.getF040n(), lastYoy.getF040n()));
            operatingRevenueGrowth.setYoy(f017nYoy);
            netProfitGrowth.setYoy(f033nYoy);
            grossIncomeGrowth.setYoy(f027nYoy);
            earningPerShareGrowth.setYoy(f040nYoy);
        }
        GrowthAbilityFinancial growthAbility = GrowthAbilityFinancial.builder()
                .earningPerShareGrowth(earningPerShareGrowth)
                .grossIncomeGrowth(grossIncomeGrowth)
                .netProfitGrowth(netProfitGrowth)
                .operatingRevenueGrowth(operatingRevenueGrowth).build();
        figuresFinancialEntity.setGrowthAbility(growthAbility);
    }

    private void growthAbilityTotalCal(F10KeyFiguresFinancialEntity figuresFinancialEntity, Xnhk0205 xnhk0205, Xnhk0205 yoy, Xnhk0205 lastYoy) {
        if (yoy == null) {
            return;
        }
        F10Val totalAssetsGrowth = F10Val.builder().val(calYoy(xnhk0205.getF027n(), yoy.getF027n())).build();
        if (lastYoy != null) {
            totalAssetsGrowth.setYoy(calYoy(totalAssetsGrowth.getVal(), calYoy(yoy.getF027n(), lastYoy.getF027n())));
        }
        GrowthAbilityFinancial growthAbility = GrowthAbilityFinancial.builder()
                .totalAssetsGrowth(totalAssetsGrowth).build();
        figuresFinancialEntity.setGrowthAbility(growthAbility);
    }

    /**
     * 盈利能力
     *
     * @param figuresFinancialEntity
     * @param xnhk0206
     * @param yoy
     */
    private void profitabilityCal(F10KeyFiguresFinancialEntity figuresFinancialEntity, Xnhk0206 xnhk0206, Xnhk0206 yoy) {


        F10Val costRevenue = F10Val.builder().val(xnhk0206.getF009n()).build();

        F10Val netInterestMargin = F10Val.builder().val(xnhk0206.getF008n()).build();

        F10Val loanReturn = F10Val.builder().val(xnhk0206.getF010n()).build();

        F10Val returnOnDeposit = F10Val.builder().val(xnhk0206.getF011n()).build();

        F10Val roe = F10Val.builder().val(xnhk0206.getF012n()).build();

        F10Val roa = F10Val.builder().val(xnhk0206.getF013n()).build();

        F10Val averageLoanReturn = F10Val.builder().val(xnhk0206.getF014n()).build();

        F10Val averageReturnOnDeposit = F10Val.builder().val(xnhk0206.getF015n()).build();

        F10Val averageRoe = F10Val.builder().val(xnhk0206.getF016n()).build();

        F10Val averageRoa = F10Val.builder().val(xnhk0206.getF017n()).build();

        if (yoy != null) {
            costRevenue.setYoy(calYoy(xnhk0206.getF009n(), yoy.getF009n()));

            netInterestMargin.setYoy(calYoy(xnhk0206.getF008n(), yoy.getF008n()));

            loanReturn.setYoy(calYoy(xnhk0206.getF010n(), yoy.getF010n()));

            returnOnDeposit.setYoy(calYoy(xnhk0206.getF011n(), yoy.getF011n()));

            roe.setYoy(calYoy(xnhk0206.getF012n(), yoy.getF012n()));

            roa.setYoy(calYoy(xnhk0206.getF013n(), yoy.getF013n()));

            averageLoanReturn.setYoy(calYoy(xnhk0206.getF014n(), yoy.getF014n()));

            averageReturnOnDeposit.setYoy(calYoy(xnhk0206.getF015n(), yoy.getF015n()));

            averageRoe.setYoy(calYoy(xnhk0206.getF016n(), yoy.getF016n()));

            averageRoa.setYoy(calYoy(xnhk0206.getF017n(), yoy.getF017n()));
        }
        ProfitabilityFinancial profitabilityFinancial = ProfitabilityFinancial.builder()
                .costRevenue(costRevenue).netInterestMargin(netInterestMargin)
                .loanReturn(loanReturn).returnOnDeposit(returnOnDeposit)
                .roa(roa).roe(roe).averageLoanReturn(averageLoanReturn)
                .averageReturnOnDeposit(averageReturnOnDeposit).averageRoe(averageRoe)
                .averageRoa(averageRoa).build();
        figuresFinancialEntity.setProfitability(profitabilityFinancial);

    }

    private void keyFiguresCal(F10KeyFiguresFinancialEntity figuresFinancialEntity, Xnhk0204 xnhk0204, Xnhk0204 yoy) {
        /**
         * 营业收入
         */
        F10Val operatingRevenue = F10Val.builder().val(xnhk0204.getF017n()).build();
        /**
         * 净利润
         */
        F10Val netProfits = F10Val.builder().val(xnhk0204.getF032n()).build();
        if (yoy != null) {
            operatingRevenue.setYoy(calYoy(xnhk0204.getF017n(), yoy.getF017n()));
            netProfits.setYoy(calYoy(xnhk0204.getF032n(), yoy.getF032n()));
        }
        KeyFiguresFinancial keyFiguresFinancial = KeyFiguresFinancial.builder()
                .operatingRevenue(operatingRevenue)
                .netProfits(netProfits).build();
        figuresFinancialEntity.setKeyFigures(keyFiguresFinancial);
    }


    private Xnhk0206 findYoyXnhk0206(Xnhk0206 xnhk0206, List<Xnhk0206> xnhk0206s) {

        return xnhk0206s.stream().filter(item->item.getF006v().equals(xnhk0206.getF006v())&&item.getF002d()<xnhk0206.getF002d()).max(Comparator.comparing(Xnhk0206::getF002d)).orElse(null);

    }

    private Xnhk0204 findYoyXnhk0204(Xnhk0204 xnhk0204, List<Xnhk0204> xnhk0204s) {
        if (xnhk0204 == null) {
            return null;
        }
        return xnhk0204s.stream().filter(item->item.getF006v().equals(xnhk0204.getF006v())&&item.getF002d()<xnhk0204.getF002d()).max(Comparator.comparing(Xnhk0204::getF002d)).orElse(null);
    }

    private Xnhk0205 findYoyXnhk0205(Xnhk0205 xnhk0205, List<Xnhk0205> xnhk0205s) {
        if (xnhk0205 == null) {
            return null;
        }
        return xnhk0205s.stream().filter(item->item.getF006v().equals(xnhk0205.getF006v())&&item.getF002d()<xnhk0205.getF002d()).max(Comparator.comparing(Xnhk0205::getF002d)).orElse(null);

    }

    private Map<String, F10KeyFiguresFinancialEntity> xnhk0205Sync(String code, Date updateTime) {
        List<Xnhk0205> xnhk0205s = xnhk0205Mapper.selectList(new QueryWrapper<Xnhk0205>().select("Modified_Date","f001d", "f002d", "f007d", "F027N", "f006v").eq("seccode", code));
        List<Xnhk0205> xnhk0205Update = xnhk0205s.stream().filter(item -> {
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());
        }).collect(Collectors.toList());
        xnhk0205Update = convertAndFilterReports(xnhk0205Update, Xnhk0205::getF006v, Xnhk0205::setF006v, Xnhk0205::getF007d);
        Map<String, Xnhk0205> collect = xnhk0205Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
        Map<String, F10KeyFiguresFinancialEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10KeyFiguresFinancialEntity figuresFinancialEntity = new F10KeyFiguresFinancialEntity();
            Xnhk0205 yoyXnhk0205 = findYoyXnhk0205(v, xnhk0205s);
            Xnhk0205 lastYoyXnhk0205 = findYoyXnhk0205(yoyXnhk0205, xnhk0205s);
            growthAbilityTotalCal(figuresFinancialEntity, v, yoyXnhk0205, lastYoyXnhk0205);
            result.put(k, figuresFinancialEntity);
        });
        return result;
    }

    private Map<String, F10KeyFiguresFinancialEntity> xnhk0204Sync(String code, Date updateTime) {
        List<Xnhk0204> Xnhk0204s = xnhk0204Mapper.selectList(new QueryWrapper<Xnhk0204>().eq("seccode", code));
        List<Xnhk0204> Xnhk0204Update = Xnhk0204s.stream().filter(item -> {
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());

        }).collect(Collectors.toList());
        Xnhk0204Update = convertAndFilterReports(Xnhk0204Update, Xnhk0204::getF006v, Xnhk0204::setF006v, Xnhk0204::getF007d);
        Map<String, Xnhk0204> collect = Xnhk0204Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
        Map<String, F10KeyFiguresFinancialEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10KeyFiguresFinancialEntity figuresFinancialEntity = new F10KeyFiguresFinancialEntity();

            Xnhk0204 yoyXnhk0204 = findYoyXnhk0204(v, Xnhk0204s);
            Xnhk0204 lastYoyXnhk0204 = findYoyXnhk0204(yoyXnhk0204, Xnhk0204s);
            growthAbilityCal(figuresFinancialEntity, v, yoyXnhk0204, lastYoyXnhk0204);
            keyFiguresCal(figuresFinancialEntity, v, yoyXnhk0204);
            result.put(k, figuresFinancialEntity);
        });
        return result;
    }

    private Map<String, F10KeyFiguresFinancialEntity> xnhk0210Sync(String code, Date updateTime) {
        List<Xnhk0210> xnhk0210s = xnhk0210Mapper.selectList(new QueryWrapper<Xnhk0210>().eq("seccode", code));
        List<Xnhk0210> xnhk0210Update = xnhk0210s.stream().filter(item -> {
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());

        }).collect(Collectors.toList());
        xnhk0210Update = convertAndFilterReports(xnhk0210Update, Xnhk0210::getF006v, Xnhk0210::setF006v, Xnhk0210::getF007n);
        Map<String, Xnhk0210> collect = xnhk0210Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
        Map<String, F10KeyFiguresFinancialEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10KeyFiguresFinancialEntity entity = new F10KeyFiguresFinancialEntity();
            Xnhk0210 yoyXnhk0210 = findYoyXnhk0210(v, xnhk0210s);
            calcCashFlowPerShare(entity, v, yoyXnhk0210);
            result.put(k, entity);
        });
        return result;
    }


    private Xnhk0210 findYoyXnhk0210(Xnhk0210 xnhk0210, List<Xnhk0210> xnhk0210s) {
        if (xnhk0210 == null) {
            return null;
        }
        Long f002d = xnhk0210.getF002d();
        String f006v = xnhk0210.getF006v();
        return xnhk0210s.stream().filter(item->item.getF006v().equals(f006v)&&item.getF002d()<f002d).max(Comparator.comparing(Xnhk0210::getF002d)).orElse(null);

    }

    /**
     * 计算每股现金流
     */
    private void calcCashFlowPerShare(F10KeyFiguresFinancialEntity entity, Xnhk0210 xnhk0210, Xnhk0210 yoy) {
        Xnhk0102 xnhk0102 = xnhk0102Mapper.selectOne(new QueryWrapper<Xnhk0102>().select("f069n").eq("seccode", xnhk0210.getSeccode()));
        if(xnhk0102 ==null){
            entity.setPerShareIndicator(new PerShareIndicatorFinancial());
            return;
        }
        F10Val cashFlowPerShare = F10Val.builder().val(calcCash(xnhk0210.getF008n(), xnhk0102.getF069n())).build();
        if (yoy != null) {
            cashFlowPerShare.setYoy(calYoy(calcCash(xnhk0210.getF008n(), xnhk0102.getF069n()), calcCash(yoy.getF008n(), xnhk0102.getF069n())));
        }
        PerShareIndicatorFinancial perShareIndicatorFinancial = PerShareIndicatorFinancial.builder()
                .cashFlowPerShare(cashFlowPerShare)
                .build();
        entity.setPerShareIndicator(perShareIndicatorFinancial);
    }

}
