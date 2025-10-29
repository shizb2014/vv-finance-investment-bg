package com.vv.finance.investment.bg.handler.uts.sync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.uts.Xnhk0204;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0204Mapper;
import com.vv.finance.investment.bg.mongo.model.F10FinProfitEntity;
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
 * 财务分析--利润表（金融）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class F10FinProfitHandler extends AbstractF10CommonHandler {
    private final Xnhk0204Mapper xnhk0204Mapper;


    @Override
    public void sync() {
        List<Xnhk0204> selectList = xnhk0204Mapper.selectList(new QueryWrapper<Xnhk0204>().select("SECCODE", "max(Modified_Date) Modified_Date").groupBy("seccode"));
        selectList.forEach(item -> {
            syncCheck(item.getSeccode(),item.getModifiedDate(), F10FinProfitEntity.class);
        });
    }

    @Override
    public void syncAll() {
        List<Xnhk0204> selectList = xnhk0204Mapper.selectList(new QueryWrapper<Xnhk0204>().select("SECCODE").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(), null, F10FinProfitEntity.class));
    }

    @Override
    public void doSync(String code, Date updateTime) {
        log.info("执行  F10FinProfitHandler {}",code);
        List<Xnhk0204> xnhk0204s = xnhk0204Mapper.selectList(new QueryWrapper<Xnhk0204>().eq("seccode", code));
        List<Xnhk0204> xnhk0203Update = xnhk0204s.stream().filter(item -> {
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());

        }).collect(Collectors.toList());
        xnhk0203Update = convertAndFilterReports(xnhk0203Update, Xnhk0204::getF006v, Xnhk0204::setF006v, Xnhk0204::getF007d);
        List<F10FinProfitEntity> lists = new ArrayList<>();
        xnhk0203Update.forEach(xnhk0204 -> {
            F10FinProfitEntity f10FinProfitEntity = new F10FinProfitEntity();
            f10FinProfitEntity.setStockCode(xnhk0204.getSeccode());
            f10FinProfitEntity.setUpdateTime(xnhk0204.getModifiedDate());
            f10FinProfitEntity.setReportType(xnhk0204.getF006v());
            f10FinProfitEntity.setCurrency(xnhk0204.getF003v());
            f10FinProfitEntity.setEndDate(dateFormat(xnhk0204.getF002d()));
            f10FinProfitEntity.setEndTimestamp(dateStrToLong(f10FinProfitEntity.getEndDate()));
            f10FinProfitEntity.setStartDate(calStartDate(f10FinProfitEntity.getEndDate(), xnhk0204.getF007d()));
            f10FinProfitEntity.setStartTimestamp(dateStrToLong(f10FinProfitEntity.getStartDate()));
            f10FinProfitEntity.setReleaseDate(dateFormat(xnhk0204.getF001d()));
            f10FinProfitEntity.setReleaseTimestamp(dateStrToLong(f10FinProfitEntity.getReleaseDate()));
            f10FinProfitEntity.setExchangeRate(xnhk0204.getF004n());
            Xnhk0204 yoyXnhk0204 = findYoyXnhk0204(xnhk0204, xnhk0204s);
            otherCal(f10FinProfitEntity, xnhk0204, yoyXnhk0204);

            lists.add(f10FinProfitEntity);
//            save(f10FinProfitEntity, F10FinProfitEntity.class);
        });
        saveBulk(lists, F10FinProfitEntity.class);

    }

    private void otherCal(F10FinProfitEntity f10FinProfitEntity, Xnhk0204 xnhk0204, Xnhk0204 yoy) {

        F10Val grossRevenue = F10Val.builder().val(xnhk0204.getF017n()).build();
        f10FinProfitEntity.setGrossRevenue(grossRevenue);
        F10Val netInsuranceClaim = F10Val.builder().val(xnhk0204.getF018n()).build();
        f10FinProfitEntity.setNetInsuranceClaim(netInsuranceClaim);
        F10Val netInterestIncome = F10Val.builder().val(xnhk0204.getF010n()).build();
        f10FinProfitEntity.setNetInterestIncome(netInterestIncome);
        F10Val interestIncome = F10Val.builder().val(xnhk0204.getF008n()).build();
        f10FinProfitEntity.setInterestIncome(interestIncome);
        F10Val interestExpense = F10Val.builder().val(xnhk0204.getF009n()).build();
        f10FinProfitEntity.setInterestExpense(interestExpense);
        F10Val netExpenseIncome = F10Val.builder().val(xnhk0204.getF013n()).build();
        f10FinProfitEntity.setNetExpenseIncome(netExpenseIncome);
        F10Val feeIncome = F10Val.builder().val(xnhk0204.getF011n()).build();
        f10FinProfitEntity.setFeeIncome(feeIncome);
        F10Val feeExpense = F10Val.builder().val(xnhk0204.getF012n()).build();
        f10FinProfitEntity.setFeeExpense(feeExpense);
        F10Val netTransactionRevenue = F10Val.builder().val(xnhk0204.getF014n()).build();
        f10FinProfitEntity.setNetTransactionRevenue(netTransactionRevenue);
        F10Val netInsuranceIncome = F10Val.builder().val(xnhk0204.getF015n()).build();
        f10FinProfitEntity.setNetInsuranceIncome(netInsuranceIncome);
        F10Val otherOperatingRevenue = F10Val.builder().val(xnhk0204.getF016n()).build();
        f10FinProfitEntity.setOtherOperatingRevenue(otherOperatingRevenue);
        F10Val operatingIncome = F10Val.builder().val(xnhk0204.getF019n()).build();
        f10FinProfitEntity.setOperatingIncome(operatingIncome);

        F10Val loanLossImpairment = F10Val.builder().val(xnhk0204.getF020n()).build();
        f10FinProfitEntity.setLoanLossImpairment(loanLossImpairment);

        F10Val netOperatingIncome = F10Val.builder().val(xnhk0204.getF021n()).build();
        f10FinProfitEntity.setNetOperatingIncome(netOperatingIncome);

        F10Val totalOperatingExpenses = F10Val.builder().val(xnhk0204.getF026n()).build();
        f10FinProfitEntity.setTotalOperatingExpenses(totalOperatingExpenses);

        F10Val employeeCompensationBenefits = F10Val.builder().val(xnhk0204.getF022n()).build();
        f10FinProfitEntity.setEmployeeCompensationBenefits(employeeCompensationBenefits);
        F10Val depreciationPropertyMachineryEquipment = F10Val.builder().val(xnhk0204.getF023n()).build();
        f10FinProfitEntity.setDepreciationPropertyMachineryEquipment(depreciationPropertyMachineryEquipment);
        F10Val amortizationIntangibleAssets = F10Val.builder().val(xnhk0204.getF024n()).build();
        f10FinProfitEntity.setAmortizationIntangibleAssets(amortizationIntangibleAssets);
        F10Val otherOperatingExpenditure = F10Val.builder().val(xnhk0204.getF025n()).build();
        f10FinProfitEntity.setOtherOperatingExpenditure(otherOperatingExpenditure);
        F10Val operatingProfit = F10Val.builder().val(xnhk0204.getF027n()).build();
        f10FinProfitEntity.setOperatingProfit(operatingProfit);
        F10Val pfjcac = F10Val.builder().val(xnhk0204.getF029n()).build();
        f10FinProfitEntity.setPfjcac(pfjcac);
        F10Val otherNonBusinessItems = F10Val.builder().val(xnhk0204.getF028n()).build();
        f10FinProfitEntity.setOtherNonBusinessItems(otherNonBusinessItems);
        F10Val profitBeforeTaxation = F10Val.builder().val(xnhk0204.getF030n()).build();
        f10FinProfitEntity.setProfitBeforeTaxation(profitBeforeTaxation);
        F10Val tax = F10Val.builder().val(xnhk0204.getF031n()).build();
        f10FinProfitEntity.setTax(tax);

        F10Val profitLossDuringPeriod = F10Val.builder().val(xnhk0204.getF032n()).build();
        f10FinProfitEntity.setProfitLossDuringPeriod(profitLossDuringPeriod);
        F10Val holdersShareCapitalCompany = F10Val.builder().val(xnhk0204.getF033n()).build();
        f10FinProfitEntity.setHoldersShareCapitalCompany(holdersShareCapitalCompany);
        F10Val commonStockholder = F10Val.builder().val(xnhk0204.getF037n()).build();
        f10FinProfitEntity.setCommonStockholder(commonStockholder);
        F10Val preferredStockholder = F10Val.builder().val(xnhk0204.getF035n()).build();
        f10FinProfitEntity.setPreferredStockholder(preferredStockholder);
        F10Val nonControllingInterests = F10Val.builder().val(xnhk0204.getF034n()).build();
        f10FinProfitEntity.setNonControllingInterests(nonControllingInterests);
        F10Val otherEquityHolders = F10Val.builder().val(xnhk0204.getF036n()).build();
        f10FinProfitEntity.setOtherEquityHolders(otherEquityHolders);
        F10Val dilutedEarningsPerShare = F10Val.builder().val(xnhk0204.getF041n()).build();
        f10FinProfitEntity.setDilutedEarningsPerShare(dilutedEarningsPerShare);
        F10Val basicEarningsPerShare = F10Val.builder().val(xnhk0204.getF040n()).build();
        f10FinProfitEntity.setBasicEarningsPerShare(basicEarningsPerShare);
        f10FinProfitEntity.setAuditOpinion(xnhk0204.getF066v());

        F10Val auditFee = F10Val.builder().val(xnhk0204.getF051n()).build();
        f10FinProfitEntity.setAuditFee(auditFee);
        if (yoy != null) {
            grossRevenue.setYoy(calYoy(xnhk0204.getF017n(), yoy.getF017n()));

            netInsuranceClaim.setYoy(calYoy(xnhk0204.getF018n(), yoy.getF018n()));

            netInterestIncome.setYoy(calYoy(xnhk0204.getF010n(), yoy.getF010n()));

            interestIncome.setYoy(calYoy(xnhk0204.getF008n(), yoy.getF008n()));

            interestExpense.setYoy(calYoy(xnhk0204.getF009n(), yoy.getF009n()));

            netExpenseIncome.setYoy(calYoy(xnhk0204.getF013n(), yoy.getF013n()));

            feeIncome.setYoy(calYoy(xnhk0204.getF011n(), yoy.getF011n()));

            feeExpense.setYoy(calYoy(xnhk0204.getF012n(), yoy.getF012n()));

            netTransactionRevenue.setYoy(calYoy(xnhk0204.getF014n(), yoy.getF014n()));

            netInsuranceIncome.setYoy(calYoy(xnhk0204.getF015n(), yoy.getF015n()));

            otherOperatingRevenue.setYoy(calYoy(xnhk0204.getF016n(), yoy.getF016n()));

            operatingIncome.setYoy(calYoy(xnhk0204.getF019n(), yoy.getF019n()));

            loanLossImpairment.setYoy(calYoy(xnhk0204.getF020n(), yoy.getF020n()));

            netOperatingIncome.setYoy(calYoy(xnhk0204.getF021n(), yoy.getF021n()));

            totalOperatingExpenses.setYoy(calYoy(xnhk0204.getF026n(), yoy.getF026n()));

            employeeCompensationBenefits.setYoy(calYoy(xnhk0204.getF022n(), yoy.getF022n()));

            depreciationPropertyMachineryEquipment.setYoy(calYoy(xnhk0204.getF023n(), yoy.getF023n()));

            amortizationIntangibleAssets.setYoy(calYoy(xnhk0204.getF024n(), yoy.getF024n()));

            otherOperatingExpenditure.setYoy(calYoy(xnhk0204.getF025n(), yoy.getF025n()));

            operatingProfit.setYoy(calYoy(xnhk0204.getF027n(), yoy.getF027n()));

            pfjcac.setYoy(calYoy(xnhk0204.getF029n(), yoy.getF029n()));

            otherNonBusinessItems.setYoy(calYoy(xnhk0204.getF028n(), yoy.getF028n()));

            profitBeforeTaxation.setYoy(calYoy(xnhk0204.getF030n(), yoy.getF030n()));

            tax.setYoy(calYoy(xnhk0204.getF031n(), yoy.getF031n()));

            profitLossDuringPeriod.setYoy(calYoy(xnhk0204.getF032n(), yoy.getF032n()));

            holdersShareCapitalCompany.setYoy(calYoy(xnhk0204.getF033n(), yoy.getF033n()));

            commonStockholder.setYoy(calYoy(xnhk0204.getF037n(), yoy.getF037n()));

            preferredStockholder.setYoy(calYoy(xnhk0204.getF035n(), yoy.getF035n()));

            nonControllingInterests.setYoy(calYoy(xnhk0204.getF034n(), yoy.getF034n()));

            otherEquityHolders.setYoy(calYoy(xnhk0204.getF036n(), yoy.getF036n()));

            dilutedEarningsPerShare.setYoy(calYoy(xnhk0204.getF041n(), yoy.getF041n()));

            basicEarningsPerShare.setYoy(calYoy(xnhk0204.getF040n(), yoy.getF040n()));

            auditFee.setYoy(calYoy(xnhk0204.getF051n(), yoy.getF051n()));
        }


    }

    private Xnhk0204 findYoyXnhk0204(Xnhk0204 xnhk0204, List<Xnhk0204> xnhk0204s) {
        if (xnhk0204 == null) {
            return null;
        }
        return xnhk0204s.stream().filter(item->item.getF006v().equals(xnhk0204.getF006v())&&item.getF002d()<xnhk0204.getF002d()).max(Comparator.comparing(Xnhk0204::getF002d)).orElse(null);

    }

}
