package com.vv.finance.investment.bg.entity.f10.trends;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 15:32
 * @Version 1.0
 * 公司重组
 */
@Data
@Builder
@ToString
public class CompanyRecombination implements Serializable {

    private static final long serialVersionUID = 2540888185071529799L;

    @ApiModelProperty(value = "公布日期")
    private Long releaseDate;
    @ApiModelProperty(value = "公司重组建议日期")
    private Long adviseDate;
    @ApiModelProperty(value = "公司重组完成日期")
    private Long successDate;
    @ApiModelProperty(value = "事项明细")
    private String eventDetail;

    @ApiModelProperty(value = "代码")
    private String stockCode;
}
