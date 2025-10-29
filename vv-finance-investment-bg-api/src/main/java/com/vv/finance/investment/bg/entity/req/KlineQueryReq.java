package com.vv.finance.investment.bg.entity.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author hamilton
 * @date 2020/10/29 17:16
 */
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ApiModel("k线")
public class KlineQueryReq implements Serializable {

    private static final long serialVersionUID = 8206130750961836824L;
    @NotNull
    @ApiModelProperty(value = "代码", required = true)
    private String code;
    @NotNull
    @ApiModelProperty(value = "k线类型  day | week | month | year | 1min | 5min | 15min | 30min | 60min | 120min", required = true)
    private String type;

    @ApiModelProperty(value = "数量，必须为大于0的数字",required = true)
    private Integer num;

    @NotNull
    @ApiModelProperty(value = "从哪天往后，默认今天，用 以向后去增量")
    private Long date;

    @ApiModelProperty(value = "forward：前复权；backward：后复权，为空 不复权")
    private String adjhkt;


    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public Integer getNum() {
        return num==null?1:num;
    }

    public Long getDate() {
        return date;
    }

    public String getAdjhkt() {
        return adjhkt;
    }
}
