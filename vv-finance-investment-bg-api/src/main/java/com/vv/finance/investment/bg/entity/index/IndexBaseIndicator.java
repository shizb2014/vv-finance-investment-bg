package com.vv.finance.investment.bg.entity.index;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName: IndexBaseIndicator
 * @Description:
 * @Author: Demon
 * @Datetime: 2020/10/28   17:58
 */
@Data
public class IndexBaseIndicator implements Serializable {
    private static final long serialVersionUID = -4187740381194740003L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 指数代码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 毫秒时间戳
     */
    @TableField(value = "date")
    private Date date;

    @TableField(value = "ema12")
    private BigDecimal ema12;

    @TableField(value = "ema26")
    private BigDecimal ema26;

    @TableField(value = "diff")
    private BigDecimal diff;

    @TableField(value = "dea")
    private BigDecimal dea;

    @TableField(value = "macd")
    private BigDecimal macd;

//    /**
//     * RSI指标
//     */
//    @TableField(value = "upAverage")
//    private BigDecimal upaverage;
//
//    /**
//     * RSI指标
//     */
//    @TableField(value = "downAverage")
//    private BigDecimal downaverage;

    @TableField(value = "rsi6_up_average")
    private BigDecimal rsi6UpAverage;

    /**
     * RSI指标
     */
    @TableField(value = "rsi6_down_average")
    private BigDecimal rsi6downAverage;

    @TableField(value = "rsi12_up_average")
    private BigDecimal rsi12UpAverage;

    /**
     * RSI指标
     */
    @TableField(value = "rsi12_down_average")
    private BigDecimal rsi12downAverage;

    /**
     * 6 日相对强弱指标
     */
    @TableField(value = "rsi6")
    private BigDecimal rsi6;

    /**
     * 12日相对强弱指标
     */
    @TableField(value = "rsi12")
    private BigDecimal rsi12;

    /**
     * 24日相对强弱指标
     */
    @TableField(value = "rsi24")
    private BigDecimal rsi24;

    @TableField(value = "k")
    private BigDecimal k;

    @TableField(value = "d")
    private BigDecimal d;

    @TableField(value = "j")
    private BigDecimal j;

    @TableField(value = "sar")
    private BigDecimal sar;

    private BigDecimal sarFacto;

    private BigDecimal sarUp;

    /**
     * boll上轨线(压力线）
     */
    @TableField(value = "upper")
    private BigDecimal boolUpper;

    /**
     * boll中轨线
     */
    @TableField(value = "mid")
    private BigDecimal mid;

    /**
     * boll下轨线
     */
    @TableField(value = "lower")
    private BigDecimal boolLower;

    /**
     * 上升动向均值(dmi)
     */
    @TableField(value = "dm_up_avg")
    private BigDecimal dmUpAvg;

    /**
     * 下跌动向(dmi)
     */
    @TableField(value = "dm_down")
    private BigDecimal dmDown;

    /**
     * (dmi)
     */
    @TableField(value = "dm_up")
    private BigDecimal dmUp;

    /**
     * 下跌动向敦治(dmi)
     */
    @TableField(value = "dm_down_avg")
    private BigDecimal dmDownAvg;

    /**
     * 真实波幅(dmi)
     */
    @TableField(value = "tr")
    private BigDecimal tr;

    /**
     * 真实波幅均值(dmi)
     */
    @TableField(value = "tr_avg")
    private BigDecimal trAvg;

    /**
     * 上升方向指标(dmi)
     */
    @TableField(value = "pdi")
    private BigDecimal pdi;

    /**
     * 下跌方向指标(dmi)
     */
    @TableField(value = "pdm")
    private BigDecimal pdm;

    /**
     * 动向值(dmi)
     */
    @TableField(value = "dx")
    private BigDecimal dx;

    /**
     * 平均趋向指数(dmi)
     */
    @TableField(value = "adx")
    private BigDecimal adx;

    /**
     * (dmi)
     */
    @TableField(value = "adxr")
    private BigDecimal adxr;

    @TableField(value = "obv")
    private BigDecimal obv;

    @TableField(value = "wr_14")
    private BigDecimal wr;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    public static final String COL_ID = "id";

    public static final String COL_CODE = "code";

    public static final String COL_DATE = "date";

    public static final String COL_EMA12 = "ema12";

    public static final String COL_EMA26 = "ema26";

    public static final String COL_DIFF = "diff";

    public static final String COL_DEA = "dea";

    public static final String COL_MACD = "macd";

    public static final String COL_UPAVERAGE = "upAverage";

    public static final String COL_DOWNAVERAGE = "downAverage";

    public static final String COL_RSI6 = "rsi6";

    public static final String COL_RSI12 = "rsi12";

    public static final String COL_RSI24 = "rsi24";

    public static final String COL_K = "k";

    public static final String COL_D = "d";

    public static final String COL_J = "j";

    public static final String COL_SAR = "sar";

    public static final String COL_UPPER = "upper";

    public static final String COL_MID = "mid";

    public static final String COL_LOWER = "lower";

    public static final String COL_DM_UP_AVG = "dm_up_avg";

    public static final String COL_DM_DOWN = "dm_down";

    public static final String COL_DM_UP = "dm_up";

    public static final String COL_DM_DOWN_AVG = "dm_down_avg";

    public static final String COL_TR = "tr";

    public static final String COL_TR_AVG = "tr_avg";

    public static final String COL_PDI = "pdi";

    public static final String COL_PDM = "pdm";

    public static final String COL_DX = "dx";

    public static final String COL_ADX = "adx";

    public static final String COL_ADXR = "adxr";

    public static final String COL_OBV = "obv";

    public static final String COL_WR_14 = "wr_14";

    public static final String COL_CREATE_TIME = "create_time";

    public static final String COL_UPDATE_TIME = "update_time";
}
