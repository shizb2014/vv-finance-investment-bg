package com.vv.finance.investment.bg.stock.f10.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.finance.common.utils.BigDecimalUtil;
import com.vv.finance.investment.bg.constants.ReportTypeEnum;
import com.vv.finance.investment.bg.entity.f10.DuPontAnalysisEntity;
import com.vv.finance.investment.bg.entity.f10.enums.F10MarketTypeEnum;
import com.vv.finance.investment.bg.entity.uts.Xnhk0201;
import com.vv.finance.investment.bg.entity.uts.Xnhk0202;
import com.vv.finance.investment.bg.entity.uts.Xnhk0204;
import com.vv.finance.investment.bg.entity.uts.Xnhk0205;
import com.vv.finance.investment.bg.entity.uts.Xnhk0207;
import com.vv.finance.investment.bg.entity.uts.Xnhk0208;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0201Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0202Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0204Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0205Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0207Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0208Mapper;
import com.vv.finance.investment.bg.utils.TimeConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName F10SourceHandler
 * @Deacription 组装f10数据
 * @Author lh.sz
 * @Date 2021年07月25日 14:21
 **/
@Component
@Slf4j
public class F10DuPontHandler {

    @Resource
    private Xnhk0201Mapper xnhk0201Mapper;

    @Resource
    private Xnhk0202Mapper xnhk0202Mapper;

    @Resource
    private Xnhk0204Mapper xnhk0204Mapper;

    @Resource
    private Xnhk0205Mapper xnhk0205Mapper;

    @Resource
    private Xnhk0207Mapper xnhk0207Mapper;

    @Resource
    private Xnhk0208Mapper xnhk0208Mapper;

    @Resource
    private MongoTemplate mongoTemplate;

    public List<DuPontAnalysisEntity> buildDuPontResult(String stockCode, F10MarketTypeEnum marketTypeEnum) {

        // 负债总额、资产总额、所有者权益总额、净利润、营业收入
        List<DuPontAnalysisEntity> duPontAnalysisEntities = buildDuPontEntityList(stockCode, marketTypeEnum);

        // 排除不处理的类型
        duPontAnalysisEntities = duPontAnalysisEntities.stream().filter(dpa -> !ReportTypeEnum.unResolveTypeList().contains(dpa.getReportPeriod())).collect(Collectors.toList());

        // 计算
        duPontAnalysisEntities.forEach(duPont -> {
            // 资产负债率 = 负债总额 / 资产总额
            duPont.setAssetLiabilityRatio(BigDecimalUtil.divide(duPont.getTotalLiabilities(), duPont.getTotalAssets(), 10));
            // 权益乘数 = 资产总额 / 所有者权益总额
            duPont.setEquityMultiplier(BigDecimalUtil.divide(duPont.getTotalAssets(), duPont.getTotalOwnerEquity(), 10));
            // 销售净利率 = 净利润 / 营业收入
            duPont.setNetSaleProfitRatio(BigDecimalUtil.divide(duPont.getNetProfit(), duPont.getBusinessIncome(), 10));
            // 总资产周转率 = 营业收入 / 资产总额
            duPont.setTotalAssetTurnover(BigDecimalUtil.divide(duPont.getBusinessIncome(), duPont.getTotalAssets(), 10));
            // 总资产收益率 = 销售净利率 * 总资产周转率
            duPont.setTotalAssetProfitRatio(NumberUtil.mul(duPont.getNetSaleProfitRatio(), duPont.getTotalAssetTurnover()));
            // 净资产收益率 = 权益乘数 * 总资产收益率
            duPont.setNetAssetProfitRatio(NumberUtil.mul(duPont.getEquityMultiplier(), duPont.getTotalAssetProfitRatio()));
        });

        duPontAnalysisEntities.sort(Comparator.comparing(DuPontAnalysisEntity::getDate).reversed());

        return duPontAnalysisEntities;
    }

