package com.vv.finance.investment.bg.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户行情自定义周期设定表
 * @TableName t_user_quotation_cycle_config
 */
@TableName(value ="t_user_quotation_cycle_config")
@Data
public class UserQuotationCycleConfig implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 时间范围
     */
    private Integer num;

    /**
     * 时间单位（year, month, day）
     */
    private String unit;

    /**
     * 周期(day week...)
     */
    private String cycle;

    /**
     * 创建人
     */
    private String userName;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 是否删除（1：删除 0：未删除）
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 区域类型(0:港股  1:美股)
     */
    private Integer regionType;
}