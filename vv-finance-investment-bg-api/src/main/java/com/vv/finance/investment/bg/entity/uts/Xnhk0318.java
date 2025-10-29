package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author chenzhenlong
 * @since 2021-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="Xnhks0318对象", description="")
@TableName("XNHK0318")
public class Xnhk0318 implements Serializable {

    @TableId("SECCODE")
    @Column(name = "SECCODE")
    private String seccode;

    @TableField("F001D")
    @Column(name = "F001D")
    private Long f001d;

    @TableField("F002D")
    @Column(name = "F002D")
    private Long f002d;

    @TableField("F003D")
    @Column(name = "F003D")
    private String f003d;

    @TableField("Create_Date")
//    @Column(name = "Create_Date")
    private Date createDate;

    @TableField("Modified_Date")
//    @Column(name = "Modified_Date")
    private Date modifiedDate;

    @TableField("XDBMASK")
    private Long xdbmask;


}
