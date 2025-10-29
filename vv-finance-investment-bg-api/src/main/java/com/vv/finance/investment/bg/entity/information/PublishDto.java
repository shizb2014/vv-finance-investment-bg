package com.vv.finance.investment.bg.entity.information;

import com.vv.finance.investment.bg.constants.PublishStatusEnum;
import com.vv.finance.investment.bg.constants.PublishTerminalEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/9/17 14:30
 */
@Data
public class PublishDto implements Serializable {

    private static final long serialVersionUID = -1548013394299347502L;
    @ApiModelProperty(value = " 发布终端 0--pc 1--app  2-- pc、app")
    private PublishTerminalEnum publishTerminal;
    @ApiModelProperty(value = "pc发布状态 0--已发布 1--撤销发布")
    private PublishStatusEnum publishStatus;

    @ApiModelProperty(value = "记录id")
    private Long id;

    @ApiModelProperty(value = "备注")
    private String remark;

    private Long userId;
    private Long deptId;
    private String userName;
    private String nickName;
}
