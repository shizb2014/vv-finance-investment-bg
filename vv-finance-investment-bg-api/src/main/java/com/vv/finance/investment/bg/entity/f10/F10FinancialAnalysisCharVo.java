package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wsliang
 * @date 2021/10/12 14:20
 **/
@Data
public class F10FinancialAnalysisCharVo implements Serializable {
    private static final long serialVersionUID = 3397760582604276734L;

    @ApiModelProperty("类型: 0 非金融/1 金融/2 保险")
    private Integer marketType;

    @ApiModelProperty("图表数据")
    private List<F10FinancialAnalysisChar> financialAnalysisChars;
}
