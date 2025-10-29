package com.vv.finance.investment.bg.dto.index;

import com.fenlibao.security.sdk.ws.core.model.resp.TrendResp;
import com.vv.finance.investment.bg.stock.kline.entity.RtStockKline;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenyu
 * @date 2020/12/21 16:10
 */
@Data
public class IndexDashBoardInfo implements Serializable {
    private static final long serialVersionUID = -3633196998997282060L;
    @ApiModelProperty(value = "指数名")
    private String name;
    @ApiModelProperty(value = "指数代码")
    private String code;
    @ApiModelProperty(value = "分时图")
    private List<RtStockKline> trendResp;
    @ApiModelProperty(value = "昨收价")
    private BigDecimal preClose;
    @ApiModelProperty(value = "最新价")
    private BigDecimal last;
    @ApiModelProperty(value = "涨跌幅")
    private BigDecimal increase;
    @ApiModelProperty(value = "开盘价")
    private BigDecimal open;
}
