package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
*   @ClassName:    Xnhk0102
*   @Description:
*   @Author:   Demon
*   @Datetime:    2020/11/10   17:50
*/
@Data
@TableName(value = "XNHK0102")
public class Xnhk0102 implements Serializable {
    @TableField(value = "seccode")
    private String seccode;

    /**
     * N：交易中，S：停牌
     */
    @TableField(value = "F001V")
    private String f001v;

    @TableField(value = "F002V")
    private String f002v;

    @TableField(value = "F003V")
    private String f003v;

    @TableField(value = "F004D")
    private Long f004d;

    @TableField(value = "F005V")
    private String f005v;

    @TableField(value = "F006V")
    private String f006v;

    @TableField(value = "F007N")
    private BigDecimal f007n;

    @TableField(value = "F008V")
    private String f008v;

    @TableField(value = "F009N")
    private BigDecimal f009n;

    @TableField(value = "F010N")
    private BigDecimal f010n;

    @TableField(value = "F011D")
    private Long f011d;

    @TableField(value = "F012D")
    private Long f012d;

    @TableField(value = "F013D")
    private Long f013d;

    @TableField(value = "F014D")
    private Long f014d;

    @TableField(value = "F015D")
    private Long f015d;

    @TableField(value = "F016D")
    private Long f016d;

    @TableField(value = "F017D")
    private Long f017d;

    @TableField(value = "F018V")
    private String f018v;

    @TableField(value = "F019V")
    private String f019v;

    @TableField(value = "F020N")
    private BigDecimal f020n;

    @TableField(value = "F021N")
    private BigDecimal f021n;

    @TableField(value = "F022D")
    private Long f022d;

    @TableField(value = "F023N")
    private BigDecimal f023n;

    @TableField(value = "F024N")
    private BigDecimal f024n;

    @TableField(value = "F025N")
    private BigDecimal f025n;

    @TableField(value = "F026N")
    private BigDecimal f026n;

    @TableField(value = "F027N")
    private BigDecimal f027n;

    @TableField(value = "F028N")
    private BigDecimal f028n;

    @TableField(value = "F029N")
    private BigDecimal f029n;

    @TableField(value = "F030N")
    private BigDecimal f030n;

    @TableField(value = "F031N")
    private BigDecimal f031n;

    @TableField(value = "F032N")
    private BigDecimal f032n;

    @TableField(value = "F033N")
    private BigDecimal f033n;

    @TableField(value = "F034N")
    private BigDecimal f034n;

    @TableField(value = "F035N")
    private BigDecimal f035n;

    @TableField(value = "F036N")
    private BigDecimal f036n;

    @TableField(value = "F037N")
    private BigDecimal f037n;

    @TableField(value = "F038N")
    private BigDecimal f038n;

    @TableField(value = "F039N")
    private BigDecimal f039n;

    @TableField(value = "F040N")
    private BigDecimal f040n;

    @TableField(value = "F041N")
    private BigDecimal f041n;

    @TableField(value = "F042N")
    private BigDecimal f042n;

    @TableField(value = "F043N")
    private BigDecimal f043n;

    @TableField(value = "F044N")
    private BigDecimal f044n;

    @TableField(value = "F045N")
    private BigDecimal f045n;

    @TableField(value = "F046N")
    private BigDecimal f046n;

    @TableField(value = "F047N")
    private BigDecimal f047n;

    @TableField(value = "F048N")
    private BigDecimal f048n;

    @TableField(value = "F049N")
    private Long f049n;

    @TableField(value = "F050N")
    private BigDecimal f050n;

    @TableField(value = "F051N")
    private BigDecimal f051n;

    @TableField(value = "F052N")
    private BigDecimal f052n;

    @TableField(value = "F053N")
    private BigDecimal f053n;

    @TableField(value = "F054N")
    private BigDecimal f054n;

    @TableField(value = "F055N")
    private BigDecimal f055n;

    @TableField(value = "F056N")
    private BigDecimal f056n;

    @TableField(value = "F057N")
    private BigDecimal f057n;

    @TableField(value = "F058N")
    private BigDecimal f058n;

