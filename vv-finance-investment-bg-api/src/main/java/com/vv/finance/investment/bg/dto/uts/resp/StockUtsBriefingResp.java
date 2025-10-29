package com.vv.finance.investment.bg.dto.uts.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2021/7/13 10:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockUtsBriefingResp implements Serializable {
    private static final long serialVersionUID = -280458786483350870L;
    @ApiModelProperty("公司名称")
    private String name;

    @ApiModelProperty("所属市场")
    private String marketType;

    @ApiModelProperty("上市日期")
    private Long ipoTime;

    @ApiModelProperty("董事长")
    private String chairmanName;

    @ApiModelProperty("发行价格")
    private BigDecimal ipoPrice;

    @ApiModelProperty("发行数量")
    private BigDecimal ipoNum;

    @ApiModelProperty("主要业务")
    private String mainBusiness;


}
