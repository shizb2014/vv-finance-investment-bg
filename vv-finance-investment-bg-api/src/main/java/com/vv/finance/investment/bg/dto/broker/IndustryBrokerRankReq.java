package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author qinxi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("行业经纪商榜单请求")
public class IndustryBrokerRankReq implements Serializable {
    private static final long serialVersionUID = 8618929186251084021L;

    @ApiModelProperty(value = "行业ID")
    private Long industryId;

    @ApiModelProperty(value = "行业代码")
    private String industryCode;

    @ApiModelProperty(value = "排序字段")
    private String sortKey;

    @ApiModelProperty(value = "排序方式 asc升序 desc降序")
    private String sort;

    @ApiModelProperty(value = "日期类型 近一日：type = 0; 近五日：type = 1;近十日：type = 2; 近二十日：type = 3; 近六十日：type=4;")
    private Integer type;

    @ApiModelProperty(value = "开始日期", hidden = true)
    private LocalDate startDate;

    @ApiModelProperty(value = "结束日期", hidden = true)
    private LocalDate endDate;
}
