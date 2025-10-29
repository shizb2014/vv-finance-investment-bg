package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Author: wsliang
 * @Date: 2021/9/14 10:11
 **/
@Data
@ToString
@ApiModel("日历资讯-财经事件")
public class FinancialEventVo implements Serializable {

    private static final long serialVersionUID = -4013941928216487437L;
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("资讯id")
    private Long newsid;

    @ApiModelProperty("发布时间 日期+时分秒")
    private Long time;

    @ApiModelProperty("资讯内容")
    private String text;

    @ApiModelProperty("地区")
    private String areaName;

    /**
     * 市场预测
     */
    @ApiModelProperty("市场预测")
    private String prediction;

    /**
     * 重要等级
     */
    @ApiModelProperty("重要等级")
    private Long level;

    /**
     * 前值
     */
    @ApiModelProperty("前值")
    private String lastValue;

    /**
     * 公布值
     */
    @ApiModelProperty("公布值")
    private String publishValue;
}
