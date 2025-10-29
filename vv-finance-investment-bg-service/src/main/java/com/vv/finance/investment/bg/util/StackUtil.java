package com.vv.finance.investment.bg.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yangpeng
 * @date 2024/10/17 10:42
 * @description
 */
public class StackUtil {

    private static final String BASE_PACKAGE = "com.vv.finance";

    public static String getStackTraceString() {
        List<String> stackTraceList = getStackTraceList();
        return String.join(" --> ", stackTraceList);
    }

    public static List<String> getStackTraceList() {
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
        // 去掉 Thread#getStackTrace 和 StackUtil#getStackTraceList
        StackTraceElement[] subStacks = ArrayUtil.sub(stackTraces, 2, stackTraces.length);
        // 过滤掉其他调用，仅保留 com.vv.finance 相关
        StackTraceElement[] filterStacks = ArrayUtil.filter(subStacks, stack -> StrUtil.containsIgnoreCase(stack.getClassName(), BASE_PACKAGE));
        // 合并
        return Arrays.stream(filterStacks).map(st -> StrUtil.join("#", st.getClassName(), st.getMethodName(), st.getLineNumber())).collect(Collectors.toList());
    }
}

