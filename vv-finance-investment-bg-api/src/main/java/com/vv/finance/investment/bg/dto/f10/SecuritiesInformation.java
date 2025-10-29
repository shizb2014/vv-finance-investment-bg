package com.vv.finance.investment.bg.dto.f10;


import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author hamilton
 * @date 2021/8/17 15:49
 */
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class SecuritiesInformation extends StockBaseInfo {

    private static final long serialVersionUID = -1178418755452675228L;
    @ApiModelProperty(value = "证券类型")
    private String type;

    @ApiModelProperty(value = "是否沪股通标的")
    private String shanghaiStockConnect;
    @ApiModelProperty(value = "上市交易所")
    private String listedExchange;

    @ApiModelProperty(value = "是否深股通标的")
    private String shenGuTong;

    @ApiModelProperty(value = "上市板块")
    private String market;

    @ApiModelProperty(value = "A股代码")
    private String aStockCode;

    @ApiModelProperty(value = "上市日期")
    private String marketDate;

    @ApiModelProperty(value = "是否可卖空标的")
    private String shortSelling;

    @ApiModelProperty(value = "交易状态")
    private String tradeState;

    @ApiModelProperty(value = "恒生指数成份股标记")
    private String hangSengMarker;

    @ApiModelProperty(value = "交易货币")
    private String tradeCurrency;

    @ApiModelProperty(value = "国企指数成份股标记")
    private String stateEnterpriseMarker;

    @ApiModelProperty(value = "买卖单位(每手股数)")
    private Long lotSize;

    @ApiModelProperty(value = "红筹指数成份股标记")
    private String redChipMarker;

    @ApiModelProperty(value = "IPO资料")
    private List<IpoInformation> ipoInformationList= Lists.newArrayList();

    @ApiModelProperty(value = "招股说明书url")
    private String instructionUrl;

    /**
     * IPO资料
     */
    @Data
    public static class IpoInformation implements Serializable{

        private static final long serialVersionUID = -2645867831228277770L;
        @ApiModelProperty(value = "发行资料")
        private IssueInformation issueInformation;

        @ApiModelProperty(value = "发行数量")
        private IssueInQuantity issueInQuantity;

        @ApiModelProperty(value = "募集金额")
        private AmountRaised amountRaised;

        @ApiModelProperty(value = "申请日期")
        private String applyDate;

        @ApiModelProperty(value = "申购起始日")
        private String subscriptionStartDate;

        @ApiModelProperty(value = "申购截止日")
        private String subscriptionDeadline;

        @ApiModelProperty(value = "定价日")
        private String pricingDate;

        @ApiModelProperty(value = "发行结果公告日")
        private String issueResultDate ;

        @ApiModelProperty(value = "预计上市日")
        private String expectedListingDate ;



    }
    /**
     * Amount raised 募集金额
     */
    @Data
    public static class AmountRaised implements Serializable{
        private static final long serialVersionUID = -5241084538855820642L;
        @ApiModelProperty(value = "发售股份后市值")
        private String afterSaleMarketValue;

        @ApiModelProperty(value = "首发集资总额")
        private String totalInitialFundRaising;

        @ApiModelProperty(value = "发售集资净额")
        private String saleNetAmount;

        @ApiModelProperty(value = "首发每股面值")
        private BigDecimal initialParValue;

        @ApiModelProperty(value = "每股面值货币")
        private String currency ;



    }

    /**
     * 发行数量
     */
    @Data
    public static class IssueInQuantity implements Serializable{
        private static final long serialVersionUID = 4782065427427820585L;
        @ApiModelProperty(value = "实际发售股份数量(股)")
        private BigDecimal actualSale;

        @ApiModelProperty(value = "计划发售股份数量(股)")
        private BigDecimal planSale;

        @ApiModelProperty(value = "超额配售股份数量(股)")
        private BigDecimal overAllotment;

        @ApiModelProperty(value = "售股股东股份数量(股)")
        private BigDecimal sellingStockholder;

        @ApiModelProperty(value = "香港发售股份数量(股)")
        private BigDecimal hongKongSale;

        @ApiModelProperty(value = "国际发售股份数量(股)")
        private BigDecimal internationalSale;

        @ApiModelProperty(value = "优先发售股份数量(股)")
        private BigDecimal prioritySale;



    }
    /**
     * 发行资料
     */
    @Data
    public static class  IssueInformation implements Serializable{
        private static final long serialVersionUID = 572698895685612220L;
        @ApiModelProperty(value = "招股状态")
        private String tradeState;

        @ApiModelProperty(value = "上市板块")
        private String market;

        @ApiModelProperty(value = "发行货币")
        private String issueCurrency;

        @ApiModelProperty(value = "发行价格")
        private BigDecimal issuePrice;

        @ApiModelProperty(value = "发行方式")
        private String issueType;

        @ApiModelProperty(value = "入场费")
        private BigDecimal entranceFee;

        @ApiModelProperty(value = "最高发行价")
        private BigDecimal maximumIssuePrice;

        @ApiModelProperty(value = "最低发行价")
        private BigDecimal minimumIssuePrice;

        @ApiModelProperty(value = "一手中签率")
        private BigDecimal oneHandSigningRate;

        @ApiModelProperty(value = "公开认购倍数")
        private BigDecimal publicSubscriptionMultiple;

        @ApiModelProperty(value = "首发买卖单位(股)")
        private Long firstLaunchLotSize;

        @ApiModelProperty(value = "所属行业[子行业]")
        private String subIndustry;

        @ApiModelProperty(value = "招股说明书")
        private Prospectus prospectus;

        @ApiModelProperty(value = "保荐人")
        private String sponsor;



    }
}
