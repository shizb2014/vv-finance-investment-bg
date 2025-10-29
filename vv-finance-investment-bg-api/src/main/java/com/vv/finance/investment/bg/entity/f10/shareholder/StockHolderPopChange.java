package com.vv.finance.investment.bg.entity.f10.shareholder;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * 股东
 * @author yangpeng
 * @date 2023/10/17
 */
@Data
@Builder
@ToString
public class StockHolderPopChange implements Serializable {

    private static final long serialVersionUID = -7412995455335303366L;

    /**
     * 日期
     */
    @ApiModelProperty(value = "日期")
    private Long date;

    /**
     * 股东列表
     */
    @ApiModelProperty(value = "股东列表")
    private Map<String, List<StockHolder>> records;
}
