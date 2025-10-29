package com.vv.finance.investment.bg.api.f10;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.entity.f10.estimation.EstimationAnalyzeVO;
import com.vv.finance.investment.bg.entity.f10.estimation.EstimationChar;
import com.vv.finance.investment.bg.entity.f10.estimation.EstimationRadar;

import java.util.List;

/**
 * @ClassName F10EstimationApi
 * @Deacription 估值分析
 * @Author lh.sz
 * @Date 2021年09月09日 15:01
 **/
public interface F10EstimationApi {
    /**
     * 估值分析雷达图
     *
     * @param code 股票代码
     * @return
     */
    ResultT<List<EstimationRadar>> getEstimationRadar(String code);

    /**
     * 估值分析线型图
     *
     * @param code 股票代码
     * @return
     */
    ResultT<List<EstimationChar>> getEstimationAnalyzeChar(String code);

}
