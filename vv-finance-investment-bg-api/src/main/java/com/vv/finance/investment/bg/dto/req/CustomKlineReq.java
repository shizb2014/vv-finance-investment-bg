package com.vv.finance.investment.bg.dto.req;

import com.vv.finance.investment.bg.enums.UnitEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
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
public class CustomKlineReq implements Serializable {

    private static final long serialVersionUID = 82061307508731L;

    @NotBlank(message = "股票代码不能为空")
    @ApiModelProperty(value = "代码", required = true)
    private String code;

    @ApiModelProperty(value = "not 不复权；forward：前复权;backward：后复权;")
    private String adjhkt;

    @NotBlank(message = "K线类型不能为空")
    @ApiModelProperty(value = "k线类型 day | week | month | year | quarter | min1 | min5 | min15 | min30 | min60 | min120", required = true)
    private String type;

    @NotNull(message = "时间单位不能为空")
    @ApiModelProperty(value = "时间单位")
    private UnitEnum unit;

    @NotNull(message = "时间单位对应的值不能为空")
    @ApiModelProperty(value = "时间单位对应的数值")
    private Integer num;

    @ApiModelProperty(value = "指标数据")
    private List<Integer> indicators;

    @ApiModelProperty(value = "是否计算买卖点")
    private boolean calculateBuySellPoint;
}
