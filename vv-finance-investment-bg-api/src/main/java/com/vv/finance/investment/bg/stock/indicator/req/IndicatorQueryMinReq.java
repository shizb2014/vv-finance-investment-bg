package com.vv.finance.investment.bg.stock.indicator.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author hqj
 * @date 2020/10/30 11:57
 */
@Data
public class IndicatorQueryMinReq implements Serializable {

    private static final long serialVersionUID = 8529591741007753141L;
    /**
     * 股票代码
     */
    private String code;
    /**
     * 查询数量
     */
    private Integer number;
    /**
     * 什么时间往后 yyyy-MM-dd HH:mm:ss
     */
    private Date timestamp;
    /*
     *类型:分钟数，支持1，5，15，30，60，120
     */
    private  String  type;

    @ApiModelProperty(value = "forward：前复权；backward：后复权，为空 不复权")
    private String adjhkt;

}
