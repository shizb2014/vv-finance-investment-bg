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
public enum StockAnalyzeIndicatorEnum {

    /**
     * 指标解读枚举集合
     */
    RSI12(0, "RSI(12)","＞85","＜15","","","15≤RSI(12)≤85"),
    RSI6(8, "RSI(6)","＞85","＜15","","","15≤RSI(6)≤85"),
    KDJ_SELL_BUY(9, "KDJ","D线＞70","D线＜30","","","30≤D线≤70"),
    KDJ(2, "KDJ","","","K线上穿D线","K线下穿D线","K线与D线未交叉"),
    WMSR(1, "WMSR","<15",">85","","","15≤WMSR≤85"),

    CCI(9, "CCI", "从上向下突破+100，从下向上突破+100", "从上向下突破-100，从下向上突破-100", "", "", "没有突破-100或+100"),
    AR(10, "AR", "＞120", "＜80", "", "", "80≤AR≤120"),
    BR(11, "BR", "＞125", "＜75", "", "", "75≤BR≤125"),
    VR(12, "VR", "＞150", "＜80", "", "", "80≤VR≤150"),
    BIAS(13, "BIAS", "BIAS(6)>5或BIAS(12)>7或BIAS(24)>11", "BIAS(6)<-5或BIAS(12)<-7或BIAS(24)<-11", "", "", "-5≤BIAS(6)≤5且-7≤BIAS(12)≤7且-11≤BIAS(24)≤11"),
    PSY(14, "PSY", "＞75", "＜25", "", "", "25≤PSY≤75"),

    MACD(3, "MACD","","","DIF线上穿DEA线","DIF线下穿DEA线","DIF线与DEA线未交叉"),
    BOLL(4, "BOLL","","","股价下穿下轨线","股价上穿上轨线","股价与上下轨线未交叉"),
    SAR(6, "SAR","","","股价上穿SAR线","股价下穿SAR线","股价与SAR线未交叉"),
    DMI(5, "DMI","","","PDI线上穿MDI线","PDI线下穿MDI线","PDI线与MDI线未交叉"),
    OBV(7, "OBV","","","OBV负值转为正值","OBV正值转为负值","OBV保持正值或负值不变");

    @JsonValue
    @EnumValue
    int code;
    String message;
    //超买
    String overBuy;
    //超卖
    String overSell;
    //积极
    String positive;
    //消极
    String negative;
    //中立
    String neutral;

    StockAnalyzeIndicatorEnum(int code, String message,String overBuy,String overSell,String positive,String negative,String neutral) {
        this.code = code;
        this.message = message;
        this.overBuy = overBuy;
        this.overSell = overSell;
        this.positive = positive;
        this.negative = negative;
        this.neutral = neutral;
    }
}
