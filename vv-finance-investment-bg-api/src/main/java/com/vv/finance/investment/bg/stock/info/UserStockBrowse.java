package com.vv.finance.investment.bg.stock.info;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author chenyu
 * @date 2020/11/11 12:01
 */
@Data
@TableName("t_user_stock_browse")
public class UserStockBrowse implements Serializable {


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String code;
    private Long userId;
    private Date createTime;
    private Date updateTime;

    @JsonIgnore
    @TableLogic
    private Integer deletedFlag;
    private Integer type;


}
