package com.vv.finance.investment.bg.dto;

import com.vv.finance.common.entity.receiver.Order;
import com.vv.finance.common.entity.common.StockSnapshot;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * @author chenyu
 * @date 2020/11/12 14:09
 */
@Data
public class RtInfoDTO implements Serializable {

    @ApiModelProperty(value = "买卖10档")
    private Order order;

    @ApiModelProperty("股票最新快照")
    private StockSnapshot snapshot;

    /**
     * 停牌标识
     * 200001 交易中
     * 200002 暂停交易
     * 200003 复牌
     * 200004 停牌
     * 200005 退市
     */
    @ApiModelProperty(value = "停牌标识 200001 交易中 200002 暂停交易 200003 复牌 200004 停牌 200005 退市")
    private Integer suspension =200002;
}
