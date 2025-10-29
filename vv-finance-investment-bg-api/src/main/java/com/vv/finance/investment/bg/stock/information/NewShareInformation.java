package com.vv.finance.investment.bg.stock.information;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName NewShareVO
 * @Deacription 资讯新股
 * @Author lh.sz
 * @Date 2021年09月13日 11:15
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@Builder
public class NewShareInformation extends BaseInformation implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;

    @ApiModelProperty(value = "图片地址")
    private String imageUrl;
}
