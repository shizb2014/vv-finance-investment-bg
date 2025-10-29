package com.vv.finance.investment.bg.entity.information.app;

import com.vv.finance.investment.bg.entity.information.StockNewsVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@ApiModel("个股资讯详情-app")
public class StockNewsDetailVoApp extends StockNewsVo {
    private static final long serialVersionUID = -7836301330386356632L;

    @ApiModelProperty("资讯详情文本")
    private String content;

    @ApiModelProperty("资讯详情文本/翻译后")
    private String contentTranslated;

    @ApiModelProperty("股票信息")
    private List<SimpleStockVoApp> simpleStockVos;
}
