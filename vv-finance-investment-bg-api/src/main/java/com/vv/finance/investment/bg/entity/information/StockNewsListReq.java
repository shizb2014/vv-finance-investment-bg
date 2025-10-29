package com.vv.finance.investment.bg.entity.information;

import com.vv.finance.common.bean.SimplePageReq;
import com.vv.finance.investment.bg.constants.PublishStatusEnum;
import com.vv.finance.investment.bg.constants.PublishTerminalEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author hamilton
 * @date 2021/9/17 14:34
 */
@Data
public class StockNewsListReq  extends SimplePageReq {
    private static final long serialVersionUID = 7147171045419515226L;
    @ApiModelProperty(value = "一级分类")
    private String topCategory;
    @ApiModelProperty(value = "二级分类")
    private String secondCategory;
    @ApiModelProperty(value = "资讯来源")
    private String source;
    @ApiModelProperty(value = "关键词")
    private String keyword;

    @ApiModelProperty(value = "标题")
    private String newsTitle;

    @ApiModelProperty(value = "关联股票代码")
    private String relationStock;

    @ApiModelProperty(value = "发布状态 0--已发布 1--未发布")
    private Integer publishStatus;



    @ApiModelProperty(value = "发布终端 0--pc 1--app  2-- pc、app")
    private Integer publishTerminal;

    @ApiModelProperty(value = "资讯开始日期")
    private Long startDate;
    @ApiModelProperty(value = "资讯结束日期")
    private Long endDate;

}
