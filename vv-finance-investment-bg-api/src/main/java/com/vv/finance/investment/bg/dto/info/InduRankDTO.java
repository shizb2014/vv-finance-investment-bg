package com.vv.finance.investment.bg.dto.info;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author chenyu
 * @date 2021/3/5 14:01
 */
@Data
@Builder
public class InduRankDTO implements Serializable {
    private List<InduBaseRankDTO> list;
    private Long time;
}
