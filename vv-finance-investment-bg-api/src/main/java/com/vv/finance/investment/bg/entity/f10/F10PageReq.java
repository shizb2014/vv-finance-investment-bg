package com.vv.finance.investment.bg.entity.f10;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hamilton
 * @date 2021/7/23 15:21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class F10PageReq <T>{
    /**
     * 頁碼
     */
    private int currentPage;
    /**
     * 每頁長度
     */
    private int pageSize;

    /**
     * 排序字段
     */
    private String sortKey;
    /**
     * 是否倒序
     */
    private Boolean desc;

    /**
     * 條件
     */
    private T params;
}
