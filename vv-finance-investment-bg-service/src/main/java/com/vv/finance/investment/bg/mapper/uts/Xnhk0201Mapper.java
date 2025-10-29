package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vv.finance.common.entity.quotation.f10.ComFinancialNotifyVO;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.Xnhk0201;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@DS("db2")
public interface Xnhk0201Mapper extends BaseMapper<Xnhk0201> {
    List<CompanyTrendsMergeEntity> listByDbmask(@Param("dbmask") Long dbmask, @Param("code") Integer code);


    CompanyTrendsMergeEntity getByPrimary(@Param("type") Integer type, @Param("seccode") String seccode, @Param("F002D") Long f002d, @Param("F006V") String f006v);

    List<ComFinancialNotifyVO> listFinancialReportList(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("list") List<String> codes);
}