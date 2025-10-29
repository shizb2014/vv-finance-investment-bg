package com.vv.finance.investment.bg.entity.information;

import com.vv.finance.base.domain.PageDomain;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author wsliang
 * @date 2021/11/1 15:59
 **/
@Data
public class PageWithStockHolder<T> extends PageDomain<T> {

    private static final long serialVersionUID = 8178200474240125434L;

    @ApiModelProperty(value = "所有股东类型（A,C,D,F,I,L,T,O）")
    private List<String> holderTypes;

    @ApiModelProperty(value = "日期")
    private Long date;
}
