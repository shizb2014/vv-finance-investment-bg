package com.vv.finance.investment.bg.stock.quotes;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author chenyu
 * @date 2020/10/23 11:02
 */
@Data
@TableName("t_index_define")
public class IndexDefine extends BaseEntity {

    private static final long serialVersionUID = 1809025573990324391L;
    /**
     * 协议类型
     */
    private String protocol;
    /**
     * 证券类型
     */
    private String code;
    /**
     * 指数来源
     */
    private String indexsource;
    /**
     * 币种
     */
    private String currency;
}
