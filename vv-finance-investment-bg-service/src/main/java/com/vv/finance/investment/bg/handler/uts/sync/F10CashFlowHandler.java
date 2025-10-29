package com.vv.finance.investment.bg.handler.uts.sync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.uts.Xnhk0201;
import com.vv.finance.investment.bg.entity.uts.Xnhk0204;
import com.vv.finance.investment.bg.entity.uts.Xnhk0207;
import com.vv.finance.investment.bg.entity.uts.Xnhk0210;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0201Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0204Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0207Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0210Mapper;
import com.vv.finance.investment.bg.mongo.model.F10CashFlowEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName F10CashFlowHandler
 * @Deacription 现金流量表handler
 * @Author lh.sz
 * @Date 2021年07月21日 13:51
 **/
@Component
@RequiredArgsConstructor
@Slf4j
public class F10CashFlowHandler extends AbstractF10CommonHandler {

    private final Xnhk0210Mapper xnhk0210Mapper;

    private final Xnhk0201Mapper xnhk0201Mapper;

    private final Xnhk0204Mapper xnhk0204Mapper;

    private final Xnhk0207Mapper xnhk0207Mapper;

    private final Executor asyncServiceExecutor;


    @Override
    public void sync() {
        List<Xnhk0210> selectList = xnhk0210Mapper.selectList(new QueryWrapper<Xnhk0210>().select("SECCODE", "max(Modified_Date) Modified_Date").groupBy("seccode"));
        selectList.forEach(item -> {
            syncCheck(item.getSeccode(),item.getModifiedDate(), F10CashFlowEntity.class);
        });
    }

