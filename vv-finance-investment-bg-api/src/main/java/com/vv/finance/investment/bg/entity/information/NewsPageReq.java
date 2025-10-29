package com.vv.finance.investment.bg.entity.information;

import com.vv.finance.common.bean.SimplePageReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wsliang
 * @Date: 2021/9/13 15:44
 **/
@Data
public class NewsPageReq extends SimplePageReq {
    private static final long serialVersionUID = -548070182679517996L;

    @ApiModelProperty("按id坐标值 自选,新股,异动,港美股,个股资讯,日历中财经事件")
    private Long id;

    @ApiModelProperty("新闻时间")
    private Long time;
}
