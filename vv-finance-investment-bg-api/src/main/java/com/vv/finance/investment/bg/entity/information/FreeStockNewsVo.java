package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author: wsliang
 * @Date: 2021/9/13 13:58
 **/
@Data
@ToString
@ApiModel("自选股资讯列表简讯")
public class FreeStockNewsVo implements Serializable {

    private static final long serialVersionUID = -4702986132129228204L;

    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("资讯id")
    private Long newsid;

    @ApiModelProperty("资讯标题")
    private String title;

    @ApiModelProperty("图片")
    private String image;

    @ApiModelProperty("标签 新闻类别")
    private String newsType;

    @ApiModelProperty("标签 新闻类别名称")
    private String newsTypeName;

    @ApiModelProperty("新闻市场")
    private String market;

    @ApiModelProperty("相关股票代码")
    private String stockCode;

    @ApiModelProperty("来源")
    private String source;

    @ApiModelProperty("日期+时间")
    private Long dateTime;

    @ApiModelProperty("xdbmask")
    private String xdbmask;

    @ApiModelProperty("股票信息")
    private SimpleStockVo simpleStockVo;

    @ApiModelProperty("剩余相关股票/行业个数")
    private Integer remainRelationStockNumber;
}
