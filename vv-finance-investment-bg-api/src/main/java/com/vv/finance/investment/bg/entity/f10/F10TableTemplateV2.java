package com.vv.finance.investment.bg.entity.f10;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName F10TableTemplate
 * @Deacription 表格模板
 * @Author lh.sz
 * @Date 2021年07月15日 15:36
 **/
@Data
@ToString
@TableName(value = "t_f10_table_template_v2")
public class F10TableTemplateV2 implements Serializable {
    private static final long serialVersionUID = -1682670390094234663L;

    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    @ApiModelProperty(value = "表格类型")
    private String sheetType;

    @ApiModelProperty(value = "表格名称")
    private String sheetName;

    @ApiModelProperty(value = "排序id")
    private String sortId;

    @ApiModelProperty(value = "字段名称")
    private String fieldName;

    @ApiModelProperty(value = "数据 包含同步数据JSON")
    private String fieldValue;


    @ApiModelProperty(value = "是否加粗 0:加粗 1:不加粗")
    private int isOverstriking;

    @ApiModelProperty(value = "是否有同比 0:有 1:没有")
    private int isYoy;

    @ApiModelProperty(value = "是否为null 0:null 1:notNull")
    private int isNull;

    @ApiModelProperty(value = "映射字段")
    private String mappedFields;

    @ApiModelProperty(value = "格式化数据 0:金额 1:百分比 2:正常显示")
    private String valueFormat;

    @ApiModelProperty(value = "映射字段")
    private Integer layer;

    @ApiModelProperty(value = "颜色 0:黑色 1:灰色")
    private Integer color;
}
