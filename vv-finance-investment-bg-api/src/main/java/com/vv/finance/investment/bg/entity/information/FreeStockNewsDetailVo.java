package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @Author: wsliang
 * @Date: 2021/9/13 14:05
 **/
@Data
@ToString
@ApiModel("自选资讯详情")
public class FreeStockNewsDetailVo extends FreeStockNewsVo {

    private static final long serialVersionUID = 2445026613121578097L;
    @ApiModelProperty("资讯详情文本")
    private String content;

    @ApiModelProperty("图片")
    private String image;
}
