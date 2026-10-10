package com.ancientcharmoffujianstyle.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户实体")
public class SysUser {

  @ApiModelProperty("用户id")
  @TableId(type = IdType.AUTO)
  private Long userId;
  @ApiModelProperty("用户名")
  private String userName;
  @ApiModelProperty("密码")
  @com.fasterxml.jackson.annotation.JsonIgnore
  private String password;
  @ApiModelProperty("创建时间")
  private java.sql.Timestamp createTime;
  @ApiModelProperty("状态")
  private Long status;

}
