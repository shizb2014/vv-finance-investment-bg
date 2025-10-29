package com.vv.finance.investment.bg.dto.kline;

import com.vv.finance.investment.bg.dto.info.EventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hamilton
 * @date 2021/11/9 15:22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KlineDTO implements Serializable {
    private static final long serialVersionUID = 4670336431203000025L;
    /**
     * 不复权
     */
    private List<BaseKlineDTO> klineList;

    /**
     * 前复权
     */
    private List<BaseKlineDTO> forwardKlineList;

    /**
     * 后复权
     */
    private List<BaseKlineDTO> backwardKlineList;

    public  SimpleKlineListDto covertSimpleKlineList(){
        return SimpleKlineListDto.builder()
                .klineList(covertSimpleKline(this.klineList))
                .backwardKlineList(covertSimpleKline(this.backwardKlineList))
                .forwardKlineList(covertSimpleKline(this.forwardKlineList)).build();
    }
    public static List<SimpleKlineDto> covertSimpleKline(List<BaseKlineDTO> klineList){
        return klineList.stream().map(item->{
            SimpleKlineDto simpleKline=new SimpleKlineDto();
            BeanUtils.copyProperties(item,simpleKline);
            simpleKline.setChangeRate(StringUtils.isNotBlank(item.getChangeRate())?new BigDecimal(item.getChangeRate()):null);
            List<EventDTO> eventDTOS = item.getEvent();
            if(eventDTOS !=null){
                eventDTOS.sort(Comparator.comparing(EventDTO::getTime));
                simpleKline.setEvent(eventDTOS);
            }

            return simpleKline;
        }).collect(Collectors.toList());
    }

    public static List<SimpleKlineDto> covertTimeChart(List<BaseKlineDTO> klineList,boolean isPc,Boolean rt,BigDecimal preClose){
        BigDecimal todayPreClose = klineList.get(0).getPreClose() == null || klineList.get(0).getPreClose().compareTo(BigDecimal.ZERO) == 0
                ? klineList.get(0).getOpen() : klineList.get(0).getPreClose();
        return klineList.stream().map(item->{
            SimpleKlineDto simpleKline=new SimpleKlineDto();
            BeanUtils.copyProperties(item,simpleKline);
            simpleKline.setChangeRate(StringUtils.isNotBlank(item.getChangeRate())?new BigDecimal(item.getChangeRate()):null);
            // 说明是新股，取招股价，招股价取不到取当天的开盘价
            if (item.getPreClose() == null || item.getPreClose().compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal preCloseReal = preClose == null || preClose.compareTo(BigDecimal.ZERO) == 0 ? todayPreClose : preClose;
                item.setPreClose(preCloseReal);
                simpleKline.setPreClose(preCloseReal);
            }
            if(isPc||rt){
                if (item.getPreClose() != null && item.getPreClose().compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal chgPct = (item.getClose().subtract(item.getPreClose())).divide(item.getPreClose(), 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    simpleKline.setChgPct(chgPct);
                    simpleKline.setChg(item.getClose().subtract(item.getPreClose()));
                }
            }else {
                if (preClose != null && preClose.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal chgPct = (item.getClose().subtract(preClose)).divide(preClose, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    simpleKline.setChgPct(chgPct);
                    simpleKline.setChg(item.getClose().subtract(preClose));
                }
            }
            List<EventDTO> eventDTOS = item.getEvent();
            if(eventDTOS !=null){
                eventDTOS.sort(Comparator.comparing(EventDTO::getTime));
                simpleKline.setEvent(eventDTOS);
            }

            return simpleKline;
        }).collect(Collectors.toList());
    }
}
