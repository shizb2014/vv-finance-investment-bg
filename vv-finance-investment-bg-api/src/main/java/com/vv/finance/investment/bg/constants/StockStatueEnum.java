package com.vv.finance.investment.bg.constants;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author chenyu
 * @date 2020/11/12 14:27
 */
public enum StockStatueEnum {
    /**
     * 交易中
     */
    GOING(200001, "交易中"),
    /**
     * 暂停交易
     */
    PAUSE(200002, "暂停交易"),
    /**
     * 复牌
     */
    RESUME(200003, "复牌"),
    /**
     * 停牌
     */
    STOP(200004, "停牌"),

    /**
     * 退市
     */
    QUIT(200005, "退市"),
    /**
     * 收盘
     */
    CLOSED(200006, "已收盘"),
    /**
     * 休市
     */
    REST(200007, "休市"),
    /**
     * 开盘前
     */
    PRE_OPEN(200008, "盘前竞价"),
    /**
     * 开盘
     */
    OPEN(200009, "开盘"),
    /**
     * 收市竞价
     */
    CLOSE_BIDDING(200010, "收市竞价"),
    /**
     * 午间休市
     */
    NOON_REST(200011, "午间休市"),
    /**
     * 临时休市
     */
    TEMPORARY_CLOSE(200012, "临时休市"),
    /**
     * 等待开盘
     */
    WAITING_OPEN(200013, "等待开盘");
    @EnumValue
    @JsonValue
    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    StockStatueEnum(
            int code,
            String desc
    ) {
        this.code = code;
        this.desc = desc;
    }

    public static StockStatueEnum getByCode(int code) {
        for (StockStatueEnum value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }
}
