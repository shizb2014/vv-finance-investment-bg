package com.vv.finance.investment.bg.job.uts;


import com.vv.finance.investment.bg.entity.uts.SehkEp;
import com.vv.finance.investment.bg.mapper.uts.SehkEpMapper;
import com.vv.finance.investment.bg.stock.info.mapper.StockBrokerMapper;
import com.vv.finance.investment.bg.stock.quotes.StockBroker;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class StockBrokerUpdateJob {

    @Autowired
    SehkEpMapper sehkEpMapper;

    @Autowired
    StockBrokerMapper stockBrokerMapper;

    @XxlJob(value = "StockBrokerUpdate", cron = "0 0 5 ? * 2,3,4,5,6 *", author = "史志彪", desc = "更新经济席位名称")
    public ReturnT<String> StockBrokerUpdate(String param) {

        List<StockBroker> stockBrokers = new ArrayList<>();
        List<SehkEp> sehkEps = sehkEpMapper.selectList(null);
        for (SehkEp sehkEp : sehkEps) {
            if(("中国投资").equals(sehkEp.getShortName())){
                sehkEp.setShortName("沪港通");
            }
            if(("中国创盈").equals(sehkEp.getShortName())){
                sehkEp.setShortName("深港通");
            }
            if(StringUtils.isEmpty(sehkEp.getBroekrNo())){
                continue;
            }
            String[] split = sehkEp.getBroekrNo().split(",");
            for (String brokerNo : split) {
                StockBroker stockBroker = new StockBroker();
                stockBroker.setBrokerCode(codeBroKerCode(brokerNo.trim()));
                stockBroker.setSimplifiedName(sehkEp.getShortName());
                stockBroker.setComplexName(sehkEp.getShortName());
                stockBrokers.add(stockBroker);
            }
        }

        if (!CollectionUtils.isEmpty(stockBrokers)) {
            stockBrokerMapper.insertOrUpdateBatch(stockBrokers);
        }


        return ReturnT.SUCCESS;
    }

    /**
     * 经纪席位代码补齐4位
     *
     * @param code 经纪席位代码
     * @return
     */
    public static String codeBroKerCode(String code) {
        return new DecimalFormat("0000").format(Integer.valueOf(code));
    }


}
