package com.vv.finance.investment.bg.entity.broker.allBroker;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketValueTrend implements Serializable {
    private static final long serialVersionUID = 5655190041382435001L;
    @ApiModelProperty("股票ID")
    private Long stockId;
    @ApiModelProperty(value = "行业代码")
    private String industryCode;
    @ApiModelProperty(value = "行业名称")
    private String industryName;
    @ApiModelProperty(value = "市值数据与日期")
    private List<MarketValueAndDate> valueAndDateList;
    @ApiModelProperty(value = "数据更新时间")
    private Long date;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("市值数据与日期")
    public static class MarketValueAndDate implements Serializable {
        private static final long serialVersionUID = 2897413524796585181L;
        @ApiModelProperty(value = "数值")
        private BigDecimal val;
        @ApiModelProperty(value = "日期")
        private Long date;
        @ApiModelProperty(value = "总市值")
        private BigDecimal totalMarketValue;
    }
}
