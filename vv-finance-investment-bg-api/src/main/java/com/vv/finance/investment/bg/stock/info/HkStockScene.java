package com.vv.finance.investment.bg.stock.info;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vv.finance.investment.bg.stock.quotes.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName HkStockScene
 * @Deacription 港股股票特殊场景表
 * @Author lh.sz
 * @Date 2020年11月13日 15:54
 **/
@Data
@TableName("t_hk_stock_scene")
public class HkStockScene extends BaseEntity {

    /**
     * 证券代码
     */
    @TableField(value = "code")
    @ApiModelProperty(value = "证券代码")
    private String code;

    /**
     * 业务类型：0-正常，1-临时股票，2-代码复用，3-转板
     */
    @TableField(value = "scene_type")
    @ApiModelProperty(value = "业务类型：0-正常，1-临时股票，2-代码复用，3-转板")
    private Integer sceneType;

    /**
     * 可能是以下几种场景产生的代码：
     * 0、新股代码 1、临时股票代码 2、代码复用场景重新上市代码 3、港股转板之后的代码
     */
    @TableField(value = "scene_code")
    @ApiModelProperty(value = "场景代码")
    private String sceneCode;

    /**
     * 可能是以下几种场景产生的日期：
     * 0、新股上市日期 1、并行交易开始日期 2、代码复用开始日期 3、港股转板日期 格式：yyyyMMdd
     */
    @TableField(value = "scene_start_date")
    @ApiModelProperty(value = "场景开始日期")
    private Long sceneStartDate;

    /**
     * 可能是以下几种场景产生的日期：
     * 并行交易完结日期(临时股票场景该字段有值) 格式：yyyyMMdd
     */
    @TableField(value = "scene_end_date")
    @ApiModelProperty(value = "场景结束日期")
    private Long sceneEndDate;

    /**
     * 并行交易暂停买卖期(临时股票场景该字段有值) 格式：yyyy/MM/dd - yyyy/MM/dd
     */
    @TableField(value = "suspend_trading_time")
    @ApiModelProperty(value = "并行交易暂停买卖期")
    private String suspendTradingTime;

    /**
     * 并行交易买卖完结期(临时股票场景该字段有值) 格式：yyyy/MM/dd - yyyy/MM/dd
     */
    @TableField(value = "closed_trading_time")
    @ApiModelProperty(value = "并行交易买卖完结期")
    private String closedTradingTime;

    /**
     * 备注
     */
    @TableField(value = "remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public static final String COL_CODE = "code";

    public static final String SCENE_TYPE = "scene_type";

    public static final String SCENE_CODE = "scene_code";

    public static final String SCENE_START_DATE = "scene_start_date";

    public static final String SCENE_END_DATE = "scene_end_date";


}
