package com.vv.finance.investment.bg.stock.rank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.dto.StockIndustry;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;

import java.util.List;

/**
 * <p>
 * 行业明细 服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
public interface IIndustrySubsidiaryService extends IService<IndustrySubsidiary> {

    /**
     * 列出所有行业
     *
     * @return {@link List}<{@link IndustrySubsidiary}>
     */
    List<IndustrySubsidiary> getAllIndustry();

    /**
     * 获取股票所属行业
     *
     * @param stockCode 股票编号
     * @return {@link IndustrySubsidiary }
     */
    IndustrySubsidiary getStockIndustry(String stockCode);

    /**
     * 获取股票所属行业
     *
     * @param industryCode 行业编号
     * @return {@link IndustrySubsidiary }
     */
    IndustrySubsidiary getOneIndustry(String industryCode);

    /**
     * 获取股票所属行业
     *
     * @param stockCodes 股票编号
     * @return {@link StockIndustry }
     */
    List<StockIndustry> getStockIndustries(List<String> stockCodes);
}
