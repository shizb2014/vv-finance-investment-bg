package com.vv.finance.investment.bg.entity.f10;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName ReportTypeEntity
 * @Deacription 报告类型
 * @Author lh.sz
 * @Date 2021年07月22日 18:58
 **/
@Data
@ToString
@Builder
public class ReportTypeEntity implements Serializable {


    private static final long serialVersionUID = -1682670390094234663L;

    @ApiModelProperty(value = "报告类型 F:年报 I：中报 Q：季报")
    private String reportType;
    @ApiModelProperty(value = "时间")
    private Long time;

}
