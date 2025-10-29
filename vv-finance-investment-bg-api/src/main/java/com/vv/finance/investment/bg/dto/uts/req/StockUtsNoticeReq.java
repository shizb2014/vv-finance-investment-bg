package com.vv.finance.investment.bg.dto.uts.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenyu
 * @date 2021/7/12 13:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockUtsNoticeReq {
    @ApiModelProperty("类型")
    private Integer type;
    @ApiModelProperty("股票代码")
    private String stockCode;
}
