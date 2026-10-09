package com.ancientcharmoffujianstyle.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("规划信息")
public class PlanQuery {

    @ApiModelProperty("开始城市id")
    private Long start;

    @ApiModelProperty("结束城市id")
    private Long end;

}
