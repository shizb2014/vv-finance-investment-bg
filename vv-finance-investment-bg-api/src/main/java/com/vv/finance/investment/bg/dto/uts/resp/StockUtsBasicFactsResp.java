package com.vv.finance.investment.bg.dto.uts.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author chenyu
 * @date 2021/7/12 13:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockUtsBasicFactsResp implements Serializable {

    private static final long serialVersionUID = 6069765027390013612L;
    @ApiModelProperty("公司名称")
    private String name;

    @ApiModelProperty("英文名")
    private String englishName;

    @ApiModelProperty("所属市场")
    private String marketType;

    @ApiModelProperty("上市日期")
    private Long ipoTime;

    @ApiModelProperty("董事长")
    private String chairmanName;

    @ApiModelProperty("所属行业")
    private String industry;

    @ApiModelProperty("注册地址")
    private String registeredAddress;

    @ApiModelProperty("公司秘书")
    private String companySecretary;

    @ApiModelProperty("员工数量")
    private Long staffNum;

    @ApiModelProperty("审计机构")
    private String auditingBody;

    @ApiModelProperty("会计年结日")
    private Long accountingDate;

    @ApiModelProperty("会计年结日-新")
    private String accountingDateStr;

    @ApiModelProperty("办公地址")
    private String businessAddress;

    @ApiModelProperty("联系电话")
    private String phoneNum;

    @ApiModelProperty("联系传真")
    private String fox;

    @ApiModelProperty("公司邮箱")
    private String email;

    @ApiModelProperty("公司网址")
    private String website;

    @ApiModelProperty("主要业务")
    private String mainBusiness;

    @ApiModelProperty("ISIN代码")
    private String isIn;
}
