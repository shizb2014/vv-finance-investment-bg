package com.vv.finance.investment.bg.api.index;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.dto.req.IndexQueryReq;
import com.vv.finance.investment.bg.entity.index.TIndexComponent;
import com.vv.finance.investment.bg.entity.index.TIndexDefine;
import com.vv.finance.investment.bg.entity.index.TIndexInfo;
import com.vv.finance.investment.bg.entity.index.TIndexSnapshot;

import java.util.List;

/**
 * @ClassName: IndexInfoService
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/28   9:59
 */
public interface IndexInfoService {

    /**
     * 获取成分股
     * @param indexCode
     * @return
     */

    ResultT<List<TIndexComponent>> queryComponent(String indexCode);

    /**
     * 获取指数快照
     * @param req
     * @return
     */

    ResultT<List<TIndexSnapshot>> querySnapshot(IndexQueryReq req);

    /**
     * 指数信息
     * @param req
     * @return
     */
    ResultT<List<TIndexInfo>> queryInfo(IndexQueryReq req);

    /**
     * 指数码表
     * @param define
     * @return
     */
    ResultT<Void> saveDefine(TIndexDefine define);

    /**
     * 成分股
     * @param component
     * @return
     */
    ResultT<Void> saveComponent(TIndexComponent component);

    /**
     * 指数快照
     * @param snapshot
     * @return
     */
    ResultT<Void> saveSnapshot(TIndexSnapshot snapshot);

    /**
     * 批量保存快照
     * @param snapshots
     * @return
     */
    ResultT<Void> saveSnapshotBatch(List<TIndexSnapshot> snapshots);

    /**
     * 指数信息
     * @param info
     * @return
     */
    ResultT<Void> saveInfo(TIndexInfo info);
}
