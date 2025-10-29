package com.vv.finance.investment.bg.stock.info;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 股票关系表
 * @TableName t_hk_stock_relation
 */
@TableName(value ="t_hk_stock_relation")
@Data
public class HkStockRelation implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 唯一 ID 港股 1000000001
     */
    @TableField(value = "stock_id")
    private Long stockId;

    /**
     * 证券代码（原股票代码）
     */
    @TableField(value = "source_code")
    private String sourceCode;

    /**
     * 证券代码
     */
    @TableField(value = "inner_code")
    private String innerCode;

    /**
     * 状态,0-正常;1-失效
     */
    @TableField(value = "security_status")
    private Integer securityStatus;

    /**
     * 业务类型，0-正常，1-临时股票，2-代码复用，3-转板 4-股权
     */
    @TableField(value = "biz_type")
    private Integer bizType;

    /**
     * 业务时间，格式：yyyyMMdd，如临时股票存并行交易结束时间，代码复用或转板存对应变更时间
     */
    @TableField(value = "biz_time")
    private String bizTime;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        HkStockRelation other = (HkStockRelation) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getStockId() == null ? other.getStockId() == null : this.getStockId().equals(other.getStockId()))
            && (this.getSourceCode() == null ? other.getSourceCode() == null : this.getSourceCode().equals(other.getSourceCode()))
            && (this.getInnerCode() == null ? other.getInnerCode() == null : this.getInnerCode().equals(other.getInnerCode()))
            && (this.getSecurityStatus() == null ? other.getSecurityStatus() == null : this.getSecurityStatus().equals(other.getSecurityStatus()))
            && (this.getBizType() == null ? other.getBizType() == null : this.getBizType().equals(other.getBizType()))
            && (this.getBizTime() == null ? other.getBizTime() == null : this.getBizTime().equals(other.getBizTime()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getStockId() == null) ? 0 : getStockId().hashCode());
        result = prime * result + ((getSourceCode() == null) ? 0 : getSourceCode().hashCode());
        result = prime * result + ((getInnerCode() == null) ? 0 : getInnerCode().hashCode());
        result = prime * result + ((getSecurityStatus() == null) ? 0 : getSecurityStatus().hashCode());
        result = prime * result + ((getBizType() == null) ? 0 : getBizType().hashCode());
        result = prime * result + ((getBizTime() == null) ? 0 : getBizTime().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", stockId=").append(stockId);
        sb.append(", sourceCode=").append(sourceCode);
        sb.append(", innerCode=").append(innerCode);
        sb.append(", securityStatus=").append(securityStatus);
        sb.append(", bizType=").append(bizType);
        sb.append(", bizTime=").append(bizTime);
        sb.append(", remark=").append(remark);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}