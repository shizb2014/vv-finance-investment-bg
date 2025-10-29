package com.vv.finance.investment.bg.constants;

/**
 * @author chenyu
 * @date 2021/3/3 14:30
 */
public enum DBTypeEnum {
    db1("db1"), db2("db2");
    private String value;

    DBTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
