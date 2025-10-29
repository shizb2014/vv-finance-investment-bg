package com.vv.finance.investment.bg.constants;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author chenyu
 * @date 2020/11/12 14:27
 */
public enum StockUtsNoticeEnum {
    /**
     * 全部公告
     */
    ALL(1, "全部公告","all"),
    /**
     * 财务报告
     */
    FINANCIAL_REPORT(2, "财务报告","financial_report"),
    /**
     * 公司变动
     */
    COMPANY_CHANGE(3, "公司变动","company_change"),
    /**
     * 股权股本
     */
    STOCK_EQUITY(4, "股权股本","stock_equity"),

    /**
     * 交易相关
     */
    TRADING(5, "交易相关","trading"),
    /**
     * 上市文件
     */
    IPO_FILE(10, "上市文件","ipo_file"),
    /**
     * 会议表决
     */
    MEETING(6, "会议表决","meeting"),
    /**
     * 交易披露
     */
    DISCLOSURE(7, "交易披露","disclosure"),
    /**
     * 债券及权证
     */
    BONDS_AND_WARRANTS(8, "债券及权证","bonds_and_warrants"),
    /**
     * 其他公告
     */
    OTHER(9, "其他公告","other")
    ;

    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;
    private final String operation;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getOperation() {
        return operation;
    }

    StockUtsNoticeEnum(
            int code,
            String desc,
            String operation
    ) {
        this.code = code;
        this.desc = desc;
        this.operation = operation;
    }

    public static StockUtsNoticeEnum getByCode(int code) {
        for (StockUtsNoticeEnum value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }
}
