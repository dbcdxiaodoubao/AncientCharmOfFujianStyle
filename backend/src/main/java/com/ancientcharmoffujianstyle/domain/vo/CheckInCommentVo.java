package com.ancientcharmoffujianstyle.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("打卡评论")
public class CheckInCommentVo {

    @ApiModelProperty("评论id")
    private Long id;

    @ApiModelProperty("打卡记录id")
    private Long checkinId;

    @ApiModelProperty("评论用户id")
    private Long userId;

    @ApiModelProperty("评论用户名")
    private String userName;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("评论时间")
    private LocalDateTime createTime;
}
