package com.vv.finance.investment.bg.dto.warrant;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName WarrantCodeVo
 * @Deacription 权证code
 * @Author lh.sz
 * @Date 2021年12月21日 14:24
 **/
@Data
@ToString
public class WarrantCodeVo implements Serializable {

    private static final long serialVersionUID = -1;
    /**
     * 证券代码
     */
    @ApiModelProperty(value = "证券代码")
    private String warrantCode;

    /**
     * 证券名称
     */
    @ApiModelProperty(value = "证券名称")
    private String warrantName;
}
