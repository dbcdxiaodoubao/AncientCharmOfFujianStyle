package com.ancientcharmoffujianstyle.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户信息列表")
public class SysUserListVo {

  @ApiModelProperty("用户id")
  private Long userId;
  @ApiModelProperty("用户名")
  private String userName;
  @ApiModelProperty("密码")
  private String password;
  @ApiModelProperty("创建时间")
  private java.sql.Timestamp createTime;
  @ApiModelProperty("状态")
  private Long status;


}
