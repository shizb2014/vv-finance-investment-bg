package com.vv.finance.investment.bg.dto.stock;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author chenyu
 * @date 2021/3/5 13:54
 */
@Data
@Builder
public class StockRankDTO implements Serializable {
    private static final long serialVersionUID = 8625088944089207839L;
    private Long time;
    private List<StockBaseDTO> stock;
}
