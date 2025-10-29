package com.vv.finance.investment.bg.handler.uts.sync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.uts.Xnhk0207;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0207Mapper;
import com.vv.finance.investment.bg.mongo.model.F10InsureProfitEntity;
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
 * 财务分析--利润表（保险）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class F10InsureProfitHandler extends AbstractF10CommonHandler {
    private final Xnhk0207Mapper xnhk0207Mapper;

    @Override
    public void sync(){
        List<Xnhk0207> selectList = xnhk0207Mapper.selectList(new QueryWrapper<Xnhk0207>().select("SECCODE", "max(Modified_Date) Modified_Date").groupBy("seccode"));
        selectList.forEach(item -> {
            syncCheck(item.getSeccode(),item.getModifiedDate(), F10InsureProfitEntity.class);
        });
    }

    @Override
    public void syncAll() {
        List<Xnhk0207> selectList = xnhk0207Mapper.selectList(new QueryWrapper<Xnhk0207>().select("SECCODE").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(), null, F10InsureProfitEntity.class));
    }

    @Override
    public void doSync(String code, Date updateTime) {
        log.info("执行  F10InsureProfitHandler {}",code);
        List<Xnhk0207> xnhk0207s = xnhk0207Mapper.selectList(new QueryWrapper<Xnhk0207>().eq("seccode", code));
        List<Xnhk0207> xnhk0203Update = xnhk0207s.stream().filter(item -> {
            if(updateTime==null){
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());

        }).collect(Collectors.toList());
        xnhk0203Update = convertAndFilterReports(xnhk0203Update, Xnhk0207::getF006v, Xnhk0207::setF006v, Xnhk0207::getF007n);
        List<F10InsureProfitEntity> lists = new ArrayList<>();
        xnhk0203Update.forEach(xnhk0207 -> {
            F10InsureProfitEntity f10InsureProfitEntity=new F10InsureProfitEntity();
            f10InsureProfitEntity.setStockCode(xnhk0207.getSeccode());
            f10InsureProfitEntity.setUpdateTime(xnhk0207.getModifiedDate());
            f10InsureProfitEntity.setReportType(xnhk0207.getF006v());
            f10InsureProfitEntity.setCurrency(xnhk0207.getF003v());
            f10InsureProfitEntity.setEndDate(dateFormat(xnhk0207.getF002d()));
            f10InsureProfitEntity.setEndTimestamp(dateStrToLong(f10InsureProfitEntity.getEndDate()));
            f10InsureProfitEntity.setStartDate(calStartDate(f10InsureProfitEntity.getEndDate(),xnhk0207.getF007n()));
            f10InsureProfitEntity.setStartTimestamp(dateStrToLong(f10InsureProfitEntity.getStartDate()));
            f10InsureProfitEntity.setReleaseDate(dateFormat(xnhk0207.getF001d()));
            f10InsureProfitEntity.setReleaseTimestamp(dateStrToLong(f10InsureProfitEntity.getReleaseDate()));
            f10InsureProfitEntity.setExchangeRate(xnhk0207.getF004n());
            Xnhk0207 yoyXnhk0207 = findYoyXnhk0207(xnhk0207, xnhk0207s);
            otherCal(f10InsureProfitEntity,xnhk0207,yoyXnhk0207);

            lists.add(f10InsureProfitEntity);
//            save(f10InsureProfitEntity,F10InsureProfitEntity.class);
        });
        saveBulk(lists, F10InsureProfitEntity.class);


    }
    private void otherCal(F10InsureProfitEntity f10InsureProfitEntity,Xnhk0207 xnhk0207,Xnhk0207 yoy) {

         // 2022年3月7日修改，营业总收入取值由 F017N 改为 F018N
         F10Val grossRevenue= F10Val.builder().val(xnhk0207.getF018n()).build();
         f10InsureProfitEntity.setGrossRevenue(grossRevenue);

         F10Val realizedNetPremiumIncome= F10Val.builder().val(xnhk0207.getF012n()).build();
         f10InsureProfitEntity.setRealizedNetPremiumIncome(realizedNetPremiumIncome);

         F10Val netPremiumIncome= F10Val.builder().val(xnhk0207.getF010n()).build();
         f10InsureProfitEntity.setNetPremiumIncome(netPremiumIncome);

         F10Val totalPremium= F10Val.builder().val(xnhk0207.getF008n()).build();
         f10InsureProfitEntity.setTotalPremium(totalPremium);

         F10Val reinsurancePremium= F10Val.builder().val(xnhk0207.getF009n()).build();
         f10InsureProfitEntity.setReinsurancePremium(reinsurancePremium);

         F10Val unearnedPremiumReserve= F10Val.builder().val(xnhk0207.getF011n()).build();
         f10InsureProfitEntity.setUnearnedPremiumReserve(unearnedPremiumReserve);

         F10Val netInvestmentIncome= F10Val.builder().val(xnhk0207.getF013n()).build();
         f10InsureProfitEntity.setNetInvestmentIncome(netInvestmentIncome);

         F10Val investmentIncome= F10Val.builder().val(xnhk0207.getF015n()).build();
         f10InsureProfitEntity.setInvestmentIncome(investmentIncome);

         F10Val exchangeGain= F10Val.builder().val(xnhk0207.getF016n()).build();
         f10InsureProfitEntity.setExchangeGain(exchangeGain);

         F10Val otherOperatingRevenue= F10Val.builder().val(xnhk0207.getF017n()).build();
         f10InsureProfitEntity.setOtherOperatingRevenue(otherOperatingRevenue);

         F10Val insuranceExpensesTotalExpenses= F10Val.builder().val(xnhk0207.getF024n()).build();
         f10InsureProfitEntity.setInsuranceExpensesTotalExpenses(insuranceExpensesTotalExpenses);

         F10Val indemnityInsuranceInterests= F10Val.builder().val(xnhk0207.getF019n()).build();
         f10InsureProfitEntity.setIndemnityInsuranceInterests(indemnityInsuranceInterests);

         F10Val deferredPolicyCosts= F10Val.builder().val(xnhk0207.getF020n()).build();
         f10InsureProfitEntity.setDeferredPolicyCosts(deferredPolicyCosts);

         F10Val netCommissionExpenses= F10Val.builder().val(xnhk0207.getF021n()).build();
         f10InsureProfitEntity.setNetCommissionExpenses(netCommissionExpenses);

         F10Val administrationExpenses= F10Val.builder().val(xnhk0207.getF022n()).build();
         f10InsureProfitEntity.setAdministrationExpenses(administrationExpenses);

         F10Val otherOperatingExpenditure= F10Val.builder().val(xnhk0207.getF023n()).build();
         f10InsureProfitEntity.setOtherOperatingExpenditure(otherOperatingExpenditure);

         F10Val operatingProfit= F10Val.builder().val(xnhk0207.getF025n()).build();
         f10InsureProfitEntity.setOperatingProfit(operatingProfit);

         F10Val financingCost= F10Val.builder().val(xnhk0207.getF026n()).build();
         f10InsureProfitEntity.setFinancingCost(financingCost);

         F10Val pfjcac= F10Val.builder().val(xnhk0207.getF028n()).build();
         f10InsureProfitEntity.setPfjcac(pfjcac);

         F10Val profitBeforeTaxation= F10Val.builder().val(xnhk0207.getF029n()).build();
         f10InsureProfitEntity.setProfitBeforeTaxation(profitBeforeTaxation);

         F10Val tax= F10Val.builder().val(xnhk0207.getF030n()).build();
         f10InsureProfitEntity.setTax(tax);

         F10Val profitLossDuringPeriod= F10Val.builder().val(xnhk0207.getF031n()).build();
         f10InsureProfitEntity.setProfitLossDuringPeriod(profitLossDuringPeriod);

         F10Val holdersShareCapitalCompany= F10Val.builder().val(xnhk0207.getF032n()).build();
         f10InsureProfitEntity.setHoldersShareCapitalCompany(holdersShareCapitalCompany);

         F10Val commonStockholder= F10Val.builder().val(xnhk0207.getF036n()).build();
         f10InsureProfitEntity.setCommonStockholder(commonStockholder);

         F10Val preferredStockholder= F10Val.builder().val(xnhk0207.getF034n()).build();
         f10InsureProfitEntity.setPreferredStockholder(preferredStockholder);

         F10Val nonControllingInterests= F10Val.builder().val(xnhk0207.getF033n()).build();
         f10InsureProfitEntity.setNonControllingInterests(nonControllingInterests);

         F10Val otherEquityHolders= F10Val.builder().val(xnhk0207.getF035n()).build();
         f10InsureProfitEntity.setOtherEquityHolders(otherEquityHolders);

         F10Val dilutedEarningsPerShare= F10Val.builder().val(xnhk0207.getF040n()).build();
         f10InsureProfitEntity.setDilutedEarningsPerShare(dilutedEarningsPerShare);

         F10Val basicEarningsPerShare= F10Val.builder().val(xnhk0207.getF039n()).build();
         f10InsureProfitEntity.setBasicEarningsPerShare(basicEarningsPerShare);

         f10InsureProfitEntity.setAuditOpinion(xnhk0207.getF065v());

         F10Val auditFee= F10Val.builder().val(xnhk0207.getF050n()).build();
         f10InsureProfitEntity.setAuditFee(auditFee);

         if(yoy !=null){
              grossRevenue.setYoy(calYoy(xnhk0207.getF018n(),yoy.getF018n()));

              realizedNetPremiumIncome.setYoy(calYoy(xnhk0207.getF012n(),yoy.getF012n()));

              netPremiumIncome.setYoy(calYoy(xnhk0207.getF010n(),yoy.getF010n()));


             totalPremium.setYoy(calYoy(xnhk0207.getF008n(),yoy.getF008n()));


             reinsurancePremium.setYoy(calYoy(xnhk0207.getF009n(),yoy.getF009n()));


             unearnedPremiumReserve.setYoy(calYoy(xnhk0207.getF011n(),yoy.getF011n()));


             netInvestmentIncome.setYoy(calYoy(xnhk0207.getF013n(),yoy.getF013n()));


             investmentIncome.setYoy(calYoy(xnhk0207.getF015n(),yoy.getF015n()));


             exchangeGain.setYoy(calYoy(xnhk0207.getF016n(),yoy.getF016n()));


             otherOperatingRevenue.setYoy(calYoy(xnhk0207.getF017n(),yoy.getF017n()));


             insuranceExpensesTotalExpenses.setYoy(calYoy(xnhk0207.getF024n(),yoy.getF024n()));

             indemnityInsuranceInterests.setYoy(calYoy(xnhk0207.getF019n(),yoy.getF019n()));

             deferredPolicyCosts.setYoy(calYoy(xnhk0207.getF020n(),yoy.getF020n()));

             netCommissionExpenses.setYoy(calYoy(xnhk0207.getF021n(),yoy.getF021n()));

             administrationExpenses.setYoy(calYoy(xnhk0207.getF022n(),yoy.getF022n()));

             otherOperatingExpenditure.setYoy(calYoy(xnhk0207.getF023n(),yoy.getF023n()));

             operatingProfit.setYoy(calYoy(xnhk0207.getF025n(),yoy.getF025n()));

             financingCost.setYoy(calYoy(xnhk0207.getF026n(),yoy.getF026n()));

             pfjcac.setYoy(calYoy(xnhk0207.getF028n(),yoy.getF028n()));

             profitBeforeTaxation.setYoy(calYoy(xnhk0207.getF029n(),yoy.getF029n()));

             tax.setYoy(calYoy(xnhk0207.getF030n(),yoy.getF030n()));

             profitLossDuringPeriod.setYoy(calYoy(xnhk0207.getF031n(),yoy.getF031n()));

             holdersShareCapitalCompany.setYoy(calYoy(xnhk0207.getF032n(),yoy.getF032n()));

             commonStockholder.setYoy(calYoy(xnhk0207.getF036n(),yoy.getF036n()));

             preferredStockholder.setYoy(calYoy(xnhk0207.getF034n(),yoy.getF034n()));

             nonControllingInterests.setYoy(calYoy(xnhk0207.getF033n(),yoy.getF033n()));

             otherEquityHolders.setYoy(calYoy(xnhk0207.getF035n(),yoy.getF035n()));

             dilutedEarningsPerShare.setYoy(calYoy(xnhk0207.getF040n(),yoy.getF040n()));

             basicEarningsPerShare.setYoy(calYoy(xnhk0207.getF039n(),yoy.getF039n()));


             auditFee.setYoy(calYoy(xnhk0207.getF050n(),yoy.getF050n()));
         }




    }

    private Xnhk0207 findYoyXnhk0207(Xnhk0207 xnhk0207,List<Xnhk0207> xnhk0207s){
        if(xnhk0207==null){
            return null;
        }
        return xnhk0207s.stream().filter(item->item.getF006v().equals(xnhk0207.getF006v())&&item.getF002d()<xnhk0207.getF002d()).max(Comparator.comparing(Xnhk0207::getF002d)).orElse(null);
    }
}
