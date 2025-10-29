package com.vv.finance.investment.bg.entity.f10.shareholder;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * 股东
 * @author yangpeng
 * @date 2023/10/17
 */
@Data
@Builder
@ToString
public class StockPopAndChange implements Serializable {

    private static final long serialVersionUID = -4364804184217279261L;

    /**
     * 日期
     */
    @ApiModelProperty(value = "日期")
    private Long date;

    @ApiModelProperty(value = "所有股东类型（A,C,D,F,I,L,T,O）")
    private List<String> holderTypes;

    /**
     * 股东列表
     */
    @ApiModelProperty(value = "股东列表")
    private List<TimeStockHolder> records;

    @Data
    @Builder
    @ToString
    public static class TimeStockHolder implements Serializable {

        private static final long serialVersionUID = 1018868985811417460L;

        @ApiModelProperty(value = "年份")
        private Long time;

        @ApiModelProperty(value = "股东")
        private List<StockHolder> stockHolders;
    }
}
