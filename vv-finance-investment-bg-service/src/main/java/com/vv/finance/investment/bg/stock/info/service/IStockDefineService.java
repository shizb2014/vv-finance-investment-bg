package com.vv.finance.investment.bg.stock.info.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.investment.bg.stock.info.StockDefine;

import java.util.List;

/**
 * <p>
 * 股票码表 服务类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
public interface IStockDefineService extends IService<StockDefine> {

    /**
     * 查询股票列表（正股）
     *
     * @param columns 列
     * @return {@link List}<{@link StockDefine}>
     */
    List<StockDefine> listStockColumns(List<String> columns);

    /**
     * 查询股票列表（正股）
     *
     * @param codes 股票代码
     * @return {@link List}<{@link StockDefine}>
     */
    List<StockDefine> listStockByCodes(List<String> codes);

    /**
     * 查询股票列表
     *
     * @param columns   列
     * @param stockType 股票类型
     * @return {@link List}<{@link StockDefine}>
     */
    List<StockDefine> listColumnsByType(List<String> columns, Integer stockType);

    /**
     * 查询股票列表
     *
     * @param codes     股票代码
     * @param stockType 股票类型
     * @return {@link List}<{@link StockDefine}>
     */
    List<StockDefine> listDefinesByCodeType(List<String> codes, Integer stockType);
}
