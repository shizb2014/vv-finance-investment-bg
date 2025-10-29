package com.vv.finance.investment.bg.dto.uts.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author chenyu
 * @date 2021/7/20 11:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockUtsNoticeEnclosureResp implements Serializable {

    private static final long serialVersionUID = 4055286756405249575L;
    @ApiModelProperty(value = "附件名")
    private String name;

    @ApiModelProperty(value = "附件地址")
    private String path;
}
