package com.vv.finance.investment.bg.dto.notice;

import lombok.Data;

/**
 * @authoer:Gongmc
 * @Date:2023/3/11
 * @description: 公告修复传参
 *
 */
@Data
public class NoticeRepair {
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
}
