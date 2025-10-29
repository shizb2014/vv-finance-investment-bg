package com.vv.finance.investment.bg.entity.f10.shareholder;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/17 17:48
 * @Version 1.0
 */
@Data
@Builder
@ToString
public class PopChanges implements Serializable {

    private static final long serialVersionUID = 4439436272386348875L;

    /**
     * 个人
     */
    @ApiModelProperty(value = "个人")
    private List<Val> personalList;
    /**
     * 机构
     */
    @ApiModelProperty(value = "机构")
    private List<Val> mechanismList;
    /**
     * 董事
     */
    @ApiModelProperty(value = "董事")
    private List<Val> directorList;

    @ApiModelProperty("更新时间")
    private Long updateDate;
}
