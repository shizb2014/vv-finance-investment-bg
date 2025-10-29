package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @Author: wsliang
 * @Date: 2021/9/14 10:14
 **/
@Data
@ToString
@ApiModel("日历资讯-新股")
public class NewShareVo implements Serializable {
    private static final long serialVersionUID = -1108586368983775787L;

    /**
     * 股票代码
     */
    @ApiModelProperty("股票代码")
    private String SECCODE;

    @ApiModelProperty(value = "股票代码")
    private String stockCode;
    /**
     * 股票名称
     */
    @ApiModelProperty("股票名称")
    private String stockName;
    /**
     * 最低招股价
     */
    @ApiModelProperty("最低招股价")
    private BigDecimal minPrice;

    /**
     * 最高招股价
     */
    @ApiModelProperty("最高招股价")
    private BigDecimal maxPrice;

    @ApiModelProperty("最终招股价 当minPrice,maxPrice都为空的情况下使用finalPrice")
    private BigDecimal finalPrice;

    /**
     * 招股书链接
     */
    @ApiModelProperty("招股书链接")
    private String stockLink;

    /**
     * 认购开始时间
     */
    @ApiModelProperty("认购开始时间")
    private Long subscribeStartDate;

    /**
     * 认购结束时间
     */
    @ApiModelProperty("认购结束时间")
    private Long subscribeEndDate;

    /**
     * 公布中签日期
     */
    @ApiModelProperty("公布中签日期")
    private Long publicityDate;

    /**
     * 上市时间
     */
    @ApiModelProperty("上市时间")
    private Long marketDate;

    /**
     * 状态 0:认购中 1:待公布中签 2:公布中签 3:上市
     */
    @ApiModelProperty("状态 0:认购中 1:待公布中签 2:公布中签 3:上市")
    private Integer marketStatus;

    /**
     * 是否可跳转(标蓝)
     */
    @ApiModelProperty("是否可跳转(标蓝)")
    private Boolean isMarket = false;

    /**
     * 每手股数
     */
    @ApiModelProperty("每手股数")
    private Long shareNum;

    /**
     * 入场费
     */
    @ApiModelProperty("入场费")
    private BigDecimal entranceFee;

    @ApiModelProperty("交易所")
    private String exchange;

    @ApiModelProperty("区域代码 0-中国香港 1-美国 2-中国大陆 3-新加坡")
    private Integer regionType;

    @ApiModelProperty("金融产品类型 1-正股 2-ETF 3-权证 4-指数 5-板块 6-其他 7-基金 8-债券 9-ETN；决定点击跳转的页面类型是指数、股票、权证、基金、期货等；")
    private Integer stockType;

    @AllArgsConstructor
    public enum MarketStatus {
        ZERO(0, "认购中"),
        ONE(1, "待公布中签"),
        TWO(2, "公布中签"),
        THREE(3, "上市"),
        ;

        private Integer code;
        private String value;

        public Integer getCode() {
            return code;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewShareVo that = (NewShareVo) o;
        return Objects.equals(SECCODE, that.SECCODE);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SECCODE);
    }
}
