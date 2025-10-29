//package com.vv.finance.investment.bg.api.impl.index;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import com.vv.finance.base.dto.ResultT;
//import com.vv.finance.investment.bg.api.index.IndexInfoService;
//import com.vv.finance.investment.bg.dto.req.IndexQueryReq;
//import com.vv.finance.investment.bg.entity.index.TIndexComponent;
//import com.vv.finance.investment.bg.entity.index.TIndexDefine;
//import com.vv.finance.investment.bg.entity.index.TIndexInfo;
//import com.vv.finance.investment.bg.entity.index.TIndexSnapshot;
//import com.vv.finance.investment.bg.mapper.index.TIndexComponentMapper;
//import com.vv.finance.investment.bg.mapper.index.TIndexDefineMapper;
//import com.vv.finance.investment.bg.mapper.index.TIndexInfoMapper;
//import com.vv.finance.investment.bg.mapper.index.TIndexSnapshotMapper;
//import org.apache.dubbo.config.annotation.DubboService;
//
//import javax.annotation.Resource;
//import java.util.List;
//
///**
// * @ClassName: IndexInfoServiceImpl
// * @Description:
// * @Author: Demon
// * @Datetime: 2020/10/27   16:51
// */
//@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
//public class IndexInfoServiceImpl extends IndexBaseServiceImpl implements IndexInfoService {
//
//    @Resource
//    private TIndexDefineMapper defineMapper;
//
//    @Resource
//    private TIndexComponentMapper componentMapper;
//
//    @Resource
//    private TIndexSnapshotMapper snapshotMapper;
//
//    @Resource
//    private TIndexInfoMapper infoMapper;
//
//    @Override
//    public ResultT<List<TIndexComponent>> queryComponent(String indexCode) {
//        List<TIndexComponent> listEntity = getListEntity(componentMapper, new QueryWrapper<TIndexComponent>()
//                .eq(TIndexComponent.COL_INDEX_CODE, indexCode));
//        return ResultT.success(listEntity);
//    }
//
//    @Override
//    public ResultT<List<TIndexSnapshot>> querySnapshot(IndexQueryReq req) {
//        List<TIndexSnapshot> listEntity = getListEntity(snapshotMapper, new QueryWrapper<TIndexSnapshot>()
//                .eq(TIndexSnapshot.COL_CODE, req.getCode()));
//
//        return ResultT.success(listEntity);
//    }
//
//    @Override
//    public ResultT<List<TIndexInfo>> queryInfo(IndexQueryReq req) {
//        List<TIndexInfo> listEntity = getListEntity(infoMapper, new QueryWrapper<TIndexInfo>()
//                .eq(TIndexInfo.COL_CODE, req.getCode()));
//        return ResultT.success(listEntity);
//    }
//
//    @Override
//    public ResultT<Void> saveDefine(TIndexDefine define) {
//        saveOrUpdate(define, defineMapper, new UpdateWrapper<TIndexDefine>().eq(TIndexDefine.COL_CODE, define.getCode()));
//        return ResultT.success();
//    }
//
//    @Override
//    public ResultT<Void> saveComponent(TIndexComponent component) {
//        saveOrUpdate(component, componentMapper, new UpdateWrapper<TIndexComponent>()
//                .eq(TIndexComponent.COL_CODE, component.getCode())
//                .eq(TIndexComponent.COL_INDEX_CODE, component.getIndexCode()));
//        return ResultT.success();
//    }
//
//    @Override
//    public ResultT<Void> saveSnapshot(TIndexSnapshot snapshot) {
//        saveOrUpdate(snapshot, snapshotMapper, new UpdateWrapper<TIndexSnapshot>()
//                .eq(TIndexSnapshot.COL_CODE, snapshot.getCode())
//                .eq(TIndexSnapshot.COL_TIME, snapshot.getTime()));
//        return ResultT.success();
//    }
//
//    @Override
//    public ResultT<Void> saveSnapshotBatch(List<TIndexSnapshot> snapshots) {
//
//         batchSaveOrUpdate(snapshots,snapshotMapper);
//         return ResultT.success();
//    }
//
//    @Override
//    public ResultT<Void> saveInfo(TIndexInfo info) {
//        saveOrUpdate(info, infoMapper, new UpdateWrapper<TIndexInfo>()
//                .eq(TIndexInfo.COL_CODE, info.getCode())
//                .eq(TIndexInfo.COL_DATE, info.getDate()));
//        return ResultT.success();
//    }
//}
