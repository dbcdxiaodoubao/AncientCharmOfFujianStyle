package com.ancientcharmoffujianstyle.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("非遗信息列表")
public class FyinfoListVo {

    @ApiModelProperty("非遗id")
    private Long id;

    @ApiModelProperty("非遗名称")
    private String name;

    @ApiModelProperty("图片路径")
    private String pictureUrl;

    @ApiModelProperty("所属城市（1-9分别代表漳州、厦门、泉州、莆田、福州、宁德、南平、三明、龙岩）")
    private Long city;

    @ApiModelProperty("级别（1-5分别代表世界级、国家级、省级、市级、县级）")
    private Long level;

    @ApiModelProperty("非遗类型")
    private String type;
}
