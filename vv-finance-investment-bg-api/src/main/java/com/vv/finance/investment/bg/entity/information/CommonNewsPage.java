package com.vv.finance.investment.bg.entity.information;

import com.vv.finance.common.bean.SimplePageReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * @author wsliang
 * @date 2021/11/3 10:35
 **/
@Data
public class CommonNewsPage extends SimplePageReq {
    private static final long serialVersionUID = -7252205057725848152L;

    /**
     * 按id坐标定位
     */
    @ApiModelProperty("按id坐标定位")
    private Long id;

    /**
     * 新闻时间
     */
    @ApiModelProperty("新闻时间")
    private Long time;

    /**
     * 个股查询必传
     */
    @ApiModelProperty("股票ID")
    private Long stockId;

    /**
     * 个股查询必传
     */
    @ApiModelProperty("权证，个股查询必传")
    private String stockCode;

    @ApiModelProperty(value = "美股新股股票集合", hidden = true)
    private List<String> usNewStocks;

    /**
     * 0: 自选, 1: 异动, 2: 新股, 3: 港股, 4: 美股, 5: 个股资讯
     */
    @ApiModelProperty(" 0: 自选, 1: 异动, 2: 新股, 3: 港股, 4: 美股, 5: 个股资讯, 6：权证, 7：其他")
    private Integer queryCode;

    @Getter
    @AllArgsConstructor
    public enum QueryCodeEnum {
        OPTIONAL(0, "自选",""),
        TRANSACTION(1, "异动",""),
        NEW(2, "新股",""),
        HK(3, "港股","港股"),
        US(4, "美股","美股"),
        SIMPLE(5, "个股资讯",""),
        WARRANT(6, "权证资讯",""),
        OTHER(7, "其他",""),
        ASSIGN(8, "分配股票",""),
        ;

        private Integer code;
        private String value;
        private String market;
    }

    @ApiModelProperty("区域代码类型")
    private Integer regionType;
}
