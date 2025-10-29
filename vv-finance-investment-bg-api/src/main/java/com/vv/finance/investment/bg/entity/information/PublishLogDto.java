package com.vv.finance.investment.bg.entity.information;

import com.vv.finance.investment.bg.constants.PublishStatusEnum;
import com.vv.finance.investment.bg.constants.PublishTerminalEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/9/17 14:38
 */
@Data
public class PublishLogDto implements Serializable {

    private static final long serialVersionUID = 2306541079517269507L;
    @ApiModelProperty(value = "操作用户id")
    private Long optUserId;
    @ApiModelProperty(value = "操作用户名")
    private String optUserName;
    @ApiModelProperty(value = "操作时间")
    private String dateTime;

    @ApiModelProperty(value = " 发布终端 0--pc 1--app  2-- pc、app")
    private PublishTerminalEnum publishTerminal;
    @ApiModelProperty(value = "pc发布状态 0--已发布 1--撤销发布")
    private PublishStatusEnum publishStatus;


    @ApiModelProperty(value = "备注")
    private String remark;
}
