package com.vv.finance.investment.bg.handler.uts.sync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.uts.Xnhk0202;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0202Mapper;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesInsuranceEntity;
import com.vv.finance.investment.bg.mongo.model.F10AssetsLiabilitiesNonFinancialEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/7/22 15:23
 * @Version 1.0
 * 资产负债-非金融
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class F10AssetsLiabilitiesNonFinancialHandler extends AbstractF10CommonHandler {

    private final Xnhk0202Mapper xnhk0202Mapper;


    @Override
    public void sync() {
        List<Xnhk0202> selectList = xnhk0202Mapper.selectList(new QueryWrapper<Xnhk0202>().select("SECCODE", "max(Modified_Date) Modified_Date").groupBy("seccode"));
        selectList.forEach(item -> {
            syncCheck(item.getSeccode(),item.getModifiedDate(), F10AssetsLiabilitiesNonFinancialEntity.class);
        });
    }

    @Override
    public void syncAll() {
        List<Xnhk0202> selectList = xnhk0202Mapper.selectList(new QueryWrapper<Xnhk0202>().select("SECCODE").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(), null, F10AssetsLiabilitiesNonFinancialEntity.class));
    }

    @Override
    public void doSync(String code, Date updateTime) {
        log.info("执行  F10AssetsLiabilitiesNonFinancialHandler {}",code);
        Map<String, F10AssetsLiabilitiesNonFinancialEntity> xnhk0202 = xnhk0202Sync(code, updateTime);
//        xnhk0202.forEach((k, v) -> save(v, F10AssetsLiabilitiesNonFinancialEntity.class));
        saveBulk(new ArrayList<>(xnhk0202.values()), F10AssetsLiabilitiesNonFinancialEntity.class);
    }

    private Map<String, F10AssetsLiabilitiesNonFinancialEntity> xnhk0202Sync(String code, Date updateTime) {
        List<Xnhk0202> xnhk0202s = xnhk0202Mapper.selectList(new QueryWrapper<Xnhk0202>().eq("seccode", code));
        //所有需要更新的数据条数
        List<Xnhk0202> xnhk0202Update = xnhk0202s.stream().filter(item -> {
            if (updateTime == null) {
                return true;
            }
            if (item.getModifiedDate() == null) {
                return true;
            }
            return updateTime.before(item.getModifiedDate());
        }).collect(Collectors.toList());
        xnhk0202Update = convertAndFilterReports(xnhk0202Update, Xnhk0202::getF006v, Xnhk0202::setF006v, Xnhk0202::getF007d);
        Map<String, Xnhk0202> collect = xnhk0202Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
        Map<String, F10AssetsLiabilitiesNonFinancialEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            try {
                F10AssetsLiabilitiesNonFinancialEntity assetsLiabilitiesNonFinancialEntity = new F10AssetsLiabilitiesNonFinancialEntity();
                assetsLiabilitiesNonFinancialEntity.setReportType(v.getF006v());
                assetsLiabilitiesNonFinancialEntity.setStockCode(v.getSeccode());
                assetsLiabilitiesNonFinancialEntity.setCurrency(v.getF003v());
                assetsLiabilitiesNonFinancialEntity.setReleaseDate(dateFormat(v.getF001d()));
                assetsLiabilitiesNonFinancialEntity.setReleaseTimestamp(dateStrToLong(assetsLiabilitiesNonFinancialEntity.getReleaseDate()));
                assetsLiabilitiesNonFinancialEntity.setEndDate(dateFormat(v.getF002d()));
                assetsLiabilitiesNonFinancialEntity.setEndTimestamp(dateStrToLong(assetsLiabilitiesNonFinancialEntity.getEndDate()));
                assetsLiabilitiesNonFinancialEntity.setStartDate(calStartDate(assetsLiabilitiesNonFinancialEntity.getEndDate(), v.getF007d()));
                assetsLiabilitiesNonFinancialEntity.setStartTimestamp(dateStrToLong(assetsLiabilitiesNonFinancialEntity.getStartDate()));
                assetsLiabilitiesNonFinancialEntity.setUpdateTime(v.getModifiedDate());
                assetsLiabilitiesNonFinancialEntity.setAuditOpinion(v.getF049v());
                //取出上个周期数据
                Xnhk0202 yoyXnhk0202 = findYoyXnhk0202(v, xnhk0202s);
                //计算同比并且添加进去
                addValAndYoy(assetsLiabilitiesNonFinancialEntity, v, yoyXnhk0202);
                result.put(k, assetsLiabilitiesNonFinancialEntity);
            } catch (Exception e) {
                log.error("xnhk0203Sync", e);
            }

        });
        return result;
    }

    private void addValAndYoy(F10AssetsLiabilitiesNonFinancialEntity f10AssetsLiabilitiesNonFinancialEntity, Xnhk0202 xnhk0202, Xnhk0202 yoy) {
        F10Val totalAssets = F10Val.builder().val(xnhk0202.getF019n()).build(); //总资产
        F10Val currentAsset = F10Val.builder().val(xnhk0202.getF018n()).build(); //流动资产
        F10Val inventory = F10Val.builder().val(xnhk0202.getF013n()).build(); //存货
        F10Val accountsReceivable = F10Val.builder().val(xnhk0202.getF014n()).build(); //应收款帐
        F10Val financialAssets = F10Val.builder().val(xnhk0202.getF015n()).build(); //金融资产
        F10Val cashAndBankBalances = F10Val.builder().val(xnhk0202.getF016n()).build(); //现金及银行结存
        F10Val otherCurrentAsset = F10Val.builder().val(xnhk0202.getF017n()).build(); //其他流动资产
        F10Val nonCurrentAssets = F10Val.builder().val(xnhk0202.getF012n()).build(); //非流动资产
        F10Val fixedAssets = F10Val.builder().val(xnhk0202.getF008n()).build(); //固定资产
        F10Val investment = F10Val.builder().val(xnhk0202.getF009n()).build(); //投资
        F10Val goodwillAndIntangibleAssets = F10Val.builder().val(xnhk0202.getF010n()).build(); //商誉及无形资产
        F10Val otherNonCurrentAssets = F10Val.builder().val(xnhk0202.getF011n()).build(); //其他非流动资产

        F10Val totalLiabilities = F10Val.builder().val(xnhk0202.getF047n()).build(); //总负债
        F10Val currentLiabilities = F10Val.builder().val(xnhk0202.getF023n()).build(); //流动负债
        F10Val accountsPayable = F10Val.builder().val(xnhk0202.getF020n()).build(); //应付账款
        F10Val shortTermLiabilities = F10Val.builder().val(xnhk0202.getF021n()).build(); //短期债项
        F10Val otherShortTermLiabilities = F10Val.builder().val(xnhk0202.getF022n()).build(); //其他短期负债
        F10Val nonCurrentLiability = F10Val.builder().val(xnhk0202.getF026n()).build(); //非流动负债
        F10Val longTermLiabilities = F10Val.builder().val(xnhk0202.getF024n()).build(); //长期债项
        F10Val otherNonCurrentLiabilities = F10Val.builder().val(xnhk0202.getF025n()).build(); //其他非流动负债

        F10Val netAssetValue = F10Val.builder().val(xnhk0202.getF046n()).build(); //资产净值
        F10Val totalEquityAndNonCurrentLiabilities = F10Val.builder().val(xnhk0202.getF045n()).build(); //总权益及非流动负债
        F10Val totalEquity = F10Val.builder().val(xnhk0202.getF038n()).build(); //总权益
        F10Val totalCapital = F10Val.builder().val(xnhk0202.getF029n()).build(); //股本总额
        F10Val capitalStockCommonStock = F10Val.builder().val(xnhk0202.getF027n()).build(); //股本（普通股）
        F10Val capitalStockPreferredStock = F10Val.builder().val(xnhk0202.getF028n()).build(); //股本（优先股）
        F10Val capitalStockPremium = F10Val.builder().val(xnhk0202.getF030n()).build(); //股本溢价
        F10Val capitalReserve = F10Val.builder().val(xnhk0202.getF031n()).build(); //资本储备
        F10Val otherReserve = F10Val.builder().val(xnhk0202.getF032n()).build(); //其他储备
        F10Val retainedProfit = F10Val.builder().val(xnhk0202.getF033n()).build(); //保留溢利
        F10Val totalReserves = F10Val.builder().val(xnhk0202.getF034n()).build(); //储备总额

        F10Val stockholdersEquity = F10Val.builder().val(xnhk0202.getF035n()).build(); //股东权益
        F10Val nonControllingInterests = F10Val.builder().val(xnhk0202.getF037n()).build(); //非控股权益
        F10Val otherEquityHolders = F10Val.builder().val(xnhk0202.getF036n()).build(); //其他权益持有人

        if (yoy != null) {
            //有上周期数据 即计算同比
            totalAssets.setYoy(calYoy(xnhk0202.getF019n(), yoy.getF019n()));
            currentAsset.setYoy(calYoy(xnhk0202.getF018n(), yoy.getF018n()));
            inventory.setYoy(calYoy(xnhk0202.getF013n(), yoy.getF013n()));
            accountsReceivable.setYoy(calYoy(xnhk0202.getF014n(), yoy.getF014n()));
            financialAssets.setYoy(calYoy(xnhk0202.getF015n(), yoy.getF015n()));
            cashAndBankBalances.setYoy(calYoy(xnhk0202.getF016n(), yoy.getF016n()));
            otherCurrentAsset.setYoy(calYoy(xnhk0202.getF017n(), yoy.getF017n()));
            nonCurrentAssets.setYoy(calYoy(xnhk0202.getF012n(), yoy.getF012n()));
            fixedAssets.setYoy(calYoy(xnhk0202.getF008n(), yoy.getF008n()));
            investment.setYoy(calYoy(xnhk0202.getF009n(), yoy.getF009n()));
            goodwillAndIntangibleAssets.setYoy(calYoy(xnhk0202.getF010n(), yoy.getF010n()));
            otherNonCurrentAssets.setYoy(calYoy(xnhk0202.getF011n(), yoy.getF011n()));

            totalLiabilities.setYoy(calYoy(xnhk0202.getF047n(), yoy.getF047n()));
            currentLiabilities.setYoy(calYoy(xnhk0202.getF023n(), yoy.getF023n()));
            accountsPayable.setYoy(calYoy(xnhk0202.getF020n(), yoy.getF020n()));
            shortTermLiabilities.setYoy(calYoy(xnhk0202.getF021n(), yoy.getF021n()));
            otherShortTermLiabilities.setYoy(calYoy(xnhk0202.getF022n(), yoy.getF022n()));
            nonCurrentLiability.setYoy(calYoy(xnhk0202.getF026n(), yoy.getF026n()));
            longTermLiabilities.setYoy(calYoy(xnhk0202.getF024n(), yoy.getF024n()));
            otherNonCurrentLiabilities.setYoy(calYoy(xnhk0202.getF025n(), yoy.getF025n()));

            netAssetValue.setYoy(calYoy(xnhk0202.getF046n(), yoy.getF046n()));
            totalEquityAndNonCurrentLiabilities.setYoy(calYoy(xnhk0202.getF045n(), yoy.getF045n()));
            totalEquity.setYoy(calYoy(xnhk0202.getF038n(), yoy.getF038n()));
            totalCapital.setYoy(calYoy(xnhk0202.getF029n(), yoy.getF029n()));
            capitalStockCommonStock.setYoy(calYoy(xnhk0202.getF027n(), yoy.getF027n()));
            capitalStockPreferredStock.setYoy(calYoy(xnhk0202.getF028n(), yoy.getF028n()));
            capitalStockPremium.setYoy(calYoy(xnhk0202.getF030n(), yoy.getF030n()));
            capitalReserve.setYoy(calYoy(xnhk0202.getF031n(), yoy.getF031n()));
            otherReserve.setYoy(calYoy(xnhk0202.getF032n(), yoy.getF032n()));
            retainedProfit.setYoy(calYoy(xnhk0202.getF033n(), yoy.getF033n()));
            totalReserves.setYoy(calYoy(xnhk0202.getF034n(), yoy.getF034n()));

            stockholdersEquity.setYoy(calYoy(xnhk0202.getF035n(), yoy.getF035n()));
            nonControllingInterests.setYoy(calYoy(xnhk0202.getF037n(), yoy.getF037n()));
            otherEquityHolders.setYoy(calYoy(xnhk0202.getF036n(), yoy.getF036n()));
        }
        f10AssetsLiabilitiesNonFinancialEntity.setTotalAssets(totalAssets);
        f10AssetsLiabilitiesNonFinancialEntity.setCurrentAsset(currentAsset);
        f10AssetsLiabilitiesNonFinancialEntity.setInventory(inventory);
        f10AssetsLiabilitiesNonFinancialEntity.setAccountsReceivable(accountsReceivable);
        f10AssetsLiabilitiesNonFinancialEntity.setFinancialAssets(financialAssets);
        f10AssetsLiabilitiesNonFinancialEntity.setCashAndBankBalances(cashAndBankBalances);
        f10AssetsLiabilitiesNonFinancialEntity.setOtherCurrentAsset(otherCurrentAsset);
        f10AssetsLiabilitiesNonFinancialEntity.setNonCurrentAssets(nonCurrentAssets);
        f10AssetsLiabilitiesNonFinancialEntity.setFixedAssets(fixedAssets);
        f10AssetsLiabilitiesNonFinancialEntity.setInvestment(investment);
        f10AssetsLiabilitiesNonFinancialEntity.setGoodwillAndIntangibleAssets(goodwillAndIntangibleAssets);
        f10AssetsLiabilitiesNonFinancialEntity.setOtherNonCurrentAssets(otherNonCurrentAssets);

        f10AssetsLiabilitiesNonFinancialEntity.setTotalLiabilities(totalLiabilities);
        f10AssetsLiabilitiesNonFinancialEntity.setCurrentLiabilities(currentLiabilities);
        f10AssetsLiabilitiesNonFinancialEntity.setAccountsPayable(accountsPayable);
        f10AssetsLiabilitiesNonFinancialEntity.setShortTermLiabilities(shortTermLiabilities);
        f10AssetsLiabilitiesNonFinancialEntity.setOtherShortTermLiabilities(otherShortTermLiabilities);
        f10AssetsLiabilitiesNonFinancialEntity.setNonCurrentLiability(nonCurrentLiability);
        f10AssetsLiabilitiesNonFinancialEntity.setLongTermLiabilities(longTermLiabilities);
        f10AssetsLiabilitiesNonFinancialEntity.setOtherNonCurrentLiabilities(otherNonCurrentLiabilities);
        f10AssetsLiabilitiesNonFinancialEntity.setNetAssetValue(netAssetValue);
        f10AssetsLiabilitiesNonFinancialEntity.setTotalEquityAndNonCurrentLiabilities(totalEquityAndNonCurrentLiabilities);
        f10AssetsLiabilitiesNonFinancialEntity.setTotalEquity(totalEquity);
        f10AssetsLiabilitiesNonFinancialEntity.setTotalCapital(totalCapital);
        f10AssetsLiabilitiesNonFinancialEntity.setCapitalStockCommonStock(capitalStockCommonStock);
        f10AssetsLiabilitiesNonFinancialEntity.setCapitalStockPreferredStock(capitalStockPreferredStock);
        f10AssetsLiabilitiesNonFinancialEntity.setCapitalStockPremium(capitalStockPremium);
        f10AssetsLiabilitiesNonFinancialEntity.setCapitalReserve(capitalReserve);
        f10AssetsLiabilitiesNonFinancialEntity.setOtherReserve(otherReserve);
        f10AssetsLiabilitiesNonFinancialEntity.setRetainedProfit(retainedProfit);
        f10AssetsLiabilitiesNonFinancialEntity.setTotalReserves(totalReserves);
        f10AssetsLiabilitiesNonFinancialEntity.setStockholdersEquity(stockholdersEquity);
        f10AssetsLiabilitiesNonFinancialEntity.setNonControllingInterests(nonControllingInterests);
        f10AssetsLiabilitiesNonFinancialEntity.setOtherEquityHolders(otherEquityHolders);
    }


    private Xnhk0202 findYoyXnhk0202(Xnhk0202 xnhk0202, List<Xnhk0202> xnhk0202s) {
        return xnhk0202s.stream().filter(item->item.getF006v().equals(xnhk0202.getF006v())&&item.getF002d()<xnhk0202.getF002d()).max(Comparator.comparing(Xnhk0202::getF002d)).orElse(null);

    }

}
