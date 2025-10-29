package com.vv.finance.investment.bg.entity.f10.trends;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wsliang
 * @date 2021/9/26 16:48
 **/
@Data
public class  DividendDetail implements Serializable {
    private static final long serialVersionUID = 4232914968921714920L;
    @ApiModelProperty(value = "分配类型")
    private String assignmentType;
    @ApiModelProperty(value = "分红方案")
    private String dividendScheme;
    @ApiModelProperty(value = "公布日期")
    private String publicationDate;
    @ApiModelProperty(value = "除净日")
    private String exDate;
    @ApiModelProperty(value = "股权登记日")
    private String stockRightDate;
    @ApiModelProperty(value = "截止过户日")
    private String lastTransferDate;
    @ApiModelProperty(value = "派息日")
    private String dividendDay;
}
