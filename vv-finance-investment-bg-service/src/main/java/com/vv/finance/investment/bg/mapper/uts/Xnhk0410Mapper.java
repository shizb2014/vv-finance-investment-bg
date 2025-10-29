package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.dto.uts.resp.ValuationGrowth;
import com.vv.finance.investment.bg.entity.uts.Xnhk0410;
import io.swagger.annotations.ApiModelProperty;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author lh.sz
 * @since 2021-11-16
 */
@DS("db2")
public interface Xnhk0410Mapper extends BaseMapper<Xnhk0410> {

//    @ApiModelProperty(value = "每股盈利复合增长率（三年")
//    private BigDecimal earningsPerShareCAGR3;
//    @ApiModelProperty(value = "每股盈利复合增长率（五年)")
//    private BigDecimal earningsPerShareCAGR5;
//    @ApiModelProperty(value = "总资产复合增长率（三年）")
//    private BigDecimal totalAssetsCAGR3;
//    @ApiModelProperty(value = "总资产复合增长率（五年)")
//    private BigDecimal totalAssetsCAGR5;
//    @ApiModelProperty(value = "营业额复合增长率（三年）")
//    private BigDecimal  turnoverCAGR3;
//    @ApiModelProperty(value = "营业额复合增长率（五年）")
//    private BigDecimal  turnoverCAGR5;
//
//    @ApiModelProperty(value = "毛利润复合增长率（三年）")
//    private BigDecimal grossProfitCAGR3;
//    @ApiModelProperty(value = "毛利润复合增长率(五年）")
//    private BigDecimal grossProfitCAGR5;
//    @ApiModelProperty(value = "净利润复合增长率（三年）")
//    private BigDecimal netProfitCAGR3;
//    @ApiModelProperty(value = "净利润复合增长率（五年)")
//    private BigDecimal netProfitCAGR5;
//    @ApiModelProperty(value = "经营溢利复合增长率（三年）")
//    private BigDecimal  operatingProfitCAGR3;
//    @ApiModelProperty(value = "经营溢利复合增长率（五年）")
//    private BigDecimal  operatingProfitCAGR5;

    @Select("select b.f014n ttm ,a.F008N earnings_per_share_CAGR3,a.F009N earnings_Per_Share_CAGR5," +
            "a.F014N total_Assets_CAGR3,a.F015N total_Assets_CAGR5,a.F016N turnover_CAGR3,a.F017N turnover_CAGR5,"  +
            "a.F018N gross_Profit_CAGR3,a.F019N gross_Profit_CAGR5,a.F010N net_Profit_CAGR3,a.F011N net_Profit_CAGR3,"+
            "a.F020N operating_Profit_CAGR3,a.F021N operating_Profit_CAGR5,a.seccode code"+
            " from XNHK0410 a left join XNHK0406 b on a.seccode=b.seccode")
    Page<ValuationGrowth> pageValuationGrowth(Page<Xnhk0410> page);
}
