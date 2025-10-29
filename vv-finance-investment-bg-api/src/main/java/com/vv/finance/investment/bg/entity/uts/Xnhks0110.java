package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author chenzhenlong
 * @since 2021-08-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("XNHKS0110")
@ApiModel(value = "Xnhks0110", description = "")
public class Xnhks0110 implements Serializable {

    private static final long serialVersionUID = 622073134219583219L;

    @TableField("SECCODE")
    private String seccode;

    @TableField("F001V")
    private String f001v;

    @TableField("F002D")
    private Long f002d;

    @TableId("F003D")
    private Long f003d;

    @TableField("F004D")
    private Long f004d;

    @TableField("F005V")
    private String f005v;

    @TableField("F006V")
    private String f006v;

    @TableField("F007v")
    private String f007v;

    @TableField("F008V")
    private String f008v;

    @TableField("F009N")
    private BigDecimal f009n;

    @TableField("F010N")
    private BigDecimal f010n;

    @TableField("F011V")
    private String f011v;

    @TableField("F012V")
    private String f012v;

    @TableField("Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;


}
