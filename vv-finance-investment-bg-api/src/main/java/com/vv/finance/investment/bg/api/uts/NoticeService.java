package com.vv.finance.investment.bg.api.uts;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.entity.uts.HkIisNewsAttachmentBase;

/**
 * @author luoyj
 * @date 2022/4/19
 * @description
 */
public interface NoticeService {

    /**
     * 修复公告数据
     * @param lineId
     * @return
     */
    ResultT doRepair(String lineId, HkIisNewsAttachmentBase attachment);
}
