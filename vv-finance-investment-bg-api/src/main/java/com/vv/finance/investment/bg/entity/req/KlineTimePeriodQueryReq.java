package com.vv.finance.investment.bg.entity.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author hamilton
 * @date 2021/2/3 17:28
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ApiModel("k线")
@Data
public class KlineTimePeriodQueryReq implements Serializable {


    private static final long serialVersionUID = 2451736755648272127L;
    @ApiModelProperty(value = "股票代码", required = true)
    private String code;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;
    @ApiModelProperty(value = "forward：前复权；backward：后复权，为空 不复权")
    private String adjhkt;
}
