package com.vv.finance.investment.bg.api.frontend;


import com.vv.finance.investment.bg.dto.kline.BaseKlineDTO;

import java.util.List;

/**
 * @author hamilton
 * @date 2021/11/9 15:20
 */
public interface IStockKlineService {

    List<BaseKlineDTO> queryAndSetEvent(String code, String type, String adjhkt, List<BaseKlineDTO> klineList);

}
