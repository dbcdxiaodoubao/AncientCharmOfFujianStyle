package com.ancientcharmoffujianstyle.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("打卡详情")
public class CheckInDtlVo {

    @ApiModelProperty("打卡id")
    private Long id;
    @ApiModelProperty("发布者用户名")
    private String userName;
    @ApiModelProperty("图片地址")
    private String pictureUrl;
    @ApiModelProperty("文本信息")
    private String txt;
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("非遗名称")
    private String fyName;
    @ApiModelProperty("所属非遗项目ID")
    private Long fyId;
    @ApiModelProperty("该非遗项目的总打卡人次")
    private Integer visitCount;
}
