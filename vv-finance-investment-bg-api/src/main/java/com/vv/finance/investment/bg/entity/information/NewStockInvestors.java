package com.vv.finance.investment.bg.entity.information;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author wsliang
 * @date 2021/11/2 16:05
 **/
@Data
@Builder
public class NewStockInvestors implements Serializable {
    private static final long serialVersionUID = -1632267658812005537L;

    /**
     * 基石投资者
     */
    @ApiModelProperty("基石投资者者")
    private List<NewShareInvestorInfo> investorInfos;

    /**
     * 保荐人信息
     */
    @ApiModelProperty("保荐人")
    private List<String> sponsor;

    /**
     * 承销商
     */
    @ApiModelProperty("承销商")
    private List<String> underwriter;
}
