package com.vv.finance.investment.bg.dto.kline;

import com.baomidou.mybatisplus.annotation.TableField;
import com.vv.finance.investment.bg.dto.info.DealDTO;
import com.vv.finance.investment.bg.dto.info.EventDTO;
import com.vv.finance.common.calc.hk.entity.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author chenyu
 * @date 2021/3/16 10:32
 */
@Data
public class BaseKlineDTO implements Serializable {

    private static final long serialVersionUID = 7767046104726150983L;
    @ApiModelProperty("股票ID")
    private Long stockId;
    @ApiModelProperty("股票代码")
    private String code;
    @ApiModelProperty("时间戳")
    private Long time;
    @ApiModelProperty("字符串时间")
    private String timeStr;
    @ApiModelProperty("开盘价")
    private BigDecimal open;
    @ApiModelProperty("最高价")
    private BigDecimal high;
    @ApiModelProperty("最低价")
    private BigDecimal low;
    @ApiModelProperty("收盘价")
    private BigDecimal close;

    /**
     * 在分k 和 日周年月中的定义是否一致
     */
    @ApiModelProperty("昨日收盘价")
    private BigDecimal preClose;

    @ApiModelProperty("成交量")
    private BigDecimal volume;
    @ApiModelProperty("涨跌额")
    private BigDecimal chg;
    @ApiModelProperty("涨跌幅")
    private BigDecimal chgPct;
    @ApiModelProperty("成交额")
    private BigDecimal amount;
    @ApiModelProperty("平均价")
    @TableField(exist = false)
    private BigDecimal vwap;
    @ApiModelProperty("分时涨跌额")
    @TableField(exist = false)
    private BigDecimal tsChg;
    @ApiModelProperty("分时涨跌幅")
    @TableField(exist = false)
    private BigDecimal tsChgPct;
    @ApiModelProperty("5日分时涨跌额")
    @TableField(exist = false)
    private BigDecimal ts5Chg;
    @ApiModelProperty("5日分时涨跌幅")
    @TableField(exist = false)
    private BigDecimal ts5ChgPct;
    @ApiModelProperty(value = "换手率")
    private String changeRate;

    /**
     * forward：前复权；backward：后复权; null 不复权
     * split-adjusted share prices
     */
    @ApiModelProperty(value = "复权方式")
    private String adjhkt;

    @ApiModelProperty(value = "事件字段")
    private List<EventDTO> event;

    @ApiModelProperty(value = "买卖点")
    private List<DealDTO> dealList;

    private MACDEntity macdEntity;
    private KDJEntity kdjEntity;
    private WREntity wrEntity;
    private SAREntity sarEntity;
    private RSIEntity rsiEntity;
    private OBVEntity obvEntity;
    private MAEntity maEntity;
    private EMAEntity emaEntity;
    private DMIEntity dmiEntity;
    private BOLLEntity bollEntity;
    private MAVOLEntity mavolEntity;
    private DMAEntity dmaEntity;
    private CCIEntity cciEntity;
    private VROCEntity vrocEntity;
    private ROCEntity rocEntity;
    private BIASEntity biasEntity;
    private PSYEntity psyEntity;
    private CDPEntity cdpEntity;
    private OSCEntity oscEntity;
    private VREntity vrEntity;
    private ARBREntity arbrEntity;


    /**
     * 监控耗时时间字段
     */
    private String monitorTime;
}
