package com.vv.finance.investment.bg.entity.information;

import lombok.Data;

import java.io.Serializable;

/**
 * 资讯标签实体
 * @author wsliang
 * @date 2021/11/3 17:17
 **/
@Data
public class TagDto implements Serializable {
    private static final long serialVersionUID = 2236511021458363015L;

    /**
     * code
     */
    private String code;

    /**
     * 标签文本
     */
    private String tagValue;

    /**
     * 标签分类
     * 0:自选; 1:异动; 2:新股; 3:港股; 4:美股;5:个股资讯; 6:日历; 7:7*24;
     */
    private Integer category;
}
