package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Author: wsliang
 * @Date: 2021/9/13 15:39
 **/
@Data
@ToString
//@Builder
@ApiModel("7*24资讯详情")
public class TwentyFourHourNewsVo implements Serializable {

    private static final long serialVersionUID = 6695845173477835305L;

    @ApiModelProperty("发布时间 时分秒")
    private String time;

    @ApiModelProperty("文本内容")
    private String text;

    @ApiModelProperty("分类")
    private String type;

    @ApiModelProperty("资讯id")
    private Long newsid;

    @ApiModelProperty("id")
    private String id;

}
