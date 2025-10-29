package com.vv.finance.investment.bg.convert;

import com.fenlibao.security.sdk.ws.core.model.resp.KlineResp;
import com.google.common.base.Converter;
import com.vv.finance.common.entity.dto.IndicatorDTO;
import com.vv.finance.common.entity.dto.KLineAndIndicatorDTO;
import com.vv.finance.common.entity.dto.StockKline;
import com.vv.finance.investment.bg.dto.kline.BaseKlineDTO;
import com.vv.finance.common.calc.hk.entity.*;
import org.springframework.beans.BeanUtils;


/**
 * @author chenyu
 * @date 2021/3/16 14:00
 */
public class CalcKlineConvert extends Converter<KLineAndIndicatorDTO, BaseKlineDTO> {
    @Override
    protected BaseKlineDTO doForward(KLineAndIndicatorDTO klineResp) {
        BaseKlineDTO baseKlineDTO = new BaseKlineDTO();
        IndicatorDTO indicator = klineResp.getIndicator();
        StockKline stockKline = klineResp.getStockKline();
        BeanUtils.copyProperties(stockKline,baseKlineDTO);
        SAREntity sarEntity = new SAREntity();
        if (indicator.getSar()!=null){
            BeanUtils.copyProperties(indicator.getSar(),sarEntity);
//            sarEntity.setTime(indicator.getSar().getDateTime());
        }
        OBVEntity obvEntity = new OBVEntity();
        if (indicator.getObv()!=null){
            BeanUtils.copyProperties(indicator.getObv(),obvEntity);
//            obvEntity.setTime(indicator.getObv().getDateTime());
        }
        return baseKlineDTO;
    }

    @Override
    protected KLineAndIndicatorDTO doBackward(BaseKlineDTO baseKlineDTO) {
        return null;
    }
}
