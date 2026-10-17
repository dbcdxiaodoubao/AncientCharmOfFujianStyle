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

    @ApiModelProperty("游玩天数")
    @javax.validation.constraints.Min(value = 1, message = "游玩天数至少为1天")
    @javax.validation.constraints.Max(value = 14, message = "游玩天数最多为14天")
    private Integer days = 1;

    @ApiModelProperty("每天交通时间，单位分钟")
    @javax.validation.constraints.Min(value = 0, message = "每天交通时间不能小于0分钟")
    @javax.validation.constraints.Max(value = 720, message = "每天交通时间不能超过720分钟")
    private Integer transportMinutes = 60;

    @ApiModelProperty("用户id，用于读取推荐偏好，可为空")
    private Long userId;

}
