package com.vv.finance.investment.bg.handler.uts.sync;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.f10.insurance.*;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresInsuranceEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/7/20 14:20
 * 保险--主要指标摘要数据（图表、估值分析数据）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class F10KeyFiguresInsuranceHandler extends AbstractF10CommonHandler {

    private final Xnhk0209Mapper xnhk0209Mapper;
    private final Xnhk0207Mapper xnhk0207Mapper;
    private final Xnhk0208Mapper xnhk0208Mapper;
    private final Xnhk0210Mapper xnhk0210Mapper;
    private final Xnhk0102Mapper xnhk0102Mapper;


    @Override
    public void sync() {
        List<Xnhk0209> selectList = xnhk0209Mapper.selectList(new QueryWrapper<Xnhk0209>().select("SECCODE", "max(Modified_Date) Modified_Date").groupBy("seccode"));
        selectList.forEach(item -> {
            syncCheck(item.getSeccode(), item.getModifiedDate(), F10KeyFiguresInsuranceEntity.class);
        });
    }

    @Override
    public void syncAll() {
        List<Xnhk0209> selectList = xnhk0209Mapper.selectList(new QueryWrapper<Xnhk0209>().select("SECCODE").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(), null, F10KeyFiguresInsuranceEntity.class));
    }

    @Override
    @SneakyThrows
    public void doSync(String code, Date updateTime) {
        log.info("执行  F10KeyFiguresInsuranceHandler {}", code);

        CompletableFuture<Map<String, F10KeyFiguresInsuranceEntity>> xnhk0209Future = CompletableFuture.supplyAsync(() -> xnhk0209Sync(code, updateTime));
        CompletableFuture<Map<String, F10KeyFiguresInsuranceEntity>> xnhk0208Future = CompletableFuture.supplyAsync(() -> xnhk0208Sync(code, updateTime));
        CompletableFuture<Map<String, F10KeyFiguresInsuranceEntity>> xnhk0207Future = CompletableFuture.supplyAsync(() -> xnhk0207Sync(code, updateTime));
        CompletableFuture<Map<String, F10KeyFiguresInsuranceEntity>> xnhk0210Future = CompletableFuture.supplyAsync(() -> xnhk0210Sync(code, updateTime));
        Map<String, F10KeyFiguresInsuranceEntity> xnhk0209 = xnhk0209Future.get();
        Map<String, F10KeyFiguresInsuranceEntity> xnhk0207 = xnhk0207Future.get();
        Map<String, F10KeyFiguresInsuranceEntity> xnhk0208 = xnhk0208Future.get();
        Map<String, F10KeyFiguresInsuranceEntity> xnhk0210 = xnhk0210Future.get();
        if (MapUtils.isNotEmpty(xnhk0210)) {
            List<F10KeyFiguresInsuranceEntity> lists = new ArrayList<>();
            xnhk0209.forEach((k, v) -> {
                F10KeyFiguresInsuranceEntity xnhk0207Val = xnhk0207.get(k);
                if (xnhk0207Val != null) {
                    v.setKeyFigures(xnhk0207Val.getKeyFigures());
                    GrowthAbilityInsurance growthAbility = xnhk0207Val.getGrowthAbility();
                    F10KeyFiguresInsuranceEntity xnhk0208Val = xnhk0208.get(k);
                    if (xnhk0208Val != null && growthAbility != null && xnhk0208Val.getGrowthAbility() != null) {
                        growthAbility.setTotalAssetsGrowth(xnhk0208Val.getGrowthAbility().getTotalAssetsGrowth());
                    }
                    v.setGrowthAbility(growthAbility);
                }
                F10KeyFiguresInsuranceEntity xnhk0210F = xnhk0210.get(k);
                if (xnhk0210F != null) {
                    F10Val cash = xnhk0210F.getPerShareIndicator().getCashFlowPerShare();
                    if (v.getPerShareIndicator() != null) {
                        v.getPerShareIndicator().setCashFlowPerShare(cash);
                    } else {
                        v.setPerShareIndicator(PerShareIndicatorInsurance.builder().cashFlowPerShare(cash).build());
                    }
                }
                lists.add(v);
//            save(v, F10KeyFiguresInsuranceEntity.class);
            });
            saveBulk(lists, F10KeyFiguresInsuranceEntity.class);
        }


    }

    private Map<String, F10KeyFiguresInsuranceEntity> xnhk0209Sync(String code, Date updateTime) {
        List<Xnhk0209> xnhk0209s = xnhk0209Mapper.selectList(new QueryWrapper<Xnhk0209>().eq("seccode", code));
        List<Xnhk0209> xnhk0209Update = xnhk0209s.stream().filter(item -> {
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());

        }).collect(Collectors.toList());
        xnhk0209Update = convertAndFilterReports(xnhk0209Update, Xnhk0209::getF006v, Xnhk0209::setF006v, Xnhk0209::getF007n);
        Map<String, Xnhk0209> collect = xnhk0209Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
        Map<String, F10KeyFiguresInsuranceEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10KeyFiguresInsuranceEntity figuresInsuranceEntity = new F10KeyFiguresInsuranceEntity();
            figuresInsuranceEntity.setReportType(v.getF006v());
            figuresInsuranceEntity.setStockCode(v.getSeccode());
            figuresInsuranceEntity.setCurrency(v.getF003v());
            figuresInsuranceEntity.setReleaseDate(dateFormat(v.getF001d()));
            figuresInsuranceEntity.setReleaseTimestamp(dateStrToLong(figuresInsuranceEntity.getReleaseDate()));
            figuresInsuranceEntity.setEndDate(dateFormat(v.getF002d()));
            figuresInsuranceEntity.setEndTimestamp(dateStrToLong(figuresInsuranceEntity.getEndDate()));
            figuresInsuranceEntity.setStartDate(calStartDate(figuresInsuranceEntity.getEndDate(), v.getF007n()));
            figuresInsuranceEntity.setStartTimestamp(dateStrToLong(figuresInsuranceEntity.getStartDate()));
            figuresInsuranceEntity.setUpdateTime(v.getModifiedDate());
            Xnhk0209 yoyXnhk0209 = findYoyXnhk0209(v, xnhk0209s);
            profitabilityCal(figuresInsuranceEntity, v, yoyXnhk0209);
            operatingCapacityCal(figuresInsuranceEntity, v, yoyXnhk0209);
            solvencyCal(figuresInsuranceEntity, v, yoyXnhk0209);
            perShareIndicator(figuresInsuranceEntity, v, yoyXnhk0209);
            result.put(k, figuresInsuranceEntity);
        });
        return result;
    }

    private void perShareIndicator(F10KeyFiguresInsuranceEntity figuresInsuranceEntity, Xnhk0209 xnhk0209, Xnhk0209 yoy) {
        BigDecimal f004n = xnhk0209.getF004n();
        F10Val earningPerShare = F10Val.builder().val(divideRate(xnhk0209.getF040n(), f004n)).build();
        F10Val netAssetPerShare = F10Val.builder().val(divideRate(xnhk0209.getF041n(), f004n)).build();

        F10Val impliedValuePerShare = F10Val.builder().val(divideRate(xnhk0209.getF042n(), f004n)).build();

        if (yoy != null) {
            BigDecimal f004nYoy = yoy.getF004n();
            earningPerShare.setYoy(calYoy(earningPerShare.getVal(), divideRate(yoy.getF040n(), f004nYoy)));
            netAssetPerShare.setYoy(calYoy(netAssetPerShare.getVal(), divideRate(yoy.getF041n(), f004nYoy)));
            impliedValuePerShare.setYoy(calYoy(impliedValuePerShare.getVal(), divideRate(yoy.getF042n(), f004nYoy)));
        }
        PerShareIndicatorInsurance perShareIndicatorInsurance = PerShareIndicatorInsurance.builder()
                .earningPerShare(earningPerShare)
                .netAssetPerShare(netAssetPerShare)
                .impliedValuePerShare(impliedValuePerShare).build();
        figuresInsuranceEntity.setPerShareIndicator(perShareIndicatorInsurance);

    }

    /**
     * 计算每股现金流
     */
    private void calcCashFlowPerShare(F10KeyFiguresInsuranceEntity figuresInsuranceEntity, Xnhk0210 xnhk0210, Xnhk0210 yoy) {
        Xnhk0102 xnhk0102 = xnhk0102Mapper.selectOne(new QueryWrapper<Xnhk0102>().select("f069n").eq("seccode", xnhk0210.getSeccode()));
        if (ObjectUtil.isNotEmpty(xnhk0102)) {
            F10Val cashFlowPerShare = F10Val.builder().val(calcCash(xnhk0210.getF008n(), xnhk0102.getF069n())).build();
            if (yoy != null) {
                cashFlowPerShare.setYoy(calYoy(calcCash(xnhk0210.getF008n(), xnhk0102.getF069n()), calcCash(yoy.getF008n(), xnhk0102.getF069n())));
            }
            PerShareIndicatorInsurance perShareIndicatorInsurance = PerShareIndicatorInsurance.builder()
                    .cashFlowPerShare(cashFlowPerShare)
                    .build();
            figuresInsuranceEntity.setPerShareIndicator(perShareIndicatorInsurance);
        }

    }

    /**
     * 偿债能力
     *
     * @param figuresInsuranceEntity
     * @param xnhk0209
     * @param yoy
     */
    private void solvencyCal(F10KeyFiguresInsuranceEntity figuresInsuranceEntity, Xnhk0209 xnhk0209, Xnhk0209 yoy) {

        F10Val totalInvestmentAssets = F10Val.builder().val(xnhk0209.getF017n()).build();

        F10Val cashTotalAssets = F10Val.builder().val(xnhk0209.getF018n()).build();

        F10Val stockholderEquityTotalAssets = F10Val.builder().val(xnhk0209.getF019n()).build();

        F10Val totalEquityTotalAssets = F10Val.builder().val(xnhk0209.getF020n()).build();


        F10Val debtSecurityTotalInvestment = F10Val.builder().val(xnhk0209.getF015n()).build();


        F10Val equitySecurityTotalInvestment = F10Val.builder().val(xnhk0209.getF016n()).build();
        if (yoy != null) {
            totalInvestmentAssets.setYoy(calYoy(xnhk0209.getF017n(), yoy.getF017n()));
            cashTotalAssets.setYoy(calYoy(xnhk0209.getF018n(), yoy.getF018n()));
            stockholderEquityTotalAssets.setYoy(calYoy(xnhk0209.getF019n(), yoy.getF019n()));
            totalEquityTotalAssets.setYoy(calYoy(xnhk0209.getF020n(), yoy.getF020n()));
            debtSecurityTotalInvestment.setYoy(calYoy(xnhk0209.getF015n(), yoy.getF015n()));
            equitySecurityTotalInvestment.setYoy(calYoy(xnhk0209.getF016n(), yoy.getF016n()));

        }
        SolvencyInsurance solvencyInsurance = SolvencyInsurance.builder()
                .debtSecurityTotalInvestment(debtSecurityTotalInvestment)
                .cashTotalAssets(cashTotalAssets)
                .totalEquityTotalAssets(totalEquityTotalAssets)
                .totalInvestmentAssets(totalInvestmentAssets)
                .equitySecurityTotalInvestment(equitySecurityTotalInvestment)
                .stockholderEquityTotalAssets(stockholderEquityTotalAssets).build();
        figuresInsuranceEntity.setSolvency(solvencyInsurance);
    }

    /**
     * 运营能力
     *
     * @param figuresInsuranceEntity
     * @param xnhk0209
     * @param yoy
     */
    private void operatingCapacityCal(F10KeyFiguresInsuranceEntity figuresInsuranceEntity, Xnhk0209 xnhk0209, Xnhk0209 yoy) {
        /**
         * 毛承保费及保单费增长
         */
        F10Val grossPremiumsPolicyFeeGrowth = F10Val.builder().val(xnhk0209.getF021n()).build();
        /**
         * 净承保费及保单费增长
         */
        F10Val netPremiumsPolicyFeeGrowth = F10Val.builder().val(xnhk0209.getF022n()).build();
        /**
         * 已赚取保费及保单费增长
         */
        F10Val earnedPremiumsPolicyFeeGrowth = F10Val.builder().val(xnhk0209.getF023n()).build();
        /**
         * 净投资收益增长
         */
        F10Val netInvestmentIncomeGrowth = F10Val.builder().val(xnhk0209.getF024n()).build();

        /**
         * 保险准备金增长
         */
        F10Val insuranceReserveGrowth = F10Val.builder().val(xnhk0209.getF025n()).build();

        if (yoy != null) {
            BigDecimal f021nYoy = calYoy(xnhk0209.getF021n(), yoy.getF021n());
            BigDecimal f022nYoy = calYoy(xnhk0209.getF022n(), yoy.getF022n());
            BigDecimal f023nYoy = calYoy(xnhk0209.getF023n(), yoy.getF023n());
            BigDecimal f024nYoy = calYoy(xnhk0209.getF024n(), yoy.getF024n());
            BigDecimal f025nYoy = calYoy(xnhk0209.getF025n(), yoy.getF025n());
            grossPremiumsPolicyFeeGrowth.setYoy(f021nYoy);
            netPremiumsPolicyFeeGrowth.setYoy(f022nYoy);
            earnedPremiumsPolicyFeeGrowth.setYoy(f023nYoy);
            netInvestmentIncomeGrowth.setYoy(f024nYoy);
            insuranceReserveGrowth.setYoy(f025nYoy);
        }
        OperatingCapacityInsurance operatingCapacityInsurance = OperatingCapacityInsurance.builder()
                .earnedPremiumsPolicyFeeGrowth(earnedPremiumsPolicyFeeGrowth)
                .grossPremiumsPolicyFeeGrowth(grossPremiumsPolicyFeeGrowth)
                .insuranceReserveGrowth(insuranceReserveGrowth)
                .netInvestmentIncomeGrowth(netInvestmentIncomeGrowth)
                .netPremiumsPolicyFeeGrowth(netPremiumsPolicyFeeGrowth).build();
        figuresInsuranceEntity.setOperatingCapacity(operatingCapacityInsurance);
    }

    /**
     * 成长能力
     *
     * @param figuresInsuranceEntity
     * @param xnhk0207
     * @param yoy
     */
    private void growthAbilityCal(F10KeyFiguresInsuranceEntity figuresInsuranceEntity, Xnhk0207 xnhk0207, Xnhk0207 yoy, Xnhk0207 lastYoy) {
        if (yoy == null) {
            return;
        }
        F10Val operatingRevenueGrowth = F10Val.builder().val(calYoy(xnhk0207.getF018n(), yoy.getF018n())).build();
        F10Val netProfitGrowth = F10Val.builder().val(calYoy(xnhk0207.getF031n(), yoy.getF031n())).build();
        F10Val grossIncomeGrowth = F10Val.builder().val(calYoy(xnhk0207.getF025n(), yoy.getF025n())).build();
        F10Val earningPerShareGrowth = F10Val.builder().val(calYoy(xnhk0207.getF039n(), yoy.getF039n())).build();

        if (lastYoy != null) {
            BigDecimal f018nYoy = calYoy(operatingRevenueGrowth.getVal(), calYoy(yoy.getF018n(), lastYoy.getF018n()));
            BigDecimal f032nYoy = calYoy(netProfitGrowth.getVal(), calYoy(yoy.getF031n(), lastYoy.getF031n()));
            BigDecimal f025nYoy = calYoy(grossIncomeGrowth.getVal(), calYoy(yoy.getF025n(), lastYoy.getF025n()));
            BigDecimal f039nYoy = calYoy(earningPerShareGrowth.getVal(), calYoy(yoy.getF039n(), lastYoy.getF039n()));
            operatingRevenueGrowth.setYoy(f018nYoy);
            netProfitGrowth.setYoy(f032nYoy);
            grossIncomeGrowth.setYoy(f025nYoy);
            earningPerShareGrowth.setYoy(f039nYoy);
        }
        GrowthAbilityInsurance growthAbility = GrowthAbilityInsurance.builder()
                .earningPerShareGrowth(earningPerShareGrowth)
                .grossIncomeGrowth(grossIncomeGrowth)
                .netProfitGrowth(netProfitGrowth)
                .operatingRevenueGrowth(operatingRevenueGrowth).build();
        figuresInsuranceEntity.setGrowthAbility(growthAbility);
    }

    private void growthAbilityTotalCal(F10KeyFiguresInsuranceEntity figuresInsuranceEntity, Xnhk0208 xnhk0208, Xnhk0208 yoy, Xnhk0208 lastYoy) {
        if (yoy == null) {
            return;
        }
        F10Val totalAssetsGrowth = F10Val.builder().val(calYoy(xnhk0208.getF029n(), yoy.getF029n())).build();
        if (lastYoy != null) {
            totalAssetsGrowth.setYoy(calYoy(totalAssetsGrowth.getVal(), calYoy(yoy.getF029n(), lastYoy.getF029n())));
        }
        GrowthAbilityInsurance growthAbility = GrowthAbilityInsurance.builder()
                .totalAssetsGrowth(totalAssetsGrowth).build();
        figuresInsuranceEntity.setGrowthAbility(growthAbility);
    }

    /**
     * 盈利能力
     *
     * @param figuresInsuranceEntity
     * @param xnhk0209
     * @param yoy
     */
    private void profitabilityCal(F10KeyFiguresInsuranceEntity figuresInsuranceEntity, Xnhk0209 xnhk0209, Xnhk0209 yoy) {

        F10Val indemnityRatio = F10Val.builder().val(xnhk0209.getF008n()).build();

        F10Val earningBeforeTaxRatio = F10Val.builder().val(xnhk0209.getF009n()).build();

        F10Val netProfitRatio = F10Val.builder().val(xnhk0209.getF010n()).build();

        F10Val roe = F10Val.builder().val(xnhk0209.getF011n()).build();

        F10Val roa = F10Val.builder().val(xnhk0209.getF012n()).build();

        F10Val averageRoe = F10Val.builder().val(xnhk0209.getF013n()).build();

        F10Val averageRoa = F10Val.builder().val(xnhk0209.getF014n()).build();
        //同比不为空
        if (yoy != null) {
            BigDecimal f008nYoy = calYoy(xnhk0209.getF008n(), yoy.getF008n());
            BigDecimal f009nYoy = calYoy(xnhk0209.getF009n(), yoy.getF009n());
            BigDecimal f010nYoy = calYoy(xnhk0209.getF010n(), yoy.getF010n());
            BigDecimal f011nYoy = calYoy(xnhk0209.getF011n(), yoy.getF011n());
            BigDecimal f012nYoy = calYoy(xnhk0209.getF012n(), yoy.getF012n());
            BigDecimal f013nYoy = calYoy(xnhk0209.getF013n(), yoy.getF013n());
            BigDecimal f014nYoy = calYoy(xnhk0209.getF014n(), yoy.getF014n());
            indemnityRatio.setYoy(f008nYoy);
            earningBeforeTaxRatio.setYoy(f009nYoy);
            netProfitRatio.setYoy(f010nYoy);
            roe.setYoy(f011nYoy);
            roa.setYoy(f012nYoy);
            averageRoe.setYoy(f013nYoy);
            averageRoa.setYoy(f014nYoy);
        }
        ProfitabilityInsurance profitabilityInsurance = ProfitabilityInsurance.builder()
                .indemnityRatio(indemnityRatio).earningBeforeTaxRatio(earningBeforeTaxRatio)
                .netProfitRatio(netProfitRatio).roe(roe).roa(roa).averageRoe(averageRoe)
                .averageRoa(averageRoa).build();
        figuresInsuranceEntity.setProfitability(profitabilityInsurance);

    }

    private void keyFiguresCal(F10KeyFiguresInsuranceEntity figuresInsuranceEntity, Xnhk0207 xnhk0207, Xnhk0207 yoy) {
        /**
         * 营业收入
         */
        F10Val operatingRevenue = F10Val.builder().val(xnhk0207.getF018n()).build();
        /**
         * 净利润
         */
        F10Val netProfits = F10Val.builder().val(xnhk0207.getF031n()).build();
        if (yoy != null) {
            operatingRevenue.setYoy(calYoy(xnhk0207.getF018n(), yoy.getF018n()));
            netProfits.setYoy(calYoy(xnhk0207.getF031n(), yoy.getF031n()));
        }
        KeyFiguresInsurance keyFiguresInsurance = KeyFiguresInsurance.builder()
                .operatingRevenue(operatingRevenue)
                .netProfits(netProfits).build();
        figuresInsuranceEntity.setKeyFigures(keyFiguresInsurance);
    }

    private Xnhk0209 findYoyXnhk0209(Xnhk0209 xnhk0209, List<Xnhk0209> xnhk0209s) {
        return xnhk0209s.stream().filter(item -> item.getF006v().equals(xnhk0209.getF006v()) && item.getF002d() < xnhk0209.getF002d()).max(Comparator.comparing(Xnhk0209::getF002d)).orElse(null);

    }

    private Xnhk0207 findYoyXnhk0207(Xnhk0207 xnhk0207, List<Xnhk0207> xnhk0207s) {
        if (xnhk0207 == null) {
            return null;
        }
        return xnhk0207s.stream().filter(item -> item.getF006v().equals(xnhk0207.getF006v()) && item.getF002d() < xnhk0207.getF002d()).max(Comparator.comparing(Xnhk0207::getF002d)).orElse(null);

    }


    private Xnhk0210 findYoyXnhk0210(Xnhk0210 xnhk0210, List<Xnhk0210> xnhk0210s) {
        if (xnhk0210 == null) {
            return null;
        }
        return xnhk0210s.stream().filter(item -> item.getF006v().equals(xnhk0210.getF006v()) && item.getF002d() < xnhk0210.getF002d()).max(Comparator.comparing(Xnhk0210::getF002d)).orElse(null);

    }

    private Xnhk0208 findYoyXnhk0208(Xnhk0208 xnhk0208, List<Xnhk0208> xnhk0208s) {
        if (xnhk0208 == null) {
            return null;
        }
        return xnhk0208s.stream().filter(item -> item.getF006v().equals(xnhk0208.getF006v()) && item.getF002d() < xnhk0208.getF002d()).max(Comparator.comparing(Xnhk0208::getF002d)).orElse(null);

    }


    private Map<String, F10KeyFiguresInsuranceEntity> xnhk0208Sync(String code, Date updateTime) {
        List<Xnhk0208> xnhk0208s = xnhk0208Mapper.selectList(new QueryWrapper<Xnhk0208>().eq("seccode", code));
        List<Xnhk0208> xnhk0208Update = xnhk0208s.stream().filter(item -> {
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());
        }).collect(Collectors.toList());
        xnhk0208Update = convertAndFilterReports(xnhk0208Update, Xnhk0208::getF006v, Xnhk0208::setF006v, Xnhk0208::getF007n);
        Map<String, Xnhk0208> collect = xnhk0208Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
        Map<String, F10KeyFiguresInsuranceEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10KeyFiguresInsuranceEntity figuresInsuranceEntity = new F10KeyFiguresInsuranceEntity();
            Xnhk0208 yoyXnhk0208 = findYoyXnhk0208(v, xnhk0208s);
            Xnhk0208 lastYoyXnhk0208 = findYoyXnhk0208(yoyXnhk0208, xnhk0208s);
            growthAbilityTotalCal(figuresInsuranceEntity, v, yoyXnhk0208, lastYoyXnhk0208);
            result.put(k, figuresInsuranceEntity);
        });
        return result;
    }

    private Map<String, F10KeyFiguresInsuranceEntity> xnhk0207Sync(String code, Date updateTime) {
        List<Xnhk0207> xnhk0207s = xnhk0207Mapper.selectList(new QueryWrapper<Xnhk0207>().eq("seccode", code));
        List<Xnhk0207> xnhk0207Update = xnhk0207s.stream().filter(item -> {
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());

        }).collect(Collectors.toList());
        xnhk0207Update = convertAndFilterReports(xnhk0207Update, Xnhk0207::getF006v, Xnhk0207::setF006v, Xnhk0207::getF007n);
        Map<String, Xnhk0207> collect = xnhk0207Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
        Map<String, F10KeyFiguresInsuranceEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10KeyFiguresInsuranceEntity figuresInsuranceEntity = new F10KeyFiguresInsuranceEntity();

            Xnhk0207 yoyXnhk0207 = findYoyXnhk0207(v, xnhk0207s);
            Xnhk0207 lastYoyXnhk0207 = findYoyXnhk0207(yoyXnhk0207, xnhk0207s);
            growthAbilityCal(figuresInsuranceEntity, v, yoyXnhk0207, lastYoyXnhk0207);
            keyFiguresCal(figuresInsuranceEntity, v, yoyXnhk0207);
            result.put(k, figuresInsuranceEntity);
        });
        return result;
    }


    private Map<String, F10KeyFiguresInsuranceEntity> xnhk0210Sync(String code, Date updateTime) {
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
        Map<String, F10KeyFiguresInsuranceEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10KeyFiguresInsuranceEntity figuresInsuranceEntity = new F10KeyFiguresInsuranceEntity();
            Xnhk0210 yoyXnhk0210 = findYoyXnhk0210(v, xnhk0210s);
            calcCashFlowPerShare(figuresInsuranceEntity, v, yoyXnhk0210);
            if (ObjectUtil.isNotEmpty(figuresInsuranceEntity.getPerShareIndicator())) {
                result.put(k, figuresInsuranceEntity);
            }

        });
        return result;
    }


}
