package com.vv.finance.investment.bg.entity.information.app;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ToString
@ApiModel("简要股票信息-app")
public class SimpleStockVoApp  implements Serializable {
    private static final long serialVersionUID = -1265368111399666149L;

    @ApiModelProperty(value = "股票名字")
    private String stockName;

    @ApiModelProperty(value = "股票ID")
    private Long stockId;

    @ApiModelProperty(value = "股票代码")
    private String stockCode;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "涨跌额")
    private BigDecimal chg;

    @ApiModelProperty(value = "涨跌辐")
    private BigDecimal chgPct;

    @ApiModelProperty(value = "权证代码")
    private String warrantCode;
    @ApiModelProperty(value = "权证名称")
    private String warrantName;

    @ApiModelProperty(value = "区域类型 0-港股 1-美股")
    private Integer regionType;
    @ApiModelProperty("金融产品类型 1-正股 2-ETF 3-权证 4-指数 5-板块 6-其他 7-基金 8-债券 9-ETN；决定点击跳转的页面类型是指数、股票、权证、基金、期货等；")
    private Integer stockType;

    // pc端要求填充字段(可以为null)
    @ApiModelProperty("维度/影响范围(null 无 0-个股 1-行业 2-大盘)")
    private Integer influenceScope;

    @ApiModelProperty(value = "影响范围关联信息")
    private String relationInfluenceScope;

    @ApiModelProperty(value = "利好利空程度( -3:大利空 -2:中利空 -1:小利空 0:无 1:小利好, 2:中利好, 3:大利好 )")
    private Integer positiveNegative;

    @ApiModelProperty("分数")
    private Integer positiveScore;

    @ApiModelProperty(value = "最新价")
    private BigDecimal last;
}
