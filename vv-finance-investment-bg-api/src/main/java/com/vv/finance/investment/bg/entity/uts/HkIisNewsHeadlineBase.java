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
@ApiModel(value="HkIisNewsHeadline对象", description="")
public class HkIisNewsHeadlineBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("LINE_ID")
    private String lineId;

    @TableField("NEWS_SUBTYPE_ID")
    private Integer newsSubtypeId;

    @TableField("SEQ_NO")
    private String seqNo;

    @TableField("PROVIDER_ID")
    private String providerId;

    @TableField("DATE_ID")
    private String dateId;

    @TableField("NEWS_ITEM_ID")
    private String newsItemId;

    @TableField("LANGUAGE_ID")
    private Integer languageId;

    @TableField("EXPIRY_DATE")
    private LocalDateTime expiryDate;

    @TableField("DATE_LINE")
    private String dateLine;

    @TableField("HEADLINE")
    private String headline;

    @TableField("ATTACHMENT_NUM")
    private Integer attachmentNum;

    @TableField("EXCHNG_ID")
    private Integer exchngId;

    @TableField("MARKET_CODE")
    private String marketCode;

    @TableField("ENTRY_TIME")
    private LocalDateTime entryTime;

    @TableField("XDBMASK")
    private Long xdbmask;


    public static final String LINE_ID = "LINE_ID";

    public static final String NEWS_SUBTYPE_ID = "NEWS_SUBTYPE_ID";

    public static final String SEQ_NO = "SEQ_NO";

    public static final String PROVIDER_ID = "PROVIDER_ID";

    public static final String DATE_ID = "DATE_ID";

    public static final String NEWS_ITEM_ID = "NEWS_ITEM_ID";

    public static final String LANGUAGE_ID = "LANGUAGE_ID";

    public static final String EXPIRY_DATE = "EXPIRY_DATE";

    public static final String DATE_LINE = "DATE_LINE";

    public static final String HEADLINE = "HEADLINE";

    public static final String ATTACHMENT_NUM = "ATTACHMENT_NUM";

    public static final String EXCHNG_ID = "EXCHNG_ID";

    public static final String MARKET_CODE = "MARKET_CODE";

    public static final String ENTRY_TIME = "ENTRY_TIME";

    public static final String XDBMASK = "XDBMASK";

}
