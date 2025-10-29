package com.vv.finance.investment.bg.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author chenyu
 * @date 2021/3/17 14:10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("k线")
public class RtKlineReq implements Serializable {

    private static final long serialVersionUID = 82061307508731L;

    @ApiModelProperty("股票ID")
    private Long stockId;

    // @NotNull
    @ApiModelProperty(value = "代码", required = true)
    private String code;

    @ApiModelProperty(value = "forward：前复权；backward：后复权, not 不复权")
    private String adjhkt;

    @NotNull
    @ApiModelProperty(value = "k线类型 rt | fiveDay", required = true)
    private String type;

    @ApiModelProperty(value = "指标数据")
    private List<Integer> indicators;

    @ApiModelProperty(value = "是否计算买卖点")
    private boolean calculateBuySellPoint;

}
