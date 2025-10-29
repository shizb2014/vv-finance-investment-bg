package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 * @author chenzhenlong
 * @since 2021-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Xnhks0317对象", description = "")
public class Xnhks0317 implements Serializable {

    @Column(name = "SECCODE")
    private static final long serialVersionUID = 4861443476423317044L;

    @TableId("SECCODE")
    @Column(name = "SECCODE")
    private String seccode;

    @TableField("F001D")
    @Column(name = "F001D")
    private Long f001d;

    @TableField("F002D")
    @Column(name = "F002D")
    private Long f002d;

    @TableField("F003V")
    @Column(name = "F003V")
    private String f003v;

    @TableField("Create_Date")
//    @Column(name = "Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
//    @Column(name = "Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    @Column(name = "XDBMASK")
    private Long xdbmask;


}
