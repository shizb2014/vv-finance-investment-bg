package com.vv.finance.investment.bg.api.impl.stock;

import cn.hutool.core.util.StrUtil;
import com.ibm.icu.text.Collator;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.common.constants.RedisKeyConstants;
import com.vv.finance.common.entity.common.StockSnapshot;
import com.vv.finance.common.utils.BigDecimalUtil;
import com.vv.finance.common.utils.SortListUtil;
import com.vv.finance.investment.bg.api.frontend.StockService;
import com.vv.finance.investment.bg.api.stock.AhStockApi;
import com.vv.finance.investment.bg.api.stock.StockInfoApi;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.dto.StockCodeNameBaseDTO;
import com.vv.finance.investment.bg.dto.stock.AhStockDetailResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author:maling
 * @Date:2023/6/28
 * @Description:
 */
@Slf4j
@RequiredArgsConstructor
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class AhStockApiImpl implements AhStockApi {

    @Resource
    private StockService stockService;

    @Resource
    private RedisClient redisClient;

    @Resource
    private StockInfoApi stockInfoApi;

    private final static String NAME = "name";

    private final static String CODE = "code";

    @Override
    public ResultT<List<StockCodeNameBaseDTO>> queryAhStockCodeList(String sort, String sortKey) {
        List<AhStockDetailResp> list = getAhStockDetailRespList(null);
        List<StockCodeNameBaseDTO> stockCodeNameBaseDTOList = getStockCodeNameBaseDTOList(list, sort, sortKey);
        return ResultT.success(stockCodeNameBaseDTOList);
    }

    @Override
    public ResultT<List<AhStockDetailResp>> queryAhStockDetailList(List<String> ahStockCodeList) {
        log.info("查询AH股票列表,ahStockCodeList={}",ahStockCodeList);
        List<AhStockDetailResp> list = getAhStockDetailRespList(ahStockCodeList);
        return ResultT.success(list);
    }

    private List<AhStockDetailResp> getAhStockDetailRespList(List<String> ahStockCodeList){

        List<AhStockDetailResp> list = redisClient.get(RedisKeyConstants.SOUTHWARD_AH_STOCK_LIST);
        if(CollectionUtils.isEmpty(list)){
            return new ArrayList<>();
        }
        if (!CollectionUtils.isEmpty(ahStockCodeList)) {
            list = list.stream().filter(origin ->
                    ahStockCodeList.contains(origin.getCode())).collect(Collectors.toList());
        }
        log.info("查询AH股票列表,list={}",list);
        String[] stockCodeList = list.stream().map(AhStockDetailResp::getCode).toArray(String[]::new);
        log.info("查询AH股票列表,stockCodeList={}",stockCodeList);
        //获取快照里面的最新价，涨跌幅，涨跌额，股息率TTM，市盈率TTM
        List<StockSnapshot> snapshotList = stockService.getSnapshotList(stockCodeList);
        Map<String, StockSnapshot> map = snapshotList.stream().collect(Collectors.toMap(StockSnapshot::getCode, Function.identity()));
        for(AhStockDetailResp resp :  list){
            resp.setCnChangeRate(BigDecimalUtil.getDivide100Result(resp.getCnChangeRate(),6));
            resp.setCnRelChangeRate(BigDecimalUtil.getDivide100Result(resp.getCnRelChangeRate(),6));
            resp.setAhPremiumRate(BigDecimalUtil.getDivide100Result(resp.getAhPremiumRate(),6));
            StockSnapshot stockSnapshot = map.get(resp.getCode());
            if(stockSnapshot != null){
                resp.setStockId(stockSnapshot.getStockId());
                resp.setLast(stockSnapshot.getLast());
                resp.setChgPct(stockSnapshot.getChgPct());
                resp.setChg(stockSnapshot.getChg());
                resp.setDividendRateTtm(BigDecimalUtil.getDivide100Result(stockSnapshot.getDividendRateTtm(),6));
                resp.setPeTtm(stockSnapshot.getPeTtm());
                resp.setTotalValue(stockSnapshot.getTotalValue());
                resp.setStockType(stockSnapshot.getStockType());
                resp.setRegionType(stockSnapshot.getRegionType());
            }
        }
        return list;
    }

    private List<StockCodeNameBaseDTO> getStockCodeNameBaseDTOList(List<AhStockDetailResp> list, String sort, String sortKey) {
        List<StockCodeNameBaseDTO> dtoList = new ArrayList<>();
        try {
            if (StringUtils.isEmpty(sort) || StringUtils.isEmpty(sortKey)) {
                sortKey = "chgPct";
                sort = SortListUtil.DESC;
            }
            if (NAME.equals(sortKey)) {
                boolean ascFlag = StrUtil.isBlank(sort) || SortListUtil.ASC.equalsIgnoreCase(sort);
                list.sort((o1, o2) -> {
                    Collator chinaCollator = Collator.getInstance(Locale.CHINESE);
                    return ascFlag ? chinaCollator.compare(o1.getName(), o2.getName()) : chinaCollator.compare(o2.getName(), o1.getName());
                });
            } else {
                list = (List<AhStockDetailResp>) SortListUtil.sortIncludingNull(list, sortKey, sort);
            }
            list.forEach(resp -> {
                StockCodeNameBaseDTO baseDTO = new StockCodeNameBaseDTO();
                baseDTO.setStockId(resp.getStockId());
                baseDTO.setCode(resp.getCode());
                baseDTO.setName(resp.getName());
                baseDTO.setRegionType(resp.getRegionType());
                baseDTO.setStockType(resp.getStockType());
                dtoList.add(baseDTO);
            });
        } catch (Exception e) {
            log.error("查询AH股票代码列表异常", e);
        }
        return dtoList;
    }

}