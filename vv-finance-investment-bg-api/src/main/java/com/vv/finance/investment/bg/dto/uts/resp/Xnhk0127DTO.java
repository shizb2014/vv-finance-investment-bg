package com.vv.finance.investment.bg.dto.uts.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @Description Xnhk0127实体对应的DTO
 * @Author liuxing
 * @Create 2023/9/14 15:02
 */
@Data
public class Xnhk0127DTO implements java.io.Serializable{

    private static final long serialVersionUID = 1462068702062706300L;
    @ApiModelProperty(value = "股票code")
    private String stockCode;
    @ApiModelProperty("股票ID")
    private Long stockId;
    /**
     * @see com.vv.finance.common.enums.XrTypeEnum
     */
    @ApiModelProperty(value = "除权除息事件类型(CD=股息，SD=特别股息，OD=其他派送，BS=送股，BW=红利股权证，SS=拆股，SC=并股，RS=供股，OO=公开售股)")
    private String xrType;

    @ApiModelProperty(value = "股息派息日(CD和SD会有值)")
    private LocalDate dividendDate;

    @ApiModelProperty(value = "股权登记日(CD和SD会有值)")
    private LocalDate registerDate;

    @ApiModelProperty(value = "除权（净）日（或股权登记日后一个交易日）")
    private LocalDate xrDate;

    @ApiModelProperty(value = "除权结束日期(SS和SC会有值)")
    private LocalDate xrEndDate;

    @ApiModelProperty(value = "倍数因子（SS,SC解析F006V的文本，其他还是用F004N）")
    private BigDecimal rateFactor;

    @ApiModelProperty(value = "增减因子")
    private BigDecimal changeFactor;

    @ApiModelProperty(value = "除权描述")
    private String xrDesc;

    @ApiModelProperty(value = "拆并股详情")
    private SsScJson ssScJson;


    @ApiModelProperty(value = "股票市场类型 0:港股 1:美股 2:沪深")
    private Integer regionType;

    @Data
    public static class SsScJson implements java.io.Serializable{

        private static final long serialVersionUID = 6809321871303314797L;
        //事件类型为SS,SC时会有以下字段
        @ApiModelProperty(value = "股票名称")
        private String stockName;

        @ApiModelProperty(value = "临时股票code")
        private String tempStockCode;

        @ApiModelProperty(value = "临时股票名称")
        private String tempStockName;

        @ApiModelProperty("每手股数")
        private Integer lotSize;

        @ApiModelProperty("原股票暂停交易期（示例：2023/05/03-2023/05/16 或 空）")
        private String stopTradingPeriod;

        @ApiModelProperty("原股票并行交易期（示例：2023/05/17-2023/06/07）")
        private String parallelTradingPeriod;

        @ApiModelProperty("并行交易原因")
        private String parallelTradingReason;
    }

}
