package com.vv.finance.investment.bg.stock.info.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.finance.common.dto.ComStockRelationDto;
import com.vv.finance.investment.bg.stock.info.HkStockRelation;

import java.util.List;
import java.util.Map;

/**
* @author Lekt
* @description 针对表【t_hk_stock_relation(股票关系表)】的数据库操作Service
* @createDate 2024-06-25 16:24:49
*/
public interface HkStockRelationService extends IService<HkStockRelation> {

    /**
     * 保存新关系
     *
     * @param codeList 股票code
     */
    void saveNewRelations(List<String> codeList);

    /**
     * 保存业务场景股票关系
     *
     * @param relationDtoList 关系 DTO
     * @return
     */
    Map<String, Long> updateStockRelations(List<ComStockRelationDto> relationDtoList);

    /**
     * 获取股票 ID
     *
     * @param relationDto 关系 DTO
     * @return {@link Long}
     */
    Long buildStockId(ComStockRelationDto relationDto);

    /**
     * 根据code批量查询stockId
     *
     * @param codes 代码
     * @return {@link Map}<{@link String}, {@link Long}>
     */
    Map<String, Long> selectStockIdByCodes(List<String> codes);

    /**
     * 批量查询复用之后的code
     *
     * @param codes 代码
     * @return {@link Map}<{@link String}, {@link String}>
     */
    Map<String, String> selectReuseCodeMap(List<String> codes);
    /**
     * 根据股票code(原code/现code)查询股票关联关系
     *
     * @param sourceCodes 原股票code
     * @return
     */
    List<HkStockRelation> selectByCodes(List<String> sourceCodes);
    /**
     * 获取指定日期发生转板/代码复用的股票信息
     *
     * @param bizTypes 业务类型，2-代码复用，3-转板
     * @param bizTime 业务时间，格式：yyyyMMdd，如临时股票存并行交易结束时间，代码复用或转板存对应变更时间
     * @return
     */
    List<HkStockRelation> selectByBizTypeAndBizTime(List<Integer> bizTypes,String bizTime);
}
