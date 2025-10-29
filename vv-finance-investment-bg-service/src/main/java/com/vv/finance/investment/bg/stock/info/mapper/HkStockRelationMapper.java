package com.vv.finance.investment.bg.stock.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.investment.bg.stock.info.HkStockRelation;

import java.util.List;

/**
* @author Lekt
* @description 针对表【t_hk_stock_relation(股票关系表)】的数据库操作Mapper
* @createDate 2024-06-25 16:24:49
* @Entity generator.domain.HkStockRelation
*/
public interface HkStockRelationMapper extends BaseMapper<HkStockRelation> {

    Long selectStockIdByInnerCode(String innerCode);

    Long selectMaxStockId();

    List<String> selectInnerCodeList(String sourceCode);

    int updateSelectiveByStockId(HkStockRelation stockRelation);
}




