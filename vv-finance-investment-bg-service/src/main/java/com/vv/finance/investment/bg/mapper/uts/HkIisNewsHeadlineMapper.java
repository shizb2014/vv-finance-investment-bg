package com.vv.finance.investment.bg.mapper.uts;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vv.finance.investment.bg.entity.uts.HkIisNewsHeadline;
import com.vv.finance.investment.bg.entity.uts.HkIisNewsHeadlineBase;
import com.vv.finance.investment.bg.entity.uts.HkIisNewsHeadlineHistoric;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author chenyu
 * @since 2021-07-14
 */
@DS("db2")
public interface HkIisNewsHeadlineMapper extends BaseMapper<HkIisNewsHeadline> {

    /**
     * 查询公告
     * @param headlinePageDomain
     * @param code
     * @param type
     * @param types
     * @return
     */
    Page<HkIisNewsHeadlineBase> selectNotice(Page<HkIisNewsHeadlineBase> headlinePageDomain, @Param("code") String code, @Param("type") Integer type, @Param("values") List<String> types);
}
