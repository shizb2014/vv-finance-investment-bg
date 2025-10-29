package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/4/21 15:34
 */
@Data
public class StockSimpleInfo implements Serializable {
    private static final long serialVersionUID = -6534132782967115L;
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
    @ApiModelProperty(value = "首字母")
    private String nameInitials;
    @ApiModelProperty(value = "拼音")
    private String namePinyin;
    @ApiModelProperty(value = "首字母")
    private String shortNameInitials;
    @ApiModelProperty(value = "拼音")
    private String shortNamePinyin;
    @ApiModelProperty(value = "首字母")
    private String stockNameInitials;
    @ApiModelProperty(value = "拼音")
    private String stockNamePinyin;
    @ApiModelProperty(value = "品种（BOND-债券，EQTY-股票，TRST-信托产品，WRNT-权证）")
    private String instrument;
    @ApiModelProperty(value = "市场代号（MAIN-主板，GEM-创业板，ETS-扩充交易证券，NASD-NASQAQ AMX市场）")
    private String marketcode;
    @ApiModelProperty("类型 1-指数 0-股票 2-权证")
    private Integer type;
    @ApiModelProperty("股票类型")
    private Integer stockType;
    @ApiModelProperty("交易所类型")
    private String exchange;
    @ApiModelProperty("区域代码类型")
    private Integer regionType;
    @ApiModelProperty("股票状态,0,正常;1,未上市;2,熔断;3,停牌")
    private Integer securityStatus;
    @ApiModelProperty(value = "行业代码")
    private String industryCode;

    @ApiModelProperty(value = "行业名称")
    private String industryName;

    @ApiModelProperty(value = "币种")
    private String currency;

    @ApiModelProperty(value = "每手股数")
    private Integer lotsize;
}
