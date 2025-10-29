package com.vv.finance.investment.bg.entity.f10.shareholder;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/17 17:29
 * @Version 1.0
 */
@Data
@Builder
@ToString
public class ShareholdingPop implements Serializable {

    private static final long serialVersionUID = 5662500434631473780L;

    /**
     * 个人
     */
    @ApiModelProperty(value = "个人")
    private Val personal;
    /**
     * 机构
     */
    @ApiModelProperty(value = "机构")
    private Val mechanism;
    /**
     * 董事
     */
    @ApiModelProperty(value = "董事")
    private Val director;
    /**
     * 更新日期
     */
    @ApiModelProperty(value = "更新日期")
    private Long updateDate;
}
