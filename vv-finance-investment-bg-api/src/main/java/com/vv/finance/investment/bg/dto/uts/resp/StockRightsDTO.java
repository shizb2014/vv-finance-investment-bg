package com.vv.finance.investment.bg.dto.uts.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: GMC
 * @Date: 2024/9/19 15:10
 * @Description:
 * @version: 1.0
 */
@ApiModel("股权股票")
@Data
public class StockRightsDTO implements Serializable {
    /**
     * 股权股票code
     */
    @ApiModelProperty("股权股票code")
    private String code;
    /**
     * 股权股票名称
     */
    @ApiModelProperty("股权股票名称")
    private String name;
    /**
     * 股权股票最初上市日期
     */
    @ApiModelProperty("股权股票最初上市日期 yyyyMMdd")
    private Long startListingDate;
    /**
     * 股权股票最后上市日期
     */
    @ApiModelProperty("股权股票最后上市日期 yyyyMMdd")
    private Long endListingDate;
    /**
     * 股权股票id
     */
    @ApiModelProperty("股权股票id")
    private Long stockId;
}
