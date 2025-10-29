package com.vv.finance.investment.bg.entity.f10.trends;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 14:15
 * @Version 1.0
 * 收购及合并
 */
@Data
@Builder
@ToString
public class AcquisitionsAndMergers implements Serializable {

    private static final long serialVersionUID = 6340885410094835964L;

    @ApiModelProperty(value = "公布日期")
    private Long releaseDate;
    @ApiModelProperty(value = "寄发文件及要约日期")
    private Long documentsAndOfferDate;
    @ApiModelProperty(value = "要约最终结束日期")
    private Long offerClosingDate;
    @ApiModelProperty(value = "寄发要约汇款金额最后日期")
    private Long sendOfferAmountDate;
    @ApiModelProperty(value = "事项明细")
    private String eventDetail;

    @ApiModelProperty(value = "代码")
    private String stockCode;
}
