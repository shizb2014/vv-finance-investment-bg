package com.vv.finance.investment.bg.api.impl.f10;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.entity.quotation.f10.ComEstimationVO;
import com.vv.finance.investment.bg.api.f10.F10IndustryContrastApi;
import com.vv.finance.investment.bg.entity.f10.EstimationSortEnum;
import com.vv.finance.investment.bg.entity.f10.enums.GrowthContrastSortEnum;
import com.vv.finance.investment.bg.entity.f10.enums.ScaleSortEnum;
import com.vv.finance.investment.bg.entity.f10.industry.Estimation;
import com.vv.finance.investment.bg.entity.f10.industry.GrowthContrast;
import com.vv.finance.investment.bg.entity.f10.industry.MarketPresence;
import com.vv.finance.investment.bg.entity.f10.industry.Scale;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10IndustryContrastServiceImpl;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName F10IndustryContrastApiImpl
 * @Deacription 行业对比
 * @Author lh.sz
 * @Date 2021年08月22日 9:56
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class F10IndustryContrastApiImpl implements F10IndustryContrastApi {

    @Resource
    F10IndustryContrastServiceImpl f10IndustryContrastService;


    @Override
    public ResultT<List<GrowthContrast>> getGrowthContrast(String code, GrowthContrastSortEnum growthContrastSortEnum) {
        return ResultT.success(f10IndustryContrastService.getGrowthContrast(code, growthContrastSortEnum));
    }

    @Override
    public ResultT<List<Scale>> getScale(String code, ScaleSortEnum sortEnum) {
        return ResultT.success(f10IndustryContrastService.getScale(code, sortEnum));
    }

    @Override
    public ResultT<List<Estimation>> buildEstimationV2(String code) {
        return ResultT.success(f10IndustryContrastService.buildEstimationV2(code));
    }

}
