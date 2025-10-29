package com.vv.finance.investment.bg.stock.information;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.apache.dubbo.common.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @ClassName InformationConstant
 * @Deacription 资讯常量
 * @Author lh.sz
 * @Date 2021年09月15日 17:06
 **/
@Data
@ToString
public class InformationConstant {

    public static final String TOP_CATEGORY = "top_category";

    public static final String NEWS_ID = "news_id";

    public static final String NEWS_HK_HQ = "news_hk_hq";

    public static final String NEWS_HK = "news_hk";

    public static final String NEWS_US = "news_us";

    public static final String SECOND_CATEGORY = "second_category";

    public static final String MARKET = "market";

    public static final String DATE_TIME = "date_time";

    public static final String DATE = "date";

    public static final String CONTENT = "content";

    public static final String XDBMASK = "XDBMASK";

    public static final String PUBLISH_STATUS = "publish_status";

    //**************************** table name ******************************

    public static final String TABLE_NEWS24HOURS = "news_24hours";

    public static final String TABLE_CALENDAR = "news_calendar";

    //***************************** value ***********************************

    public static final String SECOND_CATEGORY_0 = "经济事件";

    public static List<String> getLevelValue(List<Integer> level) {
        if (CollectionUtils.isEmpty(level)) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>(Level.values().length);
        for (Level l : Level.values()) {
            if (level.contains(l.level)) {
                values.add(l.value);
            }
        }
        if (values.size() == Level.values().length) {
            return Collections.emptyList();
        }
        return values;
    }

    @AllArgsConstructor
    @Getter
    public enum Level {
        LOW(1, "【重要性】：低"),
        MID(2, "【重要性】：中"),
        HIGH(3, "【重要性】：高"),
        ;

        private final Integer level;
        private final String value;

    }

    //****************************** 公司动向资讯 *********************************

    public static final String COLUMN_ID = "id";

    public static final String COLUMN_RELEASE_DATE = "releaseDate";

    public static final String COLUMN_DATE1 = "date1";

    public static final String COLUMN_SORT_DATE = "sortDate";

    public static final String COLUMN_SECCODE = "SECCODE";

    public static final String COLUMN_TYPE = "type";

    public static final String COLUMN_ORDER_DATE = "order_date";

    //*******************************  财报  **************************************

    public static final String COL_CATEGORT = "categoryId";

    public static final String COL_LANGUAGE = "language";

    public static final String COL_STOCKCODE = "stockCode";

    public static final String COL_DATE_LINE = "dateLine";
}
