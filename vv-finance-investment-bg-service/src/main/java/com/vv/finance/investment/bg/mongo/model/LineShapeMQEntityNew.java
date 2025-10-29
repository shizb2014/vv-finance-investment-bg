package com.vv.finance.investment.bg.mongo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vv.finance.common.constants.lineshape.LineShapeTraceDurationEnum;
import com.vv.finance.common.constants.lineshape.LineShapeAdjhktEnum;
import com.vv.finance.common.constants.lineshape.LineShapeIndicatorEnum;
import com.vv.finance.common.constants.lineshape.LineShapeStockFormEnum;
import com.vv.finance.common.constants.lineshape.domain.StockKlineMin;
import com.vv.finance.common.constants.lineshape.domain.TriggerCount;
import com.vv.finance.common.calc.hk.entity.StockKline;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "line_shape_list")
@CompoundIndex(def = "{'calcTimeStamp': 1, 'stockKline.code': 1, 'durationEnum': 1}", unique = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineShapeMQEntityNew implements Serializable {

    private static final long serialVersionUID = -4136214088416853431L;

    @Indexed
    private String uniqueIndex;

    /**
     * 最新股票信息
     */
    private StockKlineMin stockKline;
    /**
     * 复权类型
     */
    private LineShapeAdjhktEnum adjhktEnum;
    /**
     * 周期类型
     */
    private LineShapeTraceDurationEnum durationEnum;
    /**
     * 满足条件的指标形态
     */
    private List<LineShapeIndicatorEnum> indicatorEnumList;
    /**
     * 满足条件的选股形态
     */
    private List<LineShapeStockFormEnum> stockFormEnumList;


    /**
     * 指标轨迹 带次数
     */
    @Builder.Default
    private Map<LineShapeIndicatorEnum, TriggerCount> indicatorTraceCountMap = new HashMap<>();

    /**
     * 形态轨迹 带次数
     */
    @Builder.Default
    private Map<LineShapeStockFormEnum, TriggerCount> stockFormTraceCountMap = new HashMap<>();


    /**
     * 当前计算时间戳
     */
    private Long calcTimeStamp;

    /**
     * 落库时间
     */
    private Long createTime;

    /**
     * 接收到数据的时间
     */
    private Long dataAcceptsTime;

}
