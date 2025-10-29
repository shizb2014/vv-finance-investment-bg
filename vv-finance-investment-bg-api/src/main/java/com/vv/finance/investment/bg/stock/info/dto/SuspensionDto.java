package com.vv.finance.investment.bg.stock.info.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2020/10/29 19:01
 */
@Data
@EqualsAndHashCode
public class SuspensionDto  implements Serializable {
    private String code;
    private String suspension;
}
