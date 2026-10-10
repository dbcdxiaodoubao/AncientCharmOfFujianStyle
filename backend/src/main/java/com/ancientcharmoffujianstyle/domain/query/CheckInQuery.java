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
  @javax.validation.constraints.Positive(message = "发布者id必须大于0")
  private Long userId;

  @ApiModelProperty("打卡非遗的id")
  @NotNull(message = "非遗id不能为空")
  @javax.validation.constraints.Positive(message = "非遗id必须大于0")
  private Long fyId;

  @ApiModelProperty("文本信息")
  @javax.validation.constraints.Size(max = 1000, message = "打卡文字不能超过1000字")
  private String txt;

}
