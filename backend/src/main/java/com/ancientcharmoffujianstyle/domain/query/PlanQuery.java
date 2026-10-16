package com.ancientcharmoffujianstyle.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("规划信息")
public class PlanQuery {

    @ApiModelProperty("开始城市id")
    @javax.validation.constraints.NotNull(message = "请选择出发城市")
    @javax.validation.constraints.Min(value = 1, message = "出发城市必须在1至9之间")
    @javax.validation.constraints.Max(value = 9, message = "出发城市必须在1至9之间")
    private Long start;

    @ApiModelProperty("结束城市id")
    @javax.validation.constraints.NotNull(message = "请选择结束城市")
    @javax.validation.constraints.Min(value = 1, message = "结束城市必须在1至9之间")
    @javax.validation.constraints.Max(value = 9, message = "结束城市必须在1至9之间")
    private Long end;

}
