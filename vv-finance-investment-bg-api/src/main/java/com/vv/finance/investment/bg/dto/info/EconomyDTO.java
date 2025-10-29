package com.vv.finance.investment.bg.dto.info;

import lombok.Data;

import java.io.Serializable;

/**
 * @author chenyu
 * @date 2020/10/29 19:39
 */
@Data
public class EconomyDTO implements Serializable {
    private static final long serialVersionUID = 2829605236022181613L;
    /**
     * 买卖方经济代码
     */
    private String code;

    /**
     * 买卖方经济名称
     */
    private String name;

}