    @TableField(value = "F059N")
    private BigDecimal f059n;

    @TableField(value = "F060N")
    private BigDecimal f060n;

    @TableField(value = "F061N")
    private BigDecimal f061n;

    @TableField(value = "F062N")
    private BigDecimal f062n;

    @TableField(value = "F063N")
    private BigDecimal f063n;

    @TableField(value = "F064N")
    private BigDecimal f064n;

    @TableField(value = "F065N")
    private BigDecimal f065n;

    @TableField(value = "F066N")
    private BigDecimal f066n;

    @TableField(value = "F067N")
    private BigDecimal f067n;

    @TableField(value = "F068N")
    private BigDecimal f068n;

    /**
     * 流通股本
     */
    @TableField(value = "F069N")
    private BigDecimal f069n;

    /**
     * 总股本
     */
    @TableField(value = "F070N")
    private BigDecimal f070n;

    @TableField(value = "F071N")
    private Long f071n;

    @TableField(value = "F072D")
    private Long f072d;

    @TableField(value = "F073V")
    private String f073v;

    @TableField(value = "F074V")
    private String f074v;

    /**
     * 总市值
     */
    @TableField(value = "F075N")
    private BigDecimal f075n;

    @TableField(value = "F076V")
    private String f076v;

    @TableField(value = "F077N")
    private BigDecimal f077n;

    @TableField(value = "F078N")
    private BigDecimal f078n;

    @TableField(value = "F079N")
    private BigDecimal f079n;

    @TableField(value = "F080N")
    private BigDecimal f080n;

    @TableField(value = "F081N")
    private BigDecimal f081n;

    @TableField(value = "F082N")
    private BigDecimal f082n;

    @TableField(value = "F083N")
    private BigDecimal f083n;

    /**
     * 市盈率（TTM）
     */
    @TableField(value = "F084N")
    private BigDecimal f084n;

    /**
     * 市盈率（静态）
     */
    @TableField(value = "F085N")
    private BigDecimal f085n;

    @TableField(value = "F086N")
    private BigDecimal f086n;

    @TableField(value = "F087N")
    private BigDecimal f087n;

    /**
     * 市净率
     */
    @TableField(value = "F088N")
    private BigDecimal f088n;

    @TableField(value = "F089N")
    private BigDecimal f089n;

    @TableField(value = "F090N")
    private BigDecimal f090n;

    @TableField(value = "F091N")
    private BigDecimal f091n;

    @TableField(value = "F092N")
    private BigDecimal f092n;

    @TableField(value = "F093N")
    private BigDecimal f093n;

    @TableField(value = "F094N")
    private BigDecimal f094n;

    @TableField(value = "F095N")
    private BigDecimal f095n;

    @TableField(value = "F096N")
    private BigDecimal f096n;

    @TableField(value = "F097N")
    private BigDecimal f097n;

    @TableField(value = "F098N")
    private BigDecimal f098n;

    @TableField(value = "F099N")
    private BigDecimal f099n;

    @TableField(value = "Create_Date")
    private Date createDate;

    @TableField(value = "Modified_Date")
    private Date modifiedDate;

    @TableField(value = "XDBMASK")
    private Long xdbmask;

    /**
     * 历史最低
     */
    @TableField(exist = false)
    private BigDecimal hisLowestPrice;

    /**
     * 历史最高
     */
    @TableField(exist = false)
    private BigDecimal hisHighestPrice;

    private static final long serialVersionUID = 1L;

    public static final String COL_SECCODE = "seccode";

    public static final String COL_F001V = "F001V";

    public static final String COL_F002V = "F002V";

    public static final String COL_F003V = "F003V";

    public static final String COL_F004D = "F004D";

    public static final String COL_F005V = "F005V";

    public static final String COL_F006V = "F006V";

    public static final String COL_F007N = "F007N";

    public static final String COL_F008V = "F008V";

    public static final String COL_F009N = "F009N";

    public static final String COL_F010N = "F010N";

    public static final String COL_F011D = "F011D";

    public static final String COL_F012D = "F012D";

    public static final String COL_F013D = "F013D";

