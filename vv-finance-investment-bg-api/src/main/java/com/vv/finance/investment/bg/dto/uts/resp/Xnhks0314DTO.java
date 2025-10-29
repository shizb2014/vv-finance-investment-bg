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
public class Xnhks0314DTO implements java.io.Serializable{

    private static final long serialVersionUID = 1462068702062706300L;
    @ApiModelProperty(value = "股票code")
    private String stockCode;

    /**
     * @see com.vv.finance.common.enums.XrTypeEnum
     */
    @ApiModelProperty(value = "除权除息事件类型(CD=股息，SD=特别股息，OD=其他派送，BS=送股，BW=红利股权证，SS=拆股，SC=并股，RS=供股，OO=公开售股)")
    private String xrType;

    @ApiModelProperty(value = "股息派息日(CD和SD会有值)")
    private LocalDate dividendDate;

    @ApiModelProperty(value = "股权登记日(CD和SD会有值)")
    private LocalDate registerDate;

    @ApiModelProperty(value = "除权（净）日（股权登记日后一个交易日）")
    private LocalDate xrDate;

//    @ApiModelProperty(value = "除权结束日期(SS和SC会有值)")
//    private LocalDate xrEndDate;

    @ApiModelProperty(value = "倍数因子（SS,SC解析F006V的文本，其他还是用F004N）")
    private BigDecimal rateFactor;

    @ApiModelProperty(value = "增减因子")
    private BigDecimal changeFactor;

    @ApiModelProperty(value = "除权描述")
    private String xrDesc;
}
