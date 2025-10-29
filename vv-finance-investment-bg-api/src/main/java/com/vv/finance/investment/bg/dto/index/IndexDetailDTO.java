package com.vv.finance.investment.bg.dto.index;

import com.vv.finance.common.entity.common.StockSnapshot;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenyu
 * @date 2020/10/29 19:11
 */
@Data
public class IndexDetailDTO extends StockSnapshot {

    private static final long serialVersionUID = -8359035431264114072L;
    @ApiModelProperty(value = "自选标识")
    private Boolean optional;

}
