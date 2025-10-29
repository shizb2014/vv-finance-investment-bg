package com.vv.finance.investment.bg.stock.information;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName HkOrAmericanStockVO
 * @Deacription 港股/美股 股票市场
 * @Author lh.sz
 * @Date 2021年09月13日 11:23
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@Builder
public class HkOrAmericanInformation extends BaseInformation implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

//    @ApiModelProperty(value = "图片地址")
//    private String imageUrl;
}
