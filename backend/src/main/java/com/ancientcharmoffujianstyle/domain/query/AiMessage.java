package com.ancientcharmoffujianstyle.domain.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("AI对话消息")
public class AiMessage {

    @ApiModelProperty("角色，user 或 assistant")
    private String role;

    @ApiModelProperty("消息内容")
    private String content;
}
