package com.vv.finance.investment.bg.dto.stock;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @公司：微微科技有限公司（金融事业部）
 * @描述：
 * @作者：Liam（梁殿豪）
 * @邮箱：liangdianhao@vv.cn
 * @时间：2021/2/20 16:20
 * @版本：1.0
 */
@Data
public class GetLastPriceByDatetimeDTO implements Serializable {
    private String code;
    private BigDecimal price;
}
