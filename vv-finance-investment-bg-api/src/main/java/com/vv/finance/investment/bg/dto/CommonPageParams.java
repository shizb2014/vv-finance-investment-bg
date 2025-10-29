package com.vv.finance.investment.bg.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Description 列表分页公共参数
 * @Author liuxing
 * @Date 2021/11/24 13:51
 * @Version v1.0
 */
@Data
public class CommonPageParams implements Serializable {

    private static final long serialVersionUID = -6041511459226544831L;
    private static final String SORT_ASC = "asc";

    @ApiModelProperty(value = "当前页数", required = true)
    @NotNull
    private Long currentPage;

    @ApiModelProperty(value = "每页条数", required = true)
    @NotNull
    private Long pageSize;
    @ApiModelProperty(value = "sort(desc-降序 asc-升序)")
    private String sort;

    @ApiModelProperty(value = "sortKey(对象对应key)")
    private String sortKey;


    public boolean isAsc() {
        return SORT_ASC.equalsIgnoreCase(this.sort);
    }

}
