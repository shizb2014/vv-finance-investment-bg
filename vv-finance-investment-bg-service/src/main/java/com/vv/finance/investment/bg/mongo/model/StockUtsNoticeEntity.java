package com.vv.finance.investment.bg.mongo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
public class StockUtsNoticeEntity implements Serializable {

    private static final long serialVersionUID = -6614667457708650446L;


    private String lineId;

    private Integer stockCode;

    private String headLine;

    private String dateLine;

    private String fileDesc;

    private String categoryId;

    private String fileName;

    private String rawPath;

    private Integer attachmentNum;

    private Integer language;

    private String dirs;

}
