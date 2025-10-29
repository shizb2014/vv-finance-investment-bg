package com.vv.finance.investment.bg.dto.f10;



import com.vv.finance.common.bean.FileBean;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author hamilton
 * @date 2021/8/19 12:03
 */
@Data
@Builder
public class Prospectus implements Serializable {
    private static final long serialVersionUID = -2314021288986967624L;
    @ApiModelProperty("说明书列表")
    private List<FileBean> prospectusList;
    @ApiModelProperty("默认显示")
    private String desc="招股说明书";
}
