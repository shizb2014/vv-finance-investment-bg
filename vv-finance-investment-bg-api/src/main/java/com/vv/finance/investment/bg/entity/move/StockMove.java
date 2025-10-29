package com.vv.finance.investment.bg.entity.move;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName StockMove
 * @Deacription 异动主题
 * @Author lh.sz
 * @Date 2021年04月29日 10:41
 **/
@Data
@ToString
@TableName(value = "t_stock_move_theme")
public class StockMove implements Serializable {
    private static final long serialVersionUID = -1682670390094234663L;
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    @TableField(value = "code")
    @ApiModelProperty(value = "股票代码")
    private String code;

    @TableField(value = "name")
    @ApiModelProperty(value = "股票名称")
    private String name;

    @TableField(value = "move_type")
    @ApiModelProperty(value = "异动类型")
    private Integer moveType;

    @TableField(value = "move_data")
    @ApiModelProperty(value = "异动数据")
    private String moveData;

    @TableField(value = "time")
    @ApiModelProperty(value = "异动时间")
    private Long time;

    @TableField(value = "move_num")
    @ApiModelProperty("异动类型数量")
    private Integer moveNum;

    @ApiModelProperty(value = "serialId")
    @TableField(value = "serial_id")
    private String serialId;

    @TableField(value = "create_time")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @TableField(value = "update_time")
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;

    /**
     * 唯一 ID 港股 1000000001
     */
    @TableField(value = "stock_id")
    private Long stockId;
}
