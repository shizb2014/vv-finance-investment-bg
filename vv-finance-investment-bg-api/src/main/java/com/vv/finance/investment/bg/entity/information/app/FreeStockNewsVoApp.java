package com.vv.finance.investment.bg.entity.information.app;

import com.vv.finance.investment.bg.entity.information.SimpleStockVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@ApiModel("自选股资讯列表简讯-app")
public class FreeStockNewsVoApp  implements Serializable {

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
    private SimpleStockVoApp simpleStockVo;

    @ApiModelProperty("剩余相关股票/行业个数")
    private Integer remainRelationStockNumber;


    // pc端要求填充字段(可以为null)
    @ApiModelProperty("是否过期 0-有效 1-过期")
    private Integer overTimeFlag;

    @ApiModelProperty(value = "利好利空程度( -3:大利空 -2:中利空 -1:小利空 0:无 1:小利好, 2:中利好, 3:大利好 )")
    private Integer positiveNegative;

    @ApiModelProperty(value = "处理状态 0初始化 1已处理 ")
    private Integer dealStatus;

    @ApiModelProperty(value = "维度/影响范围(-1: 无关联 0-个票 1-行业 2-大盘)")
    private Integer influenceScope;

    @ApiModelProperty(value = "资讯类型：0为彭博，1为其他")
    private Integer type;
}
