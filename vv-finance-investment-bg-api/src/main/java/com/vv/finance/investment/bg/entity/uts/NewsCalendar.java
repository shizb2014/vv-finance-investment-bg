package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 
 * </p>
 *
 * @author hamilton
 * @since 2021-09-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="NewsCalendar对象", description="")
@TableName("news_calendar")
public class NewsCalendar implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private LocalDate date;

    private String time;

    private String type;

    private String content;

    private Long xdbmask;


}
