package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @Author: wsliang
 * @Date: 2021/9/13 15:39
 **/
@Data
@ToString
//@Builder
@ApiModel("7*24资讯详情-外层")
public class GroupTwentyFourHourNewsVo implements Serializable {

    private static final long serialVersionUID = 6695845173477835305L;

    @ApiModelProperty("发布日期")
    private Long date;

    List<TwentyFourHourNewsVo> list;

}
