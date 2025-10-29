package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 * @Auto: chenzhenlong
 * @Date: 2021/8/16 11:34
 * @Version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("Xnhk0307")
public class Xnhk0307 implements Serializable {

    private static final long serialVersionUID = 7384511029925466617L;

    @TableId("SECCODE")
    @Column(name = "SECCODE")
    private String seccode;

    @TableField("F001D")
    @Column(name = "F001D")
    private Long f001d;

    @TableField("F002V")
    @Column(name = "F002V")
    private String f002v;

    @TableField("F003D")
    @Column(name = "F003D")
    private Long f003d;

    @TableField("F004V")
    @Column(name = "F004V")
    private String f004v;

    @TableField("F005D")
    @Column(name = "F005D")
    private Long f005d;
}