    public static final String COL_F014D = "F014D";

    public static final String COL_F015D = "F015D";

    public static final String COL_F016D = "F016D";

    public static final String COL_F017D = "F017D";

    public static final String COL_F018V = "F018V";

    public static final String COL_F019V = "F019V";

    public static final String COL_F020N = "F020N";

    public static final String COL_F021N = "F021N";

    public static final String COL_F022D = "F022D";

    public static final String COL_F023N = "F023N";

    public static final String COL_F024N = "F024N";

    public static final String COL_F025N = "F025N";

    public static final String COL_F026N = "F026N";

    public static final String COL_F027N = "F027N";

    public static final String COL_F028N = "F028N";

    public static final String COL_F029N = "F029N";

    public static final String COL_F030N = "F030N";

    public static final String COL_F031N = "F031N";

    public static final String COL_F032N = "F032N";

    public static final String COL_F033N = "F033N";

    public static final String COL_F034N = "F034N";

    public static final String COL_F035N = "F035N";

    public static final String COL_F036N = "F036N";

    public static final String COL_F037N = "F037N";

    public static final String COL_F038N = "F038N";

    public static final String COL_F039N = "F039N";

    public static final String COL_F040N = "F040N";

    public static final String COL_F041N = "F041N";

    public static final String COL_F042N = "F042N";

    public static final String COL_F043N = "F043N";

    public static final String COL_F044N = "F044N";

    public static final String COL_F045N = "F045N";

    public static final String COL_F046N = "F046N";

    public static final String COL_F047N = "F047N";

    public static final String COL_F048N = "F048N";

    public static final String COL_F049N = "F049N";

    public static final String COL_F050N = "F050N";

    public static final String COL_F051N = "F051N";

    public static final String COL_F052N = "F052N";

    public static final String COL_F053N = "F053N";

    public static final String COL_F054N = "F054N";

    public static final String COL_F055N = "F055N";

    public static final String COL_F056N = "F056N";

    public static final String COL_F057N = "F057N";

    public static final String COL_F058N = "F058N";

    public static final String COL_F059N = "F059N";

    public static final String COL_F060N = "F060N";

    public static final String COL_F061N = "F061N";

    public static final String COL_F062N = "F062N";

    public static final String COL_F063N = "F063N";

    public static final String COL_F064N = "F064N";

    public static final String COL_F065N = "F065N";

    public static final String COL_F066N = "F066N";

    public static final String COL_F067N = "F067N";

    public static final String COL_F068N = "F068N";

    public static final String COL_F069N = "F069N";

    public static final String COL_F070N = "F070N";

    public static final String COL_F071N = "F071N";

    public static final String COL_F072D = "F072D";

    public static final String COL_F073V = "F073V";

    public static final String COL_F074V = "F074V";

    public static final String COL_F075N = "F075N";

    public static final String COL_F076V = "F076V";

    public static final String COL_F077N = "F077N";

    public static final String COL_F078N = "F078N";

    public static final String COL_F079N = "F079N";

    public static final String COL_F080N = "F080N";

    public static final String COL_F081N = "F081N";

    public static final String COL_F082N = "F082N";

    public static final String COL_F083N = "F083N";

    public static final String COL_F084N = "F084N";

    public static final String COL_F085N = "F085N";

    public static final String COL_F086N = "F086N";

    public static final String COL_F087N = "F087N";

    public static final String COL_F088N = "F088N";

    public static final String COL_F089N = "F089N";

    public static final String COL_F090N = "F090N";

    public static final String COL_F091N = "F091N";

    public static final String COL_F092N = "F092N";

    public static final String COL_F093N = "F093N";

    public static final String COL_F094N = "F094N";

    public static final String COL_F095N = "F095N";

    public static final String COL_F096N = "F096N";

    public static final String COL_F097N = "F097N";

    public static final String COL_F098N = "F098N";

    public static final String COL_F099N = "F099N";

    public static final String COL_CREATE_DATE = "Create_Date";

    public static final String COL_MODIFIED_DATE = "Modified_Date";

    public static final String COL_XDBMASK = "XDBMASK";
}