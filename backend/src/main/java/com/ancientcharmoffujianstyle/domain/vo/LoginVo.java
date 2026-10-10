package com.ancientcharmoffujianstyle.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("登录实体")
public class LoginVo {

    @ApiModelProperty("用户id")
    private Long userId;
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("登录结果状态：SUCCESS-成功，FAILED-失败，LOCKED-账户锁定")
  private String loginStatus;

  @ApiModelProperty("登录会话令牌")
  private String token;

  @ApiModelProperty("会话过期时间（Unix毫秒）")
  private Long expiresAt;

}
