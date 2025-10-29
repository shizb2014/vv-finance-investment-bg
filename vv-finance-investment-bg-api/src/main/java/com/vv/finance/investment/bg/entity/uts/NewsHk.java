package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author lh.sz
 * @since 2021-09-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("news_hk")
public class NewsHk extends Model {

    private static final long serialVersionUID = 1L;

    private Long newsid;

    private LocalDate date;

    private String time;

    private String newstitle;

    private String keyword;

    private String author;

    private String source;

    private String content;

    private String newstype;

    private String relatesymbol;

    private String market;

    private String imageUrl;

    private Long xdbmask;

    private String newstype2;


}
