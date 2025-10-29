package com.vv.finance.investment.bg.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

/**
 * @Author:maling
 * @Date:2024/2/28
 * @Description:休市信息对象 注意：该对象使用是建立在交易日的判断基础上
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradingStateResp implements Serializable {
    private static final long serialVersionUID = -5493163083436932250L;

    @ApiModelProperty(value = "上午是否临时休市 true:是 false:否")
    private Boolean morningClose;

    @ApiModelProperty(value = "下午是否临时休市 true:是 false:否")
    private Boolean afternoonClose;
}