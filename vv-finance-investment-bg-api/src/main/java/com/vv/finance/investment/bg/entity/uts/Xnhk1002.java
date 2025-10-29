package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="Xnhk1002对象", description="")
public class Xnhk1002 implements Serializable {

    private static final long serialVersionUID = -2117310389896220720L;

    @ApiModelProperty(value = "指数code")
    @TableId("SECCODE")
    private String seccode;

    @ApiModelProperty(value = "股票code")
    @TableField("F001V")
    private String f001v;
}
