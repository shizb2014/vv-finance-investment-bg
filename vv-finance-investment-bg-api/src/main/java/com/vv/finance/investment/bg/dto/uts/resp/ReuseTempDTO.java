package com.vv.finance.investment.bg.dto.uts.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/12/27 15:21
 */
@Data
public class ReuseTempDTO implements Serializable {
    private static final long serialVersionUID = -852822160249434825L;
    /**
     * 临时股票id
     */
    private Long stockId;
    /**
     * 临时code
     */
    private String code;
    /**
     * 关联code
     */
    private String relationCode;
    /**
     * 临时交易开始时间
     */
    private Long startTime;
    /**
     * 临时交易结束时间
     */
    private Long endTime;
    /**
     * 临时code股票名称
     */
    private String stockName;
    /**
     * 关联code股票名称
     */
    private String relationStockName;
    /**
     * 暂停交易时间
     */
    private String stopTradeTime;
    /**
     * 恢复交易时间
     */
    private String restoreTradeTime;

}
