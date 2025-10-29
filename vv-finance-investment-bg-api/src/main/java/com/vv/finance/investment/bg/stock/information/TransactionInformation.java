package com.vv.finance.investment.bg.stock.information;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @ClassName transactionVO
 * @Deacription 资讯异动数据
 * @Author lh.sz
 * @Date 2021年09月13日 10:51
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@Builder
public class TransactionInformation extends BaseInformation implements Serializable {

    private static final long serialVersionUID = -1682670390094234663L;


}
