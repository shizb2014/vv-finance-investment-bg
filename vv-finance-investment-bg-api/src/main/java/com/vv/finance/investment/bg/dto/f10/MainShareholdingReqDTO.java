package com.vv.finance.investment.bg.dto.f10;

import com.vv.finance.common.bean.SimplePageReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * description: MainShareholdingReqDTO
 * date: 2022/6/21 10:58
 * author: fenghua.cai
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MainShareholdingReqDTO extends SimplePageReq{

    private static final long serialVersionUID = -3749042810502541190L;

    @NotBlank(message = "股票代码不能为空" )
    @ApiModelProperty(value = "股票代码")
    private String stockCode;

    @ApiModelProperty(value = "股东类型")
    private String holderType;

}
