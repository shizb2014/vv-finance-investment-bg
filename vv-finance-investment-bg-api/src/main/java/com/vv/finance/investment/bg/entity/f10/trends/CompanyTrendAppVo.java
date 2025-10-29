package com.vv.finance.investment.bg.entity.f10.trends;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 各类别公司动向
 *
 * @author wsliang
 * @date 2021/9/26 16:26
 **/
@Data
@ToString
public class CompanyTrendAppVo implements Serializable {

    private static final long serialVersionUID = 1622555496600174205L;

    @ApiModelProperty("动向类型")
    private String type;

    @ApiModelProperty("类型code 1, 交易警报; 2, 并行交易; 3, 停/复牌; 4, 股东大会;5, 公司重组;6, 收购及合并;7, 拆股合并;8, 分红派息;9, 股票回购")
    private Integer typeCode;

    @ApiModelProperty("时间1 前端展示用 若时间不为空则展示")
    private String colDate1;

    @ApiModelProperty("时间2 前端展示用 若不为空则和date1按需求拼接展示")
    private String colDate2;

    @ApiModelProperty("文本1 若不为空则展示")
    private String content1;

    @ApiModelProperty("文本2 为空可忽略,根据页面需求组装拼接")
    private String content2;
}
