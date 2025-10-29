package com.vv.finance.investment.bg.entity.information;

import com.vv.finance.investment.bg.dto.info.EventDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("公司事件返回体")
public class CompanyEventVo implements Serializable {
    private static final long serialVersionUID = 8140568842346780088L;

    @ApiModelProperty(value = "时间戳")
    private Long time;

    @ApiModelProperty(value = "公司事件集合")
    private List<EventDTO> eventList;
}
