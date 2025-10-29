package com.vv.finance.investment.bg.util;

import com.vv.finance.base.utils.ZoneDateUtils;
import com.vv.finance.common.dingding.DingDingTextMessage;
import com.vv.finance.common.enums.BusinessCheckEventEnum;
import com.vv.finance.common.utils.DingDingMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * 记录重要job监控时间
 *
 * @author wuchiwen
 * @date 2022年12月01日 11:35
 */
@Component
@Slf4j
public class OperationEventUtil {

    @Value("${open.api.url:http://172.16.6.54:8901}")
    public String openApiUrl;
    @Value("${dingding.dutyEvent.url:https://oapi.dingtalk.com/robot/send?access_token=607d73aa9677427f07b47786d05ffe1f3f6dce42bf3d5a23a4299adec25b8051}")
    private String DutyEventDingDingUrl;
    @Value("${dingding.dutyEvent.secret:SECea8a67d27141b629e47d54ccbd6d9dfa258889732ef469c2a195686d3badbba9}")
    private String DutyEventDingDingSecret;

    private static final String SERVICE_FORMAT = "{}：股票 [{}] 名称由 [{}] 改为 [{}] {}";

    public void sendDingDingMessage(String[] param) {
        sendDingDingMessage(param[0], param[1], param[2]);
    }

    // 推送钉钉消息
    public void sendDingDingMessage(String code, String oldName, String newName) {

        String time = ZoneDateUtils.convertTimeStr_yyyyMMddHHmmSSS(ZoneDateUtils.getHongKongDateTime());
        String[] params = {BusinessCheckEventEnum.HK_STOCK_NAME_CHANGE.getEvent(), code, oldName, newName, time};
        String message = MessageFormatter.arrayFormat(SERVICE_FORMAT, params).getMessage();

//        DingDingMessageUtils.sendTextMessage(DingDingTextMessage.builder()
//                .webhook(DutyEventDingDingUrl)
//                .secret(DutyEventDingDingSecret)
//                .text(message)
//                .mobileList(null)
//                .build());
    }
}
