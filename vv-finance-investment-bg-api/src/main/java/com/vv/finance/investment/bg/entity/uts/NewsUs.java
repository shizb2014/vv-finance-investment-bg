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
@ApiModel(value="NewsUs对象", description="")
@TableName("news_us")
public class NewsUs implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer newsid;

    private LocalDate date;

    private String time;

    private String newstitle;

    private String keyword;

    private String author;

    private String source;

    private String content;

    private String newstype;

    private String newstype2;

    private String relatesymbol;

    private String market;

    private String imageUrl;

    private Long xdbmask;


}
