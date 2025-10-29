package com.vv.finance.investment.bg.dto.broker;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Xnhk0609Resp implements Serializable {

    private static final long serialVersionUID = 2168577355760317257L;

    @ApiModelProperty(value = "经纪商编号")
    private String brokerId;

    @ApiModelProperty(value = "经纪商名称")
    private String brokerName;

    @ApiModelProperty(value = "参与者持股数量")
    private BigDecimal f003n;

    @ApiModelProperty(value = "参与者持股数量百分比")
    private BigDecimal f004n;
}
