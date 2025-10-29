package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: wsliang
 * @Date: 2021/9/14 10:11
 **/
@Data
@ToString
@ApiModel("日历按日期分组")
public class InformationGroupVo<T> implements Serializable {

    private static final long serialVersionUID = -3517274497749179124L;

    @ApiModelProperty("日期")
    private Long date;

    @ApiModelProperty("数据")
    private List<T> list;
}
