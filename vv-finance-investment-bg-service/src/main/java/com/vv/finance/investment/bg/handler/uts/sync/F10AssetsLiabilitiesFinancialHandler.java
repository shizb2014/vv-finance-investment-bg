package com.vv.finance.investment.bg.handler.uts.sync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.uts.Xnhk0201;
import com.vv.finance.investment.bg.entity.uts.Xnhk0205;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0205Mapper;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesFinancialEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资产负债-金融 Handler
 *
 * @Auto: chenzhenlong
 * @Date: 2021/7/23 16:07
 * @Version 1.0
 * 资产负债-金融
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class F10AssetsLiabilitiesFinancialHandler extends AbstractF10CommonHandler {

    private final Xnhk0205Mapper xnhk0205Mapper;


    @Override
    public void sync() {
        //select SECCODE,MAX(Modified_Date) modifiedDate from xnhk0205 group by SECCODE;
        List<Xnhk0205> selectList = xnhk0205Mapper.selectList(new QueryWrapper<Xnhk0205>().select("SECCODE", "max(Modified_Date) Modified_Date").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(),item.getModifiedDate(),F10AssetsLiabilitiesFinancialEntity.class));
    }

    @Override
    public void syncAll() {
        List<Xnhk0205> selectList = xnhk0205Mapper.selectList(new QueryWrapper<Xnhk0205>().select("SECCODE").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(), null, F10AssetsLiabilitiesFinancialEntity.class));
    }

    @Override
    public void doSync(String code, Date updateTime) {
        log.info("执行  F10AssetsLiabilitiesFinancialHandler {}",code);
        Map<String, F10AssetsLiabilitiesFinancialEntity> xnhk0205 = xnhk0205Sync(code, updateTime);
        /*xnhk0205.forEach((k, v) -> {
            save(v, F10AssetsLiabilitiesFinancialEntity.class);
        });*/
        saveBulk(new ArrayList<>(xnhk0205.values()),F10AssetsLiabilitiesFinancialEntity.class);

    }

    //基于mongodb反查的updateTime 为条件,再查询 Xnhk0205表的数据 >
    private Map<String, F10AssetsLiabilitiesFinancialEntity> xnhk0205Sync(String code, Date updateTime) {
        List<Xnhk0205> xnhk0205s = xnhk0205Mapper.selectList(new QueryWrapper<Xnhk0205>().eq("seccode", code));
        //所有需要更新的数据条数
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
        Map<String, Xnhk0205> collect = xnhk0205Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), t -> t));
        Map<String, F10AssetsLiabilitiesFinancialEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            try {
                F10AssetsLiabilitiesFinancialEntity assetsLiabilitiesFinancialEntity = new F10AssetsLiabilitiesFinancialEntity();
                assetsLiabilitiesFinancialEntity.setReportType(v.getF006v());
                assetsLiabilitiesFinancialEntity.setStockCode(v.getSeccode());
                assetsLiabilitiesFinancialEntity.setCurrency(v.getF003v());
                assetsLiabilitiesFinancialEntity.setReleaseDate(dateFormat(v.getF001d()));
                assetsLiabilitiesFinancialEntity.setReleaseTimestamp(dateStrToLong(assetsLiabilitiesFinancialEntity.getReleaseDate()));
                assetsLiabilitiesFinancialEntity.setEndDate(dateFormat(v.getF002d()));
                assetsLiabilitiesFinancialEntity.setEndTimestamp(dateStrToLong(assetsLiabilitiesFinancialEntity.getEndDate()));
                assetsLiabilitiesFinancialEntity.setStartDate(calStartDate(assetsLiabilitiesFinancialEntity.getEndDate(), v.getF007d()));
                assetsLiabilitiesFinancialEntity.setStartTimestamp(dateStrToLong(assetsLiabilitiesFinancialEntity.getStartDate()));
                assetsLiabilitiesFinancialEntity.setUpdateTime(v.getModifiedDate());
                assetsLiabilitiesFinancialEntity.setAuditOpinion(v.getF066v());
                //取出上个周期数据
                Xnhk0205 yoyXnhk0205 = findYoyXnhk0205(v, xnhk0205s);
                //计算同比并且添加进去
                addValAndYoy(assetsLiabilitiesFinancialEntity, v, yoyXnhk0205);
                result.put(k, assetsLiabilitiesFinancialEntity);
            } catch (Exception e) {
                log.error("xnhk0203Sync", e);
            }

        });
        return result;
    }

    private void addValAndYoy(F10AssetsLiabilitiesFinancialEntity f10AssetsLiabilitiesFinancialEntity, Xnhk0205 xnhk0205, Xnhk0205 yoy) {
        F10Val totalAssets = F10Val.builder().val(xnhk0205.getF027n()).build(); //总资产
        F10Val cashAndShortTermFunds = F10Val.builder().val(xnhk0205.getF008n()).build(); //库存现金及短缺资金
        F10Val collectionOfItemsFromOtherBank = F10Val.builder().val(xnhk0205.getF009n()).build(); //向其他银行托收中之项目
        F10Val depositsInInterbankAndOtherFinancialInstitutions = F10Val.builder().val(xnhk0205.getF010n()).build(); //银行同业及其他金融机构存款
        F10Val hongKongGovermentCertificateOfIndebtedness = F10Val.builder().val(xnhk0205.getF011n()).build(); //香港政府负债证明书
        F10Val commercialPaper = F10Val.builder().val(xnhk0205.getF012n()).build(); //商业票据
        F10Val assetsForTradingPurposes = F10Val.builder().val(xnhk0205.getF013n()).build(); //交易用途资产
        F10Val assetsForNonTradingPurposes = F10Val.builder().val(xnhk0205.getF014n()).build(); //非交易用途资产
        F10Val gainAndLoseFinancialAssetsAtFairValue = F10Val.builder().val(xnhk0205.getF015n()).build(); //按公平值入损益金融资产
        F10Val availableForSaleFinancialAssets = F10Val.builder().val(xnhk0205.getF016n()).build(); //可供出售金融资产
        F10Val derivativeFinancialAssets = F10Val.builder().val(xnhk0205.getF017n()).build(); //衍生性金融资产
        F10Val certificateOfDepositHeld = F10Val.builder().val(xnhk0205.getF018n()).build(); //所持存款证
        F10Val interbankLoansAndAdvances = F10Val.builder().val(xnhk0205.getF019n()).build(); //银行同业贷款及垫款
        F10Val customerLoansAndAdvances = F10Val.builder().val(xnhk0205.getF020n()).build(); //客户贷款及垫款
        F10Val financialInvestment = F10Val.builder().val(xnhk0205.getF021n()).build(); //金融投资
        F10Val holdToMaturityInvestment = F10Val.builder().val(xnhk0205.getF022n()).build(); //持至到期投资
        F10Val jointVenturesAndJointVenturesInterets = F10Val.builder().val(xnhk0205.getF023n()).build(); //联营及合资公司权益
        F10Val goodwillAndIntangibleAssets = F10Val.builder().val(xnhk0205.getF024n()).build(); //商誉及无形资产
        F10Val fiexedAssets = F10Val.builder().val(xnhk0205.getF025n()).build(); //固定资产
        F10Val otherAssets = F10Val.builder().val(xnhk0205.getF026n()).build(); //其他资产
        F10Val totalLiabilities = F10Val.builder().val(xnhk0205.getF042n()).build(); //总负债
        F10Val hongKongGovernmentCirculatesPaperMoney = F10Val.builder().val(xnhk0205.getF028n()).build(); //香港政府流通纸币
        F10Val itemsTransmittedToOtherBanks = F10Val.builder().val(xnhk0205.getF031n()).build(); //向其他银行传送之项目
        F10Val depositsOfInterbankAndFinancialInstitutionsLiabilities = F10Val.builder().val(xnhk0205.getF029n()).build(); //银行同业及金融机构存款（负债）
        F10Val timeDepositsInterbankAndFinancialInstitutionsLiabilities = F10Val.builder().val(xnhk0205.getF032n()).build(); //定期存放银行同业及金融机构（负债）
        F10Val customersDeposit = F10Val.builder().val(xnhk0205.getF030n()).build(); //客户存款
        F10Val certificatesOfDepositIssued = F10Val.builder().val(xnhk0205.getF034n()).build(); //已发行存款证
        F10Val bondsIssued = F10Val.builder().val(xnhk0205.getF033n()).build(); //已发行债券
        F10Val convertibleBondsIssued = F10Val.builder().val(xnhk0205.getF035n()).build(); //已发行可换股债券
        F10Val floatingRateNotesIssued = F10Val.builder().val(xnhk0205.getF036n()).build(); //已发行浮息票据
        F10Val tradingLiabilities = F10Val.builder().val(xnhk0205.getF037n()).build(); //交易用途负债
        F10Val enteringProfitAndLossFinancialLiabilitiesAtFairValue = F10Val.builder().val(xnhk0205.getF038n()).build(); //按公平值入损益金融负债
        F10Val derivativeFinancialLiability = F10Val.builder().val(xnhk0205.getF039n()).build(); //衍生金融负债
        F10Val subordinatedDebt = F10Val.builder().val(xnhk0205.getF040n()).build(); //后偿负债
        F10Val otherDebt = F10Val.builder().val(xnhk0205.getF041n()).build(); //其他负债
        F10Val netAssetValue = F10Val.builder().val(xnhk0205.getF064n()).build(); //资产净值
        F10Val totalEquity = F10Val.builder().val(xnhk0205.getF054n()).build(); //总权益
        F10Val totalCapital = F10Val.builder().val(xnhk0205.getF045n()).build(); //股本总额
        F10Val capitalStockCommonStock = F10Val.builder().val(xnhk0205.getF043n()).build(); //股本（普通股）
        F10Val capitalStockPreferredStock = F10Val.builder().val(xnhk0205.getF044n()).build(); //股本（优先股）
        F10Val capitalStockPremium = F10Val.builder().val(xnhk0205.getF046n()).build(); //股本溢价
        F10Val capitalReserve = F10Val.builder().val(xnhk0205.getF047n()).build(); //资本储备
        F10Val otherReserve = F10Val.builder().val(xnhk0205.getF048n()).build(); //其他储备
        F10Val retainedProfit = F10Val.builder().val(xnhk0205.getF049n()).build(); //保留溢利
        F10Val totalReserves = F10Val.builder().val(xnhk0205.getF050n()).build(); //储备总额
        F10Val stockholdersEquity = F10Val.builder().val(xnhk0205.getF051n()).build(); //股东权益
        F10Val nonControllingInterests = F10Val.builder().val(xnhk0205.getF053n()).build(); //非控股权益
        F10Val otherEquityHolders = F10Val.builder().val(xnhk0205.getF052n()).build(); //其他权益持有人

        if (yoy != null) {
            //有上周期数据 即计算同比
            totalAssets.setYoy(calYoy(xnhk0205.getF027n(), yoy.getF027n()));
            cashAndShortTermFunds.setYoy(calYoy(xnhk0205.getF008n(), yoy.getF008n()));
            collectionOfItemsFromOtherBank.setYoy(calYoy(xnhk0205.getF009n(), yoy.getF009n()));
            depositsInInterbankAndOtherFinancialInstitutions.setYoy(calYoy(xnhk0205.getF010n(), yoy.getF010n()));
            hongKongGovermentCertificateOfIndebtedness.setYoy(calYoy(xnhk0205.getF011n(), yoy.getF011n()));
            commercialPaper.setYoy(calYoy(xnhk0205.getF012n(), yoy.getF012n()));
            assetsForTradingPurposes.setYoy(calYoy(xnhk0205.getF013n(), yoy.getF013n()));
            assetsForNonTradingPurposes.setYoy(calYoy(xnhk0205.getF014n(), yoy.getF014n()));
            gainAndLoseFinancialAssetsAtFairValue.setYoy(calYoy(xnhk0205.getF015n(), yoy.getF015n()));
            availableForSaleFinancialAssets.setYoy(calYoy(xnhk0205.getF016n(), yoy.getF016n()));
            derivativeFinancialAssets.setYoy(calYoy(xnhk0205.getF017n(), yoy.getF017n()));
            certificateOfDepositHeld.setYoy(calYoy(xnhk0205.getF018n(), yoy.getF018n()));
            interbankLoansAndAdvances.setYoy(calYoy(xnhk0205.getF019n(), yoy.getF019n()));
            customerLoansAndAdvances.setYoy(calYoy(xnhk0205.getF020n(), yoy.getF020n()));
            financialInvestment.setYoy(calYoy(xnhk0205.getF021n(), yoy.getF021n()));
            holdToMaturityInvestment.setYoy(calYoy(xnhk0205.getF022n(), yoy.getF022n()));
            jointVenturesAndJointVenturesInterets.setYoy(calYoy(xnhk0205.getF023n(), yoy.getF023n()));
            goodwillAndIntangibleAssets.setYoy(calYoy(xnhk0205.getF024n(), yoy.getF024n()));
            fiexedAssets.setYoy(calYoy(xnhk0205.getF025n(), yoy.getF025n()));
            otherAssets.setYoy(calYoy(xnhk0205.getF026n(), yoy.getF026n()));
            totalLiabilities.setYoy(calYoy(xnhk0205.getF042n(), yoy.getF042n()));
            hongKongGovernmentCirculatesPaperMoney.setYoy(calYoy(xnhk0205.getF028n(), yoy.getF028n()));
            itemsTransmittedToOtherBanks.setYoy(calYoy(xnhk0205.getF031n(), yoy.getF031n()));
            depositsOfInterbankAndFinancialInstitutionsLiabilities.setYoy(calYoy(xnhk0205.getF029n(), yoy.getF029n()));
            timeDepositsInterbankAndFinancialInstitutionsLiabilities.setYoy(calYoy(xnhk0205.getF032n(), yoy.getF032n()));
            customersDeposit.setYoy(calYoy(xnhk0205.getF030n(), yoy.getF030n()));
            certificatesOfDepositIssued.setYoy(calYoy(xnhk0205.getF034n(), yoy.getF034n()));
            bondsIssued.setYoy(calYoy(xnhk0205.getF033n(), yoy.getF033n()));
            convertibleBondsIssued.setYoy(calYoy(xnhk0205.getF035n(), yoy.getF035n()));
            floatingRateNotesIssued.setYoy(calYoy(xnhk0205.getF036n(), yoy.getF036n()));
            tradingLiabilities.setYoy(calYoy(xnhk0205.getF037n(), yoy.getF037n()));
            enteringProfitAndLossFinancialLiabilitiesAtFairValue.setYoy(calYoy(xnhk0205.getF038n(), yoy.getF038n()));
            derivativeFinancialLiability.setYoy(calYoy(xnhk0205.getF039n(), yoy.getF039n()));
            subordinatedDebt.setYoy(calYoy(xnhk0205.getF040n(), yoy.getF040n()));
            otherDebt.setYoy(calYoy(xnhk0205.getF041n(), yoy.getF041n()));
            netAssetValue.setYoy(calYoy(xnhk0205.getF064n(), yoy.getF064n()));
            totalEquity.setYoy(calYoy(xnhk0205.getF054n(), yoy.getF054n()));
            totalCapital.setYoy(calYoy(xnhk0205.getF045n(), yoy.getF045n()));
            capitalStockCommonStock.setYoy(calYoy(xnhk0205.getF043n(), yoy.getF043n()));
            capitalStockPreferredStock.setYoy(calYoy(xnhk0205.getF044n(), yoy.getF044n()));
            capitalStockPremium.setYoy(calYoy(xnhk0205.getF046n(), yoy.getF046n()));
            capitalReserve.setYoy(calYoy(xnhk0205.getF047n(), yoy.getF047n()));
            otherReserve.setYoy(calYoy(xnhk0205.getF048n(), yoy.getF048n()));
            retainedProfit.setYoy(calYoy(xnhk0205.getF049n(), yoy.getF049n()));
            totalReserves.setYoy(calYoy(xnhk0205.getF050n(), yoy.getF050n()));
            stockholdersEquity.setYoy(calYoy(xnhk0205.getF051n(), yoy.getF051n()));
            nonControllingInterests.setYoy(calYoy(xnhk0205.getF053n(), yoy.getF053n()));
            otherEquityHolders.setYoy(calYoy(xnhk0205.getF052n(), yoy.getF052n()));
        }
        f10AssetsLiabilitiesFinancialEntity.setTotalAssets(totalAssets);
        f10AssetsLiabilitiesFinancialEntity.setCashAndShortTermFunds(cashAndShortTermFunds);
        f10AssetsLiabilitiesFinancialEntity.setCollectionOfItemsFromOtherBank(collectionOfItemsFromOtherBank);
        f10AssetsLiabilitiesFinancialEntity.setDepositsInInterbankAndOtherFinancialInstitutions(depositsInInterbankAndOtherFinancialInstitutions);
        f10AssetsLiabilitiesFinancialEntity.setHongKongGovermentCertificateOfIndebtedness(hongKongGovermentCertificateOfIndebtedness);
        f10AssetsLiabilitiesFinancialEntity.setCommercialPaper(commercialPaper);
        f10AssetsLiabilitiesFinancialEntity.setAssetsForTradingPurposes(assetsForTradingPurposes);
        f10AssetsLiabilitiesFinancialEntity.setAssetsForNonTradingPurposes(assetsForNonTradingPurposes);
        f10AssetsLiabilitiesFinancialEntity.setGainAndLoseFinancialAssetsAtFairValue(gainAndLoseFinancialAssetsAtFairValue);
        f10AssetsLiabilitiesFinancialEntity.setAvailableForSaleFinancialAssets(availableForSaleFinancialAssets);
        f10AssetsLiabilitiesFinancialEntity.setDerivativeFinancialAssets(derivativeFinancialAssets);
        f10AssetsLiabilitiesFinancialEntity.setCertificateOfDepositHeld(certificateOfDepositHeld);
        f10AssetsLiabilitiesFinancialEntity.setInterbankLoansAndAdvances(interbankLoansAndAdvances);
        f10AssetsLiabilitiesFinancialEntity.setCustomerLoansAndAdvances(customerLoansAndAdvances);
        f10AssetsLiabilitiesFinancialEntity.setFinancialInvestment(financialInvestment);
        f10AssetsLiabilitiesFinancialEntity.setHoldToMaturityInvestment(holdToMaturityInvestment);
        f10AssetsLiabilitiesFinancialEntity.setJointVenturesAndJointVenturesInterets(jointVenturesAndJointVenturesInterets);
        f10AssetsLiabilitiesFinancialEntity.setGoodwillAndIntangibleAssets(goodwillAndIntangibleAssets);
        f10AssetsLiabilitiesFinancialEntity.setFiexedAssets(fiexedAssets);
        f10AssetsLiabilitiesFinancialEntity.setOtherAssets(otherAssets);
        f10AssetsLiabilitiesFinancialEntity.setTotalLiabilities(totalLiabilities);
        f10AssetsLiabilitiesFinancialEntity.setHongKongGovernmentCirculatesPaperMoney(hongKongGovernmentCirculatesPaperMoney);
        f10AssetsLiabilitiesFinancialEntity.setItemsTransmittedToOtherBanks(itemsTransmittedToOtherBanks);
        f10AssetsLiabilitiesFinancialEntity.setDepositsOfInterbankAndFinancialInstitutionsLiabilities(depositsOfInterbankAndFinancialInstitutionsLiabilities);
        f10AssetsLiabilitiesFinancialEntity.setTimeDepositsInterbankAndFinancialInstitutionsLiabilities(timeDepositsInterbankAndFinancialInstitutionsLiabilities);
        f10AssetsLiabilitiesFinancialEntity.setCustomersDeposit(customersDeposit);
        f10AssetsLiabilitiesFinancialEntity.setCertificatesOfDepositIssued(certificatesOfDepositIssued);
        f10AssetsLiabilitiesFinancialEntity.setBondsIssued(bondsIssued);
        f10AssetsLiabilitiesFinancialEntity.setConvertibleBondsIssued(convertibleBondsIssued);
        f10AssetsLiabilitiesFinancialEntity.setFloatingRateNotesIssued(floatingRateNotesIssued);
        f10AssetsLiabilitiesFinancialEntity.setTradingLiabilities(tradingLiabilities);
        f10AssetsLiabilitiesFinancialEntity.setEnteringProfitAndLossFinancialLiabilitiesAtFairValue(enteringProfitAndLossFinancialLiabilitiesAtFairValue);
        f10AssetsLiabilitiesFinancialEntity.setDerivativeFinancialLiability(derivativeFinancialLiability);
        f10AssetsLiabilitiesFinancialEntity.setSubordinatedDebt(subordinatedDebt);
        f10AssetsLiabilitiesFinancialEntity.setOtherDebt(otherDebt);
        f10AssetsLiabilitiesFinancialEntity.setNetAssetValue(netAssetValue);
        f10AssetsLiabilitiesFinancialEntity.setTotalEquity(totalEquity);
        f10AssetsLiabilitiesFinancialEntity.setTotalCapital(totalCapital);
        f10AssetsLiabilitiesFinancialEntity.setCapitalStockCommonStock(capitalStockCommonStock);
        f10AssetsLiabilitiesFinancialEntity.setCapitalStockPreferredStock(capitalStockPreferredStock);
        f10AssetsLiabilitiesFinancialEntity.setCapitalStockPremium(capitalStockPremium);
        f10AssetsLiabilitiesFinancialEntity.setCapitalReserve(capitalReserve);
        f10AssetsLiabilitiesFinancialEntity.setOtherReserve(otherReserve);
        f10AssetsLiabilitiesFinancialEntity.setRetainedProfit(retainedProfit);
        f10AssetsLiabilitiesFinancialEntity.setTotalReserves(totalReserves);
        f10AssetsLiabilitiesFinancialEntity.setStockholdersEquity(stockholdersEquity);
        f10AssetsLiabilitiesFinancialEntity.setNonControllingInterests(nonControllingInterests);
        f10AssetsLiabilitiesFinancialEntity.setOtherEquityHolders(otherEquityHolders);
    }


    private Xnhk0205 findYoyXnhk0205(Xnhk0205 xnhk0205, List<Xnhk0205> xnhk0205s) {
        return xnhk0205s.stream().filter(item->item.getF006v().equals(xnhk0205.getF006v())&&item.getF002d()<xnhk0205.getF002d()).max(Comparator.comparing(Xnhk0205::getF002d)).orElse(null);

    }


}
