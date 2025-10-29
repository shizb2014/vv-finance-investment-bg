package com.vv.finance.investment.bg.entity.f10.mainBusiness;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @Author: wsliang
 * @Date: 2021/9/2 14:54
 **/
@Data
//@Builder
@ToString
public class MainBusinessData implements Serializable {

    private static final long serialVersionUID = 1742835908450583366L;

    @ApiModelProperty(value = "列表数据")
    private List<YearBusVal> yearBusVals;

    @ApiModelProperty(value = "业务类型集合")
    private List<String> business;

    @ApiModelProperty(value = "地区集合")
    private List<String> areas;

    @ApiModelProperty(value = "业务更新时间")
    private Long busDate;

    @ApiModelProperty(value = "地区更新时间")
    private Long areaDate;

    @ApiModelProperty(value = "币种单位 港元/人民币 等(业务)")
    private String busCurrency;

    @ApiModelProperty(value = "币种单位 港元/人民币 等(地区)")
    private String areaCurrency;

}
