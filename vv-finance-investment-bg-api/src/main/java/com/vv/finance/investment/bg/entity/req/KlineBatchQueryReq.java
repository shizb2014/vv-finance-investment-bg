package com.vv.finance.investment.bg.entity.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author chenyu
 * @date 2021/3/3 16:02
 */
@Builder
@Data
public class KlineBatchQueryReq implements Serializable {
    private static final long serialVersionUID = 820613075096183L;
    @NotNull
    @ApiModelProperty(value = "代码", required = true)
    private List<String> codes;

    @NotNull
    @ApiModelProperty(value = "k线类型  day | week | month | year | 1 | 5 | 15 | 30 | 60 | 120", required = true)
    private String type;

    @ApiModelProperty(value = "数量，必须为大于0的数字")
    private Integer number;

    @NotNull
    @ApiModelProperty(value = "从哪天往后，默认今天，用 以向后去增量")
    private Long time;

    @ApiModelProperty(value = "forward：前复权；backward：后复权，为空 不复权")
    private String adjhkt;

}
