package com.vv.finance.investment.bg.entity.information;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.finance.investment.bg.constants.PublishStatusEnum;
import com.vv.finance.investment.bg.constants.PublishTerminalEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author hamilton
 * @date 2021/9/14 17:43
 * @desc 新闻资讯
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="NewsUs对象", description="")
@TableName("t_stock_news")
public class StockNewsEntity implements Serializable {

    private static final long serialVersionUID = 3973747490318172432L;
    @ApiModelProperty(value = "自增id")
    @TableId(type=IdType.AUTO)
    private Long id;
    @ApiModelProperty(value = "新闻ID")
    private Long newsId;
    @ApiModelProperty(value = "一级分类 表")
    private String topCategory;
    @ApiModelProperty(value = "一级分类 中文名字 前端显示用")
    @TableField(exist = false)
    private String topCategoryName;
    @ApiModelProperty(value = "二级分类 新闻类别1 各表的类型1 level1")
    private String secondCategory;

    @ApiModelProperty(value = "三级分类 新闻类别2 各表的类型2 level2")
    private String tertiaryCategory;

    @ApiModelProperty(value = "资讯来源")
    private String source;
    @ApiModelProperty(value = "关键词")
    private String keyword;

    @ApiModelProperty(value = "标题")
    private String newsTitle;

    @ApiModelProperty(value = "关联股票代码")
    private String relationStock;

    @ApiModelProperty(value = "关联股票代码")
    private String relationIndustry;

    @ApiModelProperty(value = "是否过期 0-有效 1-过期")
    private Integer overTimeFlag;

    @ApiModelProperty(value = " 发布终端 0--pc 1--app  2-- pc、app")
    private PublishTerminalEnum publishTerminal;

    @ApiModelProperty(value = "pc发布状态 0--已发布 1--撤销发布")
    private PublishStatusEnum publishStatus;

    @ApiModelProperty(value = "资讯日期")
    private LocalDate date;

    @ApiModelProperty(value = "资讯时间")
    private String time;

    @ApiModelProperty(value = "阅读量")
    private Long   readingVolume;


    @ApiModelProperty(value = "新闻内容")
    private String content;
    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "新闻市场")
    private String market;

    @ApiModelProperty(value = "涨跌额")
    @TableField(exist = false)
    private BigDecimal chg;

    @ApiModelProperty(value = "涨跌幅")
    @TableField(exist = false)
    private BigDecimal chgPct;

    @ApiModelProperty(value = "图片地址")
    private String imageUrl;

    @ApiModelProperty(value = "同步时间戳")
    private Long xdbmask;
    @ApiModelProperty(value = "新闻时间")
    private LocalDateTime dateTime;

    @ApiModelProperty(value = "备注")
    private String remark;
}
