package com.vv.finance.investment.bg.dto.f10;

import com.vv.finance.common.bean.SimplePageResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author hamilton
 * @date 2021/8/17 18:40
 * 分红派息
 */
@Data
public class Dividend extends SimplePageResp<Dividend.DividendDetail> {
    private static final long serialVersionUID = 5443961329856656265L;
    @ApiModelProperty(value = "累计分红次数")
    private Long cumulativeDividendTimes;
    @ApiModelProperty(value = "累计分红金额")
    private BigDecimal cumulativeDividendAmount;



   @Data
   public static class  DividendDetail implements Serializable{
       private static final long serialVersionUID = 4232914968921714920L;
       @ApiModelProperty(value = "年度")
       private String year;
       @ApiModelProperty(value = "分配类型")
       private String assignmentType;
       @ApiModelProperty(value = "分红方案")
       private String dividendScheme;
       @ApiModelProperty(value = "公布日期")
       private String publicationDate;
       @ApiModelProperty(value = "除净日")
       private String exDate;
       @ApiModelProperty(value = "股权登记日")
       private String stockRightDate;
       @ApiModelProperty(value = "截止过户日")
       private String lastTransferDate;
       @ApiModelProperty(value = "派息日")
       private String dividendDay;
   }
}
