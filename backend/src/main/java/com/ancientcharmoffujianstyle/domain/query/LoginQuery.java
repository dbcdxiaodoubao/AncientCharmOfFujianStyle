package com.ancientcharmoffujianstyle.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("登录实体")
public class LoginQuery {

  @ApiModelProperty(value = "用户名" , required = true)
  @NotBlank(message = "用户名不能为空")
  private String userName;
  @ApiModelProperty(value = "密码" ,required = true)
  @NotBlank(message = "密码不能为空")
  private String password;

}
