package com.vv.finance.investment.bg.api.impl.broker;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vv.finance.investment.bg.api.broker.BrokerHKStockHotSpotApi;
import com.vv.finance.investment.bg.api.impl.HkTradingCalendarApiImpl;
import com.vv.finance.investment.bg.cache.StockCache;
import com.vv.finance.investment.bg.config.RedisClient;
import com.vv.finance.investment.bg.constants.BrokerConstants;
import com.vv.finance.investment.bg.dto.broker.BrokerHoldingsTrendDTO;
import com.vv.finance.investment.bg.dto.broker.TopConcentrationRankDTO;
import com.vv.finance.investment.bg.dto.broker.TopFiveConcentrationCalculateDTO;
import com.vv.finance.investment.bg.entity.broker.allBroker.BrokerShareHoldingsByCode;
import com.vv.finance.investment.bg.entity.uts.Xnhks0101;
import com.vv.finance.investment.bg.entity.uts.Xnhks0104;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0101Mapper;
import com.vv.finance.investment.bg.mapper.uts.Xnhks0104Mapper;
import com.vv.finance.investment.bg.stock.info.StockDefine;
import com.vv.finance.investment.bg.stock.info.mapper.StockDefineMapper;
import com.vv.finance.investment.bg.stock.rank.entity.IndustrySubsidiary;
import com.vv.finance.investment.bg.stock.rank.service.IIndustrySubsidiaryService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName BrokerHKStockHotSpotApiImpl
 * @Description 市场-港股热点 实现类
 * @Author liujiajian
 * @Date 2022/10/8
 * @Version: 1.0
 *
 */
@RequiredArgsConstructor
@DubboService(group = "${dubbo.investment.bg.service.group:bg}",registry="bgservice")
public class BrokerHKStockHotSpotApiImpl implements BrokerHKStockHotSpotApi {



    //公司基本资料-简体中文
    @Resource
    Xnhks0101Mapper xnhks0101Mapper;

    @Resource
    Xnhks0104Mapper xnhks0104Mapper;

    @Resource
    StockDefineMapper stockDefineMapper;

    @Resource
    RedisTemplate redisTemplate;

    @Resource
    RedisClient redisClient;

    @Resource
    BrokerCommonUtils brokerCommonUtils;

    @Autowired
    HkTradingCalendarApiImpl hkTradingCalendarApi;

    @Resource
    private IIndustrySubsidiaryService industrySubsidiaryService;

    @Resource
    private StockCache stockCache;

