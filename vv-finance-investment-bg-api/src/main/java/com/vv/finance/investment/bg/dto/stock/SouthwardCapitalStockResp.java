package com.vv.finance.investment.bg.dto.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author:maling
 * @Date:2023/9/19
 * @Description:
 */
@Data
public class SouthwardCapitalStockResp implements Serializable {
    private static final long serialVersionUID = 2150051909086491984L;

    @ApiModelProperty(value = "股票id")
    private Long stockId;

    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "股票名")
    private String name;

    @ApiModelProperty(value = "证券简写")
    private String shortname;

    @ApiModelProperty(value = "证券名称")
    private String stockName;

    @ApiModelProperty(value = "正股代码")
    private String justStockCode;

    @ApiModelProperty(value = "品种（BOND-债券，EQTY-股票，TRST-信托产品，WRNT-权证）")
    private String instrument;

    @ApiModelProperty(value = "市场代号（MAIN-主板，GEM-创业板，ETS-扩充交易证券，NASD-NASQAQ AMX市场）")
    private String marketCode;
    /**
     * 市场类型 0-港股 1-A股 2—美股
     */
    @ApiModelProperty(value = "市场类型 0-港股 1-A股 2-美股")
    private Integer marketType;

    @ApiModelProperty("是否加入自选股")
    private Boolean optional;

    @ApiModelProperty("类型 1-指数 0-股票 2-权证")
    private Integer type;

    @ApiModelProperty("是否已建 true 已建")
    private Boolean exist;

    @ApiModelProperty(value = "创建时间",hidden = true)
    private Date createTime;

    @ApiModelProperty("跳转策略id")
    private Long strategyId;
}