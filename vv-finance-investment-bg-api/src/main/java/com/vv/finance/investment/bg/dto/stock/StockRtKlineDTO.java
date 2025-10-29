package com.vv.finance.investment.bg.dto.stock;

import com.vv.finance.investment.bg.stock.indicator.entity.BaseStockIndicator;
import com.vv.finance.investment.bg.stock.kline.entity.BaseStockKlineEntity;
import com.vv.finance.investment.bg.stock.kline.entity.RtStockKline;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author chenyu
 * @date 2020/11/19 10:02
 */
@Data
public class StockRtKlineDTO  implements Serializable {

    private static final long serialVersionUID = 3970603128986256407L;
    private List<Map<String,List<RtStockKline>>> klines;

    private List<Map<String,List<BaseStockIndicator>>> indicatorList;

    @ApiModelProperty("昨收价")
    private String preClose = "";


    @ApiModelProperty("今开价")
    private String open = "";

//    private List<List<BaseStockIndicator>> indicatorList;
}
