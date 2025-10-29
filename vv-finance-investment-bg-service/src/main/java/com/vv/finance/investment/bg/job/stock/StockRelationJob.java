package com.vv.finance.investment.bg.job.stock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.vv.finance.investment.bg.api.stock.StockSceneSimulateApi;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.entity.index.TIndexInfo;
import com.vv.finance.investment.bg.mapper.index.TIndexInfoMapper;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.service.HkStockRelationService;
import com.vv.finance.investment.bg.stock.info.service.IStockDefineService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author yangpeng
 * @date 2024/6/25 16:36
 * @description
 */
@Slf4j
@Component
public class StockRelationJob {

    @Resource
    private IStockDefineService stockDefineService;

    @Resource
    private HkStockRelationService hkStockRelationService;

    @Resource
    private StockSceneSimulateApi stockSceneSimulateApi;

    @XxlJob(value = "syncAllStockRelation", author = "杨鹏", desc = "保存所有股票关系")
    public ReturnT<String> syncAllStockRelation(String param) {
        log.info("StockRelationJob syncAllStockRelation start");
        // 正股、 行业
        List<StockDefine> stockDefineAll = stockDefineService.list(Wrappers.<StockDefine>lambdaQuery().select(StockDefine::getCode));
        List<String> codeList = CollUtil.map(stockDefineAll, StockDefine::getCode, true);
        // 指数
        // List<TIndexInfo> tIndexInfos = tIndexInfoMapper.selectList(null);
        // List<String> indexCodes = CollUtil.map(tIndexInfos, TIndexInfo::getCode, true);
        // // 保存关联关系
        // Set<String> codeAll = CollUtil.unionDistinct(codeList, indexCodes);
        hkStockRelationService.saveNewRelations(new ArrayList<>(codeList));
        // // 刷新缓存
        // stockCache.updateStockSimpleInfo();
        log.info("StockRelationJob syncAllStockRelation end");
        return ReturnT.SUCCESS;
    }

    @XxlJob(value = "deleteSimulateData", author = "杨鹏", desc = "删除模拟数据")
    public ReturnT<String> deleteSimulateData(String param) {
        log.info("StockRelationJob deleteSimulateData start, param: {}", param);
        if (StrUtil.isNotBlank(param)) {
            List<String> codeList = StrUtil.split(param, ",");
            // 删除指定code
            codeList.forEach(code -> stockSceneSimulateApi.deleteDataSceneByCode(false, code));
        }
        log.info("StockRelationJob deleteSimulateData end");
        return ReturnT.SUCCESS;
    }
}

