package com.vv.finance.investment.bg.dto.f10;

import lombok.Data;

import java.io.Serializable;

/**
 * @公司：微微科技有限公司（金融事业部）
 * @描述：
 * @作者：Liam（梁殿豪）
 * @邮箱：liangdianhao@vv.cn
 * @时间：2021/9/23 17:06
 * @版本：1.0
 */
@Data
public class IncrementCompanyEventDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long time;
    private String stockCode;
}