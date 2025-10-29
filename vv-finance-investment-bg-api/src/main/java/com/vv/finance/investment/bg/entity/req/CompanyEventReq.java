package com.vv.finance.investment.bg.entity.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("公司事件请求")
public class CompanyEventReq implements Serializable {
    private static final long serialVersionUID = 820613075096183L;

    @ApiModelProperty(value = "代码", required = true)
    private String code;

    @ApiModelProperty(value = "开始时间")
    private Long startTime;

    @ApiModelProperty(value = "结束时间")
    private Long endTime;

    @ApiModelProperty(value = "类型 -- day,week,month,quarter,year")
    private String type;
}
