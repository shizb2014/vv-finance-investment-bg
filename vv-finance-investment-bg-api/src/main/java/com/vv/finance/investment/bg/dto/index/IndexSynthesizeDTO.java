package com.vv.finance.investment.bg.dto.index;

import com.fenlibao.security.sdk.ws.core.model.resp.IndexComponentResp;
import com.vv.finance.investment.bg.dto.Indicator.BaseIndicatorDTO;
import com.vv.finance.investment.bg.dto.stock.StockBaseDTO;
import com.vv.finance.investment.bg.entity.index.IndexBaseIndicator;
import com.vv.finance.investment.bg.entity.index.IndexBaseMinKline;
import com.vv.finance.investment.bg.stock.indicator.entity.BaseStockIndicator;
import com.vv.finance.investment.bg.stock.kline.entity.BaseStockKlineEntity;
import com.vv.finance.investment.bg.stock.kline.entity.StockKline;
import lombok.Data;

import java.util.List;

/**
 * @author chenyu
 * @date 2020/10/30 11:33
 */
@Data
public class IndexSynthesizeDTO extends IndexDetailDTO {

    /**
     * k线图
     */
    private List<StockKline> klines;

    /**
     * 指标列表
     */
    private List<? extends IndexBaseIndicator> indicatorList;


}
