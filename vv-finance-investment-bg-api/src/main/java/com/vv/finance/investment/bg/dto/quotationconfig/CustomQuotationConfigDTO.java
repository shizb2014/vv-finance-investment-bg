package com.vv.finance.investment.bg.dto.quotationconfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * description: CustomQuotationConfigDTO
 * date: 2022/8/10 13:50
 * author: fenghua.cai
 */
@Data
@ApiModel("添加用户行情自定义周期请求体")
public class CustomQuotationConfigDTO implements Serializable {

    @ApiModelProperty(value = "时间范围", example = "5", required = true)
    private int num;

    @ApiModelProperty(value = "时间单位（year,month,day）", example = "day", required = true)
    private String unit;

    @ApiModelProperty(value = "周期(min1,min5,min15,min30,min60,min120,day,week,month,quarter,year)", example = "day", required = true)
    private String cycle;

    @ApiModelProperty(value = "区域类型", example = "0", required = false)
    private Integer regionType;
}
