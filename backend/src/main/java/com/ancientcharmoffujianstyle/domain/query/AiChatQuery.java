package com.ancientcharmoffujianstyle.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("AI对话请求")
public class AiChatQuery {

    @ApiModelProperty("用户id，可为空（游客）")
    private Long userId;

    @ApiModelProperty("用户本轮提问内容")
    private String message;

    @ApiModelProperty("历史对话（不含本轮问题），可为空")
    private List<AiMessage> history;
}
