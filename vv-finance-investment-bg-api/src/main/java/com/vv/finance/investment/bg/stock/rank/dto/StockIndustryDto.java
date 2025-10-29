package com.vv.finance.investment.bg.stock.rank.dto;

import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author hamilton
 * @date 2020/11/6 16:25
 */
@Data
public class StockIndustryDto  implements Serializable {
    private static final long serialVersionUID = 8211418391626356216L;
    private String stockCode;
    private IndustrySubsidiary industrySubsidiary;



}