    private List<DuPontAnalysisEntity> buildDuPontEntityList(String stockCode, F10MarketTypeEnum marketTypeEnum) {
        if (F10MarketTypeEnum.NO_FINANCIAL.equals(marketTypeEnum)) {
            List<Xnhk0201> xk0201s = xnhk0201Mapper.selectList(Wrappers.lambdaQuery(Xnhk0201.class).eq(Xnhk0201::getSeccode, stockCode).orderByDesc(Xnhk0201::getF002d).last("limit 8"));
            List<Xnhk0202> xk0202s = xnhk0202Mapper.selectList(Wrappers.lambdaQuery(Xnhk0202.class).eq(Xnhk0202::getSeccode, stockCode).orderByDesc(Xnhk0202::getF002d).last("limit 8"));

            Map<Long, Xnhk0201> x0201Map = xk0201s.stream().collect(Collectors.toMap(Xnhk0201::getF002d, v -> v, (o, v) -> v));
            Map<Long, Xnhk0202> x0202Map = xk0202s.stream().collect(Collectors.toMap(Xnhk0202::getF002d, v -> v, (o, v) -> v));
            Collection<Long> dayColl = CollUtil.union(x0201Map.keySet(), x0202Map.keySet());

            return dayColl.stream().map(day -> {
                Xnhk0201 xk21 = x0201Map.getOrDefault(day, new Xnhk0201());
                Xnhk0202 xk22 = x0202Map.getOrDefault(day, new Xnhk0202());
                return DuPontAnalysisEntity.builder()
                        .date(TimeConvertUtil.getYmdStrByDay(day))
                        .totalLiabilities(xk22.getF047n())
                        .totalAssets(xk22.getF019n())
                        .totalOwnerEquity(xk22.getF038n())
                        .netProfit(xk21.getF024n())
                        .businessIncome(xk21.getF008n())
                        .currencyType(xk22.getF003v())
                        .reportPeriod(xk21.getF006v())
                        .build();
            }).collect(Collectors.toList());
        } else if (F10MarketTypeEnum.FINANCIAL.equals(marketTypeEnum)) {
            List<Xnhk0204> xk0204s = xnhk0204Mapper.selectList(Wrappers.lambdaQuery(Xnhk0204.class).eq(Xnhk0204::getSeccode, stockCode).orderByDesc(Xnhk0204::getF002d).last("limit 8"));
            List<Xnhk0205> xk0205s = xnhk0205Mapper.selectList(Wrappers.lambdaQuery(Xnhk0205.class).eq(Xnhk0205::getSeccode, stockCode).orderByDesc(Xnhk0205::getF002d).last("limit 8"));

            Map<Long, Xnhk0204> x0204Map = xk0204s.stream().collect(Collectors.toMap(Xnhk0204::getF002d, v -> v, (o, v) -> v));
            Map<Long, Xnhk0205> x0205Map = xk0205s.stream().collect(Collectors.toMap(Xnhk0205::getF002d, v -> v, (o, v) -> v));
            Collection<Long> dayColl = CollUtil.union(x0204Map.keySet(), x0205Map.keySet());

            return dayColl.stream().map(day -> {
                Xnhk0204 xk24 = x0204Map.getOrDefault(day, new Xnhk0204());
                Xnhk0205 xk25 = x0205Map.getOrDefault(day, new Xnhk0205());
                return DuPontAnalysisEntity.builder()
                        .date(TimeConvertUtil.getYmdStrByDay(day))
                        .totalLiabilities(xk25.getF042n())
                        .totalAssets(xk25.getF027n())
                        .totalOwnerEquity(xk25.getF054n())
                        .netProfit(xk24.getF027n())
                        .businessIncome(xk24.getF017n())
                        .currencyType(xk25.getF003v())
                        .reportPeriod(xk24.getF006v())
                        .build();
            }).collect(Collectors.toList());
        } else if (F10MarketTypeEnum.INSURANCE.equals(marketTypeEnum)) {
            List<Xnhk0207> xk0207s = xnhk0207Mapper.selectList(Wrappers.lambdaQuery(Xnhk0207.class).eq(Xnhk0207::getSeccode, stockCode).orderByDesc(Xnhk0207::getF002d).last("limit 8"));
            List<Xnhk0208> xk0208s = xnhk0208Mapper.selectList(Wrappers.lambdaQuery(Xnhk0208.class).eq(Xnhk0208::getSeccode, stockCode).orderByDesc(Xnhk0208::getF002d).last("limit 8"));

            Map<Long, Xnhk0207> x0207Map = xk0207s.stream().collect(Collectors.toMap(Xnhk0207::getF002d, v -> v, (o, v) -> v));
            Map<Long, Xnhk0208> x0208Map = xk0208s.stream().collect(Collectors.toMap(Xnhk0208::getF002d, v -> v, (o, v) -> v));
            Collection<Long> dayColl = CollUtil.union(x0207Map.keySet(), x0208Map.keySet());

            return dayColl.stream().map(day -> {
                Xnhk0207 xk27 = x0207Map.getOrDefault(day, new Xnhk0207());
                Xnhk0208 xk28 = x0208Map.getOrDefault(day, new Xnhk0208());
                return DuPontAnalysisEntity.builder()
                        .date(TimeConvertUtil.getYmdStrByDay(day))
                        .totalLiabilities(xk28.getF040n())
                        .totalAssets(xk28.getF029n())
                        .totalOwnerEquity(xk28.getF053n())
                        .netProfit(xk27.getF025n())
                        .businessIncome(xk27.getF018n())
                        .currencyType(xk28.getF003v())
                        .reportPeriod(xk27.getF006v())
                        .build();
            }).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
