package com.vv.finance.investment.bg.stock.info;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.finance.investment.bg.stock.quotes.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName: TStockDefine
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/23   10:03
 */

/**
 * 股票码表
 *
 * @author hamilton
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "t_stock_define")
public class StockDefine extends BaseEntity {


    /**
     * 证券代码
     */
    @TableField(value = "code")
    @ApiModelProperty(value = "证券代码")
    private String code;

    /**
     * 证券名称（公司维护）
     */
    @TableField(value = "stock_name")
    @ApiModelProperty(value = "证券名称（公司维护）")
    private String stockName;

    /**
     * 证券名称
     */
    @TableField(value = "name")
    @ApiModelProperty(value = "证券名称")
    private String name;


    /**
     * 证券简称
     */
    @TableField(value = "shortname")
    @ApiModelProperty(value = "证券简称")
    private String shortname;

    /**
     * 行业代码
     */
    @ApiModelProperty(value = "行业代码")
    private String industryCode;

    /**
     * 市场代号（MAIN-主板，GEM-创业板，ETS-扩充交易证券，NASD-NASQAQ AMX市场）
     */
    @TableField(value = "marketCode")
    @ApiModelProperty(value = "市场代号（MAIN-主板，GEM-创业板，ETS-扩充交易证券，NASD-NASQAQ AMX市场）")
    private String marketcode;

    /**
     * 币种
     */
    @TableField(value = "currency")
    @ApiModelProperty(value = "币种")
    private String currency;

    /**
     * ISIN码
     */
    @TableField(value = "isincode")
    private String isincode;

    /**
     * 品种（BOND-债券，EQTY-股票，TRST-信托产品，WRNT-权证）
     */
    @TableField(value = "instrument")
    private String instrument;

    /**
     * 产品类型
     */
    @TableField(value = "producttype")
    private String producttype;

    /**
     * 股票类型: 1-正股;12-行业
     */
    @TableField(value = "stock_type")
    private Integer stockType;

    /**
     * 价差标识
     */
    @TableField(value = "spread")
    private String spread;

    /**
     * 每手股数
     */
    @TableField(value = "lotsize")
    private Integer lotsize;

    /**
     * 昨收价（元）
     */
    @TableField(value = "preclose")
    private BigDecimal preclose;

    /**
     * VCM标识（Y-是VCM范畴，N-非VCM范畴）
     */
    @TableField(value = "vcmflag")
    private String vcmflag;

    /**
     * 沽空标识（Y-可沽空，N-不可沽空）
     */
    @TableField(value = "shortsell")
    private String shortsell;

    /**
     * CAS标识（Y-是收盘竞价，N-非收盘竞价）
     */
    @TableField(value = "casflag")
    private String casflag;

    /**
     * CCASS标识（Y-是CCASS证券，N-非CCASS证券）
     */
    @TableField(value = "ccassflag")
    private String ccassflag;

    /**
     * 虚拟证券标识
     */
    @TableField(value = "dummyflag")
    private String dummyflag;

    /**
     * 测试标识
     */
    @TableField(value = "testflag")
    private String testflag;

    /**
     * 印花税标识
     */
    @TableField(value = "stampdutyflag")
    private String stampdutyflag;

    /**
     * 上市日期
     */
    @TableField(value = "listingdate")
    private String listingdate;

    /**
     * 退市日期
     */
    @TableField(value = "delistingdate")
    private String delistingdate;

    /**
     * 空闲文本
     */
    @TableField(value = "freetext")
    private String freetext;

    /**
     * POS标识
     */
    @TableField(value = "posflag")
    private String posflag;

    /**
     * POS最高限价
     */
    @TableField(value = "posupper")
    private BigDecimal posupper;

    /**
     * POS最低限价
     */
    @TableField(value = "poslower")
    private BigDecimal poslower;

    /**
     * EFN标识
     */
    @TableField(value = "enfflag")
    private String enfflag;

    /**
     * 利息
     */
    @TableField(value = "accured")
    private BigDecimal accured;

    /**
     * 息票利率
     */
    @TableField(value = "couponrate")
    private BigDecimal couponrate;

    /**
     * 兑换比率
     */
    @TableField(value = "counversionratio")
    private BigDecimal counversionratio;

    /**
     * 行使价1
     */
    @TableField(value = "strikeprice1")
    private BigDecimal strikeprice1;

    /**
     * 行使价2
     */
    @TableField(value = "strikeprice2")
    private BigDecimal strikeprice2;

    /**
     * 到期日
     */
    @TableField(value = "maturitydate")
    private String maturitydate;

    /**
     * 认购认沽标识
     */
    @TableField(value = "callput")
    private String callput;

    /**
     * 权证类型
     */
    @TableField(value = "style")
    private String style;

    /**
     * 涡轮类型
     */
    @TableField(value = "warrenttype")
    private String warrenttype;

    /**
     * 回收价
     */
    @TableField(value = "callprice")
    private BigDecimal callprice;

    /**
     * 回收价小数位
     */
    @TableField(value = "decimalprice")
    private BigDecimal decimalprice;

    /**
     * Entitlement
     */
    @TableField(value = "entitlement")
    private String entitlement;

    /**
     * DecimalsInEntitlem
     * ent
     */
    @TableField(value = "decimalentitlement")
    private String decimalentitlement;

    /**
     * NoWarrantsPerEnti
     * tlement
     */
    @TableField(value = "nowarrants")
    private String nowarrants;

    /**
     * NoUnderlyingSecur
     * ities
     */
    @TableField(value = "nounderlying")
    private String nounderlying;
    /**
     * 股票状态 0:正常 1:待上市 2:熔断 3:停牌 4:退市 {@link com.vv.finance.common.enums.StockSecurityStatusEnum}
     */
    @TableField(value = "suspension")
    private Integer suspension;

    /**
     * 更新用户ID 系统：0(融聚汇)
     */
    @TableField(value = "update_user_id")
    private Long updateUserId;

    /**
     * 股票名称stockName更新时间
     */
    @TableField(value = "update_stock_name_time")
    private LocalDateTime updateStockNameTime;

    /**
     * 双柜台股票关联代码
     */
    @TableField(value = "domain_code")
    @ApiModelProperty(value = "双柜台股票关联代码")
    private String domainCode;

    private static final long serialVersionUID = 1L;

    public static final String COL_PROTOCOL = "protocol";

    public static final String COL_CODE = "code";

    public static final String COL_NAME = "name";

    public static final String COL_SHORTNAME = "shortname";

    public static final String COL_MARKETCODE = "marketCode";

    public static final String COL_INDUSTRY_CODE = "industry_code";

    public static final String COL_CURRENCY = "currency";

    public static final String COL_ISINCODE = "isincode";

    public static final String COL_INSTRUMENT = "instrument";

    public static final String COL_PRODUCTTYPE = "producttype";

    public static final String COL_SPREAD = "spread";

    public static final String COL_LOTSIZE = "lotsize";

    public static final String COL_PRECLOSE = "preclose";

    public static final String COL_VCMFLAG = "vcmflag";

    public static final String COL_SHORTSELL = "shortsell";

    public static final String COL_CASFLAG = "casflag";

    public static final String COL_CCASSFLAG = "ccassflag";

    public static final String COL_DUMMYFLAG = "dummyflag";

    public static final String COL_TESTFLAG = "testflag";

    public static final String COL_STAMPDUTYFLAG = "stampdutyflag";

    public static final String COL_LISTINGDATE = "listingdate";

    public static final String COL_DELISTINGDATE = "delistingdate";

    public static final String COL_FREETEXT = "freetext";

    public static final String COL_POSFLAG = "posflag";

    public static final String COL_POSUPPER = "posupper";

    public static final String COL_POSLOWER = "poslower";

    public static final String COL_ENFFLAG = "enfflag";

    public static final String COL_ACCURED = "accured";

    public static final String COL_COUPONRATE = "couponrate";

    public static final String COL_COUNVERSIONRATIO = "counversionratio";

    public static final String COL_STRIKEPRICE1 = "strikeprice1";

    public static final String COL_STRIKEPRICE2 = "strikeprice2";

    public static final String COL_MATURITYDATE = "maturitydate";

    public static final String COL_CALLPUT = "callput";

    public static final String COL_STYLE = "style";

    public static final String COL_WARRENTTYPE = "warrenttype";

    public static final String COL_CALLPRICE = "callprice";

    public static final String COL_DECIMALPRICE = "decimalprice";

    public static final String COL_ENTITLEMENT = "entitlement";

    public static final String COL_DECIMALENTITLEMENT = "decimalentitlement";

    public static final String COL_NOWARRANTS = "nowarrants";

    public static final String COL_NOUNDERLYING = "nounderlying";

    public static final String COL_STOCK_NAME = "stock_name";

}