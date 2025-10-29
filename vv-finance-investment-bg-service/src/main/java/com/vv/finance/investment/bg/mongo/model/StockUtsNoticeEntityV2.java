package com.vv.finance.investment.bg.mongo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;

/**
 * @author chenyu
 * @date 2021/7/19 14:52
 */
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Document(collection = "stock_uts_notice")
public class StockUtsNoticeEntityV2 implements Serializable {

    private static final long serialVersionUID = -6614667457708650446L;

    @MongoId
    private String id;

    private String lineId;

    private Integer stockCode;

    private String headLine;

    private String dateLine;

    private String fileDesc;

    private String categoryId;

    private String dirs;

    private String fileName;

    private String rawPath;

    private Integer attachmentNum;

    private Integer language;

}
