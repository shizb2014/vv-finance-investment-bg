package com.vv.finance.investment.bg.constants;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @ClassName: StockAnalyzeEnum
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/11/18   11:19
 */
@Getter
public enum StockAnalyzeSignalEnum {

    NEUTRAL(0, "中性", 3),
    //表示消极
    OVER_BOUGHT(3, "超买", 5),

    //表示积极
    OVER_SELL(4, "超卖", 1);

    @JsonValue
    @EnumValue
    int code;
    String message;
    int sort;

    StockAnalyzeSignalEnum(int code, String message, int sort) {
        this.code = code;
        this.message = message;
        this.sort = sort;
    }
}
