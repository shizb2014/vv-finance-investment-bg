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
@ApiModel(value="HkIisNewsSecurityRef对象", description="")
public class HkIisNewsSecurityRefBase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("SEC_LINE_SEQ")
    private Long secLineSeq;

    @TableField("SEC_CODE")
    private Integer secCode;

    @TableField("SEC_NAME")
    private String secName;

    @TableField("LINE_ID")
    private String lineId;

    @TableField("ENTRY_TIME")
    private LocalDateTime entryTime;

    @TableField("XDBMASK")
    private Long xdbmask;


    public static final String SEC_LINE_SEQ = "SEC_LINE_SEQ";

    public static final String SEC_CODE = "SEC_CODE";

    public static final String SEC_NAME = "SEC_NAME";

    public static final String LINE_ID = "LINE_ID";

    public static final String ENTRY_TIME = "ENTRY_TIME";

    public static final String XDBMASK = "XDBMASK";

}
