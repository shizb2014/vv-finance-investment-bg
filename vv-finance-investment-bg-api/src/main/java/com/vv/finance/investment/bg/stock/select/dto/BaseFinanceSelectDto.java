package com.vv.finance.investment.bg.stock.select.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author wsliang
 * @date 2022/3/3 16:50
 **/
@Data
public class BaseFinanceSelectDto implements Serializable {
    private static final long serialVersionUID = 4251619710587505106L;

    private String stockCode;
    @ApiModelProperty(value = "报告类型 I\n" +
            "F\n" +
            "Q1\n" +
            "Q3\n" +
            "P\n" +
            "Q4\n" +
            "Q5")
    private String reportType;

    private Long endTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseFinanceSelectDto that = (BaseFinanceSelectDto) o;
        return Objects.equals(stockCode, that.stockCode) &&
                Objects.equals(reportType, that.reportType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockCode, reportType);
    }
}
