package com.vv.finance.investment.bg.dto.southward.req;

import com.vv.finance.investment.bg.enums.SouthwardCapitalDateTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author qinxi
 * @date 2023/6/25 11:38
 * @description: 南向资金排行榜请求
 */
@Data
public class SouthwardCapitalStockRankReq implements Serializable {


    private static final long serialVersionUID = 8555953987869968015L;

    /**
     * @see SouthwardCapitalDateTypeEnum
     */
    @ApiModelProperty(value = "日期类型 0：当日 1：近5日 2：近20日：3：近60日 null：全部")
    private Integer dateType;

    @ApiModelProperty(value = "前几名")
    private Integer topNum;



}
