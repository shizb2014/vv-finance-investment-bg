package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * CapitalDistriButionVo
 * @author wsl
 * @date 2020/11/30 15:12
 */
@Data
public class CapitalDistributionVo implements Serializable {

    private static final long serialVersionUID = -3902000971518271214L;
    @ApiModelProperty(value = "更新时间")
    private Long time;

    @ApiModelProperty("资金分布")
    private List<CapitalDistributionDTO> capitalDistributionList;
}
