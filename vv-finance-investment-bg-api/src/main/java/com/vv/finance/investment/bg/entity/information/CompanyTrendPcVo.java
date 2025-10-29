package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Author: wsliang
 * @Date: 2021/9/14 10:39
 **/
@Data
@ToString
@ApiModel("日历资讯-公司动向")
public class CompanyTrendPcVo implements Serializable {
    private static final long serialVersionUID = 8140568842346780088L;

    @ApiModelProperty(value = "自增id")
    private Long id;
    @ApiModelProperty(value = "股票代码")
    private String SECCODE;

    @ApiModelProperty(value = "股票代码")
    private String stockCode;

    @ApiModelProperty("股票名称")
    private String stockName;

    @ApiModelProperty("文本内容1：不展示（交易警报）；文本展示（并行交易）；不展示（停/复牌）；文本展示（公司重组）；文本展示（收购及合并）")
    private String content1;

    @ApiModelProperty("文本内容2：文本展示（交易警报）；用”与“字拼接在date2后面并追加”-并行交易“（并行交易）；文本展示（并行交易）；不展示（停/复牌）；不展示（公司重组）；不展示（收购及合并）")
    private String content2;

    @ApiModelProperty("发布日期")
    private Long releaseDate;

    @ApiModelProperty("date1: 不展示（交易警报）；用”于“字拼接在content1后面（并行交易）；停牌日期（停/复牌）；不展示（公司重组）；不展示（收购及合并）")
    private Long date1;

    @ApiModelProperty("date2：不展示（交易警报）；用”-“拼接在date1后面（并行交易）；复牌日期（停/复牌）；不展示（公司重组）；不展示（收购及合并）")
    private Long date2;

    @ApiModelProperty("动向类型")
    private String trendsType;

    @ApiModelProperty("动向类型 1：交易警报，2：并行交易 ，3：停/复牌，4：股东大会，5：公司重组，6：收购及合并")
    private Integer trendsCode;


    @ApiModelProperty("排序日期。有些数据是用date1参与排序，有些是用releaseDate排序，所以增加一个orderDate用于排序")
    private Long orderDate;


}
