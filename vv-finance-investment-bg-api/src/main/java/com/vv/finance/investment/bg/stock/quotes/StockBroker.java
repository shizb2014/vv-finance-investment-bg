package com.vv.finance.investment.bg.stock.quotes;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @ClassName StockBrokerComparison
 * @Deacription 经纪席位代码名称对照表
 * @Author lh.sz
 * @Date 2020年11月13日 15:54
 **/
@Data
@TableName("t_stock_broker")
public class StockBroker implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "经纪席位代码")
    private String brokerCode;
    @ApiModelProperty(value = "简体中文")
    private String simplifiedName;
    @ApiModelProperty(value = "繁体中文")
    private String complexName;
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

}
