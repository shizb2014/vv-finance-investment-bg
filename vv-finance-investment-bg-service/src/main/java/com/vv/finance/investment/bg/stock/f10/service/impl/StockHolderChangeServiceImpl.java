package com.vv.finance.investment.bg.stock.f10.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.common.utils.BigDecimalUtil;
import com.vv.finance.investment.bg.api.uts.IShareholderService;
import com.vv.finance.investment.bg.entity.f10.shareholder.StockHolder;
import com.vv.finance.investment.bg.entity.f10.shareholder.StockHolderChange;
import com.vv.finance.investment.bg.mapper.uts.Xnhk0129Mapper;
import com.vv.finance.investment.bg.stock.f10.mapper.StockHolderChangeMapper;
import com.vv.finance.investment.bg.stock.f10.service.IStockHolderChangeService;
import com.vv.finance.investment.bg.utils.TimeConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 股票码表 服务实现类
 * </p>
 *
 * @author hamilton
 * @since 2020-10-29
 */
@Service
@DS("db1")
@Slf4j
public class StockHolderChangeServiceImpl extends ServiceImpl<StockHolderChangeMapper, StockHolderChange> implements IStockHolderChangeService {

    @Resource
    private IShareholderService shareholderService;

    @Resource
    private Xnhk0129Mapper xnhk0129Mapper;

    @Resource
    private StockHolderChangeMapper stockHolderChangeMapper;

    private static final Long INIT_DATE = 20231030L;

