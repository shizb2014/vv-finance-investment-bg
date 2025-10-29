package com.vv.finance.investment.bg.dto.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @公司：微微科技有限公司（金融事业部）
 * @描述：交易日历事件dto
 * @作者：Liam（梁殿豪）
 * @邮箱：liangdianhao@vv.cn
 * @时间：2021/8/17 16:38
 * @版本：1.0
 */
@Data
public class FinanceCalenderDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "0点时间戳")
    private Long time;
    @ApiModelProperty(value = "是否交易日")
    private boolean tradeDayFlag;
    @ApiModelProperty(value = "是否有新股事件")
    private boolean newStockEventFlag;
    @ApiModelProperty(value = "是否有除权事件")
    private boolean xrEventFlag;
    @ApiModelProperty(value = "是否有公司动向事件")
    private boolean companyTrendFlag;
    @ApiModelProperty(value = "是否有经济事件")
    private boolean economicEventFlag;
}
