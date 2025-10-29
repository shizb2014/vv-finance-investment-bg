package com.vv.finance.investment.bg.entity.f10.trends;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 15:37
 * @Version 1.0
 * 股东大会
 */
@Data
@Builder
@ToString
public class GeneralMeeting implements Serializable {

    private static final long serialVersionUID = 1622555496600174204L;

    @ApiModelProperty(value = "公布日期")
    private Long releaseDate;
    @ApiModelProperty(value = "事件编号")
    private String eventNo;
    @ApiModelProperty(value = "会议日期")
    private Long meetingDate;
    @ApiModelProperty(value = "会议类型")
    private String meetingType;
    @ApiModelProperty(value = "事项明细")
    private String eventDetail;

    @ApiModelProperty(value = "代码")
    private String stockCode;
}
