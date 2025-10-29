package com.vv.finance.investment.bg.dto.uts.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenyu
 * @date 2021/7/21 15:09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeMongoPageResp {
    private List<StockUtsNoticeResp> data;

    private Long total;
}
