package com.vv.finance.investment.bg.api.impl.quotationconfig;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vv.finance.base.dto.ResultT;
import com.vv.finance.investment.bg.api.quotationconfig.QuotationConfigServiceApi;
import com.vv.finance.investment.bg.domain.UserQuotationCycleConfig;
import com.vv.finance.investment.bg.dto.quotationconfig.CustomQuotationConfigDTO;
import com.vv.finance.investment.bg.dto.quotationconfig.UserQuotationConfigResDTO;
import com.vv.finance.investment.bg.enums.QuotationCycleEnum;
import com.vv.finance.investment.bg.enums.UnitEnum;
import com.vv.finance.investment.bg.mapper.quotationconfig.UserQuotationCycleConfigMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * description: QuotationConfigServiceApiImpl
 * date: 2022/8/11 10:11
 * author: fenghua.cai
 */
@Service
@DubboService(group = "${dubbo.investment.bg.service.group:bg}", registry = "bgservice")
public class QuotationConfigServiceApiImpl implements QuotationConfigServiceApi {

    @Resource
    UserQuotationCycleConfigMapper userQuotationCycleConfigMapper;

    @Override
    public ResultT<Long> addUserCustomQuotationConfig(CustomQuotationConfigDTO customQuotationConfigDTO, String userName) {
        // 默认的1年-日K，3年-日K，5年-日K几个选项 不让添加
        if (QuotationCycleEnum.DAILY.getName().equalsIgnoreCase(customQuotationConfigDTO.getCycle()) && UnitEnum.YEAR.getName().equalsIgnoreCase(customQuotationConfigDTO.getUnit())) {
            if (customQuotationConfigDTO.getNum() == 1 || customQuotationConfigDTO.getNum() == 3 || customQuotationConfigDTO.getNum() == 5) {
                return ResultT.fail("该设置已重复");
            }
        }
        //未传区域类型默认港股
        if(customQuotationConfigDTO.getRegionType() == null){
            customQuotationConfigDTO.setRegionType(0);
        }
        UserQuotationCycleConfig userQuotationConfig = userQuotationCycleConfigMapper.selectOne(
                new LambdaQueryWrapper<UserQuotationCycleConfig>().eq(UserQuotationCycleConfig::getUserName, userName)
                        .eq(UserQuotationCycleConfig::getCycle, customQuotationConfigDTO.getCycle())
                        .eq(UserQuotationCycleConfig::getUnit, customQuotationConfigDTO.getUnit())
                        .eq(UserQuotationCycleConfig::getNum, customQuotationConfigDTO.getNum())
                        .eq(UserQuotationCycleConfig::getRegionType, customQuotationConfigDTO.getRegionType())
        );

        if (!ObjectUtils.isEmpty(userQuotationConfig)) {
            return ResultT.fail("该设置已重复");
        }

        UserQuotationCycleConfig userQuotationCycleConfig = new UserQuotationCycleConfig();
        BeanUtils.copyProperties(customQuotationConfigDTO, userQuotationCycleConfig);
        userQuotationCycleConfig.setUserName(userName);
        userQuotationCycleConfigMapper.insert(userQuotationCycleConfig);
        return ResultT.success(userQuotationCycleConfig.getId());
    }

    @Override
    public ResultT<Integer> removeUserCustomQuotationConfig(Long id) {
        return ResultT.success(userQuotationCycleConfigMapper.deleteById(id));
    }

    @Override
    public ResultT<List<UserQuotationConfigResDTO>> getUserCustomQuotationConfig(String userName) {
        List<UserQuotationCycleConfig> userQuotationCycleConfigs = userQuotationCycleConfigMapper.selectList(
                new LambdaQueryWrapper<UserQuotationCycleConfig>().eq(UserQuotationCycleConfig::getUserName, userName)
                        .orderByAsc(UserQuotationCycleConfig::getCreatedTime)
        );
        List<UserQuotationConfigResDTO> userQuotationConfigResDTOList = new ArrayList<>();
        for (UserQuotationCycleConfig userQuotationCycleConfig : userQuotationCycleConfigs) {
            UserQuotationConfigResDTO userQuotationConfigResDTO = new UserQuotationConfigResDTO();
            BeanUtils.copyProperties(userQuotationCycleConfig, userQuotationConfigResDTO);
            userQuotationConfigResDTOList.add(userQuotationConfigResDTO);
        }
        return ResultT.success(userQuotationConfigResDTOList);
    }

    @Override
    public ResultT<List<UserQuotationConfigResDTO>> getUserCustomQuotationConfigNew(Integer regionType,String userName) {
        List<UserQuotationCycleConfig> userQuotationCycleConfigs = userQuotationCycleConfigMapper.selectList(
                new LambdaQueryWrapper<UserQuotationCycleConfig>().eq(UserQuotationCycleConfig::getUserName, userName)
                        .eq(UserQuotationCycleConfig::getRegionType,regionType)
                        .orderByAsc(UserQuotationCycleConfig::getCreatedTime)
        );
        List<UserQuotationConfigResDTO> userQuotationConfigResDTOList = new ArrayList<>();
        for (UserQuotationCycleConfig userQuotationCycleConfig : userQuotationCycleConfigs) {
            UserQuotationConfigResDTO userQuotationConfigResDTO = new UserQuotationConfigResDTO();
            BeanUtils.copyProperties(userQuotationCycleConfig, userQuotationConfigResDTO);
            userQuotationConfigResDTOList.add(userQuotationConfigResDTO);
        }
        return ResultT.success(userQuotationConfigResDTOList);
    }
}
