package com.vv.finance.investment.bg.entity.f10.shareholder;

import com.vv.finance.base.domain.PageDomain;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/19 17:45
 * @Version 1.0
 */
@Data
@ToString
@NoArgsConstructor
public class MainShareholdingVo implements Serializable {

    private static final long serialVersionUID = -9138080548665287512L;

    /**
     * 更新日期
     */
    @ApiModelProperty(value = "更新日期")
    private Long updateDate;

    /**
     * 主要股东数据
     */
    @ApiModelProperty(value = "主要股东数据")
    private PageDomain<MainShareholding> mainShareholdingPageDomain;
}
