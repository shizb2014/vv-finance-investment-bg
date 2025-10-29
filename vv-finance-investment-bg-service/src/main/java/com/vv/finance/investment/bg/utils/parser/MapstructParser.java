package com.vv.finance.investment.bg.utils.parser;

import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.quotation.common.ComSimpleStockDefine;
import com.vv.finance.investment.bg.mapper.MapstructMapper;

import java.util.List;

/**
 * description: MapstructParser
 * date: 2024/1/5 11:20
 * author: shizhibiao
 */
public class MapstructParser {

    public static List<ComSimpleStockDefine> convertBean(List<ComStockSimpleDto> source) {
        MapstructMapper mapper = MapstructMapper.INSTANCE;
        return mapper.sourceToDestination(source);
    }

}
