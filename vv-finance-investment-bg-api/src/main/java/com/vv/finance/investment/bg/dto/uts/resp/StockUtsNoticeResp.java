package com.vv.finance.investment.bg.dto.uts.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * @author chenyu
 * @date 2021/7/19 14:52
 */
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class StockUtsNoticeResp implements Serializable {

    private static final long serialVersionUID = -6614667457708650446L;


    private String lineId;

    private Integer stockCode;

    private String headLine;

    private String dateLine;

    private String fileDesc;

    private String categoryId;

    private String fileName;

    private Integer attachmentNum;

    private String dirs;

}
