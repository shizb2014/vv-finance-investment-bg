package com.vv.finance.investment.bg.entity.broker.allBroker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustrySearch implements Serializable {
    private static final long serialVersionUID = 9102063059718468628L;
    @ApiModelProperty("股票ID")
    private Long industryId;
    @ApiModelProperty(value = "行业代码")
    private String industryCode;
    @ApiModelProperty(value = "行业名称")
    private String industryName;
}
