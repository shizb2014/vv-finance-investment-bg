package com.vv.finance.investment.bg.stock.info;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName StockRelatedDetails
 * @Deacription 股票相关详情
 * @Author lh.sz
 * @Date 2021年12月27日 11:29
 **/
@Data
@TableName("t_stock_related_details")
public class StockRelatedDetails implements Serializable {
    private static final long serialVersionUID = -1;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 股票代码/权证代码
     */
    private String code;
    /**
     * 港股或者权证
     */
    private Integer type;
    /**
     * 快照详情
     */
    private String snapshotDetails;
    /**
     * 委托挂单详情
     */
    private String orderDetails;
    /**
     * 买盘经纪详情
     */
    private String orderBrokerBuyDetails;
    /**
     * 卖盘经纪详情
     */
    private String orderBrokerSellDetails;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;

}
