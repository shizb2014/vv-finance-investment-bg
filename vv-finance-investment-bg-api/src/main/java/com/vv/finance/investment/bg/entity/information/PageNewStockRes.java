package com.vv.finance.investment.bg.entity.information;

import com.vv.finance.base.domain.PageDomain;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 新股分页
 * @author wsliang
 * @date 2021/11/1 15:59
 **/
@Data
public class PageNewStockRes<T> extends PageWithTime<T> {

    private static final long serialVersionUID = -2012685288979376286L;

    /**
     * 当前页展示需要的时间日期集合
     */
    @ApiModelProperty("当前页展示需要的时间日期集合")
    private List<Long> dateList;

}
