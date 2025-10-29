package com.vv.finance.investment.bg.api.broker;

import com.vv.finance.investment.bg.dto.broker.BrokerHoldingsTrendDTO;
import com.vv.finance.investment.bg.dto.broker.TopConcentrationRankDTO;

import java.util.List;

/**
 * @ClassName HKStockHotSpotApi
 * @Description 市场-港股热点
 * @Author liujiajian
 * @Date 2022/10/8
 */

public interface BrokerHKStockHotSpotApi {
    /**
     * 获取今日经纪商增持/减持比例，增减持市值排行榜（前五的股票）
     * @Param type
     * @return
     */

    List<BrokerHoldingsTrendDTO> getBrokerHoldingsRank(Integer type);


    /**
     * 获取top5/10经纪商集中度持股比例变动排行榜（前五的股票）
     * @Param type
     * @return
     */
    List<TopConcentrationRankDTO> getTopBrokersHoldingsPercentRank(Integer type);

}
