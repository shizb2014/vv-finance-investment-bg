package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * CapitalDistriButionDTO
 *
 * @author chenyu
 * @date 2020/11/30 15:12
 */
@Data
public class CapitalDistributionDTO implements Serializable {

    private static final long serialVersionUID = 4906795332784391867L;

    @ApiModelProperty(value = "分类 0:今日;1:5日;2:10日;3:20日;4:60日")
    private Integer type;
    @ApiModelProperty(value = "股票代码")
    private String code;

    @ApiModelProperty(value = "资金流入总额")
    private BigDecimal capitalInTotal;
    @ApiModelProperty(value = "资金流入小单")
    private BigDecimal capitalInSmall;
    @ApiModelProperty(value = "资金流入中单")
    private BigDecimal capitalInMid;
    @ApiModelProperty(value = "资金流入大单")
    private BigDecimal capitalInLarge;
    @ApiModelProperty(value = "资金流入特大单")
    private BigDecimal capitalInXLarge;

    @ApiModelProperty(value = "资金流出总额")
    private BigDecimal capitalOutTotal;
    @ApiModelProperty(value = "资金流出小单")
    private BigDecimal capitalOutSmall;
    @ApiModelProperty(value = "资金流出中单")
    private BigDecimal capitalOutMid;
    @ApiModelProperty(value = "资金流出大单")
    private BigDecimal capitalOutLarge;
    @ApiModelProperty(value = "资金流出特大单")
    private BigDecimal capitalOutXLarge;

    public CapitalDistributionDTO() {
        this.capitalInTotal = BigDecimal.ZERO;
        this.capitalInSmall = BigDecimal.ZERO;
        this.capitalInMid = BigDecimal.ZERO;
        this.capitalInLarge = BigDecimal.ZERO;
        this.capitalInXLarge = BigDecimal.ZERO;
        this.capitalOutTotal = BigDecimal.ZERO;
        this.capitalOutSmall = BigDecimal.ZERO;
        this.capitalOutMid = BigDecimal.ZERO;
        this.capitalOutLarge = BigDecimal.ZERO;
        this.capitalOutXLarge = BigDecimal.ZERO;
    }

    public void build() {
        this.capitalInTotal = BigDecimal.ZERO;
        this.capitalInSmall = BigDecimal.ZERO;
        this.capitalInMid = BigDecimal.ZERO;
        this.capitalInLarge = BigDecimal.ZERO;
        this.capitalInXLarge = BigDecimal.ZERO;
        this.capitalOutTotal = BigDecimal.ZERO;
        this.capitalOutSmall = BigDecimal.ZERO;
        this.capitalOutMid = BigDecimal.ZERO;
        this.capitalOutLarge = BigDecimal.ZERO;
        this.capitalOutXLarge = BigDecimal.ZERO;
    }

    public static CapitalDistributionDTO add(CapitalDistributionDTO a, CapitalDistributionDTO b) {
        CapitalDistributionDTO distributionDTO = new CapitalDistributionDTO();
        distributionDTO.setCapitalInMid(a.getCapitalInMid().add(b.getCapitalInMid()));
        distributionDTO.setCapitalInLarge(a.getCapitalInLarge().add(b.getCapitalInLarge()));
        distributionDTO.setCapitalInSmall(a.getCapitalInSmall().add(b.getCapitalInSmall()));
        distributionDTO.setCapitalInTotal(a.getCapitalInTotal().add(b.getCapitalInTotal()));
        distributionDTO.setCapitalInXLarge(a.getCapitalInXLarge().add(b.getCapitalInXLarge()));

        distributionDTO.setCapitalOutMid(a.getCapitalOutMid().add(b.getCapitalOutMid()));
        distributionDTO.setCapitalOutLarge(a.getCapitalOutLarge().add(b.getCapitalOutLarge()));
        distributionDTO.setCapitalOutSmall(a.getCapitalOutSmall().add(b.getCapitalOutSmall()));
        distributionDTO.setCapitalOutTotal(a.getCapitalOutTotal().add(b.getCapitalOutTotal()));
        distributionDTO.setCapitalOutXLarge(a.getCapitalOutXLarge().add(b.getCapitalOutXLarge()));
        return distributionDTO;
    }
}
