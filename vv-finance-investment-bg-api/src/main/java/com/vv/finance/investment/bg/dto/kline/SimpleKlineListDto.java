package com.vv.finance.investment.bg.dto.kline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author hamilton
 * @date 2021/11/12 13:42
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleKlineListDto implements Serializable {
    private static final long serialVersionUID =1;
    /**
     * 不复权
     */
    private List<SimpleKlineDto> klineList;

    /**
     * 前复权
     */
    private List<SimpleKlineDto> forwardKlineList;

    /**
     * 后复权
     */
    private List<SimpleKlineDto> backwardKlineList;
}
