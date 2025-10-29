package com.vv.finance.investment.bg.stock.information;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 资讯类型
 *
 * @author lh.sz
 */
@Getter
public enum NewsTypeEnum {

    /**
     * 新股
     */
    NEW_SHARE(1, "新股"),

    HK_STOCK(2, "港股新闻"),

    USA_STOCK(3, "美股新闻");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;

    NewsTypeEnum(
            int code,
            String desc
    ) {
        this.code = code;
        this.desc = desc;
    }

    public static NewsTypeEnum getByCode(int code) {
        for (NewsTypeEnum value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }
}
