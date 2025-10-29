package com.vv.finance.investment.bg.handler.uts.sync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.uts.Xnhk0208;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0208Mapper;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesFinancialEntity;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesInsuranceEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资产负债-保险 Handler
 *
 * @Auto: chenzhenlong
 * @Date: 2021/7/23 16:08
 * @Version 1.0
 * 资产负债-保险
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class F10AssetsLiabilitiesInsuranceHandler extends AbstractF10CommonHandler {

    private final Xnhk0208Mapper xnhk0208Mapper;


    @Override
    public void sync() {
        List<Xnhk0208> selectList = xnhk0208Mapper.selectList(new QueryWrapper<Xnhk0208>().select("SECCODE", "max(Modified_Date) Modified_Date").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(),item.getModifiedDate(),F10AssetsLiabilitiesInsuranceEntity.class));
    }

    @Override
    public void syncAll() {
        List<Xnhk0208> selectList = xnhk0208Mapper.selectList(new QueryWrapper<Xnhk0208>().select("SECCODE").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(), null, F10AssetsLiabilitiesInsuranceEntity.class));
    }

    @Override
    public void doSync(String code, Date updateTime) {
        log.info("执行  F10AssetsLiabilitiesInsuranceHandler {}",code);
        Map<String, F10AssetsLiabilitiesInsuranceEntity> xnhk0208 = xnhk0208Sync(code, updateTime);
//        xnhk0208.forEach((k, v) -> save(v, F10AssetsLiabilitiesInsuranceEntity.class));
        saveBulk(new ArrayList<>(xnhk0208.values()), F10AssetsLiabilitiesInsuranceEntity.class);
    }

    private Map<String, F10AssetsLiabilitiesInsuranceEntity> xnhk0208Sync(String code, Date updateTime) {
        List<Xnhk0208> xnhk0208s = xnhk0208Mapper.selectList(new QueryWrapper<Xnhk0208>().eq("seccode", code));
        //所有需要更新的数据条数
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
        Map<String, Xnhk0208> collect = xnhk0208Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), t -> t));
        Map<String, F10AssetsLiabilitiesInsuranceEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            try {
                F10AssetsLiabilitiesInsuranceEntity assetsLiabilitiesInsuranceEntity = new F10AssetsLiabilitiesInsuranceEntity();
                assetsLiabilitiesInsuranceEntity.setReportType(v.getF006v());
                assetsLiabilitiesInsuranceEntity.setStockCode(v.getSeccode());
                assetsLiabilitiesInsuranceEntity.setCurrency(v.getF003v());
                assetsLiabilitiesInsuranceEntity.setReleaseDate(dateFormat(v.getF001d()));
                assetsLiabilitiesInsuranceEntity.setReleaseTimestamp(dateStrToLong(assetsLiabilitiesInsuranceEntity.getReleaseDate()));
                assetsLiabilitiesInsuranceEntity.setEndDate(dateFormat(v.getF002d()));
                assetsLiabilitiesInsuranceEntity.setEndTimestamp(dateStrToLong(assetsLiabilitiesInsuranceEntity.getEndDate()));
                assetsLiabilitiesInsuranceEntity.setStartDate(calStartDate(assetsLiabilitiesInsuranceEntity.getEndDate(), v.getF007n()));
                assetsLiabilitiesInsuranceEntity.setStartTimestamp(dateStrToLong(assetsLiabilitiesInsuranceEntity.getStartDate()));
                assetsLiabilitiesInsuranceEntity.setUpdateTime(v.getModifiedDate());
                assetsLiabilitiesInsuranceEntity.setAuditOpinion(v.getF061v());
                //取出上个周期数据
                Xnhk0208 yoyXnhk0208 = findYoyXnhk0208(v, xnhk0208s);
                //计算同比并且添加进去
                addValAndYoy(assetsLiabilitiesInsuranceEntity, v, yoyXnhk0208);
                result.put(k, assetsLiabilitiesInsuranceEntity);
            } catch (Exception e) {
                log.error("xnhk0203Sync", e);
            }

        });
        return result;
    }

    private void addValAndYoy(F10AssetsLiabilitiesInsuranceEntity f10AssetsLiabilitiesInsuranceEntity, Xnhk0208 xnhk0208, Xnhk0208 yoy) {
        F10Val totalAssets = F10Val.builder().val(xnhk0208.getF029n()).build(); //总资产
        F10Val mandatoryDeposits = F10Val.builder().val(xnhk0208.getF026n()).build(); //法定存款
        F10Val cashAndBankDeposit = F10Val.builder().val(xnhk0208.getF027n()).build(); //现金及银行寄存
        F10Val debtSecurities = F10Val.builder().val(xnhk0208.getF012n()).build(); //债权类证券
        F10Val equitySecurities = F10Val.builder().val(xnhk0208.getF013n()).build(); //股权类证券
        F10Val derivativeFinancialAssets = F10Val.builder().val(xnhk0208.getF014n()).build(); //衍生金融资产
        F10Val investmentProperty = F10Val.builder().val(xnhk0208.getF008n()).build(); //投资物业
        F10Val housingDeliveryRoomAndEquipment = F10Val.builder().val(xnhk0208.getF009n()).build(); //房屋，产房及设备
        F10Val interestInLeasedLand = F10Val.builder().val(xnhk0208.getF010n()).build(); //租赁土地权益
        F10Val prepaidLandLeasePayments = F10Val.builder().val(xnhk0208.getF011n()).build(); //预付土地/租赁款项
        F10Val loans = F10Val.builder().val(xnhk0208.getF015n()).build(); //贷款
        F10Val otherInvestment = F10Val.builder().val(xnhk0208.getF016n()).build(); //其他投资
        F10Val interestInAssociates = F10Val.builder().val(xnhk0208.getF017n()).build(); //联营公司权益
        F10Val jcci = F10Val.builder().val(xnhk0208.getF018n()).build(); //共同控制公司权益
        F10Val goodWill = F10Val.builder().val(xnhk0208.getF019n()).build(); //商誉
        F10Val otherIntangibleAssets = F10Val.builder().val(xnhk0208.getF020n()).build(); //其他无形资产
        F10Val insuranceReceivable = F10Val.builder().val(xnhk0208.getF021n()).build(); //应收保险款项
        F10Val reinsuranceAssets = F10Val.builder().val(xnhk0208.getF022n()).build(); //分保资产
        F10Val arrc = F10Val.builder().val(xnhk0208.getF023n()).build();//应收关连公司款项
        F10Val deferredPolicyAcquisitionCosts = F10Val.builder().val(xnhk0208.getF024n()).build();//递延保单获取成本
        F10Val deferredTaxAssets = F10Val.builder().val(xnhk0208.getF025n()).build();//递延税项资产
        F10Val otherAssets = F10Val.builder().val(xnhk0208.getF028n()).build();//其他资产
        F10Val totalLiabilities = F10Val.builder().val(xnhk0208.getF040n()).build(); //总负债
        F10Val insuranceReserve = F10Val.builder().val(xnhk0208.getF030n()).build(); //保险准备金
        F10Val insuranceProtectionFond = F10Val.builder().val(xnhk0208.getF031n()).build(); //保险保障基金
        F10Val insurancePayable = F10Val.builder().val(xnhk0208.getF032n()).build(); //应付保险款项
        F10Val insuredInvestmentContractLiabilities = F10Val.builder().val(xnhk0208.getF033n()).build(); //保户投资合同负债
        F10Val amountsPayableToRelatedCompanies = F10Val.builder().val(xnhk0208.getF036n()).build(); //应付关连公司款项
        F10Val incomeTaxPayable = F10Val.builder().val(xnhk0208.getF037n()).build(); //应付所得税
        F10Val deferredTaxLiabilities = F10Val.builder().val(xnhk0208.getF038n()).build(); //递延税项负债
        F10Val otherDebt = F10Val.builder().val(xnhk0208.getF039n()).build(); //其他负债
        F10Val netAssetValue = F10Val.builder().val(xnhk0208.getF059n()).build(); //资产净值
        F10Val totalEquity = F10Val.builder().val(xnhk0208.getF053n()).build(); //总权益
        F10Val totalCapital = F10Val.builder().val(xnhk0208.getF043n()).build(); //股本总额
        F10Val capitalStockCommonStock = F10Val.builder().val(xnhk0208.getF041n()).build(); //股本（普通股）
        F10Val capitalStockPreferredStock = F10Val.builder().val(xnhk0208.getF042n()).build(); //股本（优先股）
        F10Val capitalStockPremium = F10Val.builder().val(xnhk0208.getF044n()).build(); //股本溢价
        F10Val capitalReserve = F10Val.builder().val(xnhk0208.getF045n()).build(); //资本储备
        F10Val otherReserve = F10Val.builder().val(xnhk0208.getF046n()).build(); //其他储备
        F10Val retainedProfit = F10Val.builder().val(xnhk0208.getF047n()).build(); //保留溢利
        F10Val totalReserves = F10Val.builder().val(xnhk0208.getF048n()).build(); //储备总额
        F10Val stockholdersEquity = F10Val.builder().val(xnhk0208.getF049n()).build(); //股东权益
        F10Val nonControllingInterests = F10Val.builder().val(xnhk0208.getF052n()).build(); //非控股权益
        F10Val otherEquityHolders = F10Val.builder().val(xnhk0208.getF051n()).build(); //其他权益持有人
        //卖出回购资产
        F10Val sellBuybackAssets = F10Val.builder().val(xnhk0208.getF034n()).build();
        //衍生金融负债
        F10Val derivativeFinancialLiability = F10Val.builder().val(xnhk0208.getF035n()).build();

        if (yoy != null) {
            totalAssets.setYoy(calYoy(xnhk0208.getF029n(), yoy.getF029n()));
            mandatoryDeposits.setYoy(calYoy(xnhk0208.getF026n(), yoy.getF026n()));
            cashAndBankDeposit.setYoy(calYoy(xnhk0208.getF027n(), yoy.getF027n()));
            debtSecurities.setYoy(calYoy(xnhk0208.getF012n(), yoy.getF012n()));
            equitySecurities.setYoy(calYoy(xnhk0208.getF013n(), yoy.getF013n()));
            derivativeFinancialAssets.setYoy(calYoy(xnhk0208.getF014n(), yoy.getF014n()));
            investmentProperty.setYoy(calYoy(xnhk0208.getF008n(), yoy.getF008n()));
            housingDeliveryRoomAndEquipment.setYoy(calYoy(xnhk0208.getF009n(), yoy.getF009n()));
            interestInLeasedLand.setYoy(calYoy(xnhk0208.getF010n(), yoy.getF010n()));
            prepaidLandLeasePayments.setYoy(calYoy(xnhk0208.getF011n(), yoy.getF011n()));
            loans.setYoy(calYoy(xnhk0208.getF015n(), yoy.getF015n()));
            otherInvestment.setYoy(calYoy(xnhk0208.getF016n(), yoy.getF016n()));
            interestInAssociates.setYoy(calYoy(xnhk0208.getF017n(), yoy.getF017n()));
            jcci.setYoy(calYoy(xnhk0208.getF018n(), yoy.getF018n()));
            goodWill.setYoy(calYoy(xnhk0208.getF019n(), yoy.getF019n()));
            otherIntangibleAssets.setYoy(calYoy(xnhk0208.getF020n(), yoy.getF020n()));
            insuranceReceivable.setYoy(calYoy(xnhk0208.getF021n(), yoy.getF021n()));
            reinsuranceAssets.setYoy(calYoy(xnhk0208.getF022n(), yoy.getF022n()));
            arrc.setYoy(calYoy(xnhk0208.getF023n(), yoy.getF023n()));
            deferredPolicyAcquisitionCosts.setYoy(calYoy(xnhk0208.getF024n(), yoy.getF024n()));
            deferredTaxAssets.setYoy(calYoy(xnhk0208.getF025n(), yoy.getF025n()));
            otherAssets.setYoy(calYoy(xnhk0208.getF028n(), yoy.getF028n()));
            totalLiabilities.setYoy(calYoy(xnhk0208.getF040n(), yoy.getF040n()));
            insuranceReserve.setYoy(calYoy(xnhk0208.getF030n(), yoy.getF030n()));
            insuranceProtectionFond.setYoy(calYoy(xnhk0208.getF031n(), yoy.getF031n()));
            insurancePayable.setYoy(calYoy(xnhk0208.getF032n(), yoy.getF032n()));
            insuredInvestmentContractLiabilities.setYoy(calYoy(xnhk0208.getF033n(), yoy.getF033n()));
            amountsPayableToRelatedCompanies.setYoy(calYoy(xnhk0208.getF036n(), yoy.getF036n()));
            incomeTaxPayable.setYoy(calYoy(xnhk0208.getF037n(), yoy.getF037n()));
            deferredTaxLiabilities.setYoy(calYoy(xnhk0208.getF038n(), yoy.getF038n()));
            otherDebt.setYoy(calYoy(xnhk0208.getF039n(), yoy.getF039n()));
            netAssetValue.setYoy(calYoy(xnhk0208.getF059n(), yoy.getF059n()));
            totalEquity.setYoy(calYoy(xnhk0208.getF053n(), yoy.getF053n()));
            totalCapital.setYoy(calYoy(xnhk0208.getF043n(), yoy.getF043n()));
            capitalStockCommonStock.setYoy(calYoy(xnhk0208.getF041n(), yoy.getF041n()));
            capitalStockPreferredStock.setYoy(calYoy(xnhk0208.getF042n(), yoy.getF042n()));
            capitalStockPremium.setYoy(calYoy(xnhk0208.getF044n(), yoy.getF044n()));
            capitalReserve.setYoy(calYoy(xnhk0208.getF045n(), yoy.getF045n()));
            otherReserve.setYoy(calYoy(xnhk0208.getF046n(), yoy.getF046n()));
            retainedProfit.setYoy(calYoy(xnhk0208.getF047n(), yoy.getF047n()));
            totalReserves.setYoy(calYoy(xnhk0208.getF048n(), yoy.getF048n()));
            stockholdersEquity.setYoy(calYoy(xnhk0208.getF049n(), yoy.getF049n()));
            nonControllingInterests.setYoy(calYoy(xnhk0208.getF052n(), yoy.getF052n()));
            otherEquityHolders.setYoy(calYoy(xnhk0208.getF051n(), yoy.getF051n()));
            sellBuybackAssets.setYoy(calYoy(xnhk0208.getF034n(), yoy.getF034n()));
            derivativeFinancialLiability.setYoy(calYoy(xnhk0208.getF035n(), yoy.getF035n()));
        }
        f10AssetsLiabilitiesInsuranceEntity.setTotalAssets(totalAssets);
        f10AssetsLiabilitiesInsuranceEntity.setMandatoryDeposits(mandatoryDeposits);
        f10AssetsLiabilitiesInsuranceEntity.setCashAndBankDeposit(cashAndBankDeposit);
        f10AssetsLiabilitiesInsuranceEntity.setDebtSecurities(debtSecurities);
        f10AssetsLiabilitiesInsuranceEntity.setEquitySecurities(equitySecurities);
        f10AssetsLiabilitiesInsuranceEntity.setDerivativeFinancialAssets(derivativeFinancialAssets);
        f10AssetsLiabilitiesInsuranceEntity.setInvestmentProperty(investmentProperty);
        f10AssetsLiabilitiesInsuranceEntity.setHousingDeliveryRoomAndEquipment(housingDeliveryRoomAndEquipment);
        f10AssetsLiabilitiesInsuranceEntity.setInterestInLeasedLand(interestInLeasedLand);
        f10AssetsLiabilitiesInsuranceEntity.setPrepaidLandLeasePayments(prepaidLandLeasePayments);
        f10AssetsLiabilitiesInsuranceEntity.setLoans(loans);
        f10AssetsLiabilitiesInsuranceEntity.setOtherInvestment(otherInvestment);
        f10AssetsLiabilitiesInsuranceEntity.setInterestInAssociates(interestInAssociates);
        f10AssetsLiabilitiesInsuranceEntity.setJcci(jcci);
        f10AssetsLiabilitiesInsuranceEntity.setGoodWill(goodWill);
        f10AssetsLiabilitiesInsuranceEntity.setOtherIntangibleAssets(otherIntangibleAssets);
        f10AssetsLiabilitiesInsuranceEntity.setInsuranceReceivable(insuranceReceivable);
        f10AssetsLiabilitiesInsuranceEntity.setReinsuranceAssets(reinsuranceAssets);
        f10AssetsLiabilitiesInsuranceEntity.setArrc(arrc);
        f10AssetsLiabilitiesInsuranceEntity.setDeferredPolicyAcquisitionCosts(deferredPolicyAcquisitionCosts);
        f10AssetsLiabilitiesInsuranceEntity.setDeferredTaxAssets(deferredTaxAssets);
        f10AssetsLiabilitiesInsuranceEntity.setOtherAssets(otherAssets);
        f10AssetsLiabilitiesInsuranceEntity.setTotalLiabilities(totalLiabilities);
        f10AssetsLiabilitiesInsuranceEntity.setInsuranceReserve(insuranceReserve);
        f10AssetsLiabilitiesInsuranceEntity.setInsuranceProtectionFond(insuranceProtectionFond);
        f10AssetsLiabilitiesInsuranceEntity.setInsurancePayable(insurancePayable);
        f10AssetsLiabilitiesInsuranceEntity.setInsuredInvestmentContractLiabilities(insuredInvestmentContractLiabilities);
        f10AssetsLiabilitiesInsuranceEntity.setAmountsPayableToRelatedCompanies(amountsPayableToRelatedCompanies);
        f10AssetsLiabilitiesInsuranceEntity.setIncomeTaxPayable(incomeTaxPayable);
        f10AssetsLiabilitiesInsuranceEntity.setDeferredTaxLiabilities(deferredTaxLiabilities);
        f10AssetsLiabilitiesInsuranceEntity.setOtherDebt(otherDebt);
        f10AssetsLiabilitiesInsuranceEntity.setNetAssetValue(netAssetValue);
        f10AssetsLiabilitiesInsuranceEntity.setTotalEquity(totalEquity);
        f10AssetsLiabilitiesInsuranceEntity.setTotalCapital(totalCapital);
        f10AssetsLiabilitiesInsuranceEntity.setCapitalStockCommonStock(capitalStockCommonStock);
        f10AssetsLiabilitiesInsuranceEntity.setCapitalStockPreferredStock(capitalStockPreferredStock);
        f10AssetsLiabilitiesInsuranceEntity.setCapitalStockPremium(capitalStockPremium);
        f10AssetsLiabilitiesInsuranceEntity.setCapitalReserve(capitalReserve);
        f10AssetsLiabilitiesInsuranceEntity.setOtherReserve(otherReserve);
        f10AssetsLiabilitiesInsuranceEntity.setRetainedProfit(retainedProfit);
        f10AssetsLiabilitiesInsuranceEntity.setTotalReserves(totalReserves);
        f10AssetsLiabilitiesInsuranceEntity.setStockholdersEquity(stockholdersEquity);
        f10AssetsLiabilitiesInsuranceEntity.setNonControllingInterests(nonControllingInterests);
        f10AssetsLiabilitiesInsuranceEntity.setOtherEquityHolders(otherEquityHolders);
        f10AssetsLiabilitiesInsuranceEntity.setSellBuybackAssets(sellBuybackAssets);
        f10AssetsLiabilitiesInsuranceEntity.setDerivativeFinancialLiability(derivativeFinancialLiability);
    }


    private Xnhk0208 findYoyXnhk0208(Xnhk0208 xnhk0208, List<Xnhk0208> xnhk0208s) {
        return xnhk0208s.stream().filter(item->item.getF006v().equals(xnhk0208.getF006v())&&item.getF002d()<xnhk0208.getF002d()).max(Comparator.comparing(Xnhk0208::getF002d)).orElse(null);

    }

}
