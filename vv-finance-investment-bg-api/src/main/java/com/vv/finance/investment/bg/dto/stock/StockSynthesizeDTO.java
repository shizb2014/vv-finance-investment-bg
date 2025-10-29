package com.vv.finance.investment.bg.dto.stock;

import com.vv.finance.investment.bg.dto.Indicator.BaseIndicatorDTO;
import com.vv.finance.investment.bg.dto.info.TradeBaseDTO;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import lombok.Data;

import java.util.List;

/**
 * @author chenyu
 * @date 2020/10/30 10:48
 */
@Data
public class StockSynthesizeDTO extends StockDetailDTO{

    private static final long serialVersionUID = 7219062122449057604L;
    /**
     * k线
     *
     */
    private List<StockKline> klines;

    /**
     * 指标
     */
    private List<? extends BaseIndicatorDTO> indicatorList;

    /**
     * 成交量
     *
     */
    private List<TradeBaseDTO> tradeList;

    /**
     * 是否加入自选
     */
    private Integer option;


}
