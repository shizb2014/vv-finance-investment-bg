package com.vv.finance.investment.bg.dto.stock;

import com.vv.finance.investment.bg.stock.indicator.entity.BaseStockIndicator;
import com.vv.finance.investment.bg.stock.kline.entity.BaseStockKlineEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author chenyu
 * @date 2020/11/3 16:02
 */
@Data
public class StockKlineDTO implements Serializable {
    private static final long serialVersionUID = 9004077745145012001L;
    /**
     * k线
     *
     */
    private List<? extends BaseStockKlineEntity> klines;


    private List<? extends BaseStockIndicator> indicatorList;
}
