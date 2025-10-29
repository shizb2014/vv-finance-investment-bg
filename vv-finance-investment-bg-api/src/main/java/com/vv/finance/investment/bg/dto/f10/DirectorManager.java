package com.vv.finance.investment.bg.dto.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/8/17 18:34
 * @desc  董事高管模型
 */
@Data
public class DirectorManager implements Serializable {
    private static final long serialVersionUID = 3536759643983045297L;
    @ApiModelProperty(value = "顺序")
    private Integer sort;
    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "职务")
    private String post;


    @ApiModelProperty(value = "年薪")
    private BigDecimal yearlySalary;

    @ApiModelProperty(value = "年薪单位")
    private String yearlySalaryUnit;

    @ApiModelProperty(value = "任职日期")
    private String holdPostDate;
    @ApiModelProperty(value = "任职日期long" ,hidden = true)
    private Long holdPostDateLong;
    @ApiModelProperty(value = "年龄")
    private Integer age;

    @ApiModelProperty(value = "更新日期")
    private String updateDate;

    @ApiModelProperty(value = "详情")
    private String detail;




}
