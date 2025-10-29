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
@ApiModel(value="News24hours对象", description="")
@TableName("news_24hours")
public class News24hours implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private LocalDate date;

    private String time;

    private String level1;

    private String level2;

    private String content;

    private Long xdbmask;


}
