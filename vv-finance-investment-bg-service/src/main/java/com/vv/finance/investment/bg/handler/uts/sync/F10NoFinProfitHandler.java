package com.vv.finance.investment.bg.handler.uts.sync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.f10.NoFinOperatingAndOtherRevenue;
import com.vv.finance.investment.bg.entity.f10.NoFinOperatingCostsAndExpenses;
import com.vv.finance.investment.bg.entity.f10.NoFinOperatingProfit;
import com.vv.finance.investment.bg.entity.uts.Xnhk0201;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0201Mapper;
import com.vv.finance.investment.bg.mongo.model.F10NoFinProfitEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/7/22 16:47
 * 财务分析--利润表（非金融）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class F10NoFinProfitHandler extends AbstractF10CommonHandler {
    private final Xnhk0201Mapper xnhk0201Mapper;


    @Override
    public void sync(){
        List<Xnhk0201> selectList = xnhk0201Mapper.selectList(new QueryWrapper<Xnhk0201>().select("SECCODE", "max(Modified_Date) Modified_Date").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(),item.getModifiedDate(), F10NoFinProfitEntity.class));
    }

    @Override
    public void syncAll() {
        List<Xnhk0201> selectList = xnhk0201Mapper.selectList(new QueryWrapper<Xnhk0201>().select("SECCODE").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(), null, F10NoFinProfitEntity.class));
    }

    @Override
    public void doSync(String code, Date updateTime) {
        log.info("执行  F10NoFinProfitHandler {}",code);
        List<Xnhk0201> xnhk0201s = xnhk0201Mapper.selectList(new QueryWrapper<Xnhk0201>().eq("seccode", code));
        List<Xnhk0201> xnhk0203Update = xnhk0201s.stream().filter(item -> {
            // 以前未同步到MongoDB,所有数据需要同步
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            // MongoDB更新的时间小于uts数据库数据记录修改日期,则需要同步满足这条件的数据
            return updateTime.before(item.getModifiedDate());

        }).collect(Collectors.toList());
        xnhk0203Update = convertAndFilterReports(xnhk0203Update, Xnhk0201::getF006v, Xnhk0201::setF006v, Xnhk0201::getF007d);
        List<F10NoFinProfitEntity> lists = new ArrayList<>();
        xnhk0203Update.forEach(xnhk0201 -> {
            F10NoFinProfitEntity f10NoFinProfitEntity = new F10NoFinProfitEntity();
            f10NoFinProfitEntity.setStockCode(xnhk0201.getSeccode());
            f10NoFinProfitEntity.setUpdateTime(xnhk0201.getModifiedDate());
            f10NoFinProfitEntity.setReportType(xnhk0201.getF006v());
            f10NoFinProfitEntity.setCurrency(xnhk0201.getF003v());
            f10NoFinProfitEntity.setExchangeRate(xnhk0201.getF004n());
            f10NoFinProfitEntity.setEndDate(dateFormat(xnhk0201.getF002d()));
            f10NoFinProfitEntity.setEndTimestamp(dateStrToLong(f10NoFinProfitEntity.getEndDate()));
            f10NoFinProfitEntity.setStartDate(calStartDate(f10NoFinProfitEntity.getEndDate(), xnhk0201.getF007d()));
            f10NoFinProfitEntity.setStartTimestamp(dateStrToLong(f10NoFinProfitEntity.getStartDate()));
            f10NoFinProfitEntity.setReleaseDate(dateFormat(xnhk0201.getF001d()));
            f10NoFinProfitEntity.setReleaseTimestamp(dateStrToLong(f10NoFinProfitEntity.getReleaseDate()));
            Xnhk0201 yoyXnhk0201 = findYoyXnhk0201(xnhk0201, xnhk0201s);
            operatingAndOtherRevenue(f10NoFinProfitEntity, xnhk0201, yoyXnhk0201);
            operatingCostsAndExpenses(f10NoFinProfitEntity, xnhk0201, yoyXnhk0201);
            operatingProfit(f10NoFinProfitEntity, xnhk0201, yoyXnhk0201);
            otherCal(f10NoFinProfitEntity, xnhk0201, yoyXnhk0201);

            lists.add(f10NoFinProfitEntity);
//            save(f10NoFinProfitEntity,F10NoFinProfitEntity.class);

        });
        saveBulk(lists, F10NoFinProfitEntity.class);

    }

    private void otherCal(F10NoFinProfitEntity f10NoFinProfitEntity, Xnhk0201 xnhk0201, Xnhk0201 yoy) {

        F10Val profitBeforeTaxation = F10Val.builder().val(xnhk0201.getF027n()).build();
        f10NoFinProfitEntity.setProfitBeforeTaxation(profitBeforeTaxation);
        F10Val tax = F10Val.builder().val(xnhk0201.getF028n()).build();
        f10NoFinProfitEntity.setTax(tax);
        F10Val profitAndLossDuringThePeriod = F10Val.builder().val(xnhk0201.getF030n()).build();
        f10NoFinProfitEntity.setProfitAndLossDuringThePeriod(profitAndLossDuringThePeriod);
        F10Val holdersOfShareCapitalOfTheCompany = F10Val.builder().val(xnhk0201.getF031n()).build();
        f10NoFinProfitEntity.setHoldersOfShareCapitalOfTheCompany(holdersOfShareCapitalOfTheCompany);
        F10Val commonStockholder = F10Val.builder().val(xnhk0201.getF035n()).build();
        f10NoFinProfitEntity.setCommonStockholder(commonStockholder);
        F10Val preferredStockholder = F10Val.builder().val(xnhk0201.getF033n()).build();
        f10NoFinProfitEntity.setPreferredStockholder(preferredStockholder);
        F10Val nonControllingInterests = F10Val.builder().val(xnhk0201.getF032n()).build();
        f10NoFinProfitEntity.setNonControllingInterests(nonControllingInterests);
        F10Val otherEquityHolders = F10Val.builder().val(xnhk0201.getF034n()).build();
        f10NoFinProfitEntity.setOtherEquityHolders(otherEquityHolders);
        F10Val tedaftcob = F10Val.builder().val(xnhk0201.getF036n()).build();
        f10NoFinProfitEntity.setTedaftcob(tedaftcob);
        F10Val toapldtp = F10Val.builder().val(xnhk0201.getF037n()).build();
        f10NoFinProfitEntity.setToapldtp(toapldtp);
        F10Val dilutedEarningsPerShare = F10Val.builder().val(xnhk0201.getF039n()).build();
        f10NoFinProfitEntity.setDilutedEarningsPerShare(dilutedEarningsPerShare);
        F10Val basicEarningsPerShare = F10Val.builder().val(xnhk0201.getF038n()).build();
        f10NoFinProfitEntity.setBasicEarningsPerShare(basicEarningsPerShare);
        String auditOpinion = xnhk0201.getF070v();
        f10NoFinProfitEntity.setAuditOpinion(auditOpinion);
        F10Val auditFee = F10Val.builder().val(xnhk0201.getF054n()).build();
        f10NoFinProfitEntity.setAuditFee(auditFee);
        if (yoy != null) {
            profitBeforeTaxation.setYoy(calYoy(xnhk0201.getF027n(), yoy.getF027n()));
            tax.setYoy(calYoy(xnhk0201.getF028n(), yoy.getF028n()));
            profitAndLossDuringThePeriod.setYoy(calYoy(xnhk0201.getF030n(), yoy.getF030n()));

            holdersOfShareCapitalOfTheCompany.setYoy(calYoy(xnhk0201.getF031n(), yoy.getF031n()));

            commonStockholder.setYoy(calYoy(xnhk0201.getF035n(), yoy.getF035n()));

            preferredStockholder.setYoy(calYoy(xnhk0201.getF033n(), yoy.getF033n()));

            nonControllingInterests.setYoy(calYoy(xnhk0201.getF032n(), yoy.getF032n()));

            otherEquityHolders.setYoy(calYoy(xnhk0201.getF034n(), yoy.getF034n()));

            tedaftcob.setYoy(calYoy(xnhk0201.getF036n(), yoy.getF036n()));

            toapldtp.setYoy(calYoy(xnhk0201.getF037n(), yoy.getF037n()));

            dilutedEarningsPerShare.setYoy(calYoy(xnhk0201.getF039n(), yoy.getF039n()));

            basicEarningsPerShare.setYoy(calYoy(xnhk0201.getF038n(), yoy.getF038n()));

            auditFee.setYoy(calYoy(xnhk0201.getF054n(), yoy.getF054n()));

        }


    }

    private void operatingProfit(F10NoFinProfitEntity f10NoFinProfitEntity, Xnhk0201 xnhk0201, Xnhk0201 yoy) {
        NoFinOperatingProfit operatingProfit = new NoFinOperatingProfit();
        operatingProfit.setVal(xnhk0201.getF024n());

        /**
         * 融资成本
         */
        F10Val financingCost = F10Val.builder().val(xnhk0201.getF025n()).build();
        /**
         * 共同控制及联营公司溢利
         */
        F10Val profitFromJointControlAndAssociatedCompanies = F10Val.builder().val(xnhk0201.getF026n()).build();

        if (yoy != null) {
            operatingProfit.setYoy(calYoy(xnhk0201.getF024n(), yoy.getF024n()));
            financingCost.setYoy(calYoy(xnhk0201.getF025n(), yoy.getF025n()));
            profitFromJointControlAndAssociatedCompanies.setYoy(calYoy(xnhk0201.getF026n(), yoy.getF026n()));

        }
        operatingProfit.setFinancingCost(financingCost);
        operatingProfit.setProfitFromJointControlAndAssociatedCompanies(profitFromJointControlAndAssociatedCompanies);
        f10NoFinProfitEntity.setOperatingProfit(operatingProfit);

    }

    /**
     * 营业成本及支出
     *
     * @param f10NoFinProfitEntity
     * @param xnhk0201
     * @param yoy
     */
    private void operatingCostsAndExpenses(F10NoFinProfitEntity f10NoFinProfitEntity, Xnhk0201 xnhk0201, Xnhk0201 yoy) {
        NoFinOperatingCostsAndExpenses operatingCostsAndExpenses = new NoFinOperatingCostsAndExpenses();
        operatingCostsAndExpenses.setVal(xnhk0201.getF018n());

        F10Val grossProfit = F10Val.builder().val(xnhk0201.getF013n()).build();

        F10Val sellingCost = F10Val.builder().val(xnhk0201.getF012n()).build();

        F10Val sellingExpense = F10Val.builder().val(xnhk0201.getF014n()).build();

        F10Val administrationExpense = F10Val.builder().val(xnhk0201.getF015n()).build();

        F10Val rdExpense = F10Val.builder().val(xnhk0201.getF016n()).build();

        F10Val otherOperatingExpenditure = F10Val.builder().val(xnhk0201.getF017n()).build();

        F10Val totalOperatingExpenses = F10Val.builder().val(xnhk0201.getF018n()).build();
        if (yoy != null) {
            operatingCostsAndExpenses.setYoy(calYoy(xnhk0201.getF018n(), yoy.getF018n()));
            grossProfit.setYoy(calYoy(xnhk0201.getF013n(), yoy.getF013n()));
            sellingCost.setYoy(calYoy(xnhk0201.getF012n(), yoy.getF012n()));
            sellingExpense.setYoy(calYoy(xnhk0201.getF014n(), yoy.getF014n()));
            administrationExpense.setYoy(calYoy(xnhk0201.getF015n(), yoy.getF015n()));
            rdExpense.setYoy(calYoy(xnhk0201.getF016n(), yoy.getF016n()));
            otherOperatingExpenditure.setYoy(calYoy(xnhk0201.getF017n(), yoy.getF017n()));
            totalOperatingExpenses.setYoy(calYoy(xnhk0201.getF018n(), yoy.getF018n()));
        }
        operatingCostsAndExpenses.setGrossProfit(grossProfit);
        operatingCostsAndExpenses.setSellingCost(sellingCost);
        operatingCostsAndExpenses.setSellingExpense(sellingExpense);
        operatingCostsAndExpenses.setAdministrationExpense(administrationExpense);
        operatingCostsAndExpenses.setRdExpense(rdExpense);
        operatingCostsAndExpenses.setOtherOperatingExpenditure(otherOperatingExpenditure);
        operatingCostsAndExpenses.setTotalOperatingExpenses(totalOperatingExpenses);
        f10NoFinProfitEntity.setOperatingCostsAndExpenses(operatingCostsAndExpenses);

    }

    private void operatingAndOtherRevenue(F10NoFinProfitEntity f10NoFinProfitEntity, Xnhk0201 xnhk0201, Xnhk0201 yoy) {
        NoFinOperatingAndOtherRevenue noFinOperatingAndOtherRevenue = new NoFinOperatingAndOtherRevenue();
        noFinOperatingAndOtherRevenue.setVal(xnhk0201.getF010n());
        F10Val primeOperatingRevenue = F10Val.builder().val(xnhk0201.getF008n()).build();
        /**
         * 其他业务收入
         */
        F10Val otherOperatingRevenue = F10Val.builder().val(xnhk0201.getF009n()).build();

        if (yoy != null) {
            noFinOperatingAndOtherRevenue.setYoy(calYoy(xnhk0201.getF010n(), yoy.getF010n()));
            primeOperatingRevenue.setYoy(calYoy(xnhk0201.getF008n(), yoy.getF008n()));
            otherOperatingRevenue.setYoy(calYoy(xnhk0201.getF009n(), yoy.getF009n()));
        }
        noFinOperatingAndOtherRevenue.setOtherOperatingRevenue(otherOperatingRevenue);
        noFinOperatingAndOtherRevenue.setPrimeOperatingRevenue(primeOperatingRevenue);
        f10NoFinProfitEntity.setOperatingAndOtherRevenue(noFinOperatingAndOtherRevenue);
    }

    private Xnhk0201 findYoyXnhk0201(Xnhk0201 xnhk0201, List<Xnhk0201> xnhk0201s) {
        if (xnhk0201 == null) {
            return null;
        }
        return xnhk0201s.stream().filter(item->item.getF006v().equals(xnhk0201.getF006v())&&item.getF002d()<xnhk0201.getF002d()).max(Comparator.comparing(Xnhk0201::getF002d)).orElse(null);

    }
}
