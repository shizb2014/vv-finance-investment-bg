package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author chenyu
 * @since 2021-07-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="HkIisNewsCateRef对象", description="")
public class HkIisNewsCateRefBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("LINE_CATE_SEQ")
    private Long lineCateSeq;

    @TableField("LINE_ID")
    private String lineId;

    @TableField("CATEGORY_ID")
    private String categoryId;

    @TableField("ENTRY_TIME")
    private LocalDateTime entryTime;

    @TableField("XDBMASK")
    private Long xdbmask;


    public static final String LINE_CATE_SEQ = "LINE_CATE_SEQ";

    public static final String LINE_ID = "LINE_ID";

    public static final String CATEGORY_ID = "CATEGORY_ID";

    public static final String ENTRY_TIME = "ENTRY_TIME";

    public static final String XDBMASK = "XDBMASK";

}
