package com.vv.finance.investment.bg.api.f10;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.dto.f10.NewStockEventTimeLineDTO;
import com.vv.finance.investment.bg.entity.uts.Xnhks0112;
import com.vv.finance.investment.bg.entity.uts.Xnhks0503;

import java.util.List;

/**
 * @公司：微微科技有限公司（金融事业部）
 * @描述：
 * @作者：Liam（梁殿豪）
 * @邮箱：liangdianhao@vv.cn
 * @时间：2021/8/31 15:44
 * @版本：1.0
 */
public interface F10MonitorPlateApi {
    List<String> listAllStockCodes(
    );

    List<Xnhks0112> listXrEvent(
        List<String> code,
        Long time
    );

    List<Xnhks0112> listIncrementXrEvent(
        Long beginTime,
        Long endTime
    );

    List<NewStockEventTimeLineDTO> listNewStockTimeLine(
        List<String> codes,
        Long time
    );

    List<NewStockEventTimeLineDTO> listIncrementNewStockTimeLine(
        Long beginTime,
        Long endTime
    );

    List<Xnhks0503> listXnhks0503(
        List<String> codes
    );

    /**
     * 根据股票code和登记日期查询派息
     * @param codes
     * @param starTime
     * @param endTime
     * @return
     */
    ResultT<List<Xnhks0112>> listDividendInfoByF018D(List<String> codes, Long starTime, Long endTime);
}
