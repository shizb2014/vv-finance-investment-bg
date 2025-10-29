package com.vv.finance.investment.bg.dto.broker;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class NetBuyAndSellResp implements Serializable {

    private static final long serialVersionUID = 2168577355760317257L;

    @ApiModelProperty(value = "净买入前十")
    private List<Xnhk0609Resp> xnhk0609RespListBuy;


    @ApiModelProperty(value = "净卖出前十")
    private List<Xnhk0609Resp> xnhk0609RespListSale;

    @ApiModelProperty(value = "数据更新时间")
    private Long date;
}
