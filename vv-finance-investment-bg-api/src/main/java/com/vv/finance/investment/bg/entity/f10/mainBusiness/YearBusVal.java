package com.vv.finance.investment.bg.entity.f10.mainBusiness;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/17 16:20
 * @Version 1.0
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class YearBusVal implements Serializable {

    private static final long serialVersionUID = -1930625739633864737L;

    @ApiModelProperty(value = "业务类型值")
    private List<BusVal> busValList;
    @ApiModelProperty(value = "地区类型值")
    private List<AreaVal> areaValList;
    @ApiModelProperty(value = "年份")
    private String year;

}
