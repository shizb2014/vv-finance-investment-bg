package com.vv.finance.investment.bg.dto.broker;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author qinxi
 * @date 2024/6/20 17:20
 * @description:
 */
@Data
public class IndustryBrokersProportionDTO implements Serializable {


    private static final long serialVersionUID = -1736132340735624356L;

    @ApiModelProperty("深港通")
    private List<BrokersProportionItem> brokersProportionHKStockConnectShenzhen;

    @ApiModelProperty("沪港通")
    private List<BrokersProportionItem> brokersProportionHKStockConnectShanghai;

    @ApiModelProperty("持股市值排行前5的经纪商")
    private List<List<BrokersProportionItem>> brokersProportionList;

    @ApiModelProperty("价格")
    private List<StockPriceItem> stockPriceList;


    @Data
    @ApiModel("价格")
    public static class StockPriceItem implements Serializable {

        private static final long serialVersionUID = 8562690015117839616L;

        @ApiModelProperty("时间戳")
        private Long timestamp;

        @ApiModelProperty("日期")
        private String date;

        @ApiModelProperty("行业Id")
        private Long industryId;

        @ApiModelProperty("行业code")
        private String industryCode;

        @ApiModelProperty("行业名称")
        private String industryName;

        @ApiModelProperty("行业收盘价")
        private BigDecimal close;

        @ApiModelProperty("行业涨跌幅")
        private BigDecimal chgPct;

    }

    @Data
    @ApiModel("经纪商趋势")
    public static class BrokersProportionItem implements Serializable {

        private static final long serialVersionUID = 8562690015117839616L;

        @ApiModelProperty("时间戳")
        private Long timestamp;

        @ApiModelProperty("日期")
        private String date;

        @ApiModelProperty("经纪商ID")
        private String brokerId;

        @ApiModelProperty("经纪商名称")
        private String brokerName;

        @TableField("持股市值")
        private BigDecimal marketVal;

    }



}
