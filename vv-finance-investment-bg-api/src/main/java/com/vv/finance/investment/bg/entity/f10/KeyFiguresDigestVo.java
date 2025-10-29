package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: wsliang
 * @Date: 2021/9/9 10:43
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyFiguresDigestVo implements Serializable {
    private static final long serialVersionUID = 3975706599786357199L;
    /**
     * 营业收入
     */
    @ApiModelProperty("营业收入")
    private F10Val operatingRevenue;
    /**
     * 净利润
     */
    @ApiModelProperty("净利润")
    private F10Val netProfits;
}
