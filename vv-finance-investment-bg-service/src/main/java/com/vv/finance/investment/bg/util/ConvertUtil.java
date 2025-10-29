package com.vv.finance.investment.bg.util;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.vv.finance.common.utils.MyStringUtil;

/**
 * <P>
 *     1. 纯中文，去掉内部的空格, 如 阿里巴巴 -> alibaba
 *     2. 纯英文，保留空格, 如 Adeia Inc. -> adeia inc.
 *     3. 中英文混合，去掉中间的空格 Assembly生物 -> assemblyshengwu
 * </P>
 *
 * @author yangpeng
 * @date 2024/1/19 14:52
 * @description
 */
public class ConvertUtil {

    private static final String REGEX = "^[a-zA-Z\\s\\&\\d,.!#$%&'*+/=?^_`{|}~-]+$";

    public static String getPinyin(String input) {

        if (StrUtil.isBlank(input)) {
            return null;
        }

        input = MyStringUtil.toDBC(input);

        // 是否为纯英文
        boolean match = ReUtil.isMatch(REGEX, input);
        // 如果是纯英文，不替换空格； 否则去掉空格
        return match ? input.toLowerCase() : PinyinUtil.getPinyin(input).toLowerCase().replace(" ", "");
    }

    public static String getFirstLetter(String input) {

        if (StrUtil.isBlank(input)) {
            return null;
        }

        String firstLetter = PinyinUtil.getFirstLetter(input, "");

        if (StrUtil.isBlank(firstLetter)) {
            return null;
        }
        return firstLetter.toLowerCase();
    }

    public static void main(String[] args) {
        // String pinYin = getPinyin("COSMOPOL INT'L");
        // String pinYin = getPinyin("A８新媒体");
        // String pinYin = getPinyin("C-LINK SQ");
        // String pinYin = getPinyin("B & D STRATEGIC");
        // String pinYin = getPinyin("K2 F&B");
        String pinYin = getPinyin("ALX Oncology Holdings, Inc.");
        Console.error(1);
    }
}

