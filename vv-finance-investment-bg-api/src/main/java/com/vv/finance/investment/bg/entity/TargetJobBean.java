package com.vv.finance.investment.bg.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author hamilton
 * @date 2021/1/21 15:49
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TargetJobBean  implements Serializable {
    private static final long serialVersionUID = 3505320318669454935L;
    Integer num;
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    String date;
    String codes;
}
