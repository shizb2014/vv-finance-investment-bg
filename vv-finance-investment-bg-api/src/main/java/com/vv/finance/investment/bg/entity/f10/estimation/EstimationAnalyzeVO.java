package com.vv.finance.investment.bg.entity.f10.estimation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName EstimationAnalyzeVO
 * @Deacription 估值分析VO
 * @Author lh.sz
 * @Date 2021年08月17日 16:37
 **/
@Data
@ToString
@Builder
public class EstimationAnalyzeVO implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

    /**
     * 当前时间
     */
    @ApiModelProperty(value = "当前时间")
    private Long time;

    /**
     * 市盈率TTM
     */
    @ApiModelProperty(value = "市盈率TTM")
    private EstimationAnalyzeVal peTtm;

    /**
     * 市盈率（静）
     */
    @ApiModelProperty(value = "市盈率（静）")
    private EstimationAnalyzeVal pe;

    /**
     * 市净率
     */
    @ApiModelProperty(value = "市净率")
    private EstimationAnalyzeVal pb;

}
