package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateResp implements Serializable {
    private static final long serialVersionUID = -519933417347375681L;

    @ApiModelProperty(value="最新数据更新时间")
    private Long date;
}
