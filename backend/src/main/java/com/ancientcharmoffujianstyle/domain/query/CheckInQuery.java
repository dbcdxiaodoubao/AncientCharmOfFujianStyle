package com.ancientcharmoffujianstyle.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("打卡实体")
public class CheckInQuery {

  @ApiModelProperty("发布者id")
  @NotNull(message = "发布者id不能为空")
  private Long userId;

  @ApiModelProperty("打卡非遗的id")
  @NotNull(message = "非遗id不能为空")
  private Long fyId;

  @ApiModelProperty("文本信息")
  private String txt;

}
