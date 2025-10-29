package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author hamilton
 * @date 2021/7/23 15:03
 */
@Data
@Builder
@ToString
public class F10CommonRequest {
    private String stockCode;
    private String reportType;
    private Long reportTime;
    private boolean filterPq; // 过滤P和Q5类型
    @ApiModelProperty(value = "报告种类 0:全部 ,1:年报 ,2:中报 3:季报")
    private int reportId;
    @ApiModelProperty(value = "截至日期")
    private String endDate;
}
