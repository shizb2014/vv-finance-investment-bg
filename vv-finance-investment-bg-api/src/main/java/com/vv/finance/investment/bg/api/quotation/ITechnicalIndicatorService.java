package com.vv.finance.investment.bg.api.quotation;

import com.vv.finance.common.constants.lineshape.LineShapeStockFormEnum;
import com.vv.finance.common.dto.TechnicalIndicatorDTONew;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author qinxi
 * @date 2024/4/18 20:29
 * @description:
 */
public interface ITechnicalIndicatorService {

    /**
     * 获取k线技术指标形态
     *
     * @param codes    股票代码
     * @param limit    条数
     * @param duration k线类型 LineShapeTraceDurationEnum 周期：0-日K，1-5分，2-15分，3-60分，4-120分
     * @return key 股票代码  value 实体
     */
    Map<String, List<TechnicalIndicatorDTONew>> getTechnicalIndicatorMap(Collection<String> codes, Integer limit, Integer duration, long localTimeStamp);


    /**
     * 获取选股形态
     *
     * @param codes     股票代码
     * @param duration  k线类型 LineShapeTraceDurationEnum 周期：0-日K，1-5分，2-15分，3-60分，4-120分
     * @param timestamp 时间戳
     * @return
     */
    Map<String, List<LineShapeStockFormEnum>> getLineShapeStockFormEnum(Collection<String> codes, Integer duration, long timestamp);
}
