package com.vv.finance.investment.bg.api.impl.information;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.api.information.InformationApi;
import com.vv.finance.investment.bg.api.information.InformationAppApi;
import com.vv.finance.investment.bg.entity.information.*;
import com.vv.finance.investment.bg.entity.information.app.FreeStockNewsVoApp;
import com.vv.finance.investment.bg.entity.information.app.SimpleStockVoApp;
import com.vv.finance.investment.bg.entity.information.app.StockNewsDetailVoApp;
import com.vv.finance.investment.bg.stock.information.handler.InformationHandler;
import com.vv.finance.investment.bg.stock.information.handler.InformationHandlerV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
@RequiredArgsConstructor
public class InformationAppApiImpl implements InformationAppApi {
    @Resource
    private InformationHandler informationHandler;

    @Resource
    private InformationHandlerV2 informationHandlerV2;

    @Override
    public ResultT<PageWithTime<FreeStockNewsVoApp>> transactionInformationPageV2(CommonNewsPage page) {
        PageWithTime<FreeStockNewsVo> result = informationHandlerV2.transactionInformationPageV2(page);
        return ResultT.success(transformModel(result));
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVoApp>> newShareInformationPageV2(CommonNewsPage page) {
        PageWithTime<FreeStockNewsVo> result = informationHandlerV2.newShareInformationPageV2(page);
        return ResultT.success(transformModel(result));
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVoApp>> hkOrAmericanInformationPageV2(CommonNewsPage page, CommonNewsPage.QueryCodeEnum type) {
        PageWithTime<FreeStockNewsVo> result = informationHandlerV2.hkOrAmericanInformationPageV2(page,type);
        return ResultT.success(transformModel(result));
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVoApp>> listNewsBySimpleStockVoV2(CommonNewsPage page) {
        PageWithTime<FreeStockNewsVo> result = informationHandlerV2.listNewsBySimpleStockVoV2(page);
        return ResultT.success(transformModel(result));
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVoApp>> listFreeNewsV2(CommonNewsPage page, List<String> stockCodes) {
        PageWithTime<FreeStockNewsVo> result = informationHandlerV2.pageFreeVoV2(page,stockCodes);
        return ResultT.success(transformModel(result));
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVoApp>> listNewsByWarrantStockVoV2(CommonNewsPage page) {
        PageWithTime<FreeStockNewsVo> result = informationHandler.listNewsByWarrantStockVo(page);
        return ResultT.success(transformModel(result));
    }

    @Override
    public ResultT<StockNewsDetailVoApp> findFreeDetailByIdV2(Long newsid, boolean needWarrant) {
        StockNewsDetailVo newsDetail = informationHandler.findByNewsidInHk(newsid,needWarrant);
        return ResultT.success(transformDetailModel(newsDetail));
    }
    /**
     * 删除临时股票资讯
     *
     * @param stockCode
     */
    @Async
    @Override
    public void delByStockCode(String stockCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("删除临时股票资讯 开始：stockCode：{}",stockCode);
            informationHandler.delByStockCode(stockCode);
            log.info("删除临时股票资讯 结束：stockCode：{} 耗时：{}",stockCode,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("删除临时股票资讯 stockCode：{} 异常",stockCode,e);
        }

    }
    /**
     * 变更资讯股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    @Override
    public void upInformationStockCode(String sourceCode, String targetCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("变更资讯股票code 开始：sourceCode：{} targetCode：{}",sourceCode,targetCode);
            informationHandler.upInformationStockCode(sourceCode,targetCode);
            log.info("变更资讯股票code 结束：sourceCode：{} targetCode：{} 耗时：{}",sourceCode,targetCode,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("变更资讯股票code：sourceCode：{} targetCode：{} 异常",sourceCode,targetCode,e);
        }

    }
    /**
     * 新增模拟股票资讯数据
     *
     * @param simulateCode 模拟股票code
     */
    @Override
    public void saveSimulateInformation(String simulateCode) {
        try {
            long l = System.currentTimeMillis();
            log.info("新增模拟股票资讯数据 开始：simulateCode：{}",simulateCode);
            informationHandler.saveSimulateInformation(simulateCode);
            log.info("新增模拟股票资讯数据 结束：simulateCode：{} 耗时：{}",simulateCode,System.currentTimeMillis()-l);
        }catch (Exception e){
            log.error("新增模拟股票资讯数据：simulateCode：{} 异常",simulateCode,e);
        }
    }

    @Override
    public ResultT<PageWithTime<FreeStockNewsVoApp>> assignInformationPageV2(CommonNewsPage page, List<String> stockCodes) {
        PageWithTime<FreeStockNewsVo> result = informationHandlerV2.assignInformationPageV2(page,stockCodes);
        return ResultT.success(transformModel(result));
    }

    /**
     * 将模型替换为app所需要模型（资讯列表）
     */
    private PageWithTime<FreeStockNewsVoApp> transformModel(PageWithTime<FreeStockNewsVo> source){
        PageWithTime<FreeStockNewsVoApp> target = new PageWithTime<FreeStockNewsVoApp>();
        if (ObjectUtil.isNotEmpty(source)) {
            BeanUtils.copyProperties(source,target);
            List<FreeStockNewsVo> records = CollUtil.defaultIfEmpty(source.getRecords(), Collections.emptyList());
            List<FreeStockNewsVoApp> appRecords = records.stream().map(item -> {
                FreeStockNewsVoApp appModel = new FreeStockNewsVoApp();
                BeanUtils.copyProperties(item, appModel);
                SimpleStockVo simpleStockVo = item.getSimpleStockVo();
                if (simpleStockVo != null) {
                    SimpleStockVoApp simpleStockVoApp = new SimpleStockVoApp();
                    BeanUtils.copyProperties(simpleStockVo, simpleStockVoApp);
                    simpleStockVoApp.setStockName(simpleStockVo.getName());
                    appModel.setSimpleStockVo(simpleStockVoApp);
                }
                return appModel;
            }).collect(Collectors.toList());
            target.setRecords(appRecords);
        }
        return target;
    }

    /**
     * 将模型替换为app所需要模型（资讯详情）
     */
    private StockNewsDetailVoApp transformDetailModel(StockNewsDetailVo source){
        if(source == null){
            return null;
        }
        StockNewsDetailVoApp target = new StockNewsDetailVoApp();
        BeanUtils.copyProperties(source,target);
        List<SimpleStockVo> simpleStockVos = source.getSimpleStockVos();
        if(CollUtil.isNotEmpty(simpleStockVos)){
            List<SimpleStockVoApp> simpleStockVoApps = simpleStockVos.stream().map(item -> {
                SimpleStockVoApp simpleStockVoApp = new SimpleStockVoApp();
                BeanUtils.copyProperties(item, simpleStockVoApp);
                simpleStockVoApp.setStockName(item.getName());
                return simpleStockVoApp;
            }).collect(Collectors.toList());
            target.setSimpleStockVos(simpleStockVoApps);
        }
        return target;
    }
}
