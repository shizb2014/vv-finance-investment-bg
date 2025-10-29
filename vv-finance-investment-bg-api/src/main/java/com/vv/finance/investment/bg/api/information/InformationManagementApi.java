package com.vv.finance.investment.bg.api.information;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.bean.SimplePageResp;
import com.vv.finance.investment.bg.entity.information.PublishDto;
import com.vv.finance.investment.bg.entity.information.PublishLogDto;
import com.vv.finance.investment.bg.entity.information.StockNewsEntity;
import com.vv.finance.investment.bg.entity.information.StockNewsListReq;

import java.util.List;

/**
 * @author hamilton
 * @date 2021/9/17 14:22
 */
public interface InformationManagementApi {

    /**
     * 资讯管理列表
     * @param stockNewsListReq
     * @return
     */
     ResultT<SimplePageResp<StockNewsEntity>> list(StockNewsListReq stockNewsListReq);

    /**
     * 资讯详情
     * @param id
     * @return
     */
     ResultT<StockNewsEntity> detail(Long id );

    /**
     * 发布 取消发布
     * @param publishDto
     * @return
     */
     ResultT<Void> publish( PublishDto publishDto);

    /**
     * 发布 取消发布 日志
     * @param  id
     * @return
     */
     ResultT<List<PublishLogDto>> publishLog(Long id);

}
