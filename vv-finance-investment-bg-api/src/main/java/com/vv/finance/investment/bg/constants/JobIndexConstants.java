package com.vv.finance.investment.bg.constants;

/**
 * @ClassName: JobIndexConstants
 * @Description:  指数任务涉及常量
 * @Author: Demon
 * @Datetime: 2020/10/28   10:18
 */
public interface JobIndexConstants {

    /**
     * K线指数编码 0000100(恒生指数) 0001400(国企指数) 0001500(红筹指数)
     * 多个指数以英文逗号隔开
     */
    String code = "code";

    /**
     * 获取数据量
     */
    String number = "number";

    /**
     * 格式为20190101。从哪天往后，默认今天，用以向后去
     * 增量
     */
    String day = "day";

    /**
     * 历史几天，如：1
     */
    String historyDay = "historyDay";

    /**
     * 格式为yyyy-MM-dd HH:mm:ss.SSS。从哪天往后，默认今天，用以向后去增量
     */
    String date = "date";

    /**
     * 格式为yyyy-MM-dd HH:mm:ss.SSS。
     */
    String startDate = "startDate";

    /**
     * 格式为yyyy-MM-dd HH:mm:ss.SSS。
     */
    String endDate = "endDate";

    /**
     * 模式 dly：延时，rt：实时
     */
    String mode = "mode";

    /**
     * forward：前复权；backward：后复权，为空不复权
     */
    String adjhkt = "adjhkt";

    /**
     * 分钟数，支持1，5，15，30
     */
    String minc = "minc";

    /**
     * 分页页数
     */
    String pageNum = "pageNum";

    /**
     * 每页显示数据条数
     */
    String pageSize = "pageSize";

}
