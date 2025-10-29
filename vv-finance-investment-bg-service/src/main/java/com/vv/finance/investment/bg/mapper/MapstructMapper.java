package com.vv.finance.investment.bg.mapper;

import com.vv.finance.common.dto.ComStockSimpleDto;
import com.vv.finance.common.entity.quotation.common.ComSimpleStockDefine;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

@Mapper
public interface MapstructMapper {

    MapstructMapper INSTANCE = Mappers.getMapper(MapstructMapper.class);

    List<ComSimpleStockDefine> sourceToDestination(List<ComStockSimpleDto> source);

}
