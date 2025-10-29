package com.vv.finance.investment.bg.entity.f10.shareholder;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/18 11:03
 * @Version 1.0
 * 股权变动
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EquityChange  implements Serializable {
    private static final long serialVersionUID = 9077351615048463903L;

    /**
     * 公布日期
     */
    @ApiModelProperty(value = "公布日期")
    private String releaseDate;
    /**
     * 股东名称
     */
    @ApiModelProperty(value = "股东名称")
    private String shareholdingName;
    /**
     * 持仓类型：好仓、淡仓
     */
    @ApiModelProperty(value = "持仓类型")
    private String positionType;
    /**
     * 持仓类型：rise/fall
     */
    @ApiModelProperty(value = "持仓类型（Key）")
    private String positionKey;
    /**
     * 持股变化
     */
    @ApiModelProperty(value = "持股量变化(股)")
    private BigDecimal positionChange;
    /**
     * 每股均价
     */
    @ApiModelProperty(value = "每股平均价")
    private BigDecimal averagePrice;
    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currencyType;
    /**
     * 变动后数量
     */
    @ApiModelProperty(value = "变动后数量(股)")
    private BigDecimal num;
    /**
     * 变动后持股比例
     */
    @ApiModelProperty(value = "变动后持股比例")
    private BigDecimal ratio;
}
