package com.vv.finance.investment.bg.entity.f10.trends;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Tolerate;

import java.io.Serializable;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/20 10:05
 * @Version 1.0
 * 停复牌
 */
@Data
@Builder
@ToString
public class StopAndResume implements Serializable {

    private static final long serialVersionUID = -3734908546198944431L;

    @ApiModelProperty(value = "公布日期")
    private Long releaseDate;
    @ApiModelProperty(value = "停牌日期")
    private Long stopDate;
    @ApiModelProperty(value = "复牌日期")
    private Long resumeDate;
    @ApiModelProperty(value = "停牌原因")
    private String stopReason;

    @ApiModelProperty(value = "代码")
    private String stockCode;

    @Tolerate
    public StopAndResume() {

    }
}
