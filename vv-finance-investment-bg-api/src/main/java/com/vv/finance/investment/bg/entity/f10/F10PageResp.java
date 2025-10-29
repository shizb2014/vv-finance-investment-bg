package com.vv.finance.investment.bg.entity.f10;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hamilton
 * @date 2021/7/23 15:23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class F10PageResp<T> {
    /**
     * 頁碼
     */
    private int currentPage;
    /**
     * 每頁長度
     */
    private int pageSize;
    /**
     * 总条数
     */
    private long total;

    private List<T> record;


}
