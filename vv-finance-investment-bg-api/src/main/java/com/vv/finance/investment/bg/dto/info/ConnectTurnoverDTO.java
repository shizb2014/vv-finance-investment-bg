package com.vv.finance.investment.bg.dto.info;

import com.vv.finance.common.entity.receiver.ConnectTurnover;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenyu
 * @date 2021/4/22 14:49
 */
@Data
public class ConnectTurnoverDTO implements Serializable {
    private static final long serialVersionUID = -893567159134441586L;
    @ApiModelProperty("深股通")
    private List<ConnectTurnoverBase> szTurnover;
    @ApiModelProperty("沪股通")
    private List<ConnectTurnoverBase> shTurnOver;
}
