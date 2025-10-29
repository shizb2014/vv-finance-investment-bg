package com.vv.finance.investment.bg.warant.service;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.fastjson.JSON;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.entity.common.WarrantSnapshot;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.warrant.WarrantCodeVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName Warrant
 * @Deacription 权证
 * @Author lh.sz
 * @Date 2021年12月21日 14:29
 **/
@Component
@Slf4j
public class WarrantService {

    @Resource
    RedisClient redisClient;

    private static final String UP = "up";

    /**
     * 获取权证code
     *
     * @param sort    排序(up-升序，down-降序)
     * @param sortKey 排序字段
     * @return List
     */
    public List<WarrantCodeVo> getWarrantCodeList(String sort,
                                                  String sortKey) {
        Map<Object, Object> map = redisClient.hmget(RedisKeyConstants.RECEIVER_WARRANT_MAP_STOCK_SNAPSHOT);
        List<WarrantSnapshot> warrantSnapshots = map.values().stream().map(s ->
                JSON.parseObject(s.toString(), WarrantSnapshot.class)).filter(s ->
                StringUtils.isNotEmpty(s.getStockCode())).collect(Collectors.toList());
//        if (UP.equals(sort)) {
//            warrantSnapshots.sort(Comparator.comparing(s -> ReflectUtil.getFieldValue(s, sortKey).toString(),
//                    Comparator.nullsFirst(Comparator.naturalOrder())));
//        } else {
//            warrantSnapshots.sort(Comparator.comparing(s -> ReflectUtil.getFieldValue(s, sortKey).toString(),
//                    Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
//        }
        if (UP.equals(sort)) {
            warrantSnapshots.sort(Comparator.comparing(s -> {
                String val = "";
                if (ReflectUtil.getFieldValue(s, sortKey) != null) {
                    val = ReflectUtil.getFieldValue(s, sortKey).toString();
                }
                return val;
            }));
        } else {
            warrantSnapshots.sort(Comparator.comparing(s -> {
                String val = "";
                if (ReflectUtil.getFieldValue(s, sortKey) != null) {
                    val = ReflectUtil.getFieldValue(s, sortKey).toString();
                }
                return val;
            }).reversed());
        }
        return warrantSnapshots.stream().map(s -> {
            WarrantCodeVo vo = new WarrantCodeVo();
            vo.setWarrantCode(s.getWarrantCode());
            vo.setWarrantName(s.getWarrantName());
            return vo;
        }).collect(Collectors.toList());
    }

}
