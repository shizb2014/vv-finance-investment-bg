package com.vv.finance.investment.bg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @authoer:Gongmc
 * @Date:2023/3/12
 * @description:
 */
@AllArgsConstructor
@Getter
public enum NoticeJobTypeEnum {
    save(0, "公告同步job"),
    repair(1, "公告修复job"),
    ;

    private Integer jobType;
    private String desc;
}
