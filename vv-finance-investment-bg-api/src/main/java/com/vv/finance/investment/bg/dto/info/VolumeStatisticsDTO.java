package com.vv.finance.investment.bg.dto.info;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * @author chenyu
 * @date 2021/4/8 19:07
 */
@Data
@ApiModel(value = "成交统计", description = "成交统计")
public class VolumeStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 2628156861707427427L;
    @ApiModelProperty(value = "时间戳")
    private Long time;
    @ApiModelProperty(value = "平均价")
    private BigDecimal avgPrice;
    @ApiModelProperty(value = "昨收价")
    private BigDecimal preClose;
    @ApiModelProperty(value = "总笔数")
    private Integer totalStrokes;
    @ApiModelProperty(value = "总成交量")
    private BigDecimal totalVolume;
    @ApiModelProperty(value = "主动买入")
    private BigDecimal totalInVolume;
    @ApiModelProperty(value = "主动卖出")
    private BigDecimal totalOutVolume;
    @ApiModelProperty(value = "中性")
    private BigDecimal totalNeuterVolume;
    @ApiModelProperty(value = "成交分布")
    private List<DealStatisticsDTO> dealList;

    public void init(){
        this.avgPrice = BigDecimal.ZERO;
        this.preClose = BigDecimal.ZERO;
        this.totalVolume = BigDecimal.ZERO;
        this.totalInVolume = BigDecimal.ZERO;
        this.totalOutVolume = BigDecimal.ZERO;
        this.totalNeuterVolume = BigDecimal.ZERO;
        this.totalStrokes = 0;
        this.dealList = new LinkedList<>();
        this.time = System.currentTimeMillis();

    }




}
