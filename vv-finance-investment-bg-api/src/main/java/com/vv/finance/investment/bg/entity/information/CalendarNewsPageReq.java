package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: wsliang
 * @Date: 2021/9/13 17:58
 **/
@Data
@ApiModel("日历资讯请求参数实体类")
public class CalendarNewsPageReq extends NewsPageReq {
    private static final long serialVersionUID = 3230938407930967467L;

    @ApiModelProperty("按资讯类型")
    private Integer newType;

    @ApiModelProperty("按重要等级。0：全部，1：一星，2：二星，3：三星，默认全部, 多选用英文逗号隔开")
    private List<Integer> level ;

    @ApiModelProperty("按地区. CN：中国，NA：北美，EU：欧洲，AP：亚太，OTHER：其他.默认全部, 多选用英文逗号隔开")
    private List<String> area ;

    @ApiModelProperty("按股票类型 0-自定义分组，1-全部自选，3-持仓股票，4-清仓股票，5-关注股票，6-条件分组, 100-全部, 多选用英文逗号隔开")
    private List<Integer> groupTypes;

    @ApiModelProperty("按动向类型。0：全部，1：交易警报，2：并行交易 ，3：停/复牌，4：股东大会，5：公司重组，6：收购及合并,10: 除权, 11: 财报, 多选用英文逗号隔开")
    private List<Integer> trendsType;

    @ApiModelProperty("起始时间 毫秒时间戳")
    private Long startTime;

    @ApiModelProperty("结束时间 毫秒时间戳")
    private Long endTime;

    @ApiModelProperty("新闻时间")
    private Long time;

}
