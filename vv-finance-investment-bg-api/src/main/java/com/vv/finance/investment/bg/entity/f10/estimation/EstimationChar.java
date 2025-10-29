package com.vv.finance.investment.bg.entity.f10.estimation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName EstimationChar
 * @Deacription 估值分析图表
 * @Author lh.sz
 * @Date 2021年07月29日 15:05
 **/
@Data
@ToString
public class EstimationChar implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 盈利
     */
    @ApiModelProperty(value = "盈利")
    private List<BigDecimal> profit;

    /**
     * 成长
     */
    @ApiModelProperty(value = "成长")
    private List<BigDecimal> growth;

    /**
     * 运营
     */
    @ApiModelProperty(value = "运营")
    private List<BigDecimal> operating;

    /**
     * 偿债
     */
    @ApiModelProperty(value = "偿债")
    private List<BigDecimal> debt;

    /**
     * 现金
     */
    @ApiModelProperty(value = "现金")
    private List<BigDecimal> cash;

    private Long time;

}
