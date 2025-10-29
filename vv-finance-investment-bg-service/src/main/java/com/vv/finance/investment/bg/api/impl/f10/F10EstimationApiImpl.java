package com.vv.finance.investment.bg.api.impl.f10;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.api.f10.F10EstimationApi;
import com.vv.finance.investment.bg.entity.f10.estimation.EstimationAnalyzeVO;
import com.vv.finance.investment.bg.entity.f10.estimation.EstimationChar;
import com.vv.finance.investment.bg.entity.f10.estimation.EstimationRadar;
import com.vv.finance.investment.bg.stock.f10.service.impl.F10EstimationAnalyzeServiceImpl;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName F10EstimationApiImpl
 * @Deacription 估值分析
 * @Author lh.sz
 * @Date 2021年09月09日 15:08
 **/
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class F10EstimationApiImpl implements F10EstimationApi {

    @Resource
    F10EstimationAnalyzeServiceImpl f10EstimationAnalyzeService;

    @Override
    public ResultT<List<EstimationRadar>> getEstimationRadar(String code) {
        return ResultT.success(f10EstimationAnalyzeService.getEstimationRadarChar(code));
    }

    @Override
    public ResultT<List<EstimationChar>> getEstimationAnalyzeChar(String code) {
        return ResultT.success(f10EstimationAnalyzeService.getEstimationAnalyzeChar(code));
    }
}
