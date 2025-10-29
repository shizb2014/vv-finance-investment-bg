package com.vv.finance.investment.bg.dto.stock;

import com.fenlibao.security.sdk.ws.core.model.resp.RsiResp;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author hamilton
 * @date 2020/12/10 10:54
 */
@Data
public class RsiDto implements Serializable {

    private static final long serialVersionUID = -4336937678793239058L;
    private Date date;
    private RsiResp rsi6;
    private RsiResp rsi12;

}