    @Override
    public void syncAll() {
        List<Xnhk0210> selectList = xnhk0210Mapper.selectList(new QueryWrapper<Xnhk0210>().select("SECCODE").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(),null, F10CashFlowEntity.class));
    }




    @Override
    @SneakyThrows
    public void doSync(String code, Date updateTime) {
        log.info("执行  F10CashFlowHandler {}",code);
        CompletableFuture<Map<String, F10CashFlowEntity>> xnhk0210Future = CompletableFuture.supplyAsync(() -> xnhk0210Sync(code, updateTime), asyncServiceExecutor);
        CompletableFuture<Map<String, F10CashFlowEntity>> xnhk0207Future = CompletableFuture.supplyAsync(() -> xnhk0207Sync(code, updateTime), asyncServiceExecutor);
        CompletableFuture<Map<String, F10CashFlowEntity>> xnhk0201Future = CompletableFuture.supplyAsync(() -> xnhk0201Sync(code, updateTime), asyncServiceExecutor);
        CompletableFuture<Map<String, F10CashFlowEntity>> xnhk0204Future = CompletableFuture.supplyAsync(() -> xnhk0204Sync(code, updateTime), asyncServiceExecutor);
        Map<String, F10CashFlowEntity> xnhk0210 = xnhk0210Future.get();
        Map<String, F10CashFlowEntity> xnhk0207 = xnhk0207Future.get();
        Map<String, F10CashFlowEntity> xnhk0201 = xnhk0201Future.get();
        Map<String, F10CashFlowEntity> xnhk0204 = xnhk0204Future.get();

        List<F10CashFlowEntity> lists = new ArrayList<>();
        xnhk0210.forEach((k, v) -> {
            F10CashFlowEntity xnhk0207Val = xnhk0207.get(k);
            F10CashFlowEntity xnhk0201Val = xnhk0201.get(k);
            F10CashFlowEntity xnhk0204Val = xnhk0204.get(k);
            //除税前溢利
            if (xnhk0207Val != null) {
                v.setProfitBeforeTaxation(xnhk0207Val.getProfitBeforeTaxation());
            }
            if (xnhk0201Val != null) {
                v.setProfitBeforeTaxation(xnhk0201Val.getProfitBeforeTaxation());
                v.setDpme(xnhk0201Val.getDpme());
                v.setAmortizationIntangibleAssets(xnhk0201Val.getAmortizationIntangibleAssets());
                v.setDepreciationAmortization(xnhk0201Val.getDepreciationAmortization());
            }
            if (xnhk0204Val != null) {
                v.setProfitBeforeTaxation(xnhk0204Val.getProfitBeforeTaxation());
                v.setNetInterestIncome(xnhk0204Val.getNetInterestIncome());
                v.setInterestIncome(xnhk0204Val.getInterestIncome());
                v.setDpme(xnhk0204Val.getDpme());
                v.setAmortizationIntangibleAssets(xnhk0204Val.getAmortizationIntangibleAssets());


            }
            lists.add(v);
//            save(v, F10CashFlowEntity.class);
        });
        saveBulk(lists, F10CashFlowEntity.class);

    }


    /**
     * 现金流量表报告类型截止日期等字段
     *
     * @param code       股票代码
     * @param updateTime 更新时间
     * @return
     */
    private Map<String, F10CashFlowEntity> xnhk0210Sync(String code, Date updateTime) {
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
        Map<String, F10CashFlowEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10CashFlowEntity cashFlowEntity = new F10CashFlowEntity();
            cashFlowEntity.setStockCode(v.getSeccode());
            cashFlowEntity.setReportType(v.getF006v());
            cashFlowEntity.setCurrency(v.getF003v());
            cashFlowEntity.setReleaseDate(dateFormat(v.getF001d()));
            cashFlowEntity.setReleaseTimestamp(dateStrToLong(cashFlowEntity.getReleaseDate()));
            cashFlowEntity.setEndDate(dateFormat(v.getF002d()));
            cashFlowEntity.setEndTimestamp(dateStrToLong(cashFlowEntity.getEndDate()));
            cashFlowEntity.setStartDate(calStartDate(cashFlowEntity.getEndDate(), v.getF007n()));
            cashFlowEntity.setStartTimestamp(dateStrToLong(cashFlowEntity.getStartDate()));
            cashFlowEntity.setUpdateTime(v.getModifiedDate());
            Xnhk0210 yoyXnhk0210 = findYoyXnhk0210(v, xnhk0210s);
            calcCashFlowBy0210(cashFlowEntity, v, yoyXnhk0210);
            result.put(k, cashFlowEntity);
        });
        return result;
    }

    /**
     * @param code
     * @param updateTime
     * @return
     */
    private Map<String, F10CashFlowEntity> xnhk0207Sync(String code, Date updateTime) {
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
        Map<String, F10CashFlowEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10CashFlowEntity cashFlowEntity = new F10CashFlowEntity();
            Xnhk0207 yoy0207 = findYoyXnhk0207(v, xnhk0207s);
            calcCashFlowBy0207(cashFlowEntity, v, yoy0207);
            result.put(k, cashFlowEntity);
        });
        return result;
    }

    /**
     * @param code
     * @param updateTime
     * @return
     */
    private Map<String, F10CashFlowEntity> xnhk0201Sync(String code, Date updateTime) {
        List<Xnhk0201> xnhk0201s = xnhk0201Mapper.selectList(new QueryWrapper<Xnhk0201>().eq("seccode", code));

        List<Xnhk0201> xnhk0201Update = xnhk0201s.stream().filter(item -> {
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());

        }).collect(Collectors.toList());
        xnhk0201Update = convertAndFilterReports(xnhk0201Update, Xnhk0201::getF006v, Xnhk0201::setF006v, Xnhk0201::getF007d);
        Map<String, Xnhk0201> collect = xnhk0201Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
        Map<String, F10CashFlowEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10CashFlowEntity cashFlowEntity = new F10CashFlowEntity();
            Xnhk0201 yoy0201 = findYoyXnhk0201(v, xnhk0201s);

            calcCashFlowBy0201(cashFlowEntity, v, yoy0201);
            result.put(k, cashFlowEntity);
        });
        return result;
    }

    /**
     * 同步204数据
     *
     * @param code       股票代码
     * @param updateTime 时间
     * @return
     */
    private Map<String, F10CashFlowEntity> xnhk0204Sync(String code, Date updateTime) {
        List<Xnhk0204> xnhk0204s = xnhk0204Mapper.selectList(new QueryWrapper<Xnhk0204>().eq("seccode", code));

        List<Xnhk0204> xnhk0204Update = xnhk0204s.stream().filter(item -> {
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());

        }).collect(Collectors.toList());
        xnhk0204Update = convertAndFilterReports(xnhk0204Update, Xnhk0204::getF006v, Xnhk0204::setF006v, Xnhk0204::getF007d);
        Map<String, Xnhk0204> collect = xnhk0204Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
        Map<String, F10CashFlowEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            F10CashFlowEntity cashFlowEntity = new F10CashFlowEntity();
            Xnhk0204 yoy0204 = findYoyXnhk0204(v, xnhk0204s);
            calcCashFlowBy0204(cashFlowEntity, v, yoy0204);
            result.put(k, cashFlowEntity);
        });
        return result;
    }


    /**
     * 填充同比数据
     *
     * @param cashFlowEntity 现金流量表
     * @param xnhk0210       2010数据
     * @param yoy0210        上一周周期2010数据
     */
    private void calcCashFlowBy0210(
            F10CashFlowEntity cashFlowEntity,
            Xnhk0210 xnhk0210,
            Xnhk0210 yoy0210
    ) {
        cashFlowEntity.setExchangeRate(xnhk0210.getF004n());

        F10Val cashFlowFromeOperations = F10Val.builder().val(xnhk0210.getF008n()).build();
        cashFlowEntity.setCashFlowFromeOperations(cashFlowFromeOperations);

        F10Val cashFlowFromOperations = F10Val.builder().val(xnhk0210.getF016n()).build();
        cashFlowEntity.setCashFlowFromOperations(cashFlowFromOperations);

        F10Val receivedDividend = F10Val.builder().val(xnhk0210.getF019n()).build();
        cashFlowEntity.setReceivedDividend(receivedDividend);

        F10Val paidTaxes = F10Val.builder().val(xnhk0210.getF023n()).build();
        cashFlowEntity.setPaidTaxes(paidTaxes);

        F10Val receivedInterest = F10Val.builder().val(xnhk0210.getF017n()).build();
        cashFlowEntity.setReceivedInterest(receivedInterest);

        F10Val cashFlowFromInvestmentActivities = F10Val.builder().val(xnhk0210.getF009n()).build();
        cashFlowEntity.setCashFlowFromInvestmentActivities(cashFlowFromInvestmentActivities);

        F10Val additionFixdedAssets = F10Val.builder().val(xnhk0210.getF024n()).build();
        cashFlowEntity.setAdditionFixdedAssets(additionFixdedAssets);

        F10Val increasedInvestment = F10Val.builder().val(xnhk0210.getF025n()).build();
        cashFlowEntity.setIncreasedInvestment(increasedInvestment);

        F10Val saleOfPlantAssets = F10Val.builder().val(xnhk0210.getF026n()).build();
        cashFlowEntity.setSaleOfPlantAssets(saleOfPlantAssets);

        F10Val lowerInvestment = F10Val.builder().val(xnhk0210.getF027n()).build();
        cashFlowEntity.setLowerInvestment(lowerInvestment);

        F10Val cashFlowAssociate = F10Val.builder().val(xnhk0210.getF028n()).build();
        cashFlowEntity.setCashFlowAssociate(cashFlowAssociate);

        F10Val othersInvestment = F10Val.builder().val(xnhk0210.getF029n()).build();
        cashFlowEntity.setOthersInvestment(othersInvestment);

        F10Val cashFlowFromFinancingActivites = F10Val.builder().val(xnhk0210.getF010n()).build();
        cashFlowEntity.setCashFlowFromFinancingActivites(cashFlowFromFinancingActivites);

        F10Val newBankLoans = F10Val.builder().val(xnhk0210.getF030n()).build();
        cashFlowEntity.setNewBankLoans(newBankLoans);

        F10Val repayTheLoan = F10Val.builder().val(xnhk0210.getF031n()).build();
        cashFlowEntity.setRepayTheLoan(repayTheLoan);

        F10Val interestPaid = F10Val.builder().val(xnhk0210.getF018n()).build();
        cashFlowEntity.setInterestPaid(interestPaid);

        F10Val hasSentDividend = F10Val.builder().val(xnhk0210.getF020n()).build();
        cashFlowEntity.setHasSentDividend(hasSentDividend);

        F10Val othersFinancing = F10Val.builder().val(xnhk0210.getF036n()).build();
        cashFlowEntity.setOthersFinancing(othersFinancing);

        F10Val exchangeRateInfluence = F10Val.builder().val(xnhk0210.getF013n()).build();
        cashFlowEntity.setExchangeRateInfluence(exchangeRateInfluence);

        F10Val netCash = F10Val.builder().val(xnhk0210.getF011n()).build();
        cashFlowEntity.setNetCash(netCash);

        F10Val initialCash = F10Val.builder().val(xnhk0210.getF012n()).build();
        cashFlowEntity.setInitialCash(initialCash);
        F10Val finalCash = F10Val.builder().val(xnhk0210.getF014n()).build();
        cashFlowEntity.setFinalCash(finalCash);
        //审核意见

        cashFlowEntity.setAuditOpinion(xnhk0210.getF015v());
        if (null != yoy0210) {
            BigDecimal yoy008n = calYoy(xnhk0210.getF008n(), yoy0210.getF008n());
            BigDecimal yoy016n = calYoy(xnhk0210.getF016n(), yoy0210.getF016n());
            BigDecimal yoy017n = calYoy(xnhk0210.getF017n(), yoy0210.getF017n());
            BigDecimal yoy019n = calYoy(xnhk0210.getF019n(), yoy0210.getF019n());
            BigDecimal yoy023n = calYoy(xnhk0210.getF023n(), yoy0210.getF023n());
            BigDecimal yoy009n = calYoy(xnhk0210.getF009n(), yoy0210.getF009n());
            BigDecimal yoy024n = calYoy(xnhk0210.getF024n(), yoy0210.getF024n());
            BigDecimal yoy025n = calYoy(xnhk0210.getF025n(), yoy0210.getF025n());
            BigDecimal yoy026n = calYoy(xnhk0210.getF026n(), yoy0210.getF026n());
            BigDecimal yoy027n = calYoy(xnhk0210.getF027n(), yoy0210.getF027n());
            BigDecimal yoy028n = calYoy(xnhk0210.getF028n(), yoy0210.getF028n());
            BigDecimal yoy029n = calYoy(xnhk0210.getF029n(), yoy0210.getF029n());
            BigDecimal yoy010n = calYoy(xnhk0210.getF010n(), yoy0210.getF010n());
            BigDecimal yoy030n = calYoy(xnhk0210.getF030n(), yoy0210.getF030n());
            BigDecimal yoy031n = calYoy(xnhk0210.getF031n(), yoy0210.getF031n());
            BigDecimal yoy018n = calYoy(xnhk0210.getF018n(), yoy0210.getF018n());
            BigDecimal yoy020n = calYoy(xnhk0210.getF020n(), yoy0210.getF020n());
            BigDecimal yoy036n = calYoy(xnhk0210.getF036n(), yoy0210.getF036n());
            BigDecimal yoy013n = calYoy(xnhk0210.getF013n(), yoy0210.getF013n());
            BigDecimal yoy011n = calYoy(xnhk0210.getF011n(), yoy0210.getF011n());
            BigDecimal yoy012n = calYoy(xnhk0210.getF012n(), yoy0210.getF012n());
            BigDecimal yoy014n = calYoy(xnhk0210.getF014n(), yoy0210.getF014n());
            cashFlowFromeOperations.setYoy(yoy008n);
            cashFlowFromOperations.setYoy(yoy016n);
            receivedDividend.setYoy(yoy019n);
            paidTaxes.setYoy(yoy023n);
            receivedInterest.setYoy(yoy017n);
            cashFlowFromInvestmentActivities.setYoy(yoy009n);
            additionFixdedAssets.setYoy(yoy024n);
            increasedInvestment.setYoy(yoy025n);
            saleOfPlantAssets.setYoy(yoy026n);
            lowerInvestment.setYoy(yoy027n);
            cashFlowAssociate.setYoy(yoy028n);
            othersInvestment.setYoy(yoy029n);
            cashFlowFromFinancingActivites.setYoy(yoy010n);
            newBankLoans.setYoy(yoy030n);
            repayTheLoan.setYoy(yoy031n);
            interestPaid.setYoy(yoy018n);
            hasSentDividend.setYoy(yoy020n);
            othersFinancing.setYoy(yoy036n);
            exchangeRateInfluence.setYoy(yoy013n);
            netCash.setYoy(yoy011n);
            initialCash.setYoy(yoy012n);
            finalCash.setYoy(yoy014n);

        }
    }

    /**
     * 填充同比数据
     *
     * @param cashFlowEntity 现金流量
     * @param xnhk0207       207
     * @param yoy0207        207
     */
    private void calcCashFlowBy0207(
            F10CashFlowEntity cashFlowEntity,
            Xnhk0207 xnhk0207,
            Xnhk0207 yoy0207
    ) {
        F10Val profitBeforeTaxation = F10Val.builder().val(xnhk0207.getF029n()).build();
        if (null != yoy0207) {
            BigDecimal yoy029n = calYoy(xnhk0207.getF029n(), yoy0207.getF029n());
            profitBeforeTaxation.setYoy(yoy029n);
        }
        cashFlowEntity.setProfitBeforeTaxation(profitBeforeTaxation);
    }

    /**
     * 填充同比数据
     *
     * @param cashFlowEntity 现金流量
     * @param xnhk0201       201
     * @param yoy0201        201
     */
    private void calcCashFlowBy0201(
            F10CashFlowEntity cashFlowEntity,
            Xnhk0201 xnhk0201,
            Xnhk0201 yoy0201
    ) {
        //除税前溢利
        F10Val profitBeforeTaxation = F10Val.builder().val(xnhk0201.getF027n()).build();
        cashFlowEntity.setProfitBeforeTaxation(profitBeforeTaxation);
        //物业、机器及设备折旧
        F10Val dpme = F10Val.builder().val(xnhk0201.getF047n()).build();
        cashFlowEntity.setDpme(dpme);
        //无形资产摊销
        F10Val amortizationIntangibleAssets = F10Val.builder().val(xnhk0201.getF048n()).build();
        cashFlowEntity.setAmortizationIntangibleAssets(amortizationIntangibleAssets);
        //折旧及摊销
        F10Val depreciationAmortization = F10Val.builder().val(xnhk0201.getF049n()).build();
        cashFlowEntity.setDepreciationAmortization(depreciationAmortization);
        if (null != yoy0201) {
            BigDecimal yoy027n = calYoy(xnhk0201.getF027n(), yoy0201.getF027n());
            BigDecimal yoy047n = calYoy(xnhk0201.getF047n(), yoy0201.getF047n());
            BigDecimal yoy048n = calYoy(xnhk0201.getF048n(), yoy0201.getF048n());
            BigDecimal yoy049n = calYoy(xnhk0201.getF049n(), yoy0201.getF049n());
            profitBeforeTaxation.setYoy(yoy027n);
            dpme.setYoy(yoy047n);
            amortizationIntangibleAssets.setYoy(yoy048n);
            depreciationAmortization.setYoy(yoy049n);
        }
    }

    private void calcCashFlowBy0204(
            F10CashFlowEntity cashFlowEntity,
            Xnhk0204 xnhk0204,
            Xnhk0204 yoy0204
    ) {
        F10Val profitBeforeTaxation = F10Val.builder().val(xnhk0204.getF030n()).build();
        cashFlowEntity.setProfitBeforeTaxation(profitBeforeTaxation);
        //利息净收入
        F10Val netInterestIncome = F10Val.builder().val(xnhk0204.getF010n()).build();
        cashFlowEntity.setNetInterestIncome(netInterestIncome);
        //利息收入
        F10Val interestIncome = F10Val.builder().val(xnhk0204.getF008n()).build();
        cashFlowEntity.setInterestIncome(interestIncome);
        //物业、机器及设备折旧
        F10Val dpme = F10Val.builder().val(xnhk0204.getF023n()).build();
        cashFlowEntity.setDpme(dpme);
        //无形资产摊销
        F10Val amortizationIntangibleAssets = F10Val.builder().val(xnhk0204.getF024n()).build();
        cashFlowEntity.setAmortizationIntangibleAssets(amortizationIntangibleAssets);
        if (null != yoy0204) {
            BigDecimal yoy030n = calYoy(xnhk0204.getF030n(), yoy0204.getF030n());
            BigDecimal yoy010n = calYoy(xnhk0204.getF010n(), yoy0204.getF010n());
            BigDecimal yoy008n = calYoy(xnhk0204.getF008n(), yoy0204.getF008n());
            BigDecimal yoy023n = calYoy(xnhk0204.getF023n(), yoy0204.getF023n());
            BigDecimal yoy024n = calYoy(xnhk0204.getF024n(), yoy0204.getF024n());
            profitBeforeTaxation.setYoy(yoy030n);
            netInterestIncome.setYoy(yoy010n);
            interestIncome.setYoy(yoy008n);
            dpme.setYoy(yoy023n);
            amortizationIntangibleAssets.setYoy(yoy024n);
        }
    }


    /**
     * 获取0210同比数据
     *
     * @param xnhk0210  当前数据
     * @param xnhk0210s 所有数据
     * @return
     */
    private Xnhk0210 findYoyXnhk0210(Xnhk0210 xnhk0210, List<Xnhk0210> xnhk0210s) {
        return xnhk0210s.stream().filter(item->item.getF006v().equals(xnhk0210.getF006v())&&item.getF002d()<xnhk0210.getF002d()).max(Comparator.comparing(Xnhk0210::getF002d)).orElse(null);

    }

    /**
     * 获取0207同比数据
     *
     * @param xnhk0204  当前数据
     * @param xnhk0204s 所有数据
     * @return
     */
    private Xnhk0204 findYoyXnhk0204(Xnhk0204 xnhk0204, List<Xnhk0204> xnhk0204s) {
        return xnhk0204s.stream().filter(item->item.getF006v().equals(xnhk0204.getF006v())&&item.getF002d()<xnhk0204.getF002d()).max(Comparator.comparing(Xnhk0204::getF002d)).orElse(null);


    }

    /**
     * 获取0207同比数据
     *
     * @param xnhk0201  当前数据
     * @param xnhk0201s 所有数据
     * @return
     */
    private Xnhk0201 findYoyXnhk0201(Xnhk0201 xnhk0201, List<Xnhk0201> xnhk0201s) {
        return xnhk0201s.stream().filter(item->item.getF006v().equals(xnhk0201.getF006v())&&item.getF002d()<xnhk0201.getF002d()).max(Comparator.comparing(Xnhk0201::getF002d)).orElse(null);

    }

    /**
     * 获取0207同比数据
     *
     * @param xnhk0207  当前数据
     * @param xnhk0207s 所有数据
     * @return
     */
    private Xnhk0207 findYoyXnhk0207(Xnhk0207 xnhk0207, List<Xnhk0207> xnhk0207s) {
        return xnhk0207s.stream().filter(item->item.getF006v().equals(xnhk0207.getF006v())&&item.getF002d()<xnhk0207.getF002d()).max(Comparator.comparing(Xnhk0207::getF002d)).orElse(null);

    }


}
