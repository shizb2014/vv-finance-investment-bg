package com.vv.finance.investment.bg.dto.req;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName: IndexQueryReq
 * @Description: 指标查询实体类
 * @Author: Demon
 * @Datetime: 2020/11/2   14:01
 */
@Data
public class IndexQueryReq implements Serializable {

    private static final long serialVersionUID = 3176768826243747548L;

    /**
     * 指数编码
     */
    String code;

    /**
     * 从什么时间开始往后
     */
    Date date;


    /**
     * 查询条数
     */
    Integer num;

    /**
     * forward：前复权；backward：后复权，为空 不复权
     */
    String adjhkt;



}
