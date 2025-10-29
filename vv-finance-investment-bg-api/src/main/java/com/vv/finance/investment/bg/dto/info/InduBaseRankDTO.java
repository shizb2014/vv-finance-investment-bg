package com.vv.finance.investment.bg.dto.info;

import com.fenlibao.security.sdk.ws.core.model.resp.RankResp;
import com.vv.finance.investment.bg.dto.stock.StockBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author chenyu
 * @date 2020/11/13 14:12
 */
@Data
public class InduBaseRankDTO implements Serializable {
    private static final long serialVersionUID = 7197837039410832919L;
    @ApiModelProperty("行业名称")
    private String name;
    @ApiModelProperty("行业代码")
    private String symbol;
    @ApiModelProperty("行业涨跌幅")
    private String chg_pct;
    @ApiModelProperty("行业涨跌额")
    private String amount;
    @ApiModelProperty("股票列表")
    private List<RankResp> stocks;
    @ApiModelProperty("上涨")
    private Integer rise;
    @ApiModelProperty("平盘")
    private Integer flat;
    @ApiModelProperty("下跌")
    private Integer fall;
}
