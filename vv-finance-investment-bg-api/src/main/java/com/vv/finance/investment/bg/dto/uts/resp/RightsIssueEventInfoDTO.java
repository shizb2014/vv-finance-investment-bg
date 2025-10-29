package com.vv.finance.investment.bg.dto.uts.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author bryce
 * @date 2023-08-14 13:58
 */
@Data
@Accessors(chain = true)
public class RightsIssueEventInfoDTO implements Serializable {
    private static final long serialVersionUID = -7481950821137561032L;

    @ApiModelProperty(value = "股票代码")
    private String stockCode;

    @ApiModelProperty(value = "除权日")
    private LocalDate exRightDate;

    @ApiModelProperty(value = "除权事件")
    private String exRightEvent;

    @ApiModelProperty(value = "除权类型")
    private String exRightType;
}
