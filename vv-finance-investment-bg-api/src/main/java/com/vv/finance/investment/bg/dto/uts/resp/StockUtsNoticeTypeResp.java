package com.vv.finance.investment.bg.dto.uts.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author chenyu
 * @date 2021/7/12 13:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockUtsNoticeTypeResp implements Serializable {
    private static final long serialVersionUID = 8281929053962800791L;
    @ApiModelProperty("类型")
    private Integer type;

    @ApiModelProperty("标题")
    private String name;

}
