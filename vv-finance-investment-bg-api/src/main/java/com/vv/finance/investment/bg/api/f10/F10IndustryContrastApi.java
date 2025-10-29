package com.vv.finance.investment.bg.api.f10;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.entity.quotation.f10.ComEstimationVO;
import com.vv.finance.investment.bg.entity.f10.EstimationSortEnum;
import com.vv.finance.investment.bg.entity.f10.enums.GrowthContrastSortEnum;
import com.vv.finance.investment.bg.entity.f10.enums.ScaleSortEnum;
import com.vv.finance.investment.bg.entity.f10.industry.Estimation;
import com.vv.finance.investment.bg.entity.f10.industry.GrowthContrast;
import com.vv.finance.investment.bg.entity.f10.industry.MarketPresence;
import com.vv.finance.investment.bg.entity.f10.industry.Scale;

import java.util.List;

/**
 * 行业对比api
 *
 * @author lh.sz
 */
public interface F10IndustryContrastApi {

    /**
     * 成长性对比
     *
     * @param code                   股票代码
     * @param growthContrastSortEnum 排序字段
     * @return
     */
    ResultT<List<GrowthContrast>> getGrowthContrast(String code, GrowthContrastSortEnum growthContrastSortEnum);

    /**
     * 规模对比
     *
     * @param code     股票代码
     * @param sortEnum 排序字段
     * @return
     */
    ResultT<List<Scale>> getScale(String code, ScaleSortEnum sortEnum);

    ResultT<List<Estimation>> buildEstimationV2(String code);

}
