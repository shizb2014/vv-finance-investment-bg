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
public class KlineJobBean  implements Serializable {
    private static final long serialVersionUID = -4067373569668569311L;
    Integer num;
    Long day;
    String codes;

}
