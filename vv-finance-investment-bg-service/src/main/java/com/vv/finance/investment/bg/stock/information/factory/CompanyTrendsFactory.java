package com.vv.finance.investment.bg.stock.information.factory;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.vv.finance.investment.bg.entity.information.CompanyTrendsMergeEntity;
import com.vv.finance.investment.bg.entity.uts.*;
import com.vv.finance.investment.bg.mapper.uts.*;
import com.vv.finance.investment.bg.stock.information.enun.CompanyTrendsSubType;
import com.vv.finance.investment.bg.stock.information.enun.CompanyTrendsType;
import org.springframework.util.ObjectUtils;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CompanyTrendsFactory {
    public static final String YYYYMMDD_FORMAT = "yyyyMMdd";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYYMMDD_FORMAT);
    private static final Map<String, String> reportTypeMap = MapUtil.builder("I", "中期").put("Q1", "第一季度").put("Q2", "第二季度")
            .put("Q3", "第三季度").put("Q4", "第四季度").put("Q5", "第五季度").put("Q6", "第六季度").put("F", "年度").put("P", "上市前").build();

    public static CompanyTrendsMergeEntity fromXnhk127(Xnhk0127 xnhk0127) {
        CompanyTrendsMergeEntity companyTrendsMergeEntity = new CompanyTrendsMergeEntity();
        companyTrendsMergeEntity.setSECCODE(xnhk0127.getSeccode());
        companyTrendsMergeEntity.setContent(xnhk0127.getF006v());
        companyTrendsMergeEntity.setType(CompanyTrendsType.TEN.getCode());
        companyTrendsMergeEntity.setReleaseDate(xnhk0127.getF003d().format(formatter));
        companyTrendsMergeEntity.setOrderDate(xnhk0127.getF003d().format(formatter));
        companyTrendsMergeEntity.setDate1(xnhk0127.getF003d().format(formatter));
        companyTrendsMergeEntity.setDate2(null);
        companyTrendsMergeEntity.setSxdbmask(xnhk0127.getXdbmask());
        // type + 主键
        companyTrendsMergeEntity.setUni(getMD5(CompanyTrendsType.TEN.getCode(), xnhk0127.getId()));
        companyTrendsMergeEntity.setSubType(null);
        return companyTrendsMergeEntity;
    }


    public static CompanyTrendsMergeEntity fromXnhk0201(Xnhk0201 xnhk0201) {
        CompanyTrendsMergeEntity companyTrendsMergeEntity = SpringUtil.getBean(Xnhk0201Mapper.class).getByPrimary(CompanyTrendsType.ELEVEN.getCode(),
                xnhk0201.getSeccode(), xnhk0201.getF002d(), xnhk0201.getF006v());
        if(ObjectUtils.isEmpty(companyTrendsMergeEntity)){
            return null;
        }
        if (StrUtil.isBlank(companyTrendsMergeEntity.getContent())) {
            companyTrendsMergeEntity.setContent("发布" + reportTypeMap.get(xnhk0201.getF006v()) + "财报");
        }
        companyTrendsMergeEntity.setSubType(CompanyTrendsSubType.FINANCIAL_REPORT_1.getCode());
        // type + sub_type + seccode + f002d + f006v
        companyTrendsMergeEntity.setUni(getMD5(String.valueOf(CompanyTrendsType.ELEVEN.getCode()),
                companyTrendsMergeEntity.getSubType(),
                xnhk0201.getSeccode(),
                xnhk0201.getF001d(),
                xnhk0201.getF006v()));
        return companyTrendsMergeEntity;
    }

    public static CompanyTrendsMergeEntity fromXnhk0204(Xnhk0204 xnhk0204) {
        CompanyTrendsMergeEntity companyTrendsMergeEntity = SpringUtil.getBean(Xnhk0204Mapper.class).getByPrimary(CompanyTrendsType.ELEVEN.getCode(),
                xnhk0204.getSeccode(), xnhk0204.getF002d(), xnhk0204.getF006v());
        if(ObjectUtils.isEmpty(companyTrendsMergeEntity)){
            return null;
        }
        if (StrUtil.isBlank(companyTrendsMergeEntity.getContent())) {
            companyTrendsMergeEntity.setContent("发布" + reportTypeMap.get(xnhk0204.getF006v()) + "财报");
        }
        companyTrendsMergeEntity.setSubType(CompanyTrendsSubType.FINANCIAL_REPORT_2.getCode());
        // type + sub_type + seccode + f002d + f006v
        companyTrendsMergeEntity.setUni(getMD5(String.valueOf(CompanyTrendsType.ELEVEN.getCode()),
                companyTrendsMergeEntity.getSubType(),
                xnhk0204.getSeccode(),
                xnhk0204.getF001d(),
                xnhk0204.getF006v()));
        return companyTrendsMergeEntity;
    }

    public static CompanyTrendsMergeEntity fromXnhk0207(Xnhk0207 xnhk0207) {
        CompanyTrendsMergeEntity companyTrendsMergeEntity = SpringUtil.getBean(Xnhk0207Mapper.class).getByPrimary(CompanyTrendsType.ELEVEN.getCode(),
                xnhk0207.getSeccode(), xnhk0207.getF002d(), xnhk0207.getF006v());
        if(ObjectUtils.isEmpty(companyTrendsMergeEntity)){
            return null;
        }
        if (StrUtil.isBlank(companyTrendsMergeEntity.getContent())) {
            companyTrendsMergeEntity.setContent("发布" + reportTypeMap.get(xnhk0207.getF006v()) + "财报");
        }
        companyTrendsMergeEntity.setSubType(CompanyTrendsSubType.FINANCIAL_REPORT_3.getCode());
        // type + sub_type + seccode + f002d + f006v
        companyTrendsMergeEntity.setUni(getMD5(String.valueOf(CompanyTrendsType.ELEVEN.getCode())
                , companyTrendsMergeEntity.getSubType()
                , xnhk0207.getSeccode()
                , xnhk0207.getF001d()
                , xnhk0207.getF006v()));
        return companyTrendsMergeEntity;
    }

    public static CompanyTrendsMergeEntity fromXnhks0308(Xnhks0308 xnhks0308) {
        CompanyTrendsMergeEntity companyTrendsMergeEntity = new CompanyTrendsMergeEntity();
        companyTrendsMergeEntity.setSECCODE(xnhks0308.getSeccode());
        companyTrendsMergeEntity.setContent(xnhks0308.getF006v());
        companyTrendsMergeEntity.setType(CompanyTrendsType.PURCHASE.getCode());
        companyTrendsMergeEntity.setReleaseDate(String.valueOf(xnhks0308.getF001d()));
        companyTrendsMergeEntity.setOrderDate(String.valueOf(xnhks0308.getF005d()));
        companyTrendsMergeEntity.setDate1(String.valueOf(xnhks0308.getF005d()));
        companyTrendsMergeEntity.setSxdbmask(xnhks0308.getXdbmask());
        // type + seccode + f001d
        companyTrendsMergeEntity.setUni(getMD5(String.valueOf(CompanyTrendsType.PURCHASE.getCode())
                , xnhks0308.getSeccode()
                , xnhks0308.getF001d()));
        companyTrendsMergeEntity.setSubType(null);
        return companyTrendsMergeEntity;
    }


    public static CompanyTrendsMergeEntity fromXnhks0310(Xnhks0310 xnhks0310) {
        CompanyTrendsMergeEntity companyTrendsMergeEntity = SpringUtil.getBean(Xnhks0310Mapper.class).getByPrimary(CompanyTrendsType.METTING.getCode(),
                xnhks0310.getSeccode(), xnhks0310.getF002v());
        if(ObjectUtils.isEmpty(companyTrendsMergeEntity)){
            return null;
        }
        companyTrendsMergeEntity.setSubType(null);
        // type + seccode + f002v
        companyTrendsMergeEntity.setUni(getMD5(String.valueOf(CompanyTrendsType.METTING.getCode()),
                xnhks0310.getSeccode(),
                xnhks0310.getF003d(),
                xnhks0310.getF005v()));
        return companyTrendsMergeEntity;
    }

    public static CompanyTrendsMergeEntity fromXnhk0311(Xnhk0311 xnhk0311) {
        CompanyTrendsMergeEntity companyTrendsMergeEntity = SpringUtil.getBean(Xnhk0311Mapper.class).getByPrimary(CompanyTrendsType.ALARM.getCode(),
                xnhk0311.getSeccode(), xnhk0311.getF001d(), xnhk0311.getF003v());
        if(ObjectUtils.isEmpty(companyTrendsMergeEntity)){
            return null;
        }
        companyTrendsMergeEntity.setSubType(null);
        // type + seccode + F001D + F003V
        companyTrendsMergeEntity.setUni(getMD5(String.valueOf(CompanyTrendsType.ALARM.getCode()),
                xnhk0311.getSeccode(),
                xnhk0311.getF001d(),
                xnhk0311.getF003v()));
        return companyTrendsMergeEntity;
    }


    public static CompanyTrendsMergeEntity fromXnhks0314(Xnhks0314 xnhks0314) {
        CompanyTrendsMergeEntity companyTrendsMergeEntity = new CompanyTrendsMergeEntity();
        companyTrendsMergeEntity.setSECCODE(xnhks0314.getSeccode());
        companyTrendsMergeEntity.setContent("并行证券代码：0" + xnhks0314.getF003v());
        companyTrendsMergeEntity.setContent2("并行证券名称：" + xnhks0314.getF005v());
        companyTrendsMergeEntity.setType(CompanyTrendsType.PARALLEL.getCode());
        companyTrendsMergeEntity.setReleaseDate(String.valueOf(xnhks0314.getF001d()));
        companyTrendsMergeEntity.setOrderDate(String.valueOf(xnhks0314.getF001d()));

        String[] f009VSplit = xnhks0314.getF009v().split("-");
        companyTrendsMergeEntity.setDate1(f009VSplit[0].replace("/", ""));
        companyTrendsMergeEntity.setDate2(f009VSplit[1].replace("/", ""));
        companyTrendsMergeEntity.setSxdbmask(xnhks0314.getXdbmask());
        companyTrendsMergeEntity.setSubType(null);
        // type + seccode + F006D
        companyTrendsMergeEntity.setUni(getMD5(String.valueOf(CompanyTrendsType.PARALLEL.getCode()),
                xnhks0314.getSeccode(),
                xnhks0314.getF006d()));
        return companyTrendsMergeEntity;
    }

    public static CompanyTrendsMergeEntity fromXnhks0317(Xnhks0317 xnhks0317) {
        CompanyTrendsMergeEntity companyTrendsMergeEntity = SpringUtil.getBean(Xnhks0317Mapper.class).getByPrimary(CompanyTrendsType.TRADING.getCode(),
                xnhks0317.getSeccode(), xnhks0317.getF002d());
        if (companyTrendsMergeEntity == null) {
            return null;
        }
        companyTrendsMergeEntity.setSubType(CompanyTrendsSubType.SUSPENSION.getCode());
        // type + sub_type + seccode + F002D
        companyTrendsMergeEntity.setUni(getMD5(String.valueOf(CompanyTrendsType.TRADING.getCode()),
                companyTrendsMergeEntity.getSubType(),
                xnhks0317.getSeccode(),
                xnhks0317.getF002d()));
        return companyTrendsMergeEntity;
    }


    public static CompanyTrendsMergeEntity fromXnhk0318(Xnhk0318 xnhk0318) {
        CompanyTrendsMergeEntity companyTrendsMergeEntity = new CompanyTrendsMergeEntity();
        companyTrendsMergeEntity.setSECCODE(xnhk0318.getSeccode());
        companyTrendsMergeEntity.setContent("");
        companyTrendsMergeEntity.setType(CompanyTrendsType.TRADING.getCode());
        companyTrendsMergeEntity.setReleaseDate(String.valueOf(xnhk0318.getF001d()));
        companyTrendsMergeEntity.setOrderDate(String.valueOf(xnhk0318.getF001d()));
        companyTrendsMergeEntity.setDate1(String.valueOf(xnhk0318.getF003d()));
        companyTrendsMergeEntity.setDate2(String.valueOf(xnhk0318.getF002d()));
        companyTrendsMergeEntity.setSxdbmask(xnhk0318.getXdbmask());
        companyTrendsMergeEntity.setSubType(CompanyTrendsSubType.RESUMPTION.getCode());
        // type + sub_type + seccode + F002D
        companyTrendsMergeEntity.setUni(getMD5(String.valueOf(CompanyTrendsType.TRADING.getCode()),
                companyTrendsMergeEntity.getSubType(),
                xnhk0318.getSeccode(),
                xnhk0318.getF002d()));
        return companyTrendsMergeEntity;
    }


    public static String getMD5(Object... args) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            sb.append(args[i].toString());
            // 在每个参数之间添加特殊符号 "-"
            if (i < args.length - 1) {
                sb.append("-");
            }
        }
        return DigestUtil.md5Hex(sb.toString());
    }


}
