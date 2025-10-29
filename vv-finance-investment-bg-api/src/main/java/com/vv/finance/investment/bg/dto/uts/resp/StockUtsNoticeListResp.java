package com.vv.finance.investment.bg.dto.uts.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author chenyu
 * @date 2021/7/12 13:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockUtsNoticeListResp implements Serializable {
    private static final long serialVersionUID = 2631403811921490392L;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("时间戳")
    private Long timestamps;

    @ApiModelProperty("附件地址")
    private List<StockUtsNoticeEnclosureResp> enclosure;
}
