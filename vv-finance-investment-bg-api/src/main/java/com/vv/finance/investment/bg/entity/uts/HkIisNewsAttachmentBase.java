package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@ApiModel(value="HkIisNewsAttachment对象", description="")
public class HkIisNewsAttachmentBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("ATTACHMENT_ID")
    private String attachmentId;

    @TableField("LINE_ID")
    private String lineId;

    @TableField("MD5")
    private String md5;

    @TableField("DESCRIPTION")
    private String description;

    @TableField("MIME")
    private String mime;

    @TableField("ATTCH_SIZE")
    private Long attchSize;

    @TableField("PATH")
    private String path;

    @TableField("ENTRY_TIME")
    private LocalDateTime entryTime;

    @TableField("DOWNLOAD_PATH")
    private String downloadPath;

    @TableField("DOWNLOAD_FLAG")
    private String downloadFlag;

    @TableField("XDBMASK")
    private Long xdbmask;


    public static final String ATTACHMENT_ID = "ATTACHMENT_ID";

    public static final String LINE_ID = "LINE_ID";

    public static final String MD5 = "MD5";

    public static final String DESCRIPTION = "DESCRIPTION";

    public static final String MIME = "MIME";

    public static final String ATTCH_SIZE = "ATTCH_SIZE";

    public static final String PATH = "PATH";

    public static final String ENTRY_TIME = "ENTRY_TIME";

    public static final String DOWNLOAD_PATH = "DOWNLOAD_PATH";

    public static final String DOWNLOAD_FLAG = "DOWNLOAD_FLAG";

    public static final String XDBMASK = "XDBMASK";

}
