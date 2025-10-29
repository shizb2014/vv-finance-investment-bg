package com.vv.finance.investment.bg.handler.uts.sync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.vv.finance.investment.bg.constants.ReportTypeEnum;
import com.vv.finance.investment.bg.entity.f10.F10Val;
import com.vv.finance.investment.bg.entity.f10.nonfinancial.*;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.mongo.model.F10KeyFiguresNonFinancialEntity;
import com.vv.finance.investment.bg.utils.CollectUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/7/20 14:20
 * 非金融--主要指标摘要数据（图表、估值分析数据）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class F10KeyFiguresNonFinancialHandler extends AbstractF10CommonHandler {

    private final Xnhk0203Mapper xnhk0203Mapper;
    private final Xnhk0201Mapper xnhk0201Mapper;
    private final Xnhk0202Mapper xnhk0202Mapper;
    private final Xnhk0210Mapper xnhk0210Mapper;
    private final Xnhk0118Mapper xnhk0118Mapper;
    private final Xnhk0102Mapper xnhk0102Mapper;

    @Override
    public void sync() {
        List<Xnhk0203> selectList = xnhk0203Mapper.selectList(new QueryWrapper<Xnhk0203>().select("SECCODE", "max(Modified_Date) Modified_Date").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(), item.getModifiedDate(), F10KeyFiguresNonFinancialEntity.class));
    }

    @Override
    public void syncAll() {
        List<Xnhk0203> selectList = xnhk0203Mapper.selectList(new QueryWrapper<Xnhk0203>().select("SECCODE").groupBy("seccode"));
        selectList.forEach(item -> syncCheck(item.getSeccode(), null, F10KeyFiguresNonFinancialEntity.class));
    }

    @Override
    @SneakyThrows
    public void doSync(String code, Date updateTime) {
        log.info("执行F10KeyFiguresNonFinancialHandler处理股票代码：{}", code);
        CompletableFuture<Map<String, F10KeyFiguresNonFinancialEntity>> xnhk0203Future = CompletableFuture.supplyAsync(() -> xnhk0203Sync(code, updateTime));
        CompletableFuture<Map<String, F10KeyFiguresNonFinancialEntity>> xnhk0201Future = CompletableFuture.supplyAsync(() -> xnhk0201Sync(code, updateTime));
        Map<String, F10KeyFiguresNonFinancialEntity> xnhk0203 = xnhk0203Future.get();
        Map<String, F10KeyFiguresNonFinancialEntity> xnhk0201 = xnhk0201Future.get();

        List<F10KeyFiguresNonFinancialEntity> lists = new ArrayList<>();
        xnhk0203.forEach((k, v) -> {
            F10KeyFiguresNonFinancialEntity xnhk0201Val = xnhk0201.get(k);
            if (xnhk0201Val != null) {
                v.setKeyFigures(xnhk0201Val.getKeyFigures());
                v.setGrowthAbility(xnhk0201Val.getGrowthAbility());
                if (Objects.nonNull(xnhk0201Val.getCashFlowIndicator())) {
                    v.setCashFlowIndicator(xnhk0201Val.getCashFlowIndicator());
                }
                if (Objects.nonNull(xnhk0201Val.getCostProfitability())) {
                    v.setCostProfitability(xnhk0201Val.getCostProfitability());
                }

            }

            lists.add(v);
//            save(v,F10KeyFiguresNonFinancialEntity.class);
        });
        saveBulk(lists, F10KeyFiguresNonFinancialEntity.class);

    }

    private Map<String, F10KeyFiguresNonFinancialEntity> xnhk0203Sync(String code, Date updateTime) {
        List<Xnhk0203> xnhk0203s = xnhk0203Mapper.selectList(new QueryWrapper<Xnhk0203>().eq("seccode", code));
        List<Xnhk0203> xnhk0203Update = xnhk0203s.stream().filter(item -> {
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
        xnhk0203Update = convertAndFilterReports(xnhk0203Update, Xnhk0203::getF006v, Xnhk0203::setF006v, Xnhk0203::getF007d);
        Map<String, Xnhk0203> collect = xnhk0203Update.stream().collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
        //资产负债表（非金融）XNHK0202
        List<Xnhk0202> xnhk0202s = xnhk0202Mapper.selectList(new QueryWrapper<Xnhk0202>().eq("seccode", code));
        //现金流量表
        List<Xnhk0210> xnhk0210s = xnhk0210Mapper.selectList(new QueryWrapper<Xnhk0210>().eq("seccode", code));
        //利润分配表（非金融）XNHK0201
        List<Xnhk0201> xnhk0201s = xnhk0201Mapper.selectList(new QueryWrapper<Xnhk0201>().eq("seccode", code));
        //十年财务摘要(非金融) XNHK0118
        List<Xnhk0118> xnhk0118s = xnhk0118Mapper.selectList(new QueryWrapper<Xnhk0118>().eq("seccode", code));
        //股票基本数据 XNHK0102
        Xnhk0102 xnhk0102 = xnhk0102Mapper.selectOne(new QueryWrapper<Xnhk0102>().eq("seccode", code));

        List<Long> reportTime = xnhk0203Update.stream().map(item -> item.getF002d()).collect(Collectors.toList());

        Map<String, Xnhk0202> xnhk0202Map = getXnhk0202Map(xnhk0202s,reportTime);
        Map<String, Xnhk0210> xnhk0210Map = getXnhk0210Map(xnhk0210s,reportTime);
        Map<String, Xnhk0201> xnhk02021Map = getXnhk0201Map(xnhk0201s, reportTime);

        // 股东权益周转率构建map 年份、报表类型 对应的记录
        Map<String, Map<String,List<Xnhk0202>>> xnhk0202MapResult = getXnhk0202MapResult(xnhk0202s);

        // 企业价值倍数构建map 按照年分组
        Map<String, List<Xnhk0118>> xnhk0118Map = getXnhk0118MapResult(xnhk0118s);

        Map<String, F10KeyFiguresNonFinancialEntity> result = Maps.newConcurrentMap();
        collect.forEach((k, v) -> {
            try {
                F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity = new F10KeyFiguresNonFinancialEntity();
                figuresNonFinancialEntity.setReportType(v.getF006v());
                figuresNonFinancialEntity.setStockCode(v.getSeccode());
                figuresNonFinancialEntity.setCurrency(v.getF003v());
                figuresNonFinancialEntity.setReleaseDate(dateFormat(v.getF001d()));
                figuresNonFinancialEntity.setReleaseTimestamp(dateStrToLong(figuresNonFinancialEntity.getReleaseDate()));
                figuresNonFinancialEntity.setEndDate(dateFormat(v.getF002d()));
                figuresNonFinancialEntity.setEndTimestamp(dateStrToLong(figuresNonFinancialEntity.getEndDate()));
                figuresNonFinancialEntity.setStartDate(calStartDate(figuresNonFinancialEntity.getEndDate(), v.getF007d()));
                figuresNonFinancialEntity.setStartTimestamp(dateStrToLong(figuresNonFinancialEntity.getStartDate()));
                figuresNonFinancialEntity.setUpdateTime(v.getModifiedDate());

                Xnhk0201 xnhk0201 = xnhk02021Map.get(k);
                Xnhk0202 xnhk0202 = xnhk0202Map.get(k);
                Xnhk0210 xnhk0210 = xnhk0210Map.get(k);

                Xnhk0203 yoyXnhk0203 = findYoyXnhk0203(v, xnhk0203s);
                Xnhk0202 yoyXnhk0202 = findYoyXnhk0202(xnhk0202, xnhk0202s);
                Xnhk0210 yoyXnhk0210 = findYoyXnhk0210(xnhk0210, xnhk0210s);
                Xnhk0201 yoyXnhk0201 = findYoyXnhk0201(xnhk0201,xnhk0201s);
                // 盈利能力
                profitabilityCal(figuresNonFinancialEntity, v, yoyXnhk0203);
                // V1.5.8 盈利能力扩展
                profitabilityExtCal(figuresNonFinancialEntity,xnhk0201,yoyXnhk0201,xnhk0202,yoyXnhk0202);

                // 运营能力
                operatingCapacityCal(figuresNonFinancialEntity, v, yoyXnhk0203);
                //偿债能力
                solvencyCal(figuresNonFinancialEntity, v, yoyXnhk0203);

                // V1.5.8 财务分析偿债能力
                if(Objects.nonNull(xnhk0202) && Objects.nonNull(xnhk0210)){
                    solvencyCalExt(figuresNonFinancialEntity,xnhk0202,xnhk0210,yoyXnhk0202,yoyXnhk0210);
                }

                perShareIndicator(figuresNonFinancialEntity, v, yoyXnhk0203);
                cashability(figuresNonFinancialEntity, v, yoyXnhk0203);
                // V1.5.8 企业价值倍数
                if(Objects.nonNull(xnhk0118Map) && Objects.nonNull(xnhk0201) && Objects.nonNull(xnhk0202)){
                    evebitdaCal(figuresNonFinancialEntity,xnhk0118Map,xnhk0202,xnhk0201);
                }
                // V1.5.8 股东权益周转率,资产回报率和资产周转率
                if(Objects.nonNull(xnhk0202MapResult) && Objects.nonNull(xnhk0201) && Objects.nonNull(xnhk0202)) {
                    equityTurnover(figuresNonFinancialEntity, xnhk0202, xnhk0201, xnhk0202MapResult);
                    // roaCal(figuresNonFinancialEntity, xnhk0202, xnhk0201, xnhk0202MapResult);
                    // calculateTotalAssetsTurnover(figuresNonFinancialEntity, xnhk0202, xnhk0201, xnhk0202MapResult);
                }
                result.put(k, figuresNonFinancialEntity);
            } catch (Exception e) {
                log.error("异常股票代码：{}", code);
                log.error("xnhk0203Sync", e);
            }

        });
        return result;
    }

    /**
     * 盈利能力扩展
     * @param figuresNonFinancialEntity
     * @param xnhk0201
     * @param yoyXnhk0201
     */
    private void profitabilityExtCal(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity,
                                     Xnhk0201 xnhk0201, Xnhk0201 yoyXnhk0201,
                                     Xnhk0202 xnhk0202, Xnhk0202 yoyXnhk0202
                                     ) {
        ProfitabilityNonFinancial profitability = figuresNonFinancialEntity.getProfitability();
        if(Objects.isNull(profitability)){
            profitability = ProfitabilityNonFinancial.builder().build();
        }
        //销售净利率
        F10Val npoms = F10Val.builder().build();
        //todo 修改后的净利润率、股东权益回报率、资本回报率公式
        //净利润率
        F10Val netProfitRatio = F10Val.builder().build();
        //股东权益回报率
        F10Val roe = F10Val.builder().build();
        //资本回报率
        F10Val roce = F10Val.builder().build();

        if(Objects.nonNull(xnhk0201)){
            BigDecimal npomsVal =  percentage(calcCashFour(xnhk0201.getF030n(),xnhk0201.getF008n()));
            npoms.setVal(npomsVal);
            if(Objects.nonNull(yoyXnhk0201)){
                BigDecimal yoyNpomsVal = percentage(calcCashFour(yoyXnhk0201.getF030n(),yoyXnhk0201.getF008n()));
                npoms.setYoy(calYoy(npomsVal,yoyNpomsVal));
            }
            profitability.setNpoms(npoms);

            BigDecimal netProfitRatioVal = percentage(calcCashFour(xnhk0201.getF030n(),xnhk0201.getF010n()));
            netProfitRatio.setVal(netProfitRatioVal);
            if (Objects.nonNull(yoyXnhk0201)){
                BigDecimal yoyNetProfitRatioVal = percentage(calcCashFour(yoyXnhk0201.getF030n(),yoyXnhk0201.getF010n()));
                netProfitRatio.setYoy(calYoy(netProfitRatioVal,yoyNetProfitRatioVal));
            }
            profitability.setNetProfitRatio(netProfitRatio);
        }

        if(Objects.nonNull(xnhk0201) && Objects.nonNull(xnhk0202)){
            BigDecimal roeVal = percentage(calcCashFour(xnhk0201.getF031n(),xnhk0202.getF038n()));
            // roe.setVal(roeVal);
            if(Objects.nonNull(yoyXnhk0201) && Objects.nonNull(yoyXnhk0202)){
                BigDecimal yoyRoeVal = percentage(calcCashFour(yoyXnhk0201.getF031n(),yoyXnhk0202.getF038n()));
                // roe.setYoy(calYoy(roeVal,yoyRoeVal));
            }
            // profitability.setRoe(roe);

        }

        figuresNonFinancialEntity.setProfitability(profitability);
    }


    /**
     * 资产周转率计算方法
     * @param figuresNonFinancialEntity
     * @param xnhk0202
     * @param xnhk0201
     * @param xnhk0202MapResult
     */
    private void calculateTotalAssetsTurnover(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Xnhk0202 xnhk0202, Xnhk0201 xnhk0201, Map<String, Map<String, List<Xnhk0202>>> xnhk0202MapResult){

        //获取期初总资产
        BigDecimal beginTotalAssets = getBeginTotalAssets(figuresNonFinancialEntity,xnhk0202MapResult);

        if(Objects.isNull(beginTotalAssets) || Objects.isNull(xnhk0201.getF008n()) || Objects.isNull(xnhk0202.getF019n())){
            return;
        }
        //平均资产
        BigDecimal averageTotalAssets = (xnhk0202.getF019n().add(beginTotalAssets)).divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);
        //将值转换成F10Val
        BigDecimal totalAssetsTurnoverVal = calcCash(xnhk0201.getF008n(), averageTotalAssets);
        F10Val totalAssetsTurnover=F10Val.builder().val(totalAssetsTurnoverVal).build();
        //通过figuresNonFinancialEntity获取到该对象的operatingCapacity
        // OperatingCapacityNonFinancial operatingCapacity = figuresNonFinancialEntity.getOperatingCapacity();
        // if(Objects.isNull(operatingCapacity)){
        //     operatingCapacity=OperatingCapacityNonFinancial.builder().build();
        // }
        // //将计算的值
        // operatingCapacity.setTotalAssetsTurnover(totalAssetsTurnover);
        // figuresNonFinancialEntity.setOperatingCapacity(operatingCapacity);
    }

    /**
     * 资产回报率    12个月的净利润/12个月平均资产总额*100%
     * @param figuresNonFinancialEntity
     * @param xnhk0202
     * @param xnhk0201
     * @param xnhk0202MapResult
     */
    private void roaCal(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Xnhk0202 xnhk0202, Xnhk0201 xnhk0201, Map<String, Map<String, List<Xnhk0202>>> xnhk0202MapResult){

        //获取期初总资产
        BigDecimal beginTotalAssets = getBeginTotalAssets(figuresNonFinancialEntity,xnhk0202MapResult);

        if(Objects.isNull(beginTotalAssets) || Objects.isNull(xnhk0201.getF030n()) || Objects.isNull(xnhk0202.getF019n())){
            return;
        }
        //平均资产
        BigDecimal averageTotalAssets = (xnhk0202.getF019n().add(beginTotalAssets)).divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);
        BigDecimal roaVal = percentage(calcCashFour(xnhk0201.getF030n(), averageTotalAssets));
        F10Val roa = F10Val.builder().val(roaVal).build();

        // ProfitabilityNonFinancial profitability = figuresNonFinancialEntity.getProfitability();
        // if(Objects.isNull(profitability)){
        //     profitability = ProfitabilityNonFinancial.builder().build();
        // }
        // profitability.setRoa(roa);
        // figuresNonFinancialEntity.setProfitability(profitability);
    }

    /**
     * 根据传入的时间和报表类型获取到期初总资产
     * @param figuresNonFinancialEntity
     * @param xnhk0202MapResult
     * @return
     */
    public BigDecimal getBeginTotalAssets(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Map<String, Map<String, List<Xnhk0202>>> xnhk0202MapResult) {

        Map<String, List<Xnhk0202>> xn = xnhk0202MapResult.get(String.valueOf(Integer.valueOf(figuresNonFinancialEntity.getEndDate().substring(0, 4)) - 1));

        if (Objects.isNull(xn) || xn.size() <= 0) {
            return null;
        }

        Xnhk0202 xnhk0202Q1 = xn.get(ReportTypeEnum.Q1.getCode()) == null ? null : xn.get(ReportTypeEnum.Q1.getCode()).get(0);
        Xnhk0202 xnhk0202I = xn.get(ReportTypeEnum.I.getCode()) == null ? null : xn.get(ReportTypeEnum.I.getCode()).get(0);
        Xnhk0202 xnhk0202Q3 = xn.get(ReportTypeEnum.Q3.getCode()) == null ? null : xn.get(ReportTypeEnum.Q3.getCode()).get(0);
        Xnhk0202 xnhk0202F = xn.get(ReportTypeEnum.F.getCode()) == null ? null : xn.get(ReportTypeEnum.F.getCode()).get(0);

        if (ReportTypeEnum.Q1.getCode().equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报减去上一年的三季度报
            if (Objects.isNull(xnhk0202F) || Objects.isNull(xnhk0202Q3) || Objects.isNull(xnhk0202F.getF019n()) || Objects.isNull(xnhk0202Q3.getF019n())) {
                return null;
            }
            return xnhk0202F.getF019n().subtract(xnhk0202Q3.getF019n());
        } else if (ReportTypeEnum.I.getCode().equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报减去上一年的中报
            if (Objects.isNull(xnhk0202F) || Objects.isNull(xnhk0202I) || Objects.isNull(xnhk0202F.getF019n()) || Objects.isNull(xnhk0202I.getF019n())) {
                return null;
            }
            return xnhk0202F.getF019n().subtract(xnhk0202I.getF019n());
        } else if (ReportTypeEnum.Q3.getCode().equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报减去上一年的Q1
            if (Objects.isNull(xnhk0202F) || Objects.isNull(xnhk0202Q1) || Objects.isNull(xnhk0202F.getF019n()) || Objects.isNull(xnhk0202Q1.getF019n())) {
                return null;
            }
            return xnhk0202F.getF019n().subtract(xnhk0202Q1.getF019n());
        } else if (ReportTypeEnum.F.getCode().equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报
            if (Objects.isNull(xnhk0202F) || Objects.isNull(xnhk0202F.getF019n())) {
                return null;
            }
            return xnhk0202F.getF019n();
        } else {
            return null;
        }
    }

    /**
     * 企业价值倍数
     * 公式：（市值+总负债-现金和现金等价物）/营业利润
     * @param figuresNonFinancialEntity
     * @param xnhk0118Map
     * @param xnhk0202
     * @param xnhk0201
     */
    private void evebitdaCal(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Map<String, List<Xnhk0118>> xnhk0118Map, Xnhk0202 xnhk0202, Xnhk0201 xnhk0201) {

        //只有年报才可以计算企业价值倍数
        if (!ReportTypeEnum.F.getCode().equals(figuresNonFinancialEntity.getReportType())) {
            return;
        }

        List<Xnhk0118> xn = xnhk0118Map.get(figuresNonFinancialEntity.getEndDate().substring(0, 4));

        if (CollectionUtils.isEmpty(xn) || Objects.isNull(xn.get(0))) {
            return;
        }

        if (Objects.isNull(xn.get(0).getF044n()) || Objects.isNull(xnhk0202.getF047n()) || Objects.isNull(xnhk0202.getF016n())) {
            return;
        }
        //市值+总负债-现金和现金等价物
        BigDecimal ev = (xn.get(0).getF044n().add(xnhk0202.getF047n())).subtract(xnhk0202.getF016n());

        //企业价值倍数
        BigDecimal evebitda = calcCash(ev, xnhk0201.getF024n());

        ProfitabilityNonFinancial profitabilityNonFinancial = figuresNonFinancialEntity.getProfitability();

        if(Objects.isNull(profitabilityNonFinancial)){
            profitabilityNonFinancial =new ProfitabilityNonFinancial();
        }
        profitabilityNonFinancial.setEvebitda(evebitda);

        figuresNonFinancialEntity.setProfitability(profitabilityNonFinancial);

    }

    /**
     * 股东权益周转率
     * 公式：销售收入 / 平均股东权益
     * @param figuresNonFinancialEntity
     * @param xnhk0201
     */
    private void equityTurnover(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Xnhk0202 xnhk0202, Xnhk0201 xnhk0201, Map<String, Map<String, List<Xnhk0202>>> xnhk0202MapResult) {

        //根据 报告类型 分季报，中报，年报 计算平均股东权益 ==== I 中报，F 年报， Q1， Q3

        //获取期初股东权益
        BigDecimal beginEquity = getBeginEquity(figuresNonFinancialEntity, xnhk0202MapResult);

        if (Objects.isNull(beginEquity) || Objects.isNull(xnhk0202.getF035n()) || Objects.isNull(xnhk0201.getF008n())) {
            return;
        }
        //平均股东权益
        BigDecimal averageEquity = (xnhk0202.getF035n().add(beginEquity)).divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);

        BigDecimal toseVal = percentage(calcCashFour(xnhk0201.getF008n(), averageEquity));
        F10Val tose = F10Val.builder().val(toseVal).build();

        OperatingCapacityNonFinancial operatingCapacityNonFinancial = figuresNonFinancialEntity.getOperatingCapacity();
        if(Objects.isNull(operatingCapacityNonFinancial)){
            operatingCapacityNonFinancial = new OperatingCapacityNonFinancial();
        }
        operatingCapacityNonFinancial.setTose(tose);
        figuresNonFinancialEntity.setOperatingCapacity(operatingCapacityNonFinancial);
    }

    /**
     * 根据传入的时间和报表类型获取到期初股东权益
     * @param figuresNonFinancialEntity
     * @return
     */
    public BigDecimal getBeginEquity(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity) {

        //上一年的报表种类
        List<Xnhk0202> xnhk0202s = xnhk0202Mapper.selectList(
                new QueryWrapper<Xnhk0202>()
                        .eq("seccode", figuresNonFinancialEntity.getStockCode())
                        .apply("left(F002D,4) = '" + (Integer.valueOf(figuresNonFinancialEntity.getEndDate().substring(0, 4)) - 1) + "'"));

        if (CollectionUtils.isEmpty(xnhk0202s)) {
            return null;
        }

        Xnhk0202 xnhk0202Q1 = new Xnhk0202();
        Xnhk0202 xnhk0202I = new Xnhk0202();
        Xnhk0202 xnhk0202Q3 = new Xnhk0202();
        Xnhk0202 xnhk0202F = new Xnhk0202();
        for (Xnhk0202 xnhk0202 : xnhk0202s) {
            if (ReportTypeEnum.Q1.equals(xnhk0202.getF006v())) {
                xnhk0202Q1 = xnhk0202;
            } else if (ReportTypeEnum.I.equals(xnhk0202.getF006v())) {
                xnhk0202I = xnhk0202;
            } else if (ReportTypeEnum.Q3.equals(xnhk0202.getF006v())) {
                xnhk0202Q3 = xnhk0202;
            } else if (ReportTypeEnum.F.equals(xnhk0202.getF006v())) {
                xnhk0202F = xnhk0202;
            }
        }
        if (ReportTypeEnum.Q1.equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报减去上一年的三季度报
            if (Objects.isNull(xnhk0202F.getF035n()) || Objects.isNull(xnhk0202Q3.getF035n())) {
                return null;
            }
            return xnhk0202F.getF035n().subtract(xnhk0202Q3.getF035n());
        } else if (ReportTypeEnum.I.equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报减去上一年的中报
            if (Objects.isNull(xnhk0202F.getF035n()) || Objects.isNull(xnhk0202I.getF035n())) {
                return null;
            }
            return xnhk0202F.getF035n().subtract(xnhk0202I.getF035n());
        } else if (ReportTypeEnum.Q3.equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报减去上一年的Q1
            if (Objects.isNull(xnhk0202F.getF035n()) || Objects.isNull(xnhk0202Q1.getF035n())) {
                return null;
            }
            return xnhk0202F.getF035n().subtract(xnhk0202Q1.getF035n());
        } else if (ReportTypeEnum.F.equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报
            if (Objects.isNull(xnhk0202F.getF035n())) {
                return null;
            }
            return xnhk0202F.getF035n();
        } else {
            return null;
        }

    }

    /**
     * 根据传入的时间和报表类型获取到期初股东权益
     *
     * @param figuresNonFinancialEntity
     * @return
     */
    public BigDecimal getBeginEquity(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Map<String, Map<String, List<Xnhk0202>>> xnhk0202MapResult) {

        Map<String, List<Xnhk0202>> xn = xnhk0202MapResult.get(String.valueOf(Integer.valueOf(figuresNonFinancialEntity.getEndDate().substring(0, 4)) - 1));

        if (Objects.isNull(xn) || xn.size() <= 0) {
            return null;
        }

        Xnhk0202 xnhk0202Q1 = xn.get(ReportTypeEnum.Q1.getCode()) == null ? null : xn.get(ReportTypeEnum.Q1.getCode()).get(0);
        Xnhk0202 xnhk0202I = xn.get(ReportTypeEnum.I.getCode()) == null ? null : xn.get(ReportTypeEnum.I.getCode()).get(0);
        Xnhk0202 xnhk0202Q3 = xn.get(ReportTypeEnum.Q3.getCode()) == null ? null : xn.get(ReportTypeEnum.Q3.getCode()).get(0);
        Xnhk0202 xnhk0202F = xn.get(ReportTypeEnum.F.getCode()) == null ? null : xn.get(ReportTypeEnum.F.getCode()).get(0);

        if (ReportTypeEnum.Q1.getCode().equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报减去上一年的三季度报
            if (Objects.isNull(xnhk0202F) || Objects.isNull(xnhk0202Q3)) {
                return null;
            }

            if (Objects.isNull(xnhk0202F.getF035n()) || Objects.isNull(xnhk0202Q3.getF035n())) {
                return null;
            }
            return xnhk0202F.getF035n().subtract(xnhk0202Q3.getF035n());
        } else if (ReportTypeEnum.I.getCode().equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报减去上一年的中报
            if (Objects.isNull(xnhk0202F) || Objects.isNull(xnhk0202I)) {
                return null;
            }

            if (Objects.isNull(xnhk0202F.getF035n()) || Objects.isNull(xnhk0202I.getF035n())) {
                return null;
            }
            return xnhk0202F.getF035n().subtract(xnhk0202I.getF035n());
        } else if (ReportTypeEnum.Q3.getCode().equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报减去上一年的Q1
            if (Objects.isNull(xnhk0202F) || Objects.isNull(xnhk0202Q1)) {
                return null;
            }

            if (Objects.isNull(xnhk0202F.getF035n()) || Objects.isNull(xnhk0202Q1.getF035n())) {
                return null;
            }
            return xnhk0202F.getF035n().subtract(xnhk0202Q1.getF035n());
        } else if (ReportTypeEnum.F.getCode().equals(figuresNonFinancialEntity.getReportType())) {
            //获取上一年的年报
            if (Objects.isNull(xnhk0202F)) {
                return null;
            }

            if (Objects.isNull(xnhk0202F.getF035n())) {
                return null;
            }
            return xnhk0202F.getF035n();
        } else {
            return null;
        }
    }

    private Map<String, F10KeyFiguresNonFinancialEntity> xnhk0201Sync(String code, Date updateTime) {
        List<Xnhk0201> xnhk0201s = xnhk0201Mapper.selectList(new QueryWrapper<Xnhk0201>().eq("seccode", code));
        List<Xnhk0210> xnhk0210s = xnhk0210Mapper.selectList(new QueryWrapper<Xnhk0210>().eq("seccode", code));
        List<Xnhk0201> xnhk0201Update = xnhk0201s.stream().filter(item -> {
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
        xnhk0201Update = convertAndFilterReports(xnhk0201Update, Xnhk0201::getF006v, Xnhk0201::setF006v, Xnhk0201::getF007d);
        Map<String, Xnhk0201> xnhk0201Map = xnhk0201Update.stream().
                collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));

        List<Long> reportTime = xnhk0201Update.stream().map(item -> item.getF002d()).collect(Collectors.toList());
        Map<String, Xnhk0210> xnhk0210Map = getXnhk0210Map(xnhk0210s, reportTime);
        Map<String, F10KeyFiguresNonFinancialEntity> result = Maps.newConcurrentMap();
        xnhk0201Map.forEach((k, v) -> {
            F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity = new F10KeyFiguresNonFinancialEntity();

            // V1.5.8计算现金流指标
            Xnhk0210 xnhk0210 = xnhk0210Map.get(k);
            Xnhk0210 xnhk02101 = xnhk0210Map.get(k);
            Xnhk0210 yoyXnhk0210 = findYoyXnhk0210(xnhk02101, xnhk0210s);
            Xnhk0201 yoyXnhk0201 = findYoyXnhk0201(v, xnhk0201s);
            Xnhk0201 lastYoyXnhk0201 = findYoyXnhk0201(yoyXnhk0201, xnhk0201s);
            if(Objects.nonNull(xnhk0210) ){
                cashFlowIndexCal(figuresNonFinancialEntity,v,yoyXnhk0201,yoyXnhk0210,xnhk0210);
            }

            // V1.5.8 计算成本盈利能力
            costProfitability(figuresNonFinancialEntity,v,yoyXnhk0201);

            growthAbilityCal(figuresNonFinancialEntity, v, yoyXnhk0201, lastYoyXnhk0201);
            keyFiguresCal(figuresNonFinancialEntity, v, yoyXnhk0201);
            result.put(k, figuresNonFinancialEntity);
        });
        return result;
    }

    /**
     * 计算成本盈利能力
     * @param figuresNonFinancialEntity
     * @param xnhk0201
     * @param yoyXnhk0201
     */
    private void costProfitability(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Xnhk0201 xnhk0201, Xnhk0201 yoyXnhk0201) {
        /**
         * 成本费用利润率
         */
        BigDecimal costFeeVal = costFee(xnhk0201.getF014n(), xnhk0201.getF015n(),
                xnhk0201.getF025n(),xnhk0201.getF028n());
        BigDecimal costProfitMarginVal = percentage(calcCashFour(xnhk0201.getF035n(),costFeeVal));
        F10Val costProfitMargin = F10Val.builder().val(costProfitMarginVal).build()  ;
        /**
         * 成本费用率
         */
        BigDecimal costRateVal = percentage(calcCashFour(xnhk0201.getF035n(),xnhk0201.getF012n()));
        F10Val costRate = F10Val.builder().val(costRateVal).build();
        /**
         * 主营成本占营收比率
         */
        BigDecimal majorCostRatioVal = percentage(calcCashFour(xnhk0201.getF012n(),xnhk0201.getF008n()));
        F10Val majorCostRatio = F10Val.builder().val(majorCostRatioVal).build();
        /**
         * 销售费用占比
         */
        BigDecimal salesExpenseRatioVal = percentage(calcCashFour(xnhk0201.getF014n(),xnhk0201.getF008n()));
        F10Val salesExpenseRatio = F10Val.builder().val(salesExpenseRatioVal).build();
        /**
         * 行政费用占比
         */
        BigDecimal administrativeCostsRatioVal = percentage(calcCashFour(xnhk0201.getF015n(),xnhk0201.getF008n()));
        F10Val administrativeCostsRatio = F10Val.builder().val(administrativeCostsRatioVal).build();
        /**
         * 财务成本占比
         */
        BigDecimal financialCostsRatioVal = percentage(calcCashFour(xnhk0201.getF025n(),xnhk0201.getF008n()));
        F10Val financialCostsRatio = F10Val.builder().val(financialCostsRatioVal).build();
        //计算同比
        if(Objects.nonNull(yoyXnhk0201)){
            BigDecimal yoyCostFeeVal = costFee(yoyXnhk0201.getF014n(), yoyXnhk0201.getF015n(),
                    yoyXnhk0201.getF025n(),yoyXnhk0201.getF028n());
            BigDecimal yoyCostProfitMarginVal = calcCash(yoyXnhk0201.getF035n(),yoyCostFeeVal);
            costProfitMargin.setYoy(calYoy(costProfitMarginVal,yoyCostProfitMarginVal));

            BigDecimal yoyCostRateVal = calcCash(yoyXnhk0201.getF035n(),yoyXnhk0201.getF012n());
            costRate.setYoy(calYoy(costRateVal,yoyCostRateVal));

            BigDecimal yoyMajorCostRatioVal = calcCash(yoyXnhk0201.getF012n(),yoyXnhk0201.getF008n());
            majorCostRatio.setYoy(calYoy(majorCostRatioVal,yoyMajorCostRatioVal));

            BigDecimal yoySalesExpenseRatioVal = calcCash(yoyXnhk0201.getF014n(),yoyXnhk0201.getF008n());
            salesExpenseRatio.setYoy(calYoy(salesExpenseRatioVal,yoySalesExpenseRatioVal));

            BigDecimal yoyAdministrativeCostsRatioVal = calcCash(yoyXnhk0201.getF015n(),yoyXnhk0201.getF008n());
            administrativeCostsRatio.setYoy(calYoy(administrativeCostsRatioVal,yoyAdministrativeCostsRatioVal));
        }

        CostProfitability costProfitability = CostProfitability
                .builder()
                .costProfitMargin(costProfitMargin)
                .costRate(costRate)
                .financialCostsRatio(financialCostsRatio)
                .salesExpenseRatio(salesExpenseRatio)
                .majorCostRatio(majorCostRatio)
                .administrativeCostsRatio(administrativeCostsRatio)
                .build();

        figuresNonFinancialEntity.setCostProfitability(costProfitability);
    }

    /**
     *
     * @param f014n
     * @param f015n
     * @param f025n
     * @param f028n
     * @return
     */
    private BigDecimal costFee(BigDecimal f014n, BigDecimal f015n,
                               BigDecimal f025n, BigDecimal f028n){
        if(Objects.isNull(f014n) || Objects.isNull(f015n) ||
                Objects.isNull(f025n) || Objects.isNull(f028n)){
            return null;
        }
        return f014n.add(f015n).add(f025n).add(f028n);
    }

    /**
     * 计算现金流量指标
     * @param figuresNonFinancialEntity
     * @param xnhk0201
     * @param yoyXnhk0201
     * @param yoyXnhk0210
     * @param xnhk0210
     */
    private void cashFlowIndexCal(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity,
                                  Xnhk0201 xnhk0201,Xnhk0201 yoyXnhk0201,
                                  Xnhk0210 yoyXnhk0210, Xnhk0210 xnhk0210) {

        /**
         * 指标：销售现金比率(%)
         */
        BigDecimal salesCashRatioVal = percentage(calcCashFour(xnhk0210.getF008n(),xnhk0201.getF008n()));
        F10Val salesCashRatio = F10Val.builder().val(salesCashRatioVal).build();

        /**
         * 指标：经营现金净流量/利润总额 (%)
         */
        BigDecimal netOperatingCashFlowAndTotalProfitVal = percentage(calcCashFour(xnhk0210.getF008n(),xnhk0201.getF027n()));
        F10Val netOperatingCashFlowAndTotalProfit = F10Val.builder().val(netOperatingCashFlowAndTotalProfitVal).build();

        /**
         * 经营现金净流量/营业总收入
         */
         BigDecimal netOperatingCashFlowAndGrossOperatingIncomeVal = percentage(calcCashFour(xnhk0210.getF008n(),xnhk0201.getF010n()));
         F10Val netOperatingCashFlowAndGrossOperatingIncome = F10Val.builder().val(netOperatingCashFlowAndGrossOperatingIncomeVal).build();

        /**
         * 净利润现金含量
         */
        BigDecimal netProfitCashContentVal = percentage(calcCashFour(xnhk0210.getF008n(),xnhk0201.getF030n()));
        F10Val netProfitCashContent = F10Val.builder().val(netProfitCashContentVal).build();

        /**
         * 营收收入现金含量
         */
        BigDecimal cashContentRevenueIncomeVal = percentage(calcCashFour(xnhk0210.getF008n(),xnhk0201.getF008n()));
        F10Val cashContentRevenueIncome = F10Val.builder().val(cashContentRevenueIncomeVal).build();


        // 计算同比
        if(Objects.nonNull(yoyXnhk0210) && Objects.nonNull(yoyXnhk0201)){
            BigDecimal yoySalesCashRatioVal = percentage(calcCash(yoyXnhk0210.getF008n(),yoyXnhk0201.getF008n()));
            BigDecimal yoyNetOperatingCashFlowAndTotalProfitVal = percentage(calcCash(yoyXnhk0210.getF008n(),yoyXnhk0201.getF027n()));
            BigDecimal yoyNetOperatingCashFlowAndGrossOperatingIncomeVal = percentage(calcCash(yoyXnhk0210.getF008n(),yoyXnhk0201.getF010n()));
            BigDecimal yoyNetProfitCashContentVal = percentage(calcCash(yoyXnhk0210.getF008n(),yoyXnhk0201.getF030n()));
            BigDecimal yoyCashContentRevenueIncomeVal = percentage(calcCash(yoyXnhk0210.getF008n(),yoyXnhk0201.getF008n()));
            salesCashRatio.setYoy(calYoy(salesCashRatioVal,yoySalesCashRatioVal));
            netOperatingCashFlowAndTotalProfit.setYoy(calYoy(netOperatingCashFlowAndTotalProfitVal,yoyNetOperatingCashFlowAndTotalProfitVal));
            netOperatingCashFlowAndGrossOperatingIncome.setYoy(calYoy(netOperatingCashFlowAndGrossOperatingIncomeVal,yoyNetOperatingCashFlowAndGrossOperatingIncomeVal));
            netProfitCashContent.setYoy(calYoy(netProfitCashContentVal,yoyNetProfitCashContentVal));
            cashContentRevenueIncome.setYoy(calYoy(cashContentRevenueIncomeVal,yoyCashContentRevenueIncomeVal));
        }
        figuresNonFinancialEntity.setCashFlowIndicator(CashFlowIndicator
                .builder()
                .salesCashRatio(salesCashRatio)
                .netOperatingCashFlowAndTotalProfit(netOperatingCashFlowAndTotalProfit)
                .netOperatingCashFlowAndGrossOperatingIncome(netOperatingCashFlowAndGrossOperatingIncome)
                .netProfitCashContent(netProfitCashContent)
                .cashContentRevenueIncome(cashContentRevenueIncome)
                .build());
    }


    /**
     *  xnhk0202与xnhk0210计算获取偿债能力
     * @param xnhk0202s
     * @param reportTime
     * @return
     */
    private Map<String, Xnhk0202> getXnhk0202Map( List<Xnhk0202> xnhk0202s,List<Long> reportTime){
//        List<Xnhk0202> xnhk0202Update = xnhk0202s.stream().filter(item -> {
//            // 以前未同步到MongoDB,所有数据需要同步
//            if (updateTime == null) {
//                return true;
//            }
//            if (item.getModifiedDate() == null) {
//                return false;
//            }
//            // MongoDB更新的时间小于uts数据库数据记录修改日期,则需要同步满足这条件的数据
//            return updateTime.before(item.getModifiedDate());
//
//        }).collect(Collectors.toList());
        List<Xnhk0202> xnhk0202Update = xnhk0202s.stream().filter(item -> {
            if (reportTime == null) {
                return false;
            }
            // 取出与需要更新的数据 时间相同的报表
            return reportTime.contains(item.getF002d());
        }).collect(Collectors.toList());
        return xnhk0202Update.stream().
                collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
    }

    /**
     * 股东权益周转率构建map：现根据年分组，然后根据报表类型分组
     * @param xnhk0202s
     * @return
     */
    private Map<String, Map<String,List<Xnhk0202>>> getXnhk0202MapResult(List<Xnhk0202> xnhk0202s){

        return xnhk0202s.stream().collect(Collectors.groupingBy((xnhk0202->{
            log.info("xnhk0202.getF002d  :{}" , xnhk0202.getF002d());
            return (String.valueOf(xnhk0202.getF002d())).substring(0,4);
        }),Collectors.groupingBy(Xnhk0202::getF006v)));

    }

    /**
     * 企业价值倍数构建map 按照年分组
     * @param xnhk0118s
     * @return
     */
    private Map<String, List<Xnhk0118>> getXnhk0118MapResult(List<Xnhk0118> xnhk0118s){

        return xnhk0118s.stream().collect(Collectors.groupingBy((xnhk0118->{
            return String.valueOf(xnhk0118.getF001d()).substring(0,4);
        })));
    }


    private Map<String, Xnhk0210> getXnhk0210Map(List<Xnhk0210> xnhk0210s, List<Long> reportTime) {
//        List<Xnhk0210> xnhk0210Update = xnhk0210s.stream().filter(item -> {
//            // 以前未同步到MongoDB,所有数据需要同步
//            if (updateTime == null) {
//                return true;
//            }
//            if (item.getModifiedDate() == null) {
//                return false;
//            }
//            // MongoDB更新的时间小于uts数据库数据记录修改日期,则需要同步满足这条件的数据
//            return updateTime.before(item.getModifiedDate());
//
//        }).collect(Collectors.toList());
        List<Xnhk0210> xnhk0210Update = xnhk0210s.stream().filter(item -> {
            if (reportTime == null) {
                return false;
            }
            // 取出与需要更新的数据 时间相同的报表
            return reportTime.contains(item.getF002d());
        }).collect(Collectors.toList());
        return xnhk0210Update.stream().
                collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
    }

    private Map<String, Xnhk0201> getXnhk0201Map( List<Xnhk0201> xnhk0201s,List<Long> reportTime){
//        List<Xnhk0201> xnhk020Update = xnhk0201s.stream().filter(item -> {
//            // 以前未同步到MongoDB,所有数据需要同步
//            if (updateTime == null) {
//                return true;
//            }
//            if (item.getModifiedDate() == null) {
//                return false;
//            }
//            // MongoDB更新的时间小于uts数据库数据记录修改日期,则需要同步满足这条件的数据
//            return updateTime.before(item.getModifiedDate());
//
//        }).collect(Collectors.toList());
        List<Xnhk0201> xnhk0201Update = xnhk0201s.stream().filter(item -> {
            if (reportTime == null) {
                return false;
            }
            // 取出与需要更新的数据 时间相同的报表
            return reportTime.contains(item.getF002d());
        }).collect(Collectors.toList());
        return xnhk0201Update.stream().
                collect(Collectors.toMap(item -> buildKey(item.getF001d(), item.getF002d(), item.getF006v()), Function.identity()));
    }

    /**
     * 更新数据，构建key
     * @param xnhk0118s
     * @param updateTime
     * @return
     */
    private Map<String, Xnhk0118> getXnhk0118Map( List<Xnhk0118> xnhk0118s,Date updateTime){
        List<Xnhk0118> xnhk0118Update = xnhk0118s.stream().filter(item -> {
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
        return null;
    }

    /**
     * 变现能力
     *
     * @param figuresNonFinancialEntity
     * @param xnhk0203
     * @param yoy
     */
    private void cashability(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Xnhk0203 xnhk0203, Xnhk0203 yoy) {
        /**
         * 流动比率
         */
        F10Val currentRatio = F10Val.builder().val(xnhk0203.getF028n()).build();
        /**
         * 速动比率
         */
        F10Val quickRatio = F10Val.builder().val(xnhk0203.getF029n()).build();

        /**
         * 现金比率
         */
        F10Val cashRatio = F10Val.builder().val(xnhk0203.getF030n()).build();

        /**
         * 营业现金流比率
         */
        F10Val operatingCashFlowRatio = F10Val.builder().val(xnhk0203.getF031n()).build();

        if (yoy != null) {
            currentRatio.setYoy(calYoy(xnhk0203.getF028n(), yoy.getF028n()));
            quickRatio.setYoy(calYoy(xnhk0203.getF029n(), yoy.getF029n()));
            cashRatio.setYoy(calYoy(xnhk0203.getF030n(), yoy.getF030n()));
            operatingCashFlowRatio.setYoy(calYoy(xnhk0203.getF031n(), yoy.getF031n()));
        }

        CashabilityNonFinancial cashabilityNonFinancial = CashabilityNonFinancial.builder()
                .cashRatio(cashRatio)
                .currentRatio(currentRatio)
                .quickRatio(quickRatio)
                .operatingCashFlowRatio(operatingCashFlowRatio)
                .build();
        figuresNonFinancialEntity.setCashability(cashabilityNonFinancial);
    }

    /**
     * 每股指标
     *
     * @param figuresNonFinancialEntity
     * @param xnhk0203
     * @param yoy
     */
    private void perShareIndicator(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Xnhk0203 xnhk0203, Xnhk0203 yoy) {
        BigDecimal f004n = xnhk0203.getF004n();
        F10Val earningPerShare = F10Val.builder().val(divideRate(xnhk0203.getF067n(), f004n)).build();
        F10Val netAssetPerShare = F10Val.builder().val(divideRate(xnhk0203.getF068n(), f004n)).build();

        F10Val cashFlowPerShare = F10Val.builder().val(divideRate(xnhk0203.getF069n(), f004n)).build();

        F10Val cashPerShare = F10Val.builder().val(divideRate(xnhk0203.getF070n(), f004n)).build();

        F10Val liabilityPerShare = F10Val.builder().val(divideRate(xnhk0203.getF071n(), f004n)).build();
        if (yoy != null) {
            BigDecimal yoyF004n = yoy.getF004n();
            earningPerShare.setYoy(calYoy(earningPerShare.getVal(), divideRate(yoy.getF067n(), yoyF004n)));
            netAssetPerShare.setYoy(calYoy(netAssetPerShare.getVal(), divideRate(yoy.getF068n(), yoyF004n)));
            cashFlowPerShare.setYoy(calYoy(cashFlowPerShare.getVal(), divideRate(yoy.getF069n(), yoyF004n)));
            cashPerShare.setYoy(calYoy(cashPerShare.getVal(), divideRate(yoy.getF070n(), yoyF004n)));
            liabilityPerShare.setYoy(calYoy(liabilityPerShare.getVal(), divideRate(yoy.getF071n(), yoyF004n)));
        }

        PerShareIndicatorNonFinancial perShareIndicatorNonFinancial = PerShareIndicatorNonFinancial.builder()
                .earningPerShare(earningPerShare)
                .netAssetPerShare(netAssetPerShare)
                .cashFlowPerShare(cashFlowPerShare)
                .cashPerShare(cashPerShare)
                .liabilityPerShare(liabilityPerShare)
                .build();
        figuresNonFinancialEntity.setPerShareIndicator(perShareIndicatorNonFinancial);

    }

    /**
     * 偿债能力
     *
     * @param figuresNonFinancialEntity
     * @param xnhk0203
     * @param yoy
     */
    private void solvencyCal(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Xnhk0203 xnhk0203, Xnhk0203 yoy) {
        /**
         * 总负债/总资产
         */
        F10Val totalLiabilityAssets = F10Val.builder().val(xnhk0203.getF038n()).build();
        /**
         *总负债/资本运用
         */
        F10Val totalLiabilityCapitalEmployed = F10Val.builder().val(xnhk0203.getF045n()).build();
        /**
         * 总负债/股东权益
         */
        F10Val totalLiabilityStockholderEquity = F10Val.builder().val(xnhk0203.getF043n()).build();
        /**
         * 总负债/权益总额
         */
        F10Val totalLiabilityEquity = F10Val.builder().val(xnhk0203.getF044n()).build();

        /**
         * 长期债务/总资产
         */
        F10Val ltLiabilityTotalAssets = F10Val.builder().val(xnhk0203.getF039n()).build();

        /**
         * 长期债务/股东权益
         */
        F10Val ltLiabilityStockholderEquity = F10Val.builder().val(xnhk0203.getF040n()).build();

        /**
         * 长期债务/权益总额
         */
        F10Val ltLiabilityEquityTotal = F10Val.builder().val(xnhk0203.getF041n()).build();
        /**
         *负债比率
         */
        F10Val assetLiabilityRatio = F10Val.builder().val(xnhk0203.getF042n()).build();
        /**
         *净负债/总资产
         */
        F10Val netLiabilityTotalAssets = F10Val.builder().val(xnhk0203.getF046n()).build();
        /**
         * 净负债/股东权益
         */
        F10Val netLiabilityStockholderEquity = F10Val.builder().val(xnhk0203.getF047n()).build();

        /**
         * 净负债/权益总额
         */
        F10Val netLiabilityTotalEquity = F10Val.builder().val(xnhk0203.getF048n()).build();

        /**
         * 股东权益/总资产
         */
        F10Val stockholderEquityTotalAssets = F10Val.builder().val(xnhk0203.getF037n()).build();

        /**
         * 产权比率
         */
        F10Val equityRatio = F10Val.builder().val(xnhk0203.getF044n()).build();
        /**
         * 权益乘数 = =1+产权比率(为null处理)
         */
        boolean isNull = Objects.isNull(xnhk0203.getF044n());
        //todo 产权比率经过百分比处理，计算权益乘数时应先除以100(done)
        F10Val equityMultiplier = F10Val.builder().val(isNull ? xnhk0203.getF044n() : xnhk0203.getF044n().divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).add(BigDecimal.ONE)).build();
        /**
         * 利息保障倍数
         */
        F10Val interestCoverageRatio = F10Val.builder().val(xnhk0203.getF050n()).build();


        if (yoy != null) {
            totalLiabilityAssets.setYoy(calYoy(xnhk0203.getF038n(), yoy.getF038n()));

            totalLiabilityCapitalEmployed.setYoy(calYoy(xnhk0203.getF045n(), yoy.getF045n()));

            totalLiabilityStockholderEquity.setYoy(calYoy(xnhk0203.getF043n(), yoy.getF043n()));

            totalLiabilityEquity.setYoy(calYoy(xnhk0203.getF044n(), yoy.getF044n()));

            ltLiabilityTotalAssets.setYoy(calYoy(xnhk0203.getF039n(), yoy.getF039n()));

            ltLiabilityStockholderEquity.setYoy(calYoy(xnhk0203.getF040n(), yoy.getF040n()));
            ltLiabilityEquityTotal.setYoy(calYoy(xnhk0203.getF041n(), yoy.getF041n()));
            assetLiabilityRatio.setYoy(calYoy(xnhk0203.getF042n(), yoy.getF042n()));
            netLiabilityTotalAssets.setYoy(calYoy(xnhk0203.getF046n(), yoy.getF046n()));
            netLiabilityStockholderEquity.setYoy(calYoy(xnhk0203.getF047n(), yoy.getF047n()));
            netLiabilityTotalEquity.setYoy(calYoy(xnhk0203.getF048n(), yoy.getF048n()));

            stockholderEquityTotalAssets.setYoy(calYoy(xnhk0203.getF037n(), yoy.getF037n()));
            // V1.5.8财务分析优化
            equityRatio.setYoy(calYoy(xnhk0203.getF044n(), yoy.getF044n()));
            Boolean yoyIsNull = isNull || Objects.isNull(yoy.getF044n());
            BigDecimal equityMultiplierYoy = yoyIsNull ? null : calYoy(xnhk0203.getF044n().divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).add(BigDecimal.ONE), yoy.getF044n().divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).add(BigDecimal.ONE));
            equityMultiplier.setYoy(equityMultiplierYoy);
            interestCoverageRatio.setYoy(calYoy(xnhk0203.getF050n(), yoy.getF050n()));
        }

        SolvencyNonFinancial solvencyNonFinancial = SolvencyNonFinancial.builder()
                .totalLiabilityAssets(totalLiabilityAssets)
                .totalLiabilityCapitalEmployed(totalLiabilityCapitalEmployed)
                .totalLiabilityStockholderEquity(totalLiabilityStockholderEquity)
                .totalLiabilityEquity(totalLiabilityEquity)
                .ltLiabilityTotalAssets(ltLiabilityTotalAssets)
                .ltLiabilityStockholderEquity(ltLiabilityStockholderEquity)
                .ltLiabilityEquityTotal(ltLiabilityEquityTotal)
                .assetLiabilityRatio(assetLiabilityRatio)
                .netLiabilityTotalAssets(netLiabilityTotalAssets)
                .netLiabilityStockholderEquity(netLiabilityStockholderEquity)
                .netLiabilityTotalEquity(netLiabilityTotalEquity)
                .stockholderEquityTotalAssets(stockholderEquityTotalAssets)
                .equityRatio(equityRatio)
                .equityMultiplier(equityMultiplier)
                .interestCoverageRatio(interestCoverageRatio)
                .build();
        figuresNonFinancialEntity.setSolvency(solvencyNonFinancial);
    }

    /**
     * 计算偿债能力新增字段
     * @param figuresNonFinancialEntity
     * @param xnhk0202
     * @param xnhk0210
     * @param yoyXnhk0202
     * @param yoyXnhk0210
     */
    public void solvencyCalExt(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity,
                               Xnhk0202 xnhk0202,Xnhk0210 xnhk0210,
                               Xnhk0202 yoyXnhk0202,Xnhk0210 yoyXnhk0210 ){

        SolvencyNonFinancial solvency = figuresNonFinancialEntity.getSolvency();
        if(Objects.isNull(solvency) ){
            solvency = new SolvencyNonFinancial();
        }
        /**
         * 偿债保障率
         */
        BigDecimal debtCoverageRatioVal = calcCash(xnhk0202.getF047n(), xnhk0210.getF008n());
        F10Val debtCoverageRatio= F10Val.builder().val(debtCoverageRatioVal).build();
        /**
         * 经营现金流量比率(长期)
         */
        BigDecimal operatingCashFlowRatioVal = percentage(calcCashFour(xnhk0210.getF008n(), xnhk0202.getF024n()));
        F10Val operatingCashFlowRatio= F10Val.builder().val(operatingCashFlowRatioVal).build();
        /**
         * 净现金流量比率(长期)
         */
        BigDecimal netCashFlowGearingRatioVal = percentage(calcCashFour(xnhk0210.getF008n(), xnhk0202.getF047n()));
        F10Val netCashFlowGearingRatio= F10Val.builder().val(netCashFlowGearingRatioVal).build();

        // 计算同比
        if (Objects.nonNull(yoyXnhk0202) && Objects.nonNull(yoyXnhk0210)) {
            BigDecimal yoyDebtCoverageRatioVal = calcCash(yoyXnhk0202.getF047n(), yoyXnhk0210.getF008n());
            BigDecimal yoyOperatingCashFlowRatioVal = calcCash(yoyXnhk0210.getF008n(), yoyXnhk0202.getF024n());
            BigDecimal yoyNetCashFlowGearingRatioVal = calcCash(yoyXnhk0210.getF008n(), yoyXnhk0202.getF047n());
            debtCoverageRatio.setYoy(calYoy(debtCoverageRatioVal,yoyDebtCoverageRatioVal));
            operatingCashFlowRatio.setYoy(calYoy(operatingCashFlowRatioVal,yoyOperatingCashFlowRatioVal));
            netCashFlowGearingRatio.setYoy(calYoy(netCashFlowGearingRatioVal,yoyNetCashFlowGearingRatioVal));
        }
        // 设置值
        solvency.setDebtCoverageRatio(debtCoverageRatio);
        solvency.setOperatingCashFlowRatio(operatingCashFlowRatio);
        solvency.setNetCashFlowGearingRatio(netCashFlowGearingRatio);

        figuresNonFinancialEntity.setSolvency(solvency);
    }

    /**
     * 运营能力
     *
     * @param figuresNonFinancialEntity
     * @param xnhk0203
     * @param yoy
     */
    private void operatingCapacityCal(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Xnhk0203 xnhk0203, Xnhk0203 yoy) {
        // 20240305资产周转率
        F10Val totalAssetsTurnover = F10Val.builder().val(xnhk0203.getF032n()).build();
        //修改资产周转率
        /**
         * 流动资金周转率
         */
        F10Val currentAssetsTurnover = F10Val.builder().val(xnhk0203.getF033n()).build();
        /**
         * 存货转换周期
         */
        F10Val inventoryTurnover = F10Val.builder().val(buildBigDecimal(xnhk0203.getF034n())).build();
        /**
         * 应收账转换周期
         */
        F10Val accountsReceivableTurnover = F10Val.builder().val(buildBigDecimal(xnhk0203.getF035n())).build();

        /**
         * 应付账转换周期
         */
        F10Val accountsPayableTurnover = F10Val.builder().val(buildBigDecimal(xnhk0203.getF036n())).build();

        /**
         * 存货周转率
         */
        BigDecimal constVal = buildBigDecimal(360L);
        //todo 存货周转率无需进行百分比处理(done)
        BigDecimal itrVal = calcCash(constVal,buildBigDecimal(xnhk0203.getF034n()));
        F10Val itr = F10Val.builder().val(itrVal).build();
        /**
         * 应收款周转率
         */
        BigDecimal toarVal = calcCash(constVal, buildBigDecimal(xnhk0203.getF035n()));
        F10Val toar = F10Val.builder().val(toarVal).build();

        /**
         * 应付款周转率
         */
        BigDecimal troapVal = calcCash(constVal, buildBigDecimal(xnhk0203.getF036n()));
        F10Val troap = F10Val.builder().val(troapVal).build();

        if (yoy != null) {
            totalAssetsTurnover.setYoy(calYoy(xnhk0203.getF032n(), yoy.getF032n()));
            currentAssetsTurnover.setYoy(calYoy(xnhk0203.getF033n(), yoy.getF033n()));
            inventoryTurnover.setYoy(calYoy(inventoryTurnover.getVal(), buildBigDecimal(yoy.getF034n())));
            accountsReceivableTurnover.setYoy(calYoy(accountsReceivableTurnover.getVal(), buildBigDecimal(yoy.getF035n())));
            accountsPayableTurnover.setYoy(calYoy(accountsPayableTurnover.getVal(), buildBigDecimal(yoy.getF036n())));

            BigDecimal yoyItrVal = calcCash(constVal, buildBigDecimal(yoy.getF034n()));
            itr.setYoy(calYoy(itrVal,yoyItrVal));
            BigDecimal yoyToarVal = calcCash(constVal, buildBigDecimal(yoy.getF035n()));
            toar.setYoy(calYoy(toarVal,yoyToarVal));
            BigDecimal yoyTroapVal = calcCash(constVal, buildBigDecimal(yoy.getF036n()));
            troap.setYoy(calYoy(troapVal,yoyTroapVal));

        }
        OperatingCapacityNonFinancial operatingCapacityNonFinancial = OperatingCapacityNonFinancial.builder()
                .totalAssetsTurnover(totalAssetsTurnover)
                .currentAssetsTurnover(currentAssetsTurnover)
                .inventoryTurnover(inventoryTurnover)
                .accountsReceivableTurnover(accountsReceivableTurnover)
                .accountsPayableTurnover(accountsPayableTurnover)
                .itr(itr)
                .toar(toar)
                .troap(troap)
                .build();
        figuresNonFinancialEntity.setOperatingCapacity(operatingCapacityNonFinancial);
    }

    private BigDecimal buildBigDecimal(Long val) {
        if (val == null) {
            return null;
        }
        return new BigDecimal(val);
    }

    /**
     * 成长能力
     *
     * @param figuresNonFinancialEntity
     * @param xnhk0201
     * @param yoy
     */
    private void growthAbilityCal(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Xnhk0201 xnhk0201, Xnhk0201 yoy, Xnhk0201 lastYoy) {
        if (yoy == null) {
            return;
        }
        List<Xnhk0202> xnhk0202s = xnhk0202Mapper.selectList(new QueryWrapper<Xnhk0202>().eq("seccode", xnhk0201.getSeccode()).
                eq("f006v", xnhk0201.getF006v()).le("f002d", xnhk0201.getF002d()).orderByDesc("f002d").last("limit 3"));
        F10Val totalAssetsGrowth = F10Val.builder().build();
        if (xnhk0202s != null && xnhk0202s.size() > 1) {
            Xnhk0202 xnhk0202 = xnhk0202s.get(0);
            Xnhk0202 xnhk0202Yoy = xnhk0202s.get(1);
            totalAssetsGrowth.setVal(calYoy(xnhk0202.getF019n(), xnhk0202Yoy.getF019n()));
            if (xnhk0202s.size() > 2) {
                Xnhk0202 xnhk0202LastYoy = xnhk0202s.get(2);
                BigDecimal f019nYoy = calYoy(totalAssetsGrowth.getVal(), calYoy(xnhk0202Yoy.getF019n(), xnhk0202LastYoy.getF019n()));
                totalAssetsGrowth.setYoy(f019nYoy);
            }
        }


        F10Val operatingRevenueGrowth = F10Val.builder().val(calYoy(xnhk0201.getF010n(), yoy.getF010n())).build();
        F10Val netProfitGrowth = F10Val.builder().val(calYoy(xnhk0201.getF030n(), yoy.getF030n())).build();

        F10Val grossIncomeGrowth = F10Val.builder().val(calYoy(xnhk0201.getF024n(), yoy.getF024n())).build();
        F10Val earningPerShareGrowth = F10Val.builder().val(calYoy(xnhk0201.getF039n(), yoy.getF039n())).build();
        if (lastYoy != null) {
            BigDecimal f010nYoy = calYoy(operatingRevenueGrowth.getVal(), calYoy(yoy.getF010n(), lastYoy.getF010n()));
            BigDecimal f031nYoy = calYoy(netProfitGrowth.getVal(), calYoy(yoy.getF030n(), lastYoy.getF030n()));
            BigDecimal f024nYoy = calYoy(grossIncomeGrowth.getVal(), calYoy(yoy.getF024n(), lastYoy.getF024n()));
            BigDecimal f039nYoy = calYoy(earningPerShareGrowth.getVal(), calYoy(yoy.getF039n(), lastYoy.getF039n()));
            operatingRevenueGrowth.setYoy(f010nYoy);
            netProfitGrowth.setYoy(f031nYoy);
            grossIncomeGrowth.setYoy(f024nYoy);
            earningPerShareGrowth.setYoy(f039nYoy);
        }
        GrowthAbilityNonFinancial growthAbility = GrowthAbilityNonFinancial.builder()
                .earningPerShareGrowth(earningPerShareGrowth)
                .grossIncomeGrowth(grossIncomeGrowth)
                .netProfitGrowth(netProfitGrowth)
                .totalAssetsGrowth(totalAssetsGrowth)
                .operatingRevenueGrowth(operatingRevenueGrowth).build();
        figuresNonFinancialEntity.setGrowthAbility(growthAbility);
    }


    /**
     * 盈利能力
     *
     * @param figuresNonFinancialEntity
     * @param xnhk0203
     * @param yoy
     */
    private void profitabilityCal(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Xnhk0203 xnhk0203, Xnhk0203 yoy) {


        F10Val profitRatio = F10Val.builder().val(xnhk0203.getF008n()).build();

        F10Val operatingProfitRatio = F10Val.builder().val(xnhk0203.getF012n()).build();

        F10Val earningBeforeTaxRatio = F10Val.builder().val(xnhk0203.getF013n()).build();

        //F10Val netProfitRatio = F10Val.builder().val(xnhk0203.getF014n()).build();

        F10Val coreProfitRatio = F10Val.builder().val(xnhk0203.getF009n()).build();

        F10Val roe = F10Val.builder().val(xnhk0203.getF015n()).build();

        F10Val roa = F10Val.builder().val(xnhk0203.getF016n()).build();

        F10Val roce = F10Val.builder().val(xnhk0203.getF017n()).build();

        F10Val averageRoe = F10Val.builder().val(xnhk0203.getF018n()).build();

        F10Val averageRoa = F10Val.builder().val(xnhk0203.getF019n()).build();

        F10Val averageRoce = F10Val.builder().val(xnhk0203.getF020n()).build();


        if (yoy != null) {
            profitRatio.setYoy(calYoy(xnhk0203.getF008n(), yoy.getF008n()));
            operatingProfitRatio.setYoy(calYoy(xnhk0203.getF012n(), yoy.getF012n()));
            earningBeforeTaxRatio.setYoy(calYoy(xnhk0203.getF013n(), yoy.getF013n()));

            //netProfitRatio.setYoy(calYoy(xnhk0203.getF014n(), yoy.getF014n()));

            coreProfitRatio.setYoy(calYoy(xnhk0203.getF009n(), yoy.getF009n()));

            roe.setYoy(calYoy(xnhk0203.getF015n(), yoy.getF015n()));

            roa.setYoy(calYoy(xnhk0203.getF016n(), yoy.getF016n()));

            roce.setYoy(calYoy(xnhk0203.getF017n(), yoy.getF017n()));

            averageRoe.setYoy(calYoy(xnhk0203.getF018n(), yoy.getF018n()));

            averageRoa.setYoy(calYoy(xnhk0203.getF019n(), yoy.getF019n()));

            averageRoce.setYoy(calYoy(xnhk0203.getF020n(), yoy.getF020n()));
        }
        ProfitabilityNonFinancial profitabilityNonFinancial = ProfitabilityNonFinancial.builder()
                .profitRatio(profitRatio)
                .operatingProfitRatio(operatingProfitRatio)
                .earningBeforeTaxRatio(earningBeforeTaxRatio)
                //.netProfitRatio(netProfitRatio)
                .coreProfitRatio(coreProfitRatio)
                .roe(roe)
                .roa(roa)
                .roce(roce)
                .averageRoe(averageRoe)
                .averageRoa(averageRoa)
                .averageRoce(averageRoce).build();

        figuresNonFinancialEntity.setProfitability(profitabilityNonFinancial);

    }

    /**
     * 关键指标
     *
     * @param figuresNonFinancialEntity
     * @param xnhk0201
     * @param yoy
     */
    private void keyFiguresCal(F10KeyFiguresNonFinancialEntity figuresNonFinancialEntity, Xnhk0201 xnhk0201, Xnhk0201 yoy) {
        /**
         * 营业收入
         */
        F10Val operatingRevenue = F10Val.builder().val(xnhk0201.getF010n()).build();
        /**
         * 净利润
         */
        F10Val netProfits = F10Val.builder().val(xnhk0201.getF030n()).build();
        if (yoy != null) {
            operatingRevenue.setYoy(calYoy(xnhk0201.getF010n(), yoy.getF010n()));
            netProfits.setYoy(calYoy(xnhk0201.getF030n(), yoy.getF030n()));
        }
        KeyFiguresNonFinancial keyFiguresNonFinancial = KeyFiguresNonFinancial.builder()
                .operatingRevenue(operatingRevenue)
                .netProfits(netProfits).build();
        figuresNonFinancialEntity.setKeyFigures(keyFiguresNonFinancial);
    }

    /**
     * 寻找同比
     *
     * @param xnhk0203
     * @param xnhk0203s
     * @return
     */
    private Xnhk0203 findYoyXnhk0203(Xnhk0203 xnhk0203, List<Xnhk0203> xnhk0203s) {
        return xnhk0203s.stream().filter(item -> item.getF006v().equals(xnhk0203.getF006v()) && item.getF002d() < xnhk0203.getF002d()).max(Comparator.comparing(Xnhk0203::getF002d)).orElse(null);
    }



    private Xnhk0201 findYoyXnhk0201(Xnhk0201 xnhk0201, List<Xnhk0201> xnhk0201s) {
        if (xnhk0201 == null) {
            return null;
        }
        return xnhk0201s.stream().filter(item -> item.getF006v().equals(xnhk0201.getF006v()) && item.getF002d() < xnhk0201.getF002d()).max(Comparator.comparing(Xnhk0201::getF002d)).orElse(null);

    }

    private Xnhk0202 findYoyXnhk0202(Xnhk0202 xnhk0202, List<Xnhk0202> xnhk0202s) {
        if (xnhk0202 == null) {
            return null;
        }
        return xnhk0202s.stream().filter(item -> item.getF006v().equals(xnhk0202.getF006v()) && item.getF002d() < xnhk0202.getF002d()).max(Comparator.comparing(Xnhk0202::getF002d)).orElse(null);

    }

    private Xnhk0210 findYoyXnhk0210(Xnhk0210 xnhk0210, List<Xnhk0210> xnhk0210s) {
        if (xnhk0210 == null) {
            return null;
        }
        return xnhk0210s.stream().filter(item -> item.getF006v().equals(xnhk0210.getF006v()) && item.getF002d() < xnhk0210.getF002d()).max(Comparator.comparing(Xnhk0210::getF002d)).orElse(null);

    }

    /**
     * 寻找环比
     *
     * @param xnhk0203
     * @param xnhk0203s
     * @return
     */
    private Xnhk0203 findCircleXnhk0203(Xnhk0203 xnhk0203, List<Xnhk0203> xnhk0203s) {

        return xnhk0203s.stream().filter(item -> item.getF006v().equals(xnhk0203.getF006v()) && item.getF002d() < xnhk0203.getF002d()).max(Comparator.comparing(Xnhk0203::getF002d)).orElse(null);
    }


}
