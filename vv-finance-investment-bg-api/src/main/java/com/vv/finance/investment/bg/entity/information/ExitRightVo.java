package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Author: wsliang
 * @Date: 2021/9/14 10:14
 **/
@Data
@ToString
@ApiModel("日历资讯-除权")
public class ExitRightVo implements Serializable {

    @ApiModelProperty("翻页与刷新的坐标值")
    private String xdbmask;

    @ApiModelProperty("发布日期")
    private Long date;

    @ApiModelProperty("股票代码")
    private String SECCODE;
    @ApiModelProperty("股票名称")
    private String stockName;

    @ApiModelProperty("派息")
    private String dividend;
    @ApiModelProperty("派息类别")
    private String dividendType;
    @ApiModelProperty("派送日期")
    private Long dividendDate;

}
