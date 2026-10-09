package com.ancientcharmoffujianstyle.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("打卡实体")
public class CheckIn {

  @ApiModelProperty("打卡id")
  @TableId(type = IdType.AUTO)
  private Long id;
  @ApiModelProperty("发布者id")
  private Long userId;
  @ApiModelProperty("图片地址")
  private String pictureUrl;
  @ApiModelProperty("文本信息")
  private String txt;
  @ApiModelProperty("创建时间")
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;
  @ApiModelProperty("打卡非遗的id(若为空就弄成未知)")
  private Long fyId;
}