    @Override
    public void updateStockHolderChangeByCode(String code) {
        List<StockHolder> stockHolders = xnhk0129Mapper.totalQuantityByType(code);
        if (CollUtil.isEmpty(stockHolders)) {
            log.warn("IStockHolderChangeService updateStockHolderChangeByCode for [{}] stockHolders is empty!", code);
            return;
        }

        List<StockHolder> updateHolders = new ArrayList<>(stockHolders);
        // 最新季度数据
        List<StockHolder> buildHolders = shareholderService.buildStockHolderList(code, true, false, true, updateHolders);
        // 转换
        List<StockHolderChange> utsHolderChanges = buildHolders.stream().map(holder -> {
            StockHolderChange change = new StockHolderChange();
            BeanUtil.copyProperties(holder, change);
            change.setCode(code);
            change.setChangeDate(holder.getDate());
            change.setQuaDate(TimeConvertUtil.getEndDayOfQuarter());
            return change;
        }).collect(Collectors.toList());
        // 库中所有数据
        List<StockHolderChange> dbHoldChanges = stockHolderChangeMapper.selectList(Wrappers.<StockHolderChange>lambdaQuery().eq(StockHolderChange::getCode, code));

        List<StockHolderChange> insertList = new ArrayList<>();
        List<StockHolderChange> updateList = new ArrayList<>();
        List<Long> deleteIdList = new ArrayList<>();

        Set<Long> duplicateIdList = getDuplicateIdList(dbHoldChanges);
        deleteIdList.addAll(duplicateIdList);

        // db记录根据季度、类型分组
        Map<Long, Map<String, StockHolderChange>> dbQuaTypeListMap = dbHoldChanges.stream().filter(dhc -> !duplicateIdList.contains(dhc.getId())).collect(
                Collectors.groupingBy(StockHolderChange::getQuaDate,
                        Collectors.groupingBy(StockHolderChange::getHolderType,
                                HashMap::new, Collectors.collectingAndThen(Collectors.toList(),
                                        list -> CollUtil.sort(list, Comparator.comparing(StockHolderChange::getChangeDate).reversed()).get(0)
                                )
                        )
                )
        );

        // uts记录分组
        Map<Long, HashMap<String, StockHolderChange>> utsQuaTypeMap = utsHolderChanges.stream().collect(
                Collectors.groupingBy(StockHolderChange::getQuaDate,
                        Collectors.groupingBy(StockHolderChange::getHolderType,
                                HashMap::new, Collectors.collectingAndThen(Collectors.toList(),
                                        list -> CollUtil.sort(list, Comparator.comparing(StockHolderChange::getChangeDate).reversed()).get(0)
                                )
                        )
                )
        );

        // quaDate列表
        Set<Long> quaDateList = CollUtil.unionDistinct(dbQuaTypeListMap.keySet(), utsQuaTypeMap.keySet());

        for (Long quaDate : quaDateList) {
            Map<String, StockHolderChange> dbListMap = dbQuaTypeListMap.get(quaDate);
            Map<String, StockHolderChange> utsListMap = utsQuaTypeMap.get(quaDate);

            if (MapUtil.isNotEmpty(utsListMap)) {
                if (MapUtil.isEmpty(dbListMap)) {
                    // 新增
                    insertList.addAll(utsListMap.values());
                } else {
                    // 更新
                    Set<String> holderTypes = CollUtil.unionDistinct(dbListMap.keySet(), utsListMap.keySet());
                    for (String holderType : holderTypes) {
                        // 可能存在类型增多或减少
                        StockHolderChange dbChange = dbListMap.get(holderType);
                        StockHolderChange utsChange = utsListMap.get(holderType);
                        if (ObjectUtil.isNotEmpty(dbChange) && ObjectUtil.isNotEmpty(utsChange)) {
                            // 更新持股占比
                            BeanUtil.copyProperties(utsChange, dbChange, CopyOptions.create().ignoreNullValue());
                            updateList.add(dbChange);
                        } else if (ObjectUtil.isNotEmpty(dbChange) && ObjectUtil.isEmpty(utsChange)) {
                            // 类型减少了
                            deleteIdList.add(dbChange.getId());
                        } else if (ObjectUtil.isEmpty(dbChange) && ObjectUtil.isNotEmpty(utsChange)) {
                            // 类型增加了
                            insertList.add(utsChange);
                        }
                    }
                }
            }
        }

        if (CollUtil.isNotEmpty(insertList)) {
            this.saveBatch(insertList);
        }

        if (CollUtil.isNotEmpty(updateList)) {
            this.updateBatchById(updateList);
        }

        if (CollUtil.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        // updateHoldChangeOld(code, quaDate, updateHolderChanges, deleteHolderChanges, buildHolders);
    }

    private void updateHoldChangeOld(String code, List<StockHolderChange> updateHolderChanges, List<Long> deleteHolderChanges, List<StockHolder> buildHolders) {
        long quaDate = TimeConvertUtil.getEndDayOfQuarter();
        List<StockHolderChange> quaHolderChanges = stockHolderChangeMapper.getHolderChangeList(code, quaDate);
        if (CollUtil.isEmpty(quaHolderChanges)) {
            // 新增当前季度记录
            // buildHolders.addAll(stockHolders);
            List<StockHolderChange> holderChanges = buildHolders.stream().map(sh -> {
                return StockHolderChange.builder().code(code).holderType(sh.getHolderType()).shareType(sh.getShareType())
                        .num(sh.getNum()).pop(sh.getPop()).changeDate(sh.getDate()).quaDate(quaDate).build();
            }).collect(Collectors.toList());
            updateHolderChanges.addAll(holderChanges);
        } else {
            List<Long> originalDateList = buildHolders.stream().map(StockHolder::getDate).collect(Collectors.toList());
            List<Long> changeDateList = quaHolderChanges.stream().map(StockHolderChange::getChangeDate).collect(Collectors.toList());
            // 有变更的日期
            List<Long> diffDates = CollUtil.subtractToList(originalDateList, changeDateList);

            if (CollUtil.isEmpty(diffDates)) {
                log.warn("IStockHolderChangeService updateStockHolderChangeByCode for [{}] diffDates is empty!", code);
                return;
            }

            // 最大日期所属季度
            // Long maxDate = CollUtil.max(diffDates);
            // long maxQua = Long.parseLong(DateUtil.format(DateUtil.endOfQuarter(DateUtil.parse(String.valueOf(CollUtil.max(diffDates)), DatePattern.PURE_DATE_FORMAT)), DatePattern.PURE_DATE_FORMAT));
            long maxQua = TimeConvertUtil.getEndDayOfQuarter(CollUtil.max(diffDates));
            // 那个季度变更记录
            List<StockHolderChange> thatHolderChanges = stockHolderChangeMapper.getHolderChangeList(code, maxQua);
            // 最新股东持股占比
            Map<String, StockHolder> stockHolderMap = buildHolders.stream().collect(Collectors.toMap(StockHolder::getHolderType, v -> v, (o, v) -> v));

            Map<String, StockHolderChange> holderChangeMap = thatHolderChanges.stream().collect(Collectors.toMap(StockHolderChange::getHolderType, v -> v, (o, v) -> v));

            List<StockHolderChange> resultChanges = buildHolders.stream().map(holder -> {
                StockHolderChange change = new StockHolderChange();
                String holderType = holder.getHolderType();
                StockHolderChange dbChange = holderChangeMap.get(holderType);
                BeanUtils.copyProperties(holder, change, "id");
                change.setCode(code);
                change.setChangeDate(holder.getDate());
                change.setQuaDate(TimeConvertUtil.getEndDayOfQuarter(holder.getDate()));
                if (ObjectUtil.isNotEmpty(dbChange)) {
                    change.setId(dbChange.getId());
                }
                return change;
            }).collect(Collectors.toList());

            updateHolderChanges.addAll(resultChanges);

            List<Long> changeIdList = thatHolderChanges.stream().filter(change -> !stockHolderMap.containsKey(change.getHolderType())).map(StockHolderChange::getId).collect(Collectors.toList());
            deleteHolderChanges.addAll(changeIdList);
        }

        if (CollUtil.isNotEmpty(updateHolderChanges)) {
            this.saveOrUpdateBatch(updateHolderChanges);
        }

        if (CollUtil.isNotEmpty(deleteHolderChanges)) {
            this.removeByIds(deleteHolderChanges);
        }
    }

    private Set<Long> getDuplicateIdList(List<StockHolderChange> holderChanges) {
        HashMap<String, List<StockHolderChange>> quaTypeListMap = holderChanges.stream().collect(
                Collectors.groupingBy(f -> buildKey(f.getQuaDate(), f.getHolderType()),
                        HashMap::new, Collectors.collectingAndThen(Collectors.toList(),
                                list -> CollUtil.sort(list, Comparator.comparing(StockHolderChange::getChangeDate).reversed())
                        )
                )
        );
        // 重复ID
        List<Long> duplicateIdList = quaTypeListMap.values().stream().filter(list -> CollUtil.size(list) > 1).map(list -> {
            List<StockHolderChange> duplicateList = CollUtil.sub(list, 1, CollUtil.size(list));
            return duplicateList.stream().map(StockHolderChange::getId).collect(Collectors.toList());
        }).reduce(new ArrayList<>(), CollUtil::unionAll);
        // 无类型或日期
        Set<Long> noQuaTypeList = holderChanges.stream().filter(hc ->
                ObjectUtil.isEmpty(hc.getQuaDate()) || StrUtil.isBlank(hc.getHolderType())
                || BigDecimalUtil.isNullOrZero(hc.getNum()) || BigDecimalUtil.isNullOrZero(hc.getPop())
        ).map(StockHolderChange::getId).collect(Collectors.toSet());
        // 无效ID
        Set<Long> invalidIds = holderChanges.stream().filter(hc -> hc.getQuaDate() < INIT_DATE).map(StockHolderChange::getId).collect(Collectors.toSet());
        // 去重
        return CollUtil.unionDistinct(duplicateIdList, noQuaTypeList, invalidIds);
    }

    private String buildKey(Long quaDate, String holderType) {
        return StrUtil.join(StrUtil.UNDERLINE, quaDate, holderType);
    }

    private boolean isSameQuarter(Long before, Long after) {
        int q1 = DateUtil.quarter(DateUtil.parse(String.valueOf(before)));
        int q2 = DateUtil.quarter(DateUtil.parse(String.valueOf(after)));
        return q1 == q2;
    }
}
