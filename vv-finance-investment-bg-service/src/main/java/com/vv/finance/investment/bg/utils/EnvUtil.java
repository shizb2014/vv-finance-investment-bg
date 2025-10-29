package com.vv.finance.investment.bg.utils;

import cn.hutool.core.util.StrUtil;
import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.util.ConfigUtil;

/**
 * @Author:maling
 * @Date:2023/9/21
 * @Description: 环境工具类
 */

public class EnvUtil {

    public static String getEnv() {
        Env env = ApolloInjector.getInstance(ConfigUtil.class).getApolloEnv();
        String result = env.name();
        if (env == Env.PRO) {
            //灰度环境的启动参数为：-Denv=pro -Dapollo.cluster=predeploy
            //因此还需要判断apollo.cluster是否为predeploy
            String apolloCluster = System.getProperty("apollo.cluster");
            String pre = "pre";
            if (StrUtil.isNotEmpty(apolloCluster) && apolloCluster.contains(pre)) {
                result = pre;
            }
        }
        return result;
    }

}
