package com.ancientcharmoffujianstyle.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("打卡非遗标签")
public class CheckInTagVo {

    @ApiModelProperty("非遗id")
    private Long fyId;

    @ApiModelProperty("非遗名称")
    private String fyName;

    @ApiModelProperty("打卡数量")
    private Integer count;
}
