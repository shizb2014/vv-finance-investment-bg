package com.vv.finance.investment.bg.dto.quotationconfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * description: UserQuotationConfigResDTO
 * date: 2022/8/10 14:46
 * author: fenghua.cai
 */
@Data
@ApiModel
public class UserQuotationConfigResDTO implements Serializable {

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "时间范围")
    private int num;

    @ApiModelProperty(value = "时间单位（year,month,day）")
    private String unit;

    @ApiModelProperty(value = "周期(min1,min5,min15,min30,min60,min120,daily,weekly,monthly,quarterly,yearly)")
    private String cycle;

    @ApiModelProperty(value = "区域类型(0:港股  1:美股)")
    private Integer regionType;
}
