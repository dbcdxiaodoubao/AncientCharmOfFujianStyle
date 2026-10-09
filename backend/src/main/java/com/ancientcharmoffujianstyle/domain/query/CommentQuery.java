package com.ancientcharmoffujianstyle.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("发表评论请求")
public class CommentQuery {

    @ApiModelProperty("打卡记录id")
    private Long checkinId;

    @ApiModelProperty("评论用户id")
    private Long userId;

    @ApiModelProperty("评论内容")
    private String content;
}
