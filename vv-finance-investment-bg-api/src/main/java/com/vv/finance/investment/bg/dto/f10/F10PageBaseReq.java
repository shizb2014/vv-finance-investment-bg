package com.vv.finance.investment.bg.dto.f10;

import com.vv.finance.common.bean.SimplePageReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * @author hamilton
 * @date 2021/8/19 14:26
 */
@Data
public class F10PageBaseReq extends SimplePageReq {
    private static final long serialVersionUID = 431089726918481132L;
    @NotBlank(message = "股票代码不能为空" )
    @ApiModelProperty(value = "股票代码")
    private String stockCode;
}
