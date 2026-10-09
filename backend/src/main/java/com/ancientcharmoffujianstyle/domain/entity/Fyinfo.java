package com.ancientcharmoffujianstyle.domain.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("非遗信息实体")
public class Fyinfo {

  @ApiModelProperty("非遗id")
  @TableId(type = IdType.AUTO)
  private Long id;

  @ApiModelProperty("非遗名称")
  private String name;

  @ApiModelProperty("非遗详情")
  private String dtl;

  @ApiModelProperty("图片路径")
  private String pictureUrl;

  @ApiModelProperty("所属城市（1-9分别代表漳州、厦门、泉州、莆田、福州、宁德、南平、三明、龙岩）")
  private Long city;

  @ApiModelProperty("级别（1-5分别代表世界级、国家级、省级、市级、县级）")
  private Long level;

  @ApiModelProperty("非遗类型")
  private String type;
}
