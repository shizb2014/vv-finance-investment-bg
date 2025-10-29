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
public class KlineReq implements Serializable {

    private static final long serialVersionUID = 82061307508731L;

    @NotNull
    @ApiModelProperty(value = "代码", required = true)
    private String code;

    @ApiModelProperty(value = "not 不复权；forward：前复权;backward：后复权;")
    private String adjhkt;

    @NotNull
    @ApiModelProperty(value = "k线类型 day | week | month | year | quarter | min1 | min5 | min15 | min30 | min60 | min120", required = true)
    private String type;

    @ApiModelProperty(value = "截止时间戳", required = true)
    private Long endTime;

    @ApiModelProperty(value = "开始时间戳")
    private Long startTime;

    @ApiModelProperty(value = "当前页码")
    private Integer current;

    @ApiModelProperty(value = "页容")
    private Integer pageSize;

    @ApiModelProperty(value = "指标数据")
    private List<Integer> indicators;

    @ApiModelProperty(value = "是否计算买卖点")
    private boolean calculateBuySellPoint;
}
