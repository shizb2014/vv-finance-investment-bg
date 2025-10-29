package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author hamilton
 * @since 2021-08-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="Xnhk0002对象", description="")
public class Xnhk0002 implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "代码")
    @TableId("CODE")
    private String code;

    @ApiModelProperty(value = "简体中文名称")
    @TableField("F001V")
    private String f001v;

    @ApiModelProperty(value = "英文名称")
    @TableField("F002V")
    private String f002v;

    @ApiModelProperty(value = "繁体中文名称")
    @TableField("F003V")
    private String f003v;

    @TableField("Create_Date")
    private LocalDateTime createDate;

    @TableField("Modified_Date")
    private LocalDateTime modifiedDate;

    @ApiModelProperty(value = "时间戳")
    @TableField("XDBMASK")
    private Long xdbmask;


}
