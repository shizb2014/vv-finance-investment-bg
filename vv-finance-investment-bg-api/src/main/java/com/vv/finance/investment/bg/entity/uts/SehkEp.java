package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@TableName(value = "SEHK_EP")
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SehkEp", description = "")
public class SehkEp implements Serializable {

    private static final long serialVersionUID = 1632742497577327868L;

    @TableField("Participant_ID")
    private String participantId;
    @TableField("Broker_No")
    private String broekrNo;
    @TableField("Participant_Name")
    private String participantName;
    @TableField("Chinese_Name")
    private String chineseName;
    @TableField("Short_Name")
    private String shortName;
    @TableField("Trading_Status")
    private String tradingStatus;
    @TableField("XDBMASK")
    private Long xdbmask;
}
