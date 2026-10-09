package com.ancientcharmoffujianstyle.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("打卡信息列表")
public class CheckInListVo {

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
    @ApiModelProperty("非遗项目ID")
    private Long fyId;
    @ApiModelProperty("打卡热度指数，值越高表示该非遗越受欢迎")
    private Integer heatIndex;
    @ApiModelProperty("点赞数")
    private Integer likeCount;
    @ApiModelProperty("评论数")
    private Integer commentCount;
}
