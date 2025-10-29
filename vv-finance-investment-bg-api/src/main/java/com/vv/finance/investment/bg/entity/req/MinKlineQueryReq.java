package com.vv.finance.investment.bg.entity.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author hamilton
 * @date 2020/10/29 17:16
 */
@Data
@ApiModel("k线")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MinKlineQueryReq implements Serializable {

    @ApiModelProperty(value = "股票代码", required = true)
    private String code;
    @ApiModelProperty(value = "k线类型  1 | 10 | 15 | 30 | 60 | 120 ", required = true)
    private Integer type;

    @ApiModelProperty(value = "数量，必须为大于0的数字")
    private Integer num;

    @ApiModelProperty(value = "从哪天往后，默认今天，用 以向后去增量")
    private Date time;
    @ApiModelProperty(value = "forward：前复权；backward：后复权，为空 不复权")
    private String adjhkt;

}
