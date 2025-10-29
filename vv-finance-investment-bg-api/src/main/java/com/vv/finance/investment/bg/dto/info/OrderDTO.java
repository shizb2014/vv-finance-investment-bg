package com.vv.finance.investment.bg.dto.info;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author chenyu
 * @date 2020/10/30 11:17
 */
@Data
public class OrderDTO implements Serializable {

    /**
     * 买方10档
     */
    private List<BuySellOrderDTO> buyList;

    /**
     * 卖方10档
     */
    private List<BuySellOrderDTO> sellList;
}
