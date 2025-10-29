package com.vv.finance.investment.bg.dto.info;

import com.fenlibao.security.sdk.ws.core.model.resp.MarketStatisticsResp;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author chenyu
 * @date 2021/3/5 14:41
 */
@Data
@Builder
public class MarketStatisticsDTO implements Serializable {
    private static final long serialVersionUID = 9058560313478167695L;
    private MarketStatisticsResp marketStatisticsResp;
    private Long time;
}
