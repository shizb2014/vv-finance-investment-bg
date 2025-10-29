package com.vv.finance.investment.bg.entity.information;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wsliang
 * @date 2021/9/22 15:32
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "公司动向数据合并", description = "")
@TableName("t_company_trends_merge")
public class CompanyTrendsMergeEntity implements Serializable {

    private static final long serialVersionUID = 2889976624840234329L;

    @ApiModelProperty(value = "自增id")
    @Id
    private Long id;

    @TableField("SECCODE")
    @ApiModelProperty(value = "股票代码")
    private String SECCODE;

    @ApiModelProperty("文本内容")
    private String content;

    @ApiModelProperty("文本内容2")
    private String content2;

    @ApiModelProperty("type")
    private Integer type;

    @TableField("releaseDate")
    @ApiModelProperty("发布日期")
    private String releaseDate;

    @TableField("order_date")
    @ApiModelProperty("排序日期。有些数据是用date1参与排序，有些是用releaseDate排序，所以增加一个orderDate用于排序")
    private String orderDate;

    @ApiModelProperty("")
    private String date1;

    @ApiModelProperty("")
    private String date2;

    @ApiModelProperty("融聚汇时间戳，已经不使用这个了，改成使用 createDate 和 modifiedDate")
    private Long sxdbmask;

    @ApiModelProperty("唯一键")
    private String uni;

    @ApiModelProperty("sub_type,部分数据是合并到同一种类型，增加子类型用于区分")
    private Integer subType;

    @TableField(value = "Create_Date")
    private Date createDate;

    @TableField(value = "Modified_Date")
    private Date modifiedDate;


}
