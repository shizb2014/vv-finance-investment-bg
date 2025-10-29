package com.vv.finance.investment.bg.utils;

import cn.hutool.core.text.CharSequenceUtil;
import com.vv.finance.base.utils.IPUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author:maling
 * @Date:2023/9/21
 * @Description: 钉钉工具
 */

@Slf4j
@Configuration
public class DingUtil {

    private static String alertUrl;

    @Value("${southward.ding.alert.url:}")
    public void setAlertUrl(String param) {
        alertUrl = param;
    }


    /**
     * 告警
     */
    public static void alert(String message) {
        if (CharSequenceUtil.isBlank(alertUrl)) {
            log.error("未配置alertUrl");
            return;
        }
        message = "环境:" + EnvUtil.getEnv() + " " + "IP:" + IPUtils.getLocalIpAddress() + "\n" + message;
        sendText(message, alertUrl, true);
    }


    private static void sendText(String message, String url, boolean isAtAll) {
//        SendMessageIn in = new SendMessageIn();
//        in.setMsgType("text");
//        in.setWebhook(url);
//        in.setText(message);
//        in.setAtAll(isAtAll);
//        DingDingUtils.sendMessage(in);
    }

}
