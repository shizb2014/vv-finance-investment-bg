package com.vv.finance.investment.bg.dto.f10;

import com.vv.finance.common.bean.SimplePageResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/8/17 19:04
 */
@Data
public class DividendComparison extends SimplePageResp<DividendComparison.DividendComparisonDetail> {
    private static final long serialVersionUID = -3709422138054808766L;
    @ApiModelProperty(value = "所属行业")
    private String industry;
    @ApiModelProperty(value = "所属行业")
    private String industryCode;

    @Data
    public static class DividendComparisonDetail extends StockBaseInfo {

        private static final long serialVersionUID = 4085918001844272067L;
        @ApiModelProperty(value = "上市日期")
        private String  listingDate;
        @ApiModelProperty(value = "上市以来累计分红金额")
        private BigDecimal  cumulativeDividendAmount;

        @ApiModelProperty(value = "所属行业")
        private String industry;
        @ApiModelProperty(value = "所属行业代码")
        private String industryCode;
        @ApiModelProperty(value = "总市值")
        private BigDecimal  totalMarketValue;
        @ApiModelProperty(value = "流通市值")
        private BigDecimal circulationMarketValue;
        @ApiModelProperty("股票类型")
        private Integer stockType;
        @ApiModelProperty("区域代码类型")
        private Integer regionType;



    }

}
