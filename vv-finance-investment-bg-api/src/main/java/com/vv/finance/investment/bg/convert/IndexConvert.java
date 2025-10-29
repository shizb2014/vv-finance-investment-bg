package com.vv.finance.investment.bg.convert;

import com.google.common.base.Converter;
import com.vv.finance.common.entity.receiver.Index;
import com.vv.finance.investment.bg.stock.quotes.IndexSnapshot;
import org.springframework.stereotype.Service;

/**
 * @author chenyu
 * @date 2020/12/10 11:16
 */
@Service
public class IndexConvert extends Converter<Index, IndexSnapshot> {
    @Override
    protected IndexSnapshot doForward(Index index) {

        return null;
    }

    @Override
    protected Index doBackward(IndexSnapshot indexSnapshot) {

        return null;
    }
}