    @Override
    public List<BrokerHoldingsTrendDTO> getBrokerHoldingsRank(Integer type) {
        List<BrokerHoldingsTrendDTO> list=new ArrayList<>();
        //首先创建一个空的set用来保存从redis缓存中获取到的Zset结构的（Key,股票-经纪商,增减持比例）集合
        Set<ZSetOperations.TypedTuple<Object>> topFiveStock=new HashSet<>();
        //首先判断type的值为1or0,1-获取增持比例排行榜，0-获取减持比例排行榜
        switch (type){
            case 1:
                //从redis缓存中获取增持比例最大的五只股票集合,考虑到可能存在经纪商ID为0的情况，因此取10条，最后List截取前五
                topFiveStock=redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_TODAYQUANTITY,0,9);
                break;
            case 2:
                //从redis缓存中获取减持比例最大的五只股票集合，考虑到可能存在经纪商ID为0的情况，因此取10条，最后List截取前五
                topFiveStock=redisClient.getZSetRangeWithScore(BrokerConstants.BROKER_TODAYQUANTITY,0,9);
                break;
            case 3:
                //从redis缓存中获取增持市值最大的五只股票集合，考虑到可能存在经纪商ID为0的情况，因此取10条，最后List截取前五
                topFiveStock = redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_TODAYMARKETVAL, 0, 9);
                break;
            case 4:
                //从redis缓存中获取减持市值最大的五只股票集合，考虑到可能存在经纪商ID为0的情况，因此取10条，最后List截取前五
                topFiveStock=redisClient.getZSetRangeWithScore(BrokerConstants.BROKER_TODAYMARKETVAL,0,9);
                break;
            default:
                break;
        }
        //调用获取增减持比例排行榜的方法获取排行榜list
        list = getTopFivePercentRank(topFiveStock);
        return list;

    }


    @Override
    public List<TopConcentrationRankDTO> getTopBrokersHoldingsPercentRank(Integer type) {
        List<TopConcentrationRankDTO> topPercentRankList=new ArrayList<>();
        //创建空的集合set用来保存从redis缓存中获取到的Zset结构的（key,股票-top持股比例之和，变动比例）集合（变动比例为正）
        Set<ZSetOperations.TypedTuple<Object>> positiveTopFiveStock=new HashSet<>();
        //创建空的集合set用来保存从redis缓存中获取到的Zset结构的（key,股票-top持股比例之和，变动比例）集合（变动比例为正）
        Set<ZSetOperations.TypedTuple<Object>> negativeTopFiveStock=new HashSet<>();
        //创建空的包含10只股票数据的map
        Map<Double, TopFiveConcentrationCalculateDTO> topMap=new HashMap<>();
        //首先判断type的值为1 or 0,1-获取TOP5集中度变动排行榜，0-获取TOP10集中度变动排行榜
        switch (type){
            case 1:
                //从redis缓存中获取TOP5集中度变动比例的集合（变动比例为正）
                positiveTopFiveStock=redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_TOP5CHANGETODAY,0,9);
                //从redis缓存中获取TOP5集中度变动比例的集合（变动比例为负）
                negativeTopFiveStock=redisClient.getZSetRangeWithScore(BrokerConstants.BROKER_TOP5CHANGETODAY,0,9);
                topMap = createTopFiveMap(positiveTopFiveStock, negativeTopFiveStock);
                break;
            case 0:
                //从redis缓存中获取TOP10集中度变动比例的集合（变动比例为正）
                positiveTopFiveStock=redisClient.getZSetReverseRangeWithScore(BrokerConstants.BROKER_TOP10CHANGETODAY,0,9);
                //从redis缓存中获取TOP10集中度变动比例的集合（变动比例为负）
                negativeTopFiveStock=redisClient.getZSetRangeWithScore(BrokerConstants.BROKER_TOP10CHANGETODAY,0,9);
                topMap = createTopTenMap(positiveTopFiveStock, negativeTopFiveStock);
                break;
            default:
                break;
            }
        //调用获取集中度变动比例排行榜的方法获取排行榜list
        topPercentRankList=getTopPercentRankList(topMap);
        if(!CollectionUtils.isEmpty(topPercentRankList)){
            topPercentRankList = topPercentRankList.stream().sorted(Comparator.comparing(item -> item.getConcentrationTrend().abs())).collect(Collectors.toList());
            Collections.reverse(topPercentRankList);
        }
        return topPercentRankList;
    }

    /**
     * @Description 实现创建对象并赋值，返回排行榜list
     * @param topFiveStockSet
     * @return
     */
    public List<BrokerHoldingsTrendDTO> getTopFivePercentRank(Set<ZSetOperations.TypedTuple<Object>> topFiveStockSet){
        //创建一个空的集合用来保存增减持比例前五的股票对象
        ArrayList<BrokerHoldingsTrendDTO> list=new ArrayList<>();
        //循环，创建 持股动向对象，根据5只股票将对象赋值，添加到list中，返回排行榜list<对象>
        Iterator<ZSetOperations.TypedTuple<Object>> iterator = topFiveStockSet.iterator();
        // 股票ID Map
        Map<String, Long> stockCodeIdMap = stockCache.queryStockIdMap(null);
        //遍历包含五支股票的集合
        while(iterator.hasNext()){
            //创建一个新的持股动向对象
            BrokerHoldingsTrendDTO brokerHoldingsTrendDTO=new BrokerHoldingsTrendDTO();
            ZSetOperations.TypedTuple<Object> next = iterator.next();
            //获取到（股票code,经纪商id)
            String value = (String) next.getValue();
            String[] split = value.split("-");
            //获取到股票code
            String code = split[0];
            //获取到经纪商Id
            String brokerId = split[1];
            //根据经纪商id获取经纪商名称
            String brokerName = brokerCommonUtils.getBrokerName(brokerCommonUtils.getBrokerId(brokerId));
            //过滤掉经纪商id有问题的数据
            if("0".equals(brokerId)||brokerId.length()==4){
                continue;
            }
            //从表stockDefine中获取到股票的基本信息
            StockDefine stockDefine = stockDefineMapper.selectOne(new QueryWrapper<StockDefine>().eq("code", code));
            //从表Xnhks0104中获取股票所属的行业
            // Xnhks0104 xnhks0104 = xnhks0104Mapper.selectOne(new QueryWrapper<Xnhks0104>().eq("SECCODE",code));
            IndustrySubsidiary subsidiary = industrySubsidiaryService.getStockIndustry(code);
            // String industryName = xnhks0104 == null ? "" : xnhks0104.getF014v();
            //获取到股票的增持比例
            Double increasePercent = next.getScore();
            brokerHoldingsTrendDTO.setCode(code);
            brokerHoldingsTrendDTO.setStockId(stockCodeIdMap.get(code));
            brokerHoldingsTrendDTO.setName(stockDefine.getStockName());
            brokerHoldingsTrendDTO.setIndustryId(ObjectUtil.defaultIfNull(subsidiary, IndustrySubsidiary::getCode, null));
            brokerHoldingsTrendDTO.setIndustryName(ObjectUtil.defaultIfNull(subsidiary, IndustrySubsidiary::getName, null));
            brokerHoldingsTrendDTO.setBrokerId(brokerId);
            brokerHoldingsTrendDTO.setBrokerName(brokerName);
            brokerHoldingsTrendDTO.setNum(BigDecimal.valueOf(increasePercent));
            list.add(brokerHoldingsTrendDTO);
        }
        return list.size()<=5 ? list : list.subList(0,5);
    }

    /**
     * @Description 创建一个Map,保存变动比例为正的五只股票集合与变动比例为负的五只股票(Top5集中度）
     * @param positiveTopFiveStockSet
     * @param negativeTopFiveStockSet
     * @return
     */
    public Map<Double, TopFiveConcentrationCalculateDTO> createTopFiveMap(Set<ZSetOperations.TypedTuple<Object>> positiveTopFiveStockSet,Set<ZSetOperations.TypedTuple<Object>> negativeTopFiveStockSet){
        //创建一个空的map，保存10个正负值的数据
        Map<Double,TopFiveConcentrationCalculateDTO> map=new HashMap<>();
        //循环，创建 持股动向对象，根据5只股票将对象赋值，添加到list中，返回排行榜list<对象>
        Iterator<ZSetOperations.TypedTuple<Object>> positiveIterator = positiveTopFiveStockSet.iterator();
        while(positiveIterator.hasNext()){
            TopFiveConcentrationCalculateDTO topFive=new TopFiveConcentrationCalculateDTO();
            ZSetOperations.TypedTuple<Object> next = positiveIterator.next();
            //获取到股票code
            String code = (String) next.getValue();
            //根据key和股票code，从redis中获取到top5比例之和
            Double topFiveConcentration = redisClient.getScore(BrokerConstants.BROKER_TOP5CONCENTRATION, code)==null?0.0:redisClient.getScore(BrokerConstants.BROKER_TOP5CONCENTRATION,code);
            if(topFiveConcentration>1 || topFiveConcentration<-1){
                continue;
            }
            topFive.setCode(code);
            topFive.setTopConcentrationPercent(BigDecimal.valueOf(topFiveConcentration));
            topFive.setConcentrationTrend(BigDecimal.valueOf(next.getScore()));
            map.put(next.getScore(),topFive);
        }
        //循环，创建 持股动向对象，根据5只股票将对象赋值，添加到list中，返回排行榜list<对象>
        Iterator<ZSetOperations.TypedTuple<Object>> negativeIterator = negativeTopFiveStockSet.iterator();
        while(negativeIterator.hasNext()){
            TopFiveConcentrationCalculateDTO topFive=new TopFiveConcentrationCalculateDTO();
            ZSetOperations.TypedTuple<Object> next = negativeIterator.next();
            String code = (String) next.getValue();
            Double topFiveConcentration = redisClient.getScore(BrokerConstants.BROKER_TOP5CONCENTRATION, code)==null?0.0:redisClient.getScore(BrokerConstants.BROKER_TOP5CONCENTRATION,code);
            if(topFiveConcentration>1 || topFiveConcentration<-1){
                continue;
            }
            topFive.setCode(code);
            topFive.setTopConcentrationPercent(BigDecimal.valueOf(topFiveConcentration));
            topFive.setConcentrationTrend(BigDecimal.valueOf(next.getScore()));
            map.put(next.getScore(),topFive);
        }
        return map;
    }

    /**
     * @Description 创建一个Map,保存变动比例为正的五只股票集合与变动比例为负的五只股票(Top10集中度）
     * @param positiveTopFiveStockSet
     * @param negativeTopFiveStockSet
     * @return
     */
    public Map<Double, TopFiveConcentrationCalculateDTO> createTopTenMap(Set<ZSetOperations.TypedTuple<Object>> positiveTopFiveStockSet,Set<ZSetOperations.TypedTuple<Object>> negativeTopFiveStockSet){
        //创建一个空的map，保存10个正负值的数据
        Map<Double,TopFiveConcentrationCalculateDTO> map=new HashMap<>();
        //循环，创建 持股动向对象，根据5只股票将对象赋值，添加到list中，返回排行榜list<对象>
        Iterator<ZSetOperations.TypedTuple<Object>> positiveIterator = positiveTopFiveStockSet.iterator();
        while(positiveIterator.hasNext()){
            TopFiveConcentrationCalculateDTO topTen=new TopFiveConcentrationCalculateDTO();
            ZSetOperations.TypedTuple<Object> next = positiveIterator.next();
            //获取到股票code
            String code = (String) next.getValue();
            //根据key和股票code，从redis中获取到top5比例之和
            Double topTenConcentration = redisClient.getScore(BrokerConstants.BROKER_TOP10CONCENTRATION, code)==null?0.0:redisClient.getScore(BrokerConstants.BROKER_TOP10CONCENTRATION, code);
            if(topTenConcentration>1 || topTenConcentration<-1){
                continue;
            }
            topTen.setCode(code);
            topTen.setTopConcentrationPercent(BigDecimal.valueOf(topTenConcentration));
            topTen.setConcentrationTrend(BigDecimal.valueOf(next.getScore()));
            map.put(next.getScore(),topTen);
        }
        //循环，创建 持股动向对象，根据5只股票将对象赋值，添加到list中，返回排行榜list<对象>
        Iterator<ZSetOperations.TypedTuple<Object>> negativeIterator = negativeTopFiveStockSet.iterator();
        while(negativeIterator.hasNext()){
            TopFiveConcentrationCalculateDTO topTen=new TopFiveConcentrationCalculateDTO();
            ZSetOperations.TypedTuple<Object> next = negativeIterator.next();
            String code = (String) next.getValue();
            Double topTenConcentration = redisClient.getScore(BrokerConstants.BROKER_TOP10CONCENTRATION, code)==null?0.0:redisClient.getScore(BrokerConstants.BROKER_TOP10CONCENTRATION, code);
            if(topTenConcentration>1 || topTenConcentration<-1){
                continue;
            }
            topTen.setCode(code);
            topTen.setTopConcentrationPercent(BigDecimal.valueOf(topTenConcentration));
            topTen.setConcentrationTrend(BigDecimal.valueOf(next.getScore()));
            map.put(next.getScore(),topTen);
        }
        return map;
    }

    /**
     * @Description 对包含10只股票数据的map进行处理，得到集中度变动比例前五的排行榜List
     * @param map
     * @return
     */
    public List<TopConcentrationRankDTO> getTopPercentRankList(Map<Double,TopFiveConcentrationCalculateDTO> map){
        //创建一个空的集合用来保存top5集中度比例变动排行榜的五个股票对象数据-TopConcentrationRankDTO
        ArrayList<TopConcentrationRankDTO> list=new ArrayList<>();
        //调用sortTopFiveHoldingsTrend方法,获取到top5集中度变动比例最大的五只股票的数据
        Map<Double, TopFiveConcentrationCalculateDTO> topFiveConcentrationCalculateDTOMap = sortTopFiveHoldingsTrend(map);
        Map<String, Long> stockCodeIdMap = stockCache.queryStockIdMap(null);
        //排序取到前五股票后，创建对象，赋值，得到得到包含TopConcentrationRankDTO的集合
        for(Map.Entry<Double,TopFiveConcentrationCalculateDTO> entry: topFiveConcentrationCalculateDTOMap.entrySet()){
            TopConcentrationRankDTO topConcentrationRankDTO=new TopConcentrationRankDTO();
            //获取到股票代码
            String code=entry.getValue().getCode();
            //从表stockDefine中获取到股票的基本信息
            StockDefine stockDefine = stockDefineMapper.selectOne(new QueryWrapper<StockDefine>().eq("code", code));
            //从表Xnhks0104中获取股票所属的行业
            IndustrySubsidiary subsidiary = industrySubsidiaryService.getStockIndustry(code);
            // Xnhks0104 xnhks0104 = xnhks0104Mapper.selectOne(new QueryWrapper<Xnhks0104>().eq("SECCODE",code));
            //String industryName = xnhks0104.getF014v();
            //对象赋值
            // if(Objects.isNull(subsidiary)){
            //     topConcentrationRankDTO.setIndustryName(null);
            //
            // }else{
            //     topConcentrationRankDTO.setIndustryName(subsidiary.getName());
            // }
            topConcentrationRankDTO.setCode(code);
            topConcentrationRankDTO.setStockId(stockCodeIdMap.get(code));
            topConcentrationRankDTO.setName(stockDefine.getStockName());
            // topConcentrationRankDTO.setIndustryId(stockDefine.getIndustryCode());
            topConcentrationRankDTO.setIndustryId(ObjectUtil.defaultIfNull(subsidiary, IndustrySubsidiary::getCode, null));
            topConcentrationRankDTO.setIndustryName(ObjectUtil.defaultIfNull(subsidiary, IndustrySubsidiary::getName, null));
            topConcentrationRankDTO.setTopConcentrationPercent(entry.getValue().getTopConcentrationPercent());
            topConcentrationRankDTO.setConcentrationTrend(entry.getValue().getConcentrationTrend().divide(BigDecimal.valueOf(100),6, RoundingMode.HALF_UP));
            list.add(topConcentrationRankDTO);
        }
        //将list 排序
        return list.stream().sorted(Comparator.comparing(TopConcentrationRankDTO::getConcentrationTrend).reversed()).collect(Collectors.toList());

    }


    /**
     * @Description 对map进行排序的方法（根据变动比例的绝对值进行排序），在处理top集中度变动比例时调用
     * @param map
     * @return
     */

    public static Map<Double, TopFiveConcentrationCalculateDTO> sortTopFiveHoldingsTrend(Map<Double,TopFiveConcentrationCalculateDTO> map){
        //利用Map的entrySet方法，转化为list进行排序
        List<Map.Entry<Double, TopFiveConcentrationCalculateDTO>> entryList=new ArrayList<Map.Entry<Double, TopFiveConcentrationCalculateDTO>>(map.entrySet());
        //利用collectios的sort方法对list排序,根据key的绝对值进行排序
        Collections.sort(entryList, new Comparator<Map.Entry<Double, TopFiveConcentrationCalculateDTO>>() {
            @Override
            public int compare(Map.Entry<Double, TopFiveConcentrationCalculateDTO> o1, Map.Entry<Double,TopFiveConcentrationCalculateDTO> o2) {
                Double key1 = Math.abs(o1.getKey());
                Double key2 =Math.abs(o2.getKey());
                return key2.compareTo(key1);
            }
        });
        //遍历排序好的list，只取前五支股票的持股数据
        LinkedHashMap<Double,TopFiveConcentrationCalculateDTO> linkedHashMap=new LinkedHashMap<Double, TopFiveConcentrationCalculateDTO>();
        if(entryList.size()<5){
            for (int i = 0; i < entryList.size() ; i++) {
                linkedHashMap.put(entryList.get(i).getKey(), entryList.get(i).getValue());
            }
        }else{
            for (int i = 0; i < 5 ; i++) {
                linkedHashMap.put(entryList.get(i).getKey(), entryList.get(i).getValue());
            }
        }
        return linkedHashMap;
    }


    /**
     * 通过四位经纪商ID获取到六位经纪商ID
     *
     * @param brokerId
     * @return
     */
//    public  String getBrokerId(String brokerId) {
//        if(brokerId.equals("9001")){
//            return "A00003";
//        }
//        if(brokerId.equals("9002")){
//            return "A00004";
//        }
//        return redisClient.get(BrokerConstants.BG_BROKER_ID_RELATION_PROFIT.concat(brokerId));
//    }
}
