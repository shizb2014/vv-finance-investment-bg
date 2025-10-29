package com.vv.finance.investment.bg.constants;

/**
 * @author wsliang
 */
public enum ShareholderTypeEnum {
    personal("1"), mechanism("2"),director("3A");
    private String value;

    ShareholderTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
