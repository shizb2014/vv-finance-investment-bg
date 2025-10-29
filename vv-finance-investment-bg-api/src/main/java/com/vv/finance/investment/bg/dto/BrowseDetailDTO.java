package com.vv.finance.investment.bg.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author chenyu
 * @date 2020/11/11 13:43
 */
@Data
public class BrowseDetailDTO implements Serializable {
    /**
     * 名称
     */
    private String name;

    /**
     * 代码
     */
    private String code;

    /**
     * 最新价格
     */
    private String price;

    /**
     * 涨跌幅
     */
    private String chgPct;

    private Integer type;

    @ApiModelProperty(value = "是否提醒")
    private Boolean notify;
}
