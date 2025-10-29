package com.vv.finance.investment.bg.api.stock;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.common.domain.PageWithCount;
import com.vv.finance.investment.bg.entity.move.StockMove;

import java.util.Collection;
import java.util.Set;

/**
 * @author lh.sz
 */
public interface StockMoveApi {

    /**
     * 异动股票数据落库
     *
     * @param stockMoves
     * @return
     */
    Boolean saveBatch(Collection<StockMove> stockMoves);

    /**
     * 分页查询异动主题
     *
     * @param page          page对象
     * @param codes         股票代码
     * @param moveType      异动主题
     * @param lastStockCode
     * @param lastTimeStamp
     * @return
     */
    Page<StockMove> pageList(Page page, Set<String> codes, Integer moveType,String lastStockCode,Long lastTimeStamp);

    /**
     * 分页查询异动主题
     *
     * @param page          page对象
     * @param codes         股票代码
     * @param moveType      异动主题
     * @param lastStockCode
     * @param lastTimeStamp
     * @return
     */
    PageWithCount<StockMove> pageListV2(Page page, Set<String> codes, Integer moveType,String lastStockCode,Long lastTimeStamp);

    /**
     * 分页查询多个异动主题
     *
     * @param page      page对象
     * @param codes     股票代码
     * @param moveTypes 异动主题
     * @param sortType  排序类型
     * @return
     */
    Page<StockMove> pageListByTypeList(Page page, Set<String> codes, Set<Integer> moveTypes, int sortType, String selectTimeStamp);

    /**
     * 分页查询多个异动主题
     *
     * @param page      page对象
     * @param codes     股票代码
     * @param moveTypes 异动主题
     * @param sortType  排序类型
     * @return
     */
    PageWithCount<StockMove> pageListByTypeListV2(Page page, Set<String> codes, Set<Integer> moveTypes, int sortType, String selectTimeStamp);

    /**
     * 分页查询多个异动主题
     *
     * @param page      page对象
     * @param codes     股票代码
     * @param moveTypes 异动主题
     * @param sortType  排序类型
     * @return
     */
    PageWithCount<StockMove> pageListByTypeListPc(Page page, Set<String> codes, Set<Integer> moveTypes, int sortType, String selectTimeStamp, Integer pageTurnType);

    /**
     * 删除临时股票异动数据
     */
    void delStockMoveByStockCode(String stockCode);

    /**
     * 变更异动数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    void updateStockMoveStockCode(String sourceCode, String targetCode);

    /**
     * 变更异动数据股票code
     *
     * @param sourceCode 原股票code
     * @param targetCode 目标股票code
     */
    void copyStockMoveStockCode(String sourceCode, String targetCode);

}
