package com.vv.finance.investment.bg.api.impl.uts;

import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.api.uts.NoticeService;
import com.vv.finance.investment.bg.entity.uts.HkIisNewsAttachmentBase;
import com.vv.finance.investment.bg.handler.uts.f10.NoticeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author luoyj
 * @date 2022/4/19
 * @description
 */
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
@Slf4j
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeHandler noticeHandler;

    @Override
    public ResultT doRepair(String lineId, HkIisNewsAttachmentBase attachment) {
        return noticeHandler.doRepair(lineId,attachment);
    }
}
