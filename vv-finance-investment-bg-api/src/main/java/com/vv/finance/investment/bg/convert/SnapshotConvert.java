package com.vv.finance.investment.bg.convert;

import com.fenlibao.security.sdk.ws.core.model.dto.SnapshotDTO;
import com.google.common.base.Converter;
import com.vv.finance.common.entity.common.StockSnapshot;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author chenyu
 * @date 2020/11/26 11:21
 */
@Service
public class SnapshotConvert extends Converter<SnapshotDTO, StockSnapshot> {

    @Override
    protected StockSnapshot doForward(SnapshotDTO snapshotDTO) {
        StockSnapshot stockSnapshot = new StockSnapshot();
        BeanUtils.copyProperties(snapshotDTO, stockSnapshot);
        stockSnapshot.setPreClose(snapshotDTO.getPreclose());
        stockSnapshot.setTime(snapshotDTO.getTime().getTime());
        return stockSnapshot;
    }


    @Override
    protected SnapshotDTO doBackward(StockSnapshot stockSnapshot) {
        return null;
    }
}
