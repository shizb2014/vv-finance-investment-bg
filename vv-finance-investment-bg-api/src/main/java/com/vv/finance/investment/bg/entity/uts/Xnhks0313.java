package com.vv.finance.investment.bg.entity.uts;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName xnhks0313
 */
@TableName(value ="xnhks0313")
@Data
public class Xnhks0313 implements Serializable {
    /**
     * 
     */
    @TableId
    private String seccode;

    /**
     * 
     */
    private String f002v;

    /**
     * 
     */
    private Long f001d;

    /**
     * 
     */
    private Long f003d;

    /**
     * 
     */
    private String f004v;

    /**
     * 
     */
    private String f005v;

    /**
     * 
     */
    private String f006v;

    /**
     * 
     */
    private Date createDate;

    /**
     * 
     */
    private Date modifiedDate;

    /**
     * 
     */
    private Long xdbmask;

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
        Xnhks0313 other = (Xnhks0313) that;
        return (this.getSeccode() == null ? other.getSeccode() == null : this.getSeccode().equals(other.getSeccode()))
            && (this.getF002v() == null ? other.getF002v() == null : this.getF002v().equals(other.getF002v()))
            && (this.getF001d() == null ? other.getF001d() == null : this.getF001d().equals(other.getF001d()))
            && (this.getF003d() == null ? other.getF003d() == null : this.getF003d().equals(other.getF003d()))
            && (this.getF004v() == null ? other.getF004v() == null : this.getF004v().equals(other.getF004v()))
            && (this.getF005v() == null ? other.getF005v() == null : this.getF005v().equals(other.getF005v()))
            && (this.getF006v() == null ? other.getF006v() == null : this.getF006v().equals(other.getF006v()))
            && (this.getCreateDate() == null ? other.getCreateDate() == null : this.getCreateDate().equals(other.getCreateDate()))
            && (this.getModifiedDate() == null ? other.getModifiedDate() == null : this.getModifiedDate().equals(other.getModifiedDate()))
            && (this.getXdbmask() == null ? other.getXdbmask() == null : this.getXdbmask().equals(other.getXdbmask()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSeccode() == null) ? 0 : getSeccode().hashCode());
        result = prime * result + ((getF002v() == null) ? 0 : getF002v().hashCode());
        result = prime * result + ((getF001d() == null) ? 0 : getF001d().hashCode());
        result = prime * result + ((getF003d() == null) ? 0 : getF003d().hashCode());
        result = prime * result + ((getF004v() == null) ? 0 : getF004v().hashCode());
        result = prime * result + ((getF005v() == null) ? 0 : getF005v().hashCode());
        result = prime * result + ((getF006v() == null) ? 0 : getF006v().hashCode());
        result = prime * result + ((getCreateDate() == null) ? 0 : getCreateDate().hashCode());
        result = prime * result + ((getModifiedDate() == null) ? 0 : getModifiedDate().hashCode());
        result = prime * result + ((getXdbmask() == null) ? 0 : getXdbmask().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", seccode=").append(seccode);
        sb.append(", f002v=").append(f002v);
        sb.append(", f001d=").append(f001d);
        sb.append(", f003d=").append(f003d);
        sb.append(", f004v=").append(f004v);
        sb.append(", f005v=").append(f005v);
        sb.append(", f006v=").append(f006v);
        sb.append(", createDate=").append(createDate);
        sb.append(", modifiedDate=").append(modifiedDate);
        sb.append(", xdbmask=").append(xdbmask);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}