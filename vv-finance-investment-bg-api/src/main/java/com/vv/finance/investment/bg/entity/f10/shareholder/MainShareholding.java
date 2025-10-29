package com.vv.finance.investment.bg.entity.f10.shareholder;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/17 17:55
 * @Version 1.0
 */
@Data
@ToString
@NoArgsConstructor
public class MainShareholding implements Serializable {

    private static final long serialVersionUID = -8519229426728245291L;
    /**
     * 股东名称
     */
    @ApiModelProperty(value = "股东名称")
    private String shareholdingName;
    /**
     * 持股数量(股)
     */
    @ApiModelProperty(value = "持股数量(股)")
    private BigDecimal num;
    /**
     * 持股比例
     */
    @ApiModelProperty(value = "持股比例")
    private BigDecimal ratio;
    /**
     * 持股比例变动
     */
    @ApiModelProperty(value = "持股比例变动")
    private BigDecimal ratioChange;
    /**
     * 股份类型
     */
    @ApiModelProperty(value = "股份类型")
    private String type;

    /**
     * 最新更新时间
     */
    @ApiModelProperty("最新更新时间")
    private String updateDate;
}
