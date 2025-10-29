package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class BrokersProportionResp implements Serializable {

    private static final long serialVersionUID = 2168577355760317257L;

    @ApiModelProperty(value="股票快照信息")
    private List<StockPrice> stockPriceList;


    @ApiModelProperty(value="前五经纪商集合")
    private List<List<BrokersProportionDetail>> brokersProportionList;

    /*

    @ApiModelProperty(value = "经纪商持股比例Top1")
    private List<BrokersProportionDetail> brokersProportionTop1;

    @ApiModelProperty(value = "经纪商持股比例Top2")
    private List<BrokersProportionDetail> brokersProportionTop2;

    @ApiModelProperty(value = "经纪商持股比例Top3")
    private List<BrokersProportionDetail> brokersProportionTop3;

    @ApiModelProperty(value = "经纪商持股比例Top4")
    private List<BrokersProportionDetail> brokersProportionTop4;

    @ApiModelProperty(value = "经纪商持股比例Top5")
    private List<BrokersProportionDetail> brokersProportionTop5;

     */

    @ApiModelProperty(value ="港股通(沪)A00003" )
    private List<BrokersProportionDetail> brokersProportionHKStockConnectShanghai;

    @ApiModelProperty(value ="港股通(深)A00004" )
    private List<BrokersProportionDetail> brokersProportionHKStockConnectShenzhen;

    @ApiModelProperty(value = "前5经纪商合计")
    private List<TopBrokersProportionDetail> brokersProportionSum5;

    @ApiModelProperty(value = "前10经纪商合计")
    private List<TopBrokersProportionDetail> brokersProportionSum10;

    @ApiModelProperty(value = "前20经纪商合计")
    private List<TopBrokersProportionDetail> brokersProportionSum20;

}
