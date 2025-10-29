package com.vv.finance.investment.bg.entity.information;

import com.vv.finance.base.domain.PageDomain;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wsliang
 * @date 2021/11/1 15:59
 **/
@Data
public class PageWithTime<T> extends PageDomain<T> {
    private static final long serialVersionUID = 286401907477854325L;

    @ApiModelProperty("当前时间戳")
    private Long currentTime = System.currentTimeMillis();
}
